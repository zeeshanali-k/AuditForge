package com.devscion.auditforge.domain.usecase.auth

import com.devscion.auditforge.domain.model.LoginResponse
import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.data.repository.AuthRepository
import org.koin.core.annotation.Factory

@Factory
class LoginUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): ApiResult<LoginResponse> =
        authRepository.login(email, password)
}
