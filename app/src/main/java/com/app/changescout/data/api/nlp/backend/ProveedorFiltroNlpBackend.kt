package com.app.changescout.data.api.nlp.backend

import com.app.changescout.data.api.backend.BackendProxyConfig
import com.app.changescout.domain.model.ErrorOperacion
import com.app.changescout.domain.model.ProductoImportado
import com.app.changescout.domain.model.PublicacionMercado
import com.app.changescout.domain.model.ResultadoFiltroNlp
import com.app.changescout.domain.model.ResultadoOperacion
import com.app.changescout.domain.repository.ProveedorFiltroNlp
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.IOException
import com.app.changescout.data.api.comoErrorRed
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

class ProveedorFiltroNlpBackend @Inject constructor(
    private val api: BackendNlpApi
) : ProveedorFiltroNlp {
    private var backendConfigurado = BackendProxyConfig.ESTA_CONFIGURADO

    internal constructor(
        api: BackendNlpApi,
        backendConfigurado: Boolean
    ) : this(api) {
        this.backendConfigurado = backendConfigurado
    }

    override val nombreProveedor: String = BackendProxyConfig.NOMBRE_PROVEEDOR_NLP

    override suspend fun filtrar(
        publicaciones: List<PublicacionMercado>,
        producto: ProductoImportado
    ): ResultadoOperacion<ResultadoFiltroNlp> {
        if (!backendConfigurado) {
            return ResultadoOperacion.Fallo(
                ErrorOperacion.ProveedorNoDisponible(
                    proveedor = nombreProveedor,
                    mensaje = MENSAJE_BACKEND_PENDIENTE
                )
            )
        }
        if (publicaciones.isEmpty()) {
            return ResultadoOperacion.Exito(resultadoVacio())
        }

        return try {
            val response = api.filtrarPublicaciones(
                BackendNlpRequest(
                    producto = producto.toBackendNlpDto(),
                    publicaciones = publicaciones.map { publicacion -> publicacion.toBackendNlpDto() }
                )
            )
            ResultadoOperacion.Exito(response.toDomain())
        } catch (error: CancellationException) {
            throw error
        } catch (error: HttpException) {
            val mensaje = if (error.code() == 401) {
                "Tu sesion expiro. Cierra sesion e ingresa nuevamente."
            } else {
                error.mensajeProxy() ?: "El filtro inteligente no pudo procesar la lectura. Intenta nuevamente en unos minutos."
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
                    mensaje = "La respuesta del proxy NLP no tiene el formato esperado."
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

    private fun resultadoVacio(): ResultadoFiltroNlp {
        return ResultadoFiltroNlp(
            publicacionesValidas = emptyList(),
            cantidadDescartadas = 0,
            razonesDescarte = emptyList(),
            precioPromedioRealPen = null,
            competidoresValidos = 0,
            puntajeConfianza = 0.0,
            trazaProveedor = "proveedor=$nombreProveedor | total=0 | validas=0"
        )
    }
}

private const val MENSAJE_BACKEND_PENDIENTE =
    "El filtro inteligente aun no esta disponible porque el backend esta pendiente de habilitarse."

private data class BackendErrorDto(
    @SerializedName("message")
    val message: String?
)
