package org.dastakvani.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dastakvani.app.domain.model.Complaint
import org.dastakvani.app.domain.model.ComplaintCategory
import org.dastakvani.app.domain.model.ComplaintStatus
import org.dastakvani.app.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusLookupScreen(
    isHindi: Boolean,
    initialComplaintId: String = "",
    onLookup: (String) -> Complaint?,
    onVoiceSearch: () -> Unit,
    onSpeakStatus: (Complaint) -> Unit,
    onViewRtiDraft: (Complaint) -> Unit,
    onNavigateBack: () -> Unit
) {
    var searchId by remember { mutableStateOf(initialComplaintId) }
    var foundComplaint by remember { mutableStateOf<Complaint?>(null) }
    var hasSearched by remember { mutableStateOf(false) }

    LaunchedEffect(initialComplaintId) {
        if (initialComplaintId.isNotBlank()) {
            searchId = initialComplaintId
            foundComplaint = onLookup(initialComplaintId)
            hasSearched = true
        }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isHindi) "शिकायत की स्थिति जांचें" else "Grievance Status",
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDarkNavy
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = PrimaryDarkNavy)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundWhite)
            )
        },
        containerColor = BackgroundWhite
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Search Bar with Voice Input
            OutlinedTextField(
                value = searchId,
                onValueChange = { searchId = it },
                label = { Text(if (isHindi) "शिकायत संख्या दर्ज करें (DV-YYYYMMDD-XXXX)" else "Enter Complaint ID") },
                placeholder = { Text("उदा. DV-20260920-4521") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = PrimaryBlue)
                },
                trailingIcon = {
                    IconButton(onClick = onVoiceSearch) {
                        Icon(imageVector = Icons.Default.Mic, contentDescription = "Voice Search", tint = PrimaryBlue)
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            Button(
                onClick = {
                    hasSearched = true
                    foundComplaint = onLookup(searchId)
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = if (isHindi) "स्थिति देखें (Check Status)" else "Search Status",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            if (hasSearched) {
                if (foundComplaint != null) {
                    val complaint = foundComplaint!!
                    ComplaintStatusResultCard(
                        isHindi = isHindi,
                        complaint = complaint,
                        onSpeakStatus = { onSpeakStatus(complaint) },
                        onViewRtiDraft = { onViewRtiDraft(complaint) }
                    )
                } else {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Default.SearchOff, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isHindi) "शिकायत नहीं मिली (Complaint Not Found)" else "Complaint ID Not Found",
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(
                                text = if (isHindi) "कृपया संख्या जांचकर पुनः प्रयास करें।" else "Please verify the ID format and try again.",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun ComplaintStatusResultCard(
    isHindi: Boolean,
    complaint: Complaint,
    onSpeakStatus: () -> Unit,
    onViewRtiDraft: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(complaint.timestamp))

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header: ID and Audio Readout button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = complaint.id,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryDarkNavy,
                        fontSize = 17.sp
                    )
                    Text(
                        text = formattedDate,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                IconButton(onClick = onSpeakStatus) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Hear status", tint = BackgroundWhite, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = BorderSubtle)

            // Current Status Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        when (complaint.status) {
                            ComplaintStatus.RESOLVED -> AccentGreen.copy(alpha = 0.12f)
                            ComplaintStatus.ESCALATED -> AccentRed.copy(alpha = 0.12f)
                            ComplaintStatus.IN_PROGRESS -> AccentAmber.copy(alpha = 0.12f)
                            else -> PrimaryBlue.copy(alpha = 0.12f)
                        }
                    )
                    .padding(12.dp)
            ) {
                val statusColor = when (complaint.status) {
                    ComplaintStatus.RESOLVED -> AccentGreen
                    ComplaintStatus.ESCALATED -> AccentRed
                    ComplaintStatus.IN_PROGRESS -> AccentAmber
                    else -> PrimaryBlue
                }
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isHindi) "वर्तमान स्थिति (Status):" else "Current Status:",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = if (isHindi) complaint.status.titleHi else complaint.status.titleEn,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Grievance summary
            Text(
                text = if (isHindi) "शिकायत का विवरण:" else "Grievance Summary:",
                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontWeight = FontWeight.Bold)
            )
            Text(
                text = "\"${complaint.transcript}\"",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextDark, lineHeight = 20.sp),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Field Volunteer Note if present
            if (!complaint.volunteerNotes.isNullOrBlank()) {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLightBlue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "📝 कार्यकर्ता टिप्पणी (${complaint.assignedVolunteerName ?: "कार्यकर्ता"}):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDarkNavy
                        )
                        Text(
                            text = complaint.volunteerNotes,
                            fontSize = 12.sp,
                            color = TextDark,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Escalation & RTI Action
            if (complaint.isEscalated || complaint.status == ComplaintStatus.ESCALATED) {
                OutlinedButton(
                    onClick = onViewRtiDraft,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Gavel, contentDescription = null, tint = AccentRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isHindi) "विधिक आरटीआई (RTI) प्रारूप देखें" else "View Auto-Generated Legal RTI Draft",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
