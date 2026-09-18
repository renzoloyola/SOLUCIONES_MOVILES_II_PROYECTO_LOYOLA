package com.app.changescout.domain.rules

import com.app.changescout.domain.model.EstadoEvaluacion
import com.app.changescout.domain.model.EvaluacionComercial
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

class PoliticaObsolescenciaEvaluacion @Inject constructor() {
    private val ventanaVigencia: Duration = Duration.ofHours(12)

    fun estaVigente(
        evaluadoEn: Instant,
        now: Instant
    ): Boolean {
        val antiguedad = Duration.between(evaluadoEn, now).coerceAtLeast(Duration.ZERO)
        return antiguedad <= ventanaVigencia
    }

    fun resolverEstado(
        evaluacion: EvaluacionComercial,
        now: Instant
    ): EstadoEvaluacion {
        if (
            evaluacion.estadoEvaluacion == EstadoEvaluacion.FALLIDO ||
            evaluacion.estadoEvaluacion == EstadoEvaluacion.INCONCLUSO
        ) {
            return evaluacion.estadoEvaluacion
        }

        return if (estaVigente(evaluacion.evaluadoEn, now)) {
            EstadoEvaluacion.VIGENTE
        } else {
            EstadoEvaluacion.OBSOLETO
        }
    }
}
