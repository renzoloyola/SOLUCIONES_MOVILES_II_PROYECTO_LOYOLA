package com.app.changescout.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.app.changescout.ui.screens.account.PantallaCuenta
import com.app.changescout.ui.screens.account.PantallaLecturasCaducadas
import com.app.changescout.ui.screens.product.detail.PantallaDetalleProducto
import com.app.changescout.ui.screens.product.form.PantallaFormularioProducto
import com.app.changescout.ui.screens.product.history.PantallaHistorialProducto
import com.app.changescout.ui.screens.radar.PantallaRadarProductos

@Composable
fun NavegacionChangeScout(
    onCerrarSesion: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = DestinoApp.RADAR_PRODUCTOS
    ) {
        composable(DestinoApp.RADAR_PRODUCTOS) {
            PantallaRadarProductos(
                onNavegarACuenta = {
                    navController.navigateSingleTop(DestinoApp.CUENTA)
                },
                onNavegarAFormulario = {
                    navController.navigateSingleTop(DestinoApp.FORMULARIO_PRODUCTO)
                },
                onNavegarADetalle = { productoId ->
                    navController.navigateSingleTop(DestinoApp.rutaDetalle(productoId))
                }
            )
        }

        composable(DestinoApp.CUENTA) {
            PantallaCuenta(
                onNavegarAtras = { navController.popBackStack() },
                onAgregarProducto = {
                    navController.navigateSingleTop(DestinoApp.FORMULARIO_PRODUCTO)
                },
                onVerCaducadas = {
                    navController.navigateSingleTop(DestinoApp.LECTURAS_CADUCADAS)
                },
                onCerrarSesion = onCerrarSesion
            )
        }

        composable(DestinoApp.LECTURAS_CADUCADAS) {
            PantallaLecturasCaducadas(
                onNavegarAtras = { navController.popBackStack() },
                onNavegarADetalle = { productoId ->
                    navController.navigateSingleTop(
                        DestinoApp.rutaDetalle(
                            productoId = productoId,
                            volverRadarAlActualizar = true
                        )
                    )
                }
            )
        }

        composable(DestinoApp.FORMULARIO_PRODUCTO) {
            PantallaFormularioProducto(
                onNavegarAtras = { navController.popBackStack() }
            )
        }

        composable(
            route = DestinoApp.FORMULARIO_PRODUCTO_EDICION,
            arguments = listOf(
                navArgument(DestinoApp.ARG_PRODUCTO_ID) {
                    type = NavType.LongType
                },
                navArgument(DestinoApp.ARG_FORM_NOMBRE) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(DestinoApp.ARG_FORM_PRECIO_FOB) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(DestinoApp.ARG_FORM_FLETE) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(DestinoApp.ARG_FORM_SEGURO) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(DestinoApp.ARG_FORM_ARANCELES) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(DestinoApp.ARG_FORM_OTROS_CARGOS) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(DestinoApp.ARG_FORM_CANTIDAD) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(DestinoApp.ARG_FORM_MARGEN_OBJETIVO) {
                    type = NavType.StringType
                    defaultValue = "20"
                },
                navArgument(DestinoApp.ARG_FORM_QUERY) {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) {
            PantallaFormularioProducto(
                onNavegarAtras = { navController.popBackStack() }
            )
        }

        composable(
            route = DestinoApp.DETALLE_PRODUCTO,
            arguments = listOf(
                navArgument(DestinoApp.ARG_PRODUCTO_ID) {
                    type = NavType.LongType
                },
                navArgument(DestinoApp.ARG_VOLVER_RADAR_AL_ACTUALIZAR) {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) {
            PantallaDetalleProducto(
                onNavegarAtras = { navController.popBackStack() },
                onNavegarARadar = {
                    navController.popBackStack(DestinoApp.RADAR_PRODUCTOS, inclusive = false)
                },
                onNavegarAHistorial = { productoId ->
                    navController.navigateSingleTop(DestinoApp.rutaHistorialProducto(productoId))
                },
                onNavegarAEditar = { producto ->
                    navController.navigateSingleTop(
                        DestinoApp.rutaEditar(
                            productoId = producto.id,
                            nombre = producto.nombre,
                            precioFobUsd = producto.componentesCosto.precioFobUsd.toInput(),
                            fleteUsd = producto.componentesCosto.fleteUsd.toInput(),
                            seguroUsd = producto.componentesCosto.seguroUsd.toInput(),
                            arancelesUsd = producto.componentesCosto.arancelesUsd.toInput(),
                            otrosCargosUsd = producto.componentesCosto.otrosCargosUsd.toInput(),
                            cantidadDisponible = producto.cantidadDisponible.toString(),
                            margenObjetivoPct = producto.margenObjetivoPct.toPercentInput(),
                            queryCompetencia = producto.queryCompetencia
                        )
                    )
                }
            )
        }

        composable(
            route = DestinoApp.HISTORIAL_PRODUCTO,
            arguments = listOf(
                navArgument(DestinoApp.ARG_PRODUCTO_ID) {
                    type = NavType.LongType
                }
            )
        ) {
            PantallaHistorialProducto(
                onNavegarAtras = { navController.popBackStack() }
            )
        }
    }
}

private fun Double.toInput(): String {
    return if (this == 0.0) "" else toString()
}

private fun Double.toPercentInput(): String {
    return if (this % 1.0 == 0.0) toInt().toString() else toString()
}

private fun NavHostController.navigateSingleTop(route: String) {
    navigate(route) {
        launchSingleTop = true
    }
}
