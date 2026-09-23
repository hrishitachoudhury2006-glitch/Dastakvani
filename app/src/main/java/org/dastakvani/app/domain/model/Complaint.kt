package org.dastakvani.app.domain.model

data class ComplaintLocation(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "ग्रामीण क्षेत्र (Rural Area)",
    val district: String = "Madhubani"
)

data class Complaint(
    val id: String,
    val transcript: String,
    val category: ComplaintCategory,
    val confidence: Float,
    val location: ComplaintLocation,
    val citizenName: String? = null,
    val phoneNumber: String? = null,
    val status: ComplaintStatus = ComplaintStatus.SUBMITTED,
    val timestamp: Long = System.currentTimeMillis(),
    val routedToGov: String = "",
    val routedToNgo: String = "",
    val isEscalated: Boolean = false,
    val escalationRtiDraft: String? = null,
    val volunteerNotes: String? = null,
    val assignedVolunteerId: String? = null,
    val assignedVolunteerName: String? = null
) {
    /**
     * Anonymized public representation for the Village Wall.
     * In compliance with Section 25 (Privacy) & Section 26 (Child Safety):
     * - Citizen name is masked or omitted.
     * - Phone number is completely redacted.
     * - High-risk child safety complaints conceal exact street specifics.
     */
    fun getAnonymizedView(): Complaint {
        val safeName = if (!citizenName.isNullOrBlank()) {
            val firstLetter = citizenName.firstOrNull() ?: 'N'
            "नागरिक $firstLetter***"
        } else {
            "ग्रामीण नागरिक"
        }

        val safeAddress = if (category == ComplaintCategory.CHILD_SAFETY) {
            "जिला ${location.district} (गोपनीय क्षेत्र)"
        } else {
            location.address
        }

        return this.copy(
            citizenName = safeName,
            phoneNumber = null, // completely redacted
            location = location.copy(address = safeAddress)
        )
    }

    /**
     * Formats masked phone number for authorized display: e.g. XXXXXX1234
     */
    fun getMaskedPhoneNumber(): String {
        val num = phoneNumber ?: return "अनुपलब्ध (Not provided)"
        val digits = num.filter { it.isDigit() }
        return if (digits.length >= 4) {
            "XXXXXX" + digits.takeLast(4)
        } else {
            "XXXXXX"
        }
    }
}
