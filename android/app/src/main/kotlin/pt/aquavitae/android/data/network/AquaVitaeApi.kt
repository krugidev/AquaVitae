package pt.aquavitae.android.data.network

import pt.aquavitae.android.data.model.AuthResponse
import pt.aquavitae.android.data.model.Avatar
import pt.aquavitae.android.data.model.BebidaDetail
import pt.aquavitae.android.data.model.BebidaRelacao
import pt.aquavitae.android.data.model.BebidaSummary
import pt.aquavitae.android.data.model.CaveBebidaRequest
import pt.aquavitae.android.data.model.CaveBebidaResponse
import pt.aquavitae.android.data.model.CaveBebidaUpdateRequest
import pt.aquavitae.android.data.model.CaveConsumirRequest
import pt.aquavitae.android.data.model.CaveConsumoResponse
import pt.aquavitae.android.data.model.CaveDetailResponse
import pt.aquavitae.android.data.model.CaveRequest
import pt.aquavitae.android.data.model.CaveResponse
import pt.aquavitae.android.data.model.CliquePendente
import pt.aquavitae.android.data.model.Casta
import pt.aquavitae.android.data.model.LoginRequest
import pt.aquavitae.android.data.model.LookupItem
import pt.aquavitae.android.data.model.Nacionalidade
import pt.aquavitae.android.data.model.OfertaCompra
import pt.aquavitae.android.data.model.PageResponse
import pt.aquavitae.android.data.model.PreferenciaRequest
import pt.aquavitae.android.data.model.PreferenciaResponse
import pt.aquavitae.android.data.model.ProdutorDetail
import pt.aquavitae.android.data.model.RecuperarPasswordRequest
import pt.aquavitae.android.data.model.RedefinirPasswordRequest
import pt.aquavitae.android.data.model.RegisterRequest
import pt.aquavitae.android.data.model.RespostaCliqueRequest
import pt.aquavitae.android.data.model.ReviewRequest
import pt.aquavitae.android.data.model.ReviewResponse
import pt.aquavitae.android.data.model.ReviewsResponse
import pt.aquavitae.android.data.model.TermosTexto
import pt.aquavitae.android.data.model.UtilizadorMe
import pt.aquavitae.android.data.model.UtilizadorUpdateRequest
import pt.aquavitae.android.data.model.VerificarCodigoRequest
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

    // Recuperação de password, em 3 passos (o código de 6 dígitos vale 15 min). O 1.º devolve sempre 202, exista ou não a conta.
    @POST("api/auth/recuperar-password")
    suspend fun recuperarPassword(@Body request: RecuperarPasswordRequest)

    @POST("api/auth/verificar-codigo")
    suspend fun verificarCodigo(@Body request: VerificarCodigoRequest)

    @POST("api/auth/redefinir-password")
    suspend fun redefinirPassword(@Body request: RedefinirPasswordRequest)

    // --- Utilizador ---

    @GET("api/users/me")
    suspend fun getMe(): UtilizadorMe

    /** Perfil do onboarding (nome, nacionalidade, descrição, avatar): só se altera o que vai preenchido. */
    @PUT("api/users/me")
    suspend fun updateMe(@Body request: UtilizadorUpdateRequest): UtilizadorMe

    /** O popup dos termos e condições: regista a aceitação agora e devolve o perfil atualizado. */
    @POST("api/users/me/termos/aceitar")
    suspend fun aceitarTermos(): UtilizadorMe

    // --- Termos e condições (público: lê-se antes de haver conta) ---

    @GET("api/legal/termos")
    suspend fun getTermos(): TermosTexto

    // --- Bebidas / catálogo ---

    @GET("api/bebidas")
    suspend fun searchBebidas(
        @Query("search") search: String? = null,
        @Query("categoriaIds") categoriaIds: List<Long>? = null,
        @Query("produtorId") produtorId: Long? = null,
        @Query("paisId") paisId: Long? = null,
        @Query("regiaoIds") regiaoIds: List<Long>? = null,
        @Query("ratingMin") ratingMin: Double? = null,
        @Query("acidezMin") acidezMin: Int? = null,
        @Query("acidezMax") acidezMax: Int? = null,
        @Query("docuraMin") docuraMin: Int? = null,
        @Query("docuraMax") docuraMax: Int? = null,
        @Query("corpoId") corpoId: Long? = null,
        @Query("taninoId") taninoId: Long? = null,
        @Query("tipoId") tipoId: Long? = null,
        @Query("castaIds") castaIds: List<Long>? = null,
        @Query("precoMin") precoMin: Double? = null,
        @Query("precoMax") precoMax: Double? = null,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("sort") sort: String? = null,
    ): PageResponse<BebidaSummary>

    /** As sugestões da homepage ("Escolhido para ti"): sem preferências, cai para o catálogo todo. */
    @GET("api/bebidas/sugeridas")
    suspend fun getBebidasSugeridas(
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
    ): PageResponse<BebidaSummary>

    @GET("api/bebidas/{id}")
    suspend fun getBebidaDetail(@Path("id") id: Long): BebidaDetail

    // --- Comprar (ofertas dos retalhistas, cliques nos links de afiliado, inquérito "Compraste?") ---

    /** Todas as ofertas de uma bebida: disponíveis primeiro (mais barata primeiro), indisponíveis no fim. */
    @GET("api/bebidas/{bebidaId}/links-compra")
    suspend fun getLinksCompra(@Path("bebidaId") bebidaId: Long): List<OfertaCompra>

    /** Regista o clique do utilizador num link (204). 409 se o link já não está disponível. */
    @POST("api/bebidas/{bebidaId}/links-compra/{linkId}/clique")
    suspend fun registarCliqueCompra(@Path("bebidaId") bebidaId: Long, @Path("linkId") linkId: Long)

    /** Cliques por perguntar ("Compraste X?"): mais de 2 min e menos de 72 h, o mais recente de cada bebida. */
    @GET("api/users/me/cliques-compra/pendentes")
    suspend fun getCliquesPendentes(): List<CliquePendente>

    @POST("api/users/me/cliques-compra/{id}/resposta")
    suspend fun responderCliqueCompra(@Path("id") id: Long, @Body request: RespostaCliqueRequest)

    // --- Produtores ---

    @GET("api/produtores/{id}")
    suspend fun getProdutorDetail(@Path("id") id: Long): ProdutorDetail

    /** O produtor em destaque da semana (rotação computada; nunca vazio se houver algum produtor com bebidas). */
    @GET("api/produtores/destaque")
    suspend fun getProdutorDestaque(): ProdutorDetail

    /** As bebidas de um produtor (os mesmos cartões do catálogo), com filtro opcional por categoria; rating desc por omissão. */
    @GET("api/produtores/{id}/bebidas")
    suspend fun getBebidasDoProdutor(
        @Path("id") id: Long,
        @Query("categoriaId") categoriaId: Long? = null,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("sort") sort: String? = null,
    ): PageResponse<BebidaSummary>

    // --- Reviews ---

    @GET("api/bebidas/{bebidaId}/reviews")
    suspend fun getReviews(@Path("bebidaId") bebidaId: Long): ReviewsResponse

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

    // --- Provadas (pré-requisito para avaliar: só se pode escrever uma review de uma bebida já marcada como provada) ---

    @GET("api/users/me/provadas")
    suspend fun getProvadas(
        @Query("categoriaId") categoriaId: Long? = null,
        @Query("ano") ano: Int? = null,
    ): List<BebidaRelacao>

    @POST("api/bebidas/{bebidaId}/provada")
    suspend fun addProvada(@Path("bebidaId") bebidaId: Long)

    @DELETE("api/bebidas/{bebidaId}/provada")
    suspend fun removeProvada(@Path("bebidaId") bebidaId: Long)

    // --- Favoritos ---

    // `GET .../favoritos` e `.../wishlist` devolvem `{ bebida, data }` por item (não `BebidaSummary` direto) —
    // apanhado ao testar ao vivo (2026-09-23): sem isto, o Moshi falhava com "Required value 'id' missing" (tentava
    // ler `id` no wrapper, não em `bebida.id`), e os dois ecrãs ficavam sempre vazios/em erro.
    @GET("api/users/me/favoritos")
    suspend fun getFavoritos(): List<BebidaRelacao>

    @POST("api/bebidas/{bebidaId}/favorito")
    suspend fun addFavorito(@Path("bebidaId") bebidaId: Long)

    @DELETE("api/bebidas/{bebidaId}/favorito")
    suspend fun removeFavorito(@Path("bebidaId") bebidaId: Long)

    // --- Wishlist ---

    @GET("api/users/me/wishlist")
    suspend fun getWishlist(@Query("sort") sort: String? = null): List<BebidaRelacao>

    @POST("api/bebidas/{bebidaId}/wishlist")
    suspend fun addWishlist(@Path("bebidaId") bebidaId: Long)

    @DELETE("api/bebidas/{bebidaId}/wishlist")
    suspend fun removeWishlist(@Path("bebidaId") bebidaId: Long)

    // --- Cave virtual ---

    /** `bebidaId`: destaca (`temBebida`) as caves onde essa bebida já está — o popup "Adicionar à cave". */
    @GET("api/users/me/caves")
    suspend fun getCaves(@Query("bebidaId") bebidaId: Long? = null): List<CaveResponse>

    @POST("api/users/me/caves")
    suspend fun createCave(@Body request: CaveRequest): CaveResponse

    @GET("api/caves/{id}")
    suspend fun getCaveDetail(@Path("id") id: Long, @Query("sort") sort: String? = null): CaveDetailResponse

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

    /** "Marcar como consumida": consome UMA garrafa (decrementa a quantidade; corpo opcional). */
    @POST("api/caves/{id}/bebidas/{caveBebidaId}/consumir")
    suspend fun consumirBebida(
        @Path("id") caveId: Long,
        @Path("caveBebidaId") caveBebidaId: Long,
        @Body request: CaveConsumirRequest = CaveConsumirRequest(),
    ): CaveConsumoResponse

    @DELETE("api/caves/{id}/bebidas/{caveBebidaId}")
    suspend fun removeBebidaFromCave(
        @Path("id") caveId: Long,
        @Path("caveBebidaId") caveBebidaId: Long,
    )

    // --- Preferências (onboarding + ecrã de perfil) ---

    @GET("api/users/me/preferencias")
    suspend fun getPreferencias(): PreferenciaResponse

    @PUT("api/users/me/preferencias")
    suspend fun updatePreferencias(@Body request: PreferenciaRequest)

    // --- Lookups (públicos: as opções dos formulários) ---

    @GET("api/lookup/nacionalidades")
    suspend fun getNacionalidades(): List<Nacionalidade>

    @GET("api/lookup/avatar-categorias")
    suspend fun getAvatarCategorias(): List<LookupItem>

    /** Os 27 avatares de uma vez (a app filtra por categoria). */
    @GET("api/lookup/avatares")
    suspend fun getAvatares(): List<Avatar>

    @GET("api/lookup/categorias-bebida")
    suspend fun getCategoriasBebida(): List<LookupItem>

    /** As 277 castas de uma vez: as de destaque primeiro, o resto por ordem alfabética. */
    @GET("api/lookup/castas")
    suspend fun getCastas(): List<Casta>

    /** Países de origem da bebida (popup de filtros, "Origem") — Portugal em destaque, resto alfabético. */
    @GET("api/lookup/paises")
    suspend fun getPaisesBebida(): List<LookupItem>

    /** Regiões do país com pelo menos uma bebida (pílulas de "Origem", por baixo do país escolhido). */
    @GET("api/lookup/regioes")
    suspend fun getRegioes(@Query("paisId") paisId: Long): List<LookupItem>

    @GET("api/lookup/vinho/corpos")
    suspend fun getVinhoCorpos(): List<LookupItem>

    @GET("api/lookup/vinho/taninos")
    suspend fun getVinhoTaninos(): List<LookupItem>

    @GET("api/lookup/vinho/tipos")
    suspend fun getVinhoTipos(): List<LookupItem>
}
