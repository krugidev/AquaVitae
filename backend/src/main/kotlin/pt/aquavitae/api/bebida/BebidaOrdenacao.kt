package pt.aquavitae.api.bebida

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

// Sem ORDER BY a paginação não é determinística (o "carregar mais 15" podia repetir ou saltar bebidas), por
// isso, se o cliente não pedir ordenação, usa-se a do mockup: melhor rating primeiro, depois nome. O id fica
// sempre no fim como desempate — também quando o cliente escolhe o `sort` (ex.: só por nome, com nomes iguais).
fun Pageable.comOrdenacaoPadraoDeBebidas(): Pageable {
    val ordem = if (sort.isUnsorted) Sort.by(Sort.Order.desc("ratingMedio"), Sort.Order.asc("nome")) else sort
    val comDesempate = if (ordem.getOrderFor("id") == null) ordem.and(Sort.by(Sort.Order.asc("id"))) else ordem
    return PageRequest.of(pageNumber, pageSize, comDesempate)
}
