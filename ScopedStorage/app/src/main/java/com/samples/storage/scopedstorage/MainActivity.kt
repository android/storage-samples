package com.samples.storage.scopedstorage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.samples.storage.scopedstorage.mediastore.AddMediaFileScreen
import com.samples.storage.scopedstorage.ui.theme.ScopedStorageTheme

class MainActivity : ComponentActivity() {
    @ExperimentalFoundationApi
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScopedStorageTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = Demos.AddMediaFile.route) {
                    composable(HomeRoute) {
                        HomeScreen(navController)
                    }

                    composable(Demos.AddMediaFile.route) { AddMediaFileScreen() }
                    composable(Demos.CaptureMediaFile.route) { NotAvailableYetScreen() }
                    composable(Demos.DownloadMediaFile.route) { NotAvailableYetScreen() }
                    composable(Demos.AddFileToDownloads.route) { NotAvailableYetScreen() }
                    composable(Demos.EditMediaFile.route) { NotAvailableYetScreen() }
                    composable(Demos.DeleteMediaFile.route) { NotAvailableYetScreen() }
                    composable(Demos.ListMediaFiles.route) { NotAvailableYetScreen() }
                    composable(Demos.SelectDocumentFile.route) { NotAvailableYetScreen() }
                    composable(Demos.CreateDocumentFile.route) { NotAvailableYetScreen() }
                    composable(Demos.EditDocumentFile.route) { NotAvailableYetScreen() }
                }
            }
        }
    }
}

const val HomeRoute = "home"

@ExperimentalMaterialApi
@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.app_name)) })
        },
        content = { paddingValues ->
            LazyColumn(Modifier.padding(paddingValues)) {
                items(Demos.list) { demo ->
                    ListItem(
                        modifier = Modifier.clickable { navController.navigate(demo.route) },
                        text = { Text(stringResource(demo.name)) },
                        secondaryText = { Text(stringResource(demo.description)) },
                        icon = { Icon(demo.icon, contentDescription = null) }
                    )
                    Divider()
                }
            }
        }
    )
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