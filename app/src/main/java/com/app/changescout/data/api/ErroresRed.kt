package com.app.changescout.data.api

import com.app.changescout.domain.model.ErrorOperacion
import java.io.IOException
import java.io.InterruptedIOException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/** OkHttp usa InterruptedIOException para el timeout total de una llamada. */
fun IOException.comoErrorRed(proveedor: String): ErrorOperacion = when (this) {
    is SocketTimeoutException, is InterruptedIOException ->
        ErrorOperacion.Timeout(proveedor, "Tiempo agotado")
    is SocketException, is UnknownHostException ->
        ErrorOperacion.SinConexion(proveedor, "Sin conexión")
    else -> ErrorOperacion.ProveedorNoDisponible(proveedor, "No se pudo completar la conexión")
}
