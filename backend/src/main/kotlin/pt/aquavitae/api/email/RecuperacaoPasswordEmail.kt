package pt.aquavitae.api.email

import org.springframework.web.util.HtmlUtils

/** Um email pronto a enviar: o assunto e duas versões do corpo (o programa de email mostra a HTML e, se não puder, a de texto). */
data class EmailComposto(val assunto: String, val texto: String, val html: String)

/**
 * O email com o código de recuperação de password. Escrito em português de Portugal, no tom da app ("tu"). Sem imagens nem
 * ligações: não há nada em que clicar (menos superfície para phishing) e o código vem sempre no corpo. O nome vem do
 * utilizador, por isso vai escapado na versão HTML (um username como `<b>` não pode partir o email nem injetar HTML).
 */
object RecuperacaoPasswordEmail {

    private const val GRENA = "#8F321D"
    private const val TINTA = "#1B1714"

    fun compor(nome: String?, codigo: String, validadeMinutos: Long): EmailComposto {
        val saudacao = nome?.trim()?.takeIf { it.isNotEmpty() }?.let { "Olá $it," } ?: "Olá,"
        val assunto = "O teu código de recuperação da AquaVitae"

        val texto = """
            $saudacao

            Recebemos um pedido para recuperar a password da tua conta AquaVitae.

            O teu código é: $codigo

            É válido durante $validadeMinutos minutos e só pode ser usado uma vez. Escreve-o na app para escolheres uma password nova.
            Não partilhes este código com ninguém.

            Se não foste tu a pedir, ignora este email: a tua password não foi alterada.

            AquaVitae
        """.trimIndent()

        val saudacaoHtml = escapar(saudacao)
        val codigoHtml = escapar(codigo)
        val html = """
            <!DOCTYPE html>
            <html lang="pt-PT">
            <head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>${escapar(assunto)}</title></head>
            <body style="margin:0;padding:0;background:#FFFFFF;">
            <span style="display:none;max-height:0;overflow:hidden;opacity:0;">O teu código: $codigoHtml (válido $validadeMinutos minutos)</span>
            <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background:#FFFFFF;">
              <tr><td align="center" style="padding:32px 16px;">
                <table role="presentation" width="480" cellpadding="0" cellspacing="0" style="max-width:480px;width:100%;background:#F8F8F8;border-radius:24px;font-family:Arial,Helvetica,sans-serif;color:$TINTA;">
                  <tr><td align="center" style="padding:28px 24px 8px;font-size:22px;letter-spacing:3px;color:$TINTA;">AQUA<span style="color:$GRENA;font-weight:300;">VITAE</span></td></tr>
                  <tr><td style="padding:8px 32px 0;font-size:16px;line-height:24px;">
                    <p style="margin:16px 0 8px;">$saudacaoHtml</p>
                    <p style="margin:0 0 16px;">Recebemos um pedido para recuperar a password da tua conta AquaVitae. O teu código é:</p>
                  </td></tr>
                  <tr><td align="center" style="padding:0 32px;">
                    <div style="background:#FFFFFF;border:2px solid $GRENA;border-radius:16px;padding:16px 8px;font-size:34px;font-weight:bold;letter-spacing:10px;color:$GRENA;">$codigoHtml</div>
                  </td></tr>
                  <tr><td style="padding:16px 32px 0;font-size:14px;line-height:21px;">
                    <p style="margin:0 0 12px;">É válido durante <strong>$validadeMinutos minutos</strong> e só pode ser usado uma vez. Escreve-o na app para escolheres uma password nova. Não partilhes este código com ninguém.</p>
                    <p style="margin:0 0 8px;color:#6D5A5F;">Se não foste tu a pedir, ignora este email: a tua password não foi alterada.</p>
                  </td></tr>
                  <tr><td style="padding:8px 32px 28px;font-size:12px;color:#6D5A5F;">AquaVitae</td></tr>
                </table>
              </td></tr>
            </table>
            </body>
            </html>
        """.trimIndent()

        return EmailComposto(assunto = assunto, texto = texto, html = html)
    }

    // Só escapa os caracteres especiais do HTML (< > & " '): com UTF-8 os acentos ficam como são, em vez de &aacute; e afins.
    private fun escapar(texto: String): String = HtmlUtils.htmlEscape(texto, "UTF-8")
}
