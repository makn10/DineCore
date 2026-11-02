package com.example.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.AppCard
import com.example.ui.components.EmptyState
import com.example.ui.components.SectionHeaderAppBar
import com.example.ui.theme.AppColors
import com.example.ui.theme.LocalExtendedColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(viewModel: MainViewModel) {
    val extended = LocalExtendedColors.current
    val auditLogs by viewModel.auditLogs.collectAsState()
    val periods by viewModel.periods.collectAsState()
    val selectedPeriodId by viewModel.selectedPeriodId.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(extended.screenBackground)
    ) {
        SectionHeaderAppBar(
            title = "History & Audit Logs",
            backgroundColor = AppColors.PrimaryDark
        )

        // Month selector filter row
        if (periods.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(periods) { p ->
                    FilterChip(
                        selected = p.id == selectedPeriodId,
                        onClick = { viewModel.selectPeriod(p.id) },
                        label = { Text(p.name + if (p.isClosed) " (Closed)" else "") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppColors.Primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        if (auditLogs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    icon = Icons.Default.History,
                    title = "No activity logs yet",
                    subtitle = "Changes to meals, bazar, and deposits will create an audit trail."
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 88.dp)
                    .testTag("audit_logs_list")
            ) {
                items(auditLogs, key = { it.id }) { log ->
                    AppCard(padding = androidx.compose.foundation.layout.PaddingValues(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val badgeColor = when (log.entityType) {
                                "MEAL" -> AppColors.MealSheet
                                "BAZAR" -> AppColors.Primary
                                "DEPOSIT" -> AppColors.Positive
                                "BILL" -> AppColors.Indigo
                                else -> AppColors.Roles
                            }

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(badgeColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = log.entityType.take(1),
                                    color = badgeColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${log.action} ${log.entityType}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = badgeColor
                                    )
                                    Text(
                                        text = SimpleDateFormat("dd MMM, hh:mm a", Locale.US).format(Date(log.timestamp)),
                                        fontSize = 11.sp,
                                        color = extended.textMuted
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = log.details,
                                    fontSize = 13.sp,
                                    color = extended.textPrimary
                                )

                                if (log.performedByName.isNotEmpty()) {
                                    Text(
                                        text = "By ${log.performedByName}",
                                        fontSize = 11.sp,
                                        color = extended.textMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
