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
import com.smartroll.api.ApiService
import com.smartroll.db.AttendanceRecordEntity
import com.smartroll.repository.MainRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class AnalyticsFragment : Fragment() {

    private lateinit var repository: MainRepository

    // Store server data for export
    private var exportCsvData: String = ""

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

            // ══════════════════════════════════════════════════
            // STUDENT VIEW — fetch personal analytics from server
            // ══════════════════════════════════════════════════
            if (user != null && user.role == "student") {
                bunkHeroCard.visibility = View.VISIBLE
                subjectBreakdownCard.visibility = View.VISIBLE
                view.findViewById<TextView>(R.id.tvAnalyticsSubtitle).text = "Student specific overview"

                val remoteData = withContext(Dispatchers.IO) {
                    try {
                        ApiService.getStudentAnalytics(user.name, user.branch, user.section)
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
                        bunkProgressBar.setBackgroundColor(Color.parseColor("#00C853"))
                    } else {
                        tvStatusEmoji.text = "⚠️"
                        tvBunkStatus.text = "SHORT"
                        tvBunkStatus.setTextColor(Color.parseColor("#FF5252"))
                        bunkProgressBar.setBackgroundColor(Color.parseColor("#FF5252"))
                    }

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
                        for (i in 0 until subjectsArr.length()) subList.add(subjectsArr.getJSONObject(i))
                    }
                    rvSubjects.adapter = SubjectAdapter(subList)

                    // Pie Chart
                    tvTotalRecords.text = "$presentCount Present | $absentCount Absent"
                    setupPieChart(pieChart, presentCount, absentCount, pct, isSafe)

                    // History list
                    val historyArr = remoteData.optJSONArray("history")
                    val historyList = mutableListOf<AttendanceRecordEntity>()
                    val csvSb = StringBuilder("Subject,Date,Status\n")
                    if (historyArr != null) {
                        for (i in 0 until historyArr.length()) {
                            val h = historyArr.getJSONObject(i)
                            val isPresent = h.optString("status") == "Present"
                            val subj = h.optString("subject", "Class")
                            val date = h.optString("date", "")
                            val st = if (isPresent) "Present" else "Absent"
                            csvSb.append("$subj,$date,$st\n")
                            if (isPresent) {
                                historyList.add(AttendanceRecordEntity(
                                    sessionId = h.optString("session_id", ""),
                                    studentName = "$subj ($date)",
                                    branch = h.optString("branch", ""),
                                    section = h.optString("section", ""),
                                    mode = h.optString("mode", "Auto"),
                                    timestamp = System.currentTimeMillis()
                                ))
                            }
                        }
                    }
                    exportCsvData = csvSb.toString()
                    rvAnalytics.adapter = AttendanceHistoryActivity.HistoryAdapter(historyList)
                    return@launch
                }
            }

            // ══════════════════════════════════════════════════
            // TEACHER VIEW — fetch class analytics from SERVER
            // ══════════════════════════════════════════════════
            bunkHeroCard.visibility = View.GONE
            subjectBreakdownCard.visibility = View.GONE
            view.findViewById<TextView>(R.id.tvAnalyticsSubtitle).text = "Class attendance overview"

            val branch = user?.branch?.takeIf { it.isNotBlank() }
            val section = user?.section?.takeIf { it.isNotBlank() }
            val teacherName = user?.name

            val serverData = withContext(Dispatchers.IO) {
                try {
                    ApiService.getTeacherAnalytics(teacherName, branch, section)
                } catch (e: Exception) { null }
            }

            if (serverData != null) {
                val totalPresent = serverData.optInt("overall_present", 0)
                val totalAbsent = serverData.optInt("overall_absent", 0)
                val pct = serverData.optDouble("overall_pct", 0.0)
                val sessions = serverData.optJSONArray("sessions")

                tvTotalRecords.text = "Present: $totalPresent | Absent: $totalAbsent"
                setupPieChart(pieChart, totalPresent, totalAbsent, pct, pct >= 75.0)

                // Build history list from sessions
                val historyList = mutableListOf<AttendanceRecordEntity>()
                val csvSb = StringBuilder("Session,Subject,Date,Present,Absent,Total\n")

                if (sessions != null) {
                    for (i in 0 until sessions.length()) {
                        val s = sessions.getJSONObject(i)
                        val subj = s.optString("subject", "Class")
                        val date = s.optString("date", "")
                        val presentCount = s.optInt("present_count", 0)
                        val absentCount = s.optInt("absent_count", 0)
                        val totalStu = s.optInt("total_students", 0)
                        val sessionId = s.optString("session_id", "")
                        csvSb.append("$sessionId,$subj,$date,$presentCount,$absentCount,$totalStu\n")

                        historyList.add(AttendanceRecordEntity(
                            sessionId = sessionId,
                            studentName = "$subj — ✅ $presentCount / $totalStu ($date)",
                            branch = branch ?: "",
                            section = section ?: "",
                            mode = "Session",
                            timestamp = System.currentTimeMillis()
                        ))
                    }
                }
                exportCsvData = csvSb.toString()
                rvAnalytics.adapter = AttendanceHistoryActivity.HistoryAdapter(historyList)
            } else {
                // Fallback: local Room DB (when server unreachable)
                val records = repository.getAttendanceHistory()
                val allStudents = repository.getRegisteredUsers(branch, section)
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                val weekAgo = calendar.timeInMillis
                val recentRecords = records.filter { it.timestamp >= weekAgo }
                val presentNames = recentRecords.map { it.studentName }.distinct()
                val absentStudents = allStudents.filter { it.name !in presentNames }

                tvTotalRecords.text = "Present: ${presentNames.size} | Absent: ${absentStudents.size} (local)"
                setupPieChart(pieChart, presentNames.size, absentStudents.size, 0.0, false)

                val csvSb = StringBuilder("Name,Status,Date\n")
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                recentRecords.forEach { r ->
                    csvSb.append("${r.studentName},Present,${sdf.format(Date(r.timestamp))}\n")
                }
                exportCsvData = csvSb.toString()
                rvAnalytics.adapter = AttendanceHistoryActivity.HistoryAdapter(recentRecords)
            }
        }
    }

    // ── Export — now exports server data (not just local) ─────
    private fun exportData() {
        if (exportCsvData.isBlank()) {
            Toast.makeText(context, "No data to export yet. Wait for data to load.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val fileName = "BlinkERP_Attendance_${System.currentTimeMillis()}.csv"
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
            val file = java.io.File(downloadsDir, fileName)
            file.writeText(exportCsvData)
            Toast.makeText(context, "✅ Exported to Downloads/$fileName", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            // Fallback: share via intent
            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(android.content.Intent.EXTRA_TEXT, exportCsvData)
                putExtra(android.content.Intent.EXTRA_SUBJECT, "BlinkERP Attendance Export")
            }
            startActivity(android.content.Intent.createChooser(shareIntent, "Share Attendance Data"))
        }
    }

    // ── Pie Chart Helper ─────────────────────────────────────
    private fun setupPieChart(pieChart: PieChart, present: Int, absent: Int, pct: Double, isSafe: Boolean) {
        val entries = listOf(
            PieEntry(present.toFloat(), "Present"),
            PieEntry(absent.toFloat(), "Absent")
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
            val pct = obj.optDouble("pct", obj.optDouble("percentage", 0.0))

            holder.title.text = subName
            holder.subtitle.text = "$p / $t classes attended"
            holder.tag.text = String.format("%.1f%%", pct)
            holder.icon.text = "📚"

            if (pct >= 75.0) {
                holder.tag.setTextColor(Color.parseColor("#00C853"))
            } else {
                holder.tag.setTextColor(Color.parseColor("#FF5252"))
            }
        }
        override fun getItemCount() = list.size
    }
}
