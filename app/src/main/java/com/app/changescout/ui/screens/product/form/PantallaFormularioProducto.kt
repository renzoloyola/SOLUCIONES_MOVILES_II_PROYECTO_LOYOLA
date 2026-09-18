package com.app.changescout.ui.screens.product.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.changescout.ui.screens.components.BotonPrimario
import com.app.changescout.ui.screens.components.EncabezadoSeccion
import com.app.changescout.ui.screens.components.FondoOperativo
import com.app.changescout.ui.screens.components.TarjetaOperativa
import com.app.changescout.ui.viewmodel.EfectoFormularioProducto
import com.app.changescout.ui.viewmodel.EstadoUiFormularioProducto
import com.app.changescout.ui.viewmodel.EventoFormularioProducto
import com.app.changescout.ui.viewmodel.ViewModelFormularioProducto

@Composable
fun PantallaFormularioProducto(
    onNavegarAtras: () -> Unit,
    formularioViewModel: ViewModelFormularioProducto = hiltViewModel()
) {
    val state by formularioViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(formularioViewModel) {
        formularioViewModel.uiEffect.collect { effect ->
            when (effect) {
                EfectoFormularioProducto.NavegarAtrasDesdeFormulario -> onNavegarAtras()
                is EfectoFormularioProducto.MostrarMensajeFormulario -> {
                    snackbarHostState.showSnackbar(effect.mensaje)
                }
            }
        }
    }

    FondoOperativo {
        ContenidoFormularioProducto(
            state = state,
            snackbarHostState = snackbarHostState,
            onEvent = formularioViewModel::onEvent
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContenidoFormularioProducto(
    state: EstadoUiFormularioProducto,
    snackbarHostState: SnackbarHostState,
    onEvent: (EventoFormularioProducto) -> Unit
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
                    Text(if (state.esEdicion) "Editar producto" else "Nuevo producto")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        onEvent(EventoFormularioProducto.RegresarDesdeFormularioSolicitado)
                    }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Volver")
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
            if (state.estaCargandoEdicion) {
                Column(modifier = contentModifier) {
                    TarjetaOperativa {
                        EncabezadoSeccion(
                            icono = Icons.Outlined.Description,
                            titulo = "Cargando ficha"
                        )
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else if (horizontal) {
                Row(
                    modifier = contentModifier,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FormularioDatosCostos(
                        state = state,
                        onEvent = onEvent,
                        modifier = Modifier.weight(1f)
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FormularioBusqueda(state, onEvent)
                        MensajeValidacionFormulario(state.mensajeValidacion)
                        BotonGuardarFormulario(state, onEvent)
                    }
                }
            } else {
                Column(
                    modifier = contentModifier,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FormularioDatosCostos(state, onEvent)
                    FormularioBusqueda(state, onEvent)
                    MensajeValidacionFormulario(state.mensajeValidacion)
                    BotonGuardarFormulario(state, onEvent)
                }
            }
        }
    }
}

@Composable
private fun FormularioDatosCostos(
    state: EstadoUiFormularioProducto,
    onEvent: (EventoFormularioProducto) -> Unit,
    modifier: Modifier = Modifier
) {
    TarjetaOperativa(modifier = modifier) {
        EncabezadoSeccion(
            icono = Icons.Outlined.Description,
            titulo = "Datos y costos"
        )
        CampoOperativo(
            value = state.nombre,
            onValueChange = { onEvent(EventoFormularioProducto.NombreCambiado(it)) },
            label = "Nombre del producto",
            leadingIcon = { Icon(Icons.Outlined.Inventory2, contentDescription = null) }
        )
        CampoOperativo(
            value = state.precioFobUsd,
            onValueChange = { onEvent(EventoFormularioProducto.PrecioFobUsdCambiado(it)) },
            label = "Precio del producto en origen (USD)",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            leadingIcon = { Icon(Icons.Outlined.AttachMoney, contentDescription = null) }
        )
        CampoOperativo(
            value = state.fleteUsd,
            onValueChange = { onEvent(EventoFormularioProducto.FleteUsdCambiado(it)) },
            label = "Envio internacional (USD, opcional)",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            leadingIcon = { Icon(Icons.Outlined.AttachMoney, contentDescription = null) }
        )
        CampoOperativo(
            value = state.seguroUsd,
            onValueChange = { onEvent(EventoFormularioProducto.SeguroUsdCambiado(it)) },
            label = "Seguro de carga (USD, opcional)",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            leadingIcon = { Icon(Icons.Outlined.AttachMoney, contentDescription = null) }
        )
        CampoOperativo(
            value = state.arancelesUsd,
            onValueChange = { onEvent(EventoFormularioProducto.ArancelesUsdCambiado(it)) },
            label = "Impuestos de importacion (USD, opcional)",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            leadingIcon = { Icon(Icons.Outlined.AttachMoney, contentDescription = null) }
        )
        CampoOperativo(
            value = state.otrosCargosUsd,
            onValueChange = { onEvent(EventoFormularioProducto.OtrosCargosUsdCambiado(it)) },
            label = "Gastos adicionales (USD, opcional)",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            leadingIcon = { Icon(Icons.Outlined.AttachMoney, contentDescription = null) }
        )
        CampoOperativo(
            value = state.cantidadDisponible,
            onValueChange = { onEvent(EventoFormularioProducto.CantidadDisponibleCambiada(it)) },
            label = "Unidades disponibles",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            leadingIcon = { Icon(Icons.Outlined.Numbers, contentDescription = null) }
        )
        CampoOperativo(
            value = state.margenObjetivoPct,
            onValueChange = { onEvent(EventoFormularioProducto.MargenObjetivoCambiado(it)) },
            label = "Margen de ganancia objetivo (%)",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            leadingIcon = { Icon(Icons.Outlined.Percent, contentDescription = null) }
        )
    }
}

@Composable
private fun FormularioBusqueda(
    state: EstadoUiFormularioProducto,
    onEvent: (EventoFormularioProducto) -> Unit
) {
    TarjetaOperativa {
        EncabezadoSeccion(
            icono = Icons.Outlined.Search,
            titulo = "Busqueda comparable"
        )
        CampoOperativo(
            value = state.queryCompetencia,
            onValueChange = { onEvent(EventoFormularioProducto.QueryCompetenciaCambiado(it)) },
            label = "Producto a buscar en el mercado",
            minLines = 3,
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) }
        )
    }
}

@Composable
private fun MensajeValidacionFormulario(mensaje: String?) {
    mensaje?.let {
        TarjetaOperativa {
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
private fun BotonGuardarFormulario(
    state: EstadoUiFormularioProducto,
    onEvent: (EventoFormularioProducto) -> Unit
) {
    BotonPrimario(
        texto = when {
            state.estaGuardando -> "Guardando ficha..."
            state.esEdicion -> "Guardar cambios"
            else -> "Guardar ficha"
        },
        icono = Icons.Outlined.Save,
        onClick = {
            onEvent(EventoFormularioProducto.GuardarProductoSolicitado)
        },
        enabled = state.puedeEnviar,
        modifier = Modifier.fillMaxWidth(),
        cargando = state.estaGuardando
    )
}

@Composable
private fun CampoOperativo(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    val campoColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        cursorColor = MaterialTheme.colorScheme.primary,
        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f)
    )
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = leadingIcon,
        minLines = minLines,
        singleLine = minLines == 1,
        keyboardOptions = keyboardOptions,
        shape = RoundedCornerShape(8.dp),
        colors = campoColors
    )
}
