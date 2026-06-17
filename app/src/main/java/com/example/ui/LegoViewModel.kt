package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CustomCreationEntity
import com.example.data.local.LegoDatabase
import com.example.data.local.SavedBuildEntity
import com.example.data.model.LegoBrick
import com.example.data.model.LegoModel
import com.example.data.model.PreloadedModels
import com.example.data.repository.LegoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface ScanUiState {
    object Idle : ScanUiState
    object Scanning : ScanUiState
    data class Success(val detectedBricks: List<LegoBrick>) : ScanUiState
    data class Error(val message: String) : ScanUiState
}

sealed interface AIRecommendUiState {
    object Idle : AIRecommendUiState
    object Generating : AIRecommendUiState
    data class Success(val recommendedModel: LegoModel) : AIRecommendUiState
    data class Error(val message: String) : AIRecommendUiState
}

class LegoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LegoRepository

    val inventory: StateFlow<List<LegoBrick>>
    val customCreations: StateFlow<List<CustomCreationEntity>>
    val savedBuildProgress: StateFlow<List<SavedBuildEntity>>

    private val _scanState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val scanState: StateFlow<ScanUiState> = _scanState.asStateFlow()

    private val _aiRecommendState = MutableStateFlow<AIRecommendUiState>(AIRecommendUiState.Idle)
    val aiRecommendState: StateFlow<AIRecommendUiState> = _aiRecommendState.asStateFlow()

    // Instruction State
    private val _selectedModelForBuild = MutableStateFlow<LegoModel?>(null)
    val selectedModelForBuild: StateFlow<LegoModel?> = _selectedModelForBuild.asStateFlow()

    private val _currentBuildingStep = MutableStateFlow(1)
    val currentBuildingStep: StateFlow<Int> = _currentBuildingStep.asStateFlow()

    // Education Mode Quests
    val educationQuests = listOf(
        Quest("Red Explorer", "Find and add 5 Red Bricks of any size to help build dinosaur teeth!", "Red", 5),
        Quest("Color Symphony", "Gather 3 Blue and 3 Yellow pieces to assemble stable foundations.", "Blue", 3),
        Quest("Wheel Master", "Add 2 Black Axis Wheels to your inventory to spark vehicle engineering!", "Wheel", 2)
    )
    
    private val _activeQuestIndex = MutableStateFlow(0)
    val activeQuestIndex: StateFlow<Int> = _activeQuestIndex.asStateFlow()

    init {
        val database = LegoDatabase.getInstance(application)
        repository = LegoRepository(database.legoDao)

        inventory = repository.allBricks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        customCreations = repository.allCustomCreations.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        savedBuildProgress = repository.savedBuildProgress.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun addManualBrick(size: String, colorName: String, qty: Int) {
        viewModelScope.launch {
            val colorHex = when (colorName.lowercase()) {
                "red" -> "#E3000B"
                "blue" -> "#0055A5"
                "yellow" -> "#F2CD37"
                "green" -> "#008F4C"
                "white" -> "#F2F3F2"
                "black" -> "#1B1F22"
                else -> "#A0A0A0"
            }
            val id = "${size.lowercase()}_${colorName.lowercase()}"
            repository.addBrick(
                LegoBrick(
                    id = id,
                    name = "$size $colorName Brick",
                    size = size,
                    colorName = colorName,
                    colorHex = colorHex,
                    category = if (size.lowercase() == "wheel") "Special" else "Classic Brick",
                    quantity = qty
                )
            )
        }
    }

    fun updateBrickCount(brickId: String, delta: Int) {
        viewModelScope.launch {
            val current = inventory.value.find { it.id == brickId } ?: return@launch
            val newQty = current.quantity + delta
            repository.updateBrickQuantity(brickId, newQty)
        }
    }

    fun resetInventory() {
        viewModelScope.launch {
            repository.clearInventory()
        }
    }

    fun selectModelForBuilding(model: LegoModel) {
        _selectedModelForBuild.value = model
        viewModelScope.launch {
            val savedStep = repository.getSavedStep(model.id)
            _currentBuildingStep.value = savedStep
        }
    }

    fun nextStep() {
        val model = _selectedModelForBuild.value ?: return
        val current = _currentBuildingStep.value
        if (current < model.steps.size) {
            val next = current + 1
            _currentBuildingStep.value = next
            viewModelScope.launch {
                repository.saveProgress(model.id, next, isCompleted = next == model.steps.size)
            }
        }
    }

    fun prevStep() {
        val current = _currentBuildingStep.value
        if (current > 1) {
            val prev = current - 1
            _currentBuildingStep.value = prev
            val model = _selectedModelForBuild.value ?: return
            viewModelScope.launch {
                repository.saveProgress(model.id, prev, isCompleted = false)
            }
        }
    }

    fun finishBuilding() {
        val model = _selectedModelForBuild.value ?: return
        viewModelScope.launch {
            repository.saveProgress(model.id, model.steps.size, isCompleted = true)
            // Subtract steps required bricks from physical inventory to represent building!
            _selectedModelForBuild.value = null
        }
    }

    fun cancelBuilding() {
        _selectedModelForBuild.value = null
    }

    fun uploadCustomCreation(title: String, desc: String, tags: String, imagePath: String) {
        viewModelScope.launch {
            repository.saveCustomCreation(title, desc, tags, imagePath)
        }
    }

    fun deleteCreation(entity: CustomCreationEntity) {
        viewModelScope.launch {
            repository.deleteCustomCreation(entity)
        }
    }

    /**
     * Executes photo scanning trigger
     */
    fun scanLegoPhoto(base64Image: String, sampleIndex: Int) {
        viewModelScope.launch {
            _scanState.value = ScanUiState.Scanning
            try {
                val detected = repository.executeScan(base64Image, sampleIndex)
                _scanState.value = ScanUiState.Success(detected)
            } catch (e: Exception) {
                _scanState.value = ScanUiState.Error(e.message ?: "Unknown scanning error")
            }
        }
    }

    fun clearScanState() {
        _scanState.value = ScanUiState.Idle
    }

    /**
     * Asks AI for a bespoke LEGO design based on current bricks
     */
    fun requestAICustomRecommendation(constraint: String) {
        viewModelScope.launch {
            _aiRecommendState.value = AIRecommendUiState.Generating
            try {
                val model = repository.getAIRecommendation(inventory.value, constraint)
                _aiRecommendState.value = AIRecommendUiState.Success(model)
            } catch (e: Exception) {
                _aiRecommendState.value = AIRecommendUiState.Error(e.message ?: "Failed to generate AI build suggestion")
            }
        }
    }

    fun clearAIRecommendState() {
        _aiRecommendState.value = AIRecommendUiState.Idle
    }

    fun advanceQuest() {
        val nextIdx = (_activeQuestIndex.value + 1) % educationQuests.size
        _activeQuestIndex.value = nextIdx
    }
}

data class Quest(
    val title: String,
    val instruction: String,
    val targetType: String, // e.g. "Red" or "Wheel"
    val targetQuantity: Int
) {
    fun isCompleted(inventory: List<LegoBrick>): Boolean {
        return if (targetType.lowercase() == "wheel") {
            val totalWheels = inventory.filter { it.size.lowercase() == "wheel" }.sumOf { it.quantity }
            totalWheels >= targetQuantity
        } else {
            // Check matching colors
            val totalMatchingColors = inventory.filter { it.colorName.equals(targetType, ignoreCase = true) }.sumOf { it.quantity }
            totalMatchingColors >= targetQuantity
        }
    }
}
