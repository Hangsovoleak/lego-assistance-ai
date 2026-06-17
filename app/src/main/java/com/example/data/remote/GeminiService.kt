package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface RetrofitGeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val api: RetrofitGeminiApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(RetrofitGeminiApi::class.java)
    }

    // Helper to check if the current configured key is a valid key or placeholder
    fun isKeyValid(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return key.isNotEmpty() && !key.startsWith("MY_") && key != "placeholder"
    }

    /**
     * Calls Gemini to detect Lego bricks in an image.
     */
    suspend fun detectBricks(base64Image: String): String {
        if (!isKeyValid()) {
            throw IllegalStateException("Gemini API key is not configured. Please add your key in the AI Studio Secrets panel.")
        }

        val systemInstruction = "You are a specialized Lego Detector assistant. Evaluate the provided photo of Lego bricks/piles. Identify every individual Lego brick type, color, size, and quantity. Reply ONLY in a structured JSON array of lego pieces. No markdown formatting, No explanation."

        val prompt = "Analyze the Lego bricks in this image. List all pieces. Ensure name is formatted like 'Size Color Brick'. e.g. '2x4 Red Brick'. Size can be basic like '2x4', '2x2', '1x4', '1x1', or 'Wheel'. Color should be standard: Red, Blue, Yellow, Green, White, Black."

        // Standard direct REST schema matching Example 3
        val schema = mapOf(
            "type" to "ARRAY",
            "items" to mapOf(
                "type" to "OBJECT",
                "properties" to mapOf(
                    "size" to mapOf("type" to "STRING", "description" to "Size format like 2x4, 2x2, 1x4, 1x2, 1x1, or Wheel"),
                    "color" to mapOf("type" to "STRING", "description" to "Standard color name: Red, Blue, Yellow, Green, White, Black"),
                    "count" to mapOf("type" to "INTEGER", "description" to "How many of this brick type exist")
                ),
                "required" to listOf("size", "color", "count")
            )
        )

        val request = GeminiRequest(
            contents = listOf(
                ContentJson(
                    parts = listOf(
                        PartJson(text = prompt),
                        PartJson(inlineData = InlineDataJson(mimeType = "image/jpeg", data = base64Image))
                    )
                )
            ),
            generationConfig = GenerationConfigJson(
                responseFormat = ResponseFormatJson(
                    text = ResponseFormatTextJson(mimeType = "application/json", schema = schema)
                ),
                temperature = 0.2f
            ),
            systemInstruction = ContentJson(parts = listOf(PartJson(text = systemInstruction)))
        )

        val response = api.generateContent(BuildConfig.GEMINI_API_KEY, request)
        return response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw Exception("Empty response from Gemini API")
    }

    /**
     * Calls Gemini to recommend a custom building model from current list of available bricks.
     */
    suspend fun recommendCustomModel(inventoryJson: String, promptConstraint: String): String {
        if (!isKeyValid()) {
            throw IllegalStateException("Gemini API key is not configured.")
        }

        val systemPrompt = "You are an expert LEGO constructor and educational STEM instructor. Suggest an incredibly creative model that can be easily made from the user's available brick inventory. Make it fun, engaging, and age-appropriate (for children 3-7 and school students)."

        val prompt = """
            My current LEGO brick inventory is:
            $inventoryJson
            
            Additional prompt requirements: $promptConstraint
            
            Please suggest a single, awesome, educational and creative LEGO model that can be buildable with these pieces. 
            Return a single structured JSON object conforming precisely to LegoModel schema. Include name, description, difficulty ("Easy", "Medium", or "Hard"), estimatedMinutes, educationalFocus, and exactly matching step-by-step building instructions. Keep building steps simple for school kids. Every step must name the shape and color of bricks added.
        """.trimIndent()

        // Configure strict JSON output schema
        val schema = mapOf(
            "type" to "OBJECT",
            "properties" to mapOf(
                "id" to mapOf("type" to "STRING", "description" to "Unique string identifier for the recommended model"),
                "name" to mapOf("type" to "STRING", "description" to "Creative name of the lego model, e.g. Mini Rocket, Turbo Plane, Little Dino"),
                "description" to mapOf("type" to "STRING", "description" to "Delightful child-friendly description of this build"),
                "difficulty" to mapOf("type" to "STRING", "description" to "Difficulty: Easy, Medium, or Hard"),
                "estimatedMinutes" to mapOf("type" to "INTEGER", "description" to "Estimated building duration in minutes"),
                "educationalFocus" to mapOf("type" to "STRING", "description" to "An educational topic this model teaches, e.g. Counting, Gravity, Balance, Symmetry, Colors"),
                "category" to mapOf("type" to "STRING", "description" to "Category e.g. Vehicles, Animals, Space, Buildings"),
                "requiredBricks" to mapOf(
                    "type" to "ARRAY",
                    "items" to mapOf(
                        "type" to "OBJECT",
                        "properties" to mapOf(
                            "size" to mapOf("type" to "STRING", "description" to "Dimensions e.g. 2x4, 2x2, 1x4"),
                            "colorName" to mapOf("type" to "STRING", "description" to "Color e.g. Red, Blue"),
                            "colorHex" to mapOf("type" to "STRING", "description" to "Hex representation e.g. #FF0000"),
                            "quantity" to mapOf("type" to "INTEGER")
                        ),
                        "required" to listOf("size", "colorName", "colorHex", "quantity")
                    )
                ),
                "steps" to mapOf(
                    "type" to "ARRAY",
                    "items" to mapOf(
                        "type" to "OBJECT",
                        "properties" to mapOf(
                            "stepNumber" to mapOf("type" to "INTEGER"),
                            "description" to mapOf("type" to "STRING", "description" to "Clear step guides, e.g. 'Place a Red 2x4 brick on flat table as foundation'"),
                            "brickCount" to mapOf("type" to "INTEGER", "description" to "How many bricks added in this step"),
                            "focusBrickId" to mapOf("type" to "STRING", "description" to "Size/color of the main brick added or null")
                        ),
                        "required" to listOf("stepNumber", "description", "brickCount")
                    )
                )
            ),
            "required" to listOf("id", "name", "description", "difficulty", "estimatedMinutes", "requiredBricks", "steps")
        )

        val request = GeminiRequest(
            contents = listOf(ContentJson(parts = listOf(PartJson(text = prompt)))),
            generationConfig = GenerationConfigJson(
                responseFormat = ResponseFormatJson(
                    text = ResponseFormatTextJson(mimeType = "application/json", schema = schema)
                ),
                temperature = 0.7f
            ),
            systemInstruction = ContentJson(parts = listOf(PartJson(text = systemPrompt)))
        )

        val response = api.generateContent(BuildConfig.GEMINI_API_KEY, request)
        return response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw Exception("Empty response from AI Recommender service")
    }
}
