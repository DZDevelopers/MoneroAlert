
package com.example.moneroalert

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class MoneroRpcClient(private val baseUrl: String) {
    private val client = OkHttpClient.Builder()
        .callTimeout(15, TimeUnit.SECONDS)
        .build()

    private val JSON = "application/json".toMediaType()

    private fun post(json: JSONObject): JSONObject? {
        val body = json.toString().toRequestBody(JSON)
        val req = Request.Builder().url(baseUrl).post(body).build()
        client.newCall(req).execute().use { resp ->
            val s = resp.body?.string() ?: return null
            return JSONObject(s)
        }
    }

    fun getInfo(): JSONObject? {
        val req = JSONObject()
        req.put("jsonrpc", "2.0")
        req.put("id", "0")
        req.put("method", "get_info")
        return try { post(req) } catch (e: IOException) { null }
    }

    fun getBlockHeaderByHeight(height: Long): JSONObject? {
        val req = JSONObject()
        req.put("jsonrpc", "2.0")
        req.put("id", "0")
        req.put("method", "get_block_header_by_height")
        val params = JSONObject()
        params.put("height", height)
        req.put("params", params)
        return try { post(req) } catch (e: IOException) { null }
    }
}
