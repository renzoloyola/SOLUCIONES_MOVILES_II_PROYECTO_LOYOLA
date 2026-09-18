package com.app.changescout.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.app.changescout.data.local.dao.ProductoImportadoDao
import com.app.changescout.data.local.dao.EvaluacionComercialDao
import com.app.changescout.data.local.entity.ProductoImportadoEntity
import com.app.changescout.data.local.entity.EvaluacionComercialEntity

@Database(
    entities = [
        ProductoImportadoEntity::class,
        EvaluacionComercialEntity::class
    ],
    version = 6,
    exportSchema = true
)
abstract class ChangeScoutDatabase : RoomDatabase() {
    abstract fun productoImportadoDao(): ProductoImportadoDao
    abstract fun evaluacionComercialDao(): EvaluacionComercialDao
}
