
package com.example.moneroalert

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("monero_prefs", Context.MODE_PRIVATE)

    var nodeUrl: String
        get() = prefs.getString("node_url", "http://127.0.0.1") ?: "http://127.0.0.1"
        set(value) = prefs.edit().putString("node_url", value).apply()

    var nodePort: Int
        get() = prefs.getInt("node_port", 18081)
        set(value) = prefs.edit().putInt("node_port", value).apply()

    var intervalMinutes: Long
        get() = prefs.getLong("interval_min", 15L)
        set(value) = prefs.edit().putLong("interval_min", value).apply()

    var reorgThreshold: Int
        get() = prefs.getInt("reorg_threshold", 4)
        set(value) = prefs.edit().putInt("reorg_threshold", value).apply()
}
