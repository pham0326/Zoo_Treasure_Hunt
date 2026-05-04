package com.pham0326.flinders.zootreasurehunt

import android.Manifest
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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
import com.pham0326.flinders.zootreasurehunt.viewmodel.ZooUiEvent
import dagger.hilt.android.AndroidEntryPoint
import android.util.Log
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.launch
import kotlin.math.sqrt

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
    val viewModel = viewModel<ZooViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var currentAnimal by remember { mutableStateOf<com.pham0326.flinders.zootreasurehunt.model.Sighting?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val hapticFeedback = LocalHapticFeedback.current

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && imageUri != null && currentAnimal != null) {
            viewModel.updateCapturedImage(
                currentAnimal!!.name,
                imageUri.toString()
            )
        }
    }

    val bottomItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Settings,
        BottomNavItem.About
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        val uri = imageUri
        if (granted && uri != null) {
            cameraLauncher.launch(uri)
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

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

    LaunchedEffect(currentRoute) {
        currentRoute?.let { route ->
            Log.d("ZooNavigation", "Current screen: $route")
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            cameraPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is ZooUiEvent.SightingUpdated -> {
                    snackbarHostState.showSnackbar(
                        message = "Sighting updated"
                    )
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
            }
        }
    }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        var lastShakeTime = 0L

        val sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return

                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val acceleration = sqrt(x * x + y * y + z * z)
                val now = System.currentTimeMillis()

                if (acceleration > 22 && now - lastShakeTime > 1000) {
                    lastShakeTime = now

                    if (searchQuery.isNotEmpty()) {
                        searchQuery = ""
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)

                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                            snackbarHostState.showSnackbar("Filter cleared by shaking phone")

                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        if (accelerometer != null) {
            sensorManager.registerListener(
                sensorListener,
                accelerometer,
                SensorManager.SENSOR_DELAY_UI
            )
        }

        onDispose {
            sensorManager.unregisterListener(sensorListener)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
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
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onEditClick = { animal ->
                        viewModel.selectSightingForEdit(animal)
                    },
                    onDelete = { animal ->
                        viewModel.deleteSighting(animal)
                    },
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