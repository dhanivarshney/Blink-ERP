package com.smartroll.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.smartroll.R
import com.smartroll.AttendanceHistoryActivity
import com.smartroll.repository.MainRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class AnalyticsFragment : Fragment() {

    private lateinit var repository: MainRepository

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_analytics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = MainRepository(requireContext())
        
        view.findViewById<RecyclerView>(R.id.rvAnalytics).layoutManager = LinearLayoutManager(context)
        view.findViewById<RecyclerView>(R.id.rvSubjects).layoutManager = LinearLayoutManager(context)
        
        view.findViewById<Button>(R.id.btnExport).setOnClickListener { exportData() }
        loadData(view)
    }

    private fun loadData(view: View) {
        lifecycleScope.launch {
            val user = repository.getCurrentUser()
            val tvTotalRecords = view.findViewById<TextView>(R.id.tvTotalRecords)
            val pieChart = view.findViewById<PieChart>(R.id.pieChart)
            val rvAnalytics = view.findViewById<RecyclerView>(R.id.rvAnalytics)

            val bunkHeroCard = view.findViewById<CardView>(R.id.bunkHeroCard)
            val tvBunkPct = view.findViewById<TextView>(R.id.tvBunkPct)
            val tvBunkRatio = view.findViewById<TextView>(R.id.tvBunkRatio)
            val tvStatusEmoji = view.findViewById<TextView>(R.id.tvStatusEmoji)
            val tvBunkStatus = view.findViewById<TextView>(R.id.tvBunkStatus)
            val bunkProgressBar = view.findViewById<View>(R.id.bunkProgressBar)
            val tvBunkMessage = view.findViewById<TextView>(R.id.tvBunkMessage)
            val subjectBreakdownCard = view.findViewById<CardView>(R.id.subjectBreakdownCard)
            val rvSubjects = view.findViewById<RecyclerView>(R.id.rvSubjects)

            if (user != null && user.role == "student") {
                bunkHeroCard.visibility = View.VISIBLE
                subjectBreakdownCard.visibility = View.VISIBLE
                view.findViewById<TextView>(R.id.tvAnalyticsSubtitle).text = "Student specific overview"

                val remoteData = withContext(Dispatchers.IO) {
                    try {
                        com.smartroll.api.ApiService.getStudentAnalytics(user.name, user.branch, user.section)
                    } catch (e: Exception) { null }
                }

                if (remoteData != null) {
                    val pct = remoteData.optDouble("attendance_pct", 0.0)
                    val presentCount = remoteData.optInt("present_count", 0)
                    val totalSessions = remoteData.optInt("total_sessions", 0)
                    val absentCount = totalSessions - presentCount
                    val status = remoteData.optString("eligibility_status", "Safe")
                    val bunkMsg = remoteData.optString("bunk_message", "")

                    val isSafe = status.equals("Safe", ignoreCase = true)
                    
                    tvBunkPct.text = String.format("%.1f%%", pct)
                    tvBunkRatio.text = "$presentCount / $totalSessions classes attended"
                    tvBunkMessage.text = "🎯 $bunkMsg"
                    
                    if (isSafe) {
                        tvStatusEmoji.text = "✅"
                        tvBunkStatus.text = "SAFE"
                        tvBunkStatus.setTextColor(Color.parseColor("#00C853"))
                        bunkProgressBar.setBackgroundColor(Color.parseColor("#00C853")) // Green
                    } else {
                        tvStatusEmoji.text = "⚠️"
                        tvBunkStatus.text = "SHORT"
                        tvBunkStatus.setTextColor(Color.parseColor("#FF5252"))
                        bunkProgressBar.setBackgroundColor(Color.parseColor("#FF5252")) // Red
                    }

                    // Animate progress bar width based on percentage (using layout params weight if in LinearLayout, but here it's FrameLayout. Let's just set width programmatically using post)
                    bunkProgressBar.post {
                        val parentWidth = (bunkProgressBar.parent as View).width
                        val newWidth = (parentWidth * (pct / 100.0)).toInt()
                        val params = bunkProgressBar.layoutParams
                        params.width = newWidth
                        bunkProgressBar.layoutParams = params
                    }

                    // Subjects
                    val subjectsArr = remoteData.optJSONArray("subject_breakdown")
                    val subList = mutableListOf<JSONObject>()
                    if (subjectsArr != null) {
                        for (i in 0 until subjectsArr.length()) {
                            subList.add(subjectsArr.getJSONObject(i))
                        }
                    }
                    rvSubjects.adapter = SubjectAdapter(subList)

                    // Pie Chart
                    tvTotalRecords.text = "$presentCount Present | $absentCount Absent"
                    val entries = listOf(
                        PieEntry(presentCount.toFloat(), "Present"),
                        PieEntry(absentCount.toFloat(), "Absent")
                    )
                    val dataSet = PieDataSet(entries, "Attendance Overview").apply {
                        colors = listOf(Color.parseColor("#00C853"), Color.parseColor("#FF5252"))
                        valueTextColor = Color.WHITE
                        valueTextSize = 13f
                    }
                    pieChart.data = PieData(dataSet)
                    pieChart.centerText = String.format("%.1f%%", pct)
                    pieChart.setCenterTextSize(18f)
                    pieChart.setCenterTextColor(if (isSafe) Color.parseColor("#00C853") else Color.parseColor("#FF5252"))
                    pieChart.description.isEnabled = false
                    pieChart.legend.textColor = Color.WHITE
                    pieChart.invalidate()

                    // Parse history
                    val historyArr = remoteData.optJSONArray("history")
                    val historyList = mutableListOf<com.smartroll.db.AttendanceRecordEntity>()
                    if (historyArr != null) {
                        for (i in 0 until historyArr.length()) {
                            val h = historyArr.getJSONObject(i)
                            val isPresent = h.optString("status") == "Present"
                            if (isPresent) {
                                historyList.add(com.smartroll.db.AttendanceRecordEntity(
                                    sessionId = h.optString("session_id", ""),
                                    studentName = "${h.optString("subject", "Class")} (${h.optString("date", "")})",
                                    branch = h.optString("branch", ""),
                                    section = h.optString("section", ""),
                                    mode = h.optString("mode", "Auto"),
                                    timestamp = System.currentTimeMillis()
                                ))
                            }
                        }
                    }
                    rvAnalytics.adapter = AttendanceHistoryActivity.HistoryAdapter(historyList)
                    return@launch
                }
            } else {
                bunkHeroCard.visibility = View.GONE
                subjectBreakdownCard.visibility = View.GONE
            }

            // ── Teacher / Fallback Class Analytics ──
            val records = repository.getAttendanceHistory()
            val allStudents = repository.getRegisteredUsers()
            
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -7)
            val weekAgo = calendar.timeInMillis
            val recentRecords = records.filter { it.timestamp >= weekAgo }

            val presentNames = recentRecords.map { it.studentName }.distinct()
            val absentStudents = allStudents.filter { it.name !in presentNames }

            tvTotalRecords.text = "Present: ${presentNames.size} | Absent: ${absentStudents.size}"
            
            val entries = listOf(
                PieEntry(presentNames.size.toFloat(), "Present"),
                PieEntry(absentStudents.size.toFloat(), "Absent")
            )
            val dataSet = PieDataSet(entries, "Weekly Stats").apply {
                colors = listOf(Color.parseColor("#00C853"), Color.parseColor("#FF5252"))
                valueTextColor = Color.WHITE
                valueTextSize = 13f
            }
            pieChart.data = PieData(dataSet)
            pieChart.description.isEnabled = false
            pieChart.legend.textColor = Color.WHITE
            pieChart.invalidate()

            rvAnalytics.adapter = AttendanceHistoryActivity.HistoryAdapter(recentRecords)
        }
    }

    private fun exportData() {
        lifecycleScope.launch {
            val records = repository.getAttendanceHistory()
            val csvData = StringBuilder("Name,Status,Date\n")
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            
            records.forEach { r ->
                csvData.append("${r.studentName},Present,${sdf.format(Date(r.timestamp))}\n")
            }
            Toast.makeText(context, "Exported: ${records.size} entries", Toast.LENGTH_SHORT).show()
        }
    }

    inner class SubjectAdapter(private val list: List<JSONObject>) : RecyclerView.Adapter<SubjectAdapter.VH>() {
        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val title: TextView = view.findViewById(R.id.title)
            val subtitle: TextView = view.findViewById(R.id.subtitle)
            val tag: TextView = view.findViewById(R.id.tag)
            val icon: TextView = view.findViewById(R.id.icon)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_row, parent, false)
            return VH(v)
        }
        override fun onBindViewHolder(holder: VH, position: Int) {
            val obj = list[position]
            val subName = obj.optString("subject", "Unknown")
            val p = obj.optInt("present", 0)
            val t = obj.optInt("total", 0)
            val pct = obj.optDouble("pct", 0.0)

            holder.title.text = subName
            holder.subtitle.text = "$p / $t classes attended"
            holder.tag.text = String.format("%.1f%%", pct)
            holder.icon.text = "📚"
            
            if (pct >= 75.0) {
                holder.tag.setTextColor(Color.parseColor("#00C853")) // Green
            } else {
                holder.tag.setTextColor(Color.parseColor("#FF5252")) // Red
            }
        }
        override fun getItemCount() = list.size
    }
}

