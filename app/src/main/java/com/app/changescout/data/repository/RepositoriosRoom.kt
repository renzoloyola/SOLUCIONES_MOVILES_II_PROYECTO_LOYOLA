package com.app.changescout.data.repository

import com.app.changescout.data.auth.AlmacenSesion
import com.app.changescout.data.local.dao.ProductoImportadoDao
import com.app.changescout.data.local.dao.EvaluacionComercialDao
import com.app.changescout.data.local.entity.ProductoImportadoEntity
import com.app.changescout.data.local.entity.EvaluacionComercialEntity
import com.app.changescout.domain.model.ProductoImportado
import com.app.changescout.domain.model.EvaluacionComercial
import com.app.changescout.domain.repository.RepositorioEvaluacionComercial
import com.app.changescout.domain.repository.RepositorioProductoImportado
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RepositorioProductoImportadoRoom(
    private val productoDao: ProductoImportadoDao,
    private val almacenSesion: AlmacenSesion
) : RepositorioProductoImportado {
    override fun observarTodos(): Flow<List<ProductoImportado>> {
        val usuarioId = usuarioIdActual()
        return productoDao.observarTodos(usuarioId)
            .map { productos -> productos.map { it.toDomain() } }
    }

    override fun observarPorId(productoId: Long): Flow<ProductoImportado?> {
        val usuarioId = usuarioIdActual()
        return productoDao.observarPorId(usuarioId, productoId)
            .map { producto -> producto?.toDomain() }
    }

    override suspend fun obtenerPorId(productoId: Long): ProductoImportado? {
        val usuarioId = usuarioIdActual()
        return productoDao.obtenerPorId(usuarioId, productoId)?.toDomain()
    }

    override suspend fun upsert(producto: ProductoImportado): Long {
        val usuarioId = usuarioIdActual()
        return productoDao.upsert(
            ProductoImportadoEntity.fromDomain(producto, usuarioId)
        )
    }

    override suspend fun eliminar(productoId: Long) {
        val usuarioId = usuarioIdActual()
        productoDao.eliminar(usuarioId, productoId)
    }

    private fun usuarioIdActual(): String = requireNotNull(almacenSesion.usuarioIdActual()) {
        "Sesion no disponible."
    }
}

class RepositorioEvaluacionComercialRoom(
    private val evaluacionDao: EvaluacionComercialDao,
    private val almacenSesion: AlmacenSesion
) : RepositorioEvaluacionComercial {
    override fun observarUltimo(productoId: Long): Flow<EvaluacionComercial?> {
        val usuarioId = usuarioIdActual()
        return evaluacionDao.observarUltimo(usuarioId, productoId)
            .map { evaluacion -> evaluacion?.toDomain() }
    }

    override fun observarUltimosDeTodos(): Flow<List<EvaluacionComercial>> {
        val usuarioId = usuarioIdActual()
        return evaluacionDao.observarUltimosDeTodos(usuarioId)
            .map { evaluaciones -> evaluaciones.map { it.toDomain() } }
    }

    override fun observarHistorial(productoId: Long, limite: Int): Flow<List<EvaluacionComercial>> {
        val usuarioId = usuarioIdActual()
        return evaluacionDao.observarHistorial(usuarioId, productoId, limite)
            .map { evaluaciones -> evaluaciones.map { it.toDomain() } }
    }

    override suspend fun obtenerHistorial(
        productoId: Long,
        limite: Int
    ): List<EvaluacionComercial> {
        val usuarioId = usuarioIdActual()
        return evaluacionDao.obtenerHistorial(usuarioId, productoId, limite)
            .map { it.toDomain() }
    }

    override suspend fun guardarEvaluacion(evaluacion: EvaluacionComercial) {
        val usuarioId = usuarioIdActual()
        evaluacionDao.insertar(
            EvaluacionComercialEntity.fromDomain(evaluacion, usuarioId)
        )
    }

    private fun usuarioIdActual(): String = requireNotNull(almacenSesion.usuarioIdActual()) {
        "Sesion no disponible."
    }
}
