package com.app.changescout.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.Immutable
import com.app.changescout.domain.model.EstadoEvaluacion
import com.app.changescout.domain.model.ProductoRadarItem
import com.app.changescout.domain.model.VeredictoComercial
import com.app.changescout.domain.usecase.ObservarRadarProductosUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@Immutable
data class EstadoUiRadarProductos(
    val productos: List<TarjetaProductoRadarUiModel> = emptyList(),
    val estaCargando: Boolean = true,
    val estaActualizando: Boolean = false,
    val mensajeError: String? = null
)

@Immutable
data class TarjetaProductoRadarUiModel(
    val productoId: Long,
    val nombre: String,
    val cantidadDisponible: Int,
    val margenNetoPct: Double?,
    val precioVentaSugeridoPen: Double?,
    val veredicto: VeredictoComercial?,
    val estadoEvaluacion: EstadoEvaluacion?,
    val evaluadoEn: String?
)

sealed interface EventoRadarProductos {
    data object AgregarProductoSolicitado : EventoRadarProductos
    data class ProductoSeleccionado(val productoId: Long) : EventoRadarProductos
    data object RefrescarRadarSolicitado : EventoRadarProductos
}

sealed interface EfectoRadarProductos {
    data object NavegarAFormularioProducto : EfectoRadarProductos
    data class NavegarADetalleProducto(val productoId: Long) : EfectoRadarProductos
}

@HiltViewModel
class ViewModelRadarProductos @Inject constructor(
    private val observarRadarProductosUseCase: ObservarRadarProductosUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(EstadoUiRadarProductos())
    val uiState: StateFlow<EstadoUiRadarProductos> = _uiState.asStateFlow()

    private val _uiEffect = Channel<EfectoRadarProductos>(Channel.BUFFERED)
    val uiEffect: Flow<EfectoRadarProductos> = _uiEffect.receiveAsFlow()
    private var observarRadarJob: Job? = null

    init {
        observarRadar(mostrarCarga = true)
    }

    private fun observarRadar(mostrarCarga: Boolean) {
        observarRadarJob?.cancel()
        observarRadarJob = viewModelScope.launch {
            _uiState.update { estado ->
                estado.copy(
                    estaCargando = mostrarCarga && estado.productos.isEmpty(),
                    estaActualizando = !mostrarCarga
                )
            }
            observarRadarProductosUseCase()
                .catch { error ->
                    _uiState.update { estado ->
                        estado.copy(
                            estaCargando = false,
                            estaActualizando = false,
                            mensajeError = error.message ?: "No se pudo cargar el radar."
                        )
                    }
                }
                .collect { radar ->
                    _uiState.update { estado ->
                        estado.copy(
                            productos = radar.aTarjetasUi(),
                            estaCargando = false,
                            estaActualizando = false,
                            mensajeError = null
                        )
                    }
                }
        }
    }

    fun onEvent(event: EventoRadarProductos) {
        viewModelScope.launch {
            when (event) {
                EventoRadarProductos.AgregarProductoSolicitado -> {
                    _uiEffect.send(EfectoRadarProductos.NavegarAFormularioProducto)
                }

                is EventoRadarProductos.ProductoSeleccionado -> {
                    _uiEffect.send(EfectoRadarProductos.NavegarADetalleProducto(event.productoId))
                }

                EventoRadarProductos.RefrescarRadarSolicitado -> {
                    observarRadar(mostrarCarga = false)
                }
            }
        }
    }
}

private fun List<ProductoRadarItem>.aTarjetasUi(): List<TarjetaProductoRadarUiModel> {
    return map { item ->
        TarjetaProductoRadarUiModel(
            productoId = item.producto.id,
            nombre = item.producto.nombre,
            cantidadDisponible = item.producto.cantidadDisponible,
            margenNetoPct = item.ultimaEvaluacion?.margenNetoPct,
            precioVentaSugeridoPen = item.ultimaEvaluacion?.precioVentaSugeridoPen,
            veredicto = item.ultimaEvaluacion?.veredicto,
            estadoEvaluacion = item.ultimaEvaluacion?.estadoEvaluacion,
            evaluadoEn = item.ultimaEvaluacion?.evaluadoEn?.aTextoRelativo()
        )
    }
}

private fun java.time.Instant.aTextoRelativo(): String {
    val segundos = ((System.currentTimeMillis() - toEpochMilli()) / 1000).coerceAtLeast(0)
    return when {
        segundos < 60 -> "Evaluado hace menos de un minuto"
        segundos < 3_600 -> "Evaluado hace ${segundos / 60} min"
        segundos < 86_400 -> "Evaluado hace ${segundos / 3_600} h"
        else -> "Evaluado hace ${segundos / 86_400} dias"
    }
}
