package com.samples.storage.playground.photopicker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoPickerScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Photo Picker", fontFamily = FontFamily.Serif) },
            )
        },
    ) { paddingValues ->
        Column(
            Modifier
                .padding(paddingValues)
        ) {

        }
    }
}