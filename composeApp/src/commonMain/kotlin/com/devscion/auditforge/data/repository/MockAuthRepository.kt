package com.devscion.auditforge.data.repository

import com.devscion.auditforge.domain.model.LoginResponse
import com.devscion.auditforge.domain.model.UserInfo
import com.devscion.auditforge.data.network.ApiResult
import kotlinx.coroutines.delay

class MockAuthRepository : AuthRepository {
    override suspend fun login(email: String, password: String): ApiResult<LoginResponse> {
        delay(400)
        return ApiResult.Success(
            LoginResponse(
                token = "mock-jwt-token-${email.hashCode()}",
                user = UserInfo(
                    id = "mock-user-001",
                    email = email,
                    name = email.substringBefore('@').replaceFirstChar { it.uppercase() },
                ),
            )
        )
    }
}
