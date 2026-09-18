package com.app.changescout.data.api.marketplace.backend

import com.app.changescout.domain.model.CondicionPublicacion
import com.app.changescout.domain.model.ErrorOperacion
import com.app.changescout.domain.model.Moneda
import com.app.changescout.domain.model.ResultadoOperacion
import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProveedorMarketplaceBackendTest {
    @Test
    fun buscar_conRespuestaValida_mapeaPublicacionesAlDominio() = runBlocking {
        val api = FakeBackendMarketplaceApi {
            listOf(
                publicacionDto(
                    id = "mpe-1",
                    title = "Audifonos bluetooth nuevos",
                    price = 129.9,
                    currency = "PEN",
                    condition = "new"
                ),
                publicacionDto(
                    id = "mpe-2",
                    title = "Audifonos bluetooth usados",
                    price = 39.0,
                    currency = "USD",
                    condition = "used"
                )
            )
        }
        val proveedor = ProveedorMarketplaceBackend(api, backendConfigurado = true)

        val resultado = proveedor.buscar(query = " audifonos bluetooth ", limit = 10)

        assertTrue(resultado is ResultadoOperacion.Exito)
        val publicaciones = (resultado as ResultadoOperacion.Exito).data
        assertEquals(2, publicaciones.size)
        assertEquals("mpe-1", publicaciones[0].publicacionId)
        assertEquals(Moneda.PEN, publicaciones[0].moneda)
        assertEquals(CondicionPublicacion.NUEVO, publicaciones[0].condicion)
        assertEquals(Moneda.USD, publicaciones[1].moneda)
        assertEquals(CondicionPublicacion.USADO, publicaciones[1].condicion)
        assertEquals("ChangeScout Marketplace Proxy", publicaciones[0].nombreFuente)
        assertEquals("audifonos bluetooth", api.ultimoQuery)
        assertEquals("PE", api.ultimoCountry)
    }

    @Test
    fun buscar_filtraPublicacionesSinCamposMinimos() = runBlocking {
        val api = FakeBackendMarketplaceApi {
            listOf(
                publicacionDto(id = null, title = "Sin id", price = 10.0),
                publicacionDto(id = "mpe-2", title = "", price = 10.0),
                publicacionDto(id = "mpe-3", title = "Precio cero", price = 0.0),
                publicacionDto(id = "mpe-4", title = "Valida", price = 100.0)
            )
        }
        val proveedor = ProveedorMarketplaceBackend(api, backendConfigurado = true)

        val resultado = proveedor.buscar(query = "consola", limit = 10)

        assertTrue(resultado is ResultadoOperacion.Exito)
        val publicaciones = (resultado as ResultadoOperacion.Exito).data
        assertEquals(1, publicaciones.size)
        assertEquals("mpe-4", publicaciones.single().publicacionId)
    }

    @Test
    fun buscar_limitaConsultaAlMaximoConfigurado() = runBlocking {
        val api = FakeBackendMarketplaceApi { emptyList() }
        val proveedor = ProveedorMarketplaceBackend(api, backendConfigurado = true)

        proveedor.buscar(query = "laptop", limit = 500)

        assertEquals(15, api.ultimoLimit)
    }

    @Test
    fun buscar_conQueryVacioRetornaValidacion() = runBlocking {
        val proveedor = ProveedorMarketplaceBackend(
            FakeBackendMarketplaceApi { emptyList() },
            backendConfigurado = true
        )

        val resultado = proveedor.buscar(query = " ", limit = 5)

        assertTrue(resultado is ResultadoOperacion.Fallo)
        assertTrue((resultado as ResultadoOperacion.Fallo).error is ErrorOperacion.Validacion)
    }

    @Test
    fun buscar_siProxyFallaRetornaProveedorNoDisponible() = runBlocking {
        val proveedor = ProveedorMarketplaceBackend(
            FakeBackendMarketplaceApi { throw IOException("sin red") },
            backendConfigurado = true
        )

        val resultado = proveedor.buscar(query = "tablet", limit = 5)

        assertTrue(resultado is ResultadoOperacion.Fallo)
        assertTrue(
            (resultado as ResultadoOperacion.Fallo).error is ErrorOperacion.ProveedorNoDisponible
        )
    }

    @Test
    fun buscar_sinBackendConfigurado_explicaQueEstaPendiente() = runBlocking {
        val proveedor = ProveedorMarketplaceBackend(
            FakeBackendMarketplaceApi { error("No deberia llamar al backend") },
            backendConfigurado = false
        )

        val resultado = proveedor.buscar(query = "tablet", limit = 5)

        assertTrue(resultado is ResultadoOperacion.Fallo)
        val error = (resultado as ResultadoOperacion.Fallo).error
        assertTrue(error is ErrorOperacion.ProveedorNoDisponible)
        assertTrue(error.mensaje.contains("pendiente"))
    }

    private fun publicacionDto(
        id: String?,
        title: String?,
        price: Double?,
        currency: String = "PEN",
        condition: String = "new"
    ): BackendPublicacionDto {
        return BackendPublicacionDto(
            id = id,
            title = title,
            price = price,
            currency = currency,
            condition = condition,
            url = "https://example.test/$id"
        )
    }

    private class FakeBackendMarketplaceApi(
        private val responder: suspend () -> List<BackendPublicacionDto>
    ) : BackendMarketplaceApi {
        var ultimoQuery: String? = null
            private set
        var ultimoCountry: String? = null
            private set
        var ultimoLimit: Int? = null
            private set

        override suspend fun buscarPublicaciones(
            query: String,
            country: String,
            limit: Int
        ): List<BackendPublicacionDto> {
            ultimoQuery = query
            ultimoCountry = country
            ultimoLimit = limit
            return responder()
        }
    }
}
