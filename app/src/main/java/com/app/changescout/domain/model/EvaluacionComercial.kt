package com.app.changescout.domain.model

import java.time.Instant

data class EvaluacionComercial(
    val evaluacionId: Long,
    val productoId: Long,
    val costoTotalUsd: Double?,
    val costoTotalPen: Double?,
    val tipoCambioVentaUsdPen: Double?,
    val precioPromedioRealPen: Double?,
    val margenObjetivoPct: Double? = null,
    val precioVentaSugeridoPen: Double? = null,
    val brechaPrecioSugeridoMercadoPct: Double? = null,
    val competidoresValidos: Int,
    val margenNetoPct: Double?,
    val metricasTendencia: MetricasTendencia?,
    val veredicto: VeredictoComercial?,
    val estadoEvaluacion: EstadoEvaluacion,
    val evaluadoEn: Instant,
    val versionAlgoritmo: String,
    val trazaProveedor: String?,
    val motivoEvidenciaInsuficiente: String? = null
) {
    fun esConclusivo(): Boolean {
        return estadoEvaluacion != EstadoEvaluacion.INCONCLUSO &&
            estadoEvaluacion != EstadoEvaluacion.FALLIDO &&
            veredicto != null &&
            veredicto != VeredictoComercial.INCONCLUSO
    }

    fun estadoBrechaPrecioSugerido(): EstadoBrechaPrecioSugerido? {
        val brecha = brechaPrecioSugeridoMercadoPct ?: return null
        val objetivo = margenObjetivoPct?.takeIf { it > 0.0 } ?: return null

        return when {
            brecha >= 0.0 -> EstadoBrechaPrecioSugerido.SOBRE_OBJETIVO
            brecha > -objetivo -> EstadoBrechaPrecioSugerido.BAJO_OBJETIVO_CON_MARGEN
            else -> EstadoBrechaPrecioSugerido.SIN_MARGEN
        }
    }
}

enum class EstadoBrechaPrecioSugerido {
    SOBRE_OBJETIVO,
    BAJO_OBJETIVO_CON_MARGEN,
    SIN_MARGEN
}

data class MetricasTendencia(
    val erosionPrecioLocalPct: Double?,
    val variacionCompetidoresPct: Double?,
    val presionCambiariaPct: Double?,
    val ventanaHistoricaDias: Int
)

enum class VeredictoComercial {
    SALUDABLE,
    PRECAUCION,
    ALERTA_TEMPRANA_QUIEBRE,
    LIQUIDACION,
    INCONCLUSO
}

enum class EstadoEvaluacion {
    VIGENTE,
    OBSOLETO,
    INCONCLUSO,
    FALLIDO
}
