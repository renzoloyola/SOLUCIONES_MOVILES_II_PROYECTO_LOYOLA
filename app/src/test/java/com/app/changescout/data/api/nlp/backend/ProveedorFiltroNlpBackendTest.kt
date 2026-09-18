package com.app.changescout.data.api.nlp.backend

import com.app.changescout.domain.model.ComponentesCostoImportacion
import com.app.changescout.domain.model.CondicionPublicacion
import com.app.changescout.domain.model.ErrorOperacion
import com.app.changescout.domain.model.Moneda
import com.app.changescout.domain.model.ProductoImportado
import com.app.changescout.domain.model.PublicacionMercado
import com.app.changescout.domain.model.ResultadoOperacion
import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProveedorFiltroNlpBackendTest {
    @Test
    fun filtrar_conRespuestaValida_mapeaResultadoAlDominio() = runBlocking {
        val api = FakeBackendNlpApi {
            BackendNlpResponse(
                publicacionesValidas = listOf(
                    comparableDto("mpe-1", "audifonos xiaomi", 39.0),
                    comparableDto("mpe-2", "audifonos jbl", 169.0)
                ),
                cantidadDescartadas = 1,
                razonesDescarte = listOf("titulo contaminado: 1"),
                precioPromedioRealPen = 104.0,
                competidoresValidos = 2,
                puntajeConfianza = 0.83,
                trazaProveedor = "proveedor=Groq | total=3 | validas=2"
            )
        }
        val proveedor = ProveedorFiltroNlpBackend(api, backendConfigurado = true)

        val resultado = proveedor.filtrar(
            publicaciones = listOf(
                publicacion("mpe-1", "Audifonos Xiaomi", 39.0),
                publicacion("mpe-2", "Audifonos JBL", 169.0),
                publicacion("mpe-3", "Funda audifonos", 19.0)
            ),
            producto = producto()
        )

        assertTrue(resultado is ResultadoOperacion.Exito)
        val filtro = (resultado as ResultadoOperacion.Exito).data
        assertEquals(2, filtro.competidoresValidos)
        assertEquals(104.0, filtro.precioPromedioRealPen ?: 0.0, 0.0001)
        assertEquals(0.83, filtro.puntajeConfianza ?: 0.0, 0.0)
        assertEquals("mpe-1", filtro.publicacionesValidas.first().publicacionOrigenId)
        assertEquals("audifonos", api.ultimoRequest?.producto?.queryCompetencia)
        assertEquals("PEN", api.ultimoRequest?.publicaciones?.first()?.currency)
        assertEquals("nuevo", api.ultimoRequest?.publicaciones?.first()?.condition)
    }

    @Test
    fun filtrar_sinPublicaciones_noLlamaAlProxy() = runBlocking {
        val api = FakeBackendNlpApi {
            error("No deberia llamar al proxy")
        }
        val proveedor = ProveedorFiltroNlpBackend(api, backendConfigurado = true)

        val resultado = proveedor.filtrar(emptyList(), producto())

        assertTrue(resultado is ResultadoOperacion.Exito)
        val filtro = (resultado as ResultadoOperacion.Exito).data
        assertEquals(0, filtro.competidoresValidos)
        assertNull(filtro.precioPromedioRealPen)
        assertNull(api.ultimoRequest)
    }

    @Test
    fun filtrar_filtraComparablesInvalidosDelProxy() = runBlocking {
        val api = FakeBackendNlpApi {
            BackendNlpResponse(
                publicacionesValidas = listOf(
                    comparableDto(null, "sin id", 10.0),
                    comparableDto("mpe-2", "", 10.0),
                    comparableDto("mpe-3", "sin precio", 0.0),
                    comparableDto("mpe-4", "valida", 99.0)
                ),
                cantidadDescartadas = null,
                razonesDescarte = null,
                precioPromedioRealPen = null,
                competidoresValidos = null,
                puntajeConfianza = 2.0,
                trazaProveedor = null
            )
        }
        val proveedor = ProveedorFiltroNlpBackend(api, backendConfigurado = true)

        val resultado = proveedor.filtrar(
            publicaciones = listOf(publicacion("mpe-4", "Valida", 99.0)),
            producto = producto()
        )

        assertTrue(resultado is ResultadoOperacion.Exito)
        val filtro = (resultado as ResultadoOperacion.Exito).data
        assertEquals(1, filtro.publicacionesValidas.size)
        assertEquals(99.0, filtro.precioPromedioRealPen ?: 0.0, 0.0001)
        assertEquals(1.0, filtro.puntajeConfianza ?: 0.0, 0.0)
    }

    @Test
    fun filtrar_siProxyFallaRetornaProveedorNoDisponible() = runBlocking {
        val proveedor = ProveedorFiltroNlpBackend(
            FakeBackendNlpApi { throw IOException("sin red") },
            backendConfigurado = true
        )

        val resultado = proveedor.filtrar(
            publicaciones = listOf(publicacion("mpe-1", "Audifonos", 50.0)),
            producto = producto()
        )

        assertTrue(resultado is ResultadoOperacion.Fallo)
        assertTrue(
            (resultado as ResultadoOperacion.Fallo).error is ErrorOperacion.ProveedorNoDisponible
        )
    }

    @Test
    fun filtrar_sinBackendConfigurado_explicaQueEstaPendiente() = runBlocking {
        val proveedor = ProveedorFiltroNlpBackend(
            FakeBackendNlpApi { error("No deberia llamar al backend") },
            backendConfigurado = false
        )

        val resultado = proveedor.filtrar(
            publicaciones = listOf(publicacion("mpe-1", "Audifonos", 50.0)),
            producto = producto()
        )

        assertTrue(resultado is ResultadoOperacion.Fallo)
        val error = (resultado as ResultadoOperacion.Fallo).error
        assertTrue(error is ErrorOperacion.ProveedorNoDisponible)
        assertTrue(error.mensaje.contains("pendiente"))
    }

    private fun producto(): ProductoImportado {
        return ProductoImportado(
            id = 7,
            nombre = "Audifonos Bluetooth",
            queryCompetencia = "audifonos",
            componentesCosto = ComponentesCostoImportacion(
                precioFobUsd = 100.0,
                fleteUsd = 10.0,
                seguroUsd = 0.0,
                arancelesUsd = 0.0
            ),
            cantidadDisponible = 5
        )
    }

    private fun publicacion(
        id: String,
        titulo: String,
        precio: Double,
        moneda: Moneda = Moneda.PEN,
        condicion: CondicionPublicacion = CondicionPublicacion.NUEVO
    ): PublicacionMercado {
        return PublicacionMercado(
            publicacionId = id,
            titulo = titulo,
            precio = precio,
            moneda = moneda,
            condicion = condicion,
            permalink = "https://example.test/$id",
            nombreFuente = "test"
        )
    }

    private fun comparableDto(
        id: String?,
        titulo: String?,
        precio: Double?
    ): BackendNlpPublicacionComparableDto {
        return BackendNlpPublicacionComparableDto(
            publicacionOrigenId = id,
            tituloNormalizado = titulo,
            precioPen = precio
        )
    }

    private class FakeBackendNlpApi(
        private val responder: suspend () -> BackendNlpResponse
    ) : BackendNlpApi {
        var ultimoRequest: BackendNlpRequest? = null
            private set

        override suspend fun filtrarPublicaciones(
            request: BackendNlpRequest
        ): BackendNlpResponse {
            ultimoRequest = request
            return responder()
        }
    }
}
