package com.app.changescout.ui.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.changescout.domain.model.EstadoEvaluacion
import com.app.changescout.domain.model.EvaluacionComercial
import com.app.changescout.domain.model.ProductoImportado
import com.app.changescout.domain.model.VeredictoComercial
import com.app.changescout.domain.usecase.ObservarDetalleProductoUseCase
import com.app.changescout.domain.usecase.ObservarHistorialEvaluacionesProductoUseCase
import com.app.changescout.ui.navigation.DestinoApp
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class EstadoUiHistorialProducto(
    val productoNombre: String = "",
    val puntos: List<PuntoHistorialProductoUi> = emptyList(),
    val ultimaLectura: PuntoHistorialProductoUi? = null,
    val estaCargando: Boolean = true,
    val mensajeError: String? = null
)

@Immutable
data class PuntoHistorialProductoUi(
    val etiquetaFecha: String,
    val precioRefPen: Double?,
    val costoDestinoPen: Double?,
    val precioSugeridoPen: Double?,
    val margenNetoPct: Double?,
    val competidoresValidos: Int,
    val erosionPrecioLocalPct: Double?,
    val variacionCompetidoresPct: Double?,
    val presionCambiariaPct: Double?,
    val estadoEvaluacion: EstadoEvaluacion,
    val veredicto: VeredictoComercial?
)

@HiltViewModel
class ViewModelHistorialProducto @Inject constructor(
    private val observarDetalleProductoUseCase: ObservarDetalleProductoUseCase,
    private val observarHistorialEvaluacionesProductoUseCase: ObservarHistorialEvaluacionesProductoUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val productoId: Long = savedStateHandle[DestinoApp.ARG_PRODUCTO_ID] ?: 0L

    private val _uiState = MutableStateFlow(EstadoUiHistorialProducto())
    val uiState: StateFlow<EstadoUiHistorialProducto> = _uiState.asStateFlow()

    init {
        if (productoId <= 0L) {
            _uiState.value = EstadoUiHistorialProducto(
                estaCargando = false,
                mensajeError = "No se pudo abrir el historial de este producto."
            )
        } else {
            viewModelScope.launch {
                combine(
                    observarDetalleProductoUseCase(productoId),
                    observarHistorialEvaluacionesProductoUseCase(productoId)
                ) { producto, historial ->
                    crearEstado(producto, historial)
                }
                    .catch { error ->
                        _uiState.update {
                            it.copy(
                                estaCargando = false,
                                mensajeError = error.message ?: "No se pudo cargar el historial."
                            )
                        }
                    }
                    .collect { estado ->
                        _uiState.value = estado
                    }
            }
        }
    }

    private fun crearEstado(
        producto: ProductoImportado?,
        historial: List<EvaluacionComercial>
    ): EstadoUiHistorialProducto {
        val puntos = historial
            .sortedBy { evaluacion -> evaluacion.evaluadoEn }
            .map { evaluacion -> evaluacion.aPuntoHistorialUi() }

        return EstadoUiHistorialProducto(
            productoNombre = producto?.nombre.orEmpty(),
            puntos = puntos,
            ultimaLectura = puntos.lastOrNull(),
            estaCargando = false,
            mensajeError = if (producto == null) {
                "No encontramos este producto en el radar actual."
            } else {
                null
            }
        )
    }

    private fun EvaluacionComercial.aPuntoHistorialUi(): PuntoHistorialProductoUi {
        return PuntoHistorialProductoUi(
            etiquetaFecha = formatoFechaHistorial.format(evaluadoEn),
            precioRefPen = precioPromedioRealPen,
            costoDestinoPen = costoTotalPen,
            precioSugeridoPen = precioVentaSugeridoPen,
            margenNetoPct = margenNetoPct,
            competidoresValidos = competidoresValidos,
            erosionPrecioLocalPct = metricasTendencia?.erosionPrecioLocalPct,
            variacionCompetidoresPct = metricasTendencia?.variacionCompetidoresPct,
            presionCambiariaPct = metricasTendencia?.presionCambiariaPct,
            estadoEvaluacion = estadoEvaluacion,
            veredicto = veredicto
        )
    }

    private companion object {
        val formatoFechaHistorial: DateTimeFormatter = DateTimeFormatter
            .ofPattern("dd/MM")
            .withZone(ZoneId.systemDefault())
    }
}
