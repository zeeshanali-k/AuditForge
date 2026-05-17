package com.devscion.auditforge.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    @SerialName("email") val email: String,
    @SerialName("password") val password: String,
)

@Serializable
data class LoginResponse(
    @SerialName("token") val token: String,
    @SerialName("user") val user: UserInfo,
)

@Serializable
data class UserInfo(
    @SerialName("id") val id: String,
    @SerialName("email") val email: String,
    @SerialName("name") val name: String,
)
