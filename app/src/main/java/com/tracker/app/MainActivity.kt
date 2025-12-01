package com.tracker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tracker.app.data.AppDatabase
import com.tracker.app.ui.theme.TrackerAppTheme
import com.tracker.app.util.LogUtils
import com.tracker.app.viewmodel.TrackerViewModel
import com.tracker.app.viewmodel.TrackerViewModelFactory

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        LogUtils.d(TAG, "onCreate: Starting MainActivity")
        
        try {
            LogUtils.d(TAG, "onCreate: Initializing database")
            val database = AppDatabase.getDatabase(applicationContext)
            LogUtils.d(TAG, "onCreate: Database initialized successfully")
            
            LogUtils.d(TAG, "onCreate: Creating ViewModelFactory")
            val viewModelFactory = TrackerViewModelFactory(database)
            LogUtils.d(TAG, "onCreate: ViewModelFactory created successfully")
            
            LogUtils.d(TAG, "onCreate: Setting content")
            setContent {
                TrackerAppTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        TrackerApp(viewModelFactory = viewModelFactory)
                    }
                }
            }
            LogUtils.d(TAG, "onCreate: Content set successfully")
            
        } catch (e: Exception) {
            LogUtils.e(TAG, "onCreate: Fatal error during initialization", e)
            e.printStackTrace()
            throw e
        }
    }
    
    override fun onStart() {
        super.onStart()
        LogUtils.d(TAG, "onStart")
    }
    
    override fun onResume() {
        super.onResume()
        LogUtils.d(TAG, "onResume")
    }
    
    override fun onPause() {
        super.onPause()
        LogUtils.d(TAG, "onPause")
    }
    
    override fun onStop() {
        super.onStop()
        LogUtils.d(TAG, "onStop")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        LogUtils.d(TAG, "onDestroy")
    }
}

