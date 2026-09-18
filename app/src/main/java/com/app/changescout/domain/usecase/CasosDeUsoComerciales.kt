package com.app.changescout.domain.usecase

import com.app.changescout.domain.model.ProductoImportado
import com.app.changescout.domain.model.ProductoRadarItem
import com.app.changescout.domain.model.EvaluacionComercial
import com.app.changescout.domain.repository.RepositorioEvaluacionComercial
import com.app.changescout.domain.repository.RepositorioProductoImportado
import com.app.changescout.domain.rules.PoliticaObsolescenciaEvaluacion
import java.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObservarRadarProductosUseCase @Inject constructor(
    private val repositorioProducto: RepositorioProductoImportado,
    private val repositorioEvaluacion: RepositorioEvaluacionComercial,
    private val politicaObsolescencia: PoliticaObsolescenciaEvaluacion,
    private val clock: Clock
) {
    operator fun invoke(): Flow<List<ProductoRadarItem>> {
        return combine(
            repositorioProducto.observarTodos(),
            repositorioEvaluacion.observarUltimosDeTodos()
        ) { productos, evaluaciones ->
            val now = clock.instant()
            val evaluacionesPorProducto = evaluaciones.associateBy { it.productoId }
            productos.map { producto ->
                ProductoRadarItem(
                    producto = producto,
                    ultimaEvaluacion = evaluacionesPorProducto[producto.id]?.conEstadoActual(
                        politicaObsolescencia = politicaObsolescencia,
                        now = now
                    )
                )
            }
        }
    }
}

class ObservarDetalleProductoUseCase @Inject constructor(
    private val repositorioProducto: RepositorioProductoImportado
) {
    operator fun invoke(productoId: Long): Flow<ProductoImportado?> {
        return repositorioProducto.observarPorId(productoId)
    }
}

class ObservarUltimaEvaluacionUseCase @Inject constructor(
    private val repositorioEvaluacion: RepositorioEvaluacionComercial,
    private val politicaObsolescencia: PoliticaObsolescenciaEvaluacion,
    private val clock: Clock
) {
    operator fun invoke(productoId: Long): Flow<EvaluacionComercial?> {
        return repositorioEvaluacion.observarUltimo(productoId)
            .map { evaluacion ->
                evaluacion?.conEstadoActual(
                    politicaObsolescencia = politicaObsolescencia,
                    now = clock.instant()
                )
            }
    }
}

class ObservarHistorialEvaluacionesProductoUseCase @Inject constructor(
    private val repositorioEvaluacion: RepositorioEvaluacionComercial
) {
    operator fun invoke(productoId: Long, limite: Int = LIMITE_HISTORIAL_PRODUCTO): Flow<List<EvaluacionComercial>> {
        require(productoId > 0L) { "Producto no valido para consultar historial." }
        return repositorioEvaluacion.observarHistorial(productoId, limite)
    }

    private companion object {
        const val LIMITE_HISTORIAL_PRODUCTO = 30
    }
}

class GuardarProductoImportadoUseCase @Inject constructor(
    private val repositorioProducto: RepositorioProductoImportado
) {
    suspend operator fun invoke(producto: ProductoImportado): Long {
        validar(producto)
        return repositorioProducto.upsert(producto)
    }

    private fun validar(producto: ProductoImportado) {
        require(producto.nombre.isNotBlank()) { "Ingresa un nombre para el producto." }
        require(producto.queryCompetencia.isNotBlank()) {
            "Ingresa un query de competencia para el radar."
        }
        require(producto.cantidadDisponible > 0) { "La cantidad disponible debe ser mayor a cero." }
        require(producto.margenObjetivoPct > 0.0 && producto.margenObjetivoPct < 100.0) {
            "El margen objetivo debe estar entre 0 y 100."
        }
        require(producto.componentesCosto.tieneValoresValidos()) {
            "Revisa los componentes de costo: FOB debe ser mayor a cero y los demas cargos no pueden ser negativos."
        }
    }
}

class EliminarProductoImportadoUseCase @Inject constructor(
    private val repositorioProducto: RepositorioProductoImportado
) {
    suspend operator fun invoke(productoId: Long) {
        require(productoId > 0L) { "Producto no valido para eliminar." }
        repositorioProducto.eliminar(productoId)
    }
}

private fun EvaluacionComercial.conEstadoActual(
    politicaObsolescencia: PoliticaObsolescenciaEvaluacion,
    now: java.time.Instant
): EvaluacionComercial {
    return copy(
        estadoEvaluacion = politicaObsolescencia.resolverEstado(this, now)
    )
}
