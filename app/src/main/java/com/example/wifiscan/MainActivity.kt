package com.example.wifiscan

import android.Manifest.permission.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.EXTRA_RESULTS_UPDATED
import android.net.wifi.WifiManager.SCAN_RESULTS_AVAILABLE_ACTION
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.SECONDS

/**
 * https://developer.android.com/guide/topics/connectivity/wifi-scan
 *
 * Android 10 and higher:
 * The same throttling limits from Android 9 apply.
 * There is a new developer option to toggle the throttling off for local testing
 * (under Developer Options > Networking > Wi-Fi scan throttling).
 */
class MainActivity : AppCompatActivity() {

    private val tag = this::class.simpleName
    private val permissions = arrayOf(ACCESS_FINE_LOCATION, CHANGE_WIFI_STATE, ACCESS_WIFI_STATE)
    private val executor = Executors.newSingleThreadScheduledExecutor()
    private lateinit var wifiManager: WifiManager

    private lateinit var writer: BufferedWriter
    private lateinit var reader: BufferedReader
    private var entryList = mutableListOf<Entry>()
    private var isResult = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermissions(permissions, 100)

        wifiManager = getSystemService(WIFI_SERVICE) as WifiManager

        val wifiScanReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                return when (intent.getBooleanExtra(EXTRA_RESULTS_UPDATED, false)) {
                    true -> when (isResult) {
                        true -> resultscansuccess()
                        false -> scanSuccess()
                    }
                    false -> scanFailure()
                }
            }
        }
        val intentFilter = IntentFilter().apply { addAction(SCAN_RESULTS_AVAILABLE_ACTION) }
        registerReceiver(wifiScanReceiver, intentFilter)

        writer = BufferedWriter(OutputStreamWriter(openFileOutput("data.csv", MODE_PRIVATE)))
        writer.write("location,timestamp,ssid,level\n")
        writer.flush()
        writer.close()
    }

    private fun resultscansuccess() {
        val entry = Entry(wifiManager.scanResults.map { Pair(it.SSID, it.level) })
        val result = NN.nearestNeighbour(entryList, entry)
        Toast.makeText(this, result, LENGTH_LONG).show()
    }


    @Suppress("DEPRECATION", "UNUSED_PARAMETER")
    fun scan(view: View) {
        isResult = false
        (0L..60L step 1L).forEach {
            executor.schedule({
                if (!wifiManager.startScan())
                    scanFailure()
            }, it, SECONDS)
        }
        Toast.makeText(this, "Scan started", LENGTH_SHORT).show()
        scanButton.isEnabled = false
        executor.schedule({
            runOnUiThread {
                Toast.makeText(this, "Scan Finished", LENGTH_LONG).show()
                scanButton.isEnabled = true
//                reader = BufferedReader(InputStreamReader(openFileInput("data.csv")))
//                var myExternalFile:File = File(getExternalFilesDir(null),"result.csv")
//                try {
//                    val fileOutPutStream = FileOutputStream(myExternalFile)
//                    for (string in reader.readLines())  fileOutPutStream.write(string.toByteArray()+"\n".toByteArray())
//                    fileOutPutStream.flush()
//                    fileOutPutStream.close()
//                    reader.close()
//                } catch (e: IOException) {
//                    e.printStackTrace()
//                }
            }
        }, 60, SECONDS)
    }

    private fun scanSuccess() {
        wifiManager.scanResults.forEach {
            if (it.SSID == "") return
            writer = BufferedWriter(OutputStreamWriter(openFileOutput("data.csv", MODE_APPEND)))
            writer.append("${locationText.text},${it.timestamp},${it.SSID},${it.level}\n")
            writer.flush()
            writer.close()
        }
        entryList.add(
            Entry(
                location = locationText.text.toString(),
                strengths = wifiManager.scanResults.map { Pair(it.SSID, it.level) }
            )
        )
    }

    private fun scanFailure() {
        Log.i(tag, "Scan Failure")
        Toast.makeText(this, "Scan Failure", LENGTH_SHORT).show()
    }

    @Suppress("DEPRECATION", "UNUSED_PARAMETER")
    fun doNearestNeighbour(view: View) {
        isResult = true
        if (!wifiManager.startScan())
            scanFailure()
    }
}
