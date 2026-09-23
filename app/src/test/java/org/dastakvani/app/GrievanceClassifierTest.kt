package org.dastakvani.app

import org.dastakvani.app.data.ml.GrievanceClassifier
import org.dastakvani.app.domain.model.ComplaintCategory
import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Test
import java.io.File

class GrievanceClassifierTest {

    companion object {
        @BeforeClass
        @JvmStatic
        fun setupClassifier() {
            // Load the actual model JSON exported from our training pipeline
            val modelFile = File("src/main/assets/dastakvani_ml_model.json")
            val altModelFile = File("app/src/main/assets/dastakvani_ml_model.json")
            val targetFile = if (modelFile.exists()) modelFile else altModelFile

            assertTrue("Trained ML model JSON must exist in assets", targetFile.exists())
            val jsonContent = targetFile.readText(Charsets.UTF_8)
            GrievanceClassifier.initializeFromJson(jsonContent)
        }
    }

    @Test
    fun testClassifiesEducationComplaint() {
        val query = "मिड डे मील में कीड़ा निकला और शिक्षक स्कूल नहीं आ रहे हैं"
        val result = GrievanceClassifier.classify(query)

        assertEquals(ComplaintCategory.EDUCATION, result.category)
        assertTrue("Confidence should be meaningful (>40%)", result.confidence >= 0.40f)
        assertFalse("Should be actual ML inference, not fallback", result.isRuleBasedFallback)
    }

    @Test
    fun testClassifiesCivicNeglectComplaint() {
        val query = "Potholes on main road and street light is not working"
        val result = GrievanceClassifier.classify(query)

        assertEquals(ComplaintCategory.CIVIC_NEGLECT, result.category)
        assertTrue("Confidence should be high (>60%)", result.confidence >= 0.60f)
    }

    @Test
    fun testClassifiesChildSafetyComplaint() {
        val query = "छोटे बच्चे से ईंट भट्ठे पर जबरन मजदूरी कराई जा रही है"
        val result = GrievanceClassifier.classify(query)

        assertEquals(ComplaintCategory.CHILD_SAFETY, result.category)
        assertTrue("Child safety confidence should be high", result.confidence >= 0.60f)
    }

    @Test
    fun testClassifiesWelfareComplaint() {
        val query = "राशन डीलर राशन काट लेता है और वृद्धा पेंशन भी बैंक में नहीं आई"
        val result = GrievanceClassifier.classify(query)

        assertEquals(ComplaintCategory.WELFARE, result.category)
        assertTrue("Welfare confidence should be meaningful", result.confidence >= 0.50f)
    }
}
