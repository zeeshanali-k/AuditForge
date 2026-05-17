package com.devscion.auditforge.data.repository

import com.devscion.auditforge.domain.model.LoginResponse
import com.devscion.auditforge.data.network.ApiResult

interface AuthRepository {
    suspend fun login(email: String, password: String): ApiResult<LoginResponse>
}
