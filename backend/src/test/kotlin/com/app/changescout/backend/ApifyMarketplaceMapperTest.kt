package com.app.changescout.backend

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ApifyMarketplaceMapperTest {
    @Test
    fun toPublicacionResponse_mapeaCamposMinimosDeApify() {
        val item = ApifyProductoDto(
            listingId = "MPE123",
            title = "Audifonos Bluetooth",
            price = 129.9,
            currency = "PEN",
            url = "articulo.mercadolibre.com.pe/MPE-123",
            error = null
        )

        val response = item.toPublicacionResponse()

        assertEquals("MPE123", response?.id)
        assertEquals("Audifonos Bluetooth", response?.title)
        assertEquals(129.9, response?.price ?: 0.0, 0.0)
        assertEquals("PEN", response?.currency)
        assertNull(response?.condition)
        assertEquals("https://articulo.mercadolibre.com.pe/MPE-123", response?.url)
    }

    @Test
    fun toPublicacionResponse_sinPrecioValido_descartaItem() {
        val item = ApifyProductoDto(
            listingId = "MPE123",
            title = "Audifonos Bluetooth",
            price = 0.0,
            currency = "PEN",
            url = null,
            error = null
        )

        assertNull(item.toPublicacionResponse())
    }

    @Test
    fun toPublicacionResponse_conErrorDeActor_descartaItem() {
        val item = ApifyProductoDto(
            listingId = "MPE123",
            title = "Audifonos Bluetooth",
            price = 129.9,
            currency = "PEN",
            url = null,
            error = "listing failed"
        )

        assertNull(item.toPublicacionResponse())
    }
}
