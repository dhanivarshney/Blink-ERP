package com.smartroll

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.smartroll.api.ApiService
import com.smartroll.repository.MainRepository
import kotlinx.coroutines.launch

/**
 * SmartRoll — Main Screen (Role Selection)
 * Same as web landing: Teacher or Student
 */
class MainActivity : AppCompatActivity() {

    private lateinit var repository: MainRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ApiService.init(this)
        repository = MainRepository(this)
        
        val tvServerIp = findViewById<TextView>(R.id.tvServerIp)
        tvServerIp.text = "Server: ${ApiService.serverUrl}"
        
        tvServerIp.setOnClickListener { showServerDialog() }

        // Check if already logged in
        lifecycleScope.launch {
            val user = repository.getCurrentUser()
            android.util.Log.d("MainActivity", "Current user: ${user?.name}")
            if (user != null) {
                startActivity(Intent(this@MainActivity, DashboardActivity::class.java))
                finish()
            }
        }

        // Role selection buttons
        findViewById<Button>(R.id.btnTeacher).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java).apply {
                putExtra("role", "teacher")
            })
        }

        findViewById<Button>(R.id.btnStudent).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java).apply {
                putExtra("role", "student")
            })
        }

        // Entrance Animations
        val logoContainer = findViewById<android.view.View>(R.id.logoContainer)
        val teacherCard = findViewById<android.view.View>(R.id.teacherCard)
        val studentCard = findViewById<android.view.View>(R.id.studentCard)

        logoContainer.translationY = -50f
        teacherCard.translationX = -100f
        studentCard.translationX = 100f

        logoContainer.animate().alpha(1f).translationY(0f).setDuration(800).start()
        teacherCard.animate().alpha(1f).translationX(0f).setDuration(800).setStartDelay(300).start()
        studentCard.animate().alpha(1f).translationX(0f).setDuration(800).setStartDelay(500).start()
    }

    private fun showServerDialog() {
        val input = EditText(this)
        input.hint = "e.g. 192.168.1.5"
        val current = ApiService.serverUrl.replace("http://", "").replace(":5000", "")
        input.setText(current)

        AlertDialog.Builder(this)
            .setTitle("Set Laptop IP")
            .setMessage("Use 10.0.2.2 for Emulator.\nEnter Laptop IP for Real Devices.")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val ip = input.text.toString()
                ApiService.updateUrl(this, ip)
                findViewById<TextView>(R.id.tvServerIp).text = "Server: ${ApiService.serverUrl}"
                Toast.makeText(this, "Server updated!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
