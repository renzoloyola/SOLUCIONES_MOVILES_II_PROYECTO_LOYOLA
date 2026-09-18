package com.app.changescout.data.auth

import com.app.changescout.domain.model.ResultadoOperacion
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Response

/** Pruebas JVM del repositorio real: sin Android, sockets ni cuentas reales. */
class LoginTest {
    private val almacen = AlmacenEnMemoria()
    private val api = ApiSimulada()
    private val servicio = RepositorioSesionSupabase(api, almacen)

    @Test
    fun a_estadoInicial_sinSesionActiva() = runBlocking {
        assertNull(servicio.sesionActual())
        assertNull(servicio.restaurarSesion())
        assertNull(almacen.obtenerToken())
        assertEquals(0, api.llamadas)
        println("(a) OK: servicio nuevo sin sesion, sin token y con 0 llamadas.")
    }

    @Test
    fun b_credencialesValidas_autenticaYGuardaToken() = runBlocking {
        assertNull(servicio.sesionActual())
        val resultado = servicio.iniciarSesion("alumno@example.test", "clave-prueba")
        assertTrue(resultado is ResultadoOperacion.Exito)
        val sesion = (resultado as ResultadoOperacion.Exito).data
        assertEquals("usuario-prueba", sesion.userId)
        assertEquals(sesion, servicio.sesionActual())
        assertEquals("token-acceso-simulado", almacen.obtenerToken())
        assertEquals("token-refresh-simulado", almacen.obtenerRefreshToken())
        assertEquals(1, almacen.escrituras)
        assertEquals(1, api.llamadas)
        assertEquals("alumno@example.test", api.ultimaPeticion?.email)
        assertEquals("clave-prueba", api.ultimaPeticion?.password)
        println("(b) OK: estado Exito, sesion activa y tokens guardados; 1 llamada.")
    }

    @Test
    fun c_backendRechaza_errorSinReintentos() = runBlocking {
        api.rechazar = true
        val resultado = servicio.iniciarSesion("alumno@example.test", "clave-erronea")
        assertTrue(resultado is ResultadoOperacion.Fallo)
        assertEquals("Correo o clave incorrectos.", (resultado as ResultadoOperacion.Fallo).error.mensaje)
        assertNull(servicio.sesionActual())
        assertNull(almacen.obtenerToken())
        assertNull(almacen.obtenerRefreshToken())
        assertEquals(0, almacen.escrituras)
        assertEquals("El rechazo no debe reintentar el login", 1, api.llamadas)
        println("(c) OK: estado Fallo tras HTTP 400; sin sesion ni token; 1 llamada, 0 reintentos.")
    }

    private class ApiSimulada : SupabaseAuthApi {
        var llamadas = 0
        var rechazar = false
        var ultimaPeticion: SupabaseAuthRequest? = null
        override suspend fun iniciarSesion(apiKey: String, grantType: String, request: SupabaseAuthRequest): SupabaseSessionResponse {
            llamadas++
            ultimaPeticion = request
            check(grantType == "password")
            if (rechazar) throw HttpException(Response.error<Unit>(400,
                """{"code":"invalid_credentials"}""".toResponseBody("application/json".toMediaType())))
            check(request.email == "alumno@example.test" && request.password == "clave-prueba")
            return SupabaseSessionResponse("token-acceso-simulado", "token-refresh-simulado",
                SupabaseUserDto("usuario-prueba", request.email))
        }
        override suspend fun crearCuenta(apiKey: String, request: SupabaseAuthRequest): SupabaseSessionResponse = error("No esperado")
        override suspend fun refrescarSesion(apiKey: String, grantType: String, request: SupabaseRefreshRequest): SupabaseSessionResponse = error("No esperado")
        override fun refrescarSesionBloqueante(apiKey: String, grantType: String, request: SupabaseRefreshRequest): Call<SupabaseSessionResponse> = error("No esperado")
        override suspend fun cerrarSesion(apiKey: String, authorization: String, scope: String): Response<Unit> = error("No esperado")
    }

    /** Sustituye exclusivamente la persistencia Android; no la logica de login. */
    private class AlmacenEnMemoria : PersistenciaSesion {
        private var respuesta: SupabaseSessionResponse? = null
        var escrituras = 0
        override fun obtenerToken() = respuesta?.accessToken
        override fun obtenerRefreshToken() = respuesta?.refreshToken
        override fun obtenerSesion(): SesionUsuario? = respuesta?.user?.let {
            SesionUsuario(requireNotNull(it.id), requireNotNull(it.email), it.nombreUsuario() ?: "Alumno")
        }
        override fun guardar(sesion: SupabaseSessionResponse, nombreUsuarioLocal: String?): SesionUsuario? {
            respuesta = sesion
            escrituras++
            return obtenerSesion()
        }
        override fun limpiar() { respuesta = null }
    }
}
