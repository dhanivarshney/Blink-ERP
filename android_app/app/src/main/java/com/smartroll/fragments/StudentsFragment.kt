package com.smartroll.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smartroll.R
import com.smartroll.PeopleActivity
import com.smartroll.repository.MainRepository
import kotlinx.coroutines.launch

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
            val branch = currentUser.branch
            val section = currentUser.section
            
            val students = repository.getRegisteredUsers(branch, section)
            
            if (students.isEmpty()) {
                tvEmpty.visibility = View.VISIBLE
                tvEmpty.text = "No students in this class"
            } else {
                tvEmpty.visibility = View.GONE
                rvStudents.adapter = PeopleActivity.UserAdapter(students)
                
                rvStudents.alpha = 0f
                rvStudents.animate().alpha(1f).setDuration(600).start()
            }
        }
    }
}
