package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lego_bricks")
data class LegoBrickEntity(
    @PrimaryKey val id: String, // Combination of size + colorName
    val name: String,
    val size: String,
    val colorName: String,
    val colorHex: String,
    val category: String,
    val quantity: Int,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "custom_creations")
data class CustomCreationEntity(
    @PrimaryKey(autoGenerate = true) val localId: Int = 0,
    val title: String,
    val description: String,
    val tags: String, // comma separated
    val imagePath: String, // visual placeholder or selected sample path
    val timestamp: Long = System.currentTimeMillis(),
    val creatorName: String = "Lego Creator"
)

@Entity(tableName = "saved_builds")
data class SavedBuildEntity(
    @PrimaryKey val modelId: String,
    val currentStep: Int = 1,
    val isCompleted: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)
