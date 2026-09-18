package com.app.changescout.ui.viewmodel

import com.app.changescout.data.api.ConfiguracionRed
import com.app.changescout.data.api.FabricaClienteHttp
import com.app.changescout.data.auth.*
import java.net.SocketException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Call
import retrofit2.Response

/** JVM: ViewModel y repositorio reales; API y almacenamiento locales. */
@OptIn(ExperimentalCoroutinesApi::class)
class ConectividadTest {
    private val dispatcher = StandardTestDispatcher()
    private val configuracion = ConfiguracionRed(timeoutMillis = 3_000L)

    @Before fun preparar() { Dispatchers.setMain(dispatcher) }
    @After fun limpiar() { Dispatchers.resetMain() }

    @Test
    fun a_demoraDe4Segundos_timeoutA3Segundos_apagaSpinner() = runTest(dispatcher) {
        val cliente = FabricaClienteHttp(configuracion).crear()
        assertEquals(3_000, cliente.callTimeoutMillis)
        assertEquals(3_000, cliente.connectTimeoutMillis)
        assertEquals(3_000, cliente.readTimeoutMillis)
        assertEquals(3_000, cliente.writeTimeoutMillis)
        val api = ApiControlada { delay(4_000L) }
        val vm = ViewModelSesion(RepositorioSesionSupabase(api, AlmacenVacio(), configuracion))
        runCurrent() // finaliza la restauracion inicial sin sesion
        iniciarLogin(vm)
        runCurrent()
        assertTrue(vm.uiState.value.estaCargando)
        advanceTimeBy(2_999L)
        runCurrent()
        assertTrue(vm.uiState.value.estaCargando)
        assertNull(vm.uiState.value.mensaje)
        advanceTimeBy(1L)
        runCurrent()
        assertEquals(3_000L, currentTime)
        assertEquals("Tiempo agotado", vm.uiState.value.mensaje)
        assertTrue(vm.uiState.value.mensajeEsError)
        assertFalse(vm.uiState.value.estaCargando)
        assertFalse(vm.uiState.value.estaAutenticado)
        advanceUntilIdle()
        assertEquals(1, api.llamadas)
        assertEquals(0, api.respuestasCompletadas)
        println("PASS (a): doble demora 4000 ms; limite 3000 ms; Tiempo agotado; spinner=false; 0 reintentos.")
    }

    @Test
    fun b_socketException_sinConexion_apagaSpinner() = runTest(dispatcher) {
        val api = ApiControlada {
            delay(1L) // permite observar el spinner antes de la falla
            throw SocketException("Red desconectada simulada")
        }
        val vm = ViewModelSesion(RepositorioSesionSupabase(api, AlmacenVacio(), configuracion))
        runCurrent()
        iniciarLogin(vm)
        runCurrent()
        assertTrue(vm.uiState.value.estaCargando)
        advanceTimeBy(1L)
        runCurrent()
        assertEquals("Sin conexión", vm.uiState.value.mensaje)
        assertTrue(vm.uiState.value.mensajeEsError)
        assertFalse(vm.uiState.value.estaCargando)
        assertFalse(vm.uiState.value.estaAutenticado)
        advanceUntilIdle()
        assertEquals(1, api.llamadas)
        assertEquals(0, api.respuestasCompletadas)
        println("PASS (b): SocketException -> Sin conexion; spinner=false; 1 llamada, 0 reintentos.")
    }

    private fun iniciarLogin(vm: ViewModelSesion) {
        vm.onEvent(EventoSesion.EmailCambiado("alumno@example.test"))
        vm.onEvent(EventoSesion.PasswordCambiado("clave-prueba"))
        vm.onEvent(EventoSesion.IniciarSesion)
    }

    private class ApiControlada(private val comportamiento: suspend () -> Unit) : SupabaseAuthApi {
        var llamadas = 0
        var respuestasCompletadas = 0
        override suspend fun iniciarSesion(apiKey: String, grantType: String, request: SupabaseAuthRequest): SupabaseSessionResponse {
            llamadas++
            comportamiento()
            respuestasCompletadas++
            return SupabaseSessionResponse("token", "refresh", SupabaseUserDto("id", request.email))
        }
        override suspend fun crearCuenta(apiKey: String, request: SupabaseAuthRequest): SupabaseSessionResponse = error("No esperado")
        override suspend fun refrescarSesion(apiKey: String, grantType: String, request: SupabaseRefreshRequest): SupabaseSessionResponse = error("No esperado")
        override fun refrescarSesionBloqueante(apiKey: String, grantType: String, request: SupabaseRefreshRequest): Call<SupabaseSessionResponse> = error("No esperado")
        override suspend fun cerrarSesion(apiKey: String, authorization: String, scope: String): Response<Unit> = error("No esperado")
    }

    private class AlmacenVacio : PersistenciaSesion {
        override fun obtenerToken(): String? = null
        override fun obtenerRefreshToken(): String? = null
        override fun obtenerSesion(): SesionUsuario? = null
        override fun guardar(sesion: SupabaseSessionResponse, nombreUsuarioLocal: String?): SesionUsuario? = error("Una falla no debe guardar tokens")
        override fun limpiar() = Unit
    }
}
