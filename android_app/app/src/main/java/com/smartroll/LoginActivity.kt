package com.smartroll

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.smartroll.api.ApiService
import com.smartroll.repository.MainRepository
import kotlinx.coroutines.launch

/**
 * BlinkERP — Login Screen
 * Teacher or Student login with name + password
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var repository: MainRepository
    private var role = "teacher"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        ApiService.init(this)
        repository = MainRepository(this)
        role = intent.getStringExtra("role") ?: "teacher"

        val titleText = findViewById<TextView>(R.id.loginTitle)
        titleText.text = when (role) {
            "teacher" -> "👩‍🏫 Teacher Login"
            "admin" -> "⚙️ Admin Login"
            else -> "🧑‍🎓 Student Login"
        }

        val etName = findViewById<EditText>(R.id.etName)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etServerUrl = findViewById<EditText>(R.id.etServerUrl)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvSwitch = findViewById<TextView>(R.id.tvSwitchToRegister)

        etServerUrl.setText(ApiService.serverUrl)
        btnLogin.text = "Login as ${role.replaceFirstChar { it.uppercase() }}"
        
        if (role == "admin") {
            tvSwitch.visibility = android.view.View.GONE
        }

        btnLogin.setOnClickListener {
            val name = etName.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val serverUrl = etServerUrl.text.toString().trim().removeSuffix("/")

            if (name.isEmpty() || password.isEmpty() || serverUrl.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Update API Service URL
            ApiService.updateUrl(this, serverUrl)

            // SECRET ADMIN LOGIN
            if (name == "admin" && password == "9999") {
                startActivity(Intent(this, AdminActivity::class.java))
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val result = repository.login(name, password, role)

                if (result.isSuccess) {
                    startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                    finish()
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Login failed"
                    Toast.makeText(this@LoginActivity, error, Toast.LENGTH_LONG).show()
                }
            }
        }

        tvSwitch.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java).apply {
                putExtra("role", role)
            })
        }
    }
}
