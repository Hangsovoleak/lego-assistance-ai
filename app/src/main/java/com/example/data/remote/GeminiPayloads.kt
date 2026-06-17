package com.example.data.remote

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PartJson(
    val text: String? = null,
    val inlineData: InlineDataJson? = null
)

@JsonClass(generateAdapter = true)
data class InlineDataJson(
    val mimeType: String,
    val data: String // base64
)

@JsonClass(generateAdapter = true)
data class ContentJson(
    val parts: List<PartJson>
)

@JsonClass(generateAdapter = true)
data class ResponseFormatTextJson(
    val mimeType: String,
    val schema: Map<String, Any>? = null
)

@JsonClass(generateAdapter = true)
data class ResponseFormatJson(
    val text: ResponseFormatTextJson? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfigJson(
    val responseFormat: ResponseFormatJson? = null,
    val temperature: Float? = null,
    val maxOutputTokens: Int? = null
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<ContentJson>,
    val generationConfig: GenerationConfigJson? = null,
    val systemInstruction: ContentJson? = null
)

@JsonClass(generateAdapter = true)
data class PartResponseJson(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class ContentResponseJson(
    val parts: List<PartResponseJson>? = null
)

@JsonClass(generateAdapter = true)
data class CandidateJson(
    val content: ContentResponseJson? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<CandidateJson>? = null
)
