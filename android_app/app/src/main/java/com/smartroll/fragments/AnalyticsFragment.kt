package com.smartroll.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.smartroll.R
import com.smartroll.AttendanceHistoryActivity
import com.smartroll.repository.MainRepository
import kotlinx.coroutines.launch
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
        view.findViewById<Button>(R.id.btnExport).setOnClickListener { exportData() }
        loadData(view)
    }

    private fun loadData(view: View) {
        lifecycleScope.launch {
            val records = repository.getAttendanceHistory()
            val allStudents = repository.getRegisteredUsers()
            
            // Filter: Last 7 days only
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -7)
            val weekAgo = calendar.timeInMillis
            val recentRecords = records.filter { it.timestamp >= weekAgo }

            val presentNames = recentRecords.map { it.studentName }.distinct()
            val absentStudents = allStudents.filter { it.name !in presentNames }
            
            // At Risk: Students with < 3 attendances in a week
            val attendanceCounts = recentRecords.groupBy { it.studentName }.mapValues { it.value.size }
            val atRisk = allStudents.filter { (attendanceCounts[it.name] ?: 0) < 3 }

            // UI Updates
            view.findViewById<TextView>(R.id.tvTotalRecords).text = "Present: ${presentNames.size} | Absent: ${absentStudents.size}"
            
            val pieChart = view.findViewById<PieChart>(R.id.pieChart)
            val entries = listOf(
                PieEntry(presentNames.size.toFloat(), "Present"),
                PieEntry(absentStudents.size.toFloat(), "Absent")
            )
            val dataSet = PieDataSet(entries, "Weekly Stats")
            dataSet.colors = listOf(0xFF00C853.toInt(), 0xFFFF5252.toInt())
            pieChart.data = PieData(dataSet)
            pieChart.invalidate()

            view.findViewById<RecyclerView>(R.id.rvAnalytics).adapter = AttendanceHistoryActivity.HistoryAdapter(recentRecords)
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
}
