package org.dastakvani.app.data.ml

import android.content.Context
import org.dastakvani.app.domain.model.ComplaintCategory
import org.json.JSONObject
import java.io.InputStreamReader
import java.util.regex.Pattern
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sqrt

data class ClassificationResult(
    val category: ComplaintCategory,
    val confidence: Float,
    val classProbabilities: Map<ComplaintCategory, Float>,
    val isRuleBasedFallback: Boolean = false,
    val inferenceTimeMs: Long = 0
)

object GrievanceClassifier {

    private var isInitialized = false
    private var classes = listOf<String>()
    private var vocabulary = mapOf<String, Int>()
    private var idf = doubleArrayOf()
    private var coef = arrayOf<DoubleArray>()
    private var intercept = doubleArrayOf()

    // Regex for complete Devanagari and English alphanumeric tokens
    private val TOKEN_PATTERN = Pattern.compile("[a-zA-Z0-9\\u0900-\\u097F]+")

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            val assetManager = context.assets
            val inputStream = assetManager.open("dastakvani_ml_model.json")
            val reader = InputStreamReader(inputStream, "UTF-8")
            val jsonString = reader.readText()
            reader.close()
            initializeFromJson(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            isInitialized = false
        }
    }

    fun initializeFromJson(jsonString: String) {
        try {
            val root = JSONObject(jsonString)

            // 1. Classes
            val classesJson = root.getJSONArray("classes")
            val classList = mutableListOf<String>()
            for (i in 0 until classesJson.length()) {
                classList.add(classesJson.getString(i))
            }
            classes = classList

            // 2. Vocabulary
            val vocabJson = root.getJSONObject("vocabulary")
            val vocabMap = mutableMapOf<String, Int>()
            val keys = vocabJson.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                vocabMap[key] = vocabJson.getInt(key)
            }
            vocabulary = vocabMap

            // 3. IDF
            val idfJson = root.getJSONArray("idf")
            val idfArray = DoubleArray(idfJson.length())
            for (i in 0 until idfJson.length()) {
                idfArray[i] = idfJson.getDouble(i)
            }
            idf = idfArray

            // 4. Coefficients (matrix of n_classes x n_features)
            val coefJson = root.getJSONArray("coef")
            val coefMatrix = Array(coefJson.length()) { i ->
                val rowJson = coefJson.getJSONArray(i)
                val row = DoubleArray(rowJson.length())
                for (j in 0 until rowJson.length()) {
                    row[j] = rowJson.getDouble(j)
                }
                row
            }
            coef = coefMatrix

            // 5. Intercepts
            val interceptJson = root.getJSONArray("intercept")
            val interceptArray = DoubleArray(interceptJson.length())
            for (i in 0 until interceptJson.length()) {
                interceptArray[i] = interceptJson.getDouble(i)
            }
            intercept = interceptArray

            isInitialized = true
        } catch (e: Exception) {
            e.printStackTrace()
            isInitialized = false
        }
    }

    /**
     * Executes real on-device vectorized ML inference matching the scikit-learn pipeline.
     */
    fun classify(text: String): ClassificationResult {
        val startTime = System.currentTimeMillis()

        if (!isInitialized || vocabulary.isEmpty()) {
            return ruleBasedFallback(text, System.currentTimeMillis() - startTime)
        }

        val cleaned = normalize(text)
        val tokens = extractTokens(cleaned)
        if (tokens.isEmpty()) {
            return ruleBasedFallback(text, System.currentTimeMillis() - startTime)
        }

        // 1. Extract unigrams and bigrams
        val ngrams = mutableListOf<String>()
        ngrams.addAll(tokens)
        for (i in 0 until tokens.size - 1) {
            ngrams.add("${tokens[i]} ${tokens[i + 1]}")
        }

        // 2. Count term frequencies for known vocabulary
        val termCounts = mutableMapOf<Int, Int>()
        for (ng in ngrams) {
            val featureIdx = vocabulary[ng]
            if (featureIdx != null) {
                termCounts[featureIdx] = (termCounts[featureIdx] ?: 0) + 1
            }
        }

        if (termCounts.isEmpty()) {
            return ruleBasedFallback(text, System.currentTimeMillis() - startTime)
        }

        // 3. Compute TF-IDF vector with sublinear scaling: (1 + ln(count)) * idf
        val tfIdfVector = mutableMapOf<Int, Double>()
        var sumSquares = 0.0
        for ((idx, count) in termCounts) {
            val tf = 1.0 + ln(count.toDouble())
            val score = tf * idf[idx]
            tfIdfVector[idx] = score
            sumSquares += score * score
        }

        // L2 Normalization
        val norm = sqrt(sumSquares)
        if (norm > 0.0) {
            for (idx in tfIdfVector.keys.toList()) {
                tfIdfVector[idx] = tfIdfVector[idx]!! / norm
            }
        }

        // 4. Compute Logits = DotProduct(x, coef[c]) + intercept[c]
        val logits = DoubleArray(classes.size)
        var maxLogit = Double.NEGATIVE_INFINITY

        for (c in classes.indices) {
            var dot = intercept[c]
            val cWeights = coef[c]
            for ((featureIdx, featureVal) in tfIdfVector) {
                if (featureIdx < cWeights.size) {
                    dot += featureVal * cWeights[featureIdx]
                }
            }
            logits[c] = dot
            if (dot > maxLogit) {
                maxLogit = dot
            }
        }

        // 5. Softmax Probabilities
        val expLogits = DoubleArray(classes.size)
        var sumExp = 0.0
        for (c in classes.indices) {
            val expVal = exp(logits[c] - maxLogit)
            expLogits[c] = expVal
            sumExp += expVal
        }

        val probabilities = mutableMapOf<ComplaintCategory, Float>()
        var bestIdx = 0
        var highestProb = 0.0

        for (c in classes.indices) {
            val prob = if (sumExp > 0.0) (expLogits[c] / sumExp).toFloat() else 0.25f
            val category = ComplaintCategory.fromModelLabel(classes[c])
            probabilities[category] = prob
            if (prob > highestProb) {
                highestProb = prob.toDouble()
                bestIdx = c
            }
        }

        val duration = System.currentTimeMillis() - startTime
        val predictedCategory = ComplaintCategory.fromModelLabel(classes[bestIdx])

        // If confidence is lower than 30%, apply rule-based safety verification
        if (highestProb < 0.30) {
            val fallback = ruleBasedFallback(text, duration)
            if (fallback.confidence > highestProb.toFloat()) {
                return fallback
            }
        }

        return ClassificationResult(
            category = predictedCategory,
            confidence = highestProb.toFloat(),
            classProbabilities = probabilities,
            isRuleBasedFallback = false,
            inferenceTimeMs = duration
        )
    }

    private fun normalize(text: String): String {
        var t = text.lowercase()
        t = t.replace(Regex("[\\r\\n\\t]+"), " ")
        t = t.replace(Regex("[^\\w\\s\\u0900-\\u097F]"), " ")
        return t.trim()
    }

    private fun extractTokens(text: String): List<String> {
        val matcher = TOKEN_PATTERN.matcher(text)
        val tokens = mutableListOf<String>()
        while (matcher.find()) {
            tokens.add(matcher.group())
        }
        return tokens
    }

    /**
     * Explicit rule-based fallback safety net in compliance with Section 20.
     */
    private fun ruleBasedFallback(text: String, duration: Long): ClassificationResult {
        val lower = text.lowercase()

        val childSafetyKeywords = listOf("बच्चा", "मासूम", "बाल", "तस्करी", "मजदूरी", "नाबालिग", "दुकान पर काम", "भट्ठा", "ईंट", "trafficking", "child", "labor", "minor", "abuse")
        val educationKeywords = listOf("स्कूल", "शिक्षक", "मास्टर", "मिड डे मील", "शौचालय", "किताब", "पोशाक", "छात्रवृत्ति", "school", "teacher", "headmaster", "toilet", "midday")
        val welfareKeywords = listOf("राशन", "पेंशन", "मनरेगा", "जॉब कार्ड", "आवास", "पर्चा", "जमीन", "डीलर", "कोटा", "ration", "pension", "mgnrega", "patta", "awas")
        val civicKeywords = listOf("सड़क", "गड्ढा", "नाली", "बिजली", "ट्रांसफार्मर", "चापाकल", "कचरा", "स्ट्रीट लाइट", "पानी", "पाइप", "road", "pothole", "drainage", "water", "electricity", "garbage")

        val scores = mutableMapOf(
            ComplaintCategory.CHILD_SAFETY to childSafetyKeywords.count { lower.contains(it) },
            ComplaintCategory.EDUCATION to educationKeywords.count { lower.contains(it) },
            ComplaintCategory.WELFARE to welfareKeywords.count { lower.contains(it) },
            ComplaintCategory.CIVIC_NEGLECT to civicKeywords.count { lower.contains(it) }
        )

        val best = scores.maxByOrNull { it.value }
        val category = if (best != null && best.value > 0) best.key else ComplaintCategory.CIVIC_NEGLECT

        return ClassificationResult(
            category = category,
            confidence = 0.50f,
            classProbabilities = mapOf(category to 0.50f),
            isRuleBasedFallback = true,
            inferenceTimeMs = duration
        )
    }
}
