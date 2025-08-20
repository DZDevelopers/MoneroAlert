
package com.example.moneroalert

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

class LogManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("monero_logs", Context.MODE_PRIVATE)
    private val key = "logs"
    private val max = 200

    fun addLog(text: String) {
        val arr = JSONArray(prefs.getString(key, "[]"))
        val ts = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val entry = "$ts — $text"
        arr.put(entry)
        // keep only last `max`
        val out = JSONArray()
        val start = if (arr.length() > max) arr.length() - max else 0
        for (i in start until arr.length()) {
            out.put(arr.getString(i))
        }
        prefs.edit().putString(key, out.toString()).apply()
    }

    fun getLogs(): List<String> {
        val arr = JSONArray(prefs.getString(key, "[]"))
        val list = mutableListOf<String>()
        for (i in 0 until arr.length()) list.add(arr.getString(i))
        return list.reversed() // show newest first
    }
}
