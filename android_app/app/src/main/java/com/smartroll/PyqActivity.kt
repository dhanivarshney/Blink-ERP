package com.smartroll

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.smartroll.api.ApiService
import com.smartroll.db.PyqEntity
import com.smartroll.repository.MainRepository
import kotlinx.coroutines.launch

/**
 * BlinkERP — PYQ Papers Activity
 * Shows list of PYQ papers with Google Drive links
 */
class PyqActivity : AppCompatActivity() {

    private lateinit var repository: MainRepository
    private var pyqs = listOf<PyqEntity>()

    companion object {
        const val GOOGLE_DRIVE_BASE = "https://drive.google.com/drive/folders/1eTxp4mDjGcuDexieUMYrfqAuKBG3S46n"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pyq)

        ApiService.init(this)
        repository = MainRepository(this)

        val btnBack = findViewById<Button>(R.id.btnPyqBack)
        val tvTitle = findViewById<TextView>(R.id.tvPyqTitle)

        tvTitle.text = "📝 PYQ Papers"

        btnBack.setOnClickListener {
            finish()
        }

        lifecycleScope.launch {
            loadPyqs()
        }
    }

    private fun loadPyqs() {
        lifecycleScope.launch {
            val user = repository.getCurrentUser()
            val allPyqs = repository.getPyqs(user?.name ?: "")
            pyqs = allPyqs

            val container = findViewById<android.widget.LinearLayout>(R.id.pyqContainer)
            val emptyView = findViewById<TextView>(R.id.tvPyqEmpty)

            container.removeAllViews()

            if (pyqs.isEmpty()) {
                emptyView.visibility = android.view.View.VISIBLE
                return@launch
            }

            emptyView.visibility = android.view.View.GONE

            pyqs.forEach { pyq ->
                val card = android.widget.LinearLayout(this@PyqActivity).apply {
                    layoutParams = android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        marginStart = 16
                        marginEnd = 16
                        topMargin = 12
                    }
                    background = getDrawable(R.drawable.card_glass)
                    orientation = android.widget.LinearLayout.VERTICAL
                    setOnClickListener {
                        openDriveLink(pyq)
                    }
                }

                val iconView = TextView(this@PyqActivity).apply {
                    text = getPyqIcon(pyq.fileName)
                    textSize = 28f
                    setPadding(0, 16, 0, 8)
                }

                val titleView = TextView(this@PyqActivity).apply {
                    text = pyq.title
                    setTextColor(getColor(R.color.text_primary))
                    textSize = 16f
                    setPadding(0, 0, 0, 4)
                    setTypeface(null, android.graphics.Typeface.BOLD)
                }

                val metaView = TextView(this@PyqActivity).apply {
                    text = "${pyq.subject} · ${pyq.branch} · ${pyq.semester ?: ""} ${pyq.year ?: ""}"
                    setTextColor(getColor(R.color.text_muted))
                    textSize = 12f
                    setPadding(0, 0, 0, 8)
                }

                val tagView = TextView(this@PyqActivity).apply {
                    text = pyq.examType ?: "PYQ"
                    setPadding(8, 4, 8, 4)
                    setBackgroundResource(R.drawable.tag_background)
                    setTextColor(getColor(R.color.text_secondary))
                    textSize = 10f
                }

                val linkView = TextView(this@PyqActivity).apply {
                    text = "🔗 Open in Drive"
                    setTextColor(getColor(R.color.accent))
                    textSize = 12f
                    setPadding(0, 8, 0, 12)
                    setTypeface(null, android.graphics.Typeface.BOLD)
                }

                card.addView(iconView)
                card.addView(titleView)
                card.addView(metaView)
                card.addView(tagView)
                card.addView(linkView)

                container.addView(card)
            }
        }
    }

    private fun openDriveLink(pyq: PyqEntity) {
        val driveUrl = pyq.driveLink ?: "$GOOGLE_DRIVE_BASE?searchTitle=${pyq.title.replace(" ", "+")}"
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(driveUrl))
            startActivity(intent)
        } catch (e: Exception) {
            android.widget.Toast.makeText(this, "Cannot open link: $driveUrl", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    private fun getPyqIcon(fileName: String?): String {
        return when {
            fileName?.endsWith(".pdf") == true -> "📕"
            fileName?.endsWith(".doc") == true || fileName?.endsWith(".docx") == true -> "📘"
            fileName?.endsWith(".ppt") == true || fileName?.endsWith(".pptx") == true -> "📊"
            else -> "📝"
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
