package com.app.changescout.backend

import com.google.gson.annotations.SerializedName
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import java.io.IOException

fun Route.marketplaceRoutes(
    marketplace: ApifyMarketplaceService,
    apifyConfig: ApifyConfig
) {
    get("/marketplace/search") {
        val query = call.request.queryParameters["query"]?.trim().orEmpty()
        if (query.isBlank() || query.length > MAX_QUERY_CHARS || query.any { char -> char.isISOControl() }) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("El query de competencia no tiene un formato valido.")
            )
            return@get
        }

        val limit = call.request.queryParameters["limit"]
            ?.toIntOrNull()
            ?.coerceIn(1, apifyConfig.maxItems)
            ?: apifyConfig.maxItems
        val country = call.request.queryParameters["country"]
            ?.trim()
            ?.takeIf { value -> value.isNotBlank() }
            ?: "PE"
        if (!COUNTRY_REGEX.matches(country)) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("El pais debe usar codigo ISO de 2 letras."))
            return@get
        }

        try {
            call.respond(marketplace.buscar(query = query, country = country.uppercase(), limit = limit))
        } catch (error: ApifyConfigException) {
            call.respond(
                HttpStatusCode.ServiceUnavailable,
                ErrorResponse("El buscador de marketplace no esta disponible ahora.")
            )
        } catch (error: ApifyActorException) {
            call.respond(HttpStatusCode.BadGateway, ErrorResponse(error.message))
        } catch (error: HttpRequestTimeoutException) {
            call.respond(
                HttpStatusCode.GatewayTimeout,
                ErrorResponse("El marketplace esta demorando por consultas repetidas. Prueba otro producto o intenta de nuevo mas tarde.")
            )
        } catch (error: ClientRequestException) {
            call.respond(
                HttpStatusCode.BadGateway,
                ErrorResponse("El marketplace no acepto esta busqueda. Prueba con otro nombre de producto.")
            )
        } catch (error: ServerResponseException) {
            call.respond(
                HttpStatusCode.BadGateway,
                ErrorResponse("El marketplace no esta disponible ahora. Intenta nuevamente en unos minutos.")
            )
        } catch (error: IOException) {
            call.respond(
                HttpStatusCode.BadGateway,
                ErrorResponse("No se pudo conectar con el marketplace. Intenta nuevamente en unos minutos.")
            )
        }
    }
}

private const val MAX_QUERY_CHARS = 80
private val COUNTRY_REGEX = Regex("^[A-Za-z]{2}$")

data class ApifyConfig(
    val token: String?,
    val actorId: String,
    val timeoutMillis: Long,
    val maxItems: Int,
    val proxyGroups: List<String>
) {
    val actorIdParaRuta: String = actorId.replace("/", "~")

    companion object {
        fun fromEnv(): ApifyConfig {
            return ApifyConfig(
                token = System.getenv("APIFY_TOKEN"),
                actorId = System.getenv("APIFY_ACTOR_ID")
                    ?: "scrapers_lat/mercadolibre-scraper",
                timeoutMillis = System.getenv("APIFY_TIMEOUT_MS")?.toLongOrNull()
                    ?.coerceIn(1_000L, 180_000L)
                    ?: 180_000L,
                maxItems = System.getenv("MARKETPLACE_MAX_ITEMS")?.toIntOrNull()?.coerceIn(1, 15)
                    ?: 15,
                proxyGroups = System.getenv("APIFY_PROXY_GROUPS")
                    ?.split(",")
                    ?.map { value -> value.trim() }
                    ?.filter { value -> value.isNotBlank() }
                    ?: listOf("RESIDENTIAL")
            )
        }
    }
}

class ApifyMarketplaceService(
    private val client: HttpClient,
    private val config: ApifyConfig
) {
    suspend fun buscar(
        query: String,
        country: String,
        limit: Int
    ): List<MarketplacePublicacionResponse> {
        val token = config.token?.takeIf { value -> value.isNotBlank() }
            ?: throw ApifyConfigException("Falta APIFY_TOKEN en el backend.")
        val input = ApifySearchInput(
            searchTerm = query,
            country = country.lowercase(),
            maxItems = limit.coerceIn(1, config.maxItems),
            proxyConfiguration = ApifyProxyConfig(
                apifyProxyGroups = config.proxyGroups
            )
        )

        val items = client.post(
            "https://api.apify.com/v2/actors/${config.actorIdParaRuta}/run-sync-get-dataset-items"
        ) {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(input)
        }.body<List<ApifyProductoDto>>()

        return items.toMarketplacePublicaciones()
    }
}

internal fun List<ApifyProductoDto>.toMarketplacePublicaciones(): List<MarketplacePublicacionResponse> {
    val publicaciones = mapNotNull { item -> item.toPublicacionResponse() }
    val errores = mapNotNull { item -> item.error?.trim()?.takeIf { error -> error.isNotBlank() } }
        .distinct()
    if (publicaciones.isEmpty() && errores.isNotEmpty()) {
        throw ApifyActorException("MercadoLibre limito esta busqueda temporalmente. Prueba otro producto o intenta de nuevo mas tarde.")
    }
    return publicaciones
}

data class ApifySearchInput(
    val searchTerm: String,
    val country: String,
    val maxItems: Int,
    val withDetails: Boolean = false,
    val proxyConfiguration: ApifyProxyConfig
)

data class ApifyProxyConfig(
    val useApifyProxy: Boolean = true,
    val apifyProxyGroups: List<String>
)

data class ApifyProductoDto(
    @SerializedName("listingId")
    val listingId: String?,
    val title: String?,
    val price: Double?,
    val currency: String?,
    val url: String?,
    val error: String?
) {
    fun toPublicacionResponse(): MarketplacePublicacionResponse? {
        if (!error.isNullOrBlank()) return null
        val publicacionId = listingId?.takeIf { value -> value.isNotBlank() } ?: return null
        val titulo = title?.takeIf { value -> value.isNotBlank() } ?: return null
        val precio = price?.takeIf { value -> value > 0.0 } ?: return null
        val moneda = currency?.takeIf { value -> value.isNotBlank() } ?: "PEN"

        return MarketplacePublicacionResponse(
            id = publicacionId,
            title = titulo,
            price = precio,
            currency = moneda,
            condition = null,
            url = url.normalizarUrl()
        )
    }
}

data class MarketplacePublicacionResponse(
    val id: String,
    val title: String,
    val price: Double,
    val currency: String,
    val condition: String?,
    val url: String?
)

class ApifyConfigException(
    override val message: String
) : RuntimeException(message)

class ApifyActorException(
    override val message: String
) : RuntimeException(message)

private fun String?.normalizarUrl(): String? {
    val value = this?.takeIf { url -> url.isNotBlank() } ?: return null
    return if (value.startsWith("http://") || value.startsWith("https://")) {
        value
    } else {
        "https://$value"
    }
}
