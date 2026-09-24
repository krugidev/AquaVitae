package pt.aquavitae.android.data.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pt.aquavitae.android.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

/**
 * Módulo Hilt responsável por toda a infraestrutura de rede: Moshi, OkHttp
 * (com [AuthInterceptor] e logging) e Retrofit, culminando na [AquaVitaeApi].
 *
 * O base URL vem de `BuildConfig.API_BASE_URL`, definido por build type em
 * app/build.gradle.kts (debug -> emulador/10.0.2.2, release -> produção).
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        // A maioria dos DTOs usa codegen (@JsonClass(generateAdapter = true)); este
        // factory de reflexão fica como rede de segurança para qualquer classe que
        // não tenha adapter gerado.
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        // Renova a sessão (uma vez, para todos os pedidos em simultâneo) quando um pedido volta com 401.
        .authenticator(tokenAuthenticator)
        .build()

    /**
     * A renovação da sessão tem o seu próprio cliente, **sem** o [AuthInterceptor] nem o [TokenAuthenticator]: é o
     * `Authenticator` que a chama, e com o mesmo cliente seria um ciclo.
     */
    @Provides
    @Singleton
    fun provideAuthRefreshApi(moshi: Moshi, loggingInterceptor: HttpLoggingInterceptor): AuthRefreshApi =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(OkHttpClient.Builder().addInterceptor(loggingInterceptor).build())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(AuthRefreshApi::class.java)

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Provides
    @Singleton
    fun provideAquaVitaeApi(retrofit: Retrofit): AquaVitaeApi =
        retrofit.create(AquaVitaeApi::class.java)
}
