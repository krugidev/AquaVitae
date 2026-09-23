package pt.aquavitae.android.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import pt.aquavitae.android.feature.auth.LoginScreen
import pt.aquavitae.android.feature.auth.RegisterScreen
import pt.aquavitae.android.feature.cave.CaveScreen
import pt.aquavitae.android.feature.provadas.ProvadasScreen
import pt.aquavitae.android.feature.catalog.CatalogScreen
import pt.aquavitae.android.feature.detail.DetailScreen
import pt.aquavitae.android.feature.favoritos.FavoritosScreen
import pt.aquavitae.android.feature.home.HomeScreen
import pt.aquavitae.android.feature.loading.LoadingScreen
import pt.aquavitae.android.feature.onboarding.OnboardingScreen
import pt.aquavitae.android.feature.recovery.RecoveryScreen
import pt.aquavitae.android.feature.reviews.ReviewsScreen
import pt.aquavitae.android.feature.wishlist.WishlistScreen
import pt.aquavitae.android.ui.components.BottomNavBar
import pt.aquavitae.android.ui.components.BottomNavItem

/**
 * Grafo de navegação principal da app, seguindo a ordem do fluxo MVP:
 * Login/Registo -> Onboarding -> Home (+ Catálogo/Cave/Wishlist/Favoritos, com a barra de navegação) -> Detalhe -> Reviews.
 */
@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = AppDestinations.LOADING) {

        composable(AppDestinations.LOADING) {
            LoadingScreen(
                onLoggedIn = {
                    navController.navigate(AppDestinations.HOME) {
                        popUpTo(AppDestinations.LOADING) { inclusive = true }
                    }
                },
                onNeedsLogin = {
                    navController.navigate(AppDestinations.LOGIN) {
                        popUpTo(AppDestinations.LOADING) { inclusive = true }
                    }
                },
            )
        }

        composable(AppDestinations.LOGIN) { entry ->
            // A recuperação de password deixa aqui um aviso ao voltar (o padrão de devolver um resultado ao ecrã anterior).
            val passwordChanged by entry.savedStateHandle
                .getStateFlow(AppDestinations.KEY_PASSWORD_CHANGED, false)
                .collectAsState()
            LoginScreen(
                // Quem já tem conta vai direto para a app; o onboarding é só de quem acabou de se registar.
                onLoggedIn = {
                    navController.navigate(AppDestinations.HOME) {
                        popUpTo(AppDestinations.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(AppDestinations.REGISTER) },
                onForgotPassword = { navController.navigate(AppDestinations.RECOVER) },
                showPasswordChanged = passwordChanged,
                onPasswordChangedDismissed = { entry.savedStateHandle[AppDestinations.KEY_PASSWORD_CHANGED] = false },
            )
        }

        composable(AppDestinations.REGISTER) {
            RegisterScreen(
                onRegistered = {
                    navController.navigate(AppDestinations.ONBOARDING) {
                        popUpTo(AppDestinations.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() },
            )
        }

        composable(AppDestinations.RECOVER) {
            RecoveryScreen(
                onBack = { navController.popBackStack() },
                onPasswordChanged = {
                    // O login (o ecrã anterior) mostra o "Password alterada!" ao reaparecer.
                    navController.previousBackStackEntry?.savedStateHandle?.set(AppDestinations.KEY_PASSWORD_CHANGED, true)
                    navController.popBackStack()
                },
            )
        }

        composable(AppDestinations.ONBOARDING) {
            OnboardingScreen(
                onOnboardingComplete = {
                    navController.navigate(AppDestinations.HOME) {
                        popUpTo(AppDestinations.ONBOARDING) { inclusive = true }
                    }
                },
            )
        }

        // --- Ecrãs de topo, com a barra de navegação (BottomNavBar) sobreposta ao fundo ---

        composable(AppDestinations.HOME) {
            TabScreen(BottomNavItem.Home, navController) {
                HomeScreen(
                    onSearchClick = { navController.navigate(AppDestinations.CATALOG) },
                    onVerCaves = { navController.navigate(AppDestinations.CAVE) },
                    // A página de um produtor ainda não existe (fica para uma fatia seguinte); por agora sem destino.
                    onVerProdutor = {},
                    // Mesmo destino da pesquisa por agora: o catálogo a sério (com o filtro de categoria já aplicado) é a fatia 2b.
                    onVerSugestoes = { navController.navigate(AppDestinations.CATALOG) },
                )
            }
        }

        // Catálogo: redesenhado na fatia 2b (já com systemBarsPadding próprio, sem o invólucro LegacyScreen). O detalhe de
        // uma bebida (toque curto ou premido num cartão) é sempre o popup BebidaDetalheSheet, gerido dentro do próprio
        // ecrã — não é uma rota, por isso CatalogScreen já não recebe onBebidaClick.
        composable(AppDestinations.CATALOG) {
            TabScreen(BottomNavItem.Catalogo, navController) {
                CatalogScreen()
            }
        }

        // Os ecrãs abaixo são os do esqueleto, ainda por redesenhar (uma fatia de cada vez): estão embrulhados em
        // LegacyScreen para respeitarem as barras do sistema (edge-to-edge). Ao redesenhar um, tira-se o invólucro.

        composable(
            route = AppDestinations.DETAIL_ROUTE,
            arguments = listOf(navArgument(AppDestinations.ARG_BEBIDA_ID) { type = NavType.LongType }),
        ) {
            LegacyScreen {
                DetailScreen(
                    onNavigateToReviews = { bebidaId -> navController.navigate(AppDestinations.reviews(bebidaId)) },
                    onNavigateToCave = { navController.navigate(AppDestinations.CAVE) },
                    onNavigateToWishlist = { navController.navigate(AppDestinations.WISHLIST) },
                    onNavigateToFavoritos = { navController.navigate(AppDestinations.FAVORITOS) },
                )
            }
        }

        composable(
            route = AppDestinations.REVIEWS_ROUTE,
            arguments = listOf(navArgument(AppDestinations.ARG_BEBIDA_ID) { type = NavType.LongType }),
        ) {
            LegacyScreen { ReviewsScreen() }
        }

        // Cave: redesenhada na fatia 3b (já com systemBarsPadding próprio, sem o invólucro LegacyScreen).
        composable(AppDestinations.CAVE) {
            TabScreen(BottomNavItem.Cave, navController) {
                CaveScreen(onVerProvadas = { navController.navigate(AppDestinations.PROVADAS) })
            }
        }

        // "Já provadas": sem barra de navegação própria (não é uma das 5 abas) — alcança-se só pela Cave por agora.
        composable(AppDestinations.PROVADAS) {
            ProvadasScreen(onVoltar = { navController.popBackStack() })
        }

        composable(AppDestinations.WISHLIST) {
            TabScreen(BottomNavItem.Wishlist, navController) { LegacyScreen { WishlistScreen() } }
        }
        composable(AppDestinations.FAVORITOS) {
            TabScreen(BottomNavItem.Favoritos, navController) { LegacyScreen { FavoritosScreen() } }
        }
    }
}

/**
 * Envolve um dos 5 ecrãs de topo com a [BottomNavBar], sobreposta ao fundo do ecrã. Tocar noutro item navega para o
 * respetivo destino, preservando o estado dos separadores já visitados (`saveState`/`restoreState`, o padrão de
 * navegação por abas) — só o `HOME` fica na pilha (`popUpTo`), para "voltar" a partir de qualquer aba ir direto a ele.
 */
@Composable
private fun TabScreen(selected: BottomNavItem, navController: NavHostController, content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        content()
        BottomNavBar(
            selected = selected,
            onSelect = { item -> navegarParaAba(navController, item) },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

private fun navegarParaAba(navController: NavHostController, item: BottomNavItem) {
    val rota = when (item) {
        BottomNavItem.Home -> AppDestinations.HOME
        BottomNavItem.Catalogo -> AppDestinations.CATALOG
        BottomNavItem.Cave -> AppDestinations.CAVE
        BottomNavItem.Wishlist -> AppDestinations.WISHLIST
        BottomNavItem.Favoritos -> AppDestinations.FAVORITOS
    }
    navController.navigate(rota) {
        popUpTo(AppDestinations.HOME) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** Dá aos ecrãs do esqueleto o espaço das barras de estado e de navegação (o edge-to-edge desenha por baixo delas). */
@Composable
private fun LegacyScreen(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().systemBarsPadding()) { content() }
}
