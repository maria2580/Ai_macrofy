package com.example.ai_macrofy

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.security.MessageDigest

class MyForegroundService : Service() {

    companion object {
        var instance: MyForegroundService? = null
    }

    // CoroutineScope를 사용하여 백그라운드에서 작업 처리
    private val serviceScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        instance = this
        super.onCreate()
    }

    // onStartCommand에서 API 키와 프롬프트 추출
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val apiKey = intent?.getStringExtra("apiKey") ?: ""
        val prompt = intent?.getStringExtra("prompt") ?: ""

        // 코루틴 작업 시작
        serviceScope.launch {
            runMacroTasks(apiKey, prompt)
        }

        // 포그라운드 서비스 시작
        startForegroundService()

        // START_STICKY: 서비스가 종료되어도 재시작
        return START_STICKY
    }

    // 코루틴 작업 예시: 백그라운드에서 작업을 반복적으로 실행
    private suspend fun runMacroTasks(apiKey: String, prompt: String) {
        val layoutDataPairs = mutableListOf<Pair<String, String>>()
        while (true) {
            Log.d("runMacroTasks in while()", "동작하는중~~~") // 응답 출력
            // 작업을 수행할 주기적인 코드 작성
            val layoutData = LayoutAccessibilityService.instance?.extractLayoutInfo()
            val gptResponse = sendToGPT(apiKey, prompt, layoutData,layoutDataPairs)
            // UI 스레드에서 결과 처리
            withContext(Dispatchers.Main) {
                // 예시로 로그에 출력
                Log.d("GPT Response", gptResponse) // 응답 출력
                try {
                    layoutDataPairs.add(Pair(layoutData.toString().length.toString(), gptResponse))
                    MacroAccessibilityService.instance?.executeActionsFromJson(gptResponse)
                } catch (e: JSONException) {
                    Log.e("JSON Error", "Failed to parse response: $gptResponse")
                }
                delay(1000) // 10초마다 작업 반복

            }
            // 일정 시간 대기 후 다시 작업을 수행

        }
    }

    // 포그라운드 서비스 알림 생성
    private fun startForegroundService() {
        val channelId = "com.example.ai_macrofy.foreground_channel"

        // NotificationChannel 생성 (API 26 이상에서 필요)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Channel for foreground service"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(notificationChannel)
        }

        val notification = createNotification(channelId)
        startForeground(1, notification) // 포그라운드 서비스로 전환
    }

    // 알림 생성
    private fun createNotification(channelId: String): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Macro is running")
            .setContentText("Your macro is being executed in the background.")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null  // 바인딩을 사용하지 않으면 null 반환
    }

    override fun onDestroy() {
        super.onDestroy()
        // 서비스 종료 시 코루틴 작업 취소
        serviceScope.cancel()
        instance = null  // 서비스 종료 시 인스턴스를 null로 설정
    }

    // GPT에 요청을 보내는 함수
    private suspend fun sendToGPT(apiKey: String, prompt: String, layoutData: JSONObject?, layoutDataPairs: MutableList<Pair<String, String>>): String {
    val layoutInfo = layoutData?.toString() ?: "No layout data available"
    val context = layoutDataPairs.toString() ?: "No context data available"
    val combinedPrompt = """
        $prompt
        I append the context data to the prompt. the list of pair of previous layout hash and your action were: $context
        and now hash of layout is ${layoutInfo.length}
        ### To prevent repeating actions:
            - Compare the current layout hash with the list of previously processed hashes and their corresponding actions.
            - If the current screen layout hash is in the list of previously processed layouts, output:
              {"actions":[{"type":"gesture","name":"back"}]}
            - If the current screen layout has changed and is not in the list, proceed with a new action based on the layout.

        You must analyze the current layout hash and compare it with the previous layout hashes and your answers
        
        - Identify whether the current screen matches the intended screen (e.g., Instagram homepage, app opened, etc.).
        - If the screen has changed, proceed with the next step.
        - If the task has been completed, return {"actions":[{"type":"done"}]}.
        - If the task hasn't been completed yet, determine what needs to be done next.
        - If you have done with your best effort, return {"actions":[{"type":"done"}]}.
        
        Layout Analysis:
        $layoutInfo
    """.trimIndent()

    Log.d("GPT Request", "Sending request: $combinedPrompt")  // 디버깅 로그 추가

    return withContext(Dispatchers.IO) {
            try {
                GPTManager.setApiKey(apiKey)
                val response = GPTManager.getResponseFromGPT(combinedPrompt).replace("```json", "").replace("```", "").trim()  // GPT API 호출
                Log.d("GPT Response", response)  // 응답 로그
                return@withContext response
            } catch (e: Exception) {
                Log.e("GPTManager", "Error during GPT request: ${e.message}", e)
                return@withContext "Error occurred: ${e.message}"
            }
        }
    }



}
