package com.samples.storage.playground.photopicker

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.samples.storage.uielements.BackButton
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoPickerScreen(navController: NavController, viewModel: PhotoPickerViewModel = viewModel()) {
    val state = viewModel.uiState

    val selectItem = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
        viewModel::onSingleSelect
    )

    val selectItems = rememberLauncherForActivityResult(
        // PickMultipleVisualMedia requires to define a maxItems property to be higher than 1
        ActivityResultContracts.PickMultipleVisualMedia(max(state.maxItems, 2)),
        viewModel::onMultipleSelect
    )

    fun launchPhotoPicker() {
        viewModel.createPickVisualMediaRequest()?.let { request ->
            if (state.maxItems == 1) {
                selectItem.launch(request)
            } else {
                selectItems.launch(request)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Photo Picker", fontFamily = FontFamily.Serif) },
                navigationIcon = {
                    BackButton(navController)
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        TypeFilterButton(
                            label = "Image",
                            icon = if (state.imageTypeFilterEnabled) Icons.Filled.Image else Icons.Outlined.Image,
                            enabled = state.imageTypeFilterEnabled,
                            onClick = viewModel::onImageFilterClick
                        )
                        TypeFilterButton(
                            label = "Video",
                            icon = if (state.videoTypeFilterEnabled) Icons.Filled.Movie else Icons.Outlined.Movie,
                            enabled = state.videoTypeFilterEnabled,
                            onClick = viewModel::onVideoFilterClick
                        )

                        MaxItemsSelect(state.maxItems, viewModel::onMaxItemsChange)
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = ::launchPhotoPicker,
                        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                    ) {
                        Icon(Icons.Filled.Add, "Select from gallery")
                    }
                }
            )
        },
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            ListItem(
                headlineText = { Text("Picker Used") },
                leadingContent = { Icon(Icons.Filled.PhotoLibrary, contentDescription = null) },
                trailingContent = { Text(state.availablePicker) }
            )
            Divider()
            PhotoGallery(state.items)
        }
    }
}

@Composable
fun TypeFilterButton(label: String, icon: ImageVector, enabled: Boolean, onClick: () -> Unit) {
    if (enabled) {
        FilledTonalButton(
            onClick = onClick,
            contentPadding = ButtonDefaults.ButtonWithIconContentPadding
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(label)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            contentPadding = ButtonDefaults.ButtonWithIconContentPadding
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(label)
        }
    }
}

@Composable
fun MaxItemsSelect(maxItems: Int, onSelect: (maxItems: Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
        TextButton(onClick = { expanded = true }) {
            Text(if (maxItems > 1) "$maxItems items max" else "1 item max")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("1 item") },
                onClick = { onSelect(1); expanded = false }
            )
            DropdownMenuItem(
                text = { Text("3 items") },
                onClick = { onSelect(3); expanded = false }
            )
            DropdownMenuItem(
                text = { Text("10 items") },
                onClick = { onSelect(10); expanded = false }
            )
            DropdownMenuItem(
                text = { Text("100 items") },
                onClick = { onSelect(100); expanded = false }
            )
        }
    }
}

@Composable
fun PhotoGallery(items: List<Uri>) {
    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        items(items) {
            AsyncImage(
                model = it,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.aspectRatio(1f)
            )
        }
    }
}