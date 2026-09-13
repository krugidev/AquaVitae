package pt.aquavitae.android.data.model

import com.squareup.moshi.JsonClass

/**
 * Espelha o formato de página do Spring Data (`org.springframework.data.domain.Page<T>`)
 * devolvido por GET /api/bebidas. Apenas os campos usados pela app estão modelados;
 * o Moshi ignora silenciosamente quaisquer outros campos presentes no JSON
 * (ex.: "pageable", "sort", "empty").
 */
@JsonClass(generateAdapter = true)
data class PageResponse<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val number: Int,
    val size: Int,
    val last: Boolean,
)
