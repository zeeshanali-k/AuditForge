package com.devscion.auditforge.di

import com.devscion.auditforge.AppFlavor
import com.devscion.auditforge.data.network.AuthApiService
import com.devscion.auditforge.data.network.SessionApiService
import com.devscion.auditforge.data.repository.AuthRepository
import com.devscion.auditforge.data.repository.AuthRepositoryImpl
import com.devscion.auditforge.data.repository.MockAuthRepository
import com.devscion.auditforge.data.repository.MockSessionRepository
import com.devscion.auditforge.data.repository.SessionRepository
import com.devscion.auditforge.data.repository.SessionRepositoryImpl
import com.devscion.auditforge.data.storage.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
@ComponentScan("com.devscion.auditforge")
class AppModule {

    @Singleton
    fun provideTokenStorage() = TokenStorage()

    @Singleton
    fun provideHttpClient() = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }
    }

    @Singleton
    fun provideAuthRepository(authApiService: AuthApiService): AuthRepository =
        if (AppFlavor.USE_MOCK_DATA) MockAuthRepository()
        else AuthRepositoryImpl(authApiService)

    @Singleton
    fun provideSessionRepository(sessionApiService: SessionApiService): SessionRepository =
        if (AppFlavor.USE_MOCK_DATA) MockSessionRepository()
        else SessionRepositoryImpl(sessionApiService)
}
