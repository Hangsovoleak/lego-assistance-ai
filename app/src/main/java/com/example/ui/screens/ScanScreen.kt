package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LegoBrick
import com.example.ui.LegoViewModel
import com.example.ui.ScanUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ScanScreen(
    viewModel: LegoViewModel,
    modifier: Modifier = Modifier
) {
    val scanState by viewModel.scanState.collectAsState()
    val scope = rememberCoroutineScope()

    // Selected preloaded sample pile index to simulate real computer vision scanning
    var selectedSampleIndex by remember { mutableStateOf(0) }
    
    // Sample images represent mock bricks
    val samples = listOf(
        SamplePhotoItem("Rainbow Block Pile", "A colorful mix of 2x4 cubes, 2x2 blocks, and wheels.", "🎨", 0),
        SamplePhotoItem("Wheeled Car Kit", "Yellow chassis, red plates, and 4 high-friction wheels.", "🚗", 1),
        SamplePhotoItem("Dino Forest Mix", "A heap of emerald green plates and yellow accent blocks.", "🦖", 2)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // High Density Header
        HighDensityHeader(
            subTitle = "LEGO ASSISTANT",
            mainTitle = "Brick Finder AI",
            avatarIcon = Icons.Filled.Face
        )
        
        Text(
            text = "Scan your scatter of LEGO bricks using Computer Vision & AI",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        // Simulated camera viewfinder frame
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(Color.Black)
                .border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(32.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Live scanning indicator badge - From High Density Layout
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                    .border(1.dp, Color.White, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Pulse LED effect with color red
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color.Red, CircleShape)
                )
                Text(
                    text = "LIVE SCANNING",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    letterSpacing = 0.5.sp
                )
            }

            // Bottom detected quick pills overlay - From High Density Layout
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFE3000B), RoundedCornerShape(4.dp))
                        .border(1.dp, Color(0xFFFCA5A5).copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("2x4 Red", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .background(Color(0xFFF2CD37), RoundedCornerShape(4.dp))
                        .border(1.dp, Color(0xFFFEF08A).copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("1x2 Yellow", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1E293B), RoundedCornerShape(4.dp))
                        .border(1.dp, Color(0xFF475569).copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Wheel-Hub", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            // Background grid representation for Lego alignment
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridSpacing = 40.dp.toPx()
                for (x in 0..size.width.toInt() step gridSpacing.toInt()) {
                    drawLine(
                        color = Color(0xFF262A3F).copy(alpha = 0.4f),
                        start = Offset(x.toFloat(), 0f),
                        end = Offset(x.toFloat(), size.height),
                        strokeWidth = 1f
                    )
                }
                for (y in 0..size.height.toInt() step gridSpacing.toInt()) {
                    drawLine(
                        color = Color(0xFF262A3F).copy(alpha = 0.4f),
                        start = Offset(0f, y.toFloat()),
                        end = Offset(size.width, y.toFloat()),
                        strokeWidth = 1f
                    )
                }
            }

            // Viewfinder Target styling
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Focus points
                Text(
                    text = samples[selectedSampleIndex].emoji,
                    fontSize = 72.sp,
                    textAlign = TextAlign.Center
                )
                
                // Corner focus lines
                Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    LegoFocusCorners()
                }
            }

            // Laser scanning line animation overlay (Active when scanning)
            if (scanState is ScanUiState.Scanning) {
                val infiniteTransition = rememberInfiniteTransition(label = "laser")
                val yOffset by infiniteTransition.animateFloat(
                    initialValue = 0.1f,
                    targetValue = 0.9f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "laser"
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.01f)
                        .align(Alignment.TopCenter)
                        .offset(y = 350.dp * yOffset)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, Color.Red, Color.Transparent)
                            )
                        )
                )
            }

            // Scanning overlays
            when (scanState) {
                is ScanUiState.Scanning -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Analyzing Brick Boundaries...",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Removing noise & counting studs",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                is ScanUiState.Error -> {
                    val errMsg = (scanState as ScanUiState.Error).message
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.75f))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Error, "Error", tint = Color.Red, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Scan Unsuccessful",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = errMsg,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.clearScanState() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Try Again")
                            }
                        }
                    }
                }
                is ScanUiState.Success -> {
                    val detected = (scanState as ScanUiState.Success).detectedBricks
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.85f))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                "Success",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "AI Scan Complete!",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Successfully identified ${detected.sumOf { it.quantity }} Lego pieces!",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // List of scanned items
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2130)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(bottom = 16.dp)
                            ) {
                                LazyColumn(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(detected) { brick ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFF262A3F), RoundedCornerShape(8.dp))
                                                .padding(10.dp)
                                        ) {
                                            // Colored Lego Stud representation
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(android.graphics.Color.parseColor(brick.colorHex)))
                                                    .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = brick.name,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color.White,
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    text = "${brick.size} • ${brick.colorName}",
                                                    color = Color.White.copy(alpha = 0.6f),
                                                    fontSize = 12.sp
                                                )
                                            }
                                            Text(
                                                text = "× ${brick.quantity}",
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontSize = 16.sp,
                                                modifier = Modifier.padding(end = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.clearScanState() },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                ) {
                                    Text("Discard")
                                }
                                Button(
                                    onClick = { viewModel.clearScanState() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("import_to_box_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Text("Save to Box", color = Color.Black)
                                }
                            }
                        }
                    }
                }
                else -> {}
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sample Pile Selection Row
        Text(
            text = "Select Lego Pile to Scan:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.Start).padding(bottom = 6.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            samples.forEach { sample ->
                val isSelected = selectedSampleIndex == sample.index
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedSampleIndex = sample.index }
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(sample.emoji, fontSize = 28.sp)
                        Text(
                            text = sample.title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Trigger Button
        Button(
            onClick = {
                // Simulate passing base64 placeholder and trigger AI or Mock Scan
                viewModel.scanLegoPhoto("MOCK_BASE64_DATA", selectedSampleIndex)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("scan_trigger_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Outlined.CameraAlt, contentDescription = "Scan")
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Detect Available Bricks",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun LegoFocusCorners() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val lineSize = 30.dp.toPx()
        val weight = 3.dp.toPx()
        
        // Top Left Corner
        drawLine(Color.White, Offset(0f, 0f), Offset(lineSize, 0f), weight)
        drawLine(Color.White, Offset(0f, 0f), Offset(0f, lineSize), weight)
        
        // Top Right Corner
        drawLine(Color.White, Offset(size.width, 0f), Offset(size.width - lineSize, 0f), weight)
        drawLine(Color.White, Offset(size.width, 0f), Offset(size.width, lineSize), weight)
        
        // Bottom Left Corner
        drawLine(Color.White, Offset(0f, size.height), Offset(lineSize, size.height), weight)
        drawLine(Color.White, Offset(0f, size.height), Offset(0f, size.height - lineSize), weight)
        
        // Bottom Right Corner
        drawLine(Color.White, Offset(size.width, size.height), Offset(size.width - lineSize, size.height), weight)
        drawLine(Color.White, Offset(size.width, size.height), Offset(size.width, size.height - lineSize), weight)
    }
}

data class SamplePhotoItem(
    val title: String,
    val description: String,
    val emoji: String,
    val index: Int
)
