package com.example.coreops.di

import com.example.coreops.data.repository.ProjectRepositoryImpl
import com.example.coreops.domain.repository.ProjectRepository
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
}