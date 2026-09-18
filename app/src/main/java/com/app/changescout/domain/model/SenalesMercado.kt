package com.app.changescout.domain.model

import java.time.Instant

data class CotizacionTipoCambio(
    val tasaVentaUsdPen: Double,
    val nombreFuente: String,
    val obtenidoEn: Instant
)

data class PublicacionMercado(
    val publicacionId: String,
    val titulo: String,
    val precio: Double,
    val moneda: Moneda,
    val condicion: CondicionPublicacion,
    val permalink: String?,
    val nombreFuente: String
)

data class PublicacionComparable(
    val publicacionOrigenId: String,
    val tituloNormalizado: String,
    val precioPen: Double
)

data class ResultadoFiltroNlp(
    val publicacionesValidas: List<PublicacionComparable>,
    val cantidadDescartadas: Int,
    val razonesDescarte: List<String>,
    val precioPromedioRealPen: Double?,
    val competidoresValidos: Int,
    val puntajeConfianza: Double?,
    val trazaProveedor: String
)

enum class CondicionPublicacion {
    NUEVO,
    USADO,
    DESCONOCIDO
}

enum class Moneda {
    USD,
    PEN,
    DESCONOCIDA
}
