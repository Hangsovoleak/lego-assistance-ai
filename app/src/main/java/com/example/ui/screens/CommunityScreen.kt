package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.local.CustomCreationEntity
import com.example.ui.LegoViewModel

@Composable
fun CommunityScreen(
    viewModel: LegoViewModel,
    modifier: Modifier = Modifier
) {
    val customCreations by viewModel.customCreations.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    // Mock global community masterpieces
    val mockMasterpieces = listOf(
        CommunityDesign("Turbo Space Station", "A huge space command center created with alternating red and green support joints. Extends upwards!", "Alex (Age 7)", "🚀 Space", "🌟 24", "4 comments"),
        CommunityDesign("Redwood Forest Tower", "An architectural challenge trying to stack 2x2 blue columns over 2x4 tree branches. Supports over 500g!", "Sienna (Age 6)", "🌲 Nature", "❤ 18", "2 comments"),
        CommunityDesign("Lil Rover Dino", "Made using remaining black wheels on dinosaur hips. He rolls when you touch him!", "Lucas (Age 5)", "🦖 Animal", "🌟 32", "6 comments")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // High Density Header
        HighDensityHeader(
            subTitle = "CREATORS COMMUNITY",
            mainTitle = "Creators Lounge",
            avatarIcon = Icons.Filled.Diversity3
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Share creations & draft masterpieces!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            )

            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier
                    .height(36.dp)
                    .testTag("publish_own_creation_button")
            ) {
                Icon(Icons.Filled.CloudUpload, contentDescription = "Publish", tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Publish", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Section 1: User's saved custom designs
            if (customCreations.isNotEmpty()) {
                item {
                    Text(
                        "Your Masterpieces Cabinet",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                items(customCreations) { creation ->
                    SavedCabinetItem(
                        creation = creation,
                        onDelete = { viewModel.deleteCreation(creation) }
                    )
                }
            }

            // Section 2: Global Inspiration Board (Prepopulated)
            item {
                Text(
                    "Global Inspiration Board",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            items(mockMasterpieces) { design ->
                GlobalInspirationCard(design = design)
            }
        }
    }

    if (showAddDialog) {
        AddCreationDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, desc, tag ->
                viewModel.uploadCustomCreation(title, desc, tag, "ic_launcher_foreground")
                showAddDialog = false
            }
        )
    }
}

@Composable
fun SavedCabinetItem(
    creation: CustomCreationEntity,
    onDelete: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.EmojiEvents, "Own design", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = creation.title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_cabinet_item_${creation.localId}")
                ) {
                    Icon(Icons.Filled.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = creation.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "🏷️ ${creation.tags}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun GlobalInspirationCard(design: CommunityDesign) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = design.title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Designed by ${design.creator}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = design.badge,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = design.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(design.likes, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("💬 ${design.comments}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AddCreationDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, desc: String, tag: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf("Vehicles") }

    val tagsOption = listOf("Vehicles", "Animals", "Space", "Buildings", "Art")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Publish Your Blueprint", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Creation Title") },
                    placeholder = { Text("e.g. Symmetrical Moon Rocket") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("creation_title_input")
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("How did you build it?") },
                    placeholder = { Text("e.g. Stacked two yellow plates over black axis wheels...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("creation_desc_input")
                )

                Text("Select Category Model Tag:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    tagsOption.forEach { t ->
                        val isSelected = selectedTag == t
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTag = t },
                            label = { Text(t) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, desc, selectedTag) },
                enabled = title.isNotEmpty() && desc.isNotEmpty(),
                modifier = Modifier.testTag("dialog_confirm_publish")
            ) {
                Text("Save to Cabinet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

data class CommunityDesign(
    val title: String,
    val description: String,
    val creator: String,
    val badge: String,
    val likes: String,
    val comments: String
)
