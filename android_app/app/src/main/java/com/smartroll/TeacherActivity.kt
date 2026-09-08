package com.smartroll

import com.smartroll.utils.PermissionManager
import com.smartroll.utils.SoundManager
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
import com.smartroll.db.SessionEntity
import com.smartroll.db.UserEntity
import com.smartroll.repository.MainRepository
import kotlinx.coroutines.launch

/**
 * SmartRoll — Teacher Dashboard
 * Start Class → BLE advertise → students detected → End Class
 */
class TeacherActivity : AppCompatActivity(), BleManager.DeviceCallback {

    private lateinit var repository: MainRepository
    private lateinit var bleManager: BleManager
    private var currentUser: UserEntity? = null
    private var currentSession: SessionEntity? = null
    private var detectedStudents = mutableSetOf<String>() // Addresses
    private var detectedStudentNames = mutableListOf<String>() // Names

    private val scanHandler = Handler(Looper.getMainLooper())
    private var isScanningActive = false

    // 🔥 Teacher only advertises, no scanning needed
    private val scanRunnable = Runnable { }


    companion object {
        private const val PERMISSION_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher)

        ApiService.init(this)
        repository = MainRepository(this)
        bleManager = BleManager(this)

        checkPermissions()

        lifecycleScope.launch {
            currentUser = repository.getCurrentUser()
            updateUi()
        }

        val btnStartClass = findViewById<Button>(R.id.btnStartClass)
        val btnEndClass = findViewById<Button>(R.id.btnEndClass)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val btnSync = findViewById<Button>(R.id.btnSync)
        val btnViewUsers = findViewById<Button>(R.id.btnViewUsers)
        val btnViewRecords = findViewById<Button>(R.id.btnViewRecords)

        btnStartClass.setOnClickListener {
            SoundManager.playClick(it)
            startClass()
        }
        btnEndClass.setOnClickListener {
            SoundManager.playClick(it)
            endClass()
        }
        btnEndClass.isEnabled = false
        btnEndClass.alpha = 0.5f

        btnSync.setOnClickListener { syncData() }

        btnViewUsers.setOnClickListener {
            // showRegisteredStudents() Implementation
        }

        btnViewRecords.setOnClickListener {
            // startActivity(Intent(this, AttendanceHistoryActivity::class.java))
        }

        val btnNotes = findViewById<Button>(R.id.btnNotes)
        btnNotes.setOnClickListener {
            // startActivity(Intent(this, NotesActivity::class.java))
        }

        val btnPyq = findViewById<Button>(R.id.btnPyq)
        btnPyq.setOnClickListener {
            // startActivity(Intent(this, PyqActivity::class.java))
        }

        btnLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes") { _, _ ->
                    cleanupBleSystem()
                    repository.logout()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    private fun checkPermissions() {
        if (!PermissionManager.hasPermissions(this)) {
            PermissionManager.requestPermissions(this, PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.any { it != PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Bluetooth permissions are required for attendance", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateUi() {
        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val tvStatus = findViewById<TextView>(R.id.tvBleStatus)
        tvWelcome.text = "Hello, ${currentUser?.name ?: "Teacher"} 👋"
        tvStatus.text = "Ready to take BLE attendance"
    }

    private fun startClass() {
        if (!bleManager.isBluetoothEnabled()) {
            Toast.makeText(this, "Please turn on Bluetooth first!", Toast.LENGTH_SHORT).show()
            return
        }

        val user = currentUser ?: return
        val branch = user.branch ?: ""
        val section = user.section ?: ""
        val subject = user.subject ?: ""
        val teacherName = user.name

        if (branch.isEmpty() || section.isEmpty() || subject.isEmpty()) {
            Toast.makeText(this, "Branch/Section/Subject missing — please re-register", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            currentSession = repository.startSession(teacherName, branch, section, subject)

            findViewById<Button>(R.id.btnStartClass).isEnabled = false
            findViewById<Button>(R.id.btnStartClass).alpha = 0.5f
            findViewById<Button>(R.id.btnEndClass).isEnabled = true
            findViewById<Button>(R.id.btnEndClass).alpha = 1f
            findViewById<TextView>(R.id.tvBleStatus).text = "🟢 Class live — Scanning for students"

            detectedStudents.clear()
            findViewById<TextView>(R.id.tvDetectedCount).text = "0 students detected"

            // Start BLE advertising (Broadcast as Teacher)
            bleManager.startAdvertising(
                userId = "TEACHER:$teacherName",
                onSuccess = {
                    runOnUiThread {
                        findViewById<TextView>(R.id.tvBleStatus).text = "🟢 Broadcasting signal..."
                    }
                },
                onError = { error ->
                    runOnUiThread {
                        findViewById<TextView>(R.id.tvBleStatus).text = "⚠️ BLE error: $error"
                        Toast.makeText(this@TeacherActivity, "BLE error: $error", Toast.LENGTH_LONG).show()
                    }
                }
            )

            val msg = if (currentSession?.remoteId != null) "Class started! ID: ${currentSession?.remoteId}" else "Offline Session started!"
            Toast.makeText(this@TeacherActivity, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun endClass() {
        val session = currentSession ?: return

        cleanupBleSystem()

        lifecycleScope.launch {
            repository.endSession(session)
            currentSession = null
            findViewById<Button>(R.id.btnStartClass).isEnabled = true
            findViewById<Button>(R.id.btnStartClass).alpha = 1f
            findViewById<Button>(R.id.btnEndClass).isEnabled = false
            findViewById<Button>(R.id.btnEndClass).alpha = 0.5f
            findViewById<TextView>(R.id.tvBleStatus).text = "Class ended. Ready for next."

            Toast.makeText(this@TeacherActivity, "Class ended!", Toast.LENGTH_SHORT).show()
            syncData()
        }
    }

    // 🔥 Helper for atomic resource cleanup
    private fun cleanupBleSystem() {
        bleManager.stopAdvertising()
    }

    private fun syncData() {
        // Sync implementation
    }

    override fun onDeviceFound(name: String, address: String, rssi: Int, id: String) {
        val studentName = name.trim()
        if (address in detectedStudents) return

        detectedStudents.add(address)
        detectedStudentNames.add(studentName)

        runOnUiThread {
            findViewById<TextView>(R.id.tvDetectedCount).text = "${detectedStudents.size} students present"
        }
    }

    override fun onScanStopped() {
        // Callback handle configuration
    }

    // 🔥 FIX: Added onDestroy lifecycle hook to completely free the hardware resources
    override fun onDestroy() {
        cleanupBleSystem()
        super.onDestroy()
    }
}
