package com.samples.storage.playground.photopicker

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.samples.storage.playground.photopicker.PhotoPickerViewModel.Companion.MAX_ITEMS_VALUES
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Photo Picker", fontFamily = FontFamily.Serif) },
                navigationIcon = {
                    BackButton(navController)
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (state.maxItems == 1) {
                        selectItem.launch(viewModel.createPickVisualMediaRequest())
                    } else {
                        selectItems.launch(viewModel.createPickVisualMediaRequest())
                    }
                },
                containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
            ) {
                Icon(Icons.Filled.Add, "Select from gallery")
            }
        }
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            ListItem(
                leadingContent = { Icon(Icons.Filled.Filter, contentDescription = null) },
                headlineText = { Text("File Type Filter") },
                trailingContent = {
                    FileTypeFilterMenu(
                        state.fileTypeFilter,
                        viewModel::onFileTypeFilterChange
                    )
                },
            )
            Divider()
            ListItem(
                leadingContent = { Icon(Icons.Filled.Tune, contentDescription = null) },
                headlineText = { Text("Max items limit") },
                trailingContent = { MaxItemsSelect(state.maxItems, viewModel::onMaxItemsChange) },
            )
            Divider()
            PhotoGallery(state.items)
        }
    }
}

@Composable
fun FileTypeFilterMenu(
    value: PhotoPickerViewModel.FileTypeFilter,
    onChange: (value: PhotoPickerViewModel.FileTypeFilter) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
        TextButton(onClick = { expanded = true }) {
            Text(value.name)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            PhotoPickerViewModel.FileTypeFilter.values().forEach {
                DropdownMenuItem(
                    text = { Text(it.name) },
                    onClick = { onChange(it); expanded = false }
                )
            }
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
            MAX_ITEMS_VALUES.forEach { value ->
                DropdownMenuItem(
                    text = { Text(if (value > 1) "$value items max" else "1 item max") },
                    onClick = { onSelect(value); expanded = false }
                )
            }
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