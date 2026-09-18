package com.app.changescout.domain.repository

import com.app.changescout.domain.model.CotizacionTipoCambio
import com.app.changescout.domain.model.ProductoImportado
import com.app.changescout.domain.model.PublicacionMercado
import com.app.changescout.domain.model.ResultadoFiltroNlp
import com.app.changescout.domain.model.ResultadoOperacion
import com.app.changescout.domain.model.EvaluacionComercial
import kotlinx.coroutines.flow.Flow

interface RepositorioProductoImportado {
    fun observarTodos(): Flow<List<ProductoImportado>>
    fun observarPorId(productoId: Long): Flow<ProductoImportado?>
    suspend fun obtenerPorId(productoId: Long): ProductoImportado?
    suspend fun upsert(producto: ProductoImportado): Long
    suspend fun eliminar(productoId: Long)
}

interface RepositorioEvaluacionComercial {
    fun observarUltimo(productoId: Long): Flow<EvaluacionComercial?>
    fun observarUltimosDeTodos(): Flow<List<EvaluacionComercial>>
    fun observarHistorial(productoId: Long, limite: Int): Flow<List<EvaluacionComercial>>
    suspend fun obtenerHistorial(productoId: Long, limite: Int): List<EvaluacionComercial>
    suspend fun guardarEvaluacion(evaluacion: EvaluacionComercial)
}

interface ProveedorTipoCambio {
    val nombreProveedor: String
    suspend fun obtenerTasaVentaUsdPen(): ResultadoOperacion<CotizacionTipoCambio>
}

interface ProveedorMarketplace {
    val nombreProveedor: String
    suspend fun buscar(query: String, limit: Int): ResultadoOperacion<List<PublicacionMercado>>
}

interface ProveedorFiltroNlp {
    val nombreProveedor: String
    suspend fun filtrar(
        publicaciones: List<PublicacionMercado>,
        producto: ProductoImportado
    ): ResultadoOperacion<ResultadoFiltroNlp>
}
