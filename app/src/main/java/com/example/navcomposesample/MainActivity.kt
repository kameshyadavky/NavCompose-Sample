package com.example.navcomposesample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.example.navcomposesample.ui.theme.NavComposeSampleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NavComposeSampleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Home()
                }
            }
        }
    }
}

@Composable
fun Home() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            val currentSelectedScreen by navController.currentScreenAsState()
            HomeBottomNavigation(
                selectedNavigation = currentSelectedScreen,
                onNavigationSelected = {
                    navController.navigate(it.route) {
                        launchSingleTop = true
                        restoreState = true

                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                    }
                }
            )
        }
    ) {
        AppNavigation(navHostController = navController)
    }
}

@Composable
fun AppNavigation(
    navHostController: NavHostController
) {
    NavHost(
        navController = navHostController,
        startDestination = Screen.FirstScreen.route
    ) {
        composable(route = Screen.FirstScreen.route) {
            ComposeScreen(text = Screen.FirstScreen.route) {}
        }

        navigation(
            startDestination = Screen.FirstScreen.route + "/second",
            route = Screen.SecondScreenGraph.route
        ) {
            composable(route = Screen.FirstScreen.route + "/second") {
                val graphEntry = remember {
                    navHostController.getBackStackEntry(Screen.SecondScreenGraph.route)
                }
                val viewmodel = viewModel<GraphViewModel>(graphEntry)
                viewmodel.count = 1
                ComposeScreen(text = Screen.FirstScreen.route + "/second") {
                    navHostController.navigate(Screen.SecondScreen.route)
                }
            }

            composable(route = Screen.SecondScreen.route) {
                val graphEntry = remember {
                    navHostController.getBackStackEntry(Screen.SecondScreenGraph.route)
                }

                val graphViewModel = viewModel<GraphViewModel>(graphEntry)
                val viewModel = viewModel<GraphViewModel>()
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    viewModel.count = 1
                    Text(
                        text = Screen.SecondScreen.route,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun ComposeScreen(text: String, onClick: () -> Unit) {
    Box(modifier = Modifier
        .fillMaxSize()
        .clickable {
            onClick()
        }) {
        Text(text = text, modifier = Modifier.align(Alignment.Center))
    }
}


@Composable
fun HomeBottomNavigation(
    selectedNavigation: Screen,
    onNavigationSelected: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    BottomNavigation(
        modifier = modifier
    ) {
        bottomNavItems.forEach {
            BottomNavigationItem(
                selected = selectedNavigation == it,
                onClick = { onNavigationSelected(it) },
                label = { Text(text = it.route) },
                icon = {}
            )
        }
    }
}

sealed class Screen(val route: String) {
    object FirstScreen : Screen("first")
    object SecondScreenGraph : Screen("second_graph")
    object SecondScreen : Screen("second")
}

val bottomNavItems = listOf(Screen.FirstScreen, Screen.SecondScreenGraph)


@Stable
@Composable
private fun NavController.currentScreenAsState(): State<Screen> {
    val selectedItem = remember { mutableStateOf<Screen>(Screen.FirstScreen) }

    DisposableEffect(this) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            when {
                destination.hierarchy.any { it.route == Screen.FirstScreen.route } -> {
                    selectedItem.value = Screen.FirstScreen
                }
                destination.hierarchy.any { it.route == Screen.SecondScreenGraph.route } -> {
                    selectedItem.value = Screen.SecondScreenGraph
                }
            }
        }
        addOnDestinationChangedListener(listener)

        onDispose {
            removeOnDestinationChangedListener(listener)
        }
    }

    return selectedItem
}