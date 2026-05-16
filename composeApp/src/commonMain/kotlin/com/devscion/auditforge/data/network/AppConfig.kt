package com.devscion.auditforge.data.network

object AppConfig {
    // Update this when the backend base URL is known
    const val BASE_URL = "http://localhost:8080"
    const val API_VERSION = "v1"
    val API_BASE_URL = "$BASE_URL/api/$API_VERSION/"
}
