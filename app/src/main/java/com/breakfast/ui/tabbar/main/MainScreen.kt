import androidx.compose.ui.res.painterResource

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.breakfast.ui.tabbar.notifications.NotificationScreen
import com.breakfast.ui.tabbar.history.HistoryScreen
import com.breakfast.ui.tabbar.history.HistoryDetailScreen
import com.breakfast.ui.settings.SettingsScreen
import com.breakfast.ui.order.OrderDetailsScreen
import com.breakfast.ui.order.OrderClosedScreen
import com.breakfast.ui.order.CollectorDetailsScreen
import com.breakfast.ui.order.AssignItemScreen
import androidx.compose.material3.Scaffold
import androidx.compose.ui.res.colorResource
import com.breakfast.ui.order.CustomItemScreen

/**
 * Represents each tab in the bottom navigation bar with its route, icon and label.
 */
sealed class BottomNavItem(val route: String, val icon: Int, val labelRes: Int) {
    object Home : BottomNavItem("home", com.breakfast.R.drawable.home_tab_bar, com.breakfast.R.string.home_screen_title)
    object Notifications : BottomNavItem("notifications", com.breakfast.R.drawable.history_tab_bar, com.breakfast.R.string.notifications)
    object History : BottomNavItem("history", com.breakfast.R.drawable.notification_tab_bar, com.breakfast.R.string.history)
    object Profile : BottomNavItem("profile", com.breakfast.R.drawable.profile_tab_bar, com.breakfast.R.string.profile)
}

@Composable
private fun BreakfastBottomBar(
    navController: NavController,
    items: List<BottomNavItem>
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = currentDestination?.route == item.route

                val containerModifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .then(
                        if (selected) {
                            Modifier.background(
                                Brush.horizontalGradient(
                                    listOf(
                                        colorResource(id = com.breakfast.R.color.blue_ribbon),
                                        colorResource(id = com.breakfast.R.color.governor_bay),
                                    )
                                )
                            )
                        } else {
                            Modifier.border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                shape = CircleShape
                            )
                        }
                    )

                Box(
                    modifier = containerModifier
                        .clickable {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = stringResource(id = item.labelRes),
                        tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(rootNavController: NavController? = null) {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Notifications,
        BottomNavItem.History,
        BottomNavItem.Profile
    )
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val hideBottomBarPrefixes = listOf(
        "add_to_order",
        "custom_item",
        "order_details",
        "order_closed",
        "assign_item",
        "history_detail",
        "collector_details"
    )
    val shouldHideBottomBar = currentRoute != null && hideBottomBarPrefixes.any { prefix ->
        currentRoute.startsWith(prefix)
    }
    Scaffold(
        bottomBar = {
            if (!shouldHideBottomBar) {
                BreakfastBottomBar(navController = navController, items = items)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Home.route) { HomeScreen(navController) }
            composable(BottomNavItem.Notifications.route) { NotificationScreen(navController) }
            composable(BottomNavItem.History.route) { HistoryScreen(navController) }
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(rootNavController ?: navController)
            }

            // Order-related screens with dynamic arguments
            composable(
                route = "order_details/{orderId}/{storeId}",
                arguments = listOf(
                    navArgument("orderId") { type = NavType.IntType },
                    navArgument("storeId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getInt("orderId") ?: 0
                val storeId = backStackEntry.arguments?.getInt("storeId") ?: 0
                OrderDetailsScreen(
                    orderId = orderId,
                    onAddItem = { navController.navigate("add_to_order/${orderId}/${storeId}") },
                    onClose = { navController.popBackStack() },
                    navController = navController
                )
            }
            composable(
                route = "add_to_order/{orderId}/{storeId}",
                arguments = listOf(
                    navArgument("orderId") { type = NavType.IntType },
                    navArgument("storeId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getInt("orderId")
                val storeId = backStackEntry.arguments?.getInt("storeId") ?: 0
                AddToOrderScreen(orderId = orderId, storeId = storeId)
            }
            composable(
                route = "custom_item/{orderId}",
                arguments = listOf(navArgument("orderId") { type = NavType.IntType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getInt("orderId") ?: 0
                CustomItemScreen(orderId = orderId, onCreate = { navController.popBackStack() })
            }
            composable(
                route = "order_closed/{orderId}",
                arguments = listOf(navArgument("orderId") { type = NavType.IntType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getInt("orderId") ?: 0
                OrderClosedScreen(orderId = orderId, onClose = { navController.popBackStack() }, onReopen = { navController.popBackStack() })
            }
            composable("collector_details") { CollectorDetailsScreen() }

            // History detail screen
            composable(
                route = "history_detail/{orderId}",
                arguments = listOf(navArgument("orderId") { type = NavType.IntType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getInt("orderId") ?: 0
                HistoryDetailScreen(orderId = orderId, navController = navController)
            }

            // Assign item screen for assigning an order item to a user
            composable(
                route = "assign_item/{orderId}/{orderItemId}",
                arguments = listOf(
                    navArgument("orderId") { type = NavType.IntType },
                    navArgument("orderItemId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val orderIdArg = backStackEntry.arguments?.getInt("orderId") ?: 0
                val orderItemId = backStackEntry.arguments?.getInt("orderItemId") ?: 0
                AssignItemScreen(orderId = orderIdArg, orderItemId = orderItemId, navController = navController)
            }

            // Settings screen for version, about us and language selection
            composable("settings") {
                SettingsScreen(navController = navController)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "MainScreen Preview")
@Composable
private fun MainScreenPreview() {
    MainScreen()
}