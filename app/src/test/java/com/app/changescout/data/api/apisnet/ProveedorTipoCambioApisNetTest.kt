package com.app.changescout.data.api.apisnet

import com.app.changescout.domain.model.ErrorOperacion
import com.app.changescout.domain.model.ResultadoOperacion
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProveedorTipoCambioApisNetTest {
    private val ahora = Instant.parse("2026-06-04T12:00:00Z")
    private val clock = Clock.fixed(ahora, ZoneOffset.UTC)

    @Test
    fun obtenerTasaVentaUsdPen_conRespuestaValida_retornaVentaSunatYCacheaElDia() = runBlocking {
        val api = FakeApisNetTipoCambioApi { fecha ->
            require(fecha == null)
            response(fecha = "2026-06-04", venta = 3.422)
        }
        val cache = TipoCambioCacheMemoria(clock)
        val proveedor = ProveedorTipoCambioApisNet(api, cache, clock)

        val primeraConsulta = proveedor.obtenerTasaVentaUsdPen()
        val segundaConsulta = proveedor.obtenerTasaVentaUsdPen()

        assertTrue(primeraConsulta is ResultadoOperacion.Exito)
        assertTrue(segundaConsulta is ResultadoOperacion.Exito)
        assertEquals(1, api.llamadas)
        assertEquals(1, cache.cantidadEntradas())
        assertEquals(
            3.422,
            (segundaConsulta as ResultadoOperacion.Exito).data.tasaVentaUsdPen,
            0.0001
        )
    }

    @Test
    fun obtenerTasaVentaUsdPen_siHoyFallaConsultaAyerYRetornaDatosObsoletos() = runBlocking {
        val api = FakeApisNetTipoCambioApi { fecha ->
            if (fecha == null) throw IOException("sin red")
            require(fecha == "2026-06-03")
            response(fecha = "2026-06-03", venta = 3.414)
        }
        val proveedor = ProveedorTipoCambioApisNet(
            api = api,
            cache = TipoCambioCacheMemoria(clock),
            clock = clock
        )

        val resultado = proveedor.obtenerTasaVentaUsdPen()

        assertTrue(resultado is ResultadoOperacion.DatosObsoletos)
        val datosObsoletos = resultado as ResultadoOperacion.DatosObsoletos
        assertEquals(3.414, datosObsoletos.data.tasaVentaUsdPen, 0.0001)
        assertTrue(datosObsoletos.causa is ErrorOperacion.ProveedorNoDisponible)
        assertEquals(2, api.llamadas)
    }

    @Test
    fun obtenerTasaVentaUsdPen_siNoHayVentaValida_retornaRespuestaInvalida() = runBlocking {
        val api = FakeApisNetTipoCambioApi {
            response(fecha = "2026-06-04", venta = null)
        }
        val proveedor = ProveedorTipoCambioApisNet(
            api = api,
            cache = TipoCambioCacheMemoria(clock),
            clock = clock
        )

        val resultado = proveedor.obtenerTasaVentaUsdPen()

        assertTrue(resultado is ResultadoOperacion.Fallo)
        assertTrue((resultado as ResultadoOperacion.Fallo).error is ErrorOperacion.RespuestaInvalida)
    }

    @Test
    fun cacheMemoria_retieneMaximoTreintaDias() {
        val cache = TipoCambioCacheMemoria(clock)
        repeat(31) { index ->
            val fecha = java.time.LocalDate.parse("2026-05-05").plusDays(index.toLong())
            cache.guardar(
                fecha = fecha,
                cotizacion = com.app.changescout.domain.model.CotizacionTipoCambio(
                    tasaVentaUsdPen = 3.4 + index,
                    nombreFuente = "test",
                    obtenidoEn = ahora
                )
            )
        }

        assertEquals(30, cache.cantidadEntradas())
    }

    private fun response(
        fecha: String,
        venta: Double?,
        moneda: String = "USD"
    ): ApisNetTipoCambioResponse {
        return ApisNetTipoCambioResponse(
            origen = "SUNAT",
            compra = 3.4,
            venta = venta,
            moneda = moneda,
            fecha = fecha
        )
    }

    private class FakeApisNetTipoCambioApi(
        private val responder: (String?) -> ApisNetTipoCambioResponse
    ) : ApisNetTipoCambioApi {
        var llamadas: Int = 0
            private set

        override suspend fun obtenerTipoCambioSunat(
            fecha: String?
        ): ApisNetTipoCambioResponse {
            llamadas++
            return responder(fecha)
        }
    }
}
