package com.app.changescout.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.changescout.data.local.entity.EvaluacionComercialEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EvaluacionComercialDao {
    @Query(
        """
        SELECT *
        FROM evaluaciones_comerciales
        WHERE usuarioId = :usuarioId AND productoId = :productoId
        ORDER BY evaluadoEnEpochMillis DESC, evaluacionId DESC
        LIMIT 1
        """
    )
    fun observarUltimo(usuarioId: String, productoId: Long): Flow<EvaluacionComercialEntity?>

    @Query(
        """
        SELECT *
        FROM evaluaciones_comerciales AS evaluacion
        WHERE evaluacion.usuarioId = :usuarioId
        AND evaluacion.evaluacionId = (
            SELECT candidata.evaluacionId
            FROM evaluaciones_comerciales AS candidata
            WHERE candidata.usuarioId = :usuarioId
            AND candidata.productoId = evaluacion.productoId
            ORDER BY candidata.evaluadoEnEpochMillis DESC, candidata.evaluacionId DESC
            LIMIT 1
        )
        ORDER BY evaluacion.evaluadoEnEpochMillis DESC, evaluacion.evaluacionId DESC
        """
    )
    fun observarUltimosDeTodos(usuarioId: String): Flow<List<EvaluacionComercialEntity>>

    @Query(
        """
        SELECT *
        FROM evaluaciones_comerciales
        WHERE usuarioId = :usuarioId AND productoId = :productoId
        ORDER BY evaluadoEnEpochMillis DESC, evaluacionId DESC
        LIMIT :limite
        """
    )
    fun observarHistorial(
        usuarioId: String,
        productoId: Long,
        limite: Int
    ): Flow<List<EvaluacionComercialEntity>>

    @Query(
        """
        SELECT *
        FROM evaluaciones_comerciales
        WHERE usuarioId = :usuarioId AND productoId = :productoId
        ORDER BY evaluadoEnEpochMillis DESC, evaluacionId DESC
        LIMIT :limite
        """
    )
    suspend fun obtenerHistorial(
        usuarioId: String,
        productoId: Long,
        limite: Int
    ): List<EvaluacionComercialEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(evaluacion: EvaluacionComercialEntity): Long
}
