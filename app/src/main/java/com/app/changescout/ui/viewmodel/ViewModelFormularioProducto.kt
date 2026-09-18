package com.app.changescout.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.Immutable
import com.app.changescout.domain.model.ComponentesCostoImportacion
import com.app.changescout.domain.model.ProductoImportado
import com.app.changescout.domain.usecase.GuardarProductoImportadoUseCase
import com.app.changescout.domain.usecase.ObservarDetalleProductoUseCase
import com.app.changescout.ui.navigation.DestinoApp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private object FormularioProductoKeys {
    const val NOMBRE = DestinoApp.ARG_FORM_NOMBRE
    const val PRECIO_FOB = DestinoApp.ARG_FORM_PRECIO_FOB
    const val FLETE = DestinoApp.ARG_FORM_FLETE
    const val SEGURO = DestinoApp.ARG_FORM_SEGURO
    const val ARANCELES = DestinoApp.ARG_FORM_ARANCELES
    const val OTROS_CARGOS = DestinoApp.ARG_FORM_OTROS_CARGOS
    const val CANTIDAD = DestinoApp.ARG_FORM_CANTIDAD
    const val MARGEN_OBJETIVO = DestinoApp.ARG_FORM_MARGEN_OBJETIVO
    const val QUERY = DestinoApp.ARG_FORM_QUERY
}

@Immutable
data class EstadoUiFormularioProducto(
    val nombre: String = "",
    val precioFobUsd: String = "",
    val fleteUsd: String = "",
    val seguroUsd: String = "",
    val arancelesUsd: String = "",
    val otrosCargosUsd: String = "",
    val cantidadDisponible: String = "",
    val margenObjetivoPct: String = "20",
    val queryCompetencia: String = "",
    val estaGuardando: Boolean = false,
    val mensajeValidacion: String? = null,
    val esEdicion: Boolean = false,
    val estaCargandoEdicion: Boolean = false
) {
    val puedeEnviar: Boolean
        get() = nombre.isNotBlank() &&
            precioFobUsd.isNotBlank() &&
            cantidadDisponible.isNotBlank() &&
            margenObjetivoPct.isNotBlank() &&
            queryCompetencia.isNotBlank() &&
            !estaGuardando
}

sealed interface EventoFormularioProducto {
    data class NombreCambiado(val value: String) : EventoFormularioProducto
    data class PrecioFobUsdCambiado(val value: String) : EventoFormularioProducto
    data class FleteUsdCambiado(val value: String) : EventoFormularioProducto
    data class SeguroUsdCambiado(val value: String) : EventoFormularioProducto
    data class ArancelesUsdCambiado(val value: String) : EventoFormularioProducto
    data class OtrosCargosUsdCambiado(val value: String) : EventoFormularioProducto
    data class CantidadDisponibleCambiada(val value: String) : EventoFormularioProducto
    data class MargenObjetivoCambiado(val value: String) : EventoFormularioProducto
    data class QueryCompetenciaCambiado(val value: String) : EventoFormularioProducto
    data object GuardarProductoSolicitado : EventoFormularioProducto
    data object RegresarDesdeFormularioSolicitado : EventoFormularioProducto
}

sealed interface EfectoFormularioProducto {
    data object NavegarAtrasDesdeFormulario : EfectoFormularioProducto
    data class MostrarMensajeFormulario(val mensaje: String) : EfectoFormularioProducto
}

@HiltViewModel
class ViewModelFormularioProducto @Inject constructor(
    private val guardarProductoImportadoUseCase: GuardarProductoImportadoUseCase,
    private val observarDetalleProductoUseCase: ObservarDetalleProductoUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val productoId: Long = savedStateHandle[DestinoApp.ARG_PRODUCTO_ID] ?: 0L
    private var productoEditadoPrecargado = false

    private val _uiState = MutableStateFlow(
        EstadoUiFormularioProducto(
            nombre = savedStateHandle[FormularioProductoKeys.NOMBRE] ?: "",
            precioFobUsd = savedStateHandle[FormularioProductoKeys.PRECIO_FOB] ?: "",
            fleteUsd = savedStateHandle[FormularioProductoKeys.FLETE] ?: "",
            seguroUsd = savedStateHandle[FormularioProductoKeys.SEGURO] ?: "",
            arancelesUsd = savedStateHandle[FormularioProductoKeys.ARANCELES] ?: "",
            otrosCargosUsd = savedStateHandle[FormularioProductoKeys.OTROS_CARGOS] ?: "",
            cantidadDisponible = savedStateHandle[FormularioProductoKeys.CANTIDAD] ?: "",
            margenObjetivoPct = savedStateHandle[FormularioProductoKeys.MARGEN_OBJETIVO] ?: "20",
            queryCompetencia = savedStateHandle[FormularioProductoKeys.QUERY] ?: "",
            esEdicion = productoId > 0L,
            estaCargandoEdicion = productoId > 0L && borradorEstaVacio()
        )
    )
    val uiState: StateFlow<EstadoUiFormularioProducto> = _uiState.asStateFlow()

    private val _uiEffect = Channel<EfectoFormularioProducto>(Channel.BUFFERED)
    val uiEffect: Flow<EfectoFormularioProducto> = _uiEffect.receiveAsFlow()

    init {
        if (productoId > 0L && borradorEstaVacio()) {
            viewModelScope.launch {
                val producto = observarDetalleProductoUseCase(productoId).first()
                if (producto != null && !productoEditadoPrecargado) {
                    productoEditadoPrecargado = true
                    cargarProducto(producto)
                } else {
                    _uiState.update {
                        it.copy(
                            estaCargandoEdicion = false,
                            mensajeValidacion = "No encontramos la ficha para editar."
                        )
                    }
                }
            }
        }
    }

    fun onEvent(event: EventoFormularioProducto) {
        when (event) {
            is EventoFormularioProducto.NombreCambiado -> actualizarBorrador(nombre = event.value)
            is EventoFormularioProducto.PrecioFobUsdCambiado -> actualizarBorrador(precioFob = event.value)
            is EventoFormularioProducto.FleteUsdCambiado -> actualizarBorrador(flete = event.value)
            is EventoFormularioProducto.SeguroUsdCambiado -> actualizarBorrador(seguro = event.value)
            is EventoFormularioProducto.ArancelesUsdCambiado -> actualizarBorrador(aranceles = event.value)
            is EventoFormularioProducto.OtrosCargosUsdCambiado -> actualizarBorrador(otrosCargos = event.value)
            is EventoFormularioProducto.CantidadDisponibleCambiada -> actualizarBorrador(cantidad = event.value)
            is EventoFormularioProducto.MargenObjetivoCambiado -> actualizarBorrador(margenObjetivo = event.value)
            is EventoFormularioProducto.QueryCompetenciaCambiado -> actualizarBorrador(query = event.value)
            EventoFormularioProducto.GuardarProductoSolicitado -> guardarProducto()
            EventoFormularioProducto.RegresarDesdeFormularioSolicitado -> {
                viewModelScope.launch {
                    _uiEffect.send(EfectoFormularioProducto.NavegarAtrasDesdeFormulario)
                }
            }
        }
    }

    private fun actualizarBorrador(
        nombre: String? = null,
        precioFob: String? = null,
        flete: String? = null,
        seguro: String? = null,
        aranceles: String? = null,
        otrosCargos: String? = null,
        cantidad: String? = null,
        margenObjetivo: String? = null,
        query: String? = null
    ) {
        _uiState.update { estado ->
            val nuevoNombre = nombre ?: estado.nombre
            val nuevoQuery = when {
                query != null -> query
                nombre != null && (estado.queryCompetencia.isBlank() || estado.queryCompetencia == estado.nombre) -> nombre
                else -> estado.queryCompetencia
            }
            estado.copy(
                nombre = nuevoNombre,
                precioFobUsd = precioFob ?: estado.precioFobUsd,
                fleteUsd = flete ?: estado.fleteUsd,
                seguroUsd = seguro ?: estado.seguroUsd,
                arancelesUsd = aranceles ?: estado.arancelesUsd,
                otrosCargosUsd = otrosCargos ?: estado.otrosCargosUsd,
                cantidadDisponible = cantidad ?: estado.cantidadDisponible,
                margenObjetivoPct = margenObjetivo ?: estado.margenObjetivoPct,
                queryCompetencia = nuevoQuery,
                mensajeValidacion = null
            )
        }
        savedStateHandle[FormularioProductoKeys.NOMBRE] = _uiState.value.nombre
        savedStateHandle[FormularioProductoKeys.PRECIO_FOB] = _uiState.value.precioFobUsd
        savedStateHandle[FormularioProductoKeys.FLETE] = _uiState.value.fleteUsd
        savedStateHandle[FormularioProductoKeys.SEGURO] = _uiState.value.seguroUsd
        savedStateHandle[FormularioProductoKeys.ARANCELES] = _uiState.value.arancelesUsd
        savedStateHandle[FormularioProductoKeys.OTROS_CARGOS] = _uiState.value.otrosCargosUsd
        savedStateHandle[FormularioProductoKeys.CANTIDAD] = _uiState.value.cantidadDisponible
        savedStateHandle[FormularioProductoKeys.MARGEN_OBJETIVO] = _uiState.value.margenObjetivoPct
        savedStateHandle[FormularioProductoKeys.QUERY] = _uiState.value.queryCompetencia
    }

    private fun guardarProducto() {
        viewModelScope.launch {
            _uiState.update { it.copy(estaGuardando = true, mensajeValidacion = null) }
            runCatching {
                guardarProductoImportadoUseCase(construirProductoImportado())
            }.onSuccess {
                limpiarBorrador()
                _uiEffect.send(EfectoFormularioProducto.NavegarAtrasDesdeFormulario)
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        estaGuardando = false,
                        mensajeValidacion = throwable.message ?: "No se pudo guardar la ficha."
                    )
                }
                _uiEffect.send(
                    EfectoFormularioProducto.MostrarMensajeFormulario(
                        _uiState.value.mensajeValidacion ?: "No se pudo guardar la ficha."
                    )
                )
            }
        }
    }

    private fun construirProductoImportado(): ProductoImportado {
        val precioFob = _uiState.value.precioFobUsd.parsearMontoUsd("precio FOB")
        val flete = _uiState.value.fleteUsd.parsearMontoUsdOpcional()
        val seguro = _uiState.value.seguroUsd.parsearMontoUsdOpcional()
        val aranceles = _uiState.value.arancelesUsd.parsearMontoUsdOpcional()
        val otrosCargos = _uiState.value.otrosCargosUsd.parsearMontoUsdOpcional()
        val cantidad = _uiState.value.cantidadDisponible.trim().toIntOrNull()
            ?: error("Ingresa una cantidad disponible valida.")
        val margenObjetivo = _uiState.value.margenObjetivoPct.parsearPorcentajeMargen()

        return ProductoImportado(
            id = productoId,
            nombre = _uiState.value.nombre.trim(),
            queryCompetencia = _uiState.value.queryCompetencia.trim(),
            componentesCosto = ComponentesCostoImportacion(
                precioFobUsd = precioFob,
                fleteUsd = flete,
                seguroUsd = seguro,
                arancelesUsd = aranceles,
                otrosCargosUsd = otrosCargos
            ),
            cantidadDisponible = cantidad,
            margenObjetivoPct = margenObjetivo
        )
    }

    private fun limpiarBorrador() {
        savedStateHandle[FormularioProductoKeys.NOMBRE] = ""
        savedStateHandle[FormularioProductoKeys.PRECIO_FOB] = ""
        savedStateHandle[FormularioProductoKeys.FLETE] = ""
        savedStateHandle[FormularioProductoKeys.SEGURO] = ""
        savedStateHandle[FormularioProductoKeys.ARANCELES] = ""
        savedStateHandle[FormularioProductoKeys.OTROS_CARGOS] = ""
        savedStateHandle[FormularioProductoKeys.CANTIDAD] = ""
        savedStateHandle[FormularioProductoKeys.MARGEN_OBJETIVO] = "20"
        savedStateHandle[FormularioProductoKeys.QUERY] = ""
        _uiState.value = EstadoUiFormularioProducto()
    }

    private fun cargarProducto(producto: ProductoImportado) {
        _uiState.value = EstadoUiFormularioProducto(
            nombre = producto.nombre,
            precioFobUsd = producto.componentesCosto.precioFobUsd.toInput(),
            fleteUsd = producto.componentesCosto.fleteUsd.toInput(),
            seguroUsd = producto.componentesCosto.seguroUsd.toInput(),
            arancelesUsd = producto.componentesCosto.arancelesUsd.toInput(),
            otrosCargosUsd = producto.componentesCosto.otrosCargosUsd.toInput(),
            cantidadDisponible = producto.cantidadDisponible.toString(),
            margenObjetivoPct = producto.margenObjetivoPct.toPercentInput(),
            queryCompetencia = producto.queryCompetencia,
            esEdicion = true,
            estaCargandoEdicion = false
        )
    }

    private fun String.parsearMontoUsd(nombreCampo: String): Double {
        return trim()
            .replace(",", ".")
            .toDoubleOrNull()
            ?: error("Ingresa un monto USD valido para $nombreCampo.")
    }

    private fun String.parsearPorcentajeMargen(): Double {
        val margen = trim()
            .replace(",", ".")
            .toDoubleOrNull()
            ?: error("Ingresa un margen objetivo valido.")
        require(margen > 0.0 && margen < 100.0) {
            "El margen objetivo debe estar entre 0 y 100."
        }
        return margen
    }

    private fun String.parsearMontoUsdOpcional(): Double {
        return takeIf { valor -> valor.isNotBlank() }
            ?.parsearMontoUsd("el costo opcional")
            ?: 0.0
    }

    private fun borradorEstaVacio(): Boolean {
        return (savedStateHandle[FormularioProductoKeys.NOMBRE] ?: "").isBlank() &&
            (savedStateHandle[FormularioProductoKeys.PRECIO_FOB] ?: "").isBlank() &&
            (savedStateHandle[FormularioProductoKeys.FLETE] ?: "").isBlank() &&
            (savedStateHandle[FormularioProductoKeys.SEGURO] ?: "").isBlank() &&
            (savedStateHandle[FormularioProductoKeys.ARANCELES] ?: "").isBlank() &&
            (savedStateHandle[FormularioProductoKeys.OTROS_CARGOS] ?: "").isBlank() &&
            (savedStateHandle[FormularioProductoKeys.CANTIDAD] ?: "").isBlank() &&
            (savedStateHandle[FormularioProductoKeys.MARGEN_OBJETIVO] ?: "").let { it.isBlank() || it == "20" } &&
            (savedStateHandle[FormularioProductoKeys.QUERY] ?: "").isBlank()
    }

    private fun Double.toInput(): String {
        return if (this == 0.0) "" else toString()
    }

    private fun Double.toPercentInput(): String {
        return if (this % 1.0 == 0.0) toInt().toString() else toString()
    }
}
