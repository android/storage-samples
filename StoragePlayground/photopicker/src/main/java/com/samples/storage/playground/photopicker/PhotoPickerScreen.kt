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
import androidx.navigation.NavController
import com.samples.storage.uielements.BackButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoPickerScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Photo Picker", fontFamily = FontFamily.Serif) },
                navigationIcon = {
                    BackButton(navController)
                }
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