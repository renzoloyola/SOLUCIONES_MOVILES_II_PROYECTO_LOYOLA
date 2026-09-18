package com.app.changescout.data.api.apisnet

import com.google.gson.annotations.SerializedName
import java.time.LocalDate
import retrofit2.http.GET
import retrofit2.http.Query

interface ApisNetTipoCambioApi {
    @GET("v1/tipo-cambio-sunat")
    suspend fun obtenerTipoCambioSunat(
        @Query("fecha") fecha: String? = null
    ): ApisNetTipoCambioResponse
}

object ApisNetTipoCambioConfig {
    const val BASE_URL = "https://api.apis.net.pe/"
    const val NOMBRE_PROVEEDOR = "APIS.net.pe SUNAT"
}

data class ApisNetTipoCambioResponse(
    @SerializedName("origen")
    val origen: String?,
    @SerializedName("compra")
    val compra: Double?,
    @SerializedName("venta")
    val venta: Double?,
    @SerializedName("moneda")
    val moneda: String?,
    @SerializedName("fecha")
    val fecha: String?
) {
    fun tasaVentaValida(): Double? {
        return venta?.takeIf { tasa -> tasa > 0.0 }
    }

    fun fechaLocalOrNull(): LocalDate? {
        return fecha?.let { rawDate ->
            runCatching { LocalDate.parse(rawDate) }.getOrNull()
        }
    }

    fun esUsd(): Boolean {
        return moneda.equals("USD", ignoreCase = true)
    }
}
