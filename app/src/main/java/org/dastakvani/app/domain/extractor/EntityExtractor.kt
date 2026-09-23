package org.dastakvani.app.domain.extractor

import java.util.regex.Pattern

data class ExtractedEntities(
    val detectedName: String? = null,
    val detectedPhone: String? = null,
    val detectedVillage: String? = null,
    val detectedKeywords: List<String> = emptyList()
)

object EntityExtractor {

    // Regex for 10-digit Indian phone numbers with optional +91 or 0 prefix
    private val PHONE_PATTERN = Pattern.compile(
        "(?:(?:\\+?91)|0)?[ -]?[6-9]\\d{4}[ -]?\\d{5}\\b"
    )

    // Name extraction patterns in Hindi & English
    private val HINDI_NAME_PATTERNS = listOf(
        Pattern.compile("(?:मेरा नाम|हमार नाम|नाम)\\s+([\\u0900-\\u097F]+(?:\\s+[\\u0900-\\u097F]+)?)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?:मैं|हमार)\\s+([\\u0900-\\u097F]+)\\s+(?:बोल रहा|बोल रही|बोल रहल|हूँ)", Pattern.CASE_INSENSITIVE)
    )

    private val ENGLISH_NAME_PATTERNS = listOf(
        Pattern.compile("(?:my name is|i am|this is)\\s+([a-zA-Z]+(?:\\s+[a-zA-Z]+)?)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("([a-zA-Z]+)\\s+(?:speaking|here)", Pattern.CASE_INSENSITIVE)
    )

    // Village / Locality patterns
    private val VILLAGE_PATTERNS = listOf(
        Pattern.compile("(?:गांव|गाँव|ग्राम|पंचायत|टोला|मोहल्ला|वार्ड)\\s+([\\u0900-\\u097Fa-zA-Z]+)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?:in village|from village|at panchayat|in ward)\\s+([a-zA-Z]+)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("([\\u0900-\\u097Fa-zA-Z]+)\\s+(?:गांव|गाँव|पंचायत)", Pattern.CASE_INSENSITIVE)
    )

    fun extract(text: String): ExtractedEntities {
        if (text.isBlank()) return ExtractedEntities()

        val detectedPhone = extractPhone(text)
        val detectedName = extractName(text)
        val detectedVillage = extractVillage(text)
        val detectedKeywords = extractKeywords(text)

        return ExtractedEntities(
            detectedName = detectedName,
            detectedPhone = detectedPhone,
            detectedVillage = detectedVillage,
            detectedKeywords = detectedKeywords
        )
    }

    private fun extractPhone(text: String): String? {
        val matcher = PHONE_PATTERN.matcher(text)
        if (matcher.find()) {
            val raw = matcher.group()
            val digits = raw.filter { it.isDigit() }
            return if (digits.length >= 10) {
                digits.takeLast(10)
            } else null
        }
        return null
    }

    private fun extractName(text: String): String? {
        for (pattern in HINDI_NAME_PATTERNS) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val candidate = matcher.group(1)?.trim()
                if (isValidNameCandidate(candidate)) {
                    return candidate
                }
            }
        }
        for (pattern in ENGLISH_NAME_PATTERNS) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val candidate = matcher.group(1)?.trim()
                if (isValidNameCandidate(candidate)) {
                    return candidate
                }
            }
        }
        return null
    }

    private fun extractVillage(text: String): String? {
        for (pattern in VILLAGE_PATTERNS) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val candidate = matcher.group(1)?.trim()
                if (!candidate.isNullOrBlank() && candidate.length > 2 && !candidate.equals("के", true) && !candidate.equals("का", true)) {
                    return candidate
                }
            }
        }
        return null
    }

    private fun isValidNameCandidate(candidate: String?): Boolean {
        if (candidate.isNullOrBlank()) return false
        val words = candidate.split(" ")
        if (words.size > 3) return false
        val stopWords = setOf("एक", "का", "की", "के", "है", "था", "from", "the", "a", "complaint")
        return !stopWords.contains(candidate.lowercase())
    }

    private fun extractKeywords(text: String): List<String> {
        val civicTerms = listOf(
            "स्कूल", "शिक्षक", "मिड डे मील", "शौचालय", "सड़क", "नाली", "बिजली", "ट्रांसफार्मर",
            "पानी", "चापाकल", "राशन", "पेंशन", "मनरेगा", "जॉब कार्ड", "आवास", "पर्चा",
            "बाल मजदूरी", "बाल विवाह", "तस्करी", "बच्चा",
            "school", "teacher", "toilet", "road", "pothole", "transformer", "water",
            "pension", "ration", "mgnrega", "child labor", "trafficking"
        )
        val lower = text.lowercase()
        return civicTerms.filter { lower.contains(it) }
    }
}
