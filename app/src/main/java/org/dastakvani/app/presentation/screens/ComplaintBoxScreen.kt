package org.dastakvani.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
fun ComplaintBoxScreen(
    isHindi: Boolean,
    complaints: List<Complaint>,
    onSelectComplaint: (Complaint) -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedCategoryFilter by remember { mutableStateOf<ComplaintCategory?>(null) }
    var selectedDistrictFilter by remember { mutableStateOf("सभी (All)") }

    val districts = listOf("सभी (All)", "Madhubani", "Darbhanga", "Muzaffarpur", "Sitamarhi")

    val filteredComplaints = complaints.filter { c ->
        val matchesCategory = selectedCategoryFilter == null || c.category == selectedCategoryFilter
        val matchesDistrict = selectedDistrictFilter == "सभी (All)" || c.location.district.contains(selectedDistrictFilter, ignoreCase = true)
        matchesCategory && matchesDistrict
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isHindi) "ग्राम मंच • सार्वजनिक शिकायत पेटी" else "Village Wall • Public Complaints",
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDarkNavy
                        )
                        Text(
                            text = if (isHindi) "सामूहिक पारदर्शिता • पूरी तरह गोपनीय एवं सुरक्षित" else "Community Accountability • Fully Anonymized",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                        )
                    }
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
        ) {
            // Category Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategoryFilter == null,
                        onClick = { selectedCategoryFilter = null },
                        label = { Text(if (isHindi) "सभी श्रेणियां" else "All Categories") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue,
                            selectedLabelColor = BackgroundWhite
                        )
                    )
                }
                items(ComplaintCategory.entries) { cat ->
                    FilterChip(
                        selected = selectedCategoryFilter == cat,
                        onClick = { selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat },
                        label = { Text(if (isHindi) cat.displayNameEn else cat.displayNameEn) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue,
                            selectedLabelColor = BackgroundWhite
                        )
                    )
                }
            }

            // District Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(districts) { dist ->
                    AssistChip(
                        onClick = { selectedDistrictFilter = dist },
                        label = { Text(dist, fontSize = 12.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (selectedDistrictFilter == dist) SurfaceLightBlue else SurfaceCard,
                            labelColor = if (selectedDistrictFilter == dist) PrimaryBlue else TextDark
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (selectedDistrictFilter == dist) PrimaryBlue else BorderSubtle
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Complaints Feed
            if (filteredComplaints.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isHindi) "इस श्रेणी में कोई शिकायत नहीं मिली।" else "No complaints found in this category.",
                        color = TextSecondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredComplaints) { complaint ->
                        PublicComplaintCard(
                            isHindi = isHindi,
                            complaint = complaint,
                            onClick = { onSelectComplaint(complaint) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PublicComplaintCard(
    isHindi: Boolean,
    complaint: Complaint,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val dateStr = dateFormat.format(Date(complaint.timestamp))

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when (complaint.status) {
                                    ComplaintStatus.RESOLVED -> AccentGreen
                                    ComplaintStatus.ESCALATED -> AccentRed
                                    ComplaintStatus.IN_PROGRESS -> AccentAmber
                                    else -> PrimaryBlue
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = complaint.id,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDarkNavy,
                        fontSize = 13.sp
                    )
                }

                Text(
                    text = dateStr,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Anonymized Citizen Grievance Quote
            Text(
                text = "\"${complaint.transcript}\"",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Normal,
                    color = TextDark,
                    lineHeight = 20.sp
                ),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Badges row: Category & Location
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Chip
                SuggestionChip(
                    onClick = {},
                    label = {
                        Text(
                            text = if (isHindi) complaint.category.displayNameEn else complaint.category.displayNameEn,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (complaint.category == ComplaintCategory.CHILD_SAFETY) AccentRed else PrimaryBlue
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = if (complaint.category == ComplaintCategory.CHILD_SAFETY) AccentRed.copy(alpha = 0.1f) else SurfaceLightBlue
                    ),
                    border = null
                )

                // Location tag
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = complaint.location.district,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
