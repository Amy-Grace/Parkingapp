package com.example.parkingactivity.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.parkingactivity.ui.screens.auth.LoginScreen
import com.example.parkingactivity.ui.screens.auth.RegisterScreen
import com.example.parkingactivity.ui.screens.dashboard.DashboardScreen
import com.example.parkingactivity.ui.screens.facility.FacilityDetailScreen
import com.example.parkingactivity.ui.screens.map.MapScreen
import com.example.parkingactivity.ui.screens.payment.PaymentScreen
import com.example.parkingactivity.ui.screens.profile.ProfileScreen
import com.example.parkingactivity.ui.screens.session.ActiveSessionScreen

object AppDestinations {
    const val LOGIN_ROUTE = "login"
    const val REGISTER_ROUTE = "register"
    const val DASHBOARD_ROUTE = "dashboard"
    const val MAP_ROUTE = "map"
    const val FACILITY_DETAIL_ROUTE = "facility"
    const val FACILITY_DETAIL_WITH_ID = "facility/{facilityId}"
    const val ACTIVE_SESSION_ROUTE = "session"
    const val ACTIVE_SESSION_WITH_ID = "session/{sessionId}"
    const val PAYMENT_ROUTE = "payment"
    const val PAYMENT_WITH_SESSION_ID = "payment/{sessionId}"
    const val PROFILE_ROUTE = "profile"
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = AppDestinations.LOGIN_ROUTE,
    modifier: Modifier = Modifier
) {
    val actions = remember(navController) { AppNavigationActions(navController) }
    
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Auth screens
        composable(AppDestinations.LOGIN_ROUTE) {
            LoginScreen(
                onLoginSuccess = actions.navigateToDashboard,
                onRegisterClick = actions.navigateToRegister
            )
        }
        
        composable(AppDestinations.REGISTER_ROUTE) {
            RegisterScreen(
                onRegisterSuccess = actions.navigateToDashboard,
                onLoginClick = actions.navigateToLogin
            )
        }
        
        // Main app screens
        composable(AppDestinations.DASHBOARD_ROUTE) {
            DashboardScreen(
                onFacilityClick = actions.navigateToFacilityDetail,
                onMapClick = actions.navigateToMap,
                onActiveSessionClick = actions.navigateToActiveSession,
                onProfileClick = actions.navigateToProfile
            )
        }
        
        composable(AppDestinations.MAP_ROUTE) {
            MapScreen(
                onFacilityClick = actions.navigateToFacilityDetail,
                onBackClick = actions.navigateUp
            )
        }
        
        composable(
            route = AppDestinations.FACILITY_DETAIL_WITH_ID,
            arguments = listOf(navArgument("facilityId") { type = NavType.StringType })
        ) { backStackEntry ->
            val facilityId = backStackEntry.arguments?.getString("facilityId") ?: ""
            FacilityDetailScreen(
                facilityId = facilityId,
                onStartSession = { sessionId -> actions.navigateToActiveSession(sessionId) },
                onBackClick = actions.navigateUp
            )
        }
        
        composable(
            route = AppDestinations.ACTIVE_SESSION_WITH_ID,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            ActiveSessionScreen(
                sessionId = sessionId,
                onPayClick = { sessionId -> actions.navigateToPayment(sessionId) },
                onBackClick = actions.navigateUp
            )
        }
        
        composable(
            route = AppDestinations.PAYMENT_WITH_SESSION_ID,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            PaymentScreen(
                sessionId = sessionId,
                onPaymentSuccess = actions.navigateToDashboard,
                onBackClick = actions.navigateUp
            )
        }
        
        composable(AppDestinations.PROFILE_ROUTE) {
            ProfileScreen(
                onLogout = actions.navigateToLogin,
                onBackClick = actions.navigateUp
            )
        }
    }
}

class AppNavigationActions(private val navController: NavHostController) {
    val navigateToLogin: () -> Unit = {
        navController.navigate(AppDestinations.LOGIN_ROUTE) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }
    
    val navigateToRegister: () -> Unit = {
        navController.navigate(AppDestinations.REGISTER_ROUTE)
    }
    
    val navigateToDashboard: () -> Unit = {
        navController.navigate(AppDestinations.DASHBOARD_ROUTE) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }
    
    val navigateToMap: () -> Unit = {
        navController.navigate(AppDestinations.MAP_ROUTE)
    }
    
    val navigateToFacilityDetail: (String) -> Unit = { facilityId ->
        navController.navigate("facility/$facilityId")
    }
    
    val navigateToActiveSession: (String) -> Unit = { sessionId ->
        navController.navigate("session/$sessionId")
    }
    
    val navigateToPayment: (String) -> Unit = { sessionId ->
        navController.navigate("payment/$sessionId")
    }
    
    val navigateToProfile: () -> Unit = {
        navController.navigate(AppDestinations.PROFILE_ROUTE)
    }
    
    val navigateUp: () -> Unit = {
        navController.navigateUp()
    }
} 