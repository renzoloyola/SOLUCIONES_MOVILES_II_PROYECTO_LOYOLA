package com.app.changescout.backend

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond
import java.net.URI
import java.util.concurrent.TimeUnit

const val AUTH_SUPABASE = "auth-supabase"

data class AuthConfig(
    val enabled: Boolean,
    val issuer: String,
    val jwksUrl: String,
    val audience: String
) {
    companion object {
        fun fromEnv(): AuthConfig = fromMap(System.getenv())

        fun fromMap(env: Map<String, String>): AuthConfig {
            val issuer = env.value("SUPABASE_ISSUER")
            val jwksUrl = env.value("SUPABASE_JWKS_URL")
            val required = env.value("AUTH_REQUIRED")
                ?.lowercase()
                ?.toBooleanStrictOrNull()
                ?: true

            if (!required) {
                return AuthConfig(
                    enabled = false,
                    issuer = "",
                    jwksUrl = "",
                    audience = "authenticated"
                )
            }

            return AuthConfig(
                enabled = true,
                issuer = issuer ?: throw AuthConfigException("Falta SUPABASE_ISSUER en el backend."),
                jwksUrl = jwksUrl ?: throw AuthConfigException("Falta SUPABASE_JWKS_URL en el backend."),
                audience = env.value("SUPABASE_AUDIENCE") ?: "authenticated"
            )
        }

        private fun Map<String, String>.value(key: String): String? {
            return this[key]?.trim()?.takeIf { value -> value.isNotBlank() }
        }
    }
}

fun Application.configureAuth(config: AuthConfig) {
    if (!config.enabled) return

    val jwkProvider = JwkProviderBuilder(URI(config.jwksUrl).toURL())
        .cached(10, 10, TimeUnit.MINUTES)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    install(Authentication) {
        jwt(AUTH_SUPABASE) {
            realm = "ChangeScout"
            verifier(jwkProvider, config.issuer) {
                acceptLeeway(30)
                withAudience(config.audience)
            }
            validate { credential ->
                val userId = credential.payload.subject
                val role = credential.payload.getClaim("role").asString()
                if (!userId.isNullOrBlank() && role == "authenticated") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Sesion no valida o expirada."))
            }
        }
    }
}

class AuthConfigException(
    override val message: String
) : RuntimeException(message)
