package com.example.coreops.di

import com.example.coreops.data.remote.AuthInterceptor
import com.example.coreops.data.remote.api.AuthApi
import com.example.coreops.data.remote.api.ProjectsApi
import com.example.coreops.data.remote.api.TasksApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * Модуль забезпечення залежностей для мережевого шару.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // Базова URL-адреса локального бекенду.
    // 10.0.2.2 використовується в емуляторі Android для доступу до localhost комп'ютера.
    private const val BASE_URL = "http://10.0.2.2:8000/"

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            // Встановлення рівня BODY дозволяє бачити повні тіла запитів та відповідей у Logcat
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        // Створення реалізації інтерфейсу AuthApi
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideProjectsApi(retrofit: Retrofit): ProjectsApi {
        return retrofit.create(ProjectsApi::class.java)
    }

    @Provides
    @Singleton
    fun provideTasksApi(retrofit: Retrofit): TasksApi {
        return retrofit.create(TasksApi::class.java)
    }
}