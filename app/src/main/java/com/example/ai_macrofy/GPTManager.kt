package com.example.ai_macrofy

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

object GPTManager {
    private const val BASE_URL = "https://api.openai.com/"
    private var apiKey: String? = null

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val api by lazy {
        retrofit.create(GPTApi::class.java)
    }

    fun setApiKey(key: String) {
        apiKey = key
    }

    suspend fun getResponseFromGPT(question: String): String {

        if (apiKey.isNullOrEmpty()) throw IllegalStateException("API key is not set")

        // 요청 생성
        val request = GPTRequest(
            model = "gpt-4o-mini",
            messages = listOf(
                Message(role = "user", content = processInstructions(question))
            )
        )

        return withContext(Dispatchers.IO) {
            try {
                // API 호출
                val response = api.getResponse("Bearer $apiKey", request)
                val choices = response.choices

                // 응답에서 첫 번째 선택지를 추출
                val firstChoice = choices.firstOrNull()
                    ?: return@withContext "No response received from GPT."

                // 선택지의 메시지 내용 반환
                firstChoice.message.content
            }catch (e: retrofit2.HttpException) {
                // 예외 처리 및 디버깅 출력
                Log.e("GPTManager", "HTTP error: ${e.code()} - ${e.message()}")
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("GPTManager", "Error body: $errorBody")
               "Error occurred: ${e.message}"
            }
            catch (e: Exception) {
                // 예외 처리 및 디버깅 출력
                Log.e("GPTManager", "Error during GPT request: ${e.message}", e)
                "Error occurred: ${e.message}"
            }
        }
    }
    fun processInstructions(input: String): String {
        // 특수 문자를 이스케이프 처리하기
        val escapedInput = input
            .replace("\\", "\\\\")  // 역슬래시 이스케이프
            .replace("\"", "\\\"")  // 큰따옴표 이스케이프
            .replace("\n", "\\n")   // 줄바꿈 이스케이프
            .replace("\r", "\\r")   // 캐리지 리턴 이스케이프
            .replace("\t", "\\t")   // 탭 문자 이스케이프

        // 프로세싱된 문자열을 그대로 JSON 포맷으로 반환
        return """
            {
                "instructions": "$escapedInput"
            }
        """.trimIndent()
    }
}

interface GPTApi {
    @POST("v1/chat/completions")
    suspend fun getResponse(
        @retrofit2.http.Header("Authorization") authorization: String,
        @Body request: GPTRequest
    ): GPTResponse
}

data class GPTRequest(val model: String, val messages: List<Message>)
data class Message(val role: String, val content: String)
data class GPTResponse(val choices: List<Choice>)
data class Choice(val message: Message)
