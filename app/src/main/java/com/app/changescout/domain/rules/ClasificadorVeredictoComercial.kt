package com.app.changescout.domain.rules

import com.app.changescout.domain.model.MetricasTendencia
import com.app.changescout.domain.model.VeredictoComercial

class ClasificadorVeredictoComercial(
    private val margenSaludablePct: Double = 20.0,
    private val margenPrecaucionPct: Double = 12.0,
    private val margenAlertaPct: Double = 5.0,
    private val erosionFuertePct: Double = -12.0,
    private val saturacionFuertePct: Double = 35.0
) {
    fun clasificar(
        margenNetoPct: Double?,
        evidenciaSuficiente: Boolean,
        metricasTendencia: MetricasTendencia?
    ): VeredictoComercial {
        if (!evidenciaSuficiente || margenNetoPct == null) {
            return VeredictoComercial.INCONCLUSO
        }

        val veredictoPorMargen = clasificarPorMargen(margenNetoPct)
        val degradaciones = contarDegradacionesPorTendencia(metricasTendencia)

        return degradar(veredictoPorMargen, degradaciones)
    }

    private fun clasificarPorMargen(margenNetoPct: Double): VeredictoComercial {
        return when {
            margenNetoPct < margenAlertaPct -> VeredictoComercial.LIQUIDACION
            margenNetoPct < margenPrecaucionPct -> VeredictoComercial.ALERTA_TEMPRANA_QUIEBRE
            margenNetoPct < margenSaludablePct -> VeredictoComercial.PRECAUCION
            else -> VeredictoComercial.SALUDABLE
        }
    }

    private fun contarDegradacionesPorTendencia(metricas: MetricasTendencia?): Int {
        if (metricas == null) return 0

        var degradaciones = 0
        if ((metricas.erosionPrecioLocalPct ?: 0.0) <= erosionFuertePct) degradaciones++
        if ((metricas.variacionCompetidoresPct ?: 0.0) >= saturacionFuertePct) degradaciones++
        if (esPresionCambiariaFuerte(metricas)) degradaciones++
        return degradaciones
    }

    private fun esPresionCambiariaFuerte(metricas: MetricasTendencia): Boolean {
        val presion = metricas.presionCambiariaPct ?: return false
        return presion >= umbralPresionCambiariaFuertePct(metricas.ventanaHistoricaDias)
    }

    private fun umbralPresionCambiariaFuertePct(ventanaHistoricaDias: Int): Double {
        return when {
            ventanaHistoricaDias <= 14 -> 8.0
            ventanaHistoricaDias <= 45 -> 10.0
            ventanaHistoricaDias <= 90 -> 12.0
            else -> 15.0
        }
    }

    private fun degradar(
        veredicto: VeredictoComercial,
        degradaciones: Int
    ): VeredictoComercial {
        var resultado = veredicto
        repeat(degradaciones.coerceAtLeast(0)) {
            resultado = when (resultado) {
                VeredictoComercial.SALUDABLE -> VeredictoComercial.PRECAUCION
                VeredictoComercial.PRECAUCION -> VeredictoComercial.ALERTA_TEMPRANA_QUIEBRE
                VeredictoComercial.ALERTA_TEMPRANA_QUIEBRE -> VeredictoComercial.LIQUIDACION
                VeredictoComercial.LIQUIDACION -> VeredictoComercial.LIQUIDACION
                VeredictoComercial.INCONCLUSO -> VeredictoComercial.INCONCLUSO
            }
        }
        return resultado
    }
}
