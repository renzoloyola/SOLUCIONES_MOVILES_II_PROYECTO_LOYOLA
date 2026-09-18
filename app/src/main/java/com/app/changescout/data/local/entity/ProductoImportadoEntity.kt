package com.app.changescout.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.app.changescout.domain.model.ComponentesCostoImportacion
import com.app.changescout.domain.model.ProductoImportado

@Entity(
    tableName = "productos_importados",
    indices = [Index(value = ["usuarioId"])]
)
data class ProductoImportadoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val usuarioId: String,
    val nombre: String,
    val queryCompetencia: String,
    val precioFobUsd: Double,
    val fleteUsd: Double,
    val seguroUsd: Double,
    val arancelesUsd: Double,
    val otrosCargosUsd: Double,
    val cantidadDisponible: Int,
    val margenObjetivoPct: Double,
    val notas: String?
) {
    fun toDomain(): ProductoImportado {
        return ProductoImportado(
            id = id,
            nombre = nombre,
            queryCompetencia = queryCompetencia,
            componentesCosto = ComponentesCostoImportacion(
                precioFobUsd = precioFobUsd,
                fleteUsd = fleteUsd,
                seguroUsd = seguroUsd,
                arancelesUsd = arancelesUsd,
                otrosCargosUsd = otrosCargosUsd
            ),
            cantidadDisponible = cantidadDisponible,
            margenObjetivoPct = margenObjetivoPct,
            notas = notas
        )
    }

    companion object {
        fun fromDomain(producto: ProductoImportado, usuarioId: String): ProductoImportadoEntity {
            return ProductoImportadoEntity(
                id = producto.id,
                usuarioId = usuarioId,
                nombre = producto.nombre,
                queryCompetencia = producto.queryCompetencia,
                precioFobUsd = producto.componentesCosto.precioFobUsd,
                fleteUsd = producto.componentesCosto.fleteUsd,
                seguroUsd = producto.componentesCosto.seguroUsd,
                arancelesUsd = producto.componentesCosto.arancelesUsd,
                otrosCargosUsd = producto.componentesCosto.otrosCargosUsd,
                cantidadDisponible = producto.cantidadDisponible,
                margenObjetivoPct = producto.margenObjetivoPct,
                notas = producto.notas
            )
        }
    }
}
