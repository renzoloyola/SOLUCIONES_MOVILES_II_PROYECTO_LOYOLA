package com.app.changescout.domain.model

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EvaluacionComercialTest {
    @Test
    fun estadoBrechaPrecioSugerido_siMercadoSuperaPrecioSugerido_cumpleObjetivo() {
        val evaluacion = evaluacion(
            margenObjetivoPct = 30.0,
            brechaPrecioSugeridoMercadoPct = 2.0
        )

        assertEquals(
            EstadoBrechaPrecioSugerido.SOBRE_OBJETIVO,
            evaluacion.estadoBrechaPrecioSugerido()
        )
    }

    @Test
    fun estadoBrechaPrecioSugerido_siBrechaNoSuperaObjetivoPeroHayMargen_noMarcaPerdida() {
        val evaluacion = evaluacion(
            margenObjetivoPct = 30.0,
            brechaPrecioSugeridoMercadoPct = -21.6
        )

        assertEquals(
            EstadoBrechaPrecioSugerido.BAJO_OBJETIVO_CON_MARGEN,
            evaluacion.estadoBrechaPrecioSugerido()
        )
    }

    @Test
    fun estadoBrechaPrecioSugerido_siBrechaCruzaObjetivo_quedaSinMargen() {
        val evaluacion = evaluacion(
            margenObjetivoPct = 30.0,
            brechaPrecioSugeridoMercadoPct = -30.0
        )

        assertEquals(
            EstadoBrechaPrecioSugerido.SIN_MARGEN,
            evaluacion.estadoBrechaPrecioSugerido()
        )
    }

    @Test
    fun estadoBrechaPrecioSugerido_sinBrecha_retornaNull() {
        assertNull(evaluacion(brechaPrecioSugeridoMercadoPct = null).estadoBrechaPrecioSugerido())
    }

    private fun evaluacion(
        margenObjetivoPct: Double? = 30.0,
        brechaPrecioSugeridoMercadoPct: Double? = -21.6
    ): EvaluacionComercial {
        return EvaluacionComercial(
            evaluacionId = 1L,
            productoId = 7L,
            costoTotalUsd = 100.0,
            costoTotalPen = 203.52,
            tipoCambioVentaUsdPen = 3.84,
            precioPromedioRealPen = 227.97,
            margenObjetivoPct = margenObjetivoPct,
            precioVentaSugeridoPen = 290.74,
            brechaPrecioSugeridoMercadoPct = brechaPrecioSugeridoMercadoPct,
            competidoresValidos = 7,
            margenNetoPct = 10.7,
            metricasTendencia = null,
            veredicto = VeredictoComercial.PRECAUCION,
            estadoEvaluacion = EstadoEvaluacion.VIGENTE,
            evaluadoEn = Instant.parse("2026-06-30T12:00:00Z"),
            versionAlgoritmo = "test",
            trazaProveedor = null
        )
    }
}
