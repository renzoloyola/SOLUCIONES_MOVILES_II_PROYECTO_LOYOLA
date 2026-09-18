package com.app.changescout.data.api.apisnet

import com.app.changescout.domain.model.CotizacionTipoCambio
import com.app.changescout.domain.model.ErrorOperacion
import com.app.changescout.domain.model.ResultadoOperacion
import com.app.changescout.domain.repository.ProveedorTipoCambio
import java.io.IOException
import com.app.changescout.data.api.comoErrorRed
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

class ProveedorTipoCambioApisNet @Inject constructor(
    private val api: ApisNetTipoCambioApi,
    private val cache: TipoCambioCacheMemoria,
    private val clock: Clock
) : ProveedorTipoCambio {
    override val nombreProveedor: String = ApisNetTipoCambioConfig.NOMBRE_PROVEEDOR

    override suspend fun obtenerTasaVentaUsdPen(): ResultadoOperacion<CotizacionTipoCambio> {
        val hoy = LocalDate.now(clock)
        cache.obtener(hoy)?.let { cotizacion ->
            return ResultadoOperacion.Exito(cotizacion)
        }

        return when (val resultadoHoy = consultar(fecha = hoy, enviarFecha = false)) {
            is ResultadoOperacion.Exito -> resultadoHoy
            is ResultadoOperacion.DatosObsoletos -> resultadoHoy
            is ResultadoOperacion.Fallo -> resolverFallback(
                fechaActual = hoy,
                causa = resultadoHoy.error
            )
        }
    }

    private suspend fun resolverFallback(
        fechaActual: LocalDate,
        causa: ErrorOperacion
    ): ResultadoOperacion<CotizacionTipoCambio> {
        val ayer = fechaActual.minusDays(1)
        cache.obtener(ayer)?.let { cotizacion ->
            return ResultadoOperacion.DatosObsoletos(cotizacion, causa)
        }

        return when (val resultadoAyer = consultar(fecha = ayer, enviarFecha = true)) {
            is ResultadoOperacion.Exito -> ResultadoOperacion.DatosObsoletos(
                data = resultadoAyer.data,
                causa = causa
            )
            is ResultadoOperacion.DatosObsoletos -> resultadoAyer
            is ResultadoOperacion.Fallo -> {
                val cotizacionCacheada = cache.obtenerMasRecienteHasta(fechaActual)
                if (cotizacionCacheada != null) {
                    ResultadoOperacion.DatosObsoletos(cotizacionCacheada, causa)
                } else {
                    resultadoAyer
                }
            }
        }
    }

    private suspend fun consultar(
        fecha: LocalDate,
        enviarFecha: Boolean
    ): ResultadoOperacion<CotizacionTipoCambio> {
        return try {
            val response = api.obtenerTipoCambioSunat(
                fecha = fecha.takeIf { enviarFecha }?.toString()
            )
            val cotizacion = response.toCotizacion(fechaSolicitada = fecha)
                ?: return ResultadoOperacion.Fallo(
                    ErrorOperacion.RespuestaInvalida(
                        proveedor = nombreProveedor,
                        mensaje = "APIS.net.pe no devolvio una tasa de venta USD/PEN valida."
                    )
                )

            cache.guardar(response.fechaLocalOrNull() ?: fecha, cotizacion)
            ResultadoOperacion.Exito(cotizacion)
        } catch (error: CancellationException) {
            throw error
        } catch (error: HttpException) {
            ResultadoOperacion.Fallo(
                ErrorOperacion.ProveedorNoDisponible(
                    proveedor = nombreProveedor,
                    mensaje = "APIS.net.pe respondio con HTTP ${error.code()}."
                )
            )
        } catch (error: IOException) {
            ResultadoOperacion.Fallo(error.comoErrorRed(nombreProveedor))
        } catch (error: RuntimeException) {
            ResultadoOperacion.Fallo(
                ErrorOperacion.RespuestaInvalida(
                    proveedor = nombreProveedor,
                    mensaje = "La respuesta de APIS.net.pe no tiene el formato esperado."
                )
            )
        }
    }

    private fun ApisNetTipoCambioResponse.toCotizacion(
        fechaSolicitada: LocalDate
    ): CotizacionTipoCambio? {
        if (!esUsd()) return null
        val tasaVenta = tasaVentaValida() ?: return null
        val fechaCotizacion = fechaLocalOrNull() ?: fechaSolicitada

        return CotizacionTipoCambio(
            tasaVentaUsdPen = tasaVenta,
            nombreFuente = "$nombreProveedor $fechaCotizacion",
            obtenidoEn = Instant.now(clock)
        )
    }
}
