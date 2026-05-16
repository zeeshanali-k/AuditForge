package com.devscion.auditforge.data.storage

import kotlinx.browser.sessionStorage

actual class TokenStorage actual constructor() {
    actual fun saveToken(token: String) {
        sessionStorage.setItem(KEY_TOKEN, token)
    }

    actual fun getToken(): String? {
        return sessionStorage.getItem(KEY_TOKEN)
    }

    actual fun clearToken() {
        sessionStorage.removeItem(KEY_TOKEN)
    }

    private companion object {
        const val KEY_TOKEN = "af_auth_token"
    }
}
