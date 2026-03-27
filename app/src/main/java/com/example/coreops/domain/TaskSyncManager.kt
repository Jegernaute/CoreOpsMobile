package com.example.coreops.domain

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskSyncManager @Inject constructor() {
    private val _taskUpdates = MutableSharedFlow<Pair<Int, String>>(extraBufferCapacity = 10)
    val taskUpdates = _taskUpdates.asSharedFlow()

    suspend fun notifyTaskStatusChanged(taskId: Int, newStatus: String) {
        _taskUpdates.emit(Pair(taskId, newStatus))
    }
}