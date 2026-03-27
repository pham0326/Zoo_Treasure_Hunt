package com.pham0326.flinders.zootreasurehunt

import android.Manifest
import android.app.Application
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pham0326.flinders.zootreasurehunt.data.FileSightingRepository
import com.pham0326.flinders.zootreasurehunt.data.SettingsRepository
import com.pham0326.flinders.zootreasurehunt.data.SightingRepository
import com.pham0326.flinders.zootreasurehunt.navigation.AboutDestination
import com.pham0326.flinders.zootreasurehunt.navigation.BottomNavItem
import com.pham0326.flinders.zootreasurehunt.navigation.HomeDestination
import com.pham0326.flinders.zootreasurehunt.navigation.SettingsDestination
import com.pham0326.flinders.zootreasurehunt.ui.components.EditSightingDialog
import com.pham0326.flinders.zootreasurehunt.ui.screens.AboutScreen
import com.pham0326.flinders.zootreasurehunt.ui.screens.ListScreen
import com.pham0326.flinders.zootreasurehunt.ui.screens.SettingsScreen
import com.pham0326.flinders.zootreasurehunt.ui.theme.ZooTreasureHuntTheme
import com.pham0326.flinders.zootreasurehunt.viewmodel.ZooViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = FileSightingRepository(applicationContext)
        val settingsRepository = SettingsRepository(applicationContext)

        setContent {
            ZooTreasureHuntTheme {
                ZooApp(
                    repository = repository,
                    settingsRepository = settingsRepository
                )
            }
        }
    }
}

@Composable
fun ZooApp(
    repository: SightingRepository,
    settingsRepository: SettingsRepository
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val viewModel: ZooViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ZooViewModel(
                    repository = repository,
                    settingsRepository = settingsRepository,
                    application = context.applicationContext as Application
                ) as T
            }
        }
    )
    val uiState by viewModel.uiState.collectAsState()

    val bottomItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Settings,
        BottomNavItem.About
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { }
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                bottomItems.forEach { item ->
                    val isSelected = when (item) {
                        BottomNavItem.Home ->
                            currentRoute?.contains("HomeDestination") == true
                        BottomNavItem.Settings ->
                            currentRoute?.contains("SettingsDestination") == true
                        BottomNavItem.About ->
                            currentRoute?.contains("AboutDestination") == true
                    }

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            when (item) {
                                BottomNavItem.Home -> {
                                    navController.navigate(HomeDestination) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                                BottomNavItem.Settings -> {
                                    navController.navigate(SettingsDestination) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                                BottomNavItem.About -> {
                                    navController.navigate(AboutDestination) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label
                            )
                        },
                        label = {
                            Text(
                                text = when (item) {
                                    BottomNavItem.Home -> stringResource(R.string.home_tab)
                                    BottomNavItem.Settings -> stringResource(R.string.settings_tab)
                                    BottomNavItem.About -> stringResource(R.string.about_tab)
                                }
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<HomeDestination> {
                ListScreen(
                    sightings = uiState.sightings,
                    onEditClick = { animal ->
                        viewModel.selectSightingForEdit(animal)
                    },
                    onDelete = { animal ->
                        viewModel.deleteSighting(animal)
                    }
                )
            }

            composable<SettingsDestination> {
                SettingsScreen(
                    isSortByName = uiState.isSortByName,
                    onSortChange = { viewModel.toggleSortOrder(it) }
                )
            }

            composable<AboutDestination> {
                AboutScreen()
            }
        }

        if (uiState.isDialogVisible) {
            uiState.selectedSighting?.let { sighting ->
                EditSightingDialog(
                    sighting = sighting,
                    onDismiss = {
                        viewModel.dismissDialog()
                    },
                    onSave = { updatedSighting ->
                        viewModel.updateSighting(updatedSighting)
                        viewModel.dismissDialog()
                    }
                )
            }
        }
    }
}

