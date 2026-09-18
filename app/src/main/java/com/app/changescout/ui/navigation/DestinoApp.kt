package com.app.changescout.ui.navigation

import android.net.Uri

object DestinoApp {
    const val RADAR_PRODUCTOS = "radar_productos"
    const val CUENTA = "cuenta"
    const val LECTURAS_CADUCADAS = "lecturas_caducadas"
    const val FORMULARIO_PRODUCTO_BASE = "formulario_producto"
    const val FORMULARIO_PRODUCTO = FORMULARIO_PRODUCTO_BASE
    const val DETALLE_PRODUCTO_BASE = "detalle_producto"
    const val HISTORIAL_PRODUCTO_BASE = "historial_producto"
    const val ARG_PRODUCTO_ID = "productoId"
    const val ARG_VOLVER_RADAR_AL_ACTUALIZAR = "volverRadarAlActualizar"
    const val ARG_FORM_NOMBRE = "nombre"
    const val ARG_FORM_PRECIO_FOB = "precioFobUsd"
    const val ARG_FORM_FLETE = "fleteUsd"
    const val ARG_FORM_SEGURO = "seguroUsd"
    const val ARG_FORM_ARANCELES = "arancelesUsd"
    const val ARG_FORM_OTROS_CARGOS = "otrosCargosUsd"
    const val ARG_FORM_CANTIDAD = "cantidadDisponible"
    const val ARG_FORM_MARGEN_OBJETIVO = "margenObjetivoPct"
    const val ARG_FORM_QUERY = "queryCompetencia"
    const val FORMULARIO_PRODUCTO_EDICION = "$FORMULARIO_PRODUCTO_BASE/{$ARG_PRODUCTO_ID}" +
        "?$ARG_FORM_NOMBRE={$ARG_FORM_NOMBRE}" +
        "&$ARG_FORM_PRECIO_FOB={$ARG_FORM_PRECIO_FOB}" +
        "&$ARG_FORM_FLETE={$ARG_FORM_FLETE}" +
        "&$ARG_FORM_SEGURO={$ARG_FORM_SEGURO}" +
        "&$ARG_FORM_ARANCELES={$ARG_FORM_ARANCELES}" +
        "&$ARG_FORM_OTROS_CARGOS={$ARG_FORM_OTROS_CARGOS}" +
        "&$ARG_FORM_CANTIDAD={$ARG_FORM_CANTIDAD}" +
        "&$ARG_FORM_MARGEN_OBJETIVO={$ARG_FORM_MARGEN_OBJETIVO}" +
        "&$ARG_FORM_QUERY={$ARG_FORM_QUERY}"
    const val DETALLE_PRODUCTO = "$DETALLE_PRODUCTO_BASE/{$ARG_PRODUCTO_ID}" +
        "?$ARG_VOLVER_RADAR_AL_ACTUALIZAR={$ARG_VOLVER_RADAR_AL_ACTUALIZAR}"
    const val HISTORIAL_PRODUCTO = "$HISTORIAL_PRODUCTO_BASE/{$ARG_PRODUCTO_ID}"

    fun rutaEditar(
        productoId: Long,
        nombre: String = "",
        precioFobUsd: String = "",
        fleteUsd: String = "",
        seguroUsd: String = "",
        arancelesUsd: String = "",
        otrosCargosUsd: String = "",
        cantidadDisponible: String = "",
        margenObjetivoPct: String = "20",
        queryCompetencia: String = ""
    ): String {
        return "$FORMULARIO_PRODUCTO_BASE/$productoId" +
            "?$ARG_FORM_NOMBRE=${nombre.url()}" +
            "&$ARG_FORM_PRECIO_FOB=${precioFobUsd.url()}" +
            "&$ARG_FORM_FLETE=${fleteUsd.url()}" +
            "&$ARG_FORM_SEGURO=${seguroUsd.url()}" +
            "&$ARG_FORM_ARANCELES=${arancelesUsd.url()}" +
            "&$ARG_FORM_OTROS_CARGOS=${otrosCargosUsd.url()}" +
            "&$ARG_FORM_CANTIDAD=${cantidadDisponible.url()}" +
            "&$ARG_FORM_MARGEN_OBJETIVO=${margenObjetivoPct.url()}" +
            "&$ARG_FORM_QUERY=${queryCompetencia.url()}"
    }

    fun rutaDetalle(
        productoId: Long,
        volverRadarAlActualizar: Boolean = false
    ): String {
        return "$DETALLE_PRODUCTO_BASE/$productoId?$ARG_VOLVER_RADAR_AL_ACTUALIZAR=$volverRadarAlActualizar"
    }

    fun rutaHistorialProducto(productoId: Long): String {
        return "$HISTORIAL_PRODUCTO_BASE/$productoId"
    }

    private fun String.url(): String = Uri.encode(this)
}
