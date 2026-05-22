package com.pham0326.flinders.zootreasurehunt

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.pham0326.flinders.zootreasurehunt.model.Sighting
import com.pham0326.flinders.zootreasurehunt.navigation.AboutDestination
import com.pham0326.flinders.zootreasurehunt.navigation.BottomNavItem
import com.pham0326.flinders.zootreasurehunt.navigation.HomeDestination
import com.pham0326.flinders.zootreasurehunt.navigation.SettingsDestination
import com.pham0326.flinders.zootreasurehunt.ui.components.EditSightingDialog
import com.pham0326.flinders.zootreasurehunt.ui.screens.AboutScreen
import com.pham0326.flinders.zootreasurehunt.ui.screens.ListScreen
import com.pham0326.flinders.zootreasurehunt.ui.screens.SettingsScreen
import com.pham0326.flinders.zootreasurehunt.ui.theme.ZooTreasureHuntTheme
import com.pham0326.flinders.zootreasurehunt.viewmodel.ZooUiEvent
import com.pham0326.flinders.zootreasurehunt.viewmodel.ZooViewModel
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ZooTreasureHuntTheme {
                ZooApp()
            }
        }
    }
}

@Composable
fun ZooApp() {
    val navController = rememberNavController()
    val viewModel: ZooViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var currentAnimal by remember { mutableStateOf<Sighting?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && imageUri != null && currentAnimal != null) {
            viewModel.updateCapturedImage(currentAnimal!!.name, imageUri.toString())
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        val uri = imageUri
        if (granted && uri != null) cameraLauncher.launch(uri)
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    val activityPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activityPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is ZooUiEvent.SightingUpdated -> {
                    snackbarHostState.showSnackbar("Sighting updated")
                }

                is ZooUiEvent.SightingDeleted -> {
                    val result = snackbarHostState.showSnackbar(
                        message = "Sighting deleted",
                        actionLabel = "UNDO",
                        duration = SnackbarDuration.Long
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoDelete(event.sighting)
                    }
                }

                is ZooUiEvent.FilterClearedByShake -> {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    snackbarHostState.showSnackbar("Filter cleared by shaking phone")
                }

                is ZooUiEvent.NocturnalModeChanged -> {
                    snackbarHostState.showSnackbar(
                        if (event.isNocturnal) {
                            "Nocturnal House detected - rewards paused"
                        } else {
                            "Bright Area detected - safari tracking resumed"
                        }
                    )
                }
            }
        }
    }

    fun createImageUri(): Uri {
        val imageFolder = File(context.cacheDir, "captured_images")
        imageFolder.mkdirs()
        val file = File(imageFolder, "animal_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    val bottomItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Settings,
        BottomNavItem.About
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(currentRoute) {
        currentRoute?.let { Log.d("ZooNavigation", "Current screen: $it") }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
                            val destination = when (item) {
                                BottomNavItem.Home -> HomeDestination
                                BottomNavItem.Settings -> SettingsDestination
                                BottomNavItem.About -> AboutDestination
                            }
                            navController.navigate(destination) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
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
                    searchQuery = uiState.searchQuery,
                    stepCount = uiState.stepCount,
                    currentLux = uiState.currentLux,
                    isNocturnalMode = uiState.isNocturnalMode,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    onEditClick = { animal -> viewModel.selectSightingForEdit(animal) },
                    onDelete = { animal -> viewModel.deleteSighting(animal) },
                    onCaptureClick = { animal ->
                        currentAnimal = animal
                        val uri = createImageUri()
                        imageUri = uri
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
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
                    onDismiss = { viewModel.dismissDialog() },
                    onSave = { updatedSighting ->
                        viewModel.updateSighting(updatedSighting)
                        viewModel.dismissDialog()
                    }
                )
            }
        }
    }
}