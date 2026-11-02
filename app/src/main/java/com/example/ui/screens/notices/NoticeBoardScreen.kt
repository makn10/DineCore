package com.example.ui.screens.notices

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.AppCard
import com.example.ui.components.AppTextField
import com.example.ui.components.EmptyState
import com.example.ui.components.SectionHeaderAppBar
import com.example.ui.theme.AppColors
import com.example.ui.theme.LocalExtendedColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NoticeBoardScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit
) {
    val extended = LocalExtendedColors.current
    val notices by viewModel.notices.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(extended.screenBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SectionHeaderAppBar(
                title = "Notice Board",
                backgroundColor = AppColors.Warning,
                onBackClick = onBackClick
            )

            if (notices.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(bottom = 80.dp), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Default.Notifications,
                        title = "No notices posted yet",
                        subtitle = "Important mess updates and announcements will appear here."
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .padding(bottom = 88.dp)
                ) {
                    items(notices, key = { it.id }) { notice ->
                        AppCard {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = notice.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = extended.textPrimary
                                    )
                                    IconButton(onClick = { viewModel.deleteNotice(notice.id) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = AppColors.Negative.copy(alpha = 0.7f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = notice.content,
                                    fontSize = 14.sp,
                                    color = extended.textPrimary,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "By ${notice.authorName}",
                                        fontSize = 11.sp,
                                        color = AppColors.Warning,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = SimpleDateFormat("dd MMM, hh:mm a", Locale.US).format(Date(notice.createdAt)),
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

        Button(
            onClick = { showAddDialog = true },
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Warning, contentColor = Color.White),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .height(48.dp)
                .testTag("add_notice_button")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.size(8.dp))
            Text(text = "Post Notice", fontWeight = FontWeight.Bold)
        }

        if (showAddDialog) {
            var title by remember { mutableStateOf("") }
            var content by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Post New Notice", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        AppTextField(value = title, onValueChange = { title = it }, label = "Notice Title")
                        Spacer(modifier = Modifier.height(10.dp))
                        AppTextField(value = content, onValueChange = { content = it }, label = "Content / Message")
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (title.isNotBlank() && content.isNotBlank()) {
                            viewModel.saveNotice(title.trim(), content.trim())
                            showAddDialog = false
                        }
                    }) {
                        Text("Post", fontWeight = FontWeight.Bold, color = AppColors.Warning)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
                },
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}
