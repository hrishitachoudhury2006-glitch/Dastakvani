package org.dastakvani.app.domain.model

enum class ComplaintStatus(
    val id: String,
    val titleHi: String,
    val titleEn: String
) {
    SUBMITTED("submitted", "दर्ज हुआ (Submitted)", "Submitted"),
    ACKNOWLEDGED("acknowledged", "स्वीकृत हुआ (Acknowledged)", "Acknowledged"),
    IN_PROGRESS("in_progress", "कार्य प्रगति पर (In Progress)", "In Progress"),
    RESOLVED("resolved", "समाधान पूर्ण (Resolved)", "Resolved"),
    ESCALATED("escalated", "विधिक अपील / RTI (Escalated)", "Escalated to RTI");

    fun getNextStatus(): ComplaintStatus? {
        return when (this) {
            SUBMITTED -> ACKNOWLEDGED
            ACKNOWLEDGED -> IN_PROGRESS
            IN_PROGRESS -> RESOLVED
            RESOLVED -> null
            ESCALATED -> RESOLVED
        }
    }
}
