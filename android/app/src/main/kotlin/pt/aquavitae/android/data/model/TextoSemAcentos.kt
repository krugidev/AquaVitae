package pt.aquavitae.android.data.model

import java.text.Normalizer

private val MarcasCombinadas = Regex("\\p{Mn}+")

/**
 * O texto sem acentos ("Esporão" → "Esporao", "Château" → "Chateau"): decompõe cada letra (Unicode NFD) e tira as marcas
 * combinadas (til, agudo, cedilha...). Serve as pesquisas que a app faz **localmente** (as castas); a pesquisa do catálogo é
 * no backend, que faz o mesmo (`PesquisaTexto`).
 */
fun String.semAcentos(): String = Normalizer.normalize(this, Normalizer.Form.NFD).replace(MarcasCombinadas, "")

/** "Contém", sem distinguir maiúsculas nem acentos: escrever "aragonez" ou "ARAGONEZ" encontra "Aragonez", e "tinta" encontra "Tíntá". */
fun String.contemSemAcentos(busca: String): Boolean = semAcentos().contains(busca.semAcentos(), ignoreCase = true)
