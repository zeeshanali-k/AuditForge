package com.devscion.auditforge.data.network

import com.devscion.auditforge.data.storage.TokenStorage
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Singleton

@Singleton
class AuditForgeHttpClient(
    private val tokenStorage: TokenStorage,
    private val http: HttpClient,
) {

    suspend fun get(path: String, block: HttpRequestBuilder.() -> Unit = {}): HttpResponse =
        http.get("${AppConfig.API_BASE_URL}$path") {
            addAuthHeader()
            block()
        }

    suspend fun post(path: String, block: HttpRequestBuilder.() -> Unit = {}): HttpResponse =
        http.post("${AppConfig.API_BASE_URL}$path") {
            addAuthHeader()
            block()
        }

    suspend fun put(path: String, block: HttpRequestBuilder.() -> Unit = {}): HttpResponse =
        http.put("${AppConfig.API_BASE_URL}$path") {
            addAuthHeader()
            block()
        }

    suspend fun patch(path: String, block: HttpRequestBuilder.() -> Unit = {}): HttpResponse =
        http.patch("${AppConfig.API_BASE_URL}$path") {
            addAuthHeader()
            block()
        }

    suspend fun delete(path: String, block: HttpRequestBuilder.() -> Unit = {}): HttpResponse =
        http.delete("${AppConfig.API_BASE_URL}$path") {
            addAuthHeader()
            block()
        }

    private fun HttpRequestBuilder.addAuthHeader() {
        tokenStorage.getToken()?.let { token ->
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }
}
