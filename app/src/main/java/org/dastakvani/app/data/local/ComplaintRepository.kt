package org.dastakvani.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.dastakvani.app.domain.escalation.RtiGenerator
import org.dastakvani.app.domain.model.Complaint
import org.dastakvani.app.domain.model.ComplaintCategory
import org.dastakvani.app.domain.model.ComplaintLocation
import org.dastakvani.app.domain.model.ComplaintStatus
import org.dastakvani.app.domain.routing.RoutingEngine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

object ComplaintRepository {

    private const val PREFS_NAME = "dastak_vani_complaints"
    private const val KEY_COMPLAINTS = "saved_complaints_json"

    private lateinit var prefs: SharedPreferences
    private val gson = Gson()
    private val counter = AtomicInteger(100)

    private val _complaintsFlow = MutableStateFlow<List<Complaint>>(emptyList())
    val complaintsFlow: StateFlow<List<Complaint>> = _complaintsFlow.asStateFlow()

    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val loaded = loadComplaintsFromStorage()
        if (loaded.isEmpty()) {
            val initial = createInitialSeedComplaints()
            saveComplaintsToStorage(initial)
            _complaintsFlow.value = initial
        } else {
            _complaintsFlow.value = loaded
        }
    }

    /**
     * Generates a unique, human-readable Complaint ID:
     * e.g., DV-20260924-4521
     */
    fun generateComplaintId(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val datePart = dateFormat.format(Date())
        val randomPart = (1000..9999).random()
        return "DV-$datePart-$randomPart"
    }

    fun submitComplaint(
        transcript: String,
        category: ComplaintCategory,
        confidence: Float,
        location: ComplaintLocation,
        citizenName: String? = null,
        phoneNumber: String? = null
    ): Complaint {
        val complaintId = generateComplaintId()
        val routing = RoutingEngine.resolveRouting(category, location.district)

        val newComplaint = Complaint(
            id = complaintId,
            transcript = transcript,
            category = category,
            confidence = confidence,
            location = location,
            citizenName = citizenName,
            phoneNumber = phoneNumber,
            status = ComplaintStatus.SUBMITTED,
            timestamp = System.currentTimeMillis(),
            routedToGov = routing.departmentName,
            routedToNgo = routing.ngoName,
            isEscalated = false,
            escalationRtiDraft = null
        )

        val updated = listOf(newComplaint) + _complaintsFlow.value
        _complaintsFlow.value = updated
        saveComplaintsToStorage(updated)
        return newComplaint
    }

    fun getComplaintById(id: String): Complaint? {
        val cleanId = id.trim().uppercase()
        return _complaintsFlow.value.find { it.id.equals(cleanId, ignoreCase = true) }
    }

    /**
     * Returns public anonymized complaints for the Village Wall.
     */
    fun getPublicAnonymizedComplaints(): List<Complaint> {
        return _complaintsFlow.value.map { it.getAnonymizedView() }
    }

    /**
     * Updates complaint status (called by authorized volunteers / NGOs).
     */
    fun updateStatus(
        complaintId: String,
        newStatus: ComplaintStatus,
        volunteerId: String,
        volunteerName: String,
        notes: String? = null
    ): Boolean {
        val currentList = _complaintsFlow.value
        val index = currentList.indexOfFirst { it.id == complaintId }
        if (index == -1) return false

        val existing = currentList[index]
        val updated = existing.copy(
            status = newStatus,
            assignedVolunteerId = volunteerId,
            assignedVolunteerName = volunteerName,
            volunteerNotes = notes ?: existing.volunteerNotes
        )

        val newList = currentList.toMutableList().apply { set(index, updated) }
        _complaintsFlow.value = newList
        saveComplaintsToStorage(newList)
        return true
    }

    /**
     * Checks for complaints that have surpassed the legal window (> 15 days)
     * and automatically generates legal RTI applications.
     */
    fun runEscalationCheck(): Int {
        val currentList = _complaintsFlow.value
        var escalatedCount = 0
        val fifteenDaysMs = 15L * 24 * 60 * 60 * 1000

        val newList = currentList.map { complaint ->
            val isOverdue = (System.currentTimeMillis() - complaint.timestamp) > fifteenDaysMs
            if (isOverdue && complaint.status == ComplaintStatus.SUBMITTED && !complaint.isEscalated) {
                escalatedCount++
                val rti = RtiGenerator.generateRtiDraft(complaint)
                complaint.copy(
                    status = ComplaintStatus.ESCALATED,
                    isEscalated = true,
                    escalationRtiDraft = rti
                )
            } else {
                complaint
            }
        }

        if (escalatedCount > 0) {
            _complaintsFlow.value = newList
            saveComplaintsToStorage(newList)
        }
        return escalatedCount
    }

    private fun loadComplaintsFromStorage(): List<Complaint> {
        return try {
            val json = prefs.getString(KEY_COMPLAINTS, null) ?: return emptyList()
            val type = object : TypeToken<List<Complaint>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveComplaintsToStorage(complaints: List<Complaint>) {
        try {
            val json = gson.toJson(complaints)
            prefs.edit().putString(KEY_COMPLAINTS, json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createInitialSeedComplaints(): List<Complaint> {
        val now = System.currentTimeMillis()
        val day = 24L * 60 * 60 * 1000

        val c1 = Complaint(
            id = "DV-20260920-4521",
            transcript = "हमारे गांव के प्राथमिक विद्यालय में पिछले दो महीने से मिड डे मील नहीं बन रहा है और शिक्षक अनुपस्थित रहते हैं।",
            category = ComplaintCategory.EDUCATION,
            confidence = 0.91f,
            location = ComplaintLocation(26.3547, 86.0712, "प्राथमिक विद्यालय, रहिका", "Madhubani"),
            citizenName = "सीता देवी",
            phoneNumber = "9876543210",
            status = ComplaintStatus.IN_PROGRESS,
            timestamp = now - (3 * day),
            routedToGov = "जिला शिक्षा पदाधिकारी (DEO), Madhubani",
            routedToNgo = "प्रथम एजुकेशन फाउंडेशन, Madhubani",
            assignedVolunteerName = "आरती कुमारी (Aarti Kumari)",
            volunteerNotes = "स्कूल का औचक निरीक्षण किया गया। बीईओ को नोटिस भेजा गया है।"
        )

        val c2 = Complaint(
            id = "DV-20260905-1829",
            transcript = "वार्ड नंबर 4 की मुख्य सड़क और नाली का पानी बहकर घरों में घुस रहा है, ट्रांसफार्मर भी जला हुआ है।",
            category = ComplaintCategory.CIVIC_NEGLECT,
            confidence = 0.88f,
            location = ComplaintLocation(26.1542, 85.8918, "वार्ड नं 4, बहादुरपुर", "Darbhanga"),
            citizenName = "राम प्रसाद",
            phoneNumber = "9123456780",
            status = ComplaintStatus.ESCALATED,
            timestamp = now - (19 * day),
            routedToGov = "कार्यपालक अभियंता (RWD/PHED), Darbhanga",
            routedToNgo = "ग्राम वाणी नागरिक पहल, Darbhanga",
            isEscalated = true,
            escalationRtiDraft = RtiGenerator.generateRtiDraft(
                Complaint(
                    id = "DV-20260905-1829",
                    transcript = "वार्ड नंबर 4 की मुख्य सड़क और नाली का पानी बहकर घरों में घुस रहा है, ट्रांसफार्मर भी जला हुआ है।",
                    category = ComplaintCategory.CIVIC_NEGLECT,
                    confidence = 0.88f,
                    location = ComplaintLocation(26.1542, 85.8918, "वार्ड नं 4, बहादुरपुर", "Darbhanga"),
                    status = ComplaintStatus.ESCALATED,
                    timestamp = now - (19 * day)
                )
            )
        )

        val c3 = Complaint(
            id = "DV-20260922-8314",
            transcript = "राशन डीलर हर कार्ड पर दो किलो राशन काट लेता है और अंगूठा लगवा कर राशन नहीं देता।",
            category = ComplaintCategory.WELFARE,
            confidence = 0.84f,
            location = ComplaintLocation(26.3621, 86.0845, "मुसहरी टोला, पंडौल", "Madhubani"),
            citizenName = "मंगली देवी",
            phoneNumber = "9431209876",
            status = ComplaintStatus.ACKNOWLEDGED,
            timestamp = now - (1 * day),
            routedToGov = "जिला आपूर्ति पदाधिकारी (DSO), Madhubani",
            routedToNgo = "हकदर्शक जवाबदेही अभियान, Madhubani"
        )

        val c4 = Complaint(
            id = "DV-20260923-9042",
            transcript = "पास के ईंट भट्ठे पर 10 साल के मासूम बच्चे से जबरन मजदूरी कराई जा रही है और मारा पीटा जाता है।",
            category = ComplaintCategory.CHILD_SAFETY,
            confidence = 0.94f,
            location = ComplaintLocation(26.1200, 85.9000, "ईंट भट्ठा क्षेत्र, सकरी", "Darbhanga"),
            citizenName = "नागरिक (गोपनीय)",
            phoneNumber = "9800112233",
            status = ComplaintStatus.SUBMITTED,
            timestamp = now - (4 * 3600 * 1000), // 4 hours ago
            routedToGov = "जिला बाल संरक्षण इकाई (DCPU), Darbhanga",
            routedToNgo = "बचपन बचाओ नेटवर्क, Darbhanga"
        )

        return listOf(c4, c3, c1, c2)
    }
}
