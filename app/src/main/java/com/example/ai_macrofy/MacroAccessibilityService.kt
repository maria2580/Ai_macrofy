package com.example.ai_macrofy

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import org.json.JSONObject
import android.graphics.Rect
import android.widget.Toast
import androidx.core.app.ServiceCompat
import org.json.JSONException

class MacroAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "MacroAccessibilityService"
        var instance: MacroAccessibilityService?  = null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or AccessibilityEvent.TYPE_VIEW_CLICKED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        }
        serviceInfo = info
        instance = this // 서비스 인스턴스 저장
        Log.d(TAG, "Service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // 이벤트 처리 필요 없음
    }

    override fun onInterrupt() {
        Log.w(TAG, "Service interrupted")
    }

    /**
     * JSON 데이터로부터 명령을 수행
     */
    fun executeActionsFromJson(json: String) {
        try {

            val actions = JSONObject(json).optJSONArray("actions")
            if (actions == null || actions.length() == 0) {
                Log.w(TAG, "No actions found in JSON.")
                return
            }

            for (i in 0 until actions.length()) {
                val action = actions.getJSONObject(i)
                val type = action.getString("type")

                when (type) {
                    "touch" -> handleTouch(action)
                    "input" -> handleInput(action)
                    "scroll" -> handleScroll(action)
                    "long_touch" -> handleLongTouch(action)
                    "drag_and_drop" -> handleDragAndDrop(action)
                    "double_tap" -> handleDoubleTap(action)
                    "swipe" -> handleSwipe(action)
                    "gesture" -> handleGesture(action)
                    "done" -> handleDone()
                    else -> Log.w(TAG, "Unknown action type: $type")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse or execute actions: ${e.message}", e)
        }catch (e: JSONException) {
            Log.e("JSON Error", "Failed to parse response: ${e.message}")
            // Handle the error case gracefully, e.g., show an error message to the user
        }
    }

    private fun handleTouch(action: JSONObject) {
        val x = action.getJSONObject("coordinates").getInt("x")
        val y = action.getJSONObject("coordinates").getInt("y")
        performTouch(x, y)
    }

    private fun handleInput(action: JSONObject) {
        val text = action.getString("text")
        val x = action.getJSONObject("coordinates").getInt("x")
        val y = action.getJSONObject("coordinates").getInt("y")
        performInput(text, x, y)
    }

    private fun handleScroll(action: JSONObject) {
        val direction = action.getString("direction")
        val distance = action.getInt("distance")
        performScroll(direction, distance)
    }

    private fun handleLongTouch(action: JSONObject) {
        val x = action.getJSONObject("coordinates").getInt("x")
        val y = action.getJSONObject("coordinates").getInt("y")
        val duration = action.getInt("duration")
        performLongTouch(x, y, duration.toLong())
    }

    private fun handleDragAndDrop(action: JSONObject) {
        val startX = action.getJSONObject("start").getInt("x")
        val startY = action.getJSONObject("start").getInt("y")
        val endX = action.getJSONObject("end").getInt("x")
        val endY = action.getJSONObject("end").getInt("y")
        val duration = action.getInt("duration")
        performDragAndDrop(startX, startY, endX, endY, duration.toLong())
    }

    private fun handleDoubleTap(action: JSONObject) {
        val x = action.getJSONObject("coordinates").getInt("x")
        val y = action.getJSONObject("coordinates").getInt("y")
        performDoubleTap(x, y)
    }

    private fun handleSwipe(action: JSONObject) {
        val startX = action.getJSONObject("start").getInt("x")
        val startY = action.getJSONObject("start").getInt("y")
        val endX = action.getJSONObject("end").getInt("x")
        val endY = action.getJSONObject("end").getInt("y")
        val duration = action.getInt("duration")
        performSwipe(startX, startY, endX, endY, duration.toLong())
    }

    private fun handleGesture(action: JSONObject) {
        val name = action.getString("name")
        when (name) {
            "back" -> performGlobalAction(GLOBAL_ACTION_BACK)
            "home" -> performGlobalAction(GLOBAL_ACTION_HOME)
            "recent_apps" -> performGlobalAction(GLOBAL_ACTION_RECENTS)
            else -> Log.w(TAG, "Unknown gesture name: $name")
        }
    }

    private fun handleDone() {
        // 완료 이벤트 처리
        Log.d(TAG, "Execution completed.")
        ServiceCompat.stopForeground(MyForegroundService.instance!!, ServiceCompat.STOP_FOREGROUND_REMOVE) // 포그라운드 알림을 제거하고 서비스를 중단합니다.
        stopService(Intent(this, MyForegroundService::class.java))
        Toast.makeText(this, "완료했습니다.", Toast.LENGTH_LONG).show()
    }
    /**
     * 화면 특정 좌표를 터치
     */
    private fun performTouch(x: Int, y: Int) {
        val rootNode = rootInActiveWindow
        if (rootNode != null) {
            val targetNode = findNodeAtCoordinates(rootNode, x, y)
            if (targetNode != null) {
                Log.d(TAG, "Node found at ($x, $y): ${targetNode.className}")
                targetNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                return
            }
        }

        // 노드 탐색 실패 시 좌표 기반 터치 수행
        Log.d(TAG, "No node found at ($x, $y), performing gesture-based touch.")
        val path = Path().apply {
            moveTo(x.toFloat(), y.toFloat())
        }

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 100))
            .build()

        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                Log.d(TAG, "Touch gesture completed at ($x, $y)")
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                Log.d(TAG, "Touch gesture cancelled.")
            }
        }, null)
    }


    /**
     * 특정 좌표에 텍스트 입력
     */
    private fun performInput(text: String, x: Int, y: Int) {
        Log.d(TAG, "Performing input '$text' at ($x, $y)")

        // 현재 화면에서 노드를 검색하여 텍스트 입력 수행
        val rootNode = rootInActiveWindow
        rootNode?.let {
            val targetNode = findNodeAtCoordinates(rootNode, x, y)
            targetNode?.let { node ->
                node.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
                val args = Bundle().apply { putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text) }
                node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
            }
        }
    }


    private fun performDoubleTap(x: Int, y: Int) {
        val path = Path().apply {
            moveTo(x.toFloat(), y.toFloat())
        }

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 100)) // 첫 번째 터치
            .addStroke(GestureDescription.StrokeDescription(path, 200, 100)) // 두 번째 터치 (간격 200ms)
            .build()

        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                Log.d(TAG, "Double tap completed at ($x, $y)")
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                Log.d(TAG, "Double tap cancelled.")
            }
        }, null)
    }


    private fun performLongTouch(x: Int, y: Int, duration: Long) {
        val path = Path().apply {
            moveTo(x.toFloat(), y.toFloat())
        }

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
            .build()

        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                Log.d(TAG, "Long touch completed at ($x, $y) for $duration ms")
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                Log.d(TAG, "Long touch cancelled.")
            }
        }, null)
    }


    private fun performScroll(direction: String, distance: Int) {
        val path = Path().apply {
            when (direction) {
                "down" -> {
                    moveTo(500f, 1000f) // 시작 좌표
                    lineTo(500f, 1000f - distance) // 아래로 스크롤
                }
                "up" -> {
                    moveTo(500f, 500f)
                    lineTo(500f, 500f + distance)
                }
                "left" -> {
                    moveTo(800f, 500f)
                    lineTo(800f - distance, 500f)
                }
                "right" -> {
                    moveTo(200f, 500f)
                    lineTo(200f + distance, 500f)
                }
            }
        }

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 500))
            .build()

        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                Log.d(TAG, "Scroll gesture completed in direction: $direction")
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                Log.d(TAG, "Scroll gesture cancelled.")
            }
        }, null)
    }

    private fun performSwipe(startX: Int, startY: Int, endX: Int, endY: Int, duration: Long) {
        val path = Path().apply {
            moveTo(startX.toFloat(), startY.toFloat())
            lineTo(endX.toFloat(), endY.toFloat())
        }

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
            .build()

        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                Log.d(TAG, "Swipe from ($startX, $startY) to ($endX, $endY) completed.")
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                Log.d(TAG, "Swipe gesture cancelled.")
            }
        }, null)
    }

    private fun performDragAndDrop(startX: Int, startY: Int, endX: Int, endY: Int, duration: Long) {
        val path = Path().apply {
            moveTo(startX.toFloat(), startY.toFloat())
            lineTo(endX.toFloat(), endY.toFloat())
        }

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
            .build()

        dispatchGesture(gesture, null, null)
    }


    /**
     * 특정 좌표에 해당하는 노드를 찾는 함수
     */
    private fun findNodeAtCoordinates(node: AccessibilityNodeInfo, x: Int, y: Int): AccessibilityNodeInfo? {
        // 현재 노드의 화면 좌표를 가져옴
        val bounds = Rect()
        node.getBoundsInScreen(bounds)

        // 좌표가 bounds 안에 포함되어 있는지 확인
        if (bounds.contains(x, y)&& node.isClickable) {
            return node
        }

        // 자식 노드를 순회하여 검색
        for (i in 0 until node.childCount) {
            val childNode = node.getChild(i) ?: continue
            val foundNode = findNodeAtCoordinates(childNode, x, y)
            if (foundNode != null) {
                return foundNode
            }
        }

        // 해당하는 노드가 없는 경우 null 반환
        return null
    }

}
