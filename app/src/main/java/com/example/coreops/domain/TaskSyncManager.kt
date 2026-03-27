package com.example.coreops.domain

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskSyncManager @Inject constructor() {
    // 1. Існуючий канал (для миттєвого оновлення списків на екрані)
    private val _taskUpdates = MutableSharedFlow<Pair<Int, String>>(extraBufferCapacity = 10)
    val taskUpdates = _taskUpdates.asSharedFlow()

    // 2. новий канал (щоб смикати сервер тільки тоді коли дані точно готові)
    private val _serverUpdates = MutableSharedFlow<Unit>(extraBufferCapacity = 10)
    val serverUpdates = _serverUpdates.asSharedFlow()

    suspend fun notifyTaskStatusChanged(taskId: Int, newStatus: String) {
        _taskUpdates.emit(Pair(taskId, newStatus))
    }

    suspend fun triggerServerFetch() {
        _serverUpdates.tryEmit(Unit)
    }
}