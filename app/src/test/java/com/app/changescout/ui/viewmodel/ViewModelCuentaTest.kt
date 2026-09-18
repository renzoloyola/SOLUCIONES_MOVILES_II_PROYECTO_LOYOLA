package com.app.changescout.ui.viewmodel

import com.app.changescout.domain.model.ComponentesCostoImportacion
import com.app.changescout.domain.model.EstadoEvaluacion
import com.app.changescout.domain.model.EvaluacionComercial
import com.app.changescout.domain.model.ProductoImportado
import com.app.changescout.domain.model.ProductoRadarItem
import com.app.changescout.domain.model.VeredictoComercial
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class ViewModelCuentaTest {
    @Test
    fun crearResumenCuenta_listaCaducadasYUltimaLectura() {
        val ahora = Instant.parse("2026-06-27T12:00:00Z")
        val radar = listOf(
            item(1, "Audifonos", VeredictoComercial.SALUDABLE, EstadoEvaluacion.VIGENTE, ahora.minusSeconds(60)),
            item(2, "Parlante", VeredictoComercial.PRECAUCION, EstadoEvaluacion.OBSOLETO, ahora.minusSeconds(3_600)),
            item(3, "Mouse", VeredictoComercial.LIQUIDACION, EstadoEvaluacion.VIGENTE, ahora.minusSeconds(7_200)),
            ProductoRadarItem(producto(4, "Teclado"), ultimaEvaluacion = null)
        )

        val resumen = crearResumenCuenta("demo@changescout.pe", "Demo", radar, ahora)

        assertEquals(1, resumen.desactualizados)
        assertEquals("Parlante", resumen.lecturasCaducadas.single().producto)
        assertEquals("Audifonos, hace 1 min", resumen.ultimaLectura)
    }

    private fun item(
        id: Long,
        nombre: String,
        veredicto: VeredictoComercial,
        estado: EstadoEvaluacion,
        evaluadoEn: Instant
    ): ProductoRadarItem {
        return ProductoRadarItem(
            producto = producto(id, nombre),
            ultimaEvaluacion = EvaluacionComercial(
                evaluacionId = id,
                productoId = id,
                costoTotalUsd = 100.0,
                costoTotalPen = 370.0,
                tipoCambioVentaUsdPen = 3.7,
                precioPromedioRealPen = 500.0,
                competidoresValidos = 5,
                margenNetoPct = 20.0,
                metricasTendencia = null,
                veredicto = veredicto,
                estadoEvaluacion = estado,
                evaluadoEn = evaluadoEn,
                versionAlgoritmo = "test",
                trazaProveedor = null
            )
        )
    }

    private fun producto(id: Long, nombre: String): ProductoImportado {
        return ProductoImportado(
            id = id,
            nombre = nombre,
            queryCompetencia = nombre,
            componentesCosto = ComponentesCostoImportacion(
                precioFobUsd = 100.0,
                fleteUsd = 0.0,
                seguroUsd = 0.0,
                arancelesUsd = 0.0
            ),
            cantidadDisponible = 1
        )
    }
}
