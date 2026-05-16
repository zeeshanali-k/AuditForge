package com.devscion.auditforge.data.network

import com.devscion.auditforge.data.model.LoginRequest
import com.devscion.auditforge.data.model.LoginResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.koin.core.annotation.Singleton

@Singleton
class AuthApiService(private val httpClient: AuditForgeHttpClient) {

    suspend fun login(email: String, password: String): ApiResult<LoginResponse> {
        return try {
            val response = httpClient.post("auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email, password))
            }
            when (response.status) {
                HttpStatusCode.OK -> ApiResult.Success(response.body())
                HttpStatusCode.Unauthorized -> ApiResult.Error("Invalid email or password", 401)
                HttpStatusCode.UnprocessableEntity -> ApiResult.Error("Invalid request format", 422)
                else -> ApiResult.Error("Sign in failed (${response.status.value})", response.status.value)
            }
        } catch (e: Exception) {
            ApiResult.NetworkError
        }
    }
}
