package com.app.changescout.domain.rules

import com.app.changescout.domain.model.ComponentesCostoImportacion
import javax.inject.Inject

class CalculadoraLandedCost @Inject constructor() {
    fun calcularCostoTotalUsd(componentes: ComponentesCostoImportacion): Double {
        require(componentes.tieneValoresValidos()) {
            "Los componentes de costo no son validos."
        }

        return componentes.precioFobUsd +
            componentes.fleteUsd +
            componentes.seguroUsd +
            componentes.arancelesUsd +
            componentes.otrosCargosUsd
    }

    fun calcularCostoTotalPen(
        costoTotalUsd: Double,
        tipoCambioVentaUsdPen: Double
    ): Double {
        require(costoTotalUsd > 0.0) { "El costo total USD debe ser mayor a cero." }
        require(tipoCambioVentaUsdPen > 0.0) { "El tipo de cambio de venta debe ser mayor a cero." }

        return costoTotalUsd * tipoCambioVentaUsdPen
    }

    fun calcularMargenNetoPct(
        precioPromedioRealPen: Double,
        costoTotalPen: Double
    ): Double {
        require(precioPromedioRealPen > 0.0) {
            "El precio promedio real PEN debe ser mayor a cero."
        }
        require(costoTotalPen > 0.0) { "El costo total PEN debe ser mayor a cero." }

        return ((precioPromedioRealPen - costoTotalPen) / precioPromedioRealPen) * 100.0
    }

    fun calcularPrecioVentaSugeridoPen(
        costoTotalPen: Double,
        margenObjetivoPct: Double
    ): Double {
        require(costoTotalPen > 0.0) { "El costo total PEN debe ser mayor a cero." }
        require(margenObjetivoPct > 0.0 && margenObjetivoPct < 100.0) {
            "El margen objetivo debe estar entre 0 y 100."
        }

        return costoTotalPen / (1.0 - margenObjetivoPct / 100.0)
    }
}
