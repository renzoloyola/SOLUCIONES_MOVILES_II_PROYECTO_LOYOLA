package com.app.changescout.app

import com.app.changescout.domain.model.EvaluacionComercial
import com.app.changescout.domain.model.ResultadoOperacion
import com.app.changescout.domain.usecase.EvaluarTendenciaProductoUseCase
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Singleton
class EvaluadorProductoEnSegundoPlano @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val evaluarTendenciaProductoUseCase: EvaluarTendenciaProductoUseCase
) {
    private val _productosEnEjecucion = MutableStateFlow<Set<Long>>(emptySet())
    val productosEnEjecucion: StateFlow<Set<Long>> = _productosEnEjecucion.asStateFlow()

    private val _resultados = MutableSharedFlow<ResultadoEvaluacionSegundoPlano>(extraBufferCapacity = 16)
    val resultados: SharedFlow<ResultadoEvaluacionSegundoPlano> = _resultados.asSharedFlow()

    @Synchronized
    fun evaluar(productoId: Long): Boolean {
        if (productoId <= 0L || productoId in _productosEnEjecucion.value) return false

        _productosEnEjecucion.value = _productosEnEjecucion.value + productoId
        appScope.launch {
            try {
                _resultados.emit(
                    ResultadoEvaluacionSegundoPlano(
                        productoId = productoId,
                        resultado = evaluarTendenciaProductoUseCase(productoId)
                    )
                )
            } finally {
                _productosEnEjecucion.update { productos -> productos - productoId }
            }
        }
        return true
    }
}

data class ResultadoEvaluacionSegundoPlano(
    val productoId: Long,
    val resultado: ResultadoOperacion<EvaluacionComercial>
)
