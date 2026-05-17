package com.devscion.auditforge

expect class ReportDownloader() {
    suspend fun downloadReport(fileName: String, bytes: ByteArray, mimeType: String)
}
