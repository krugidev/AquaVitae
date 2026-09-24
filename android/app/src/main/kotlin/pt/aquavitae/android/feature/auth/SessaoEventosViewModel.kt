package pt.aquavitae.android.feature.auth

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import pt.aquavitae.android.data.network.SessionEvents
import javax.inject.Inject

/**
 * Dá ao `AppNavHost` acesso ao [SessionEvents] (o Hilt não injeta num `@Composable`): é por aqui que ele sabe que a sessão
 * morreu a meio da utilização e leva ao login, e que o login mostra "A tua sessão expirou".
 */
@HiltViewModel
class SessaoEventosViewModel @Inject constructor(
    val eventos: SessionEvents,
) : ViewModel()
