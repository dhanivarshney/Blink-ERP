package com.smartroll

import com.smartroll.utils.PermissionManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.smartroll.api.ApiService
import com.smartroll.ble.BleManager
import com.smartroll.db.UserEntity
import com.smartroll.repository.MainRepository
import kotlinx.coroutines.launch

/**
 * BlinkERP — Student Dashboard
 * Join Class → BLE scan → detect teacher → auto-mark attendance
 * Works offline too (saves locally)
 */
class StudentActivity : AppCompatActivity(), BleManager.DeviceCallback {

    private lateinit var repository: MainRepository
    private lateinit var bleManager: BleManager
    private var currentUser: UserEntity? = null
    private var detectedDevices = mutableSetOf<String>()

    private val safetyHandler = Handler(Looper.getMainLooper())
    private val safetyRunnable = Runnable {
        stopBleOperations()
        findViewById<TextView>(R.id.tvBleStatus).text = "⚠️ Timeout: Teacher not found."
        findViewById<Button>(R.id.btnJoinClass).isEnabled = true
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student)

        ApiService.init(this)
        repository = MainRepository(this)
        bleManager = BleManager(this)

        checkPermissions()

        lifecycleScope.launch {
            currentUser = repository.getCurrentUser()
            updateUi()
        }

        val btnJoin = findViewById<Button>(R.id.btnJoinClass)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val btnSync = findViewById<Button>(R.id.btnSync)
        val btnViewUsers = findViewById<Button>(R.id.btnViewUsers)
        val btnViewRecords = findViewById<Button>(R.id.btnViewRecords)

        btnJoin.setOnClickListener { joinClass() }
        btnSync.setOnClickListener { syncData() }

        btnViewUsers.setOnClickListener {
            lifecycleScope.launch {
                val user = currentUser ?: return@launch
                val students = repository.getRegisteredUsers(user.branch, user.section)

                val names = students.map { it.name }.toTypedArray()
                AlertDialog.Builder(this@StudentActivity)
                    .setTitle("Classmates (${user.branch}-${user.section})")
                    .setItems(names, null)
                    .setPositiveButton("Close", null)
                    .show()
            }
        }

        btnViewRecords.setOnClickListener {
            // startActivity(Intent(this, AttendanceHistoryActivity::class.java))
        }

        val btnNotes = findViewById<Button>(R.id.btnNotes)
        btnNotes.setOnClickListener {
            // startActivity(Intent(this, NotesActivity::class.java))
        }

        btnLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes") { _, _ ->
                    stopBleOperations()
                    repository.logout()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    private fun checkPermissions() {
        if (!PermissionManager.hasPermissions(this, "student")) {
            PermissionManager.requestPermissions(this, PERMISSION_REQUEST_CODE, "student")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.any { it != PackageManager.PERMISSION_GRANTED }) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val scanIndex = permissions.indexOf(android.Manifest.permission.BLUETOOTH_SCAN)
                    if (scanIndex != -1 && grantResults[scanIndex] != PackageManager.PERMISSION_GRANTED) {
                        PermissionManager.showOemScanPermissionMessage(this)
                        return
                    }
                }
                Toast.makeText(this, "Bluetooth/Location permissions are required for joining class", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateUi() {
        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val tvStatus = findViewById<TextView>(R.id.tvBleStatus)
        val tvDetected = findViewById<TextView>(R.id.tvDetectedCount)
        tvWelcome.text = "Hello, ${currentUser?.name ?: "Student"} 👋"
        tvStatus.text = "Ready to join class"
        tvDetected.text = "0 devices detected"
    }

    private fun joinClass() {
        if (!bleManager.isBluetoothEnabled()) {
            Toast.makeText(this, "Please turn on Bluetooth first!", Toast.LENGTH_SHORT).show()
            return
        }

        // Location Services must be ON on Android 11 and below
        if (!PermissionManager.checkAndPromptLocation(this)) {
            return
        }

        detectedDevices.clear()
        findViewById<TextView>(R.id.tvBleStatus).text = "🔍 Searching for teacher..."
        findViewById<Button>(R.id.btnJoinClass).isEnabled = false

        // Start scanning for teacher (no filter)
        bleManager.startScan(this)

        safetyHandler.postDelayed(safetyRunnable, 20000)
    }

    private fun syncData() {
        lifecycleScope.launch {
            val result = repository.syncPendingData()
            if (result.synced > 0) {
                Toast.makeText(this@StudentActivity, "Synced ${result.synced} records!", Toast.LENGTH_SHORT).show()
            } else if (result.failed > 0) {
                Toast.makeText(this@StudentActivity, "Sync failed (server may be offline)", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@StudentActivity, "Already up to date", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Atomic Helper for BLE Reset
    private fun stopBleOperations() {
        bleManager.stopScan(this)
    }

    // ── BLE Callback: device found ───────────────────────────
    override fun onDeviceFound(name: String, address: String, rssi: Int, id: String) {
        if (!id.contains("TEACHER")) return

        // Immediately halt all radio modules to clear buffer
        stopBleOperations()

        if (address in detectedDevices) return
        detectedDevices.add(address)

        val teacherName = name.trim()
        runOnUiThread {
            findViewById<TextView>(R.id.tvDetectedCount).text = "${detectedDevices.size} teacher detected"
            findViewById<TextView>(R.id.tvBleStatus).text = "📡 Found Teacher: $teacherName"
        }

        val user = currentUser ?: return
        val branch = user.branch ?: return
        val section = user.section ?: return
        val studentName = user.name

        // Exact endpoint: POST /api/mark with student_name, branch, section, mode = "Auto"
        lifecycleScope.launch {
            val result = repository.markAttendance(studentName, branch, section, "Auto")

            runOnUiThread {
                findViewById<Button>(R.id.btnJoinClass).isEnabled = true
                if (result.isSuccess) {
                    val record = result.getOrNull()
                    if (record?.syncStatus == "SYNCED") {
                        findViewById<TextView>(R.id.tvBleStatus).text = "✅ Attendance marked online!"
                        Toast.makeText(this@StudentActivity, "Attendance marked!", Toast.LENGTH_SHORT).show()
                    } else {
                        findViewById<TextView>(R.id.tvBleStatus).text = "📱 Saved locally (server offline)"
                        Toast.makeText(this@StudentActivity, "Saved locally!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    findViewById<TextView>(R.id.tvBleStatus).text = "❌ Failed to mark attendance."
                    Toast.makeText(this@StudentActivity, "Failed to mark attendance.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onScanFailed(errorCode: Int) {
        runOnUiThread {
            findViewById<TextView>(R.id.tvBleStatus).text = "⚠️ Scan failed (error code: $errorCode)"
            Toast.makeText(this@StudentActivity, "BLE scan failed with error code $errorCode", Toast.LENGTH_LONG).show()
            findViewById<Button>(R.id.btnJoinClass).isEnabled = true
        }
    }

    override fun onScanStopped() {
        runOnUiThread {
            findViewById<Button>(R.id.btnJoinClass).isEnabled = true
        }
    }

    // 🔥 FIX: Added onDestroy configuration block to kill leaks if student leaves screen early
    override fun onDestroy() {
        stopBleOperations()
        super.onDestroy()
    }
}
