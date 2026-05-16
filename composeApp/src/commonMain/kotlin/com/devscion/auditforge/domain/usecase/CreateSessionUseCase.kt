package com.devscion.auditforge.domain.usecase

import com.devscion.auditforge.data.model.CreateSessionRequest
import com.devscion.auditforge.data.model.Session
import com.devscion.auditforge.data.model.TargetEnvironment
import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.data.repository.SessionRepository
import org.koin.core.annotation.Factory

@Factory
class CreateSessionUseCase(private val sessionRepository: SessionRepository) {

    suspend operator fun invoke(
        name: String,
        description: String?,
        environment: TargetEnvironment?,
    ): ApiResult<Session> = sessionRepository.createSession(
        CreateSessionRequest(
            name = name,
            description = description?.ifBlank { null },
            targetEnvironment = environment?.name?.lowercase(),
        )
    )
}
