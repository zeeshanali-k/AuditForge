package com.devscion.auditforge.data.storage

import java.util.prefs.Preferences

actual class TokenStorage actual constructor() {
    private val prefs: Preferences = Preferences.userRoot().node("com/devscion/auditforge")

    actual fun saveToken(token: String) {
        prefs.put(KEY_TOKEN, token)
        prefs.flush()
    }

    actual fun getToken(): String? {
        return prefs.get(KEY_TOKEN, null)
    }

    actual fun clearToken() {
        prefs.remove(KEY_TOKEN)
        prefs.flush()
    }

    private companion object {
        const val KEY_TOKEN = "auth_token"
    }
}
