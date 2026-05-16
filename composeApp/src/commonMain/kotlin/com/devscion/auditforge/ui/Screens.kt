package com.devscion.auditforge.ui

import kotlinx.serialization.Serializable

@Serializable
data object Login

@Serializable
data object SessionList

@Serializable
data class SessionDetail(val sessionId: String)
