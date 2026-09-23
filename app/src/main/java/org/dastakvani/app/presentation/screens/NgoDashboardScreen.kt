package org.dastakvani.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dastakvani.app.data.auth.AuthManager
import org.dastakvani.app.data.auth.VolunteerUser
import org.dastakvani.app.domain.model.Complaint
import org.dastakvani.app.domain.model.ComplaintCategory
import org.dastakvani.app.domain.model.ComplaintStatus
import org.dastakvani.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NgoDashboardScreen(
    isHindi: Boolean,
    complaints: List<Complaint>,
    onUpdateStatus: (String, ComplaintStatus, String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val currentUser by AuthManager.currentUser.collectAsState()

    var emailInput by remember { mutableStateOf("volunteer@biharngo.org") }
    var passwordInput by remember { mutableStateOf("dastak123") }
    var loginError by remember { mutableStateOf<String?>(null) }

    var selectedComplaintForAction by remember { mutableStateOf<Complaint?>(null) }
    var volunteerNotesInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isHindi) "कार्यकर्ता एवं एनजीओ डैशबोर्ड" else "Volunteer & NGO Dashboard",
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDarkNavy
                        )
                        if (currentUser != null) {
                            Text(
                                text = "${currentUser?.name} • ${currentUser?.organization}",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = PrimaryDarkNavy)
                    }
                },
                actions = {
                    if (currentUser != null) {
                        IconButton(onClick = { AuthManager.logout() }) {
                            Icon(imageVector = Icons.Default.Logout, contentDescription = "Logout", tint = PrimaryBlue)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundWhite)
            )
        },
        containerColor = BackgroundWhite
    ) { padding ->
        if (currentUser == null) {
            // Login Screen for Volunteer/NGO
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(SurfaceLightBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(32.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isHindi) "अधिकृत कार्यकर्ता लॉगिन" else "Authorized Volunteer Login",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = PrimaryDarkNavy)
                )

                Text(
                    text = if (isHindi) "शिकायतों के निवारण और भौतिक सत्यापन हेतु" else "For physical grievance follow-up and verification",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("ईमेल (Email)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("पासवर्ड (Password)") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (loginError != null) {
                    Text(
                        text = loginError!!,
                        color = AccentRed,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val result = AuthManager.login(emailInput, passwordInput)
                        if (result.isFailure) {
                            loginError = result.exceptionOrNull()?.message
                        } else {
                            loginError = null
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = if (isHindi) "डैशबोर्ड में प्रवेश करें" else "Log In to Dashboard",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "डेमो क्रेडेंशियल: volunteer@biharngo.org / dastak123",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        } else {
            // Authenticated Dashboard
            val user = currentUser!!
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Metrics Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLightBlue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            MetricItem("कुल मामले", complaints.size.toString(), PrimaryDarkNavy)
                            MetricItem("प्रगति पर", complaints.count { it.status == ComplaintStatus.IN_PROGRESS }.toString(), AccentAmber)
                            MetricItem("विधिक RTI", complaints.count { it.status == ComplaintStatus.ESCALATED }.toString(), AccentRed)
                            MetricItem("समाधान", complaints.count { it.status == ComplaintStatus.RESOLVED }.toString(), AccentGreen)
                        }
                    }
                }

                item {
                    Text(
                        text = if (isHindi) "क्षेत्रीय शिकायतें (जिला: ${user.assignedDistrict}):" else "Area Grievance Queue (${user.assignedDistrict}):",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryDarkNavy),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(complaints) { complaint ->
                    VolunteerComplaintActionCard(
                        complaint = complaint,
                        onAction = {
                            selectedComplaintForAction = complaint
                            volunteerNotesInput = complaint.volunteerNotes ?: ""
                        }
                    )
                }
            }
        }

        // Action Dialog for Status Update
        if (selectedComplaintForAction != null) {
            val c = selectedComplaintForAction!!
            AlertDialog(
                onDismissRequest = { selectedComplaintForAction = null },
                title = { Text("मामला अद्यतन (Update): ${c.id}") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(text = "मूल वक्तव्य: \"${c.transcript}\"", fontSize = 13.sp, color = TextDark)
                        Text(text = "संपर्क: ${c.citizenName ?: "नागरिक"} • ${c.getMaskedPhoneNumber()}", fontSize = 12.sp, color = TextSecondary)

                        OutlinedTextField(
                            value = volunteerNotesInput,
                            onValueChange = { volunteerNotesInput = it },
                            label = { Text("कार्यकर्ता भौतिक आख्या (Field Notes)") },
                            placeholder = { Text("उदा. स्कूल/स्थल का दौरा किया गया, अधिकारी को पत्र दिया") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(text = "स्थिति चुनें:", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    onUpdateStatus(c.id, ComplaintStatus.IN_PROGRESS, volunteerNotesInput)
                                    selectedComplaintForAction = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentAmber),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("प्रगति पर", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    onUpdateStatus(c.id, ComplaintStatus.RESOLVED, volunteerNotesInput)
                                    selectedComplaintForAction = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("समाधान", fontSize = 12.sp)
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { selectedComplaintForAction = null }) {
                        Text("बंद करें")
                    }
                }
            )
        }
    }
}

@Composable
fun MetricItem(label: String, count: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count, fontWeight = FontWeight.Black, fontSize = 20.sp, color = color)
        Text(text = label, fontSize = 11.sp, color = TextSecondary)
    }
}

@Composable
fun VolunteerComplaintActionCard(
    complaint: Complaint,
    onAction: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = complaint.id, fontWeight = FontWeight.Bold, color = PrimaryDarkNavy, fontSize = 13.sp)
                Badge(
                    containerColor = when (complaint.status) {
                        ComplaintStatus.RESOLVED -> AccentGreen
                        ComplaintStatus.ESCALATED -> AccentRed
                        ComplaintStatus.IN_PROGRESS -> AccentAmber
                        else -> PrimaryBlue
                    }
                ) {
                    Text(text = complaint.status.titleEn, color = BackgroundWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "\"${complaint.transcript}\"", fontSize = 13.sp, color = TextDark, maxLines = 2)

            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "📍 ${complaint.location.address}", fontSize = 11.sp, color = TextSecondary)
                Text(text = "👤 ${complaint.getMaskedPhoneNumber()}", fontSize = 11.sp, color = TextSecondary)
            }

            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onAction,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
            ) {
                Text(text = "कार्यवाही करें / स्थिति बदलें (Take Action)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
