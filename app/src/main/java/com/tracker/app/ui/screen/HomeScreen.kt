package com.tracker.app.ui.screen

import com.tracker.app.util.LogUtils
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracker.app.data.Task
import com.tracker.app.data.Log
import com.tracker.app.ui.component.Heatmap
import com.tracker.app.ui.component.TaskCard
import com.tracker.app.ui.component.SkeletonTaskCard
import com.tracker.app.ui.screen.HomeUiState
import com.tracker.app.ui.theme.CreamyBackground
import com.tracker.app.util.DateUtils
import com.tracker.app.viewmodel.TrackerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: TrackerViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToTimeline: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.homeUiState.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val logs by viewModel.allLogs.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        floatingActionButton = {
            Surface(
                onClick = { showCreateDialog = true },
                modifier = Modifier
                    .size(64.dp)
                    .shadow(6.dp, CircleShape, spotColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Create Task",
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CreamyBackground)
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // 顶部导航
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Daily",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Track small progress",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        onClick = onNavigateToTimeline,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "Timeline",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Surface(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 事务列表 - 使用 Crossfade 实现平滑过渡
            Crossfade(
                targetState = uiState,
                animationSpec = tween(durationMillis = 300),
                label = "home_state_transition"
            ) { state ->
                when (state) {
                    is HomeUiState.Loading -> {
                        // 全屏骨架加载 - 固定显示 8-10 个骨架项
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(
                                top = 8.dp,
                                bottom = 80.dp // 为右下角的 FloatingActionButton 留出空间
                            )
                        ) {
                            items(8) { // 固定 8 个骨架项，确保填满屏幕
                                SkeletonTaskCard()
                            }
                        }
                    }
                    is HomeUiState.Empty -> {
                        // 空状态视图
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.size(56.dp)
                                    )
                                }
                                Text(
                                    text = "No tasks yet",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(top = 12.dp)
                                )
                            }
                        }
                    }
                    is HomeUiState.Success -> {
                        // 成功状态 - 显示实际数据
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(
                                top = 8.dp,
                                bottom = 80.dp // 为右下角的 FloatingActionButton 留出空间
                            )
                        ) {
                            items(state.tasks) { task ->
                                // 确保事务数据有效
                                if (task.id.isBlank()) return@items
                                
                                TaskCard(
                                    task = task,
                                    logs = logs.filter { it.taskId == task.id },
                                    onCheckIn = { 
                                        try {
                                            LogUtils.d("HomeScreen", "onCheckIn called for task: ${task.id}, name: ${task.name}")
                                            val step = task.step.coerceAtLeast(1).coerceAtMost(Int.MAX_VALUE)
                                            LogUtils.d("HomeScreen", "Calculated step: $step (original: ${task.step})")
                                            if (task.id.isNotBlank() && step > 0) {
                                                LogUtils.d("HomeScreen", "Calling viewModel.checkIn with taskId=${task.id}, step=$step")
                                                viewModel.checkIn(task.id, step)
                                                LogUtils.d("HomeScreen", "viewModel.checkIn call completed")
                                            } else {
                                                LogUtils.w("HomeScreen", "onCheckIn: Invalid parameters, taskId=${task.id}, step=$step")
                                            }
                                        } catch (e: Exception) {
                                            // 捕获异常，防止崩溃
                                            LogUtils.e("HomeScreen", "onCheckIn: Exception occurred", e)
                                            e.printStackTrace()
                                        }
                                    },
                                    onClick = { 
                                        try {
                                            if (task.id.isNotBlank()) {
                                                onNavigateToDetail(task.id)
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    if (showCreateDialog) {
        CreateTaskDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, unit, step, target ->
                viewModel.createTask(name, unit, step, target)
                showCreateDialog = false
            }
        )
    }
}

