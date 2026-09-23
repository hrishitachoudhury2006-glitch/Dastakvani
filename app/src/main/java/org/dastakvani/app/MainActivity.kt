package org.dastakvani.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.dastakvani.app.data.local.ComplaintRepository
import org.dastakvani.app.data.location.LocationService
import org.dastakvani.app.data.ml.ClassificationResult
import org.dastakvani.app.data.ml.GrievanceClassifier
import org.dastakvani.app.data.speech.SpeechManager
import org.dastakvani.app.data.speech.SpeechState
import org.dastakvani.app.data.tts.TtsManager
import org.dastakvani.app.domain.extractor.EntityExtractor
import org.dastakvani.app.domain.extractor.ExtractedEntities
import org.dastakvani.app.domain.model.Complaint
import org.dastakvani.app.domain.model.ComplaintLocation
import org.dastakvani.app.presentation.screens.*
import org.dastakvani.app.presentation.theme.DastakVaniTheme

class MainActivity : ComponentActivity() {

    private lateinit var speechManager: SpeechManager
    private lateinit var ttsManager: TtsManager
    private lateinit var locationService: LocationService

    private var onAudioPermissionGranted: (() -> Unit)? = null

    private val audioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onAudioPermissionGranted?.invoke()
        } else {
            Toast.makeText(this, "माइक्रोफोन अनुमति आवश्यक है (Microphone permission required)", Toast.LENGTH_LONG).show()
        }
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* Location will be read via LocationService */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        speechManager = SpeechManager(this)
        ttsManager = TtsManager(this)
        locationService = LocationService(this)

        requestLocationPermissionsIfNecessary()

        setContent {
            DastakVaniTheme {
                val navController = rememberNavController()
                var isHindi by remember { mutableStateOf(true) }

                // Live Speech States
                val speechState by speechManager.speechState.collectAsState()
                val soundLevel by speechManager.soundLevel.collectAsState()

                // Active complaint drafting state
                var currentDraftTranscript by remember { mutableStateOf("") }
                var currentClassification by remember { mutableStateOf<ClassificationResult?>(null) }
                var currentEntities by remember { mutableStateOf(ExtractedEntities()) }
                var currentLocation by remember { mutableStateOf(ComplaintLocation()) }
                var lastSubmittedComplaint by remember { mutableStateOf<Complaint?>(null) }
                var selectedComplaintForRti by remember { mutableStateOf<Complaint?>(null) }

                val complaintsList by ComplaintRepository.complaintsFlow.collectAsState()
                val isSpeakingTts by ttsManager.isSpeaking.collectAsState()

                NavHost(navController = navController, startDestination = "home") {

                    // 1. HOME SCREEN
                    composable("home") {
                        HomeScreen(
                            isHindi = isHindi,
                            onToggleLanguage = { isHindi = !isHindi },
                            onSpeakInstruction = {
                                val text = if (isHindi) {
                                    "दस्तक वाणी में आपका स्वागत है। अपनी समस्या दर्ज कराने के लिए स्क्रीन के बीच में स्थित बड़े माइक बटन को दबाएं और स्पष्ट बोलें।"
                                } else {
                                    "Welcome to Dastak Vani. Tap the large microphone button in the center to speak your grievance."
                                }
                                ttsManager.speak(text)
                            },
                            onStartRecording = {
                                checkAudioPermissionAndStart {
                                    speechManager.startListening(if (isHindi) "hi-IN" else "en-IN")
                                    navController.navigate("record")
                                }
                            },
                            onNavigateToStatus = { navController.navigate("status_lookup") },
                            onNavigateToVillageWall = { navController.navigate("complaint_box") },
                            onNavigateToNgoPortal = { navController.navigate("ngo_dashboard") }
                        )
                    }

                    // 2. RECORD COMPLAINT SCREEN
                    composable("record") {
                        val transcript = when (val state = speechState) {
                            is SpeechState.PartialResult -> state.text
                            is SpeechState.FinalResult -> state.text
                            else -> currentDraftTranscript
                        }
                        val error = (speechState as? SpeechState.Error)?.errorMessage

                        RecordComplaintScreen(
                            isHindi = isHindi,
                            isListening = speechState is SpeechState.Listening || speechState is SpeechState.PartialResult,
                            soundLevel = soundLevel,
                            currentTranscript = transcript,
                            errorMessage = error,
                            onStopListening = { speechManager.stopListening() },
                            onCancel = {
                                speechManager.stopListening()
                                navController.popBackStack()
                            },
                            onDoneWithText = { finalTranscript ->
                                speechManager.stopListening()
                                currentDraftTranscript = finalTranscript

                                // 1. On-Device ML Inference
                                val classification = GrievanceClassifier.classify(finalTranscript)
                                currentClassification = classification

                                // 2. Entity Extraction (Name, Phone, Village)
                                currentEntities = EntityExtractor.extract(finalTranscript)

                                // 3. Real Location Fetch
                                lifecycleScope.launch {
                                    currentLocation = locationService.getCurrentLocation()

                                    // 4. Play Proof of Hearing Audio
                                    ttsManager.speakProofOfHearing(finalTranscript)

                                    navController.navigate("proof_of_hearing")
                                }
                            }
                        )
                    }

                    // 3. PROOF OF HEARING SCREEN
                    composable("proof_of_hearing") {
                        val classification = currentClassification ?: GrievanceClassifier.classify(currentDraftTranscript)
                        ProofOfHearingScreen(
                            isHindi = isHindi,
                            transcript = currentDraftTranscript,
                            classification = classification,
                            entities = currentEntities,
                            location = currentLocation,
                            isSpeakingTts = isSpeakingTts,
                            onReplayTts = {
                                ttsManager.speakProofOfHearing(currentDraftTranscript)
                            },
                            onConfirmAndSubmit = {
                                ttsManager.stop()
                                val submitted = ComplaintRepository.submitComplaint(
                                    transcript = currentDraftTranscript,
                                    category = classification.category,
                                    confidence = classification.confidence,
                                    location = currentLocation,
                                    citizenName = currentEntities.detectedName,
                                    phoneNumber = currentEntities.detectedPhone
                                )
                                lastSubmittedComplaint = submitted

                                val speechConfirm = if (isHindi) {
                                    "आपकी शिकायत दर्ज कर ली गई है। आपकी शिकायत संख्या है ${submitted.id}।"
                                } else {
                                    "Your grievance has been filed successfully. Your complaint ID is ${submitted.id}."
                                }
                                ttsManager.speak(speechConfirm)

                                navController.navigate("success") {
                                    popUpTo("home")
                                }
                            },
                            onReRecord = {
                                ttsManager.stop()
                                checkAudioPermissionAndStart {
                                    speechManager.startListening(if (isHindi) "hi-IN" else "en-IN")
                                    navController.navigate("record") {
                                        popUpTo("home")
                                    }
                                }
                            }
                        )
                    }

                    // 4. SUBMISSION SUCCESS SCREEN
                    composable("success") {
                        val complaint = lastSubmittedComplaint ?: complaintsList.firstOrNull()
                        if (complaint != null) {
                            SubmissionSuccessScreen(
                                isHindi = isHindi,
                                complaint = complaint,
                                onTrackComplaint = {
                                    navController.navigate("status_lookup/${complaint.id}")
                                },
                                onNavigateHome = {
                                    navController.navigate("home") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                },
                                onSpeakId = {
                                    val speech = if (isHindi) {
                                        "आपकी शिकायत संख्या है: ${complaint.id}"
                                    } else {
                                        "Your complaint ID is: ${complaint.id}"
                                    }
                                    ttsManager.speak(speech)
                                }
                            )
                        }
                    }

                    // 5. STATUS LOOKUP SCREEN (Supports direct query parameter)
                    composable("status_lookup") {
                        StatusLookupScreen(
                            isHindi = isHindi,
                            initialComplaintId = "",
                            onLookup = { id -> ComplaintRepository.getComplaintById(id) },
                            onVoiceSearch = {
                                checkAudioPermissionAndStart {
                                    speechManager.startListening(if (isHindi) "hi-IN" else "en-IN")
                                }
                            },
                            onSpeakStatus = { complaint ->
                                val statusText = if (isHindi) complaint.status.titleHi else complaint.status.titleEn
                                val categoryText = if (isHindi) complaint.category.displayNameHi else complaint.category.displayNameEn
                                ttsManager.speakStatus(complaint.id, statusText, categoryText)
                            },
                            onViewRtiDraft = { complaint ->
                                selectedComplaintForRti = complaint
                                navController.navigate("rti_draft")
                            },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable("status_lookup/{complaintId}") { backStackEntry ->
                        val initialId = backStackEntry.arguments?.getString("complaintId") ?: ""
                        StatusLookupScreen(
                            isHindi = isHindi,
                            initialComplaintId = initialId,
                            onLookup = { id -> ComplaintRepository.getComplaintById(id) },
                            onVoiceSearch = {
                                checkAudioPermissionAndStart {
                                    speechManager.startListening(if (isHindi) "hi-IN" else "en-IN")
                                }
                            },
                            onSpeakStatus = { complaint ->
                                val statusText = if (isHindi) complaint.status.titleHi else complaint.status.titleEn
                                val categoryText = if (isHindi) complaint.category.displayNameHi else complaint.category.displayNameEn
                                ttsManager.speakStatus(complaint.id, statusText, categoryText)
                            },
                            onViewRtiDraft = { complaint ->
                                selectedComplaintForRti = complaint
                                navController.navigate("rti_draft")
                            },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // 6. COMPLAINT BOX (VILLAGE WALL)
                    composable("complaint_box") {
                        ComplaintBoxScreen(
                            isHindi = isHindi,
                            complaints = ComplaintRepository.getPublicAnonymizedComplaints(),
                            onSelectComplaint = { selected ->
                                navController.navigate("status_lookup/${selected.id}")
                            },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // 7. NGO DASHBOARD
                    composable("ngo_dashboard") {
                        NgoDashboardScreen(
                            isHindi = isHindi,
                            complaints = complaintsList,
                            onUpdateStatus = { id, newStatus, notes ->
                                val user = org.dastakvani.app.data.auth.AuthManager.currentUser.value
                                ComplaintRepository.updateStatus(
                                    complaintId = id,
                                    newStatus = newStatus,
                                    volunteerId = user?.id ?: "vol_001",
                                    volunteerName = user?.name ?: "कार्यकर्ता",
                                    notes = notes
                                )
                                Toast.makeText(this@MainActivity, "मामला अद्यतन किया गया (Status updated)", Toast.LENGTH_SHORT).show()
                            },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // 8. RTI DRAFT SCREEN
                    composable("rti_draft") {
                        val complaint = selectedComplaintForRti ?: complaintsList.firstOrNull()
                        if (complaint != null) {
                            RtiDraftScreen(
                                complaint = complaint,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun checkAudioPermissionAndStart(onGranted: () -> Unit) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            onGranted()
        } else {
            onAudioPermissionGranted = onGranted
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun requestLocationPermissionsIfNecessary() {
        val fineLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (fineLocation != PackageManager.PERMISSION_GRANTED || coarseLocation != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechManager.stopListening()
        ttsManager.shutdown()
    }
}
