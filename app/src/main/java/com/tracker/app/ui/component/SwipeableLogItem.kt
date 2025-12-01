package com.tracker.app.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracker.app.data.Log
import com.tracker.app.util.DateUtils

@Composable
fun SwipeableLogItem(
    log: Log,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    var offsetX by remember(log.id) { mutableStateOf(0f) }
    val animatedOffset by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = tween(300),
        label = "swipe"
    )
    
    val maxOffset = 120f
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        // --- 修复开始 ---
        // 只有当向右滑动 (offset > 0) 时，才显示编辑背景
        if (animatedOffset > 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.CenterStart
            ) {
                IconButton(onClick = {
                    offsetX = 0f
                    onEdit()
                }) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Time",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // 只有当向左滑动 (offset < 0) 时，才显示删除背景
        if (animatedOffset < 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.CenterEnd
            ) {
                IconButton(onClick = {
                    offsetX = 0f
                    onDelete()
                }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
        // --- 修复结束 ---

        // 内容卡片
        Card(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = animatedOffset.dp)
                .pointerInput(log.id) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            // 逻辑修正：
                            // offsetX > 0 代表向右滑，卡片右移，露出左侧 (编辑)
                            // offsetX < 0 代表向左滑，卡片左移，露出右侧 (删除)
                            offsetX = when {
                                offsetX > 50f -> maxOffset   // 阈值设小一点，体验更好
                                offsetX < -50f -> -maxOffset
                                else -> 0f
                            }
                        }
                    ) { _, dragAmount ->
                        val newOffset = (offsetX + dragAmount).coerceIn(-maxOffset, maxOffset)
                        offsetX = newOffset
                    }
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = DateUtils.formatTime(log.timestamp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "+${log.amount}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

