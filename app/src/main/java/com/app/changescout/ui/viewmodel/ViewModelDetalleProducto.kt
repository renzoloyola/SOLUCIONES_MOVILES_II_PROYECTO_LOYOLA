package com.app.changescout.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.Immutable
import com.app.changescout.app.EvaluadorProductoEnSegundoPlano
import com.app.changescout.domain.model.ProductoImportado
import com.app.changescout.domain.model.ResultadoOperacion
import com.app.changescout.domain.model.EvaluacionComercial
import com.app.changescout.domain.model.VeredictoComercial
import com.app.changescout.domain.usecase.EliminarProductoImportadoUseCase
import com.app.changescout.domain.usecase.ObservarDetalleProductoUseCase
import com.app.changescout.domain.usecase.ObservarUltimaEvaluacionUseCase
import com.app.changescout.ui.navigation.DestinoApp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class EstadoUiDetalleProducto(
    val producto: ProductoImportado? = null,
    val evaluacion: EvaluacionComercial? = null,
    val estaCargando: Boolean = true,
    val estaEvaluando: Boolean = false,
    val estaEliminando: Boolean = false,
    val mostrarConfirmarEliminacion: Boolean = false,
    val mensajeError: String? = null
)

sealed interface EventoDetalleProducto {
    data object EvaluarProductoActualSolicitado : EventoDetalleProducto
    data object HistorialProductoSolicitado : EventoDetalleProducto
    data object EditarProductoSolicitado : EventoDetalleProducto
    data object EliminarProductoSolicitado : EventoDetalleProducto
    data object CancelarEliminacionSolicitada : EventoDetalleProducto
    data object ConfirmarEliminacionSolicitada : EventoDetalleProducto
    data object RegresarDesdeDetalleSolicitado : EventoDetalleProducto
}

sealed interface EfectoDetalleProducto {
    data object NavegarAtrasDesdeDetalle : EfectoDetalleProducto
    data object NavegarARadarDesdeDetalle : EfectoDetalleProducto
    data class NavegarAHistorialProducto(val productoId: Long) : EfectoDetalleProducto
    data class NavegarAEditarProducto(val producto: ProductoImportado) : EfectoDetalleProducto
    data class MostrarMensajeDetalle(val mensaje: String) : EfectoDetalleProducto
}

@HiltViewModel
class ViewModelDetalleProducto @Inject constructor(
    private val observarDetalleProductoUseCase: ObservarDetalleProductoUseCase,
    private val observarUltimaEvaluacionUseCase: ObservarUltimaEvaluacionUseCase,
    private val eliminarProductoImportadoUseCase: EliminarProductoImportadoUseCase,
    private val evaluadorProductoEnSegundoPlano: EvaluadorProductoEnSegundoPlano,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val productoId: Long = savedStateHandle.get<Long>(DestinoApp.ARG_PRODUCTO_ID) ?: 0L
    private val volverRadarAlActualizar: Boolean =
        savedStateHandle[DestinoApp.ARG_VOLVER_RADAR_AL_ACTUALIZAR] ?: false

    private val _uiState = MutableStateFlow(EstadoUiDetalleProducto())
    val uiState: StateFlow<EstadoUiDetalleProducto> = _uiState.asStateFlow()

    private val _uiEffect = Channel<EfectoDetalleProducto>(Channel.BUFFERED)
    val uiEffect: Flow<EfectoDetalleProducto> = _uiEffect.receiveAsFlow()

    init {
        viewModelScope.launch {
            combine(
                observarDetalleProductoUseCase(productoId),
                observarUltimaEvaluacionUseCase(productoId)
            ) { producto, evaluacion ->
                producto to evaluacion
            }.collect { (producto, evaluacion) ->
                _uiState.update { estado ->
                    estado.copy(
                        producto = producto,
                        evaluacion = evaluacion,
                        estaCargando = false,
                        mensajeError = if (producto == null) {
                            "No encontramos este producto en el radar actual."
                        } else {
                            null
                        }
                    )
                }
            }
        }
        viewModelScope.launch {
            evaluadorProductoEnSegundoPlano.productosEnEjecucion.collect { productos ->
                _uiState.update { estado ->
                    estado.copy(estaEvaluando = productoId in productos)
                }
            }
        }
        viewModelScope.launch {
            evaluadorProductoEnSegundoPlano.resultados.collect { resultado ->
                if (resultado.productoId == productoId) {
                    manejarResultadoEvaluacion(resultado.resultado)
                }
            }
        }
    }

    fun onEvent(event: EventoDetalleProducto) {
        viewModelScope.launch {
            when (event) {
                EventoDetalleProducto.EvaluarProductoActualSolicitado -> {
                    solicitarEvaluacionProductoActual()
                }

                EventoDetalleProducto.HistorialProductoSolicitado -> {
                    if (_uiState.value.producto != null) {
                        _uiEffect.send(EfectoDetalleProducto.NavegarAHistorialProducto(productoId))
                    }
                }

                EventoDetalleProducto.EditarProductoSolicitado -> {
                    _uiState.value.producto?.let { producto ->
                        _uiEffect.send(EfectoDetalleProducto.NavegarAEditarProducto(producto))
                    }
                }

                EventoDetalleProducto.EliminarProductoSolicitado -> {
                    _uiState.update { it.copy(mostrarConfirmarEliminacion = true) }
                }

                EventoDetalleProducto.CancelarEliminacionSolicitada -> {
                    _uiState.update { it.copy(mostrarConfirmarEliminacion = false) }
                }

                EventoDetalleProducto.ConfirmarEliminacionSolicitada -> {
                    eliminarProductoActual()
                }

                EventoDetalleProducto.RegresarDesdeDetalleSolicitado -> {
                    _uiEffect.send(EfectoDetalleProducto.NavegarAtrasDesdeDetalle)
                }
            }
        }
    }

    private suspend fun eliminarProductoActual() {
        if (_uiState.value.estaEliminando) return

        _uiState.update {
            it.copy(
                estaEliminando = true,
                mostrarConfirmarEliminacion = false,
                mensajeError = null
            )
        }

        runCatching {
            eliminarProductoImportadoUseCase(productoId)
        }.onSuccess {
            _uiEffect.send(EfectoDetalleProducto.NavegarAtrasDesdeDetalle)
        }.onFailure { throwable ->
            val mensaje = throwable.message ?: "No se pudo eliminar el producto."
            _uiState.update {
                it.copy(
                    estaEliminando = false,
                    mensajeError = mensaje
                )
            }
            _uiEffect.send(EfectoDetalleProducto.MostrarMensajeDetalle(mensaje))
        }
    }

    private fun solicitarEvaluacionProductoActual() {
        if (_uiState.value.estaEvaluando) return

        _uiState.update { estado ->
            estado.copy(estaEvaluando = true, mensajeError = null)
        }

        if (!evaluadorProductoEnSegundoPlano.evaluar(productoId)) {
            _uiState.update { estado ->
                estado.copy(
                    estaEvaluando = productoId in evaluadorProductoEnSegundoPlano.productosEnEjecucion.value
                )
            }
        }
    }

    private suspend fun manejarResultadoEvaluacion(resultado: ResultadoOperacion<EvaluacionComercial>) {
        when (resultado) {
            is ResultadoOperacion.Exito -> {
                _uiEffect.send(
                    EfectoDetalleProducto.MostrarMensajeDetalle(
                        "Lectura actualizada: ${resultado.data.veredicto.aTextoPresentable()}."
                    )
                )
                navegarARadarSiCorresponde()
            }
            is ResultadoOperacion.DatosObsoletos -> {
                _uiEffect.send(
                    EfectoDetalleProducto.MostrarMensajeDetalle(
                        "Lectura generada con datos obsoletos: ${resultado.causa.mensaje}"
                    )
                )
                navegarARadarSiCorresponde()
            }
            is ResultadoOperacion.Fallo -> {
                _uiState.update { estado ->
                    estado.copy(mensajeError = resultado.error.mensaje)
                }
                _uiEffect.send(
                    EfectoDetalleProducto.MostrarMensajeDetalle(resultado.error.mensaje)
                )
            }
        }
    }

    private suspend fun navegarARadarSiCorresponde() {
        if (volverRadarAlActualizar) {
            _uiEffect.send(EfectoDetalleProducto.NavegarARadarDesdeDetalle)
        }
    }

    private fun VeredictoComercial?.aTextoPresentable(): String {
        return when (this) {
            VeredictoComercial.SALUDABLE -> "Saludable"
            VeredictoComercial.PRECAUCION -> "Precaucion"
            VeredictoComercial.ALERTA_TEMPRANA_QUIEBRE -> "Margen en riesgo"
            VeredictoComercial.LIQUIDACION -> "Conviene liquidar stock"
            VeredictoComercial.INCONCLUSO -> "Sin datos suficientes"
            null -> "Sin clasificar"
        }
    }
}
