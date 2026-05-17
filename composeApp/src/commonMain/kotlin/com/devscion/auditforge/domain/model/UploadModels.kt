package com.devscion.auditforge.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class UploadType {
    @SerialName("codebase")
    Codebase,
    @SerialName("openapi_spec")
    OpenapiSpec,
    @SerialName("config")
    Config,
    @SerialName("db_schema")
    DbSchema,
    @SerialName("env_file")
    EnvFile,
    @SerialName("terraform")
    Terraform,
    @SerialName("kubernetes")
    Kubernetes;

    val allowedExtensions: List<String>
        get() = when (this) {
            Codebase -> listOf("zip", "tar.gz")
            OpenapiSpec -> listOf("json", "yaml", "yml")
            Config -> listOf("yaml", "yml", "json", "toml", "ini")
            DbSchema -> listOf("sql")
            EnvFile -> listOf("env")
            Terraform -> listOf("tf", "zip")
            Kubernetes -> listOf("yaml", "yml", "zip")
        }

    val serialName: String
        get() = when (this) {
            Codebase -> "codebase"
            OpenapiSpec -> "openapi_spec"
            Config -> "config"
            DbSchema -> "db_schema"
            EnvFile -> "env_file"
            Terraform -> "terraform"
            Kubernetes -> "kubernetes"
        }
}

@Serializable
data class UploadSummary(
    val id: String,
    val filename: String,
    @SerialName("upload_type") val uploadType: UploadType,
    @SerialName("size_bytes") val sizeBytes: Long,
    @SerialName("uploaded_at") val uploadedAt: String,
)

@Serializable
data class Upload(
    val id: String,
    @SerialName("session_id") val sessionId: String,
    val filename: String,
    @SerialName("size_bytes") val sizeBytes: Long,
    @SerialName("upload_type") val uploadType: UploadType,
    @SerialName("uploaded_at") val uploadedAt: String,
    @SerialName("checksum_sha256") val checksumSha256: String,
)

@Serializable
data class UploadListResponse(
    val data: List<UploadSummary>,
)
