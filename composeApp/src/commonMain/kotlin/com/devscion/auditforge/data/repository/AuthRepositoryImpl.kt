package com.devscion.auditforge.data.repository

import com.devscion.auditforge.data.model.LoginResponse
import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.data.network.AuthApiService

class AuthRepositoryImpl(
    private val authApiService: AuthApiService,
) : AuthRepository {
    override suspend fun login(email: String, password: String): ApiResult<LoginResponse> =
        authApiService.login(email, password)
}
