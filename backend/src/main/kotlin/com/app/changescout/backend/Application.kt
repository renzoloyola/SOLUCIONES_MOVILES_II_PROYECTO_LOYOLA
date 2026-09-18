package com.app.changescout.backend

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.serialization.gson.gson
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopping
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    embeddedServer(Netty, host = "0.0.0.0", port = port, module = Application::module)
        .start(wait = true)
}

fun Application.module(
    apifyConfig: ApifyConfig = ApifyConfig.fromEnv(),
    nlpConfig: NlpConfig = NlpConfig.fromEnv(),
    authConfig: AuthConfig = AuthConfig.fromEnv()
) {
    install(ServerContentNegotiation) {
        gson()
    }
    configureAuth(authConfig)

    val marketplaceClient = httpClient(apifyConfig.timeoutMillis)
    val nlpClient = httpClient(nlpConfig.timeoutMillis)
    val marketplace = ApifyMarketplaceService(marketplaceClient, apifyConfig)
    val nlp: NlpFiltroService = LlmNlpService(nlpClient, nlpConfig)

    monitor.subscribe(ApplicationStopping) {
        marketplaceClient.close()
        nlpClient.close()
    }

    routing {
        get("/") {
            call.respond(mapOf("service" to "changescout-backend", "status" to "ok"))
        }

        get("/health") {
            call.respond(mapOf("status" to "ok"))
        }

        if (authConfig.enabled) {
            authenticate(AUTH_SUPABASE) {
                marketplaceRoutes(marketplace, apifyConfig)
                nlpRoutes(nlp)
            }
        } else {
            marketplaceRoutes(marketplace, apifyConfig)
            nlpRoutes(nlp)
        }
    }
}

private fun httpClient(timeoutMillis: Long): HttpClient {
    return HttpClient(CIO) {
        expectSuccess = true
        install(ClientContentNegotiation) {
            gson()
        }
        install(HttpTimeout) {
            connectTimeoutMillis = 15_000
            requestTimeoutMillis = timeoutMillis
            socketTimeoutMillis = timeoutMillis
        }
    }
}
