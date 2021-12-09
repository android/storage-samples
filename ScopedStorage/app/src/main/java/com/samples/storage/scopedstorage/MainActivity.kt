package com.samples.storage.scopedstorage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.samples.storage.scopedstorage.mediastore.AddFileToDownloadsScreen
import com.samples.storage.scopedstorage.mediastore.AddMediaFileScreen
import com.samples.storage.scopedstorage.mediastore.CaptureMediaFileScreen
import com.samples.storage.scopedstorage.mediastore.ListMediaFileScreen
import com.samples.storage.scopedstorage.saf.SelectDocumentFileScreen
import com.samples.storage.scopedstorage.ui.theme.ScopedStorageTheme

class MainActivity : ComponentActivity() {
    @ExperimentalFoundationApi
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScopedStorageTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = HomeRoute
                ) {
                    composable(HomeRoute) {
                        HomeScreen(navController)
                    }

                    composable(Demos.AddMediaFile.route) {
                        AddMediaFileScreen(navController)
                    }
                    composable(Demos.CaptureMediaFile.route) {
                        CaptureMediaFileScreen(navController)
                    }
                    composable(Demos.AddFileToDownloads.route) {
                        AddFileToDownloadsScreen(navController)
                    }
                    composable(Demos.EditMediaFile.route) { NotAvailableYetScreen() }
                    composable(Demos.DeleteMediaFile.route) { NotAvailableYetScreen() }
                    composable(Demos.ListMediaFiles.route) {
                        ListMediaFileScreen(navController)
                    }
                    composable(Demos.SelectDocumentFile.route) {
                        SelectDocumentFileScreen(navController)
                    }
                    composable(Demos.CreateDocumentFile.route) { NotAvailableYetScreen() }
                    composable(Demos.EditDocumentFile.route) { NotAvailableYetScreen() }
                }
            }
        }
    }
}

@Composable
fun NotAvailableYetScreen() {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.demo_not_available_yet_label))
    }
}