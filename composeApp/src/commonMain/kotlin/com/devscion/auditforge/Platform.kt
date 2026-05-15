package com.devscion.auditforge

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform