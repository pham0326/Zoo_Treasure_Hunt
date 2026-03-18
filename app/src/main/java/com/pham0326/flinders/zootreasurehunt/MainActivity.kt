package com.pham0326.flinders.zootreasurehunt

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import com.pham0326.flinders.zootreasurehunt.ui.theme.ZooTreasureHuntTheme
import com.pham0326.flinders.zootreasurehunt.utils.FileUtils
import com.pham0326.flinders.zootreasurehunt.viewmodel.ZooViewModel

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
    val viewModel: ZooViewModel = viewModel()
    val sightings by viewModel.sightings.collectAsStateWithLifecycle()
    val isSortByName by viewModel.isSortByName.collectAsState(initial = true)

    var selectedSighting by remember { mutableStateOf<Sighting?>(null) }
    var showDialog by remember { mutableStateOf(false) }

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
                    sightings = sightings,
                    onEditClick = { animal ->
                        selectedSighting = animal
                        showDialog = true
                    },
                    onDelete = { animal ->
                        viewModel.deleteSighting(animal)
                    }
                )
            }

            composable<SettingsDestination> {
                SettingsScreen(
                    isSortByName = isSortByName,
                    onSortChange = { viewModel.toggleSortOrder(it) }
                )
            }

            composable<AboutDestination> {
                AboutScreen()
            }
        }

        if (showDialog) {
            selectedSighting?.let { sighting ->
                EditSightingDialog(
                    sighting = sighting,
                    onDismiss = { showDialog = false },
                    onSave = { updated ->
                        viewModel.updateSighting(updated)
                        showDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun AnimalCard(sighting: Sighting, onClick: () -> Unit) {
    val cardColor = if (sighting.isFound) Color(0xFFE8F5E9) else Color(0xFFF5F5F5)
    val textColor = if (sighting.isFound) Color(0xFF2E7D32) else Color.Black
    val imageModel = sighting.photoPath ?: sighting.imageUrl

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = imageModel,
                contentDescription = sighting.name,
                modifier = Modifier
                    .size(64.dp)
                    .padding(end = 8.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sighting.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                if (sighting.isFound && sighting.notes.isNotEmpty()) {
                    Text(
                        text = sighting.notes,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            if (sighting.isFound) {
                Text(
                    text = stringResource(id = R.string.found_label),
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }
    }
}

@Composable
fun EditSightingDialog(
    sighting: Sighting,
    onDismiss: () -> Unit,
    onSave: (Sighting) -> Unit
) {
    var notesText by remember { mutableStateOf(sighting.notes) }
    var isFoundChecked by remember { mutableStateOf(sighting.isFound) }

    val context = LocalContext.current
    val fileUtils = remember { FileUtils(context) }
    var currentPhotoPath by remember { mutableStateOf(sighting.photoPath) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            currentPhotoPath = tempPhotoUri.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.edit_animal)) },
        text = {
            Column {
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text(stringResource(id = R.string.notes_hint)) }
                )

                Row(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isFoundChecked,
                        onCheckedChange = { isFoundChecked = it }
                    )
                    Text(text = stringResource(id = R.string.checkbox_found))
                }

                Button(
                    onClick = {
                        val file = fileUtils.createImageFile()
                        val uri = fileUtils.getUriForFile(file)
                        tempPhotoUri = uri
                        cameraLauncher.launch(uri)
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(
                        text = if (currentPhotoPath == null) {
                            stringResource(R.string.take_photo)
                        } else {
                            stringResource(R.string.retake_photo)
                        }
                    )
                }

                if (currentPhotoPath != null) {
                    Text(
                        text = stringResource(R.string.photo_attached),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        sighting.copy(
                            isFound = isFoundChecked,
                            notes = notesText,
                            photoPath = currentPhotoPath
                        )
                    )
                }
            ) {
                Text(text = stringResource(id = R.string.save_btn))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.cancel_btn))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ZooAppPreview() {
    ZooTreasureHuntTheme {
        ZooApp()
    }
}