package com.smartroll.fragments

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smartroll.R
import com.smartroll.api.ApiService
import com.smartroll.db.UserEntity
import com.smartroll.repository.MainRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StudentsFragment : Fragment() {

    private lateinit var repository: MainRepository
    private lateinit var rvStudents: RecyclerView
    private lateinit var tvEmpty: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_students, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = MainRepository(requireContext())

        rvStudents = view.findViewById(R.id.rvStudents)
        tvEmpty = view.findViewById(R.id.tvEmpty)

        rvStudents.layoutManager = LinearLayoutManager(context)

        loadStudents()
    }

    private fun loadStudents() {
        lifecycleScope.launch {
            val currentUser = repository.getCurrentUser() ?: return@launch

            // Bug fix: agar branch/section null ya blank hai, filter mat karo — sab students lao
            val branch = currentUser.branch?.takeIf { it.isNotBlank() }
            val section = currentUser.section?.takeIf { it.isNotBlank() }

            val students = repository.getRegisteredUsers(branch, section)

            if (students.isEmpty()) {
                tvEmpty.visibility = View.VISIBLE
                tvEmpty.text = "No students in this class"
                rvStudents.visibility = View.GONE
            } else {
                tvEmpty.visibility = View.GONE
                rvStudents.visibility = View.VISIBLE

                rvStudents.adapter = StudentClickAdapter(students) { student ->
                    showStudentAnalytics(student)
                }

                rvStudents.alpha = 0f
                rvStudents.animate().alpha(1f).setDuration(600).start()
            }
        }
    }

    // ── Individual Student Analytics Dialog ──────────────────────
    private fun showStudentAnalytics(student: UserEntity) {
        val dialog = Dialog(requireContext(), R.style.Theme_BlinkERP)
        dialog.setContentView(R.layout.dialog_student_analytics)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val tvName = dialog.findViewById<TextView>(R.id.tvStudentName)
        val tvStatus = dialog.findViewById<TextView>(R.id.tvAttStatus)
        val tvPct = dialog.findViewById<TextView>(R.id.tvAttPct)
        val tvPresent = dialog.findViewById<TextView>(R.id.tvPresent)
        val tvAbsent = dialog.findViewById<TextView>(R.id.tvAbsent)
        val tvBunkMsg = dialog.findViewById<TextView>(R.id.tvBunkMsg)
        val tvClose = dialog.findViewById<TextView>(R.id.tvClose)
        val rvSubjects = dialog.findViewById<RecyclerView>(R.id.rvSubjectsDialog)
        val tvLoading = dialog.findViewById<TextView>(R.id.tvLoading)

        tvName.text = student.name
        tvLoading.visibility = View.VISIBLE
        rvSubjects.layoutManager = LinearLayoutManager(requireContext())

        tvClose.setOnClickListener { dialog.dismiss() }
        dialog.show()

        lifecycleScope.launch {
            val data = withContext(Dispatchers.IO) {
                try {
                    ApiService.getStudentAnalytics(student.name, student.branch, student.section)
                } catch (e: Exception) { null }
            }

            tvLoading.visibility = View.GONE

            if (data == null) {
                tvStatus.text = "❌ Could not load data"
                return@launch
            }

            val pct = data.optDouble("attendance_pct", 0.0)
            val presentCount = data.optInt("present_count", 0)
            val totalSessions = data.optInt("total_sessions", 0)
            val absentCount = totalSessions - presentCount
            val status = data.optString("eligibility_status", "Safe")
            val bunkMsg = data.optString("bunk_message", "")
            val isSafe = status.equals("Safe", ignoreCase = true)

            tvPct.text = String.format("%.1f%%", pct)
            tvPct.setTextColor(if (isSafe) Color.parseColor("#00C853") else Color.parseColor("#FF5252"))
            tvPresent.text = "✅ Present: $presentCount"
            tvAbsent.text = "❌ Absent: $absentCount"
            tvStatus.text = if (isSafe) "✅ SAFE" else "⚠️ SHORT"
            tvStatus.setTextColor(if (isSafe) Color.parseColor("#00C853") else Color.parseColor("#FF5252"))
            tvBunkMsg.text = "🎯 $bunkMsg"

            // Subjects
            val subjectsArr = data.optJSONArray("subject_breakdown")
            val subList = mutableListOf<org.json.JSONObject>()
            if (subjectsArr != null) {
                for (i in 0 until subjectsArr.length()) subList.add(subjectsArr.getJSONObject(i))
            }
            rvSubjects.adapter = SubjectMiniAdapter(subList)
        }
    }

    // ── Adapters ────────────────────────────────────────────────
    inner class StudentClickAdapter(
        private val students: List<UserEntity>,
        private val onClick: (UserEntity) -> Unit
    ) : RecyclerView.Adapter<StudentClickAdapter.VH>() {

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val icon: TextView = view.findViewById(R.id.icon)
            val title: TextView = view.findViewById(R.id.title)
            val subtitle: TextView = view.findViewById(R.id.subtitle)
            val tag: TextView = view.findViewById(R.id.tag)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_row, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(h: VH, position: Int) {
            val u = students[position]
            h.icon.text = "🎓"
            h.title.text = u.name
            h.subtitle.text = "${u.branch ?: ""}-${u.section ?: ""}"
            h.tag.text = "📊 Analytics"
            h.tag.setTextColor(Color.parseColor("#00B4D8"))
            h.itemView.setOnClickListener { onClick(u) }
        }

        override fun getItemCount() = students.size
    }

    inner class SubjectMiniAdapter(private val list: List<org.json.JSONObject>) :
        RecyclerView.Adapter<SubjectMiniAdapter.VH>() {

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

        override fun onBindViewHolder(h: VH, position: Int) {
            val obj = list[position]
            val sub = obj.optString("subject", "Unknown")
            val p = obj.optInt("present", 0)
            val t = obj.optInt("total", 0)
            val pct = obj.optDouble("percentage", 0.0)
            h.icon.text = "📚"
            h.title.text = sub
            h.subtitle.text = "$p/$t classes"
            h.tag.text = String.format("%.1f%%", pct)
            h.tag.setTextColor(if (pct >= 75.0) Color.parseColor("#00C853") else Color.parseColor("#FF5252"))
        }

        override fun getItemCount() = list.size
    }
}
