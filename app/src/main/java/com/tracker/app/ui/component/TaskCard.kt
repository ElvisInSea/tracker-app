package com.tracker.app.ui.component

import android.util.Log as AndroidLog
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracker.app.data.Task
import com.tracker.app.data.Log
import com.tracker.app.ui.theme.TaskColor
import com.tracker.app.ui.theme.TaskColors
import com.tracker.app.util.DateUtils

@Composable
fun TaskCard(
    task: Task,
    logs: List<Log>,
    onCheckIn: () -> Unit,
    onClick: () -> Unit
) {
    var selectedDate by remember { mutableStateOf<String?>(null) }
    
    // 安全地计算今日数据
    val todayStr = remember(task.id) {
        try {
            val str = DateUtils.formatDate(System.currentTimeMillis())
            AndroidLog.d("TaskCard", "TaskCard[${task.id}]: todayStr=$str")
            str
        } catch (e: Exception) {
            AndroidLog.e("TaskCard", "TaskCard[${task.id}]: Error getting todayStr", e)
            ""
        }
    }
    
    val todayLogs = remember(logs, todayStr) {
        try {
            if (todayStr.isBlank()) {
                AndroidLog.d("TaskCard", "TaskCard[${task.id}]: todayStr is blank, returning empty list")
                emptyList()
            } else {
                val filtered = logs.filter { 
                    try {
                        DateUtils.formatDate(it.timestamp) == todayStr
                    } catch (e: Exception) {
                        AndroidLog.e("TaskCard", "TaskCard[${task.id}]: Error filtering log ${it.id}", e)
                        false
                    }
                }
                AndroidLog.d("TaskCard", "TaskCard[${task.id}]: Found ${filtered.size} logs for today")
                filtered
            }
        } catch (e: Exception) {
            AndroidLog.e("TaskCard", "TaskCard[${task.id}]: Error in todayLogs remember", e)
            emptyList()
        }
    }
    
    val todayAmount = remember(todayLogs) {
        try {
            val sum = todayLogs.sumOf { it.amount.toLong().coerceAtLeast(0) }
            // 防止溢出，如果超过Int最大值，则使用Int最大值
            val result = if (sum > Int.MAX_VALUE) {
                AndroidLog.w("TaskCard", "TaskCard[${task.id}]: Sum overflow, using Int.MAX_VALUE")
                Int.MAX_VALUE
            } else {
                sum.toInt().coerceAtLeast(0)
            }
            AndroidLog.d("TaskCard", "TaskCard[${task.id}]: todayAmount=$result (sum=$sum)")
            result
        } catch (e: Exception) {
            // 捕获所有异常，防止崩溃
            AndroidLog.e("TaskCard", "TaskCard[${task.id}]: Error calculating todayAmount", e)
            0
        }
    }
    
    val isDone = remember(task.target, todayAmount) {
        val done = task.target > 0 && todayAmount >= task.target
        AndroidLog.d("TaskCard", "TaskCard[${task.id}]: isDone=$done (target=${task.target}, todayAmount=$todayAmount)")
        done
    }
    
    val colorObj = remember(task.colorIndex) {
        try {
            if (TaskColors.isEmpty()) {
                // 如果颜色列表为空，使用默认颜色
                TaskColor(
                    bg = androidx.compose.ui.graphics.Color(0xFFFFEDD5),
                    text = androidx.compose.ui.graphics.Color(0xFFEA580C),
                    stroke = androidx.compose.ui.graphics.Color(0xFFEA580C)
                )
            } else {
                val safeIndex = if (TaskColors.size > 0) {
                    val index = task.colorIndex % TaskColors.size
                    // 处理负数情况
                    if (index < 0) (index + TaskColors.size) % TaskColors.size else index
                } else {
                    0
                }
                TaskColors.getOrElse(safeIndex.coerceAtLeast(0).coerceAtMost(TaskColors.size - 1)) {
                    TaskColors[0]
                }
            }
        } catch (e: Exception) {
            AndroidLog.e("TaskCard", "TaskCard[${task.id}]: Error calculating colorObj", e)
            // 如果出现任何异常，使用默认颜色
            TaskColor(
                bg = androidx.compose.ui.graphics.Color(0xFFFFEDD5),
                text = androidx.compose.ui.graphics.Color(0xFFEA580C),
                stroke = androidx.compose.ui.graphics.Color(0xFFEA580C)
            )
        }
    }
    
    val selectedDateLogs = remember(selectedDate, logs) {
        selectedDate?.let { date ->
            try {
                logs.filter { 
                    try {
                        DateUtils.formatDate(it.timestamp) == date
                    } catch (e: Exception) {
                        false
                    }
                }
            } catch (e: Exception) {
                emptyList()
            }
        } ?: emptyList()
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .animateContentSize() // 为容器高度变化添加动画
        ) {
            // 头部信息 - 可点击区域
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                        text = task.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colorObj.bg.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = task.unit,
                                fontSize = 10.sp,
                                color = colorObj.text,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Text(
                        text = task.description.ifEmpty { "Keep the passion alive" },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                // 打卡按钮区域
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = todayAmount.toString(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Today",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            letterSpacing = 1.sp
                        )
                    }
                    IconButton(
                        onClick = {
                            try {
                                AndroidLog.d("TaskCard", "TaskCard[${task.id}]: Check-in button clicked, isDone=$isDone, todayAmount=$todayAmount, target=${task.target}")
                                onCheckIn()
                                AndroidLog.d("TaskCard", "TaskCard[${task.id}]: Check-in callback completed")
                            } catch (e: Exception) {
                                // 捕获异常，防止崩溃
                                AndroidLog.e("TaskCard", "TaskCard[${task.id}]: Exception in check-in button onClick", e)
                                e.printStackTrace()
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isDone) MaterialTheme.colorScheme.surfaceVariant
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                    ) {
                        Icon(
                            imageVector = if (isDone) Icons.Default.Check else Icons.Default.Add,
                            contentDescription = "Check In",
                            tint = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 热力图
            Heatmap(
                logs = logs,
                task = task,
                selectedDay = selectedDate,
                onSelectDay = { date ->
                    selectedDate = if (selectedDate == date) null else date
                }
            )
            
            // 选中日期的详情展开，使用容器高度动画
            selectedDate?.let { date ->
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = date,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "${
                                    try {
                                        val sum = selectedDateLogs.sumOf { it.amount.toLong().coerceAtLeast(0) }
                                        // 防止溢出
                                        if (sum > Int.MAX_VALUE) {
                                            Int.MAX_VALUE
                                        } else {
                                            sum.toInt().coerceAtLeast(0)
                                        }
                                    } catch (e: Exception) {
                                        // 捕获所有异常，防止崩溃
                                        0
                                    }
                                } ${task.unit}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (selectedDateLogs.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            selectedDateLogs.forEach { log ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = DateUtils.formatTime(log.timestamp),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = "+${log.amount}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "No records",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

