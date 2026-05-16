package com.devscion.auditforge.domain.usecase

import com.devscion.auditforge.data.model.LoginResponse
import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.data.network.AuthApiService
import org.koin.core.annotation.Factory

@Factory
class LoginUseCase(private val authService: AuthApiService) {

    suspend operator fun invoke(email: String, password: String): ApiResult<LoginResponse> =
        authService.login(email, password)
}
