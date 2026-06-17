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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lightbulb
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
import com.example.data.model.LegoModel
import com.example.data.model.PreloadedModels
import com.example.ui.AIRecommendUiState
import com.example.ui.LegoViewModel

@Composable
fun ModelsScreen(
    viewModel: LegoViewModel,
    modifier: Modifier = Modifier
) {
    val inventory by viewModel.inventory.collectAsState()
    val aiRecommendState by viewModel.aiRecommendState.collectAsState()
    val selectedModelActive by viewModel.selectedModelForBuild.collectAsState()

    // Query constraint for custom AI builds
    var customSearchText by remember { mutableStateOf("") }
    var currentTab by remember { mutableStateOf(0) } // 0: Catalog, 1: Request Custom AI Suggestion

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (selectedModelActive != null) {
            // Interactive Instruction Builder mode is open! Show it full-screen!
            BuildingInstructionGuide(
                viewModel = viewModel,
                model = selectedModelActive!!
            )
        } else {
            // Recommendation Hub
            HighDensityHeader(
                subTitle = "MODEL LAB",
                mainTitle = "Build Center",
                avatarIcon = Icons.Filled.Lightbulb
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Suggested builds based on your LEGO box",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )

                // AI Spark
                IconButton(
                    onClick = { currentTab = if (currentTab == 1) 0 else 1 },
                    modifier = Modifier
                        .background(
                            if (currentTab == 1) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape
                        )
                        .testTag("ai_toggle_tab_button")
                ) {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = "AI Spark",
                        tint = if (currentTab == 1) Color.Black else MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Tab toggles
            if (currentTab == 1) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AutoAwesome, "AI suggestion", tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "AI Custom Model Creator",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 15.sp
                            )
                        }

                        Text(
                            "Type any idea (e.g. 'rocket', 'dinosaur', 'house') and our Gemini AI Assistant will compose custom step-by-step guidelines matching the exact blocks in your physical box!",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )

                        OutlinedTextField(
                            value = customSearchText,
                            onValueChange = { customSearchText = it },
                            label = { Text("What would you like to build today?") },
                            placeholder = { Text("e.g. Little plane, Snail, Spaceship") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ai_custom_build_input"),
                            singleLine = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        viewModel.requestAICustomRecommendation(customSearchText)
                                    },
                                    modifier = Modifier.testTag("ai_generate_button")
                                ) {
                                    Icon(Icons.Filled.Send, "Generate", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        )

                        // Handle AI Recommend states
                        when (aiRecommendState) {
                            is AIRecommendUiState.Generating -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Gemini is engineering blueprints...", fontSize = 13.sp)
                                }
                            }
                            is AIRecommendUiState.Error -> {
                                Text(
                                    "Error generating custom recommendation: ${(aiRecommendState as AIRecommendUiState.Error).message}. Showing fallback dynamic helper.",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Button(
                                    onClick = { viewModel.clearAIRecommendState() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Text("Reset Generator", color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                            is AIRecommendUiState.Success -> {
                                val aiModel = (aiRecommendState as AIRecommendUiState.Success).recommendedModel
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(12.dp))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("✨ AI RECOMMENDED CONCEPT", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                                        Text(aiModel.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Text(aiModel.description, fontSize = 12.sp, maxLines = 2)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                viewModel.selectModelForBuilding(aiModel)
                                                viewModel.clearAIRecommendState()
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                            modifier = Modifier.testTag("build_ai_model_button")
                                        ) {
                                            Text("Open AI Building Instructions", color = Color.Black, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                            is AIRecommendUiState.Idle -> {}
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // General recommendations catalog
            Text(
                text = "LEGO Model Blueprint Library",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(PreloadedModels.list) { model ->
                    val status = model.getBuildStatus(inventory)
                    ModelRowItem(
                        model = model,
                        isBuildable = status.isBuildable,
                        percentage = status.matchPercentage,
                        missingBricks = status.missingBricks,
                        onBuild = { viewModel.selectModelForBuilding(model) }
                    )
                }
            }
        }
    }
}

@Composable
fun ModelRowItem(
    model: LegoModel,
    isBuildable: Boolean,
    percentage: Int,
    missingBricks: List<com.example.data.model.MissingBrick>,
    onBuild: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isBuildable) 1.5.dp else 1.dp,
                color = if (isBuildable) MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title block
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = model.name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = model.category,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text("•", color = Color.Gray, fontSize = 11.sp)
                        Text(
                            text = "${model.estimatedMinutes} mins",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                        Text("•", color = Color.Gray, fontSize = 11.sp)
                        // Difficulty Chip
                        Text(
                            text = model.difficulty,
                            color = when (model.difficulty.lowercase()) {
                                "easy" -> Color(0xFF008F4C)
                                "medium" -> Color(0xFFFF5F00)
                                else -> Color(0xFFE3000B)
                            },
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp
                        )
                    }
                }

                // Match Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isBuildable) Color(0xFFE2F7ED) else Color(0xFFFFF3CD)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isBuildable) "Buildable!" else "$percentage% match",
                        color = if (isBuildable) Color(0xFF0D5C3A) else Color(0xFF856404),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = model.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Required Bricks list
            Text(
                text = "Required Bricks Checklist:",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Render color bricks row representing checklists
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                model.requiredBricks.forEach { required ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(android.graphics.Color.parseColor(required.colorHex)).copy(alpha = 0.15f))
                            .border(
                                1.dp,
                                Color(android.graphics.Color.parseColor(required.colorHex)),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${required.quantity}× ${required.size} ${required.colorName}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(android.graphics.Color.parseColor(required.colorHex))
                        )
                    }
                }
            }

            // Missing bricks block (if any pieces missing)
            if (!isBuildable && missingBricks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFEF9E7))
                        .border(1.dp, Color(0xFFFAE5B0), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Warning, "Missing", tint = Color(0xFFB7950B), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Missing pieces to complete: ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFF7D6608)
                        )
                    }
                    missingBricks.forEach { missing ->
                        Text(
                            text = "• Need ${missing.missingCount} more ${missing.colorName} ${missing.size} brick(s)",
                            fontSize = 10.sp,
                            color = Color(0xFF7D6608),
                            modifier = Modifier.padding(start = 20.dp, top = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Build Now CTA Button
            Button(
                onClick = onBuild,
                enabled = true, // We allow clicking even if some pieces are missing so children can substitute pieces and practice problem-solving!
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("model_build_button_${model.id}"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isBuildable) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isBuildable) Color.Black else MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(
                    if (isBuildable) Icons.Filled.PlayArrow else Icons.Outlined.Lightbulb,
                    contentDescription = "Build"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBuildable) "Let's Build!" else "Build with substitutions",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun BuildingInstructionGuide(
    viewModel: LegoViewModel,
    model: LegoModel
) {
    val currentStep by viewModel.currentBuildingStep.collectAsState()
    val totalSteps = model.steps.size
    val stepIndex = currentStep - 1
    val step = model.steps.getOrNull(stepIndex)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxSize()
            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header builder state
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Build Guide • ${model.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Step $currentStep of $totalSteps",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                IconButton(onClick = { viewModel.cancelBuilding() }) {
                    Icon(Icons.Filled.Close, contentDescription = "Exit Building")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Step dynamic Progress bar
            LinearProgressIndicator(
                progress = { currentStep.toFloat() / totalSteps.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Visual instruction preview box
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Interactive vector block builder render space!
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (step != null) {
                        Text(
                            "Visual Guide Blueprint",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        // Drawing interactive shapes on dynamic layers according to the steps!
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Lay out standard Lego stacked configuration graphics
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Draw steps up to the current step!
                                for (i in 0..stepIndex) {
                                    val priorStep = model.steps[i]
                                    // Make older steps translucent to emphasize the active focus brick!
                                    val alphaScale = if (i == stepIndex) 1f else 0.45f
                                    val isFocus = i == stepIndex
                                    
                                    Box(
                                        modifier = Modifier
                                            .width(if (priorStep.focusBrickId?.contains("2x4") == true) 120.dp else 70.dp)
                                            .height(30.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                when {
                                                    priorStep.focusBrickId?.contains("red") == true -> Color(0xFFE3000B).copy(alpha = alphaScale)
                                                    priorStep.focusBrickId?.contains("blue") == true -> Color(0xFF0055A5).copy(alpha = alphaScale)
                                                    priorStep.focusBrickId?.contains("yellow") == true -> Color(0xFFF2CD37).copy(alpha = alphaScale)
                                                    priorStep.focusBrickId?.contains("green") == true -> Color(0xFF008F4C).copy(alpha = alphaScale)
                                                    priorStep.focusBrickId?.contains("white") == true -> Color(0xFFF2F3F2).copy(alpha = alphaScale)
                                                    else -> MaterialTheme.colorScheme.primary.copy(alpha = alphaScale)
                                                }
                                            )
                                            .border(
                                                width = if (isFocus) 2.dp else 1.dp,
                                                color = if (isFocus) MaterialTheme.colorScheme.secondary else Color.White.copy(alpha = 0.3f),
                                                shape = RoundedCornerShape(4.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = priorStep.focusBrickId?.replace("_", " ")?.uppercase() ?: "BLOCK",
                                            color = if (priorStep.focusBrickId?.contains("yellow") == true || priorStep.focusBrickId?.contains("white") == true) Color.Black else Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Written instruction details
            if (step != null) {
                Text(
                    text = step.description,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                
                // Educational Tip integration
                if (model.educationalFocus != null && currentStep == 1) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.School, "Tip", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "STEM Goal: This project teaches basic principles of ${model.educationalFocus}!",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Navigation Row
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = { viewModel.prevStep() },
                    enabled = currentStep > 1,
                    modifier = Modifier.testTag("step_prev_button")
                ) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Previous")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Back")
                }

                if (currentStep < totalSteps) {
                    Button(
                        onClick = { viewModel.nextStep() },
                        modifier = Modifier.testTag("step_next_button")
                    ) {
                        Text("Next Step")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
                    }
                } else {
                    Button(
                        onClick = { viewModel.finishBuilding() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.testTag("step_finish_button")
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "Finish", tint = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Complete Build!", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
