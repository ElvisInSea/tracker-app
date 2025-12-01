package com.tracker.app

import android.app.Application
import android.util.Log
import com.tracker.app.util.LogUtils
import java.io.PrintWriter
import java.io.StringWriter

class TrackerApplication : Application() {
    
    private var defaultExceptionHandler: Thread.UncaughtExceptionHandler? = null
    
    override fun onCreate() {
        super.onCreate()
        
        // 保存系统默认的异常处理器
        defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        // 设置全局未捕获异常处理器
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            handleUncaughtException(thread, exception)
        }
        
        LogUtils.d("TrackerApplication", "Application started, global exception handler installed")
    }
    
    private fun handleUncaughtException(thread: Thread, exception: Throwable) {
        try {
            // 获取完整的堆栈跟踪信息
            val stackTrace = StringWriter()
            exception.printStackTrace(PrintWriter(stackTrace))
            val errorReport = """
                ============================================
                未捕获的异常 - 应用崩溃
                ============================================
                线程: ${thread.name}
                异常类型: ${exception.javaClass.name}
                异常消息: ${exception.message}
                
                堆栈跟踪:
                ${stackTrace.toString()}
                ============================================
            """.trimIndent()
            
            // 记录到 Logcat（错误日志始终输出）
            LogUtils.e("TrackerApp", "CRASH: ${exception.javaClass.simpleName}: ${exception.message}")
            LogUtils.e("TrackerApp", "Stack trace:", exception)
            
            // 打印完整错误报告
            LogUtils.e("TrackerApp", errorReport)
            
        } catch (e: Exception) {
            LogUtils.e("TrackerApp", "Error in exception handler", e)
        } finally {
            // 调用系统默认处理器（会显示崩溃对话框）
            defaultExceptionHandler?.uncaughtException(thread, exception)
        }
    }
}

