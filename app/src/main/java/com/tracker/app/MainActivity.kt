package com.tracker.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tracker.app.data.AppDatabase
import com.tracker.app.ui.theme.TrackerAppTheme
import com.tracker.app.viewmodel.TrackerViewModel
import com.tracker.app.viewmodel.TrackerViewModelFactory

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d(TAG, "onCreate: Starting MainActivity")
        
        try {
            Log.d(TAG, "onCreate: Initializing database")
            val database = AppDatabase.getDatabase(applicationContext)
            Log.d(TAG, "onCreate: Database initialized successfully")
            
            Log.d(TAG, "onCreate: Creating ViewModelFactory")
            val viewModelFactory = TrackerViewModelFactory(database)
            Log.d(TAG, "onCreate: ViewModelFactory created successfully")
            
            Log.d(TAG, "onCreate: Setting content")
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
            Log.d(TAG, "onCreate: Content set successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "onCreate: Fatal error during initialization", e)
            e.printStackTrace()
            throw e
        }
    }
    
    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }
    
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }
    
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }
    
    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }
}

