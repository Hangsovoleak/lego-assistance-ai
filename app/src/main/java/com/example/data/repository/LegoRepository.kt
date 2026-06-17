package com.example.data.repository

import android.util.Log
import com.example.data.local.CustomCreationEntity
import com.example.data.local.LegoBrickEntity
import com.example.data.local.LegoDao
import com.example.data.local.SavedBuildEntity
import com.example.data.model.BuildStep
import com.example.data.model.LegoBrick
import com.example.data.model.LegoModel
import com.example.data.model.RequiredBrick
import com.example.data.remote.GeminiClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class LegoRepository(private val legoDao: LegoDao) {

    private val tag = "LegoRepository"

    // Maps database entities to Domain LegoBricks
    val allBricks: Flow<List<LegoBrick>> = legoDao.getAllBricks().map { entities ->
        entities.map { it.toDomain() }
    }

    val allCustomCreations: Flow<List<CustomCreationEntity>> = legoDao.getAllCustomCreations()

    val savedBuildProgress: Flow<List<SavedBuildEntity>> = legoDao.getSavedBuilds()

    suspend fun addBrick(brick: LegoBrick) {
        legoDao.insertBrick(brick.toEntity())
    }

    suspend fun updateBrickQuantity(brickId: String, newQty: Int) {
        val existing = legoDao.getBrickById(brickId)
        if (existing != null) {
            if (newQty <= 0) {
                legoDao.deleteBrick(existing)
            } else {
                legoDao.insertBrick(existing.copy(quantity = newQty))
            }
        }
    }

    suspend fun clearInventory() {
        legoDao.clearAllBricks()
    }

    suspend fun saveCustomCreation(title: String, description: String, tags: String, imagePath: String) {
        legoDao.insertCustomCreation(
            CustomCreationEntity(
                title = title,
                description = description,
                tags = tags,
                imagePath = imagePath
            )
        )
    }

    suspend fun deleteCustomCreation(creation: CustomCreationEntity) {
        legoDao.deleteCustomCreation(creation)
    }

    suspend fun saveProgress(modelId: String, stepNumber: Int, isCompleted: Boolean) {
        legoDao.saveBuildProgress(
            SavedBuildEntity(
                modelId = modelId,
                currentStep = stepNumber,
                isCompleted = isCompleted
            )
        )
    }

    suspend fun getSavedStep(modelId: String): Int {
        return legoDao.getBuildProgress(modelId)?.currentStep ?: 1
    }

    /**
     * Executes AI scanning. Calls Gemini if Key is valid. Falls back to a realistic simulated scan
     * matching the selected sample photo if key is not configured yet.
     */
    suspend fun executeScan(base64Image: String, sampleIndex: Int): List<LegoBrick> {
        val detectedList = mutableListOf<LegoBrick>()

        if (GeminiClient.isKeyValid()) {
            try {
                val jsonResponse = GeminiClient.detectBricks(base64Image)
                Log.d(tag, "Gemini scan raw output: $jsonResponse")

                val jsonArray = JSONArray(jsonResponse)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val size = obj.optString("size", "2x4")
                    val colorName = obj.optString("color", "Red")
                    val quantity = obj.optInt("count", 1)

                    val colorHex = getColorHexFromName(colorName)
                    val id = "${size.lowercase()}_${colorName.lowercase()}"
                    detectedList.add(
                        LegoBrick(
                            id = id,
                            name = "$size $colorName Brick",
                            size = size,
                            colorName = colorName,
                            colorHex = colorHex,
                            category = getCategoryFromSize(size),
                            quantity = quantity
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e(tag, "Gemini scan failed, executing fallback simulated scan", e)
                return performSimulatedScan(sampleIndex)
            }
        } else {
            // Placeholder/simulated scan for demo
            return performSimulatedScan(sampleIndex)
        }

        // Add scanned items to database, merging quantities if exists
        for (scanned in detectedList) {
            val existing = legoDao.getBrickById(scanned.id)
            val mergedQty = (existing?.quantity ?: 0) + scanned.quantity
            legoDao.insertBrick(scanned.toEntity().copy(quantity = mergedQty))
        }

        return detectedList
    }

    private fun performSimulatedScan(sampleIndex: Int): List<LegoBrick> {
        // Different layouts for mock files to simulate realistic LEGO piles!
        val simulations = listOf(
            // Sample 1: Rainbow Mix
            listOf(
                LegoBrick("2x4_red", "2x4 Red Brick", "2x4", "Red", "#E3000B", "Classic Brick", 5),
                LegoBrick("2x2_blue", "2x2 Blue Brick", "2x2", "Blue", "#0055A5", "Classic Brick", 4),
                LegoBrick("1x4_yellow", "1x4 Yellow Brick", "1x4", "Yellow", "#F2CD37", "Classic Brick", 6),
                LegoBrick("2x2_white", "2x2 White Brick", "2x2", "White", "#F2F3F2", "Classic Brick", 3),
                LegoBrick("wheel_black", "Wheel Axis Black", "Wheel", "Black", "#1B1F22", "Special", 2)
            ),
            // Sample 2: Vehicle Core
            listOf(
                LegoBrick("2x4_yellow", "2x4 Yellow Brick", "2x4", "Yellow", "#F2CD37", "Classic Brick", 4),
                LegoBrick("1x4_red", "1x4 Red Brick", "1x4", "Red", "#E3000B", "Classic Brick", 8),
                LegoBrick("2x2_green", "2x2 Green Brick", "2x2", "Green", "#008F4C", "Classic Brick", 5),
                LegoBrick("wheel_black", "Wheel Axis Black", "Wheel", "Black", "#1B1F22", "Special", 4),
                LegoBrick("1x1_white", "1x1 White Plate", "1x1", "White", "#F2F3F2", "Plate", 6)
            ),
            // Sample 3: Dino/Forest Build
            listOf(
                LegoBrick("2x2_green", "2x2 Green Brick", "2x2", "Green", "#008F4C", "Classic Brick", 8),
                LegoBrick("2x4_green", "2x4 Green Brick", "2x4", "Green", "#008F4C", "Classic Brick", 4),
                LegoBrick("1x4_yellow", "1x4 Yellow Brick", "1x4", "Yellow", "#F2CD37", "Classic Brick", 5),
                LegoBrick("2x2_red", "2x2 Red Brick", "2x2", "Red", "#E3000B", "Classic Brick", 3),
                LegoBrick("1x1_black", "1x1 Black Brick", "1x1", "Black", "#1B1F22", "Classic Brick", 4)
            )
        )

        val selected = simulations.getOrElse(sampleIndex % simulations.size) { simulations[0] }
        return selected
    }

    /**
     * Recommends a custom LEGO build model. Calls the Gemini AI system to draft one of high creativity,
     * or triggers a beautiful offline mock generator.
     */
    suspend fun getAIRecommendation(inventory: List<LegoBrick>, promptConstraint: String): LegoModel {
        if (GeminiClient.isKeyValid()) {
            try {
                // Map inventory to minimal lightweight JSON representing the bricks
                val invArray = JSONArray()
                for (brick in inventory) {
                    val obj = JSONObject()
                    obj.put("size", brick.size)
                    obj.put("color", brick.colorName)
                    obj.put("qty", brick.quantity)
                    invArray.put(obj)
                }

                val aiResponse = GeminiClient.recommendCustomModel(invArray.toString(2), promptConstraint)
                Log.d(tag, "Gemini recommendation raw response: $aiResponse")

                val json = JSONObject(aiResponse)
                val modelId = json.optString("id", UUID.randomUUID().toString())
                val name = json.optString("name", "Custom AI Model")
                val description = json.optString("description", "A creative model generated dynamically by your Lego Assistant!")
                val difficulty = json.optString("difficulty", "Medium")
                val estimatedMinutes = json.optInt("estimatedMinutes", 15)
                val educationalFocus = json.optString("educationalFocus", "General Engineering")
                val category = json.optString("category", "Creator")

                // Parse required bricks
                val reqBricks = mutableListOf<RequiredBrick>()
                val reqArray = json.optJSONArray("requiredBricks")
                if (reqArray != null) {
                    for (i in 0 until reqArray.length()) {
                        val item = reqArray.getJSONObject(i)
                        reqBricks.add(
                            RequiredBrick(
                                size = item.optString("size", "2x2"),
                                colorName = item.optString("colorName", "Red"),
                                colorHex = item.optString("colorHex", "#E3000B"),
                                quantity = item.optInt("quantity", 1)
                            )
                        )
                    }
                }

                // Parse steps
                val steps = mutableListOf<BuildStep>()
                val stepsArray = json.optJSONArray("steps")
                if (stepsArray != null) {
                    for (i in 0 until stepsArray.length()) {
                        val item = stepsArray.getJSONObject(i)
                        steps.add(
                            BuildStep(
                                stepNumber = item.optInt("stepNumber", i + 1),
                                description = item.optString("description", "Build step"),
                                focusBrickId = item.optString("focusBrickId", null),
                                brickCount = item.optInt("brickCount", 1)
                            )
                        )
                    }
                }

                return LegoModel(
                    id = modelId,
                    name = name,
                    description = description,
                    difficulty = difficulty,
                    estimatedMinutes = estimatedMinutes,
                    requiredBricks = reqBricks,
                    steps = steps,
                    educationalFocus = educationalFocus,
                    category = category
                )
            } catch (e: Exception) {
                Log.e(tag, "Gemini recommendation failed, loading fallback local recommendations", e)
            }
        }

        // Offline creative generation helper
        return getOfflineModelRecommendation(inventory, promptConstraint)
    }

    private fun getOfflineModelRecommendation(inventory: List<LegoBrick>, constraint: String): LegoModel {
        // Generate a fun design aligning with search keywords if possible
        val c = constraint.lowercase()
        return when {
            c.contains("car") || c.contains("vehicle") || c.contains("truck") -> {
                LegoModel(
                    id = "ai_mini_racer",
                    name = "Lunar Buggy Mobile",
                    description = "An aerospace racer with rear spoilers and low center of gravity. Perfect for traversing asteroid belts!",
                    difficulty = "Easy",
                    estimatedMinutes = 8,
                    requiredBricks = listOf(
                        RequiredBrick("2x4", "Yellow", "#F2CD37", 2),
                        RequiredBrick("1x4", "Red", "#E3000B", 2),
                        RequiredBrick("Wheel", "Black", "#1B1F22", 2)
                    ),
                    steps = listOf(
                        BuildStep(1, "Place the two Yellow 2x4 bricks side-by-side as the chassis of the moon rocket racer."),
                        BuildStep(2, "Attach the two Black Wheels beneath the chassis for quick galactic movement."),
                        BuildStep(3, "Secure Red 1x4 bricks as safety bumpers on the front and rear edges.")
                    ),
                    educationalFocus = "Friction & Motion",
                    category = "Vehicles"
                )
            }
            c.contains("dino") || c.contains("animal") || c.contains("bird") || c.contains("puppy") -> {
                LegoModel(
                    id = "ai_dino",
                    name = "Stegosaurus Guard",
                    description = "A cute little Jurassic dinosaur with defensive green spikes and a heavy tail. Enjoys munching lego shrubbery!",
                    difficulty = "Medium",
                    estimatedMinutes = 12,
                    requiredBricks = listOf(
                        RequiredBrick("2x4", "Green", "#008F4C", 2),
                        RequiredBrick("2x2", "Green", "#008F4C", 3),
                        RequiredBrick("1x4", "Yellow", "#F2CD37", 2)
                    ),
                    steps = listOf(
                        BuildStep(1, "Combine Green 2x4 bricks in a row to assemble the massive dinosaur body."),
                        BuildStep(2, "Mount three Green 2x2 bricks along the spine to create stegosaurus plates."),
                        BuildStep(3, "Use Yellow 1x4 plates as the legs below the dinosaur body.")
                    ),
                    educationalFocus = "Symmetry & Center of Mass",
                    category = "Animals"
                )
            }
            else -> {
                // Default: Friendly AI Robot helper
                LegoModel(
                    id = "ai_robot_buddy",
                    name = "Robotic Companion Bot",
                    description = "A friendly humanoid robot assistant who blinks lights and is ready to help clean up the toy room!",
                    difficulty = "Medium",
                    estimatedMinutes = 15,
                    requiredBricks = listOf(
                        RequiredBrick("2x4", "Red", "#E3000B", 2),
                        RequiredBrick("2x2", "Blue", "#0055A5", 2),
                        RequiredBrick("1x4", "Yellow", "#F2CD37", 1)
                    ),
                    steps = listOf(
                        BuildStep(1, "Stand a Blue 2x2 brick vertically. This is Robot Buddy's high-tech torso."),
                        BuildStep(2, "Place a Red 2x4 brick on top horizontally to form a wide, smiling pair of shoulders."),
                        BuildStep(3, "Secure the Yellow 1x4 brick on top of the shoulders to serve as the computing sensors and eyes!")
                    ),
                    educationalFocus = "Balance & Structural Support",
                    category = "Sci-Fi"
                )
            }
        }
    }

    // UTILITIES
    private fun LegoBrickEntity.toDomain() = LegoBrick(
        id = id,
        name = name,
        size = size,
        colorName = colorName,
        colorHex = colorHex,
        category = category,
        quantity = quantity
    )

    private fun LegoBrick.toEntity() = LegoBrickEntity(
        id = id,
        name = name,
        size = size,
        colorName = colorName,
        colorHex = colorHex,
        category = category,
        quantity = quantity
    )

    private fun getColorHexFromName(name: String): String {
        return when (name.lowercase()) {
            "red" -> "#E3000B"
            "blue" -> "#0055A5"
            "yellow" -> "#F2CD37"
            "green" -> "#008F4C"
            "white" -> "#F2F3F2"
            "black" -> "#1B1F22"
            else -> "#A0A0A0"
        }
    }

    private fun getCategoryFromSize(size: String): String {
        return when {
            size.equals("wheel", ignoreCase = true) -> "Special"
            size.contains("plate", ignoreCase = true) || size.contains("1x1") -> "Plate"
            else -> "Classic Brick"
        }
    }
}
