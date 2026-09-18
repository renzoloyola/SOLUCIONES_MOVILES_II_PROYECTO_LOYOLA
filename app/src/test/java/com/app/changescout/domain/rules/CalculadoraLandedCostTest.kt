package com.app.changescout.domain.rules

import com.app.changescout.domain.model.ComponentesCostoImportacion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CalculadoraLandedCostTest {
    private val calculadora = CalculadoraLandedCost()

    @Test
    fun calcularCostoTotalUsd_sumaComponentesFragmentados() {
        val componentes = ComponentesCostoImportacion(
            precioFobUsd = 100.0,
            fleteUsd = 12.5,
            seguroUsd = 3.0,
            arancelesUsd = 8.5,
            otrosCargosUsd = 1.0
        )

        val resultado = calculadora.calcularCostoTotalUsd(componentes)

        assertEquals(125.0, resultado, 0.0001)
    }

    @Test
    fun calcularCostoTotalPen_multiplicaCostoUsdPorTipoCambioVenta() {
        val resultado = calculadora.calcularCostoTotalPen(
            costoTotalUsd = 125.0,
            tipoCambioVentaUsdPen = 3.75
        )

        assertEquals(468.75, resultado, 0.0001)
    }

    @Test
    fun calcularMargenNetoPct_calculaMargenSobrePrecioPromedioReal() {
        val resultado = calculadora.calcularMargenNetoPct(
            precioPromedioRealPen = 600.0,
            costoTotalPen = 468.75
        )

        assertEquals(21.875, resultado, 0.0001)
    }

    @Test
    fun calcularMargenNetoPct_rechazaPrecioPromedioCero() {
        assertThrows(IllegalArgumentException::class.java) {
            calculadora.calcularMargenNetoPct(
                precioPromedioRealPen = 0.0,
                costoTotalPen = 468.75
            )
        }
    }

    @Test
    fun calcularPrecioVentaSugeridoPen_calculaPrecioConMargenObjetivo() {
        val resultado = calculadora.calcularPrecioVentaSugeridoPen(
            costoTotalPen = 100.0,
            margenObjetivoPct = 20.0
        )

        assertEquals(125.0, resultado, 0.0001)
    }
}
