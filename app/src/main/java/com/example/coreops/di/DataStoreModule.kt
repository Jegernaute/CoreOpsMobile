package com.example.coreops.di

import android.content.Context
import com.example.coreops.data.local.AuthPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    /**
     * Провайдер для  менеджера токенів.
     * Hilt автоматично передасть сюди ApplicationContext,
     * а AuthPreferences сам налаштує EncryptedSharedPreferences.
     */
    @Provides
    @Singleton
    fun provideAuthPreferences(@ApplicationContext context: Context): AuthPreferences {
        return AuthPreferences(context)
    }
}