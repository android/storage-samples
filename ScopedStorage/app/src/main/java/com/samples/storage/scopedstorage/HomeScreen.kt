package com.samples.storage.scopedstorage

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController

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