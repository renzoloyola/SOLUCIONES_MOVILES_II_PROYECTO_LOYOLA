package com.app.changescout.data.api.marketplace.backend

import com.app.changescout.data.api.backend.BackendProxyConfig
import com.app.changescout.domain.model.ErrorOperacion
import com.app.changescout.domain.model.PublicacionMercado
import com.app.changescout.domain.model.ResultadoOperacion
import com.app.changescout.domain.repository.ProveedorMarketplace
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.IOException
import com.app.changescout.data.api.comoErrorRed
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

class ProveedorMarketplaceBackend @Inject constructor(
    private val api: BackendMarketplaceApi
) : ProveedorMarketplace {
    private var backendConfigurado = BackendProxyConfig.ESTA_CONFIGURADO

    internal constructor(
        api: BackendMarketplaceApi,
        backendConfigurado: Boolean
    ) : this(api) {
        this.backendConfigurado = backendConfigurado
    }

    override val nombreProveedor: String = BackendProxyConfig.NOMBRE_PROVEEDOR_MARKETPLACE

    override suspend fun buscar(
        query: String,
        limit: Int
    ): ResultadoOperacion<List<PublicacionMercado>> {
        if (!backendConfigurado) {
            return ResultadoOperacion.Fallo(
                ErrorOperacion.ProveedorNoDisponible(
                    proveedor = nombreProveedor,
                    mensaje = MENSAJE_BACKEND_PENDIENTE
                )
            )
        }
        val querySeguro = query.trim()
        if (querySeguro.isBlank()) {
            return ResultadoOperacion.Fallo(
                ErrorOperacion.Validacion("El query de competencia no puede estar vacio.")
            )
        }

        return try {
            val limiteSeguro = limit.coerceIn(1, BackendProxyConfig.LIMITE_MAXIMO_BUSQUEDA)
            val publicaciones = api.buscarPublicaciones(
                query = querySeguro,
                limit = limiteSeguro
            )
                .mapNotNull { publicacion -> publicacion.toDomain(nombreProveedor) }

            ResultadoOperacion.Exito(publicaciones)
        } catch (error: CancellationException) {
            throw error
        } catch (error: HttpException) {
            val mensaje = if (error.code() == 401) {
                "Tu sesion expiro. Cierra sesion e ingresa nuevamente."
            } else {
                error.mensajeProxy() ?: "El proxy de marketplace respondio con HTTP ${error.code()}."
            }
            ResultadoOperacion.Fallo(
                ErrorOperacion.ProveedorNoDisponible(
                    proveedor = nombreProveedor,
                    mensaje = mensaje
                )
            )
        } catch (error: IOException) {
            ResultadoOperacion.Fallo(error.comoErrorRed(nombreProveedor))
        } catch (error: RuntimeException) {
            ResultadoOperacion.Fallo(
                ErrorOperacion.RespuestaInvalida(
                    proveedor = nombreProveedor,
                    mensaje = "La respuesta del proxy de marketplace no tiene el formato esperado."
                )
            )
        }
    }

    private fun HttpException.mensajeProxy(): String? {
        val body = response()?.errorBody()?.string() ?: return null
        return runCatching {
            Gson().fromJson(body, BackendErrorDto::class.java)
                ?.message
                ?.takeIf { mensaje -> mensaje.isNotBlank() }
        }.getOrNull()
    }
}

private const val MENSAJE_BACKEND_PENDIENTE =
    "El analisis de mercado aun no esta disponible porque el backend esta pendiente de habilitarse."

private data class BackendErrorDto(
    @SerializedName("message")
    val message: String?
)
