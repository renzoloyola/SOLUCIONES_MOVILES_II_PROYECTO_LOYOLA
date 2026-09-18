package com.app.changescout.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.app.changescout.domain.model.EstadoEvaluacion
import com.app.changescout.domain.model.EvaluacionComercial
import com.app.changescout.domain.model.MetricasTendencia
import com.app.changescout.domain.model.VeredictoComercial
import java.time.Instant

@Entity(
    tableName = "evaluaciones_comerciales",
    foreignKeys = [
        ForeignKey(
            entity = ProductoImportadoEntity::class,
            parentColumns = ["id"],
            childColumns = ["productoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["usuarioId"]),
        Index(value = ["productoId"]),
        Index(value = ["usuarioId", "productoId"]),
        Index(value = ["productoId", "evaluadoEnEpochMillis"])
    ]
)
data class EvaluacionComercialEntity(
    @PrimaryKey(autoGenerate = true)
    val evaluacionId: Long = 0L,
    val usuarioId: String,
    val productoId: Long,
    val costoTotalUsd: Double?,
    val costoTotalPen: Double?,
    val tipoCambioVentaUsdPen: Double?,
    val precioPromedioRealPen: Double?,
    val margenObjetivoPct: Double?,
    val precioVentaSugeridoPen: Double?,
    val brechaPrecioSugeridoMercadoPct: Double?,
    val competidoresValidos: Int,
    val margenNetoPct: Double?,
    val erosionPrecioLocalPct: Double?,
    val variacionCompetidoresPct: Double?,
    val presionCambiariaPct: Double?,
    val ventanaHistoricaDias: Int?,
    val veredicto: String?,
    val estadoEvaluacion: String,
    val evaluadoEnEpochMillis: Long,
    val versionAlgoritmo: String,
    val trazaProveedor: String?,
    val motivoEvidenciaInsuficiente: String?
) {
    fun toDomain(): EvaluacionComercial {
        return EvaluacionComercial(
            evaluacionId = evaluacionId,
            productoId = productoId,
            costoTotalUsd = costoTotalUsd,
            costoTotalPen = costoTotalPen,
            tipoCambioVentaUsdPen = tipoCambioVentaUsdPen,
            precioPromedioRealPen = precioPromedioRealPen,
            margenObjetivoPct = margenObjetivoPct,
            precioVentaSugeridoPen = precioVentaSugeridoPen,
            brechaPrecioSugeridoMercadoPct = brechaPrecioSugeridoMercadoPct,
            competidoresValidos = competidoresValidos,
            margenNetoPct = margenNetoPct,
            metricasTendencia = toMetricasTendencia(),
            veredicto = veredicto?.toEnumOrNull<VeredictoComercial>(),
            estadoEvaluacion = estadoEvaluacion.toEnumOrNull<EstadoEvaluacion>()
                ?: EstadoEvaluacion.FALLIDO,
            evaluadoEn = Instant.ofEpochMilli(evaluadoEnEpochMillis),
            versionAlgoritmo = versionAlgoritmo,
            trazaProveedor = trazaProveedor,
            motivoEvidenciaInsuficiente = motivoEvidenciaInsuficiente
        )
    }

    private fun toMetricasTendencia(): MetricasTendencia? {
        if (
            erosionPrecioLocalPct == null &&
            variacionCompetidoresPct == null &&
            presionCambiariaPct == null &&
            ventanaHistoricaDias == null
        ) {
            return null
        }

        return MetricasTendencia(
            erosionPrecioLocalPct = erosionPrecioLocalPct,
            variacionCompetidoresPct = variacionCompetidoresPct,
            presionCambiariaPct = presionCambiariaPct,
            ventanaHistoricaDias = ventanaHistoricaDias ?: 0
        )
    }

    companion object {
        fun fromDomain(
            evaluacion: EvaluacionComercial,
            usuarioId: String
        ): EvaluacionComercialEntity {
            return EvaluacionComercialEntity(
                evaluacionId = evaluacion.evaluacionId,
                usuarioId = usuarioId,
                productoId = evaluacion.productoId,
                costoTotalUsd = evaluacion.costoTotalUsd,
                costoTotalPen = evaluacion.costoTotalPen,
                tipoCambioVentaUsdPen = evaluacion.tipoCambioVentaUsdPen,
                precioPromedioRealPen = evaluacion.precioPromedioRealPen,
                margenObjetivoPct = evaluacion.margenObjetivoPct,
                precioVentaSugeridoPen = evaluacion.precioVentaSugeridoPen,
                brechaPrecioSugeridoMercadoPct = evaluacion.brechaPrecioSugeridoMercadoPct,
                competidoresValidos = evaluacion.competidoresValidos,
                margenNetoPct = evaluacion.margenNetoPct,
                erosionPrecioLocalPct = evaluacion.metricasTendencia?.erosionPrecioLocalPct,
                variacionCompetidoresPct = evaluacion.metricasTendencia?.variacionCompetidoresPct,
                presionCambiariaPct = evaluacion.metricasTendencia?.presionCambiariaPct,
                ventanaHistoricaDias = evaluacion.metricasTendencia?.ventanaHistoricaDias,
                veredicto = evaluacion.veredicto?.name,
                estadoEvaluacion = evaluacion.estadoEvaluacion.name,
                evaluadoEnEpochMillis = evaluacion.evaluadoEn.toEpochMilli(),
                versionAlgoritmo = evaluacion.versionAlgoritmo,
                trazaProveedor = evaluacion.trazaProveedor,
                motivoEvidenciaInsuficiente = evaluacion.motivoEvidenciaInsuficiente
            )
        }
    }
}

private inline fun <reified T : Enum<T>> String.toEnumOrNull(): T? {
    return runCatching { enumValueOf<T>(this) }.getOrNull()
}
