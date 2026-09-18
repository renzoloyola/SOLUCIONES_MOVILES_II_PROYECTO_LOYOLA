package com.app.changescout.ui.screens.product.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.changescout.domain.model.EstadoBrechaPrecioSugerido
import com.app.changescout.domain.model.EstadoEvaluacion
import com.app.changescout.domain.model.EvaluacionComercial
import com.app.changescout.domain.model.ProductoImportado
import com.app.changescout.domain.model.VeredictoComercial
import com.app.changescout.ui.screens.components.BotonPrimario
import com.app.changescout.ui.screens.components.ChipOperativo
import com.app.changescout.ui.screens.components.EncabezadoSeccion
import com.app.changescout.ui.screens.components.FondoOperativo
import com.app.changescout.ui.screens.components.MetricaResumida
import com.app.changescout.ui.screens.components.TarjetaOperativa
import com.app.changescout.ui.theme.ErrorSoft
import com.app.changescout.ui.theme.SignalGold
import com.app.changescout.ui.theme.SuccessContainer
import com.app.changescout.ui.viewmodel.EfectoDetalleProducto
import com.app.changescout.ui.viewmodel.EstadoUiDetalleProducto
import com.app.changescout.ui.viewmodel.EventoDetalleProducto
import com.app.changescout.ui.viewmodel.ViewModelDetalleProducto
import kotlin.math.absoluteValue

@Composable
fun PantallaDetalleProducto(
    onNavegarAtras: () -> Unit,
    onNavegarARadar: () -> Unit,
    onNavegarAHistorial: (Long) -> Unit,
    onNavegarAEditar: (ProductoImportado) -> Unit,
    detalleViewModel: ViewModelDetalleProducto = hiltViewModel()
) {
    val state by detalleViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(detalleViewModel) {
        detalleViewModel.uiEffect.collect { effect ->
            when (effect) {
                EfectoDetalleProducto.NavegarAtrasDesdeDetalle -> onNavegarAtras()
                EfectoDetalleProducto.NavegarARadarDesdeDetalle -> onNavegarARadar()
                is EfectoDetalleProducto.NavegarAHistorialProducto -> onNavegarAHistorial(effect.productoId)
                is EfectoDetalleProducto.NavegarAEditarProducto -> onNavegarAEditar(effect.producto)
                is EfectoDetalleProducto.MostrarMensajeDetalle -> {
                    snackbarHostState.showSnackbar(effect.mensaje)
                }
            }
        }
    }

    FondoOperativo {
        ContenidoDetalleProducto(
            state = state,
            snackbarHostState = snackbarHostState,
            onEvent = detalleViewModel::onEvent
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContenidoDetalleProducto(
    state: EstadoUiDetalleProducto,
    snackbarHostState: SnackbarHostState,
    onEvent: (EventoDetalleProducto) -> Unit
) {
    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                ),
                title = {
                    Text("Producto")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        onEvent(EventoDetalleProducto.RegresarDesdeDetalleSolicitado)
                    }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onEvent(EventoDetalleProducto.HistorialProductoSolicitado) },
                        enabled = state.producto != null
                    ) {
                        Icon(Icons.Outlined.History, contentDescription = "Historial")
                    }
                    IconButton(
                        onClick = { onEvent(EventoDetalleProducto.EditarProductoSolicitado) },
                        enabled = state.producto != null && !state.estaEliminando
                    ) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Editar producto")
                    }
                    IconButton(
                        onClick = { onEvent(EventoDetalleProducto.EliminarProductoSolicitado) },
                        enabled = state.producto != null && !state.estaEliminando
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Eliminar producto")
                    }
                }
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val horizontal = maxWidth > maxHeight
            val contentModifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp)
            if (state.estaCargando) {
                Column(modifier = contentModifier) {
                    TarjetaOperativa {
                        EncabezadoSeccion(
                            icono = Icons.Outlined.Schedule,
                            titulo = "Cargando producto"
                        )
                    }
                }
            } else if (horizontal) {
                Row(
                    modifier = contentModifier,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(0.85f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TarjetaFichaProducto(state.producto)
                        MensajeErrorDetalle(state.mensajeError)
                    }
                    Column(
                        modifier = Modifier.weight(1.15f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TarjetaResultadoProducto(
                            state = state,
                            onEvent = onEvent
                        )
                    }
                }
            } else {
                Column(
                    modifier = contentModifier,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TarjetaFichaProducto(state.producto)
                    TarjetaResultadoProducto(state, onEvent)
                    MensajeErrorDetalle(state.mensajeError)
                }
            }
        }
    }

    if (state.mostrarConfirmarEliminacion) {
        AlertDialog(
            onDismissRequest = {
                onEvent(EventoDetalleProducto.CancelarEliminacionSolicitada)
            },
            title = { Text("Eliminar producto") },
            text = {
                Text("Se quitara esta ficha y sus lecturas guardadas del dispositivo.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEvent(EventoDetalleProducto.ConfirmarEliminacionSolicitada)
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onEvent(EventoDetalleProducto.CancelarEliminacionSolicitada)
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun TarjetaFichaProducto(producto: ProductoImportado?) {
    producto?.let {
        TarjetaOperativa {
            EncabezadoSeccion(
                icono = Icons.Outlined.Inventory2,
                titulo = it.nombre
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricaResumida(
                    titulo = "Costo base",
                    valor = "USD ${"%.2f".format(it.componentesCosto.precioFobUsd)}",
                    icono = Icons.Outlined.AttachMoney,
                    modifier = Modifier.weight(1f)
                )
                MetricaResumida(
                    titulo = "Stock",
                    valor = it.cantidadDisponible.toString(),
                    icono = Icons.Outlined.Inventory2,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TarjetaResultadoProducto(
    state: EstadoUiDetalleProducto,
    onEvent: (EventoDetalleProducto) -> Unit,
    modifier: Modifier = Modifier
) {
    TarjetaOperativa(modifier = modifier) {
        EncabezadoSeccion(
            icono = Icons.Outlined.QueryStats,
            titulo = "Resultado comercial"
        )

        if (state.evaluacion == null) {
            ChipOperativo(
                texto = "Sin lectura cargada",
                icono = Icons.Outlined.Schedule,
                contenedor = MaterialTheme.colorScheme.surfaceVariant
            )
            Text(
                text = "Actualiza la ficha para comparar costo en destino contra precios reales del mercado.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            state.evaluacion.evaluacionResumen(state.producto)
        }

        if (state.estaEvaluando) {
            ChipOperativo(
                texto = "Consultando mercado en vivo",
                icono = Icons.Outlined.Search,
                modifier = Modifier.fillMaxWidth(),
                contenedor = SignalGold.copy(alpha = 0.18f)
            )
        }

        BotonPrimario(
            texto = if (state.estaEvaluando) "Consultando..." else "Actualizar lectura",
            icono = Icons.Outlined.QueryStats,
            onClick = {
                onEvent(EventoDetalleProducto.EvaluarProductoActualSolicitado)
            },
            enabled = !state.estaEvaluando && state.producto != null,
            modifier = Modifier.fillMaxWidth(),
            cargando = state.estaEvaluando
        )
    }
}

@Composable
private fun MensajeErrorDetalle(mensaje: String?) {
    mensaje?.let {
        TarjetaOperativa {
            ChipOperativo(
                texto = "Revision requerida",
                icono = Icons.Outlined.WarningAmber,
                contenedor = ErrorSoft
            )
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun EvaluacionComercial.evaluacionResumen(producto: ProductoImportado?) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ChipOperativo(
            texto = veredicto?.etiqueta() ?: "Sin clasificar",
            icono = Icons.Outlined.Sell,
            contenedor = veredicto.colorContenedor()
        )
        Text(
            text = veredicto.descripcionUsuario(motivoEvidenciaInsuficiente),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        ChipOperativo(
            texto = "Evaluado ${evaluadoEn.aTextoRelativo()}",
            icono = Icons.Outlined.Schedule,
            contenedor = MaterialTheme.colorScheme.surfaceVariant
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MetricaResumida(
            titulo = "Precio ref.",
            valor = precioPromedioRealPen?.let { "S/ ${"%.2f".format(it)}" } ?: "--",
            icono = Icons.Outlined.AttachMoney,
            modifier = Modifier.weight(1f)
        )
        MetricaResumida(
            titulo = "Costo destino",
            valor = costoTotalPen?.let { "S/ ${"%.2f".format(it)}" } ?: "--",
            icono = Icons.Outlined.Inventory2,
            modifier = Modifier.weight(1f)
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MetricaResumida(
            titulo = "Precio sugerido",
            valor = precioVentaSugeridoPen?.let { "S/ ${"%.2f".format(it)}" } ?: "--",
            icono = Icons.Outlined.Sell,
            modifier = Modifier.weight(1f)
        )
        MetricaResumida(
            titulo = "Margen objetivo",
            valor = margenObjetivoPct?.let { "${"%.0f".format(it)}%" } ?: "--",
            icono = Icons.Outlined.QueryStats,
            modifier = Modifier.weight(1f)
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MetricaResumida(
            titulo = "Margen",
            valor = margenNetoPct?.let { "${"%.1f".format(it)}%" } ?: "--",
            icono = Icons.Outlined.Sell,
            modifier = Modifier.weight(1f)
        )
        MetricaResumida(
            titulo = "Validas",
            valor = competidoresValidos.toString(),
            icono = Icons.Outlined.Shield,
            modifier = Modifier.weight(1f)
        )
    }

    gananciaEstimadaLotePen(producto)?.let { ganancia ->
        MetricaResumida(
            titulo = "Ganancia lote",
            valor = "S/ ${"%.2f".format(ganancia)}",
            icono = Icons.Outlined.AttachMoney,
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (precioVentaSugeridoPen != null) {
        Text(
            text = descripcionPrecioSugerido(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (estadoEvaluacion == EstadoEvaluacion.OBSOLETO) {
        Text(
            text = "Esta lectura tiene mas de 12 horas. Actualiza para revisar el mercado de hoy.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
    }

    tipoCambioVentaUsdPen?.let { tasa ->
        ChipOperativo(
            texto = "TC venta ${"%.3f".format(tasa)}",
            icono = Icons.Outlined.AttachMoney,
            contenedor = MaterialTheme.colorScheme.surfaceVariant
        )
    }

}

private fun VeredictoComercial?.etiqueta(): String {
    return when (this) {
        VeredictoComercial.SALUDABLE -> "Saludable"
        VeredictoComercial.PRECAUCION -> "Precaucion"
        VeredictoComercial.ALERTA_TEMPRANA_QUIEBRE -> "Margen en riesgo"
        VeredictoComercial.LIQUIDACION -> "Conviene liquidar stock"
        VeredictoComercial.INCONCLUSO -> "Sin datos suficientes"
        null -> "Sin lectura"
    }
}

private fun VeredictoComercial?.descripcionUsuario(motivoEvidencia: String?): String {
    return when (this) {
        VeredictoComercial.SALUDABLE -> "El margen sigue por encima del rango de riesgo y las senales de mercado no presionan la ficha."
        VeredictoComercial.PRECAUCION -> "El producto todavia puede ser viable, pero conviene revisar precio, stock y nueva compra."
        VeredictoComercial.ALERTA_TEMPRANA_QUIEBRE -> "El margen esta cerca del punto de equilibrio. Una baja de precio o alza del dolar puede dejarlo sin ganancia."
        VeredictoComercial.LIQUIDACION -> "La lectura sugiere priorizar salida de stock antes de seguir comprando este producto."
        VeredictoComercial.INCONCLUSO -> motivoEvidencia ?: "No hay datos de mercado suficientes para recomendar una accion."
        null -> "Solicita una lectura para estimar margen, competencia y deterioro del mercado."
    }
}

private fun EvaluacionComercial.descripcionPrecioSugerido(): String {
    val margen = margenObjetivoPct?.let { "${"%.0f".format(it)}%" } ?: "objetivo"
    val brecha = brechaPrecioSugeridoMercadoPct ?: return "Para lograr $margen de margen, usa el precio sugerido como referencia antes de reponer stock."

    return when (estadoBrechaPrecioSugerido()) {
        EstadoBrechaPrecioSugerido.SOBRE_OBJETIVO -> {
            "El mercado esta ${brecha.formatearAbs()} por encima del precio sugerido. El margen objetivo parece viable."
        }
        EstadoBrechaPrecioSugerido.BAJO_OBJETIVO_CON_MARGEN -> {
            val comparacion = margenNetoPct?.let { " (${it.formatearPct()} vs $margen)" }.orEmpty()
            "El mercado esta ${brecha.formatearAbs()} por debajo del precio sugerido. Queda bajo el objetivo$comparacion, pero aun deja margen."
        }
        EstadoBrechaPrecioSugerido.SIN_MARGEN -> {
            "El mercado esta ${brecha.formatearAbs()} por debajo del precio sugerido. El precio actual queda en punto de equilibrio o por debajo."
        }
        null -> {
            "Para lograr $margen de margen, usa el precio sugerido como referencia antes de reponer stock."
        }
    }
}

private fun EvaluacionComercial.gananciaEstimadaLotePen(producto: ProductoImportado?): Double? {
    val precio = precioPromedioRealPen ?: return null
    val costo = costoTotalPen ?: return null
    val cantidad = producto?.cantidadDisponible?.takeIf { it > 0 } ?: return null
    return (precio - costo) * cantidad
}

@Composable
private fun VeredictoComercial?.colorContenedor() = when (this) {
    VeredictoComercial.SALUDABLE -> SuccessContainer
    VeredictoComercial.PRECAUCION -> SignalGold.copy(alpha = 0.18f)
    VeredictoComercial.ALERTA_TEMPRANA_QUIEBRE -> ErrorSoft
    VeredictoComercial.LIQUIDACION -> ErrorSoft
    VeredictoComercial.INCONCLUSO -> MaterialTheme.colorScheme.surfaceVariant
    null -> MaterialTheme.colorScheme.surfaceVariant
}

private fun java.time.Instant.aTextoRelativo(): String {
    val segundos = ((System.currentTimeMillis() - toEpochMilli()) / 1000).coerceAtLeast(0)
    return when {
        segundos < 60 -> "hace menos de un minuto"
        segundos < 3_600 -> "hace ${segundos / 60} min"
        segundos < 86_400 -> "hace ${segundos / 3_600} h"
        else -> "hace ${segundos / 86_400} dias"
    }
}

private fun Double.formatearAbs(): String = "${"%.1f".format(absoluteValue)}%"

private fun Double.formatearPct(): String = "${"%.1f".format(this)}%"
