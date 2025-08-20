
package com.example.moneroalert

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class ReorgWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val settings = SettingsManager(appContext)
    private val logManager = LogManager(appContext)
    private val notif = NotificationHelper(appContext)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val base = settings.nodeUrl.trimEnd('/') + ":" + settings.nodePort
            val client = MoneroRpcClient(base)
            val infoResp = client.getInfo() ?: return@withContext Result.retry()
            val result = infoResp.optJSONObject("result") ?: JSONObject()
            val height = result.optLong("height", -1)
            if (height < 0) return@withContext Result.retry()

            val threshold = settings.reorgThreshold
            // gather current top `threshold+1` hashes
            val current = mutableMapOf<Long, String>()
            for (i in 0 until (threshold + 1)) {
                val h = height - i
                if (h < 0) break
                val headerResp = client.getBlockHeaderByHeight(h) ?: continue
                val r = headerResp.optJSONObject("result")?.optJSONObject("block_header") ?: continue
                val hash = r.optString("hash", "")
                if (hash.isNotEmpty()) current[h] = hash
            }

            val prefs = applicationContext.getSharedPreferences("monero_state", Context.MODE_PRIVATE)
            val savedJson = prefs.getString("last_hashes", null)
            if (savedJson == null) {
                // first run: save and return
                prefs.edit().putString("last_hashes", JSONObject(current as Map<*, *>).toString()).apply()
                return@withContext Result.success()
            }

            val saved = JSONObject(savedJson)
            // count consecutive mismatches from top
            var reorgDepth = 0
            var h = height
            while (reorgDepth <= threshold) {
                val curHash = current[h] ?: break
                if (!saved.has(h.toString())) break
                val oldHash = saved.optString(h.toString(), "")
                if (oldHash.isEmpty()) break
                if (oldHash != curHash) {
                    reorgDepth++
                    h--
                    if (h < 0) break
                } else {
                    break
                }
            }

            if (reorgDepth >= threshold) {
                val msg = "Warning: Possible Monero network attack detected. Consider adding hashrate. (reorg depth: $reorgDepth at height $height)"
                notif.sendAlert(msg)
                logManager.addLog(msg)
            }

            // update saved hashes with current window
            prefs.edit().putString("last_hashes", JSONObject(current as Map<*, *>).toString()).apply()
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
