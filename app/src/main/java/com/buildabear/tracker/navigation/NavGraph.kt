package com.buildabear.tracker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.buildabear.tracker.data.repository.ActiveFilterStore
import com.buildabear.tracker.ui.custom.CustomBearFormScreen
import com.buildabear.tracker.ui.detail.BearDetailScreen
import com.buildabear.tracker.ui.filters.FilterBuilderScreen
import com.buildabear.tracker.ui.filters.SavedFiltersScreen
import com.buildabear.tracker.ui.list.BearListScreen
import com.buildabear.tracker.ui.list.BearListViewModel
import com.buildabear.tracker.ui.settings.SettingsScreen

@Composable
fun BearTrackerNavGraph(
    navController: NavHostController,
    activeFilterStore: ActiveFilterStore,
) {
    NavHost(navController = navController, startDestination = Routes.LIST) {
        composable(Routes.LIST) { backStackEntry ->
            val listViewModel: BearListViewModel = hiltViewModel(backStackEntry)
            LaunchedEffect(backStackEntry) {
                activeFilterStore.consumeSelection()?.let { (name, criteria) ->
                    listViewModel.applyView(name, criteria)
                }
            }
            BearListScreen(
                onBearClick = { navController.navigate(Routes.detail(it)) },
                onAddCustom = { navController.navigate(Routes.CUSTOM_NEW) },
                onFiltersClick = { navController.navigate(Routes.FILTERS) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                viewModel = listViewModel,
            )
        }

        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("bearId") { type = NavType.StringType }),
        ) {
            BearDetailScreen(
                onBack = { navController.popBackStack() },
                onEditCustom = { navController.navigate(Routes.customEdit(it)) },
            )
        }

        composable(Routes.CUSTOM_NEW) {
            CustomBearFormScreen(
                onBack = { navController.popBackStack() },
                onSaved = { bearId ->
                    navController.popBackStack()
                    navController.navigate(Routes.detail(bearId))
                },
            )
        }

        composable(
            route = Routes.CUSTOM_EDIT,
            arguments = listOf(navArgument("bearId") { type = NavType.StringType }),
        ) {
            CustomBearFormScreen(
                onBack = { navController.popBackStack() },
                onSaved = { bearId ->
                    navController.popBackStack()
                    navController.navigate(Routes.detail(bearId)) {
                        popUpTo(Routes.detail(bearId)) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.FILTERS) {
            SavedFiltersScreen(
                onBack = { navController.popBackStack() },
                onCreateFilter = {
                    navController.navigate(Routes.filterEdit())
                },
                onEditFilter = { filterId ->
                    navController.navigate(Routes.filterEdit(filterId))
                },
                onApplyFilter = { name, criteria ->
                    activeFilterStore.setSelection(name, criteria)
                    navController.popBackStack()
                },
            )
        }

        composable(
            route = Routes.FILTER_EDIT,
            arguments = listOf(
                navArgument("filterId") {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) {
            FilterBuilderScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
