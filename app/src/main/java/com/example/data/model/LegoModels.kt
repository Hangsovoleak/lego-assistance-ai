package com.example.data.model

data class LegoBrick(
    val id: String,
    val name: String, // e.g. "2x4 Red Brick"
    val size: String, // e.g. "2x4", "2x2", "1x4", "1x1", "Wheel"
    val colorName: String, // e.g. "Red", "Blue", "Yellow", "Green", "White", "Black"
    val colorHex: String, // Hex string for rendering
    val category: String, // e.g. "Classic Brick", "Plate", "Wheel", "Special"
    val quantity: Int
)

data class BuildStep(
    val stepNumber: Int,
    val description: String,
    val focusBrickId: String? = null, // ID of the brick being added in this step
    val brickCount: Int = 1,
    val xPos: Int = 0,
    val yPos: Int = 0,
    val zPos: Int = 0
)

data class LegoModel(
    val id: String,
    val name: String,
    val description: String,
    val difficulty: String, // "Easy" (5-10 bricks), "Medium" (10-30 bricks), "Hard" (30+ bricks)
    val estimatedMinutes: Int,
    val requiredBricks: List<RequiredBrick>,
    val steps: List<BuildStep>,
    val educationalFocus: String? = null, // shapes, count, symmetry, etc.
    val category: String = "Creator" // Vehicles, Animals, Buildings, Sci-Fi
) {
    // Helper to check if model can be built with current inventory
    fun getBuildStatus(inventory: List<LegoBrick>): BuildStatus {
        val missingBricks = mutableListOf<MissingBrick>()
        var totalRequiredSum = 0
        var totalAvailableMatchingSum = 0

        for (req in requiredBricks) {
            totalRequiredSum += req.quantity
            val ownedBrick = inventory.find {
                it.size.equals(req.size, ignoreCase = true) &&
                it.colorName.equals(req.colorName, ignoreCase = true)
            }
            val ownedCount = ownedBrick?.quantity ?: 0
            val neededCount = req.quantity
            totalAvailableMatchingSum += minOf(ownedCount, neededCount)

            if (ownedCount < neededCount) {
                missingBricks.add(
                    MissingBrick(
                        size = req.size,
                        colorName = req.colorName,
                        colorHex = req.colorHex,
                        required = neededCount,
                        owned = ownedCount,
                        missingCount = neededCount - ownedCount
                    )
                )
            }
        }

        val buildablePercentage = if (totalRequiredSum > 0) {
            (totalAvailableMatchingSum.toFloat() / totalRequiredSum.toFloat() * 100).toInt()
        } else {
            100
        }

        return BuildStatus(
            isBuildable = missingBricks.isEmpty(),
            missingBricks = missingBricks,
            matchPercentage = buildablePercentage
        )
    }
}

data class RequiredBrick(
    val size: String,
    val colorName: String,
    val colorHex: String,
    val quantity: Int
)

data class MissingBrick(
    val size: String,
    val colorName: String,
    val colorHex: String,
    val required: Int,
    val owned: Int,
    val missingCount: Int
)

data class BuildStatus(
    val isBuildable: Boolean,
    val missingBricks: List<MissingBrick>,
    val matchPercentage: Int
)

// Preloaded beautiful sample Lego collections for kids to "AI scan" easily
data class SampleLegoPile(
    val id: String,
    val title: String,
    val description: String,
    val imageName: String, // for displaying visually
    val items: List<LegoBrick>,
    val promptContext: String // Prompt text that would generate this list
)
