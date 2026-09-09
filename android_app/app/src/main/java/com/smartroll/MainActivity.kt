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
 * BlinkERP — Main Screen (Role Selection)
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
        tvServerIp.text = "🌐 Server: ${ApiService.serverUrl}\n(Tap to change IP)"
        
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

        findViewById<Button>(R.id.btnAdmin).setOnClickListener {
            // Re-using LoginActivity but with role=admin
            startActivity(Intent(this, LoginActivity::class.java).apply {
                putExtra("role", "admin")
            })
        }

        // Entrance Animations
        val logoContainer = findViewById<android.view.View>(R.id.logoContainer)
        val teacherCard = findViewById<android.view.View>(R.id.teacherCard)
        val studentCard = findViewById<android.view.View>(R.id.studentCard)
        val adminCard = findViewById<android.view.View>(R.id.adminCard)

        logoContainer.translationY = -50f
        teacherCard.translationX = -100f
        studentCard.translationX = 100f
        adminCard.translationX = -100f

        logoContainer.animate().alpha(1f).translationY(0f).setDuration(800).start()
        teacherCard.animate().alpha(1f).translationX(0f).setDuration(800).setStartDelay(300).start()
        studentCard.animate().alpha(1f).translationX(0f).setDuration(800).setStartDelay(500).start()
        adminCard.animate().alpha(1f).translationX(0f).setDuration(800).setStartDelay(700).start()
    }

    private fun showServerDialog() {
        val input = EditText(this)
        input.hint = "192.168.194.186"
        val current = ApiService.serverUrl.replace("http://", "").replace(":5000", "")
        input.setText(current)

        AlertDialog.Builder(this)
            .setTitle("Set Laptop IP")
            .setMessage("Your Laptop WiFi IP is 192.168.194.186\n(Port :5000 will be added automatically)")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val ip = input.text.toString().trim()
                ApiService.updateUrl(this, ip)
                findViewById<TextView>(R.id.tvServerIp).text = "🌐 Server: ${ApiService.serverUrl}\n(Tap to change IP)"
                Toast.makeText(this, "Server updated to ${ApiService.serverUrl}!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
