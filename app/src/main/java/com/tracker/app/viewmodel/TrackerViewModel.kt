package com.tracker.app.viewmodel

import android.util.Log as AndroidLog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tracker.app.data.*
import com.tracker.app.ui.screen.HomeUiState
import com.tracker.app.ui.theme.TaskColors
import com.tracker.app.util.IdGenerator
import com.tracker.app.util.DateUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class TrackerViewModel(private val database: AppDatabase) : ViewModel() {
    private val taskDao = database.taskDao()
    private val logDao = database.logDao()
    
    val tasks: StateFlow<List<Task>> = taskDao.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val allLogs: StateFlow<List<Log>> = logDao.getAllLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // UI State for HomeScreen
    private val _isInitialLoad = MutableStateFlow(true)
    
    val homeUiState: StateFlow<HomeUiState> = combine(
        tasks,
        _isInitialLoad
    ) { tasksList, isInitialLoad ->
        when {
            isInitialLoad -> HomeUiState.Loading
            tasksList.isEmpty() -> HomeUiState.Empty
            else -> HomeUiState.Success(tasksList)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        HomeUiState.Loading
    )
    
    init {
        // Mark initial load as complete after first real database emission
        viewModelScope.launch {
            // Collect from the source flow (not the StateFlow) to detect first real emission
            taskDao.getAllTasks()
                .onEach { tasksList ->
                    // Once we've received any emission, mark loading as complete
                    if (_isInitialLoad.value) {
                        _isInitialLoad.value = false
                    }
                }
                .take(1) // Only take the first emission
                .collect()
        }
    }
    
    fun getLogsByTaskId(taskId: String): Flow<List<Log>> {
        return logDao.getLogsByTaskId(taskId)
    }
    
    fun createTask(name: String, unit: String, step: Int, target: Int) {
        viewModelScope.launch {
            val colorIndex = tasks.value.size % TaskColors.size
            val task = Task(
                id = IdGenerator.generateId(),
                name = name,
                unit = unit,
                step = step,
                target = target,
                colorIndex = colorIndex
            )
            taskDao.insertTask(task)
        }
    }
    
    fun updateTask(task: Task) {
        viewModelScope.launch {
            taskDao.updateTask(task)
        }
    }
    
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskDao.deleteTask(task)
            logDao.deleteLogsByTaskId(task.id)
        }
    }
    
    private val checkInMutex = Mutex()
    private val checkingInTasks = mutableSetOf<String>()
    
    fun checkIn(taskId: String, amount: Int) {
        AndroidLog.d("TrackerViewModel", "checkIn called: taskId=$taskId, amount=$amount")
        
        // 验证参数有效性
        if (taskId.isBlank() || amount <= 0) {
            AndroidLog.w("TrackerViewModel", "checkIn: Invalid parameters, returning early")
            return
        }
        
        viewModelScope.launch {
            try {
                // 防止同一个事务被重复打卡
                checkInMutex.withLock {
                    if (checkingInTasks.contains(taskId)) {
                        AndroidLog.w("TrackerViewModel", "checkIn: Already checking in task $taskId, skipping")
                        return@launch
                    }
                    checkingInTasks.add(taskId)
                    AndroidLog.d("TrackerViewModel", "checkIn: Added task $taskId to checking set")
                }
                
                try {
                    AndroidLog.d("TrackerViewModel", "checkIn: Fetching task from database")
                    // 验证事务是否存在
                    val task = taskDao.getTaskById(taskId)
                    if (task == null) {
                        AndroidLog.w("TrackerViewModel", "checkIn: Task not found: $taskId")
                        return@launch
                    }
                    AndroidLog.d("TrackerViewModel", "checkIn: Task found: ${task.name}, target=${task.target}")
                    
                    // 验证amount值，防止溢出
                    val safeAmount = amount.coerceAtLeast(0).coerceAtMost(Int.MAX_VALUE)
                    if (safeAmount <= 0) {
                        AndroidLog.w("TrackerViewModel", "checkIn: Invalid safeAmount: $safeAmount")
                        return@launch
                    }
                    AndroidLog.d("TrackerViewModel", "checkIn: Safe amount: $safeAmount")
                    
                    val logId = IdGenerator.generateId()
                    val timestamp = System.currentTimeMillis()
                    AndroidLog.d("TrackerViewModel", "checkIn: Creating log: id=$logId, timestamp=$timestamp")
                    
                    val log = Log(
                        id = logId,
                        taskId = taskId,
                        amount = safeAmount,
                        timestamp = timestamp
                    )
                    
                    AndroidLog.d("TrackerViewModel", "checkIn: Inserting log into database")
                    logDao.insertLog(log)
                    AndroidLog.d("TrackerViewModel", "checkIn: Successfully inserted log")
                    
                } catch (e: Exception) {
                    // 捕获所有异常，防止崩溃
                    AndroidLog.e("TrackerViewModel", "checkIn: Exception in try block", e)
                    e.printStackTrace()
                } finally {
                    checkInMutex.withLock {
                        checkingInTasks.remove(taskId)
                        AndroidLog.d("TrackerViewModel", "checkIn: Removed task $taskId from checking set")
                    }
                }
            } catch (e: Exception) {
                AndroidLog.e("TrackerViewModel", "checkIn: Exception in coroutine", e)
                e.printStackTrace()
                // 确保在异常情况下也清理状态
                try {
                    checkInMutex.withLock {
                        checkingInTasks.remove(taskId)
                    }
                } catch (cleanupException: Exception) {
                    AndroidLog.e("TrackerViewModel", "checkIn: Exception during cleanup", cleanupException)
                }
            }
        }
    }
    
    fun deleteLog(log: Log) {
        viewModelScope.launch {
            logDao.deleteLog(log)
        }
    }
    
    fun getLogsByDateRange(startTime: Long, endTime: Long): Flow<List<Log>> {
        return logDao.getLogsByDateRange(startTime, endTime)
    }
    
    // 用于导入数据
    suspend fun insertTaskDirectly(task: Task) {
        taskDao.insertTask(task)
    }
    
    suspend fun insertLogDirectly(log: Log) {
        logDao.insertLog(log)
    }
    
    suspend fun deleteAllTasks() {
        val allTasks = taskDao.getAllTasks().first()
        allTasks.forEach { taskDao.deleteTask(it) }
    }
    
    suspend fun deleteAllLogs() {
        val allLogs = logDao.getAllLogs().first()
        allLogs.forEach { logDao.deleteLog(it) }
    }
}

class TrackerViewModelFactory(private val database: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrackerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TrackerViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

