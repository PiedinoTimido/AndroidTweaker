package com.android.tweaker.data

import android.content.Context
import com.android.tweaker.model.SavedLogItem
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class LogcatManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("android_tweaker_logs", Context.MODE_PRIVATE)

    fun isReadLogsGranted(): Boolean {
        return context.checkSelfPermission(android.Manifest.permission.READ_LOGS) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    fun getSavedLogs(): List<SavedLogItem> {
        val jsonStr = prefs.getString("saved_logs_json", "[]") ?: "[]"
        val list = mutableListOf<SavedLogItem>()
        try {
            val jsonArr = JSONArray(jsonStr)
            for (i in 0 until jsonArr.length()) {
                val obj = jsonArr.getJSONObject(i)
                list.add(
                    SavedLogItem(
                        id = obj.getString("id"),
                        fileName = obj.getString("fileName"),
                        timestamp = obj.getLong("timestamp"),
                        logContent = obj.getString("logContent"),
                        isSystemLog = obj.getBoolean("isSystemLog"),
                        targetPackage = obj.optString("targetPackage", ""),
                        isStarred = obj.optBoolean("isStarred", false)
                    )
                )
            }
        } catch (_: Exception) {}
        return list.sortedWith(compareByDescending<SavedLogItem> { it.isStarred }.thenByDescending { it.timestamp })
    }

    fun saveLogItem(item: SavedLogItem) {
        val logs = getSavedLogs().toMutableList()
        logs.removeAll { it.id == item.id }
        logs.add(0, item)
        saveListToPrefs(logs)
    }

    fun deleteLogItem(id: String) {
        val logs = getSavedLogs().toMutableList()
        logs.removeAll { it.id == id }
        saveListToPrefs(logs)
    }

    fun toggleStar(id: String) {
        val logs = getSavedLogs().map {
            if (it.id == id) it.copy(isStarred = !it.isStarred) else it
        }
        saveListToPrefs(logs)
    }

    fun renameLog(id: String, newName: String) {
        val logs = getSavedLogs().map {
            if (it.id == id) it.copy(fileName = newName) else it
        }
        saveListToPrefs(logs)
    }

    private fun saveListToPrefs(logs: List<SavedLogItem>) {
        val jsonArr = JSONArray()
        for (item in logs) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("fileName", item.fileName)
            obj.put("timestamp", item.timestamp)
            obj.put("logContent", item.logContent)
            obj.put("isSystemLog", item.isSystemLog)
            obj.put("targetPackage", item.targetPackage)
            obj.put("isStarred", item.isStarred)
            jsonArr.put(obj)
        }
        prefs.edit().putString("saved_logs_json", jsonArr.toString()).apply()
    }
}
