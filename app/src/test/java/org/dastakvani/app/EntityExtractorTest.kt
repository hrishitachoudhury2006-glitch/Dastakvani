package org.dastakvani.app

import org.dastakvani.app.domain.extractor.EntityExtractor
import org.junit.Assert.*
import org.junit.Test

class EntityExtractorTest {

    @Test
    fun testExtractsIndianPhoneNumber() {
        val hindiQuery = "मेरा नाम राम प्रसाद है और मोबाइल नंबर 9876543210 है, स्कूल में मास्टर नहीं आते"
        val entities = EntityExtractor.extract(hindiQuery)

        assertEquals("9876543210", entities.detectedPhone)
        assertNotNull("Name should be extracted", entities.detectedName)
    }

    @Test
    fun testExtractsVillageLocality() {
        val text = "पंचायत रहिका में चापाकल खराब पड़ा है"
        val entities = EntityExtractor.extract(text)

        assertEquals("रहिका", entities.detectedVillage)
    }

    @Test
    fun testHandlesMissingEntitiesGracefully() {
        val text = "सड़क पर गड्ढे हैं और पानी भरा है"
        val entities = EntityExtractor.extract(text)

        assertNull(entities.detectedPhone)
        assertNull(entities.detectedName)
        assertNull(entities.detectedVillage)
    }
}
