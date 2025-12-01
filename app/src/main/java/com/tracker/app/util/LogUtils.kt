package com.tracker.app.util

import android.util.Log
import com.tracker.app.BuildConfig

object LogUtils {
    private const val DEFAULT_TAG = "TrackerApp"
    
    @JvmStatic
    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }
    
    @JvmStatic
    fun d(tag: String, message: String, throwable: Throwable) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message, throwable)
        }
    }
    
    @JvmStatic
    fun e(tag: String, message: String) {
        // 错误日志始终输出，即使在 Release 版本
        Log.e(tag, message)
    }
    
    @JvmStatic
    fun e(tag: String, message: String, throwable: Throwable) {
        // 错误日志始终输出，即使在 Release 版本
        Log.e(tag, message, throwable)
    }
    
    @JvmStatic
    fun w(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, message)
        }
    }
    
    @JvmStatic
    fun w(tag: String, message: String, throwable: Throwable) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, message, throwable)
        }
    }
    
    @JvmStatic
    fun i(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, message)
        }
    }
    
    @JvmStatic
    fun v(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.v(tag, message)
        }
    }
}

