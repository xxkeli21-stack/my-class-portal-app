package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // Moshi JSON models
    data class RequestBodyModel(
        val contents: List<ContentModel>,
        val systemInstruction: ContentModel? = null
    )

    data class ContentModel(
        val parts: List<PartModel>
    )

    data class PartModel(
        val text: String
    )

    data class ResponseBodyModel(
        val candidates: List<CandidateModel>?
    )

    data class CandidateModel(
        val content: ContentModel?
    )

    suspend fun generateResponse(prompt: String, systemPrompt: String = "You are a helpful AI Study Assistant for My Class Portal. Help the student with their school subjects."): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Please configure a valid GEMINI_API_KEY in the AI Studio Secrets panel to activate the AI Study Assistant."
        }

        val requestObj = RequestBodyModel(
            contents = listOf(
                ContentModel(parts = listOf(PartModel(text = prompt)))
            ),
            systemInstruction = ContentModel(parts = listOf(PartModel(text = systemPrompt)))
        )

        val jsonAdapter = moshi.adapter(RequestBodyModel::class.java)
        val responseAdapter = moshi.adapter(ResponseBodyModel::class.java)

        val requestJson = jsonAdapter.toJson(requestObj)
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = requestJson.toRequestBody(mediaType)

        val url = "$BASE_URL?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext "API call failed with code: ${response.code}. Please verify your API Key in the Secrets panel."
                }
                val responseBodyStr = response.body?.string() ?: return@withContext "Received an empty response from Gemini."
                val responseObj = responseAdapter.fromJson(responseBodyStr)
                val reply = responseObj?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                return@withContext reply ?: "I'm sorry, I couldn't formulate a response. Please try again."
            }
        } catch (e: Exception) {
            return@withContext "Error: Could not connect to Gemini API. ${e.localizedMessage}"
        }
    }
}
