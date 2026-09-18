package com.app.changescout.domain.rules

import com.app.changescout.domain.model.EstadoEvaluacion
import com.app.changescout.domain.model.MetricasTendencia
import com.app.changescout.domain.model.EvaluacionComercial
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.pow

class MotorTendenciaComercial @Inject constructor() {
    fun calcular(
        actual: EvaluacionComercial,
        historial: List<EvaluacionComercial>
    ): MetricasTendencia {
        val historialValido = historial
            .filter { evaluacion -> evaluacion.esComparableCon(actual) }
            .filter { evaluacion -> evaluacion.estaDentroDeVentanaReciente(actual) }
            .sortedByDescending { evaluacion -> evaluacion.evaluadoEn }

        return MetricasTendencia(
            erosionPrecioLocalPct = calcularVariacionContraPromedioPonderado(
                actual = actual.precioPromedioRealPen,
                evaluacionActual = actual,
                historial = historialValido,
                valorHistorico = { evaluacion -> evaluacion.precioPromedioRealPen }
            ),
            variacionCompetidoresPct = calcularVariacionContraPromedioPonderado(
                actual = actual.competidoresValidos.toDouble(),
                evaluacionActual = actual,
                historial = historialValido,
                valorHistorico = { evaluacion -> evaluacion.competidoresValidos.toDouble() }
            ),
            presionCambiariaPct = calcularVariacionContraPromedioPonderado(
                actual = actual.tipoCambioVentaUsdPen,
                evaluacionActual = actual,
                historial = historialValido,
                valorHistorico = { evaluacion -> evaluacion.tipoCambioVentaUsdPen }
            ),
            ventanaHistoricaDias = calcularVentanaHistoricaDias(
                actual = actual,
                historial = historialValido
            )
        )
    }

    private fun EvaluacionComercial.esComparableCon(
        actual: EvaluacionComercial
    ): Boolean {
        return productoId == actual.productoId &&
            evaluadoEn.isBefore(actual.evaluadoEn) &&
            estadoEvaluacion != EstadoEvaluacion.FALLIDO &&
            estadoEvaluacion != EstadoEvaluacion.INCONCLUSO
    }

    private fun EvaluacionComercial.estaDentroDeVentanaReciente(
        actual: EvaluacionComercial
    ): Boolean {
        val antiguedadDias = ChronoUnit.DAYS.between(evaluadoEn, actual.evaluadoEn)
        return antiguedadDias <= VENTANA_HISTORICA_MAXIMA_DIAS
    }

    private fun calcularVariacionContraPromedioPonderado(
        actual: Double?,
        evaluacionActual: EvaluacionComercial,
        historial: List<EvaluacionComercial>,
        valorHistorico: (EvaluacionComercial) -> Double?
    ): Double? {
        if (actual == null || actual < 0.0) return null

        val valoresHistoricos = historial
            .mapNotNull { evaluacion ->
                val valor = valorHistorico(evaluacion)?.takeIf { it > 0.0 } ?: return@mapNotNull null
                val antiguedadDias = ChronoUnit.DAYS
                    .between(evaluacion.evaluadoEn, evaluacionActual.evaluadoEn)
                    .coerceAtLeast(0)
                ValorHistoricoPonderado(
                    valor = valor,
                    peso = 0.5.pow(antiguedadDias.toDouble() / VIDA_MEDIA_PONDERACION_DIAS)
                )
            }
        if (valoresHistoricos.size < MINIMO_HISTORIAL_TENDENCIA) return null

        val pesoTotal = valoresHistoricos.sumOf { it.peso }
        if (pesoTotal <= 0.0) return null

        val promedioHistorico = valoresHistoricos.sumOf { it.valor * it.peso } / pesoTotal
        if (promedioHistorico <= 0.0) return null

        return ((actual - promedioHistorico) / promedioHistorico) * 100.0
    }

    private fun calcularVentanaHistoricaDias(
        actual: EvaluacionComercial,
        historial: List<EvaluacionComercial>
    ): Int {
        val masAntiguo = historial.minByOrNull { it.evaluadoEn } ?: return 0
        return ChronoUnit.DAYS
            .between(masAntiguo.evaluadoEn, actual.evaluadoEn)
            .toInt()
            .coerceAtLeast(1)
    }

    private companion object {
        const val VENTANA_HISTORICA_MAXIMA_DIAS = 90L
        const val VIDA_MEDIA_PONDERACION_DIAS = 30.0
        const val MINIMO_HISTORIAL_TENDENCIA = 2
    }
}

private data class ValorHistoricoPonderado(
    val valor: Double,
    val peso: Double
)
