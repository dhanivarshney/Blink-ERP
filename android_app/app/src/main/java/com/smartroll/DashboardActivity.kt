package com.smartroll

import com.smartroll.utils.PermissionManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.smartroll.ble.BleManager
import com.smartroll.db.SessionEntity
import com.smartroll.db.UserEntity
import com.smartroll.repository.MainRepository
import kotlinx.coroutines.launch

/**
 * SmartRoll — Main Dashboard (role-based home)
 *
 * This is the screen the hackathon demo runs on:
 *  - TEACHER: "START CLASS" creates the session on the server FIRST (so the
 *    laptop dashboard sees it live), then BLE-advertises as teacher + scans
 *    for student phones.
 *  - STUDENT: "JOIN CLASS" BLE-scans for the teacher; when the teacher's
 *    beacon is detected, attendance is auto-marked on the server and shows
 *    up on the laptop dashboard within seconds.
 */
class DashboardActivity : AppCompatActivity(), BleManager.DeviceCallback {

    private lateinit var bleManager: BleManager
    private lateinit var repository: MainRepository
    private var currentUser: UserEntity? = null
    private var currentSession: SessionEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        repository = MainRepository(this)
        bleManager = BleManager(this)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        
        bottomNav.setupWithNavController(navController)
        
        lifecycleScope.launch {
            currentUser = repository.getCurrentUser()
        }

        checkPermissions()
    }

    private fun checkPermissions() {
        if (!PermissionManager.hasPermissions(this)) {
            PermissionManager.requestPermissions(this, 103)
        }
    }

    fun startBleAction() {
        val user = currentUser ?: return
        if (!bleManager.isBluetoothEnabled()) {
            Toast.makeText(this, "Bluetooth is OFF", Toast.LENGTH_SHORT).show()
            return
        }

        MainRepository.attendanceStatus.value = null
        MainRepository.isScanningOrAdvertising = true
        MainRepository.detectedDevices.clear()

        if (user.role == "teacher") {
            val branch = user.branch ?: ""
            val section = user.section ?: ""
            val subject = user.subject ?: ""
            if (branch.isEmpty() || section.isEmpty() || subject.isEmpty()) {
                Toast.makeText(this, "Branch/Section/Subject missing — re-register", Toast.LENGTH_LONG).show()
                return
            }

            // Create the session on the server FIRST so the laptop dashboard
            // picks it up as LIVE, then broadcast our teacher beacon.
            lifecycleScope.launch {
                currentSession = repository.startSession(user.name, branch, section, subject)
                val synced = currentSession?.remoteId != null
                runOnUiThread {
                    Toast.makeText(
                        this@DashboardActivity,
                        if (synced) "Class started! LIVE on laptop dashboard 📡"
                        else "Class started (server offline — will sync)",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            bleManager.startAdvertising(user.id.toString(), {
                Log.d("Dashboard", "Teacher advertising started")
            }, { err ->
                Toast.makeText(this, "BLE Error: $err", Toast.LENGTH_SHORT).show()
            })
            bleManager.startScan(this)
        } else {
            // Student: advertise our presence AND scan for the teacher's beacon.
            bleManager.startAdvertising(user.id.toString(), {
                Log.d("Dashboard", "Student advertising started")
            }, { err ->
                Log.e("Dashboard", "Student advertising error: $err")
            })
            bleManager.startScan(this)
        }
    }

    fun stopBleAction() {
        bleManager.stopAdvertising()
        bleManager.stopScan(this)
        MainRepository.isScanningOrAdvertising = false

        // End the server session if the teacher had one running.
        val session = currentSession
        currentSession = null
        if (session != null) {
            lifecycleScope.launch {
                repository.endSession(session)
            }
        }
    }

    private var lastAttendanceMarkedTime: Long = 0

    override fun onDeviceFound(name: String, address: String, rssi: Int, id: String) {
        val user = currentUser ?: return
        if (user.role == "teacher") {
            // Check if ID belongs to a student
            if (id.startsWith("stu_") && address !in MainRepository.detectedDevices) {
                MainRepository.detectedDevices.add(address)
            }
        } else if (user.role == "student") {
            val now = System.currentTimeMillis()
            // Check if ID belongs to a teacher
            if (id.startsWith("tea_") && address !in MainRepository.detectedDevices && (now - lastAttendanceMarkedTime > 5000)) {
                MainRepository.detectedDevices.add(address)
                lastAttendanceMarkedTime = now
                val branch = user.branch ?: return
                val section = user.section ?: return
                val studentName = user.name
                lifecycleScope.launch {
                    val session = repository.getActiveSession(branch, section)
                    val sessionId = session?.remoteId?.toString()
                    val result = repository.markAttendance(studentName, branch, section, "Auto", sessionId)
                    runOnUiThread {
                        val record = result.getOrNull()
                        if (record?.syncStatus == "SYNCED") {
                            MainRepository.attendanceStatus.value = "marked"
                            Toast.makeText(
                                this@DashboardActivity,
                                "✅ Attendance marked! See laptop dashboard.",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                this@DashboardActivity,
                                "Attendance saved locally (server offline)",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    override fun onScanStopped() {
        Log.d("Dashboard", "Scan stopped")
    }

    override fun onDestroy() {
        super.onDestroy()
        bleManager.stopAdvertising()
        bleManager.stopScan(this)
    }
}
