package com.devscion.auditforge.domain.usecase

import com.devscion.auditforge.data.model.SessionListResponse
import com.devscion.auditforge.data.model.SessionStatus
import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.data.repository.SessionRepository
import org.koin.core.annotation.Factory

@Factory
class GetSessionsUseCase(private val sessionRepository: SessionRepository) {

    suspend operator fun invoke(
        page: Int = 1,
        pageSize: Int = 20,
        status: SessionStatus? = null,
    ): ApiResult<SessionListResponse> = sessionRepository.getSessions(page, pageSize, status)
}
