package com.devscion.auditforge.data.network

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()
    data object Unauthorized : ApiResult<Nothing>()
    data object NetworkError : ApiResult<Nothing>()
}

inline fun <T, R> ApiResult<T>.map(transform: (T) -> R): ApiResult<R> = when (this) {
    is ApiResult.Success -> ApiResult.Success(transform(data))
    is ApiResult.Error -> this
    ApiResult.Unauthorized -> ApiResult.Unauthorized
    ApiResult.NetworkError -> ApiResult.NetworkError
}

fun <T> ApiResult<T>.getOrNull(): T? = (this as? ApiResult.Success)?.data
