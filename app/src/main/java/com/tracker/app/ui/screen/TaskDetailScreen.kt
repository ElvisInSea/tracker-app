package com.tracker.app.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.compose.style.ChartStyle
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.tracker.app.data.Task
import com.tracker.app.data.Log
import com.tracker.app.ui.component.EditTimeDialog
import com.tracker.app.ui.component.SwipeableLogItem
import com.tracker.app.ui.theme.CreamyBackground
import com.tracker.app.ui.theme.TaskColor
import com.tracker.app.ui.theme.TaskColors
import com.tracker.app.util.DateUtils
import com.tracker.app.viewmodel.TrackerViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String,
    viewModel: TrackerViewModel,
    onBack: () -> Unit
) {
    val tasks by viewModel.tasks.collectAsState()
    val task = tasks.find { it.id == taskId } ?: return
    
    val logs by viewModel.getLogsByTaskId(taskId).collectAsState(initial = emptyList())
    
    var isEditMode by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var editingLog by remember { mutableStateOf<Log?>(null) }
    
    var editStep by remember { mutableStateOf(task.step.toString()) }
    var editDescription by remember { mutableStateOf(task.description) }
    
    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = {
                            if (isEditMode) {
                                // 如果在编辑模式，先退出编辑模式
                                isEditMode = false
                            } else {
                                // 否则退出详情页
                                onBack()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                    
                    Text(
                        text = if (isEditMode) "Edit Task" else "Task Details",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Surface(
                        onClick = {
                            if (isEditMode) {
                                viewModel.updateTask(
                                    task.copy(
                                        step = editStep.toIntOrNull() ?: task.step,
                                        description = editDescription
                                    )
                                )
                                isEditMode = false
                            } else {
                                isEditMode = true
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (isEditMode) Icons.Default.Check else Icons.Default.Edit,
                                contentDescription = if (isEditMode) "Save" else "Edit",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        // 使用 Crossfade 实现编辑模式和详情模式的平滑切换动画
        Crossfade(
            targetState = isEditMode,
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            ),
            label = "edit_detail_transition"
        ) { editMode ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CreamyBackground)
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (editMode) {
                    item {
                        EditTaskCard(
                            task = task,
                            step = editStep,
                            onStepChange = { editStep = it },
                            description = editDescription,
                            onDescriptionChange = { editDescription = it },
                            onDelete = { showDeleteConfirm = true }
                        )
                    }
                } else {
                    item {
                        TaskStatsCard(task = task, logs = logs)
                    }
                    item {
                        TrendChartCard(task = task, logs = logs)
                    }
                    item {
                        LogsSection(
                            task = task,
                            logs = logs,
                            onDeleteLog = { viewModel.deleteLog(it) },
                            onEditLog = { editingLog = it }
                        )
                    }
                }
            }
        }
    }
    
    // 时间编辑对话框
    editingLog?.let { log ->
        EditTimeDialog(
            log = log,
            onDismiss = { editingLog = null },
            onConfirm = { newTimestamp ->
                viewModel.updateLog(log.copy(timestamp = newTimestamp))
                editingLog = null
            }
        )
    }
    
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            shape = RoundedCornerShape(32.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Outlined.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                    Text(
                        "Confirm Action",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Text(
                    "Are you sure you want to delete this task? All history will be cleared. This action cannot be undone.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Surface(
                    onClick = {
                        viewModel.deleteTask(task)
                        onBack()
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        "Confirm Delete",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            dismissButton = {
                Surface(
                    onClick = { showDeleteConfirm = false },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Text(
                        "Cancel",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        )
    }
}

@Composable
fun TaskStatsCard(task: Task, logs: List<Log>) {
    val totalCount = try {
        val sum = logs.sumOf { it.amount.toLong() }
        // 防止溢出
        if (sum > Int.MAX_VALUE) Int.MAX_VALUE else sum.toInt()
    } catch (e: Exception) {
        0
    }
    val activeDays = try {
        logs.map { DateUtils.formatDate(it.timestamp) }.distinct().size
    } catch (e: Exception) {
        0
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItemWithUnit("Total", totalCount.toString(), task.unit)
            StatItem("Days", activeDays.toString())
            StatItem("Step", task.step.toString())
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = value,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun StatItemWithUnit(label: String, value: String, unit: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = value,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "$label ($unit)",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun TrendChartCard(task: Task, logs: List<Log>) {
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
    
    val (chartData, dateLabels) = remember(logs) {
        val data = mutableListOf<Float>()
        val labels = mutableListOf<String>()
        val today = Date()
        val calendar = Calendar.getInstance()
        
        for (i in 13 downTo 0) {
            calendar.time = today
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val dateStr = DateUtils.formatDate(calendar.timeInMillis)
            val dayAmount = try {
                val sum = logs
                    .filter { DateUtils.formatDate(it.timestamp) == dateStr }
                    .sumOf { it.amount.toLong() }
                // 防止溢出
                if (sum > Int.MAX_VALUE) Int.MAX_VALUE else sum.toInt()
            } catch (e: Exception) {
                0
            }
            data.add(dayAmount.toFloat())
            // 只显示天数，格式如 "25"
            labels.add(calendar.get(Calendar.DAY_OF_MONTH).toString())
        }
        Pair(entryModelOf(*data.toTypedArray()), labels)
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Icon(
                Icons.Default.TrendingUp,
                contentDescription = "Trend",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Chart(
                chart = lineChart(
                    lines = listOf(
                        lineSpec(
                            lineColor = colorObj.stroke,
                            lineThickness = 3.dp
                        )
                    )
                ),
                model = chartData,
                startAxis = rememberStartAxis(
                    valueFormatter = { value, _ ->
                        // y轴不显示小数，只显示整数
                        value.toInt().toString()
                    },
                    guideline = null // 移除网格线
                ),
                bottomAxis = rememberBottomAxis(
                    valueFormatter = { value, _ ->
                        // x轴显示日期标签
                        val index = value.toInt().coerceIn(0, dateLabels.size - 1)
                        dateLabels[index]
                    },
                    guideline = null // 移除网格线
                )
            )
        }
    }
}

@Composable
fun LogsSection(
    task: Task,
    logs: List<Log>,
    onDeleteLog: (Log) -> Unit,
    onEditLog: (Log) -> Unit
) {
    val groupedLogs = remember(logs) {
        logs.groupBy { DateUtils.formatDate(it.timestamp) }
            .toList()
            .sortedByDescending { it.first }
    }
    
    // 为每个日期组维护独立的展开状态
    var expandedDates by remember { mutableStateOf(setOf<String>()) }
    
    Column {
        Spacer(modifier = Modifier.height(4.dp))
        
        if (groupedLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.List,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
            }
        } else {
            groupedLogs.forEach { (date, dayLogs) ->
                val isExpanded = expandedDates.contains(date)
                
                Column(
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    // 日期组标题，可点击展开/收起
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedDates = if (isExpanded) {
                                    expandedDates - date
                                } else {
                                    expandedDates + date
                                }
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = date,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 8.dp)
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                    
                    // 展开时显示该日期的所有记录，添加平滑动画
                    val springSpecFloat = spring<Float>(
                        dampingRatio = 0.8f, // 阻尼比，值越小越平滑
                        stiffness = 200f // 刚度，值越小越平滑
                    )
                    val springSpecIntSize = spring<androidx.compose.ui.unit.IntSize>(
                        dampingRatio = 0.8f,
                        stiffness = 200f
                    )
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = fadeIn(animationSpec = springSpecFloat) + expandVertically(
                            expandFrom = Alignment.Top,
                            animationSpec = springSpecIntSize
                        ),
                        exit = fadeOut(animationSpec = springSpecFloat) + shrinkVertically(
                            shrinkTowards = Alignment.Top,
                            animationSpec = springSpecIntSize
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            dayLogs.forEach { log ->
                                SwipeableLogItem(
                                    log = log,
                                    onDelete = { onDeleteLog(log) },
                                    onEdit = { onEditLog(log) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditTaskCard(
    task: Task,
    step: String,
    onStepChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column {
                Text(
                    text = "Step Amount",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = step,
                    onValueChange = onStepChange,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.AddCircleOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                )
            }
            Column {
                Text(
                    text = "Description",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    maxLines = 4,
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                )
            }
            Surface(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Delete This Task",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

