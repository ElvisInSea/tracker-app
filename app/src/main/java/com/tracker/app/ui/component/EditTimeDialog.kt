package com.tracker.app.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.tracker.app.data.Log
import java.util.*

/**
 * 时间编辑对话框
 * 使用 CreamyTimePicker 来编辑 Log 的时间戳
 * 参考新建事务弹窗的样式
 */
@Composable
fun EditTimeDialog(
    log: Log,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = log.timestamp
    }
    
    var selectedHour by remember(log.id) { mutableStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember(log.id) { mutableStateOf(calendar.get(Calendar.MINUTE)) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 时间选择器
                CreamyTimePicker(
                    initialHour = selectedHour,
                    initialMinute = selectedMinute,
                    onTimeSelected = { hour, minute ->
                        selectedHour = hour
                        selectedMinute = minute
                    }
                )
                
                // 确认按钮 - 参考新建事务弹窗的按钮样式
                Button(
                    onClick = {
                        // 创建新的时间戳（保持日期不变，只修改时分）
                        val newCalendar = Calendar.getInstance().apply {
                            timeInMillis = log.timestamp
                            set(Calendar.HOUR_OF_DAY, selectedHour)
                            set(Calendar.MINUTE, selectedMinute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        onConfirm(newCalendar.timeInMillis)
                        onDismiss() // 确保对话框关闭
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Confirm",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

