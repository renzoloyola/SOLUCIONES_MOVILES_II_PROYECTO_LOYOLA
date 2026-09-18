package com.app.changescout.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.app.changescout.data.local.entity.ProductoImportadoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoImportadoDao {
    @Query("SELECT * FROM productos_importados WHERE usuarioId = :usuarioId ORDER BY nombre ASC")
    fun observarTodos(usuarioId: String): Flow<List<ProductoImportadoEntity>>

    @Query("SELECT * FROM productos_importados WHERE usuarioId = :usuarioId AND id = :productoId LIMIT 1")
    fun observarPorId(usuarioId: String, productoId: Long): Flow<ProductoImportadoEntity?>

    @Query("SELECT * FROM productos_importados WHERE usuarioId = :usuarioId AND id = :productoId LIMIT 1")
    suspend fun obtenerPorId(usuarioId: String, productoId: Long): ProductoImportadoEntity?

    @Transaction
    suspend fun upsert(producto: ProductoImportadoEntity): Long {
        return if (producto.id == 0L) {
            insertar(producto)
        } else {
            val actualizados = actualizar(
                id = producto.id,
                usuarioId = producto.usuarioId,
                nombre = producto.nombre,
                queryCompetencia = producto.queryCompetencia,
                precioFobUsd = producto.precioFobUsd,
                fleteUsd = producto.fleteUsd,
                seguroUsd = producto.seguroUsd,
                arancelesUsd = producto.arancelesUsd,
                otrosCargosUsd = producto.otrosCargosUsd,
                cantidadDisponible = producto.cantidadDisponible,
                margenObjetivoPct = producto.margenObjetivoPct,
                notas = producto.notas
            )
            check(actualizados == 1) { "No se pudo actualizar el producto de esta sesion." }
            producto.id
        }
    }

    @Insert
    suspend fun insertar(producto: ProductoImportadoEntity): Long

    @Query(
        """
        UPDATE productos_importados
        SET nombre = :nombre,
            queryCompetencia = :queryCompetencia,
            precioFobUsd = :precioFobUsd,
            fleteUsd = :fleteUsd,
            seguroUsd = :seguroUsd,
            arancelesUsd = :arancelesUsd,
            otrosCargosUsd = :otrosCargosUsd,
            cantidadDisponible = :cantidadDisponible,
            margenObjetivoPct = :margenObjetivoPct,
            notas = :notas
        WHERE id = :id AND usuarioId = :usuarioId
        """
    )
    suspend fun actualizar(
        id: Long,
        usuarioId: String,
        nombre: String,
        queryCompetencia: String,
        precioFobUsd: Double,
        fleteUsd: Double,
        seguroUsd: Double,
        arancelesUsd: Double,
        otrosCargosUsd: Double,
        cantidadDisponible: Int,
        margenObjetivoPct: Double,
        notas: String?
    ): Int

    @Query("DELETE FROM productos_importados WHERE usuarioId = :usuarioId AND id = :productoId")
    suspend fun eliminar(usuarioId: String, productoId: Long)
}
