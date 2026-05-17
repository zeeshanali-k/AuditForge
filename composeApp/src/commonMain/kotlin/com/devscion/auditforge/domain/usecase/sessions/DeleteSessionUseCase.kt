package com.devscion.auditforge.domain.usecase.sessions

import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.data.repository.SessionRepository
import org.koin.core.annotation.Factory

@Factory
class DeleteSessionUseCase(private val sessionRepository: SessionRepository) {
    suspend operator fun invoke(sessionId: String): ApiResult<Unit> =
        sessionRepository.deleteSession(sessionId)
}
