package com.app.changescout.domain.rules

import com.app.changescout.domain.model.EstadoEvaluacion
import com.app.changescout.domain.model.EvaluacionComercial
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class PoliticaObsolescenciaEvaluacionTest {
    private val politica = PoliticaObsolescenciaEvaluacion()
    private val base = Instant.parse("2026-06-04T12:00:00Z")

    @Test
    fun estaVigente_retornaTrueDentroDeVentana() {
        assertTrue(
            politica.estaVigente(
                evaluadoEn = base,
                now = base.plusSeconds(11 * 60 * 60)
            )
        )
    }

    @Test
    fun estaVigente_retornaFalseFueraDeVentana() {
        assertFalse(
            politica.estaVigente(
                evaluadoEn = base,
                now = base.plusSeconds(13 * 60 * 60)
            )
        )
    }

    @Test
    fun estaVigente_toleraNowAnteriorAEvaluacion() {
        assertTrue(
            politica.estaVigente(
                evaluadoEn = base,
                now = base.minusSeconds(1)
            )
        )
    }

    @Test
    fun resolverEstado_preservaevaluacionFallido() {
        val evaluacion = evaluacion(EstadoEvaluacion.FALLIDO)

        val estado = politica.resolverEstado(
            evaluacion = evaluacion,
            now = base.plusSeconds(20 * 60 * 60)
        )

        assertEquals(EstadoEvaluacion.FALLIDO, estado)
    }

    @Test
    fun resolverEstado_marcaObsoletoSiSuperaVentana() {
        val evaluacion = evaluacion(EstadoEvaluacion.VIGENTE)

        val estado = politica.resolverEstado(
            evaluacion = evaluacion,
            now = base.plusSeconds(13 * 60 * 60)
        )

        assertEquals(EstadoEvaluacion.OBSOLETO, estado)
    }

    private fun evaluacion(estado: EstadoEvaluacion): EvaluacionComercial {
        return EvaluacionComercial(
            evaluacionId = 1L,
            productoId = 1L,
            costoTotalUsd = null,
            costoTotalPen = null,
            tipoCambioVentaUsdPen = null,
            precioPromedioRealPen = null,
            competidoresValidos = 0,
            margenNetoPct = null,
            metricasTendencia = null,
            veredicto = null,
            estadoEvaluacion = estado,
            evaluadoEn = base,
            versionAlgoritmo = "test",
            trazaProveedor = null
        )
    }
}
