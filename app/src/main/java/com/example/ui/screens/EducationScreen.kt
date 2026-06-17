package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.ui.LegoViewModel

@Composable
fun EducationScreen(
    viewModel: LegoViewModel,
    modifier: Modifier = Modifier
) {
    val inventory by viewModel.inventory.collectAsState()
    val activeQuestIdx by viewModel.activeQuestIndex.collectAsState()
    
    val activeQuest = viewModel.educationQuests[activeQuestIdx]
    val isQuestCompleted = activeQuest.isCompleted(inventory)

    // Counting Game state
    var countingAnswer by remember { mutableStateOf<Int?>(null) }
    val correctCountGameAnswer = 4 // Pre-scripted game question: "How many studs are on a standard 2x2 LEGO block?"
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Core Header
        HighDensityHeader(
            subTitle = "STEM & LEARNING",
            mainTitle = "STEM Playroom",
            avatarIcon = Icons.Filled.School
        )
        Text(
            text = "Learn math, physics, & shapes through building blocks!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 1. ACTIVE STEM QUESTS
        ElevatedCard(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = if (isQuestCompleted) Color(0xFFE2F7ED) else MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 2.dp,
                    color = if (isQuestCompleted) Color(0xFF008F4C) else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(24.dp)
                )
                .testTag("quest_card")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isQuestCompleted) Color(0xFF008F4C) else MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (isQuestCompleted) Icons.Filled.Stars else Icons.Filled.Extension,
                                contentDescription = "Quest",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Daily STEM Quest",
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isQuestCompleted) Color(0xFF0D5C3A) else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isQuestCompleted) Color(0xFF008F4C) else MaterialTheme.colorScheme.secondary)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isQuestCompleted) "COMPLETE!" else "ACTIVE",
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isQuestCompleted) Color.White else Color.Black,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = activeQuest.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = if (isQuestCompleted) Color(0xFF0D5C3A) else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = activeQuest.instruction,
                    fontSize = 13.sp,
                    color = if (isQuestCompleted) Color(0xFF196F3D) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedVisibility(
                    visible = isQuestCompleted,
                    enter = fadeIn() + expandVertically()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "🎉 Incredible job, young engineer! Your box has the matching blocks!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF0D5C3A),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.advanceQuest() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF008F4C)),
                            modifier = Modifier.testTag("advance_quest_button")
                        ) {
                            Text("Unlock Next Challenge", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (!isQuestCompleted) {
                    Text(
                        text = "Tip: Head to My Toy Box or use the AI Scanner to scan raw pieces in your playroom to unlock completion!",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. MATH & SHAPE PUZZLE CORNER SPECIAL FOR KIDS 3-7
        Text(
            text = "Block Counting Mini-Game",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Lego block drawn dynamically
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE3000B)),
                    contentAlignment = Alignment.Center
                ) {
                    // Standard 2x2 studs
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.3f)))
                            Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.3f)))
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.3f)))
                            Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.3f)))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Stud Counting Quiz:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Look at the Red 2x2 LEGO brick pictured above. How many circular circular bumps (studs) are sitting on top of it?",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                // Render options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(2, 4, 6, 8).forEach { option ->
                        val isSelected = countingAnswer == option
                        val isCorrect = option == correctCountGameAnswer

                        Button(
                            onClick = { countingAnswer = option },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) {
                                    if (isCorrect) Color(0xFF008F4C) else MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                                contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("quiz_option_$option")
                        ) {
                            Text(text = "$option studs", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                // Feedback
                if (countingAnswer != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    if (countingAnswer == correctCountGameAnswer) {
                        Text(
                            text = "⭐ Spot on! A 2x2 brick has exactly 2 rows of 2 studs, which equals 4 studs!",
                            color = Color(0xFF008F4C),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = "❌ Try again! Hint: Count all the white circular bumps on the red model.",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
