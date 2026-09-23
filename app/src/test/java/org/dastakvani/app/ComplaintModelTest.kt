package org.dastakvani.app

import org.dastakvani.app.domain.model.Complaint
import org.dastakvani.app.domain.model.ComplaintCategory
import org.dastakvani.app.domain.model.ComplaintLocation
import org.dastakvani.app.domain.model.ComplaintStatus
import org.junit.Assert.*
import org.junit.Test

class ComplaintModelTest {

    @Test
    fun testAnonymizedViewConcealsCitizenPrivateData() {
        val complaint = Complaint(
            id = "DV-20260924-1234",
            transcript = "नाली का पानी सड़क पर बह रहा है",
            category = ComplaintCategory.CIVIC_NEGLECT,
            confidence = 0.90f,
            location = ComplaintLocation(26.12, 85.90, "वार्ड 3, सकरी", "Darbhanga"),
            citizenName = "राम कुमार",
            phoneNumber = "9876543210",
            status = ComplaintStatus.SUBMITTED
        )

        val anonymized = complaint.getAnonymizedView()

        // 1. Phone number must be completely null in public view
        assertNull("Phone number must be redacted in public view", anonymized.phoneNumber)

        // 2. Citizen name must be masked
        assertTrue("Citizen name must be masked", anonymized.citizenName?.contains("***") == true)

        // 3. Masked phone number helper format
        assertEquals("XXXXXX3210", complaint.getMaskedPhoneNumber())
    }

    @Test
    fun testChildSafetyConcealsPreciseStreetLocation() {
        val childSafetyComplaint = Complaint(
            id = "DV-20260924-9999",
            transcript = "ईंट भट्ठे पर बच्चे से मजदूरी कराई जा रही है",
            category = ComplaintCategory.CHILD_SAFETY,
            confidence = 0.95f,
            location = ComplaintLocation(26.12, 85.90, "सकरी मुख्य बाजार, घर संख्या 42", "Darbhanga"),
            citizenName = "गोपनीय नागरिक",
            phoneNumber = "9876500000"
        )

        val anonymized = childSafetyComplaint.getAnonymizedView()

        // For child safety, exact street specifics must be suppressed
        assertTrue(
            "Child safety public location must be protected",
            anonymized.location.address.contains("गोपनीय क्षेत्र")
        )
    }
}
