package com.smartroll.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.smartroll.DashboardActivity
import com.smartroll.R
import com.smartroll.db.UserEntity
import com.smartroll.repository.MainRepository
import androidx.viewpager2.widget.ViewPager2
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var repository: MainRepository
    private var currentUser: UserEntity? = null
    private val isLive = MutableStateFlow(false)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = MainRepository(requireContext())

        setupUi(view)
        setupBanners(view)
        loadUserData(view)

        lifecycleScope.launch {
            isLive.collectLatest { _ ->
                updateLiveUi(view)
            }
        }

        // Show feedback when a student's attendance is synced to the server
        lifecycleScope.launch {
            MainRepository.attendanceStatus.collectLatest { status ->
                if (status == "marked") {
                    val statusText = view.findViewById<TextView>(R.id.tvBleStatus)
                    val countText = view.findViewById<TextView>(R.id.tvDetectedCount)
                    val btnMain = view.findViewById<Button>(R.id.btnMainAction)
                    val btnStop = view.findViewById<Button>(R.id.btnStopAction)
                    statusText.text = "✅ Attendance marked!"
                    countText.text = "${MainRepository.detectedDevices.size} device(s) detected"
                    btnMain.isEnabled = true
                    btnMain.text = "JOIN CLASS"
                    btnStop.visibility = View.GONE
                }
            }
        }
    }

    private fun setupUi(view: View) {
        val btnMain = view.findViewById<Button>(R.id.btnMainAction)
        val btnStop = view.findViewById<Button>(R.id.btnStopAction)
        val btnOpenStudents = view.findViewById<View>(R.id.btnOpenStudents)

        btnMain.setOnClickListener {
            (activity as? DashboardActivity)?.startBleAction()
            isLive.value = true
        }

        btnStop.setOnClickListener {
            (activity as? DashboardActivity)?.stopBleAction()
            isLive.value = false
        }

        btnOpenStudents.setOnClickListener {
            androidx.navigation.fragment.NavHostFragment.findNavController(this)
                .navigate(R.id.nav_students)
        }
    }

    private fun updateLiveUi(view: View?) {
        val v = view ?: return
        val statusText = v.findViewById<TextView>(R.id.tvBleStatus)
        val countText = v.findViewById<TextView>(R.id.tvDetectedCount)
        val btnMain = v.findViewById<Button>(R.id.btnMainAction)
        val btnStop = v.findViewById<Button>(R.id.btnStopAction)

        if (isLive.value) {
            val role = currentUser?.role
            if (role == "teacher") {
                statusText.text = "🟢 Class live — Scanning"
                btnMain.visibility = View.GONE
                btnStop.visibility = View.VISIBLE
                countText.text = "${MainRepository.detectedDevices.size} students detected"
            } else {
                statusText.text = "🔍 Searching for teacher..."
                btnMain.isEnabled = false
                btnMain.text = "SEARCHING..."
                btnStop.visibility = View.VISIBLE
                btnStop.text = "STOP SEARCH"
            }
        } else {
            statusText.text = "🔴 Ready to start"
            btnMain.visibility = View.VISIBLE
            btnMain.isEnabled = true
            btnMain.text = if (currentUser?.role == "teacher") "START CLASS" else "JOIN CLASS"
            btnStop.visibility = View.GONE
        }
    }

    private fun setupBanners(view: View) {
        val viewPager = view.findViewById<ViewPager2>(R.id.bannerViewPager)
        val banners = listOf(
            BannerItem("Welcome to SmartRoll", "Smart attendance for modern hackathons.", "🚀", 0xFF667EEA.toInt()),
            BannerItem("Auto-Attendance", "BLE based tracking works offline.", "📡", 0xFF764BA2.toInt()),
            BannerItem("PYQ Folders", "Access all study material instantly.", "📝", 0xFF24243E.toInt())
        )
        viewPager.adapter = BannerAdapter(banners)

        lifecycleScope.launch {
            while (true) {
                delay(3000)
                if (!isAdded) break
                val current = viewPager.currentItem
                val next = if (current == banners.size - 1) 0 else current + 1
                viewPager.setCurrentItem(next, true)
            }
        }
    }

    data class BannerItem(val title: String, val desc: String, val icon: String, val color: Int)

    class BannerAdapter(private val items: List<BannerItem>) : RecyclerView.Adapter<BannerAdapter.VH>() {
        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val title: TextView = v.findViewById(R.id.tvBannerTitle)
            val desc: TextView = v.findViewById(R.id.tvBannerDesc)
            val icon: TextView = v.findViewById(R.id.tvBannerIcon)
            val layout: View = v.findViewById(R.id.bannerLayout)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_banner, parent, false)
            return VH(v)
        }
        override fun onBindViewHolder(h: VH, position: Int) {
            val item = items[position]
            h.title.text = item.title
            h.desc.text = item.desc
            h.icon.text = item.icon
            h.layout.setBackgroundColor(item.color)
        }
        override fun getItemCount() = items.size
    }

    private fun loadUserData(view: View) {
        lifecycleScope.launch {
            currentUser = repository.getCurrentUser()
            val user = currentUser
            
            view.findViewById<TextView>(R.id.tvGreeting).text = "Hello, ${user?.name ?: "User"} 👋"
            
            val sdf = java.text.SimpleDateFormat("EEEE, dd MMM yyyy", java.util.Locale.getDefault())
            val timeSdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            view.findViewById<TextView>(R.id.tvDateTime).text = "${sdf.format(java.util.Date())} • ${timeSdf.format(java.util.Date())}"

            view.findViewById<TextView>(R.id.tvRoleTag).text = user?.role?.uppercase() ?: "USER"
            view.findViewById<TextView>(R.id.tvSubject).text = user?.subject ?: "N/A"
            view.findViewById<TextView>(R.id.tvBranch).text = user?.branch ?: "N/A"
            view.findViewById<TextView>(R.id.tvSection).text = user?.section ?: "N/A"
            
            updateLiveUi(view)
        }
    }
}
