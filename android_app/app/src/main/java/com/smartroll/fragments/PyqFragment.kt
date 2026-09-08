package com.smartroll.fragments

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.smartroll.R
import com.smartroll.db.PyqEntity
import com.smartroll.repository.MainRepository
import kotlinx.coroutines.launch

class PyqFragment : Fragment() {

    private lateinit var repository: MainRepository
    private var pyqs = listOf<PyqEntity>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pyq, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = MainRepository(requireContext())
        
        view.findViewById<View>(R.id.btnOpenFullDrive).setOnClickListener {
            openDriveDirectly()
        }
        
        loadPyqs(view)
    }

    private fun openDriveDirectly() {
        val driveUrl = "https://drive.google.com/drive/folders/1eTxp4mDjGcuDexieUMYrfqAuKBG3S46n"
        try {
            Toast.makeText(requireContext(), "Opening Google Drive...", Toast.LENGTH_SHORT).show()
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(driveUrl))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            requireActivity().startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.e("PyqFragment", "Redirect error", e)
            Toast.makeText(requireContext(), "Please install Google Drive or a Browser", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadPyqs(view: View) {
        lifecycleScope.launch {
            val user = repository.getCurrentUser()
            pyqs = if (user?.role == "teacher") {
                repository.getPyqs(teacherName = user.name)
            } else {
                repository.getPyqs(branch = user?.branch)
            }
            android.util.Log.d("PyqFragment", "Loaded ${pyqs.size} PYQs.")

            val container = view.findViewById<LinearLayout>(R.id.pyqContainer)
            val emptyView = view.findViewById<TextView>(R.id.tvPyqEmpty)

            container.removeAllViews()

            if (pyqs.isEmpty()) {
                emptyView.visibility = View.VISIBLE
                return@launch
            }

            emptyView.visibility = View.GONE

            pyqs.forEach { pyq ->
                val card = LinearLayout(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(16, 16, 16, 0)
                    }
                    background = requireContext().getDrawable(R.drawable.card_glass)
                    orientation = LinearLayout.VERTICAL
                    setPadding(32, 32, 32, 32)
                    isClickable = true
                    isFocusable = true
                    
                    setOnClickListener {
                        android.util.Log.d("PyqFragment", "CLICKED: ${pyq.title}")
                        Toast.makeText(requireContext(), "Opening Google Drive...", Toast.LENGTH_SHORT).show()
                        openDriveLink(pyq)
                    }
                }

                val titleView = TextView(requireContext()).apply {
                    text = pyq.title
                    setTextColor(requireContext().getColor(R.color.text_primary))
                    textSize = 18f
                    setTypeface(null, Typeface.BOLD)
                }

                val metaView = TextView(requireContext()).apply {
                    text = "${pyq.subject} · ${pyq.branch} · ${pyq.year ?: ""}"
                    setTextColor(requireContext().getColor(R.color.text_muted))
                    textSize = 12f
                    setPadding(0, 4, 0, 8)
                }

                val linkView = TextView(requireContext()).apply {
                    text = "🔗 Open Drive"
                    setTextColor(requireContext().getColor(R.color.primary))
                    textSize = 12f
                    setTypeface(null, Typeface.BOLD)
                    setOnClickListener {
                        openDriveLink(pyq)
                    }
                }

                card.addView(titleView)
                card.addView(metaView)
                card.addView(linkView)

                // ENSURE CLICK REACHES CARD
                titleView.isClickable = false
                titleView.isFocusable = false
                metaView.isClickable = false
                metaView.isFocusable = false
                // Link view stays clickable for redundancy
                linkView.isClickable = true 

                container.addView(card)
                
                // Entrance animation for each card
                card.alpha = 0f
                card.translationY = 100f
                card.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(pyqs.indexOf(pyq) * 100L).start()
            }
        }
    }

    private fun openDriveLink(pyq: PyqEntity) {
        openDriveDirectly()
    }
}