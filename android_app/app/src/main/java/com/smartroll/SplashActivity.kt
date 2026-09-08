package com.smartroll

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.smartroll.repository.MainRepository
import com.smartroll.utils.PermissionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if (PermissionManager.hasPermissions(this)) {
            proceed()
        } else {
            PermissionManager.requestPermissions(this, 100)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        proceed()
    }

    private fun proceed() {
        val ivLogo = findViewById<ImageView>(R.id.ivLogo)
        val viewOutline = findViewById<View>(R.id.viewOutline)
        val tvAppName = findViewById<TextView>(R.id.tvAppName)
        val tvTagline = findViewById<TextView>(R.id.tvTagline)

        // Reset state for "Wow" effect
        ivLogo.alpha = 0f
        ivLogo.scaleX = 0f
        ivLogo.scaleY = 0f
        viewOutline.alpha = 0f
        viewOutline.scaleX = 0f
        viewOutline.scaleY = 0f
        ivLogo.rotation = -180f
        tvAppName.alpha = 0f
        tvTagline.alpha = 0f

        // Wow Entrance: Rotation + Scale + Fade
        ivLogo.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .rotation(360f) // Full rotation
            .setDuration(2000)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()

        viewOutline.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .rotation(-360f) // Slow counter rotation
            .setDuration(2500)
            .setStartDelay(200)
            .setInterpolator(android.view.animation.LinearInterpolator())
            .start()
        
        // Staggered text fade in
        tvAppName.animate()
            .alpha(1f)
            .setDuration(1000)
            .setStartDelay(800)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        tvTagline.animate()
            .alpha(1f)
            .setDuration(1000)
            .setStartDelay(1200)
            .start()

        lifecycleScope.launch {
            val repository = MainRepository(this@SplashActivity)
            val user = repository.getCurrentUser()
            delay(3000) 
            
            if (user != null) {
                startActivity(Intent(this@SplashActivity, DashboardActivity::class.java))
            } else {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            }
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }
}
