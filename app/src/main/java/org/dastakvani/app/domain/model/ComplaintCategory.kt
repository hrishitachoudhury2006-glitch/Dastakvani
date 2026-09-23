package org.dastakvani.app.domain.model

enum class ComplaintCategory(
    val id: String,
    val displayNameHi: String,
    val displayNameEn: String,
    val isUrgentHighRisk: Boolean
) {
    CHILD_SAFETY(
        id = "child_safety",
        displayNameHi = "बाल सुरक्षा (Child Safety)",
        displayNameEn = "Child Safety",
        isUrgentHighRisk = true
    ),
    CIVIC_NEGLECT(
        id = "civic_neglect",
        displayNameHi = "नागरिक उपेक्षा / बुनियादी ढांचा (Civic Neglect)",
        displayNameEn = "Civic Neglect",
        isUrgentHighRisk = false
    ),
    EDUCATION(
        id = "education",
        displayNameHi = "शिक्षा / स्कूल (Education)",
        displayNameEn = "Education",
        isUrgentHighRisk = false
    ),
    WELFARE(
        id = "welfare",
        displayNameHi = "कल्याण / राशन / पेंशन (Welfare)",
        displayNameEn = "Welfare",
        isUrgentHighRisk = false
    );

    companion object {
        fun fromModelLabel(label: String): ComplaintCategory {
            return when (label.trim()) {
                "Child Safety" -> CHILD_SAFETY
                "Civic Neglect" -> CIVIC_NEGLECT
                "Education" -> EDUCATION
                "Welfare" -> WELFARE
                else -> CIVIC_NEGLECT
            }
        }
    }
}
