package org.dastakvani.app

import org.dastakvani.app.domain.escalation.RtiGenerator
import org.dastakvani.app.domain.model.Complaint
import org.dastakvani.app.domain.model.ComplaintCategory
import org.dastakvani.app.domain.model.ComplaintLocation
import org.dastakvani.app.domain.model.ComplaintStatus
import org.junit.Assert.*
import org.junit.Test

class RtiGeneratorTest {

    @Test
    fun testGeneratesStructuredRtiDraft() {
        val complaint = Complaint(
            id = "DV-20260901-1001",
            transcript = "हमारे गांव की सड़क टूटी हुई है और पानी भरा है",
            category = ComplaintCategory.CIVIC_NEGLECT,
            confidence = 0.92f,
            location = ComplaintLocation(26.12, 85.90, "वार्ड 2", "Madhubani"),
            status = ComplaintStatus.ESCALATED
        )

        val rti = RtiGenerator.generateRtiDraft(complaint)

        assertTrue("RTI draft must contain Section 6(1) citation", rti.contains("धारा 6(1)"))
        assertTrue("RTI draft must reference complaint ID", rti.contains("DV-20260901-1001"))
        assertTrue("RTI draft must state 30 days statutory window", rti.contains("30 दिवस"))
        assertTrue("RTI draft must address Public Information Officer", rti.contains("लोक सूचना पदाधिकारी"))
    }
}
