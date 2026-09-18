package com.app.changescout.domain.rules

import com.app.changescout.domain.model.ResultadoFiltroNlp
import kotlin.math.sqrt

class PoliticaEvidencia(
    private val minimoCompetidoresValidos: Int = 3,
    private val coeficienteVariacionMaximo: Double = 0.60
) {
    fun tieneEvidenciaSuficiente(resultado: ResultadoFiltroNlp): Boolean {
        return evaluar(resultado).esSuficiente
    }

    fun evaluar(resultado: ResultadoFiltroNlp): ResultadoPoliticaEvidencia {
        val tienePrecio = resultado.precioPromedioRealPen != null &&
            resultado.precioPromedioRealPen > 0.0
        val preciosValidos = resultado.publicacionesValidas
            .map { publicacion -> publicacion.precioPen }
            .filter { precio -> precio > 0.0 }
        val tieneCompetidores = resultado.competidoresValidos >= minimoCompetidoresValidos &&
            preciosValidos.size >= minimoCompetidoresValidos
        val tieneDispersionAceptable = tieneDispersionAceptable(preciosValidos)
        val competidoresComparables = minOf(resultado.competidoresValidos, preciosValidos.size)

        return when {
            !tienePrecio -> ResultadoPoliticaEvidencia(
                esSuficiente = false,
                motivoInsuficiente = "No se pudo estimar un precio promedio confiable en soles."
            )
            !tieneCompetidores -> ResultadoPoliticaEvidencia(
                esSuficiente = false,
                motivoInsuficiente = "Solo se encontraron $competidoresComparables competidores comparables; se necesitan al menos $minimoCompetidoresValidos."
            )
            !tieneDispersionAceptable -> ResultadoPoliticaEvidencia(
                esSuficiente = false,
                motivoInsuficiente = "Los precios encontrados estan demasiado dispersos para calcular un margen confiable."
            )
            else -> ResultadoPoliticaEvidencia(esSuficiente = true)
        }
    }

    private fun tieneDispersionAceptable(precios: List<Double>): Boolean {
        if (precios.size < minimoCompetidoresValidos) return false

        val promedio = precios.average()
        if (promedio <= 0.0) return false

        val varianza = precios
            .map { precio -> precio - promedio }
            .map { diferencia -> diferencia * diferencia }
            .average()
        val coeficienteVariacion = sqrt(varianza) / promedio

        return coeficienteVariacion <= coeficienteVariacionMaximo
    }
}

data class ResultadoPoliticaEvidencia(
    val esSuficiente: Boolean,
    val motivoInsuficiente: String? = null
)
