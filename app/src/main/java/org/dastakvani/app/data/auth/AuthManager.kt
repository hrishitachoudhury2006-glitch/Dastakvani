package org.dastakvani.app.data.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class VolunteerUser(
    val id: String,
    val name: String,
    val email: String,
    val organization: String,
    val assignedDistrict: String,
    val role: String = "VOLUNTEER_NGO"
)

object AuthManager {

    private val _currentUser = MutableStateFlow<VolunteerUser?>(null)
    val currentUser: StateFlow<VolunteerUser?> = _currentUser.asStateFlow()

    val isAuthenticated: Boolean
        get() = _currentUser.value != null

    /**
     * Demo verified volunteer/NGO credentials for local testing.
     * When Firebase is linked, this integrates with FirebaseAuth.
     */
    fun login(email: String, pass: String): Result<VolunteerUser> {
        val cleanEmail = email.trim().lowercase()
        val cleanPass = pass.trim()

        if (cleanEmail == "volunteer@biharngo.org" && cleanPass == "dastak123") {
            val user = VolunteerUser(
                id = "vol_bihar_001",
                name = "आरती कुमारी (Aarti Kumari)",
                email = cleanEmail,
                organization = "प्रथम बाल सुरक्षा नेटवर्क (Pratham Network)",
                assignedDistrict = "Madhubani"
            )
            _currentUser.value = user
            return Result.success(user)
        }

        if (cleanEmail == "officer@biharngo.org" && cleanPass == "dastak123") {
            val user = VolunteerUser(
                id = "ngo_officer_002",
                name = "संजय कुमार (Sanjay Kumar)",
                email = cleanEmail,
                organization = "ग्राम वाणी नागरिक पहल (Gram Vaani)",
                assignedDistrict = "Darbhanga"
            )
            _currentUser.value = user
            return Result.success(user)
        }

        return Result.failure(IllegalArgumentException("गलत ईमेल या पासवर्ड (Invalid email or password). कृपया सही क्रेडेंशियल दर्ज करें।"))
    }

    fun logout() {
        _currentUser.value = null
    }
}
