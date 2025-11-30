package com.tracker.app.ui.screen

import com.tracker.app.data.Task

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val tasks: List<Task>) : HomeUiState
    data object Empty : HomeUiState
}

