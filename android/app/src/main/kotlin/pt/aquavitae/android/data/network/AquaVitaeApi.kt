package pt.aquavitae.android.data.network

import pt.aquavitae.android.data.model.AuthResponse
import pt.aquavitae.android.data.model.BebidaDetail
import pt.aquavitae.android.data.model.BebidaSummary
import pt.aquavitae.android.data.model.CaveBebidaRequest
import pt.aquavitae.android.data.model.CaveBebidaResponse
import pt.aquavitae.android.data.model.CaveBebidaUpdateRequest
import pt.aquavitae.android.data.model.CaveDetailResponse
import pt.aquavitae.android.data.model.CaveRequest
import pt.aquavitae.android.data.model.CaveResponse
import pt.aquavitae.android.data.model.LoginRequest
import pt.aquavitae.android.data.model.PageResponse
import pt.aquavitae.android.data.model.PreferenciaRequest
import pt.aquavitae.android.data.model.ProdutorDetail
import pt.aquavitae.android.data.model.RegisterRequest
import pt.aquavitae.android.data.model.ReviewRequest
import pt.aquavitae.android.data.model.ReviewResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Contrato Retrofit da API AquaVitae (backend Spring Boot Kotlin, desenvolvido em paralelo
 * em ../../backend). Os paths e os campos dos DTOs seguem exatamente o contrato acordado.
 *
 * Endpoints marcados "(auth)" no contrato exigem o header `Authorization: Bearer <token>`,
 * que é adicionado automaticamente pelo [AuthInterceptor] — não é preciso repeti-lo aqui.
 */
interface AquaVitaeApi {

    // --- Autenticação ---

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    // --- Bebidas / catálogo ---

    @GET("api/bebidas")
    suspend fun searchBebidas(
        @Query("search") search: String? = null,
        @Query("categoriaId") categoriaId: Long? = null,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
    ): PageResponse<BebidaSummary>

    @GET("api/bebidas/{id}")
    suspend fun getBebidaDetail(@Path("id") id: Long): BebidaDetail

    // --- Produtores ---

    @GET("api/produtores/{id}")
    suspend fun getProdutorDetail(@Path("id") id: Long): ProdutorDetail

    // --- Reviews ---

    @GET("api/bebidas/{bebidaId}/reviews")
    suspend fun getReviews(@Path("bebidaId") bebidaId: Long): List<ReviewResponse>

    @POST("api/bebidas/{bebidaId}/reviews")
    suspend fun postReview(
        @Path("bebidaId") bebidaId: Long,
        @Body request: ReviewRequest,
    ): ReviewResponse

    @PUT("api/reviews/{id}")
    suspend fun updateReview(
        @Path("id") id: Long,
        @Body request: ReviewRequest,
    ): ReviewResponse

    @DELETE("api/reviews/{id}")
    suspend fun deleteReview(@Path("id") id: Long)

    // --- Favoritos ---

    @GET("api/users/me/favoritos")
    suspend fun getFavoritos(): List<BebidaSummary>

    @POST("api/bebidas/{bebidaId}/favorito")
    suspend fun addFavorito(@Path("bebidaId") bebidaId: Long)

    @DELETE("api/bebidas/{bebidaId}/favorito")
    suspend fun removeFavorito(@Path("bebidaId") bebidaId: Long)

    // --- Wishlist ---

    @GET("api/users/me/wishlist")
    suspend fun getWishlist(): List<BebidaSummary>

    @POST("api/bebidas/{bebidaId}/wishlist")
    suspend fun addWishlist(@Path("bebidaId") bebidaId: Long)

    @DELETE("api/bebidas/{bebidaId}/wishlist")
    suspend fun removeWishlist(@Path("bebidaId") bebidaId: Long)

    // --- Cave virtual ---

    @GET("api/users/me/caves")
    suspend fun getCaves(): List<CaveResponse>

    @POST("api/users/me/caves")
    suspend fun createCave(@Body request: CaveRequest): CaveResponse

    @GET("api/caves/{id}")
    suspend fun getCaveDetail(@Path("id") id: Long): CaveDetailResponse

    @POST("api/caves/{id}/bebidas")
    suspend fun addBebidaToCave(
        @Path("id") caveId: Long,
        @Body request: CaveBebidaRequest,
    ): CaveBebidaResponse

    @PATCH("api/caves/{id}/bebidas/{caveBebidaId}")
    suspend fun updateCaveBebida(
        @Path("id") caveId: Long,
        @Path("caveBebidaId") caveBebidaId: Long,
        @Body request: CaveBebidaUpdateRequest,
    ): CaveBebidaResponse

    @DELETE("api/caves/{id}/bebidas/{caveBebidaId}")
    suspend fun removeBebidaFromCave(
        @Path("id") caveId: Long,
        @Path("caveBebidaId") caveBebidaId: Long,
    )

    // --- Preferências (onboarding) ---

    @PUT("api/users/me/preferencias")
    suspend fun updatePreferencias(@Body request: PreferenciaRequest)
}
