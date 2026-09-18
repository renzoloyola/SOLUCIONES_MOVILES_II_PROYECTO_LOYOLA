package com.app.changescout.data.api

import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient

/** Se inyecta desde el modulo de dependencias, nunca desde una pantalla. */
data class ConfiguracionRed(val timeoutMillis: Long = 3_000L) {
    init { require(timeoutMillis > 0) { "El timeout debe ser positivo" } }
}

class FabricaClienteHttp(private val configuracion: ConfiguracionRed) {
    fun crear(): OkHttpClient = OkHttpClient.Builder()
        .callTimeout(configuracion.timeoutMillis, TimeUnit.MILLISECONDS)
        .connectTimeout(configuracion.timeoutMillis, TimeUnit.MILLISECONDS)
        .readTimeout(configuracion.timeoutMillis, TimeUnit.MILLISECONDS)
        .writeTimeout(configuracion.timeoutMillis, TimeUnit.MILLISECONDS)
        .retryOnConnectionFailure(false)
        .build()
}
