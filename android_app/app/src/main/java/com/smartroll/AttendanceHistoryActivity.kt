package com.smartroll

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smartroll.db.AttendanceRecordEntity
import com.smartroll.repository.MainRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AttendanceHistoryActivity : AppCompatActivity() {

    private lateinit var repository: MainRepository
    private lateinit var rvHistory: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        repository = MainRepository(this)
        rvHistory = findViewById(R.id.rvHistory)
        rvHistory.layoutManager = LinearLayoutManager(this)

        loadHistory()
    }

    private fun loadHistory() {
        lifecycleScope.launch {
            val records = repository.getAttendanceHistory()
            rvHistory.adapter = HistoryAdapter(records)
        }
    }

    class HistoryAdapter(private val records: List<AttendanceRecordEntity>) : RecyclerView.Adapter<HistoryAdapter.VH>() {
        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val icon = v.findViewById<TextView>(R.id.icon)
            val title = v.findViewById<TextView>(R.id.title)
            val subtitle = v.findViewById<TextView>(R.id.subtitle)
            val tag = v.findViewById<TextView>(R.id.tag)
            val btnDelete = v.findViewById<View>(R.id.btnDelete)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_row, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(h: VH, position: Int) {
            val r = records[position]
            h.icon.text = "✅"
            h.title.text = r.studentName
            val date = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(Date(r.timestamp))
            val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(r.timestamp))
            h.subtitle.text = "${r.branch}-${r.section} · $date at $time"
            h.tag.text = r.mode
            h.btnDelete.visibility = View.GONE
        }

        override fun getItemCount() = records.size
    }
}
