package com.example.ai_macrofy

import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ServiceCompat
import androidx.core.app.ServiceCompat.stopForeground
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {
    private lateinit var textViewResult: TextView
    private lateinit var editTextPrompt: EditText
    private lateinit var editTextApiKey: EditText
    private lateinit var sharedPreferences: SharedPreferences
    private var isCapturing = false // 매크로 상태 플래그
    private val REQUEST_MEDIA_PROJECTION = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textViewResult = findViewById(R.id.textView_result)
        editTextPrompt = findViewById(R.id.editText_prompt)
        editTextApiKey = findViewById(R.id.editText_api_key)
        val buttonSaveApiKey = findViewById<Button>(R.id.button_save_api_key)
        val buttonStartMacro = findViewById<Button>(R.id.button_start_macro)
        val buttonStopMacro = findViewById<Button>(R.id.button_stop_macro)

        // SharedPreferences 초기화
        sharedPreferences = getSharedPreferences("AiMacrofyPrefs", Context.MODE_PRIVATE)
        editTextApiKey.setText(loadApiKey())

        // Start Macro 버튼 클릭
        buttonStartMacro.setOnClickListener {
            // 접근성 권한 확인
            if (!isAccessibilityServiceEnabled()) {
                Toast.makeText(this, "Enable Ai_macrofy Accessibility Service.", Toast.LENGTH_LONG).show()
                startActivity(Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS))
            } else {
                // 접근성 권한이 활성화된 경우 MediaProjection 권한 요청
             /*   if (mediaProjection == null) {
                    startActivityForResult(mediaProjectionManager!!.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION)
                }*/
                // MediaProjection 권한이 이미 허용된 경우 매크로 시작
                if (!isCapturing){
                    isCapturing = true
                    Log.d("buttonStartMacro","capture start"+isCapturing)
                    startMacro()
                }else{
                    Toast.makeText(this, "Macro is already running.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Stop Macro 버튼 클릭
        buttonStopMacro.setOnClickListener {
            if(isCapturing) {
                isCapturing = false
                Log.d("buttonStopMacro", "capture stoped" + isCapturing)
                stopForeground(
                    MyForegroundService.instance!!,
                    ServiceCompat.STOP_FOREGROUND_REMOVE
                ); // 포그라운드 알림을 제거하고 서비스를 중단합니다.
                stopService(Intent(this, MyForegroundService::class.java))
                textViewResult.text = "Macro Stopped"
            }else{

                Toast.makeText(this, "Macro is not running.", Toast.LENGTH_SHORT).show()
            }
        }

        // API 키 저장 버튼
        buttonSaveApiKey.setOnClickListener {
            val apiKey = editTextApiKey.text.toString().trim()
            if (apiKey.isNotEmpty()) {
                saveApiKey(apiKey)
                Toast.makeText(this, "API Key saved.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enter a valid API Key.", Toast.LENGTH_SHORT).show()
            }
        }
    }
/*    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_MEDIA_PROJECTION && resultCode == Activity.RESULT_OK && data != null) {
            val serviceIntent = Intent(this, ScreenCaptureService::class.java).apply {
                putExtra("resultCode", resultCode)
                putExtra("data", data)
            }
            startForegroundService(serviceIntent) // Foreground Service 시작
        } else {
            Toast.makeText(this, "Screen capture permission denied.", Toast.LENGTH_SHORT).show()
        }
    }*/


    private fun isAccessibilityServiceEnabled2(): Boolean {
        val enabledServices = android.provider.Settings.Secure.getString(
            contentResolver,
            android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return enabledServices?.contains("$packageName/${LayoutAccessibilityService::class.java.canonicalName}") == true
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val accessibilityManager =
            getSystemService(Context.ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager
        val enabledServices =
            Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        val service = "$packageName/${MacroAccessibilityService::class.java.canonicalName}"
        return enabledServices?.contains(service) == true
    }


    private fun startMacro() {
        val additionalPrompt = editTextPrompt.text.toString().trim()
        val apiKey = editTextApiKey.text.toString()
        // 기본 프롬프트
        val prompt = """
            You are an assistant performing automated tasks. The user provided the following context:
            "$additionalPrompt"
   
            You should know that the original screen size was x: 1440 y: 3200 and the coordinate system starts at (0,0) at the top-left corner and ends at (1440,3200) at the bottom-right corner.
    
            If the current screen is the same as the previous one, do not perform the same action again.
            (Ex: If you previously performed a touch action at coordinates {"x":720,"y":794}, and the screen has not changed, skip that action and do not repeat it. )
            
            Based on the layout JSON, provide step-by-step instructions in JSON format, such as:
            {"actions":[
                {"type":"touch","coordinates":{"x":100,"y":200}},
                {"type":"input","text":"example@example.com","coordinates":{"x":50,"y":300}},
                {"type":"scroll","direction":"down","distance":500},
                {"type":"scroll","direction":"up","distance":300},
                {"type":"long_touch","coordinates":{"x":150,"y":250},"duration":2000},
                {"type":"drag_and_drop","start":{"x":100,"y":100},"end":{"x":400,"y":400},"duration":1500},
                {"type":"double_tap","coordinates":{"x":200,"y":300}},
                {"type":"swipe","start":{"x":300,"y":300},"end":{"x":600,"y":600},"duration":800},
                {"type":"gesture","name":"back"},
                {"type":"gesture","name":"home"},
                {"type":"gesture","name":"recent_apps"}
            ]}
    
            Always output exactly one action in the above format.
    
            Note:
            - FrameLayout elements are non-clickable, so do not perform a touch action on them.
            - If the current screen does not match expectations, output: {"actions":[{"type":"gesture","name":"back"}]}
        """.trimIndent()

        Log.d("startMacro()","variable initiated")

        if (additionalPrompt.isEmpty()) {
            textViewResult.text = "Please enter a prompt to start macro."
            return
        }
        if (apiKey.isEmpty()) {
            textViewResult.text = "Please enter a api key to start macro."
            return
        }

        // 매크로 동작 (화면 캡처 및 GPT 요청)
        val intent = Intent(this, MyForegroundService::class.java).apply {
            putExtra("apiKey", apiKey)
            putExtra("prompt", prompt)
        }

        startService(intent)
    }

    private fun saveApiKey(apiKey: String) {
        sharedPreferences.edit().putString("API_KEY", apiKey).apply()
    }

    private fun loadApiKey(): String {
        return sharedPreferences.getString("API_KEY", "") ?: ""
    }


}
