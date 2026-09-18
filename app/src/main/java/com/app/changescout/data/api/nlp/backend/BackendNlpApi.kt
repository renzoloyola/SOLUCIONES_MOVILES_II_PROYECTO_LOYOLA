package com.app.changescout.data.api.nlp.backend

import com.app.changescout.data.api.backend.BackendProxyConfig
import com.app.changescout.domain.model.CondicionPublicacion
import com.app.changescout.domain.model.Moneda
import com.app.changescout.domain.model.ProductoImportado
import com.app.changescout.domain.model.PublicacionComparable
import com.app.changescout.domain.model.PublicacionMercado
import com.app.changescout.domain.model.ResultadoFiltroNlp
import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST

interface BackendNlpApi {
    @POST("nlp/filter")
    suspend fun filtrarPublicaciones(
        @Body request: BackendNlpRequest
    ): BackendNlpResponse
}

data class BackendNlpRequest(
    val producto: BackendNlpProductoDto,
    val publicaciones: List<BackendNlpPublicacionDto>
)

data class BackendNlpProductoDto(
    val id: Long,
    val nombre: String,
    val queryCompetencia: String
)

data class BackendNlpPublicacionDto(
    val id: String,
    val title: String,
    val price: Double,
    val currency: String,
    val condition: String,
    val url: String?
)

data class BackendNlpResponse(
    val publicacionesValidas: List<BackendNlpPublicacionComparableDto>?,
    val cantidadDescartadas: Int?,
    val razonesDescarte: List<String>?,
    val precioPromedioRealPen: Double?,
    val competidoresValidos: Int?,
    val puntajeConfianza: Double?,
    val trazaProveedor: String?
) {
    fun toDomain(): ResultadoFiltroNlp {
        val validas = publicacionesValidas
            .orEmpty()
            .mapNotNull { publicacion -> publicacion.toDomain() }
        val precioPromedioSeguro = precioPromedioRealPen
            ?.takeIf { precio -> precio > 0.0 }
            ?: validas
                .takeIf { publicaciones -> publicaciones.isNotEmpty() }
                ?.map { publicacion -> publicacion.precioPen }
                ?.average()

        return ResultadoFiltroNlp(
            publicacionesValidas = validas,
            cantidadDescartadas = cantidadDescartadas?.coerceAtLeast(0) ?: 0,
            razonesDescarte = razonesDescarte
                .orEmpty()
                .map { razon -> razon.trim() }
                .filter { razon -> razon.isNotBlank() },
            precioPromedioRealPen = precioPromedioSeguro,
            competidoresValidos = validas.size,
            puntajeConfianza = puntajeConfianza?.coerceIn(0.0, 1.0),
            trazaProveedor = trazaProveedor
                ?.takeIf { traza -> traza.isNotBlank() }
                ?: BackendProxyConfig.NOMBRE_PROVEEDOR_NLP
        )
    }
}

data class BackendNlpPublicacionComparableDto(
    @SerializedName("publicacionOrigenId")
    val publicacionOrigenId: String?,
    val tituloNormalizado: String?,
    val precioPen: Double?
) {
    fun toDomain(): PublicacionComparable? {
        val id = publicacionOrigenId?.takeIf { value -> value.isNotBlank() } ?: return null
        val titulo = tituloNormalizado?.takeIf { value -> value.isNotBlank() } ?: return null
        val precio = precioPen?.takeIf { value -> value > 0.0 } ?: return null

        return PublicacionComparable(
            publicacionOrigenId = id,
            tituloNormalizado = titulo,
            precioPen = precio
        )
    }
}

fun ProductoImportado.toBackendNlpDto(): BackendNlpProductoDto {
    return BackendNlpProductoDto(
        id = id,
        nombre = nombre,
        queryCompetencia = queryCompetencia
    )
}

fun PublicacionMercado.toBackendNlpDto(): BackendNlpPublicacionDto {
    return BackendNlpPublicacionDto(
        id = publicacionId,
        title = titulo,
        price = precio,
        currency = moneda.toBackendCode(),
        condition = condicion.toBackendCode(),
        url = permalink
    )
}

private fun Moneda.toBackendCode(): String {
    return when (this) {
        Moneda.PEN -> "PEN"
        Moneda.USD -> "USD"
        Moneda.DESCONOCIDA -> "desconocida"
    }
}

private fun CondicionPublicacion.toBackendCode(): String {
    return when (this) {
        CondicionPublicacion.NUEVO -> "nuevo"
        CondicionPublicacion.USADO -> "usado"
        CondicionPublicacion.DESCONOCIDO -> "desconocido"
    }
}
