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
    private var hasProceeded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Always start the opening Blink animation immediately
        startBlinkAnimation()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Once user finishes interacting with permission dialog, proceed
        proceedToNext()
    }

    private fun proceedToNext() {
        if (hasProceeded) return
        hasProceeded = true

        lifecycleScope.launch {
            val repository = MainRepository(this@SplashActivity)
            val user = repository.getCurrentUser()

            if (user != null) {
                startActivity(Intent(this@SplashActivity, DashboardActivity::class.java))
            } else {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            }
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }

    private fun startBlinkAnimation() {
        val ivLogo = findViewById<ImageView>(R.id.ivLogo)
        val viewOutline = findViewById<View>(R.id.viewOutline)
        val viewGlow = findViewById<View>(R.id.viewGlow)
        val tvAppName = findViewById<TextView>(R.id.tvAppName)
        val tvTagline = findViewById<TextView>(R.id.tvTagline)

        // Initial setup
        ivLogo.alpha = 1f
        ivLogo.scaleX = 1f
        ivLogo.scaleY = 0.05f // Eyelid fully shut!
        viewOutline.alpha = 0f
        viewOutline.scaleX = 0.8f
        viewOutline.scaleY = 0.8f
        viewGlow.alpha = 0f
        tvAppName.alpha = 0f
        tvAppName.translationY = 30f
        tvTagline.alpha = 0f
        tvTagline.translationY = 20f

        // 👁️ BLINK SEQUENCE: Realistic rapid eye blinks
        // Blink 1: Open slightly then shut
        ivLogo.animate()
            .scaleY(1f)
            .setDuration(220)
            .setStartDelay(200)
            .withEndAction {
                // Shut quick
                ivLogo.animate()
                    .scaleY(0.1f)
                    .setDuration(120)
                    .withEndAction {
                        // Blink 2: Open wide and stay open!
                        ivLogo.animate()
                            .scaleY(1.15f)
                            .scaleX(1.15f)
                            .setDuration(300)
                            .setInterpolator(android.view.animation.OvershootInterpolator(2.0f))
                            .withEndAction {
                                // Settle to 1.0f
                                ivLogo.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(200)
                                    .start()

                                // Glowing Flash Wave
                                viewGlow.animate()
                                    .alpha(0.9f)
                                    .scaleX(1.4f)
                                    .scaleY(1.4f)
                                    .setDuration(600)
                                    .withEndAction {
                                        viewGlow.animate().alpha(0f).setDuration(400).start()
                                    }
                                    .start()

                                // Outer Radar pulse expanding
                                viewOutline.animate()
                                    .alpha(0.6f)
                                    .scaleX(1.25f)
                                    .scaleY(1.25f)
                                    .setDuration(700)
                                    .start()
                            }
                            .start()
                    }
                    .start()
            }
            .start()

        // ⚡ Text Reveals right after eye settles
        tvAppName.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setStartDelay(950)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()

        tvTagline.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setStartDelay(1150)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()

        // After blink animation finishes (~2.2s), check permissions
        lifecycleScope.launch {
            delay(2300)
            if (!PermissionManager.hasPermissions(this@SplashActivity)) {
                // Ask permissions and WAIT for user to respond (no premature finish/flicker)
                PermissionManager.requestPermissions(this@SplashActivity, 100)
            } else {
                // Permissions already granted, proceed immediately
                proceedToNext()
            }
        }
    }
}
