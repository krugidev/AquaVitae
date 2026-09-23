package pt.aquavitae.android.data.network

import com.squareup.moshi.Moshi
import pt.aquavitae.android.data.model.ApiError
import retrofit2.HttpException
import java.io.IOException

private val apiErrorAdapter by lazy { Moshi.Builder().build().adapter(ApiError::class.java) }

/**
 * Mensagem em português para mostrar ao utilizador. As mensagens que a API escreve de propósito (401 "Username/email ou
 * password inválidos", 409 "Já existe uma conta com o email ...") passam; as de validação da API (400) são técnicas e em
 * inglês, por isso mostra-se uma frase genérica — a app valida os campos antes de enviar.
 */
fun Throwable.toUserMessage(): String = when (this) {
    is HttpException -> httpMessage()
    is IOException -> "Sem ligação ao servidor. Verifica a tua ligação e tenta novamente."
    else -> "Ocorreu um erro inesperado. Tenta novamente."
}

/** `true` se o servidor recusou o token (sessão expirada ou inválida). */
fun Throwable.isSessionInvalid(): Boolean = this is HttpException && (code() == 401 || code() == 403)

private fun HttpException.httpMessage(): String {
    val serverMessage = runCatching {
        response()?.errorBody()?.string()?.let { apiErrorAdapter.fromJson(it)?.message }
    }.getOrNull()
    return when (code()) {
        400 -> "Verifica os dados introduzidos."
        401 -> serverMessage ?: "A sessão terminou. Entra novamente."
        409 -> serverMessage ?: "Esses dados já estão em uso."
        in 500..599 -> "Ocorreu um erro no servidor. Tenta novamente daqui a pouco."
        else -> serverMessage ?: "Não foi possível concluir o pedido."
    }
}
