package com.app.changescout.data.api.backend

import com.app.changescout.BuildConfig

object BackendProxyConfig {
    val BASE_URL: String = BuildConfig.MARKETPLACE_BACKEND_URL
    val ESTA_CONFIGURADO: Boolean = BuildConfig.MARKETPLACE_BACKEND_CONFIGURED
    const val NOMBRE_PROVEEDOR_MARKETPLACE = "ChangeScout Marketplace Proxy"
    const val NOMBRE_PROVEEDOR_NLP = "ChangeScout NLP Proxy"
    const val PAIS_PERU = "PE"
    const val LIMITE_MAXIMO_BUSQUEDA = 15
}
