package com.example.coreops.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.example.coreops.data.local.AuthPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * DataStoreModule - це інструкція для Dagger Hilt.
 * Тут  інструкція Hilt, як саме створювати DataStore та AuthPreferences,
 * щоб потім він міг автоматично підставляти їх туди, де вони потрібні (Dependency Injection).
 */
@Module
@InstallIn(SingletonComponent::class) // Означає, що ці залежності житимуть стільки ж, скільки весь додаток
object DataStoreModule {

    private const val AUTH_PREFERENCES_NAME = "auth_preferences"

    /**
     * Метод-провайдер, який створює і повертає єдиний екземпляр DataStore.
     * @param context - контекст додатку, потрібен для доступу до файлової системи пристрою.
     */
    @Provides
    @Singleton // Гарантує, що DataStore буде створено лише один раз (Singleton паттерн)
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile(AUTH_PREFERENCES_NAME) }
        )
    }

    /**
     * Провайдер для нашого менеджера токенів.
     * Hilt автоматично візьме DataStore, створений у методі вище,
     * і передасть його сюди в якості аргументу.
     */
    @Provides
    @Singleton
    fun provideAuthPreferences(dataStore: DataStore<Preferences>): AuthPreferences {
        return AuthPreferences(dataStore)
    }
}