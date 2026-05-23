package com.pham0326.flinders.zootreasurehunt
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
import com.pham0326.flinders.zootreasurehunt.viewmodel.ProximityResult
import com.pham0326.flinders.zootreasurehunt.viewmodel.ZooUiEvent
import com.pham0326.flinders.zootreasurehunt.viewmodel.ZooViewModel
import android.Manifest
import android.app.Activity
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ZooApp()
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
    val coroutineScope = rememberCoroutineScope()
    DisposableEffect(uiState.isNocturnalMode) {
        val window = (context as? Activity)?.window
        val originalBrightness =
            window?.attributes?.screenBrightness
                ?: WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE

        window?.let { w ->
            val params = w.attributes
            params.screenBrightness = if (uiState.isNocturnalMode) {
                0.3f
            } else {
                WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            }
            w.attributes = params
        }

        onDispose {
            window?.let { w ->
                val params = w.attributes
                params.screenBrightness = originalBrightness
                w.attributes = params
            }
        }
    }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var currentAnimal by remember { mutableStateOf<Sighting?>(null) }
    var tooFarDialog by remember { mutableStateOf<ProximityResult.TooFar?>(null) }

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

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.startLocationUpdates()
    }

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
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
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
                is ZooUiEvent.PedometerReset -> {
                    snackbarHostState.showSnackbar("Pedometer reset to 0 steps")
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
                            "Bright area detected - safari tracking resumed"
                        }
                    )
                }
            }
        }
    }

    fun createImageUri(): Uri {
        val imageFolder = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
            ?: throw IllegalStateException("External files dir unavailable")
        imageFolder.mkdirs()
        val file = File(imageFolder, "animal_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun attemptCapture(animal: Sighting) {
        when (val result = viewModel.checkProximity(animal)) {
            is ProximityResult.Allowed,
            is ProximityResult.NoCoordinates -> {
                currentAnimal = animal
                val uri = createImageUri()
                imageUri = uri
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            is ProximityResult.TooFar -> {
                tooFarDialog = result
            }
            is ProximityResult.PermissionDenied -> {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            is ProximityResult.NoLocationYet -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        "Acquiring GPS… please try again in a moment"
                    )
                }
            }
        }
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

    ZooTreasureHuntTheme(darkTheme = uiState.isNocturnalMode) {
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
                        onCaptureClick = { animal -> attemptCapture(animal) }
                    )
                }

                composable<SettingsDestination> {
                    SettingsScreen(
                        isSortByName = uiState.isSortByName,
                        stepCount = uiState.stepCount,
                        currentLux = uiState.currentLux,
                        isNocturnalMode = uiState.isNocturnalMode,
                        isLightSensorAvailable = uiState.isLightSensorAvailable,
                        isStepCounterAvailable = uiState.isStepCounterAvailable,
                        isLocationAvailable = uiState.isLocationAvailable,
                        onSortChange = { viewModel.toggleSortOrder(it) },
                        onResetPedometer = { viewModel.resetPedometer() }
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
                        },
                        onRequestCapture = { animalToCapture ->
                            attemptCapture(animalToCapture)
                        }
                    )
                }
            }
            tooFarDialog?.let { result ->
                ProximityTooFarDialog(
                    animalName = result.animalName,
                    distanceMetres = result.distanceMetres,
                    onDismiss = { tooFarDialog = null }
                )
            }
        }
    }
}

@Composable
private fun ProximityTooFarDialog(
    animalName: String,
    distanceMetres: Float,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("You're too far away") },
        text = {
            Column {
                Text("You are too far from the $animalName enclosure.")
                Text(
                    text = "Distance: ${distanceMetres.toInt()} m  (must be within 50 m)",
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}