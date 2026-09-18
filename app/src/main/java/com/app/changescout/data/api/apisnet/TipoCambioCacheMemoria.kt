package com.app.changescout.data.api.apisnet

import com.app.changescout.domain.model.CotizacionTipoCambio
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TipoCambioCacheMemoria @Inject constructor(
    private val clock: Clock
) {
    private val cache = linkedMapOf<LocalDate, CotizacionTipoCambio>()

    @Synchronized
    fun obtener(fecha: LocalDate): CotizacionTipoCambio? {
        depurar()
        return cache[fecha]
    }

    @Synchronized
    fun guardar(
        fecha: LocalDate,
        cotizacion: CotizacionTipoCambio
    ) {
        cache[fecha] = cotizacion
        depurar()
    }

    @Synchronized
    fun obtenerMasRecienteHasta(fechaMaxima: LocalDate): CotizacionTipoCambio? {
        depurar()
        return cache
            .filterKeys { fecha -> !fecha.isAfter(fechaMaxima) }
            .maxByOrNull { entry -> entry.key.toEpochDay() }
            ?.value
    }

    @Synchronized
    fun cantidadEntradas(): Int {
        depurar()
        return cache.size
    }

    private fun depurar() {
        val fechaMinima = LocalDate.now(clock).minusDays(DIAS_MAXIMOS_CACHE - 1L)
        val fechasObsoletas = cache.keys.filter { fecha -> fecha.isBefore(fechaMinima) }
        fechasObsoletas.forEach { fecha -> cache.remove(fecha) }

        while (cache.size > DIAS_MAXIMOS_CACHE) {
            val fechaMasAntigua = cache.keys.minOrNull() ?: return
            cache.remove(fechaMasAntigua)
        }
    }

    private companion object {
        const val DIAS_MAXIMOS_CACHE = 30
    }
}
