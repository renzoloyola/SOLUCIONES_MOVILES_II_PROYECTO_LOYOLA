package com.app.changescout.domain.rules

import com.app.changescout.domain.model.EstadoEvaluacion
import com.app.changescout.domain.model.MetricasTendencia
import com.app.changescout.domain.model.EvaluacionComercial
import com.app.changescout.domain.model.VeredictoComercial
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class MotorTendenciaComercialTest {
    private val motor = MotorTendenciaComercial()
    private val actualFecha = Instant.parse("2026-06-04T12:00:00Z")

    @Test
    fun calcular_sinHistorial_retornaMetricasNulasYVentanaCero() {
        val metricas = motor.calcular(
            actual = evaluacionActual(),
            historial = emptyList()
        )

        assertNull(metricas.erosionPrecioLocalPct)
        assertNull(metricas.variacionCompetidoresPct)
        assertNull(metricas.presionCambiariaPct)
        assertEquals(0, metricas.ventanaHistoricaDias)
    }

    @Test
    fun calcular_calculaErosionContraPrecioPromedioHistorico() {
        val metricas = motor.calcular(
            actual = evaluacionActual(precioPromedioRealPen = 88.0),
            historial = listOf(
                evaluacionHistorico(precioPromedioRealPen = 100.0),
                evaluacionHistorico(precioPromedioRealPen = 120.0)
            )
        )

        assertEquals(-20.0, metricas.erosionPrecioLocalPct ?: 0.0, 0.0001)
    }

    @Test
    fun calcular_conUnaSolaLecturaHistorica_noEmiteTendencia() {
        val metricas = motor.calcular(
            actual = evaluacionActual(precioPromedioRealPen = 90.0),
            historial = listOf(
                evaluacionHistorico(precioPromedioRealPen = 100.0)
            )
        )

        assertNull(metricas.erosionPrecioLocalPct)
        assertNull(metricas.variacionCompetidoresPct)
        assertNull(metricas.presionCambiariaPct)
    }

    @Test
    fun calcular_ponderaMasElHistorialReciente() {
        val metricas = motor.calcular(
            actual = evaluacionActual(precioPromedioRealPen = 90.0),
            historial = listOf(
                evaluacionHistorico(
                    precioPromedioRealPen = 100.0,
                    evaluadoEn = actualFecha.minusSeconds(24 * 60 * 60)
                ),
                evaluacionHistorico(
                    precioPromedioRealPen = 200.0,
                    evaluadoEn = actualFecha.minusSeconds(90 * 24 * 60 * 60)
                )
            )
        )

        assertEquals(-19.1675, metricas.erosionPrecioLocalPct ?: 0.0, 0.0001)
    }

    @Test
    fun calcular_calculaCrecimientoDeCompetidoresContraPromedioHistorico() {
        val metricas = motor.calcular(
            actual = evaluacionActual(competidoresValidos = 30),
            historial = listOf(
                evaluacionHistorico(competidoresValidos = 10),
                evaluacionHistorico(competidoresValidos = 20)
            )
        )

        assertEquals(100.0, metricas.variacionCompetidoresPct ?: 0.0, 0.0001)
    }

    @Test
    fun calcular_calculaPresionCambiariaContraPromedioHistorico() {
        val metricas = motor.calcular(
            actual = evaluacionActual(tipoCambioVentaUsdPen = 3.96),
            historial = listOf(
                evaluacionHistorico(tipoCambioVentaUsdPen = 3.5),
                evaluacionHistorico(tipoCambioVentaUsdPen = 3.7)
            )
        )

        assertEquals(10.0, metricas.presionCambiariaPct ?: 0.0, 0.0001)
    }

    @Test
    fun calcular_ignoraevaluacionesNoComparables() {
        val metricas = motor.calcular(
            actual = evaluacionActual(precioPromedioRealPen = 90.0),
            historial = listOf(
                evaluacionHistorico(productoId = 99L, precioPromedioRealPen = 10.0),
                evaluacionHistorico(
                    precioPromedioRealPen = 20.0,
                    estadoEvaluacion = EstadoEvaluacion.FALLIDO
                ),
                evaluacionHistorico(
                    precioPromedioRealPen = 30.0,
                    evaluadoEn = actualFecha.plusSeconds(60)
                ),
                evaluacionHistorico(precioPromedioRealPen = 100.0),
                evaluacionHistorico(
                    precioPromedioRealPen = 100.0,
                    evaluadoEn = actualFecha.minusSeconds(2 * 24 * 60 * 60)
                )
            )
        )

        assertEquals(-10.0, metricas.erosionPrecioLocalPct ?: 0.0, 0.0001)
    }

    @Test
    fun calcular_calculaVentanaHistoricaEnDias() {
        val metricas = motor.calcular(
            actual = evaluacionActual(),
            historial = listOf(
                evaluacionHistorico(evaluadoEn = actualFecha.minusSeconds(2 * 24 * 60 * 60)),
                evaluacionHistorico(evaluadoEn = actualFecha.minusSeconds(7 * 24 * 60 * 60))
            )
        )

        assertEquals(7, metricas.ventanaHistoricaDias)
    }

    @Test
    fun calcular_ignoraHistorialFueraDeVentanaReciente() {
        val metricas = motor.calcular(
            actual = evaluacionActual(precioPromedioRealPen = 90.0),
            historial = listOf(
                evaluacionHistorico(
                    precioPromedioRealPen = 100.0,
                    evaluadoEn = actualFecha.minusSeconds(10 * 24 * 60 * 60)
                ),
                evaluacionHistorico(
                    precioPromedioRealPen = 100.0,
                    evaluadoEn = actualFecha.minusSeconds(5 * 24 * 60 * 60)
                ),
                evaluacionHistorico(
                    precioPromedioRealPen = 1000.0,
                    evaluadoEn = actualFecha.minusSeconds(120 * 24 * 60 * 60)
                )
            )
        )

        assertEquals(-10.0, metricas.erosionPrecioLocalPct ?: 0.0, 0.0001)
        assertEquals(10, metricas.ventanaHistoricaDias)
    }

    @Test
    fun calcular_sinHistorialReciente_retornaMetricasNulasYVentanaCero() {
        val metricas = motor.calcular(
            actual = evaluacionActual(),
            historial = listOf(
                evaluacionHistorico(evaluadoEn = actualFecha.minusSeconds(120 * 24 * 60 * 60))
            )
        )

        assertNull(metricas.erosionPrecioLocalPct)
        assertNull(metricas.variacionCompetidoresPct)
        assertNull(metricas.presionCambiariaPct)
        assertEquals(0, metricas.ventanaHistoricaDias)
    }

    private fun evaluacionActual(
        productoId: Long = 1L,
        precioPromedioRealPen: Double? = 100.0,
        competidoresValidos: Int = 10,
        tipoCambioVentaUsdPen: Double? = 3.8
    ): EvaluacionComercial {
        return evaluacion(
            productoId = productoId,
            precioPromedioRealPen = precioPromedioRealPen,
            competidoresValidos = competidoresValidos,
            tipoCambioVentaUsdPen = tipoCambioVentaUsdPen,
            evaluadoEn = actualFecha
        )
    }

    private fun evaluacionHistorico(
        productoId: Long = 1L,
        precioPromedioRealPen: Double? = 100.0,
        competidoresValidos: Int = 10,
        tipoCambioVentaUsdPen: Double? = 3.8,
        estadoEvaluacion: EstadoEvaluacion = EstadoEvaluacion.OBSOLETO,
        evaluadoEn: Instant = actualFecha.minusSeconds(24 * 60 * 60)
    ): EvaluacionComercial {
        return evaluacion(
            productoId = productoId,
            precioPromedioRealPen = precioPromedioRealPen,
            competidoresValidos = competidoresValidos,
            tipoCambioVentaUsdPen = tipoCambioVentaUsdPen,
            estadoEvaluacion = estadoEvaluacion,
            evaluadoEn = evaluadoEn
        )
    }

    private fun evaluacion(
        productoId: Long,
        precioPromedioRealPen: Double?,
        competidoresValidos: Int,
        tipoCambioVentaUsdPen: Double?,
        estadoEvaluacion: EstadoEvaluacion = EstadoEvaluacion.VIGENTE,
        evaluadoEn: Instant
    ): EvaluacionComercial {
        return EvaluacionComercial(
            evaluacionId = 1L,
            productoId = productoId,
            costoTotalUsd = 100.0,
            costoTotalPen = 380.0,
            tipoCambioVentaUsdPen = tipoCambioVentaUsdPen,
            precioPromedioRealPen = precioPromedioRealPen,
            competidoresValidos = competidoresValidos,
            margenNetoPct = 20.0,
            metricasTendencia = MetricasTendencia(
                erosionPrecioLocalPct = null,
                variacionCompetidoresPct = null,
                presionCambiariaPct = null,
                ventanaHistoricaDias = 0
            ),
            veredicto = VeredictoComercial.SALUDABLE,
            estadoEvaluacion = estadoEvaluacion,
            evaluadoEn = evaluadoEn,
            versionAlgoritmo = "test",
            trazaProveedor = null
        )
    }
}
