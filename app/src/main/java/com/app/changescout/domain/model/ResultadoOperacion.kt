package com.app.changescout.domain.model

sealed interface ResultadoOperacion<out T> {
    data class Exito<T>(val data: T) : ResultadoOperacion<T>
    data class DatosObsoletos<T>(
        val data: T,
        val causa: ErrorOperacion
    ) : ResultadoOperacion<T>
    data class Fallo(val error: ErrorOperacion) : ResultadoOperacion<Nothing>
}

sealed interface ErrorOperacion {
    val mensaje: String

    data class SinConexion(
        val proveedor: String,
        override val mensaje: String
    ) : ErrorOperacion

    data class ProveedorNoDisponible(
        val proveedor: String,
        override val mensaje: String
    ) : ErrorOperacion

    data class Timeout(
        val proveedor: String,
        override val mensaje: String
    ) : ErrorOperacion

    data class RespuestaInvalida(
        val proveedor: String,
        override val mensaje: String
    ) : ErrorOperacion

    data class SinDatos(
        override val mensaje: String
    ) : ErrorOperacion

    data class Validacion(
        override val mensaje: String
    ) : ErrorOperacion

    data class Desconocido(
        override val mensaje: String
    ) : ErrorOperacion
}
