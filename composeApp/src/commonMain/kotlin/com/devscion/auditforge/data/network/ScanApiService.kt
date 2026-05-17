package com.devscion.auditforge.data.network

import com.devscion.auditforge.data.storage.TokenStorage
import com.devscion.auditforge.domain.model.CancelScanResponse
import com.devscion.auditforge.domain.model.RuleSeverity
import com.devscion.auditforge.domain.model.Scan
import com.devscion.auditforge.domain.model.ScanProgressEvent
import com.devscion.auditforge.domain.model.ScanStatus
import com.devscion.auditforge.domain.model.SseCompletedData
import com.devscion.auditforge.domain.model.SseErrorData
import com.devscion.auditforge.domain.model.SseFindingData
import com.devscion.auditforge.domain.model.SseProgressData
import com.devscion.auditforge.domain.model.TriggerScanRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.utils.io.readLine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Singleton

@Singleton
class ScanApiService(
    private val http: AuditForgeHttpClient,
    private val rawClient: HttpClient,
    private val tokenStorage: TokenStorage,
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    suspend fun triggerScan(sessionId: String, request: TriggerScanRequest): ApiResult<Scan> = try {
        val response = http.post("sessions/$sessionId/scans") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        when (response.status) {
            HttpStatusCode.Accepted -> ApiResult.Success(response.body())
            HttpStatusCode.Unauthorized -> ApiResult.Unauthorized
            HttpStatusCode.Conflict -> ApiResult.Error(
                "A scan is already running for this session",
                409
            )

            HttpStatusCode.UnprocessableEntity -> ApiResult.Error(
                "Cannot start scan: no uploads or invalid policy packs",
                422,
            )

            else -> ApiResult.Error(
                "Failed to start scan (${response.status.value})",
                response.status.value
            )
        }
    } catch (e: Exception) {
        ApiResult.NetworkError
    }

    suspend fun getScan(sessionId: String, scanId: String): ApiResult<Scan> = try {
        val response = http.get("sessions/$sessionId/scans/$scanId")
        when (response.status) {
            HttpStatusCode.OK -> ApiResult.Success(response.body())
            HttpStatusCode.Unauthorized -> ApiResult.Unauthorized
            HttpStatusCode.NotFound -> ApiResult.Error("Scan not found", 404)
            else -> ApiResult.Error(
                "Failed to load scan (${response.status.value})",
                response.status.value
            )
        }
    } catch (e: Exception) {
        ApiResult.NetworkError
    }

    suspend fun cancelScan(sessionId: String, scanId: String): ApiResult<CancelScanResponse> = try {
        val response = http.post("sessions/$sessionId/scans/$scanId/cancel")
        when (response.status) {
            HttpStatusCode.OK -> ApiResult.Success(response.body())
            HttpStatusCode.Unauthorized -> ApiResult.Unauthorized
            HttpStatusCode.NotFound -> ApiResult.Error("Scan not found", 404)
            else -> ApiResult.Error(
                "Failed to cancel scan (${response.status.value})",
                response.status.value
            )
        }
    } catch (e: Exception) {
        ApiResult.NetworkError
    }

    // Streams scan progress by opening the SSE endpoint and parsing text/event-stream.
    // Falls back to polling getScan if streaming is not supported by the engine.
    fun observeScanProgress(sessionId: String, scanId: String): Flow<ScanProgressEvent> = flow {
        val token = tokenStorage.getToken()
        try {
            val response = rawClient.get(
                "${AppConfig.API_BASE_URL}sessions/$sessionId/scans/$scanId/progress",
            ) {
                token?.let { parameter("token", it) }
                header(HttpHeaders.Accept, "text/event-stream")
                header(HttpHeaders.CacheControl, "no-cache")
            }
            val channel = response.bodyAsChannel()
            var currentEvent: String? = null

            while (!channel.isClosedForRead) {
                val line = channel.readLine() ?: break
                when {
                    line.startsWith("event:") -> currentEvent = line.removePrefix("event:").trim()
                    line.startsWith("data:") -> {
                        val data = line.removePrefix("data:").trim()
                        parseSseEvent(currentEvent, data)?.let { emit(it) }
                        if (currentEvent == "completed" || currentEvent == "error") return@flow
                    }

                    line.isEmpty() -> currentEvent = null
                }
            }
        } catch (_: Exception) {
            // Streaming not supported by this engine — fall back to polling
            pollForProgress(sessionId, scanId)
        }
    }

    private suspend fun kotlinx.coroutines.flow.FlowCollector<ScanProgressEvent>.pollForProgress(
        sessionId: String,
        scanId: String,
    ) {
        while (true) {
            delay(2_000)
            val result = getScan(sessionId, scanId)
            if (result !is ApiResult.Success) break
            val scan = result.data
            emit(
                ScanProgressEvent.Progress(
                    scan.progressPercent,
                    scan.currentStep ?: "",
                    scan.findingsSoFar
                )
            )
            when (scan.status) {
                ScanStatus.Completed -> {
                    emit(ScanProgressEvent.Completed(scan.id, scan.findingsSoFar, 0))
                    return
                }

                ScanStatus.Failed -> {
                    emit(ScanProgressEvent.ScanError("SCAN_FAILED", "Scan failed"))
                    return
                }

                ScanStatus.Cancelled -> return
                else -> Unit
            }
        }
    }

    private fun parseSseEvent(eventType: String?, data: String): ScanProgressEvent? =
        when (eventType) {
            "progress" -> runCatching {
                val d = json.decodeFromString<SseProgressData>(data)
                ScanProgressEvent.Progress(d.progressPercent, d.currentStep, d.findingsSoFar)
            }.getOrNull()

            "finding" -> runCatching {
                val d = json.decodeFromString<SseFindingData>(data)
                val severity =
                    RuleSeverity.entries.find { it.name.lowercase() == d.severity.lowercase() }
                        ?: RuleSeverity.Low
                ScanProgressEvent.Finding(d.id, severity, d.ruleId, d.title)
            }.getOrNull()

            "completed" -> runCatching {
                val d = json.decodeFromString<SseCompletedData>(data)
                ScanProgressEvent.Completed(d.scanId, d.totalFindings, d.overallScore)
            }.getOrNull()

            "error" -> runCatching {
                val d = json.decodeFromString<SseErrorData>(data)
                ScanProgressEvent.ScanError(d.code, d.message)
            }.getOrNull()

            else -> null
        }
}
