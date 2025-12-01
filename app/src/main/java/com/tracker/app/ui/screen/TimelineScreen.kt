package com.tracker.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracker.app.data.Task
import com.tracker.app.data.Log
import com.tracker.app.ui.theme.CreamyBackground
import com.tracker.app.ui.theme.TaskColor
import com.tracker.app.ui.theme.TaskColors
import com.tracker.app.util.DateUtils
import com.tracker.app.viewmodel.TrackerViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    viewModel: TrackerViewModel,
    onBack: () -> Unit
) {
    var selectedDate by remember { mutableStateOf(Date()) }
    val tasks by viewModel.tasks.collectAsState()
    
    val calendar = Calendar.getInstance()
    calendar.time = selectedDate
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val startTime = calendar.timeInMillis
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MILLISECOND, 999)
    val endTime = calendar.timeInMillis
    
    val logs by viewModel.getLogsByDateRange(startTime, endTime).collectAsState(initial = emptyList())
    
    val dayLogs = logs.map { log ->
        val task = tasks.find { it.id == log.taskId }
        LogWithTask(
            log = log,
            taskName = task?.name ?: "Unknown Task",
            unit = task?.unit ?: "",
            colorObj = try {
                val colorIndex = task?.colorIndex ?: 0
                if (TaskColors.isEmpty()) {
                    // 如果颜色列表为空，使用默认颜色
                    TaskColor(
                        bg = androidx.compose.ui.graphics.Color(0xFFFFEDD5),
                        text = androidx.compose.ui.graphics.Color(0xFFEA580C),
                        stroke = androidx.compose.ui.graphics.Color(0xFFEA580C)
                    )
                } else {
                    val safeIndex = if (TaskColors.size > 0) {
                        val index = colorIndex % TaskColors.size
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
                // 如果出现任何异常，使用默认颜色
                TaskColor(
                    bg = androidx.compose.ui.graphics.Color(0xFFFFEDD5),
                    text = androidx.compose.ui.graphics.Color(0xFFEA580C),
                    stroke = androidx.compose.ui.graphics.Color(0xFFEA580C)
                )
            }
        )
    }.sortedBy { it.log.timestamp }
    
    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CreamyBackground,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    Text(
                        text = "Timeline",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Spacer(modifier = Modifier.width(48.dp)) // 平衡布局
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CreamyBackground)
                .padding(padding)
        ) {
            // 日历选择器
            CalendarSelector(
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it }
            )
            
            // 时间轴内容
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (dayLogs.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(96.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "📅",
                                        fontSize = 40.sp
                                    )
                                }
                                Text(
                                    text = "This day is quiet",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                } else {
                    itemsIndexed(dayLogs) { index, logWithTask ->
                        TimelineItem(
                            logWithTask = logWithTask,
                            isLast = index == dayLogs.size - 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarSelector(
    selectedDate: Date,
    onDateSelected: (Date) -> Unit
) {
    val calendarDays = remember(selectedDate) {
        val days = mutableListOf<Date>()
        val cal = Calendar.getInstance()
        cal.time = selectedDate
        for (i in -3..3) {
            cal.time = selectedDate
            cal.add(Calendar.DAY_OF_YEAR, i)
            days.add(cal.time)
        }
        days
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val cal = Calendar.getInstance()
                cal.time = selectedDate
                Text(
                    text = "${cal.getDisplayName(Calendar.MONTH, Calendar.LONG, java.util.Locale.ENGLISH)}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${cal.get(Calendar.YEAR)}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                calendarDays.forEach { date ->
                    val isSelected = DateUtils.formatDate(date.time) == DateUtils.formatDate(selectedDate.time)
                    Surface(
                        onClick = { onDateSelected(date) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        } else {
                            Color.Transparent
                        }
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val cal = Calendar.getInstance()
                            cal.time = date
                            Text(
                                text = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")[cal.get(Calendar.DAY_OF_WEEK) - 1],
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                }
                            )
                            Text(
                                text = "${cal.get(Calendar.DAY_OF_MONTH)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineItem(
    logWithTask: LogWithTask,
    isLast: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 时间点
        Text(
            text = DateUtils.formatTime(logWithTask.log.timestamp),
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.width(60.dp)
        )
        
        // 时间轴（圆点 + 连接线）
        Box(
            modifier = Modifier
                .width(16.dp)
        ) {
            // 连接线（从圆点中心向下延伸，足够长以连接到下一个圆点）
            // 卡片高度约60-70dp + 间距12dp，所以使用75dp应该足够
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(75.dp)
                        .offset(x = 7.dp, y = 8.dp) // 从圆点中心（8.dp）开始，水平居中（7.dp = (16-2)/2）
                        .background(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        )
                )
            }
            
            // 轴上的圆点
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(logWithTask.colorObj.stroke)
            )
        }
        
        // 卡片
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = logWithTask.taskName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "+${logWithTask.log.amount}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = logWithTask.colorObj.text
                        )
                        if (logWithTask.unit.isNotEmpty()) {
                            Text(
                                text = logWithTask.unit,
                                fontSize = 10.sp,
                                color = logWithTask.colorObj.text.copy(alpha = 0.6f),
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class LogWithTask(
    val log: Log,
    val taskName: String,
    val unit: String,
    val colorObj: com.tracker.app.ui.theme.TaskColor
)

