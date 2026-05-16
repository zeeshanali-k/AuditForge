package com.devscion.auditforge.domain.usecase.sessions

import com.devscion.auditforge.domain.model.CreateSessionRequest
import com.devscion.auditforge.domain.model.Session
import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.data.repository.SessionRepository
import org.koin.core.annotation.Factory

@Factory
class CreateSessionUseCase(private val sessionRepository: SessionRepository) {
    suspend operator fun invoke(
        name: String,
        description: String? = null,
        targetEnvironment: String? = null,
    ): ApiResult<Session> = sessionRepository.createSession(
        CreateSessionRequest(
            name = name,
            description = description,
            targetEnvironment = targetEnvironment
        )
    )
}
