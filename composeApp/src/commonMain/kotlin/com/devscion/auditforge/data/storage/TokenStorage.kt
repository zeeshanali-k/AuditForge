package com.devscion.auditforge.data.storage

expect class TokenStorage() {
    fun saveToken(token: String)
    fun getToken(): String?
    fun clearToken()
}
