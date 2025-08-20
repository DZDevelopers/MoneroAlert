
package com.example.moneroalert

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var editNodeUrl: EditText
    private lateinit var editNodePort: EditText
    private lateinit var editInterval: EditText
    private lateinit var editThreshold: EditText
    private lateinit var btnSaveStart: Button
    private lateinit var recyclerLogs: RecyclerView
    private lateinit var logsAdapter: LogsAdapter
    private lateinit var settings: SettingsManager
    private lateinit var logManager: LogManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        settings = SettingsManager(applicationContext)
        logManager = LogManager(applicationContext)

        editNodeUrl = findViewById(R.id.editNodeUrl)
        editNodePort = findViewById(R.id.editNodePort)
        editInterval = findViewById(R.id.editInterval)
        editThreshold = findViewById(R.id.editThreshold)
        btnSaveStart = findViewById(R.id.btnSaveStart)
        recyclerLogs = findViewById(R.id.recyclerLogs)

        editNodeUrl.setText(settings.nodeUrl)
        editNodePort.setText(settings.nodePort.toString())
        editInterval.setText(settings.intervalMinutes.toString())
        editThreshold.setText(settings.reorgThreshold.toString())

        logsAdapter = LogsAdapter(logManager.getLogs())
        recyclerLogs.layoutManager = LinearLayoutManager(this)
        recyclerLogs.adapter = logsAdapter

        btnSaveStart.setOnClickListener {
            settings.nodeUrl = editNodeUrl.text.toString().ifBlank { settings.nodeUrl }
            settings.nodePort = editNodePort.text.toString().toIntOrNull() ?: settings.nodePort
            settings.intervalMinutes = editInterval.text.toString().toLongOrNull() ?: settings.intervalMinutes
            settings.reorgThreshold = editThreshold.text.toString().toIntOrNull() ?: settings.reorgThreshold
            scheduleWorker()
        }
    }

    private fun scheduleWorker() {
        val interval = if (settings.intervalMinutes < 15) 15L else settings.intervalMinutes
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<ReorgWorker>(interval, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .addTag("monero_check_work")
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "monero_check_work",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    override fun onResume() {
        super.onResume()
        logsAdapter.update(logManager.getLogs())
    }
}
