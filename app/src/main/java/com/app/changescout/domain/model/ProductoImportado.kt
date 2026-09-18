package com.app.changescout.domain.model

data class ProductoRadarItem(
    val producto: ProductoImportado,
    val ultimaEvaluacion: EvaluacionComercial?
) {
    fun tieneEvaluacion(): Boolean = ultimaEvaluacion != null
}

data class ProductoImportado(
    val id: Long,
    val nombre: String,
    val queryCompetencia: String,
    val componentesCosto: ComponentesCostoImportacion,
    val cantidadDisponible: Int,
    val margenObjetivoPct: Double = 20.0,
    val notas: String? = null
) {
    fun estaListoParaEvaluacion(): Boolean {
        return nombre.isNotBlank() &&
            queryCompetencia.isNotBlank() &&
            cantidadDisponible > 0 &&
            margenObjetivoPct > 0.0 &&
            margenObjetivoPct < 100.0 &&
            componentesCosto.tieneValoresValidos()
    }
}

data class ComponentesCostoImportacion(
    val precioFobUsd: Double,
    val fleteUsd: Double,
    val seguroUsd: Double,
    val arancelesUsd: Double,
    val otrosCargosUsd: Double = 0.0
) {
    fun tieneValoresValidos(): Boolean {
        return precioFobUsd > 0.0 &&
            fleteUsd >= 0.0 &&
            seguroUsd >= 0.0 &&
            arancelesUsd >= 0.0 &&
            otrosCargosUsd >= 0.0
    }
}
