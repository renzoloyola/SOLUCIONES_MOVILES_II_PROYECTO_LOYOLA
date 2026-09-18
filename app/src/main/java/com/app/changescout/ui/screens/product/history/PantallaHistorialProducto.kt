package com.app.changescout.ui.screens.product.history

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.changescout.domain.model.EstadoEvaluacion
import com.app.changescout.domain.model.VeredictoComercial
import com.app.changescout.ui.screens.components.ChipOperativo
import com.app.changescout.ui.screens.components.EncabezadoSeccion
import com.app.changescout.ui.screens.components.FondoOperativo
import com.app.changescout.ui.screens.components.MetricaResumida
import com.app.changescout.ui.screens.components.TarjetaOperativa
import com.app.changescout.ui.theme.ErrorSoft
import com.app.changescout.ui.theme.SignalGold
import com.app.changescout.ui.theme.SignalMint
import com.app.changescout.ui.theme.SignalRed
import com.app.changescout.ui.theme.SuccessContainer
import com.app.changescout.ui.viewmodel.EstadoUiHistorialProducto
import com.app.changescout.ui.viewmodel.PuntoHistorialProductoUi
import com.app.changescout.ui.viewmodel.ViewModelHistorialProducto
import kotlin.math.absoluteValue

@Composable
fun PantallaHistorialProducto(
    onNavegarAtras: () -> Unit,
    viewModel: ViewModelHistorialProducto = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    FondoOperativo {
        ContenidoHistorialProducto(
            state = state,
            onNavegarAtras = onNavegarAtras
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContenidoHistorialProducto(
    state: EstadoUiHistorialProducto,
    onNavegarAtras: () -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                title = {
                    Text("Historial")
                },
                navigationIcon = {
                    IconButton(onClick = onNavegarAtras) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.estaCargando) {
                item {
                    TarjetaOperativa {
                        EncabezadoSeccion(
                            icono = Icons.Outlined.History,
                            titulo = "Cargando historial"
                        )
                    }
                }
                return@LazyColumn
            }

            state.mensajeError?.let { mensaje ->
                item {
                    TarjetaOperativa(acento = MaterialTheme.colorScheme.error) {
                        Text(
                            text = mensaje,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            item {
                ResumenHistorial(state)
            }

            if (state.puntos.size < MINIMO_LECTURAS_TENDENCIA) {
                item {
                    TarjetaOperativa {
                        Text(
                            text = "Necesitas al menos dos lecturas para ver tendencia.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                item {
                    GraficoLineasHistorial(
                        titulo = "Precio y costo",
                        etiquetasX = state.puntos.map { it.etiquetaFecha },
                        series = listOf(
                            SerieHistorial(
                                etiqueta = "Precio ref.",
                                valores = state.puntos.map { it.precioRefPen },
                                color = MaterialTheme.colorScheme.primary
                            ),
                            SerieHistorial(
                                etiqueta = "Costo destino",
                                valores = state.puntos.map { it.costoDestinoPen },
                                color = SignalGold
                            )
                        )
                    )
                }
                item {
                    GraficoLineasHistorial(
                        titulo = "Margen neto",
                        etiquetasX = state.puntos.map { it.etiquetaFecha },
                        series = listOf(
                            SerieHistorial(
                                etiqueta = "Margen",
                                valores = state.puntos.map { it.margenNetoPct },
                                color = SignalMint
                            )
                        )
                    )
                }
                item {
                    GraficoLineasHistorial(
                        titulo = "Competidores validos",
                        etiquetasX = state.puntos.map { it.etiquetaFecha },
                        series = listOf(
                            SerieHistorial(
                                etiqueta = "Validas",
                                valores = state.puntos.map { it.competidoresValidos.toDouble() },
                                color = SignalRed
                            )
                        )
                    )
                }
                state.ultimaLectura?.let { ultima ->
                    item {
                        TendenciasUltimaLectura(ultima)
                    }
                }
            }

            if (state.puntos.isNotEmpty()) {
                item {
                    Text(
                        text = "Lecturas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                items(state.puntos.asReversed()) { punto ->
                    FilaLecturaHistorial(punto)
                }
            }
        }
    }
}

@Composable
private fun ResumenHistorial(state: EstadoUiHistorialProducto) {
    TarjetaOperativa {
        EncabezadoSeccion(
            icono = Icons.Outlined.History,
            titulo = state.productoNombre.ifBlank { "Producto" }
        )
        val ultima = state.ultimaLectura
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricaResumida(
                titulo = "Lecturas",
                valor = state.puntos.size.toString(),
                icono = Icons.Outlined.History,
                modifier = Modifier.weight(1f)
            )
            MetricaResumida(
                titulo = "Ultima",
                valor = ultima?.etiquetaFecha ?: "--",
                icono = Icons.Outlined.QueryStats,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricaResumida(
                titulo = "Precio ref.",
                valor = ultima?.precioRefPen.aSoles(),
                icono = Icons.Outlined.AttachMoney,
                modifier = Modifier.weight(1f)
            )
            MetricaResumida(
                titulo = "Margen",
                valor = ultima?.margenNetoPct.aPct(),
                icono = Icons.Outlined.Sell,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun GraficoLineasHistorial(
    titulo: String,
    etiquetasX: List<String>,
    series: List<SerieHistorial>
) {
    TarjetaOperativa {
        EncabezadoSeccion(
            icono = Icons.Outlined.QueryStats,
            titulo = titulo
        )

        val valoresValidos = series.flatMap { serie ->
            serie.valores.mapNotNull { valor -> valor?.takeIf { it.isFinite() } }
        }
        if (valoresValidos.size < 2) {
            Text(
                text = "Aun no hay datos suficientes para graficar.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return@TarjetaOperativa
        }

        val grillaColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            val ejeIzquierdo = 44.dp.toPx()
            val ejeInferior = 28.dp.toPx()
            val ejeSuperior = 8.dp.toPx()
            val ejeDerecho = 8.dp.toPx()
            val anchoGrafico = (size.width - ejeIzquierdo - ejeDerecho).coerceAtLeast(1f)
            val altoGrafico = (size.height - ejeSuperior - ejeInferior).coerceAtLeast(1f)
            val textoColor = android.graphics.Color.argb(180, 234, 241, 245)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = textoColor
                textSize = 10.dp.toPx()
                textAlign = Paint.Align.RIGHT
            }

            repeat(3) { index ->
                val y = ejeSuperior + altoGrafico * (index + 1) / 4f
                drawLine(
                    color = grillaColor,
                    start = Offset(ejeIzquierdo, y),
                    end = Offset(ejeIzquierdo + anchoGrafico, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            val minimo = valoresValidos.minOrNull() ?: return@Canvas
            val maximo = valoresValidos.maxOrNull() ?: return@Canvas
            val rango = (maximo - minimo).takeIf { it > 0.0 } ?: 1.0
            val ultimoIndice = (series.maxOfOrNull { it.valores.size } ?: 1).minus(1).coerceAtLeast(1)
            val ejeColor = grillaColor.copy(alpha = 0.8f)

            drawLine(
                color = ejeColor,
                start = Offset(ejeIzquierdo, ejeSuperior),
                end = Offset(ejeIzquierdo, ejeSuperior + altoGrafico),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = ejeColor,
                start = Offset(ejeIzquierdo, ejeSuperior + altoGrafico),
                end = Offset(ejeIzquierdo + anchoGrafico, ejeSuperior + altoGrafico),
                strokeWidth = 1.dp.toPx()
            )

            repeat(5) { marca ->
                val proporcion = marca / 4f
                val valor = maximo - (rango * proporcion)
                val y = ejeSuperior + altoGrafico * proporcion
                drawContext.canvas.nativeCanvas.drawText(
                    valor.formatoEje(),
                    ejeIzquierdo - 6.dp.toPx(),
                    y + 3.dp.toPx(),
                    paint
                )
            }

            paint.textAlign = Paint.Align.CENTER
            val marcasX = listOf(0, ultimoIndice / 2, ultimoIndice).distinct()
            marcasX.forEach { index ->
                val etiqueta = etiquetasX.getOrNull(index) ?: return@forEach
                val x = ejeIzquierdo + anchoGrafico * index.toFloat() / ultimoIndice.toFloat()
                drawContext.canvas.nativeCanvas.drawText(
                    etiqueta,
                    x,
                    size.height - 5.dp.toPx(),
                    paint
                )
            }

            series.forEach { serie ->
                val puntos = serie.valores.mapIndexedNotNull { index, valor ->
                    valor?.takeIf { it.isFinite() }?.let { index to it }
                }
                if (puntos.size < 2) return@forEach

                val path = Path()
                puntos.forEachIndexed { posicion, (index, valor) ->
                    val x = ejeIzquierdo + anchoGrafico * index.toFloat() / ultimoIndice.toFloat()
                    val y = ejeSuperior + altoGrafico - (((valor - minimo) / rango).toFloat() * altoGrafico)
                    if (posicion == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }

                drawPath(
                    path = path,
                    color = serie.color,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
                puntos.forEach { (index, valor) ->
                    val x = ejeIzquierdo + anchoGrafico * index.toFloat() / ultimoIndice.toFloat()
                    val y = ejeSuperior + altoGrafico - (((valor - minimo) / rango).toFloat() * altoGrafico)
                    drawCircle(color = serie.color, radius = 3.5.dp.toPx(), center = Offset(x, y))
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            series.forEach { serie ->
                ChipOperativo(
                    texto = serie.etiqueta,
                    icono = Icons.Outlined.QueryStats,
                    contenedor = serie.color.copy(alpha = 0.16f)
                )
            }
        }
    }
}

@Composable
private fun TendenciasUltimaLectura(ultima: PuntoHistorialProductoUi) {
    val tendencias = listOfNotNull(
        ultima.erosionPrecioLocalPct?.let { "Precio local ${it.aPctConSigno()}" },
        ultima.variacionCompetidoresPct?.let { "Competidores ${it.aPctConSigno()}" },
        ultima.presionCambiariaPct?.let { "Dolar ${it.aPctConSigno()}" }
    )
    if (tendencias.isEmpty()) return

    TarjetaOperativa {
        EncabezadoSeccion(
            icono = Icons.Outlined.QueryStats,
            titulo = "Tendencia reciente"
        )
        tendencias.forEach { tendencia ->
            Text(
                text = tendencia,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FilaLecturaHistorial(punto: PuntoHistorialProductoUi) {
    TarjetaOperativa(acento = punto.veredicto.colorAcento()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = punto.etiquetaFecha,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = punto.veredicto.etiqueta(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            ChipOperativo(
                texto = punto.estadoEvaluacion.etiqueta(),
                icono = Icons.Outlined.History,
                contenedor = punto.estadoEvaluacion.colorContenedor()
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricaResumida(
                titulo = "Precio ref.",
                valor = punto.precioRefPen.aSoles(),
                icono = Icons.Outlined.AttachMoney,
                modifier = Modifier.weight(1f)
            )
            MetricaResumida(
                titulo = "Costo destino",
                valor = punto.costoDestinoPen.aSoles(),
                icono = Icons.Outlined.Inventory2,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricaResumida(
                titulo = "Margen",
                valor = punto.margenNetoPct.aPct(),
                icono = Icons.Outlined.Sell,
                modifier = Modifier.weight(1f)
            )
            MetricaResumida(
                titulo = "Validas",
                valor = punto.competidoresValidos.toString(),
                icono = Icons.Outlined.Shield,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private data class SerieHistorial(
    val etiqueta: String,
    val valores: List<Double?>,
    val color: Color
)

private const val MINIMO_LECTURAS_TENDENCIA = 2

private fun VeredictoComercial?.etiqueta(): String {
    return when (this) {
        VeredictoComercial.SALUDABLE -> "Saludable"
        VeredictoComercial.PRECAUCION -> "Precaucion"
        VeredictoComercial.ALERTA_TEMPRANA_QUIEBRE -> "Margen en riesgo"
        VeredictoComercial.LIQUIDACION -> "Liquidar stock"
        VeredictoComercial.INCONCLUSO -> "Sin datos"
        null -> "Sin lectura"
    }
}

@Composable
private fun VeredictoComercial?.colorAcento(): Color {
    return when (this) {
        VeredictoComercial.SALUDABLE -> SignalMint
        VeredictoComercial.PRECAUCION -> SignalGold
        VeredictoComercial.ALERTA_TEMPRANA_QUIEBRE -> MaterialTheme.colorScheme.error
        VeredictoComercial.LIQUIDACION -> MaterialTheme.colorScheme.error
        VeredictoComercial.INCONCLUSO -> MaterialTheme.colorScheme.primary
        null -> MaterialTheme.colorScheme.primary
    }
}

private fun EstadoEvaluacion.etiqueta(): String {
    return when (this) {
        EstadoEvaluacion.VIGENTE -> "Vigente"
        EstadoEvaluacion.OBSOLETO -> "Caducada"
        EstadoEvaluacion.INCONCLUSO -> "Sin datos"
        EstadoEvaluacion.FALLIDO -> "Fallida"
    }
}

@Composable
private fun EstadoEvaluacion.colorContenedor(): Color {
    return when (this) {
        EstadoEvaluacion.VIGENTE -> SuccessContainer
        EstadoEvaluacion.OBSOLETO -> SignalRed.copy(alpha = 0.18f)
        EstadoEvaluacion.INCONCLUSO -> MaterialTheme.colorScheme.surfaceVariant
        EstadoEvaluacion.FALLIDO -> ErrorSoft
    }
}

private fun Double?.aSoles(): String = this?.let { "S/ ${"%.2f".format(it)}" } ?: "--"

private fun Double?.aPct(): String = this?.let { "${"%.1f".format(it)}%" } ?: "--"

private fun Double.aPctConSigno(): String {
    val signo = if (this >= 0.0) "+" else "-"
    return "$signo${"%.1f".format(absoluteValue)}%"
}

private fun Double.formatoEje(): String {
    return if (absoluteValue >= 100.0) {
        "%.0f".format(this)
    } else {
        "%.1f".format(this)
    }
}
