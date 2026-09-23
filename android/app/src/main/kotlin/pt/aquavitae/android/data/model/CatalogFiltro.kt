package pt.aquavitae.android.data.model

/**
 * Os filtros do popup "Filtros" do catálogo — espelha `BebidaFiltro` do backend (ver `API_ENDPOINTS.md`), um campo por
 * cada query param de `GET /api/bebidas`. `categoriaId` é só um id (não uma lista): o popup mostra as categorias como
 * escolha única, ao contrário da homepage. Os atributos de vinho (`acidezMin/Max`, ...) só se aplicam com "Vinho"
 * escolhido — o ecrã esconde essa secção nas outras categorias, mas os valores ficam guardados (não se perdem ao trocar
 * de categoria e voltar).
 */
data class CatalogFiltro(
    val search: String? = null,
    val categoriaId: Long? = null,
    val paisId: Long? = null,
    val regiaoIds: Set<Long> = emptySet(),
    val ratingMin: Double? = null,
    val acidezMin: Int? = null,
    val acidezMax: Int? = null,
    val docuraMin: Int? = null,
    val docuraMax: Int? = null,
    val corpoId: Long? = null,
    val taninoId: Long? = null,
    val tipoId: Long? = null,
    val castaIds: Set<Long> = emptySet(),
    val precoMin: Double? = null,
    val precoMax: Double? = null,
) {
    /** Nº de filtros ativos além da categoria (para o "LIMPAR X" do popup — a categoria não conta, é sempre uma). */
    val totalAtivos: Int
        get() = listOfNotNull(
            paisId?.takeIf { it != PAIS_PORTUGAL_ID },
            ratingMin,
            acidezMin, acidezMax,
            docuraMin, docuraMax,
            corpoId, taninoId, tipoId,
            precoMin, precoMax,
        ).size + regiaoIds.size + castaIds.size

    /** Limpa tudo menos a categoria escolhida (o popup mantém sempre uma categoria ativa). */
    fun limpo() = CatalogFiltro(categoriaId = categoriaId)

    companion object {
        const val PAIS_PORTUGAL_ID = 1L
    }
}
