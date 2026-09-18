package com.app.changescout.domain.rules

import com.app.changescout.domain.model.PublicacionComparable
import com.app.changescout.domain.model.ResultadoFiltroNlp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PoliticaEvidenciaTest {
    private val politica = PoliticaEvidencia()

    @Test
    fun tieneEvidenciaSuficiente_retornaTrueConPrecioCompetidoresYDispersionAceptable() {
        val resultado = resultadoFiltro(
            precioPromedioRealPen = 100.0,
            competidoresValidos = 3,
            preciosValidos = listOf(95.0, 100.0, 105.0),
            puntajeConfianza = 0.1
        )

        assertTrue(politica.tieneEvidenciaSuficiente(resultado))
    }

    @Test
    fun tieneEvidenciaSuficiente_retornaFalseSinPrecioPromedio() {
        val resultado = resultadoFiltro(
            precioPromedioRealPen = null,
            competidoresValidos = 5,
            preciosValidos = listOf(95.0, 100.0, 105.0, 110.0, 115.0),
            puntajeConfianza = 0.9
        )

        assertFalse(politica.tieneEvidenciaSuficiente(resultado))
    }

    @Test
    fun tieneEvidenciaSuficiente_retornaFalseConPocosCompetidores() {
        val resultado = resultadoFiltro(
            precioPromedioRealPen = 100.0,
            competidoresValidos = 2,
            preciosValidos = listOf(95.0, 105.0),
            puntajeConfianza = 0.9
        )

        assertFalse(politica.tieneEvidenciaSuficiente(resultado))
        assertEquals(
            "Solo se encontraron 2 competidores comparables; se necesitan al menos 3.",
            politica.evaluar(resultado).motivoInsuficiente
        )
    }

    @Test
    fun tieneEvidenciaSuficiente_aceptaConfianzaNulaPorqueNoEsCriterio() {
        val resultado = resultadoFiltro(
            precioPromedioRealPen = 100.0,
            competidoresValidos = 3,
            preciosValidos = listOf(95.0, 100.0, 105.0),
            puntajeConfianza = null
        )

        assertTrue(politica.tieneEvidenciaSuficiente(resultado))
    }

    @Test
    fun tieneEvidenciaSuficiente_retornaFalseConPreciosMuyDispersos() {
        val resultado = resultadoFiltro(
            precioPromedioRealPen = 370.0,
            competidoresValidos = 3,
            preciosValidos = listOf(100.0, 110.0, 900.0),
            puntajeConfianza = 0.95
        )

        assertFalse(politica.tieneEvidenciaSuficiente(resultado))
    }

    private fun resultadoFiltro(
        precioPromedioRealPen: Double?,
        competidoresValidos: Int,
        preciosValidos: List<Double>,
        puntajeConfianza: Double?
    ): ResultadoFiltroNlp {
        return ResultadoFiltroNlp(
            publicacionesValidas = preciosValidos.mapIndexed { index, precio ->
                PublicacionComparable(
                    publicacionOrigenId = "pub-$index",
                    tituloNormalizado = "producto $index",
                    precioPen = precio
                )
            },
            cantidadDescartadas = 0,
            razonesDescarte = emptyList(),
            precioPromedioRealPen = precioPromedioRealPen,
            competidoresValidos = competidoresValidos,
            puntajeConfianza = puntajeConfianza,
            trazaProveedor = "test"
        )
    }
}
