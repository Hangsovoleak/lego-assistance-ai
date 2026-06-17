package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HighDensityHeader(
    subTitle: String = "LEGO ASSISTANT",
    mainTitle: String,
    modifier: Modifier = Modifier,
    avatarIcon: ImageVector = Icons.Filled.Extension
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = subTitle.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = mainTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Top right profile avatar/badge matching the template
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = avatarIcon,
                contentDescription = "Avatar",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
fun HighDensityStatsGrid(
    totalBricks: Int,
    buildsReady: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Card 1: Total Bricks
        Box(
            modifier = Modifier
                .weight(1f)
                .background(androidx.compose.ui.graphics.Color(0xFFE3EFFF), androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                .border(1.dp, androidx.compose.ui.graphics.Color(0xFFBFDBFE), androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = "TOTAL BRICKS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color(0xFF1D4ED8), // blue-700
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$totalBricks",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = androidx.compose.ui.graphics.Color(0xFF1E3A8A) // blue-900
                )
            }
        }

        // Card 2: Builds Ready
        Box(
            modifier = Modifier
                .weight(1f)
                .background(androidx.compose.ui.graphics.Color(0xFFF1F5F9), androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                .border(1.dp, androidx.compose.ui.graphics.Color(0xFFE2E8F0), androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = "BUILDS READY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color(0xFF475569), // slate-600
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$buildsReady",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = androidx.compose.ui.graphics.Color(0xFF0F172A) // slate-900
                )
            }
        }
    }
}
