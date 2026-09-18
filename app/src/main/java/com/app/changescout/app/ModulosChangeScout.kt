package com.app.changescout.app

import android.content.Context
import com.app.changescout.data.api.ConfiguracionRed
import com.app.changescout.data.api.FabricaClienteHttp
import com.app.changescout.data.auth.PersistenciaSesion
import androidx.room.Room
import com.app.changescout.data.api.apisnet.ApisNetTipoCambioApi
import com.app.changescout.data.api.apisnet.ApisNetTipoCambioConfig
import com.app.changescout.data.api.apisnet.ProveedorTipoCambioApisNet
import com.app.changescout.data.api.apisnet.TipoCambioCacheMemoria
import com.app.changescout.data.api.backend.BackendProxyConfig
import com.app.changescout.data.api.marketplace.backend.BackendMarketplaceApi
import com.app.changescout.data.api.marketplace.backend.ProveedorMarketplaceBackend
import com.app.changescout.data.api.nlp.backend.BackendNlpApi
import com.app.changescout.data.api.nlp.backend.ProveedorFiltroNlpBackend
import com.app.changescout.data.auth.AlmacenSesion
import com.app.changescout.data.auth.InterceptorSesionBackend
import com.app.changescout.data.auth.SupabaseAuthApi
import com.app.changescout.data.auth.SupabaseAuthConfig
import com.app.changescout.data.local.ChangeScoutDatabase
import com.app.changescout.data.local.dao.ProductoImportadoDao
import com.app.changescout.data.local.dao.EvaluacionComercialDao
import com.app.changescout.data.local.MigracionesBaseDatosChangeScout
import com.app.changescout.data.repository.RepositorioEvaluacionComercialRoom
import com.app.changescout.data.repository.RepositorioProductoImportadoRoom
import com.app.changescout.domain.repository.ProveedorFiltroNlp
import com.app.changescout.domain.repository.ProveedorMarketplace
import com.app.changescout.domain.repository.ProveedorTipoCambio
import com.app.changescout.domain.repository.RepositorioEvaluacionComercial
import com.app.changescout.domain.repository.RepositorioProductoImportado
import com.app.changescout.domain.rules.ClasificadorVeredictoComercial
import com.app.changescout.domain.rules.PoliticaEvidencia
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object ModulosChangeScout {
    @Provides
    fun providePersistenciaSesion(
        almacen: AlmacenSesion
    ): PersistenciaSesion = almacen

    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemUTC()

    @Provides
    @Singleton
    @AppScope
    fun provideAppScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Provides
    @Singleton
    @ClienteHttpPublico
    fun provideOkHttpClientPublico(configuracion: ConfiguracionRed): OkHttpClient =
        FabricaClienteHttp(configuracion).crear()

    @Provides
    @Singleton
    fun provideConfiguracionRed(): ConfiguracionRed = ConfiguracionRed(timeoutMillis = 3_000L)

    @Provides
    @Singleton
    @ClienteHttpBackend
    fun provideOkHttpClientBackend(
        @ClienteHttpPublico okHttpClient: OkHttpClient,
        interceptorSesionBackend: InterceptorSesionBackend
    ): OkHttpClient {
        return okHttpClient.newBuilder()
            .addInterceptor(interceptorSesionBackend)
            .build()
    }

    @Provides
    @Singleton
    fun provideApisNetTipoCambioApi(
        @ClienteHttpPublico okHttpClient: OkHttpClient
    ): ApisNetTipoCambioApi = retrofitApi(ApisNetTipoCambioConfig.BASE_URL, okHttpClient)

    @Provides
    @Singleton
    fun provideBackendMarketplaceApi(
        @ClienteHttpBackend okHttpClient: OkHttpClient
    ): BackendMarketplaceApi = retrofitApi(BackendProxyConfig.BASE_URL, okHttpClient)

    @Provides
    @Singleton
    fun provideBackendNlpApi(
        @ClienteHttpBackend okHttpClient: OkHttpClient
    ): BackendNlpApi = retrofitApi(BackendProxyConfig.BASE_URL, okHttpClient)

    @Provides
    @Singleton
    fun provideSupabaseAuthApi(
        @ClienteHttpPublico okHttpClient: OkHttpClient
    ): SupabaseAuthApi = retrofitApi(SupabaseAuthConfig.BASE_URL, okHttpClient)

    @Provides
    @Singleton
    fun provideProveedorTipoCambio(
        api: ApisNetTipoCambioApi,
        cache: TipoCambioCacheMemoria,
        clock: Clock
    ): ProveedorTipoCambio {
        return ProveedorTipoCambioApisNet(api, cache, clock)
    }

    @Provides
    @Singleton
    fun provideProveedorMarketplace(
        api: BackendMarketplaceApi
    ): ProveedorMarketplace {
        return ProveedorMarketplaceBackend(api)
    }

    @Provides
    @Singleton
    fun provideProveedorFiltroNlp(
        api: BackendNlpApi
    ): ProveedorFiltroNlp {
        return ProveedorFiltroNlpBackend(api)
    }

    @Provides
    fun providePoliticaEvidencia(): PoliticaEvidencia {
        return PoliticaEvidencia()
    }

    @Provides
    fun provideClasificadorVeredictoComercial(): ClasificadorVeredictoComercial {
        return ClasificadorVeredictoComercial()
    }

    @Provides
    @Singleton
    fun provideChangeScoutDatabase(
        @ApplicationContext context: Context
    ): ChangeScoutDatabase {
        return Room.databaseBuilder(
            context,
            ChangeScoutDatabase::class.java,
            "changescout.db"
        )
            .addMigrations(
                MigracionesBaseDatosChangeScout.MIGRATION_1_2,
                MigracionesBaseDatosChangeScout.MIGRATION_2_3,
                MigracionesBaseDatosChangeScout.MIGRATION_3_4,
                MigracionesBaseDatosChangeScout.MIGRATION_4_5,
                MigracionesBaseDatosChangeScout.MIGRATION_5_6
            )
            .build()
    }

    @Provides
    fun provideProductoImportadoDao(
        database: ChangeScoutDatabase
    ): ProductoImportadoDao = database.productoImportadoDao()

    @Provides
    fun provideEvaluacionComercialDao(
        database: ChangeScoutDatabase
    ): EvaluacionComercialDao = database.evaluacionComercialDao()

    @Provides
    @Singleton
    fun provideRepositorioProductoImportado(
        productoDao: ProductoImportadoDao,
        almacenSesion: AlmacenSesion
    ): RepositorioProductoImportado {
        return RepositorioProductoImportadoRoom(productoDao, almacenSesion)
    }

    @Provides
    @Singleton
    fun provideRepositorioEvaluacionComercial(
        evaluacionDao: EvaluacionComercialDao,
        almacenSesion: AlmacenSesion
    ): RepositorioEvaluacionComercial {
        return RepositorioEvaluacionComercialRoom(evaluacionDao, almacenSesion)
    }

    private inline fun <reified T> retrofitApi(
        baseUrl: String,
        okHttpClient: OkHttpClient
    ): T {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(T::class.java)
    }
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ClienteHttpPublico

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ClienteHttpBackend

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppScope
