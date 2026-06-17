package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LegoBrick
import com.example.ui.LegoViewModel

@Composable
fun InventoryScreen(
    viewModel: LegoViewModel,
    modifier: Modifier = Modifier
) {
    val inventory by viewModel.inventory.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val totalBricks = inventory.sumOf { it.quantity }
        val buildsReady = com.example.data.model.PreloadedModels.list.count { it.getBuildStatus(inventory).isBuildable }

        // High Density Header
        HighDensityHeader(
            subTitle = "TOY BOX INVENTORY",
            mainTitle = "My Brick Box",
            avatarIcon = Icons.Filled.Inbox
        )

        // Dynamic high density statistics grid
        HighDensityStatsGrid(totalBricks = totalBricks, buildsReady = buildsReady)

        Spacer(modifier = Modifier.height(4.dp))

        // Actions Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Inventory Items",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (inventory.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.resetInventory() },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                            .size(36.dp)
                            .testTag("reset_inventory_button")
                    ) {
                        Icon(
                            Icons.Filled.DeleteSweep,
                            contentDescription = "Empty Box",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                
                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("add_brick_manual_button")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Brick", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (inventory.isEmpty()) {
            EmptyInventoryState(
                onLoadStarterSet = {
                    val starter = listOf(
                        Triple("2x4", "Red", 6),
                        Triple("2x2", "Blue", 5),
                        Triple("1x4", "Yellow", 6),
                        Triple("2x2", "White", 3),
                        Triple("2x2", "Green", 4),
                        Triple("Wheel", "Black", 2)
                    )
                    starter.forEach { (size, color, qty) ->
                        viewModel.addManualBrick(size, color, qty)
                    }
                }
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(inventory) { brick ->
                    LegoBrickCard(
                        brick = brick,
                        onPlus = { viewModel.updateBrickCount(brick.id, 1) },
                        onMinus = { viewModel.updateBrickCount(brick.id, -1) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddManualBrickDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { size, color, qty ->
                viewModel.addManualBrick(size, color, qty)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun LegoBrickCard(
    brick: LegoBrick,
    onPlus: () -> Unit,
    onMinus: () -> Unit
) {
    val bubbleColor = Color(android.graphics.Color.parseColor(brick.colorHex))
    val isLightColor = brick.colorName.lowercase() == "white" || brick.colorName.lowercase() == "yellow"
    val textColor = if (isLightColor) Color.Black else Color.White

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Lego Brick visual representation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bubbleColor)
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Render stylized Studs on the Brick
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Lego Bricks Stud rows based on size
                    val rows = if (brick.size.contains("2")) 2 else 1
                    val cols = when {
                        brick.size.contains("4") -> 4
                        brick.size.contains("2") -> 2
                        brick.size.lowercase() == "wheel" -> 1
                        else -> 1
                    }

                    if (brick.size.lowercase() == "wheel") {
                        // Draw Wheel
                        Icon(
                            Icons.Filled.TripOrigin,
                            contentDescription = "Wheel stud",
                            tint = textColor.copy(alpha = 0.6f),
                            modifier = Modifier.size(40.dp)
                        )
                    } else {
                        // Standard Stud rows
                        repeat(rows) {
                            Row(horizontalArrangement = Arrangement.Center) {
                                repeat(cols) {
                                    Box(
                                        modifier = Modifier
                                            .padding(2.dp)
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(textColor.copy(alpha = 0.25f))
                                            .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = brick.size,
                        color = textColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Brick details text
            Text(
                text = "${brick.colorName} piece",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = brick.category,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action counts
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onMinus,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .testTag("minus_button_${brick.id}")
                ) {
                    Icon(
                        Icons.Filled.Remove,
                        contentDescription = "Minus",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = "${brick.quantity}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                IconButton(
                    onClick = onPlus,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .testTag("plus_button_${brick.id}")
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Plus",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyInventoryState(onLoadStarterSet: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Inbox,
                    contentDescription = "Empty",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Empty Toy Box!",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Use the AI Brick Scanner to automatically detect pieces from your workspace photo, or tap helper below to try with popular sets.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onLoadStarterSet,
                modifier = Modifier.testTag("load_starter_kit_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Import Starter Lego Box", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddManualBrickDialog(
    onDismiss: () -> Unit,
    onAdd: (size: String, color: String, qty: Int) -> Unit
) {
    var sizeInput by remember { mutableStateOf("2x4") }
    var colorInput by remember { mutableStateOf("Red") }
    var quantityInput by remember { mutableStateOf(5) }

    val sizesList = listOf("2x4", "2x2", "1x4", "1x2", "1x1", "Wheel")
    val colorsList = listOf("Red", "Blue", "Yellow", "Green", "White", "Black")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add LEGO Brick", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Size Picker
                Text("Select Brick Size/Dimensions:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    sizesList.take(3).forEach { s ->
                        val isSelected = sizeInput == s
                        FilterChip(
                            selected = isSelected,
                            onClick = { sizeInput = s },
                            label = { Text(s) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    sizesList.drop(3).forEach { s ->
                        val isSelected = sizeInput == s
                        FilterChip(
                            selected = isSelected,
                            onClick = { sizeInput = s },
                            label = { Text(s) }
                        )
                    }
                }

                // Color Picker
                Text("Select Brick Color:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    colorsList.take(3).forEach { c ->
                        val isSelected = colorInput == c
                        FilterChip(
                            selected = isSelected,
                            onClick = { colorInput = c },
                            label = { Text(c) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    colorsList.drop(3).forEach { c ->
                        val isSelected = colorInput == c
                        FilterChip(
                            selected = isSelected,
                            onClick = { colorInput = c },
                            label = { Text(c) }
                        )
                    }
                }

                // Quantity slider
                Text("Quantity: $quantityInput", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Slider(
                    value = quantityInput.toFloat(),
                    onValueChange = { quantityInput = it.toInt() },
                    valueRange = 1f..30f,
                    steps = 29
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(sizeInput, colorInput, quantityInput) },
                modifier = Modifier.testTag("dialog_confirm_add")
            ) {
                Text("Add Brick")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
