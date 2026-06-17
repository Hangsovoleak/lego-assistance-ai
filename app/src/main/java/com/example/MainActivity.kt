package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.LegoViewModel
import com.example.ui.screens.CommunityScreen
import com.example.ui.screens.EducationScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.ModelsScreen
import com.example.ui.screens.ScanScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Initialize the Lego state holder
                val viewModel: LegoViewModel = viewModel()
                
                // Track screen position using high performance state
                var selectedTabIndex by remember { mutableIntStateOf(0) }
                
                val navigationTabs = listOf(
                    NavigationTabItem("Scan", Icons.Filled.CameraAlt, "scan_tab"),
                    NavigationTabItem("Toy Box", Icons.Filled.Inbox, "inventory_tab"),
                    NavigationTabItem("Build Center", Icons.Filled.Lightbulb, "models_tab"),
                    NavigationTabItem("STEM Play", Icons.Filled.School, "education_tab"),
                    NavigationTabItem("Creators", Icons.Filled.Diversity3, "community_tab")
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier.testTag("app_bottom_nav_bar")
                        ) {
                            navigationTabs.forEachIndexed { index, tab ->
                                NavigationBarItem(
                                    selected = selectedTabIndex == index,
                                    onClick = { selectedTabIndex = index },
                                    icon = { Icon(tab.icon, contentDescription = tab.title) },
                                    label = { Text(tab.title) },
                                    modifier = Modifier.testTag(tab.testTag)
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    // Root content layouts with insets paddings
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (selectedTabIndex) {
                            0 -> ScanScreen(viewModel = viewModel)
                            1 -> InventoryScreen(viewModel = viewModel)
                            2 -> ModelsScreen(viewModel = viewModel)
                            3 -> EducationScreen(viewModel = viewModel)
                            4 -> CommunityScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

data class NavigationTabItem(
    val title: String,
    val icon: ImageVector,
    val testTag: String
)
