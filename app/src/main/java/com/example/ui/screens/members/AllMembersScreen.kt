package com.example.ui.screens.members

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
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
import com.example.ui.components.RoleBadge
import com.example.ui.components.SectionHeaderAppBar
import com.example.ui.theme.AppColors
import com.example.ui.theme.LocalExtendedColors

@Composable
fun AllMembersScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit,
    onMemberClick: (String) -> Unit
) {
    val extended = LocalExtendedColors.current
    val members by viewModel.members.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(extended.screenBackground)
    ) {
        SectionHeaderAppBar(
            title = "All Members",
            backgroundColor = AppColors.Primary,
            onBackClick = onBackClick
        )

        if (members.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Person,
                title = "No members found",
                subtitle = "Share the invite code from your profile to add members."
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp)
                    .testTag("all_members_list")
            ) {
                items(members, key = { it.id }) { member ->
                    AppCard(
                        modifier = Modifier.clickable { onMemberClick(member.id) },
                        testTag = "member_item_${member.id}"
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(AppColors.TealTint),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = member.name.take(1).uppercase(),
                                    color = AppColors.PrimaryDark,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = member.name,
                                    color = extended.textPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (member.phone.isNotEmpty()) {
                                    Text(
                                        text = member.phone,
                                        color = extended.textMuted,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            RoleBadge(role = member.role)
                        }
                    }
                }
            }
        }
    }
}
