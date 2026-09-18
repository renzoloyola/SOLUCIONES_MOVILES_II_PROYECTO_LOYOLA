package com.app.changescout.backend

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthConfigTest {
    @Test
    fun fromMap_sinVariables_fallaCerrado() {
        assertThrows(AuthConfigException::class.java) {
            AuthConfig.fromMap(emptyMap())
        }
    }

    @Test
    fun fromMap_authDesactivadoExplicitamente_dejaAuthApagado() {
        val config = AuthConfig.fromMap(mapOf("AUTH_REQUIRED" to "false"))

        assertFalse(config.enabled)
    }

    @Test
    fun fromMap_conSupabaseConfig_prendeAuth() {
        val config = AuthConfig.fromMap(
            mapOf(
                "SUPABASE_ISSUER" to "https://demo.supabase.co/auth/v1",
                "SUPABASE_JWKS_URL" to "https://demo.supabase.co/auth/v1/.well-known/jwks.json"
            )
        )

        assertTrue(config.enabled)
        assertEquals("authenticated", config.audience)
    }

    @Test
    fun fromMap_authRequeridoSinIssuer_fallaCerrado() {
        assertThrows(AuthConfigException::class.java) {
            AuthConfig.fromMap(mapOf("AUTH_REQUIRED" to "true"))
        }
    }
}
