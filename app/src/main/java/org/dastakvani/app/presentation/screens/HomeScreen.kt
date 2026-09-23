package org.dastakvani.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dastakvani.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    isHindi: Boolean,
    onToggleLanguage: () -> Unit,
    onSpeakInstruction: () -> Unit,
    onStartRecording: () -> Unit,
    onNavigateToStatus: () -> Unit,
    onNavigateToVillageWall: () -> Unit,
    onNavigateToNgoPortal: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isHindi) "दस्तक वाणी" else "Dastak Vani",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryDarkNavy
                            )
                        )
                        Text(
                            text = if (isHindi) "The Voice That Knocks — आवाज जो दस्तक दे" else "Voice-First Civic Accountability",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                        )
                    }
                },
                actions = {
                    // Language Switcher Button
                    OutlinedButton(
                        onClick = onToggleLanguage,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = SurfaceLightBlue,
                            contentColor = PrimaryDarkNavy
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "Language",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isHindi) "English" else "हिन्दी",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundWhite
                )
            )
        },
        containerColor = BackgroundWhite
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Low-Literacy Spoken Guidance Banner
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLightBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSpeakInstruction() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Hear audio instructions",
                            tint = BackgroundWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isHindi) "सुनें: शिकायत कैसे दर्ज करें" else "Tap to hear audio instructions",
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDarkNavy,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (isHindi) "माइक बटन दबाकर अपनी समस्या बोलें" else "Press the large mic button and speak",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Central Voice Action (Gigantic accessible touch target)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .shadow(12.dp, CircleShape)
                        .clip(CircleShape)
                        .background(PrimaryBlue)
                        .border(4.dp, SurfaceLightBlue, CircleShape)
                        .clickable { onStartRecording() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Speak Complaint",
                        tint = BackgroundWhite,
                        modifier = Modifier.size(68.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (isHindi) "यहाँ दबाकर शिकायत बोलें" else "Tap Here to Speak Grievance",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryDarkNavy
                    ),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (isHindi) "स्कूल • सड़क • राशन • बाल सुरक्षा" else "Education • Civic • Welfare • Child Safety",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Civic Navigation Cards
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CivicActionRow(
                    icon = Icons.Default.Search,
                    title = if (isHindi) "शिकायत की स्थिति जांचें" else "Check Complaint Status",
                    subtitle = if (isHindi) "नंबर बोलें या दर्ज करें (Spoken status)" else "Speak or enter complaint number",
                    accentColor = AccentTeal,
                    onClick = onNavigateToStatus
                )

                CivicActionRow(
                    icon = Icons.Default.Public,
                    title = if (isHindi) "ग्राम मंच / शिकायत पेटी" else "Village Wall (Public Grievances)",
                    subtitle = if (isHindi) "क्षेत्र की सार्वजनिक शिकायतें देखें" else "Live community accountability feed",
                    accentColor = PrimaryBlue,
                    onClick = onNavigateToVillageWall
                )

                CivicActionRow(
                    icon = Icons.Default.VerifiedUser,
                    title = if (isHindi) "कार्यकर्ता एवं एनजीओ पोर्टल" else "Volunteer & NGO Portal",
                    subtitle = if (isHindi) "अधिकृत संस्था एवं कार्यकर्ता लॉगिन" else "Authorized resolution dashboard",
                    accentColor = AccentPurple,
                    onClick = onNavigateToNgoPortal
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Emergency Contacts Footer
            Text(
                text = if (isHindi) "आपातकालीन हेल्पलाइन: बाल अधिकार 1098 | महिला 181 | आपातकाल 112" else "Emergency Helplines: Childline 1098 | Women 181 | Police 112",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = AccentRed,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
    }
}

@Composable
fun CivicActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    fontSize = 14.sp
                )
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
