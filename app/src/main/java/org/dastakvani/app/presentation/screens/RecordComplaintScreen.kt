package org.dastakvani.app.presentation.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dastakvani.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordComplaintScreen(
    isHindi: Boolean,
    isListening: Boolean,
    soundLevel: Float,
    currentTranscript: String,
    errorMessage: String?,
    onStopListening: () -> Unit,
    onCancel: () -> Unit,
    onDoneWithText: (String) -> Unit
) {
    var editableText by remember(currentTranscript) { mutableStateOf(currentTranscript) }

    // Pulse animation for recording ring
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isHindi) "शिकायत बोलें" else "Speak Grievance",
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDarkNavy
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel", tint = PrimaryDarkNavy)
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
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header instruction
            Text(
                text = if (isListening) {
                    if (isHindi) "हम सुन रहे हैं... कृपया स्पष्ट बोलें" else "Listening... Please speak clearly"
                } else {
                    if (isHindi) "सुनना समाप्त हुआ" else "Speech captured"
                },
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isListening) PrimaryBlue else TextDark
                ),
                textAlign = TextAlign.Center
            )

            // Animated Mic Indicator
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(160.dp)
                    .padding(vertical = 12.dp)
            ) {
                if (isListening) {
                    // Pulsing outer ripple
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .scale(pulseScale + (soundLevel * 0.3f))
                            .clip(CircleShape)
                            .background(PrimaryBlue.copy(alpha = 0.18f))
                    )
                }

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(if (isListening) AccentRed else PrimaryBlue)
                        .border(3.dp, BackgroundWhite, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Mic else Icons.Default.Check,
                        contentDescription = null,
                        tint = BackgroundWhite,
                        modifier = Modifier.size(52.dp)
                    )
                }
            }

            // Real-Time Transcript Display & Edit Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isHindi) "आपकी आवाज का विवरण (Live Transcript):" else "Spoken Transcript:",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryDarkNavy
                            )
                        )
                        if (isListening) {
                            Badge(containerColor = AccentRed) {
                                Text(
                                    text = if (isHindi) "लाइव" else "REC",
                                    color = BackgroundWhite,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = editableText,
                        onValueChange = { editableText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        placeholder = {
                            Text(
                                text = if (isHindi) "आपकी बोली गई बात यहाँ दिखाई देगी..." else "Your spoken words will appear here...",
                                color = TextSecondary
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )

                    if (!errorMessage.isNullOrBlank()) {
                        Text(
                            text = errorMessage,
                            color = AccentRed,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        onStopListening()
                        if (editableText.isNotBlank()) {
                            onDoneWithText(editableText)
                        }
                    },
                    enabled = editableText.isNotBlank(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Icon(imageVector = Icons.Default.Hearing, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isHindi) "बोलना पूरा हुआ • पुष्टि करें (Proof of Hearing)" else "Done Speaking • Verify Complaint",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                OutlinedButton(
                    onClick = onCancel,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = if (isHindi) "रद्द करें (Cancel)" else "Cancel",
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
