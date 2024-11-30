package com.example.ai_macrofy

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import org.json.JSONArray
import org.json.JSONObject

class LayoutAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "LayoutAccessibilityService"
        var instance: LayoutAccessibilityService?  = null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this // 서비스 인스턴스 저장
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // 이벤트 처리 없음
    }

    override fun onInterrupt() {
        Log.w(TAG, "Service interrupted")
    }

    /**
     * 현재 화면의 레이아웃 정보를 JSON으로 반환
     */
    fun extractLayoutInfo(): JSONObject? {
        val rootNode = rootInActiveWindow
        if (rootNode == null) {
            Log.e(TAG, "Root node is null. Cannot extract layout info.")
            return null
        }
        return parseNodeToJson(rootNode)
    }

    /**
     * 화면의 레이아웃 정보를 재귀적으로 JSON 변환
     */
    private fun parseNodeToJson(node: AccessibilityNodeInfo?): JSONObject {
        if (node == null) return JSONObject()

        val jsonObject = JSONObject()
        val childrenArray = JSONArray()

        val bounds = Rect()
        node.getBoundsInScreen(bounds)

        jsonObject.put("className", node.className)
        jsonObject.put("text", node.text ?: "")
        //jsonObject.put("contentDescription", node.contentDescription ?: "")
        var x = (bounds.right + bounds.left) / 2
        var y = (bounds.bottom + bounds.top) / 2
        jsonObject.put(
            "coordination",
            JSONObject().apply {
                put("x", x)
                put("y", y)
            }
        )
        jsonObject.put("clickable", node.isClickable)
        //jsonObject.put("id", node.viewIdResourceName ?: "")

        for (i in 0 until node.childCount) {
            val childNode = node.getChild(i) ?: continue
            childrenArray.put(parseNodeToJson(childNode))
        }

        jsonObject.put("children", childrenArray)
        return jsonObject
    }
}
