package com.devscion.auditforge.data.network

import com.devscion.auditforge.domain.model.PolicyFramework
import com.devscion.auditforge.domain.model.PolicyPackDetail
import com.devscion.auditforge.domain.model.PolicyPackListResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.koin.core.annotation.Singleton

@Singleton
class PolicyApiService(private val httpClient: AuditForgeHttpClient) {

    suspend fun getPolicyPacks(framework: PolicyFramework? = null): ApiResult<PolicyPackListResponse> =
        try {
            val response = httpClient.get("policies/packs") {
                framework?.let { parameter("framework", it.name) }
            }
            when (response.status) {
                HttpStatusCode.OK -> ApiResult.Success(response.body())
                HttpStatusCode.Unauthorized -> ApiResult.Unauthorized
                else -> ApiResult.Error(
                    "Failed to load policy packs (${response.status.value})",
                    response.status.value
                )
            }
        } catch (e: Exception) {
            ApiResult.NetworkError
        }

    suspend fun getPolicyPackDetail(packId: String): ApiResult<PolicyPackDetail> = try {
        val response = httpClient.get("policies/packs/$packId")
        when (response.status) {
            HttpStatusCode.OK -> ApiResult.Success(response.body())
            HttpStatusCode.Unauthorized -> ApiResult.Unauthorized
            HttpStatusCode.NotFound -> ApiResult.Error("Policy pack not found", 404)
            else -> ApiResult.Error(
                "Failed to load policy pack (${response.status.value})",
                response.status.value
            )
        }
    } catch (e: Exception) {
        ApiResult.NetworkError
    }
}
