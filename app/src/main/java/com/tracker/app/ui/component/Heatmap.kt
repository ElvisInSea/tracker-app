package com.tracker.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tracker.app.data.Task
import com.tracker.app.data.Log
import com.tracker.app.ui.theme.TaskColor
import com.tracker.app.ui.theme.TaskColors
import com.tracker.app.util.DateUtils
import java.util.*

@Composable
fun Heatmap(
    logs: List<Log>,
    task: Task,
    selectedDay: String?,
    onSelectDay: (String) -> Unit
) {
    val days = 42 // 显示最近6周
    val today = Date()
    val calendar = Calendar.getInstance()
    
    val calendarData = remember(logs, task.id) {
        (0 until days).map { i ->
            calendar.time = today
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val date = calendar.time
            val dateStr = DateUtils.formatDate(date.time)
            
            val dayLogs = logs.filter { 
                DateUtils.formatDate(it.timestamp) == dateStr && it.taskId == task.id 
            }
            val totalAmount = try {
                val sum = dayLogs.sumOf { it.amount.toLong() }
                // 防止溢出
                if (sum > Int.MAX_VALUE) Int.MAX_VALUE else sum.toInt()
            } catch (e: Exception) {
                0
            }
            
            DayData(
                date = dateStr,
                amount = totalAmount,
                isToday = dateStr == DateUtils.formatDate(System.currentTimeMillis())
            )
        }.reversed()
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
            // 如果出现任何异常，使用默认颜色
            TaskColor(
                bg = androidx.compose.ui.graphics.Color(0xFFFFEDD5),
                text = androidx.compose.ui.graphics.Color(0xFFEA580C),
                stroke = androidx.compose.ui.graphics.Color(0xFFEA580C)
            )
        }
    }
    
    // 在初始化状态时直接设置初始位置为今天（最后一个元素）
    // 这样用户完全感知不到任何滚动或定位操作，就像它本来就在那里一样
    val initialIndex = remember(task.id, calendarData.size) {
        if (calendarData.isNotEmpty()) {
            (calendarData.size - 1).coerceAtLeast(0)
        } else {
            0
        }
    }
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = initialIndex
    )
    
    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(calendarData) { index, day ->
            // 根据该事务的打卡量和目标量计算颜色深浅
            // 每个事务使用自己的 target 值来计算比例，所以不同事务即使打卡量相同，颜色深浅也会不同
            val opacity = calculateOpacity(day.amount, task.target)
            val isSelected = selectedDay == day.date
            val baseColor = colorObj.stroke.copy(alpha = opacity)
            
            Box(
                modifier = Modifier
                    .then(
                        if (day.isToday) {
                            // 今天使用圆形和水晶样式，需要更大的空间来容纳外光环
                            // 主体 14dp + 白色描边 2dp*2 + 外光环 4dp*2 = 22dp，使用 22dp 确保完整显示
                            Modifier
                                .size(22.dp) // 足够大以容纳外光环
                                .clip(CircleShape)
                                .background(baseColor)
                                .crystalHighlight(
                                    baseColor = baseColor
                                )
                        } else {
                            // 其他日期使用圆角矩形
                            Modifier
                                .size(14.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(baseColor)
                        }
                    )
                    .clickable { onSelectDay(day.date) },
                contentAlignment = Alignment.Center
            ) {
                // Box content is empty as the background and highlight are handled by modifiers
            }
        }
    }
}

private fun calculateOpacity(amount: Int, target: Int): Float {
    if (amount == 0) return 0.1f
    // 防止除零错误：如果target为0或负数，使用默认不透明度
    if (target <= 0) return 0.5f
    val ratio = (amount.toFloat() / target.toFloat()).coerceAtMost(1.0f)
    // 确保 alpha 值在 [0.0, 1.0] 范围内
    val alpha = 0.2f + (ratio * 0.8f)
    return alpha.coerceIn(0.0f, 1.0f)
}

private data class DayData(
    val date: String,
    val amount: Int,
    val isToday: Boolean
)

