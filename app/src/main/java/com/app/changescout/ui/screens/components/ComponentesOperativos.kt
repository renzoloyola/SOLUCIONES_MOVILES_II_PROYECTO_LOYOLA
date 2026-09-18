package com.app.changescout.ui.screens.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.ui.unit.dp
import com.app.changescout.ui.theme.BullionGold
import com.app.changescout.ui.theme.OutlineSubtle

private val TarjetaShape = RoundedCornerShape(8.dp)
private val ChipShape = RoundedCornerShape(8.dp)
private val MetricaShape = RoundedCornerShape(8.dp)
private val BotonPrimarioShape = RoundedCornerShape(12.dp)
private val IconBadgeShape = RoundedCornerShape(8.dp)
private val ChipOutlineBorder = OutlineSubtle.copy(alpha = 0.55f)

@Composable
fun FondoOperativo(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        content = content
    )
}

@Composable
fun LogoChangeScout(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Change ",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Sc",
            style = MaterialTheme.typography.headlineSmall,
            color = BullionGold,
            fontWeight = FontWeight.ExtraBold
        )
        Icon(
            imageVector = Icons.Outlined.Visibility,
            contentDescription = null,
            tint = BullionGold,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = "ut",
            style = MaterialTheme.typography.headlineSmall,
            color = BullionGold,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun TarjetaOperativa(
    modifier: Modifier = Modifier,
    acento: Color = BullionGold,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = TarjetaShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, acento.copy(alpha = 0.32f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = contenido
            )
        }
    }
}
@Composable
fun BotonPrimario(
    texto: String,
    icono: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    cargando: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled && !cargando,
        modifier = modifier.defaultMinSize(minHeight = 52.dp),
        shape = BotonPrimarioShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        ContenidoBoton(
            texto = texto,
            icono = icono,
            cargando = cargando,
            colorCarga = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun BotonSecundario(
    texto: String,
    icono: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    cargando: Boolean = false
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled && !cargando,
        modifier = modifier.defaultMinSize(minHeight = 48.dp),
        shape = BotonPrimarioShape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.42f))
    ) {
        ContenidoBoton(
            texto = texto,
            icono = icono,
            cargando = cargando,
            colorCarga = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ContenidoBoton(
    texto: String,
    icono: ImageVector,
    cargando: Boolean,
    colorCarga: Color
) {
    if (cargando) {
        CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            strokeWidth = 2.dp,
            color = colorCarga
        )
    } else {
        Icon(icono, contentDescription = null, modifier = Modifier.size(18.dp))
    }
    Spacer(modifier = Modifier.width(10.dp))
    Text(
        text = texto,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
fun EncabezadoSeccion(
    icono: ImageVector,
    titulo: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            shape = IconBadgeShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(9.dp)
                    .size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ChipOperativo(
    texto: String,
    icono: ImageVector,
    modifier: Modifier = Modifier,
    contenedor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contenido: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        modifier = modifier,
        shape = ChipShape,
        color = contenedor,
        border = BorderStroke(1.dp, ChipOutlineBorder),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = contenido
            )
            Text(
                text = texto,
                color = contenido,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun MetricaResumida(
    titulo: String,
    valor: String,
    icono: ImageVector,
    modifier: Modifier = Modifier,
    acento: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        modifier = modifier.defaultMinSize(minHeight = 76.dp),
        shape = MetricaShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.64f),
        border = BorderStroke(1.dp, OutlineSubtle),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = MetricaShape,
                color = acento.copy(alpha = 0.12f)
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = acento,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(17.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = valor,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
