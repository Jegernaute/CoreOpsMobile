package com.example.coreops.di

import com.example.coreops.data.repository.NotificationRepositoryImpl
import com.example.coreops.data.repository.ProjectRepositoryImpl
import com.example.coreops.data.repository.TaskRepositoryImpl
import com.example.coreops.domain.repository.NotificationRepository
import com.example.coreops.domain.repository.ProjectRepository
import com.example.coreops.domain.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Модуль для надання залежностей репозиторіїв.
 * Використовує abstract class та @Binds, оскільки це оптимізованіший
 * спосіб зв'язати інтерфейс із його імплементацією в Hilt.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProjectRepository(
        projectRepositoryImpl: ProjectRepositoryImpl
    ): ProjectRepository

    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        taskRepositoryImpl: TaskRepositoryImpl
    ): TaskRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        notificationRepositoryImpl: NotificationRepositoryImpl
    ): NotificationRepository

}