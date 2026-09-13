package pt.aquavitae.android.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import pt.aquavitae.android.feature.auth.LoginScreen
import pt.aquavitae.android.feature.auth.RegisterScreen
import pt.aquavitae.android.feature.cave.CaveScreen
import pt.aquavitae.android.feature.catalog.CatalogScreen
import pt.aquavitae.android.feature.detail.DetailScreen
import pt.aquavitae.android.feature.favoritos.FavoritosScreen
import pt.aquavitae.android.feature.onboarding.OnboardingScreen
import pt.aquavitae.android.feature.reviews.ReviewsScreen
import pt.aquavitae.android.feature.wishlist.WishlistScreen

/**
 * Grafo de navegação principal da app, seguindo a ordem do fluxo MVP:
 * Login/Registo -> Onboarding -> Catálogo -> Detalhe -> Reviews -> Cave/Wishlist/Favoritos.
 */
@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = AppDestinations.LOGIN) {

        composable(AppDestinations.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(AppDestinations.ONBOARDING) {
                        popUpTo(AppDestinations.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(AppDestinations.REGISTER) },
            )
        }

        composable(AppDestinations.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(AppDestinations.ONBOARDING) {
                        popUpTo(AppDestinations.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() },
            )
        }

        composable(AppDestinations.ONBOARDING) {
            OnboardingScreen(
                onOnboardingComplete = {
                    navController.navigate(AppDestinations.CATALOG) {
                        popUpTo(AppDestinations.ONBOARDING) { inclusive = true }
                    }
                },
            )
        }

        composable(AppDestinations.CATALOG) {
            CatalogScreen(
                onBebidaClick = { bebidaId -> navController.navigate(AppDestinations.detail(bebidaId)) },
            )
        }

        composable(
            route = AppDestinations.DETAIL_ROUTE,
            arguments = listOf(navArgument(AppDestinations.ARG_BEBIDA_ID) { type = NavType.LongType }),
        ) {
            DetailScreen(
                onNavigateToReviews = { bebidaId -> navController.navigate(AppDestinations.reviews(bebidaId)) },
                onNavigateToCave = { navController.navigate(AppDestinations.CAVE) },
                onNavigateToWishlist = { navController.navigate(AppDestinations.WISHLIST) },
                onNavigateToFavoritos = { navController.navigate(AppDestinations.FAVORITOS) },
            )
        }

        composable(
            route = AppDestinations.REVIEWS_ROUTE,
            arguments = listOf(navArgument(AppDestinations.ARG_BEBIDA_ID) { type = NavType.LongType }),
        ) {
            ReviewsScreen()
        }

        composable(AppDestinations.CAVE) { CaveScreen() }
        composable(AppDestinations.WISHLIST) { WishlistScreen() }
        composable(AppDestinations.FAVORITOS) { FavoritosScreen() }
    }
}
