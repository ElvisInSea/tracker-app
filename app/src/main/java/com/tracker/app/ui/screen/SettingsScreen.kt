package com.tracker.app.ui.screen

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracker.app.data.Task
import com.tracker.app.data.Log
import com.tracker.app.ui.theme.CreamyBackground
import com.tracker.app.util.DateUtils
import com.tracker.app.viewmodel.TrackerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: TrackerViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
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
                        text = "Settings & Data",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(CreamyBackground)
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ExportDataCard(
                    viewModel = viewModel,
                    context = context,
                    scope = scope
                )
            }
            item {
                ImportDataCard(
                    viewModel = viewModel,
                    context = context,
                    scope = scope
                )
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Daily Tracker v1.0",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Text(
                            text = "Creamy UI Edition",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExportDataCard(
    viewModel: TrackerViewModel,
    context: Context,
    scope: CoroutineScope
) {
    // 文件保存选择器
    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    val tasks = viewModel.tasks.first()
                    val logs = viewModel.allLogs.first()
                    
                    val json = JSONObject().apply {
                        put("tasks", JSONArray(tasks.map { task ->
                            JSONObject().apply {
                                put("id", task.id)
                                put("name", task.name)
                                put("unit", task.unit)
                                put("step", task.step)
                                put("target", task.target)
                                put("description", task.description)
                                put("colorIndex", task.colorIndex)
                                put("createdAt", task.createdAt)
                            }
                        }))
                        put("logs", JSONArray(logs.map { log ->
                            JSONObject().apply {
                                put("id", log.id)
                                put("taskId", log.taskId)
                                put("amount", log.amount)
                                put("timestamp", log.timestamp)
                            }
                        }))
                    }
                    
                    // 写入文件
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        outputStream.write(json.toString(2).toByteArray())
                    }
                    
                    Toast.makeText(
                        context,
                        "Data exported successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Default.Download,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "Export Data",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "Export all your tasks and records as a JSON file for backup or migration. Please do not modify the file content.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                lineHeight = 20.sp
            )
            Surface(
                onClick = {
                    // 生成带提示的文件名
                    val dateStr = DateUtils.formatDate(System.currentTimeMillis())
                    val fileName = "backup-${dateStr}-do-not-modify.json"
                    saveFileLauncher.launch(fileName)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save Backup File",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun ImportDataCard(
    viewModel: TrackerViewModel,
    context: Context,
    scope: CoroutineScope
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    var importData by remember { mutableStateOf<Pair<List<Task>, List<Log>>?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // 文件选择器
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    val (tasks, logs) = validateAndParseJsonFile(context, it)
                    importData = Pair(tasks, logs)
                    errorMessage = null
                    showConfirmDialog = true
                } catch (e: Exception) {
                    errorMessage = e.message ?: "Unknown error"
                    Toast.makeText(context, "Validation failed: ${errorMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Default.Upload,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "Import Data",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "Select a JSON backup file to restore data. This will replace all existing tasks and records.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                lineHeight = 20.sp
            )
            if (errorMessage != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = errorMessage ?: "",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            Surface(
                onClick = {
                    filePickerLauncher.launch("application/json")
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Select Backup File",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
    
    // 确认覆盖对话框
    if (showConfirmDialog && importData != null) {
        val (tasks, logs) = importData!!
        AlertDialog(
            onDismissRequest = { 
                showConfirmDialog = false
                importData = null
            },
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
                        "Confirm Import",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "This will replace all existing data with the imported data.",
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                    Text(
                        "• Tasks: ${tasks.size}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        "• Records: ${logs.size}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        "This action cannot be undone.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {
                Surface(
                    onClick = {
                        scope.launch {
                            try {
                                // 清空现有数据
                                viewModel.deleteAllTasks()
                                viewModel.deleteAllLogs()
                                
                                // 导入新数据
                                tasks.forEach { task ->
                                    viewModel.insertTaskDirectly(task)
                                }
                                logs.forEach { log ->
                                    viewModel.insertLogDirectly(log)
                                }
                                
                                Toast.makeText(context, "Import successful!", Toast.LENGTH_SHORT).show()
                                showConfirmDialog = false
                                importData = null
                            } catch (e: Exception) {
                                Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        "Confirm Import",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            dismissButton = {
                Surface(
                    onClick = {
                        showConfirmDialog = false
                        importData = null
                    },
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

// 验证并解析JSON文件
suspend fun validateAndParseJsonFile(
    context: Context,
    uri: Uri
): Pair<List<Task>, List<Log>> = withContext(Dispatchers.IO) {
    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
    val jsonString = inputStream?.use { it.bufferedReader().use { reader -> reader.readText() } }
        ?: throw IllegalArgumentException("Failed to read file")
    
    if (jsonString.isBlank()) {
        throw IllegalArgumentException("File is empty")
    }
    
    val json = try {
        JSONObject(jsonString)
    } catch (e: Exception) {
        throw IllegalArgumentException("Invalid JSON format: ${e.message}")
    }
    
    // 验证必需字段
    if (!json.has("tasks") || !json.has("logs")) {
        throw IllegalArgumentException("Missing required fields: 'tasks' or 'logs'")
    }
    
    val tasksArray = json.getJSONArray("tasks")
    val logsArray = json.getJSONArray("logs")
    
    // 验证并解析tasks
    val tasks = mutableListOf<Task>()
    for (i in 0 until tasksArray.length()) {
        val obj = tasksArray.getJSONObject(i)
        try {
            // 验证必需字段
            val requiredFields = listOf("id", "name", "unit", "step", "target")
            for (field in requiredFields) {
                if (!obj.has(field)) {
                    throw IllegalArgumentException("Task at index $i is missing required field: $field")
                }
            }
            
            // 验证数据类型和范围
            val step = obj.getInt("step")
            val target = obj.getInt("target")
            if (step <= 0) {
                throw IllegalArgumentException("Task '${obj.getString("name")}' has invalid step: $step (must be > 0)")
            }
            if (target <= 0) {
                throw IllegalArgumentException("Task '${obj.getString("name")}' has invalid target: $target (must be > 0)")
            }
            
            tasks.add(
                Task(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    unit = obj.getString("unit"),
                    step = step,
                    target = target,
                    description = obj.optString("description", ""),
                    colorIndex = obj.optInt("colorIndex", 0).coerceAtLeast(0),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()).coerceAtLeast(0)
                )
            )
        } catch (e: Exception) {
            throw IllegalArgumentException("Error parsing task at index $i: ${e.message}")
        }
    }
    
    // 验证并解析logs
    val logs = mutableListOf<Log>()
    val taskIds = tasks.map { it.id }.toSet()
    for (i in 0 until logsArray.length()) {
        val obj = logsArray.getJSONObject(i)
        try {
            // 验证必需字段
            val requiredFields = listOf("id", "taskId", "amount", "timestamp")
            for (field in requiredFields) {
                if (!obj.has(field)) {
                    throw IllegalArgumentException("Log at index $i is missing required field: $field")
                }
            }
            
            val taskId = obj.getString("taskId")
            if (!taskIds.contains(taskId)) {
                throw IllegalArgumentException("Log at index $i references non-existent task: $taskId")
            }
            
            val amount = obj.getInt("amount")
            if (amount <= 0) {
                throw IllegalArgumentException("Log at index $i has invalid amount: $amount (must be > 0)")
            }
            
            val timestamp = obj.getLong("timestamp")
            if (timestamp <= 0) {
                throw IllegalArgumentException("Log at index $i has invalid timestamp: $timestamp (must be > 0)")
            }
            
            logs.add(
                Log(
                    id = obj.getString("id"),
                    taskId = taskId,
                    amount = amount,
                    timestamp = timestamp
                )
            )
        } catch (e: Exception) {
            throw IllegalArgumentException("Error parsing log at index $i: ${e.message}")
        }
    }
    
    Pair(tasks, logs)
}

