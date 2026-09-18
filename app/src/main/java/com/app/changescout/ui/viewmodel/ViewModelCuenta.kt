package com.app.changescout.ui.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.changescout.data.auth.RepositorioSesionSupabase
import com.app.changescout.domain.model.EstadoEvaluacion
import com.app.changescout.domain.model.ProductoRadarItem
import com.app.changescout.domain.usecase.ObservarRadarProductosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class EstadoUiCuenta(
    val email: String = "",
    val nombreUsuario: String = "",
    val desactualizados: Int = 0,
    val lecturasCaducadas: List<LecturaCaducadaUi> = emptyList(),
    val ultimaLectura: String = "Sin lecturas todavia",
    val estaCargando: Boolean = true,
    val mensajeError: String? = null
)

@Immutable
data class LecturaCaducadaUi(
    val productoId: Long,
    val producto: String,
    val fecha: String,
    val antiguedad: String
)

@HiltViewModel
class ViewModelCuenta @Inject constructor(
    private val observarRadarProductosUseCase: ObservarRadarProductosUseCase,
    repositorioSesion: RepositorioSesionSupabase
) : ViewModel() {
    private val sesion = repositorioSesion.sesionActual()
    private val email = sesion?.email.orEmpty()
    private val nombreUsuario = sesion?.nombreUsuario.orEmpty()
    private val _uiState = MutableStateFlow(
        EstadoUiCuenta(email = email, nombreUsuario = nombreUsuario)
    )
    val uiState: StateFlow<EstadoUiCuenta> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observarRadarProductosUseCase()
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            estaCargando = false,
                            mensajeError = error.message ?: "No se pudo cargar el resumen de cuenta."
                        )
                    }
                }
                .collect { radar ->
                    _uiState.update { estado ->
                        crearResumenCuenta(
                            email = email,
                            nombreUsuario = nombreUsuario,
                            radar = radar,
                            ahora = Instant.now()
                        ).copy(
                            mensajeError = estado.mensajeError
                        )
                    }
                }
        }
    }
}

fun crearResumenCuenta(
    email: String,
    nombreUsuario: String,
    radar: List<ProductoRadarItem>,
    ahora: Instant
): EstadoUiCuenta {
    val ultima = radar
        .mapNotNull { item -> item.ultimaEvaluacion?.let { evaluacion -> item.producto.nombre to evaluacion } }
        .maxByOrNull { (_, evaluacion) -> evaluacion.evaluadoEn }
    val caducadas = radar
        .mapNotNull { item ->
            val evaluacion = item.ultimaEvaluacion
            if (evaluacion?.estadoEvaluacion == EstadoEvaluacion.OBSOLETO) {
                LecturaCaducadaUi(
                    productoId = item.producto.id,
                    producto = item.producto.nombre,
                    fecha = evaluacion.evaluadoEn.aFechaCuenta(),
                    antiguedad = evaluacion.evaluadoEn.aTextoRelativo(ahora)
                )
            } else {
                null
            }
        }
        .sortedBy { it.producto }

    return EstadoUiCuenta(
        email = email,
        nombreUsuario = nombreUsuario,
        desactualizados = caducadas.size,
        lecturasCaducadas = caducadas,
        ultimaLectura = ultima?.let { (producto, evaluacion) ->
            "$producto, ${evaluacion.evaluadoEn.aTextoRelativo(ahora)}"
        } ?: "Sin lecturas todavia",
        estaCargando = false
    )
}

private fun Instant.aFechaCuenta(): String {
    return FORMATTER_FECHA_CUENTA.format(this)
}

private val FORMATTER_FECHA_CUENTA: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault())

private fun Instant.aTextoRelativo(ahora: Instant): String {
    val segundos = ((ahora.toEpochMilli() - toEpochMilli()) / 1000).coerceAtLeast(0)
    return when {
        segundos < 60 -> "hace menos de un minuto"
        segundos < 3_600 -> "hace ${segundos / 60} min"
        segundos < 86_400 -> "hace ${segundos / 3_600} h"
        else -> "hace ${segundos / 86_400} dias"
    }
}
