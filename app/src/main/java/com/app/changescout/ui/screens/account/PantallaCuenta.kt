package com.app.changescout.ui.screens.account

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.changescout.ui.screens.components.FondoOperativo
import com.app.changescout.ui.theme.OutlineSubtle
import com.app.changescout.ui.theme.SignalMint
import com.app.changescout.ui.theme.SignalRed
import com.app.changescout.ui.viewmodel.EstadoUiCuenta
import com.app.changescout.ui.viewmodel.LecturaCaducadaUi
import com.app.changescout.ui.viewmodel.ViewModelCuenta

private val PanelShape = RoundedCornerShape(8.dp)
private val ActionShape = RoundedCornerShape(12.dp)

@Composable
fun PantallaCuenta(
    onNavegarAtras: () -> Unit,
    onAgregarProducto: () -> Unit,
    onVerCaducadas: () -> Unit,
    onCerrarSesion: () -> Unit,
    viewModel: ViewModelCuenta = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    FondoOperativo {
        ContenidoCuenta(
            state = state,
            onNavegarAtras = onNavegarAtras,
            onAgregarProducto = onAgregarProducto,
            onVerCaducadas = onVerCaducadas,
            onCerrarSesion = onCerrarSesion
        )
    }
}

@Composable
private fun ContenidoCuenta(
    state: EstadoUiCuenta,
    onNavegarAtras: () -> Unit,
    onAgregarProducto: () -> Unit,
    onVerCaducadas: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val horizontal = maxWidth > maxHeight
            val scrollState = rememberScrollState()
            val contentModifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
                .padding(
                    PaddingValues(
                        horizontal = if (horizontal) 12.dp else 20.dp,
                        vertical = if (horizontal) 6.dp else 14.dp
                    )
                )

            if (state.estaCargando) {
                Column(
                    modifier = contentModifier,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BotonVolverCuenta(
                        onClick = onNavegarAtras,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(64.dp))
                    EstadoCargaCuenta()
                }
            } else if (horizontal) {
                val anchoPerfil = if (maxWidth < 720.dp) 240.dp else 300.dp
                Row(
                    modifier = contentModifier,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.width(anchoPerfil),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        BotonVolverCuenta(
                            onClick = onNavegarAtras,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        CabeceraCuenta(state)
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        state.mensajeError?.let { mensaje -> MensajeCuenta(mensaje) }
                        MenuCuenta(
                            state = state,
                            onVerCaducadas = onVerCaducadas,
                            onVolverRadar = onNavegarAtras,
                            onAgregarProducto = onAgregarProducto,
                            onCerrarSesion = onCerrarSesion
                        )
                    }
                }
            } else {
                Column(
                    modifier = contentModifier,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BotonVolverCuenta(
                        onClick = onNavegarAtras,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    CabeceraCuenta(state)
                    Spacer(modifier = Modifier.height(20.dp))
                    state.mensajeError?.let { mensaje ->
                        MensajeCuenta(mensaje)
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                    MenuCuenta(
                        state = state,
                        onVerCaducadas = onVerCaducadas,
                        onVolverRadar = onNavegarAtras,
                        onAgregarProducto = onAgregarProducto,
                        onCerrarSesion = onCerrarSesion
                    )
                }
            }
        }
    }
}

@Composable
private fun BotonVolverCuenta(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
            contentDescription = "Volver",
            tint = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun PantallaLecturasCaducadas(
    onNavegarAtras: () -> Unit,
    onNavegarADetalle: (Long) -> Unit,
    viewModel: ViewModelCuenta = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    FondoOperativo {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                EncabezadoCuentaScreen(
                    titulo = "Lecturas caducadas",
                    onNavegarAtras = onNavegarAtras
                )
            }

            if (state.estaCargando) {
                item { EstadoCargaCuenta() }
            } else if (state.lecturasCaducadas.isEmpty()) {
                item { LecturasAlDia() }
            } else {
                items(
                    items = state.lecturasCaducadas,
                    key = { lectura -> lectura.productoId }
                ) { lectura ->
                    FilaLecturaCaducadaNavegable(
                        lectura = lectura,
                        onClick = { onNavegarADetalle(lectura.productoId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EncabezadoCuentaScreen(
    titulo: String,
    onNavegarAtras: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavegarAtras) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Volver",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun FilaLecturaCaducadaNavegable(
    lectura: LecturaCaducadaUi,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = PanelShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, SignalRed.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconoCaducidad()
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lectura.producto,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${lectura.fecha} - ${lectura.antiguedad}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun LecturasAlDia() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = PanelShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, SignalMint.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Visibility,
                contentDescription = null,
                tint = SignalMint,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Felicidades, tus lecturas estan al dia",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun MenuCuenta(
    state: EstadoUiCuenta,
    onVerCaducadas: () -> Unit,
    onVolverRadar: () -> Unit,
    onAgregarProducto: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = PanelShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, OutlineSubtle)
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            FilaAccion(
                icono = Icons.Outlined.Schedule,
                titulo = "Lecturas caducadas",
                detalle = if (state.desactualizados > 0) {
                    "${state.desactualizados} requieren nueva lectura"
                } else {
                    "Lecturas al dia"
                },
                acento = if (state.desactualizados > 0) SignalRed else SignalMint,
                onClick = onVerCaducadas
            )
            FilaAccion(
                icono = Icons.Outlined.Inventory2,
                titulo = "Volver al radar",
                detalle = "Revisar tus productos",
                acento = MaterialTheme.colorScheme.primary,
                onClick = onVolverRadar
            )
            FilaAccion(
                icono = Icons.Outlined.AddCircleOutline,
                titulo = "Agregar producto",
                detalle = "Registrar una nueva ficha",
                acento = SignalMint,
                onClick = onAgregarProducto
            )
            FilaAccion(
                icono = Icons.AutoMirrored.Outlined.Logout,
                titulo = "Cerrar sesion",
                detalle = "Salir de esta cuenta",
                acento = SignalRed,
                onClick = onCerrarSesion
            )
        }
    }
}

@Composable
private fun EstadoCargaCuenta() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = PanelShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, OutlineSubtle)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Cargando cuenta",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun MensajeCuenta(mensaje: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = PanelShape,
        color = MaterialTheme.colorScheme.error.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f))
    ) {
        Text(
            text = mensaje,
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun CabeceraCuenta(state: EstadoUiCuenta) {
    val email = state.email.ifBlank { "cuenta@changescout.app" }
    val nombre = state.nombreUsuario.ifBlank { email.substringBefore("@").ifBlank { "Usuario" } }

    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    ) {
        Icon(
            imageVector = Icons.Outlined.AccountCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(16.dp)
                .size(42.dp)
        )
    }

    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = nombre,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
    Text(
        text = email,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
    Spacer(modifier = Modifier.height(10.dp))
    EstadoLecturaChip(state.ultimaLectura)
}

@Composable
private fun EstadoLecturaChip(texto: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "Ultima lectura: ${texto.soloTiempo()}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun IconoCaducidad() {
    Box {
        Surface(
            shape = ActionShape,
            color = SignalRed.copy(alpha = 0.13f)
        ) {
            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                tint = SignalRed,
                modifier = Modifier
                    .padding(10.dp)
                    .size(20.dp)
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(8.dp)
                .background(SignalRed, CircleShape)
        )
    }
}

@Composable
private fun FilaAccion(
    icono: ImageVector,
    titulo: String,
    detalle: String,
    acento: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = ActionShape,
                color = acento.copy(alpha = 0.13f)
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = acento,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (acento == SignalRed) SignalRed else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = detalle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private fun String.soloTiempo(): String {
    return substringAfter(", ", this)
}
