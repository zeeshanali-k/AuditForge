package com.devscion.auditforge.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class SessionStatus {
    @SerialName("created") Created,
    @SerialName("scanning") Scanning,
    @SerialName("completed") Completed,
    @SerialName("failed") Failed,
}

@Serializable
enum class TargetEnvironment {
    @SerialName("development") Development,
    @SerialName("staging") Staging,
    @SerialName("production") Production,
}

@Serializable
data class SessionSummary(
    val id: String,
    val name: String,
    val status: SessionStatus,
    @SerialName("finding_count") val findingCount: Int = 0,
    @SerialName("overall_score") val overallScore: Int? = null,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class Session(
    val id: String,
    val name: String,
    val description: String? = null,
    @SerialName("target_environment") val targetEnvironment: TargetEnvironment? = null,
    val status: SessionStatus,
    @SerialName("finding_count") val findingCount: Int = 0,
    @SerialName("overall_score") val overallScore: Int? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("created_by") val createdBy: String,
)

@Serializable
data class Pagination(
    val page: Int,
    @SerialName("page_size") val pageSize: Int,
    @SerialName("total_items") val totalItems: Int,
    @SerialName("total_pages") val totalPages: Int,
)

@Serializable
data class SessionListResponse(
    val data: List<SessionSummary>,
    val pagination: Pagination,
)

@Serializable
data class CreateSessionRequest(
    val name: String,
    val description: String? = null,
    @SerialName("target_environment") val targetEnvironment: String? = null,
)
