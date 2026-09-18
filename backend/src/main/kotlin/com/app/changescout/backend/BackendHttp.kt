package com.app.changescout.backend

import io.ktor.client.statement.HttpResponse

data class ErrorResponse(
    val message: String
)

suspend fun HttpResponse.externalErrorDetail(): String = ""
