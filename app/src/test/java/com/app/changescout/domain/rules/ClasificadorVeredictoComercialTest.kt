package com.app.changescout.domain.rules

import com.app.changescout.domain.model.MetricasTendencia
import com.app.changescout.domain.model.VeredictoComercial
import org.junit.Assert.assertEquals
import org.junit.Test

class ClasificadorVeredictoComercialTest {
    private val clasificador = ClasificadorVeredictoComercial()

    @Test
    fun clasificar_retornaInconclusoSinEvidencia() {
        val resultado = clasificador.clasificar(
            margenNetoPct = 30.0,
            evidenciaSuficiente = false,
            metricasTendencia = null
        )

        assertEquals(VeredictoComercial.INCONCLUSO, resultado)
    }

    @Test
    fun clasificar_retornaInconclusoSinMargen() {
        val resultado = clasificador.clasificar(
            margenNetoPct = null,
            evidenciaSuficiente = true,
            metricasTendencia = null
        )

        assertEquals(VeredictoComercial.INCONCLUSO, resultado)
    }

    @Test
    fun clasificar_retornaSaludableConMargenAlto() {
        val resultado = clasificador.clasificar(
            margenNetoPct = 25.0,
            evidenciaSuficiente = true,
            metricasTendencia = null
        )

        assertEquals(VeredictoComercial.SALUDABLE, resultado)
    }

    @Test
    fun clasificar_retornaPrecaucionConMargenMedio() {
        val resultado = clasificador.clasificar(
            margenNetoPct = 15.0,
            evidenciaSuficiente = true,
            metricasTendencia = null
        )

        assertEquals(VeredictoComercial.PRECAUCION, resultado)
    }

    @Test
    fun clasificar_retornaAlertaConMargenBajo() {
        val resultado = clasificador.clasificar(
            margenNetoPct = 8.0,
            evidenciaSuficiente = true,
            metricasTendencia = null
        )

        assertEquals(VeredictoComercial.ALERTA_TEMPRANA_QUIEBRE, resultado)
    }

    @Test
    fun clasificar_retornaLiquidacionConMargenCritico() {
        val resultado = clasificador.clasificar(
            margenNetoPct = 3.0,
            evidenciaSuficiente = true,
            metricasTendencia = null
        )

        assertEquals(VeredictoComercial.LIQUIDACION, resultado)
    }

    @Test
    fun clasificar_degradaUnNivelPorErosionFuerte() {
        val resultado = clasificador.clasificar(
            margenNetoPct = 25.0,
            evidenciaSuficiente = true,
            metricasTendencia = MetricasTendencia(
                erosionPrecioLocalPct = -12.5,
                variacionCompetidoresPct = null,
                presionCambiariaPct = null,
                ventanaHistoricaDias = 7
            )
        )

        assertEquals(VeredictoComercial.PRECAUCION, resultado)
    }

    @Test
    fun clasificar_degradaPorPresionCambiariaRapida() {
        val resultado = clasificador.clasificar(
            margenNetoPct = 25.0,
            evidenciaSuficiente = true,
            metricasTendencia = MetricasTendencia(
                erosionPrecioLocalPct = null,
                variacionCompetidoresPct = null,
                presionCambiariaPct = 8.0,
                ventanaHistoricaDias = 7
            )
        )

        assertEquals(VeredictoComercial.PRECAUCION, resultado)
    }

    @Test
    fun clasificar_noDegradaPorPresionCambiariaModeradaEnVentanaTrimestral() {
        val resultado = clasificador.clasificar(
            margenNetoPct = 25.0,
            evidenciaSuficiente = true,
            metricasTendencia = MetricasTendencia(
                erosionPrecioLocalPct = null,
                variacionCompetidoresPct = null,
                presionCambiariaPct = 8.0,
                ventanaHistoricaDias = 90
            )
        )

        assertEquals(VeredictoComercial.SALUDABLE, resultado)
    }

    @Test
    fun clasificar_degradaHastaLiquidacionConVariasSenalesFuertes() {
        val resultado = clasificador.clasificar(
            margenNetoPct = 18.0,
            evidenciaSuficiente = true,
            metricasTendencia = MetricasTendencia(
                erosionPrecioLocalPct = -15.0,
                variacionCompetidoresPct = 40.0,
                presionCambiariaPct = 9.0,
                ventanaHistoricaDias = 7
            )
        )

        assertEquals(VeredictoComercial.LIQUIDACION, resultado)
    }
}
