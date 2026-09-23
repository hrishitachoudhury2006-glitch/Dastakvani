package org.dastakvani.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dastakvani.app.data.ml.ClassificationResult
import org.dastakvani.app.domain.extractor.ExtractedEntities
import org.dastakvani.app.domain.model.ComplaintCategory
import org.dastakvani.app.domain.model.ComplaintLocation
import org.dastakvani.app.domain.routing.RoutingEngine
import org.dastakvani.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProofOfHearingScreen(
    isHindi: Boolean,
    transcript: String,
    classification: ClassificationResult,
    entities: ExtractedEntities,
    location: ComplaintLocation,
    isSpeakingTts: Boolean,
    onReplayTts: () -> Unit,
    onConfirmAndSubmit: () -> Unit,
    onReRecord: () -> Unit
) {
    val routing = RoutingEngine.resolveRouting(classification.category, location.district)
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isHindi) "पुष्टि करें (Proof of Hearing)" else "Proof of Hearing",
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDarkNavy
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onReRecord) {
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Audio Confirmation Playback Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLightBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PrimaryLightBlue.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSpeakingTts) Icons.Default.VolumeUp else Icons.Default.Hearing,
                                    contentDescription = null,
                                    tint = BackgroundWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isHindi) "प्रणाली ने यह समझा:" else "The system heard:",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryDarkNavy
                                )
                            )
                        }

                        IconButton(onClick = onReplayTts) {
                            Icon(
                                imageVector = Icons.Default.Replay,
                                contentDescription = "Replay audio",
                                tint = PrimaryBlue
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "\"$transcript\"",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            color = TextDark,
                            lineHeight = 22.sp
                        )
                    )
                }
            }

            // On-Device ML Inference Result Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isHindi) "पहचानी गई श्रेणी (On-Device ML):" else "Detected Category (On-Device ML):",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                        )
                        Badge(
                            containerColor = if (classification.category == ComplaintCategory.CHILD_SAFETY) AccentRed else PrimaryBlue
                        ) {
                            Text(
                                text = "${(classification.confidence * 100).toInt()}% सटीक",
                                color = BackgroundWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isHindi) classification.category.displayNameHi else classification.category.displayNameEn,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (classification.category == ComplaintCategory.CHILD_SAFETY) AccentRed else PrimaryDarkNavy
                        )
                    )

                    if (classification.category == ComplaintCategory.CHILD_SAFETY) {
                        Text(
                            text = if (isHindi) "⚠️ अति-संवेदनशील मामला: प्राथमिकता एवं गोपनीयता के साथ निस्तारित होगा।" else "⚠️ High-risk case: Handled with strict privacy and urgent priority.",
                            style = MaterialTheme.typography.bodySmall.copy(color = AccentRed),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Real Geolocation Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AccentGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = AccentGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isHindi) "स्थान एवं जिला (Geotagged):" else "Location & District:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                        )
                        Text(
                            text = "${location.address} (जिला: ${location.district})",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextDark
                            )
                        )
                    }
                }
            }

            // Dual Routing Channel Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isHindi) "समांतर दोहरी अग्रेषण व्यवस्था (Dual Routing):" else "Simultaneous Dual Routing:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountBalance, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = routing.departmentName, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Groups, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = routing.ngoName, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Confirmation Actions
            Button(
                onClick = onConfirmAndSubmit,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isHindi) "हाँ, यह बिल्कुल सही है • शिकायत दर्ज करें" else "Yes, Correct • Submit Grievance",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            OutlinedButton(
                onClick = onReRecord,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isHindi) "गलत है • फिर से बोलें (Re-record)" else "Incorrect • Re-record Grievance",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
