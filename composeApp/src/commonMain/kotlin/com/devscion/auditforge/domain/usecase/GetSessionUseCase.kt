package com.devscion.auditforge.domain.usecase

import com.devscion.auditforge.data.model.Session
import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.data.repository.SessionRepository
import org.koin.core.annotation.Factory

@Factory
class GetSessionUseCase(private val sessionRepository: SessionRepository) {

    suspend operator fun invoke(sessionId: String): ApiResult<Session> =
        sessionRepository.getSession(sessionId)
}
