package com.smartroll

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smartroll.api.ApiService
import com.smartroll.db.UserEntity
import com.smartroll.repository.MainRepository
import kotlinx.coroutines.launch

class AdminActivity : AppCompatActivity() {

    private lateinit var repository: MainRepository
    private lateinit var rvUsers: RecyclerView
    private lateinit var loader: View
    private lateinit var spinnerRole: Spinner
    private lateinit var spinnerBranch: Spinner
    private lateinit var spinnerSection: Spinner

    private var allUsers = listOf<UserEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        repository = MainRepository(this)
        rvUsers = findViewById(R.id.rvAdminUsers)
        loader = findViewById(R.id.adminLoader)
        spinnerRole = findViewById(R.id.spinnerRole)
        spinnerBranch = findViewById(R.id.spinnerBranch)
        spinnerSection = findViewById(R.id.spinnerSection)
        
        setupSpinners()

        rvUsers.layoutManager = LinearLayoutManager(this)

        loadAllUsers()
    }

    private fun setupSpinners() {
        val roles = listOf("All Roles", "Student", "Teacher")
        val branches = listOf("All Branches", "CSE", "CSE AIML", "ECE", "IT", "MECH", "CIVIL", "EE")
        val sections = listOf("All Sections", "A", "B", "C", "D")

        spinnerRole.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)
        spinnerBranch.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, branches)
        spinnerSection.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, sections)

        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) { filterUsers() }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        spinnerRole.onItemSelectedListener = listener
        spinnerBranch.onItemSelectedListener = listener
        spinnerSection.onItemSelectedListener = listener
    }

    private fun filterUsers() {
        val role = spinnerRole.selectedItem.toString()
        val branch = spinnerBranch.selectedItem.toString()
        val section = spinnerSection.selectedItem.toString()

        val filtered = allUsers.filter { u ->
            (role == "All Roles" || u.role.equals(role, true)) &&
            (branch == "All Branches" || (u.branch != null && u.branch.equals(branch, true))) &&
            (section == "All Sections" || (u.section != null && u.section.equals(section, true)))
        }
        rvUsers.adapter = AdminUserAdapter(filtered) { showDeleteConfirm(it) }
    }

    private fun loadAllUsers() {
        loader.visibility = View.VISIBLE
        lifecycleScope.launch {
            val localUsers = repository.getAllLocalUsers()
            val remoteUsersJson = try {ApiService.getUsers() } catch (e: Exception) { null }
            
            val remoteUsers = mutableListOf<UserEntity>()
            if (remoteUsersJson != null) {
                for (i in 0 until remoteUsersJson.length()) {
                    val u = remoteUsersJson.getJSONObject(i)
                    remoteUsers.add(UserEntity(
                        name = u.getString("name"),
                        password = u.optString("password", "N/A"),
                        role = u.getString("role"),
                        course = u.optString("course", ""),
                        year = u.optString("year", ""),
                        branch = u.optString("branch", ""),
                        section = u.optString("section", ""),
                        subject = u.optString("subject", ""),
                        synced = true
                    ))
                }
            }

            allUsers = (localUsers + remoteUsers).distinctBy { it.name }
            filterUsers()
            loader.visibility = View.GONE
        }
    }

    private fun showDeleteConfirm(user: UserEntity) {
        AlertDialog.Builder(this)
            .setTitle("Delete User")
            .setMessage("Remove ${user.name} (${user.role}) permanently?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    repository.deleteUser(user)
                    Toast.makeText(this@AdminActivity, "User deleted", Toast.LENGTH_SHORT).show()
                    loadAllUsers()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    class AdminUserAdapter(
        private val users: List<UserEntity>,
        private val onDelete: (UserEntity) -> Unit
    ) : RecyclerView.Adapter<AdminUserAdapter.VH>() {
        
        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val title: TextView = v.findViewById(R.id.title)
            val subtitle: TextView = v.findViewById(R.id.subtitle)
            val tag: TextView = v.findViewById(R.id.tag)
            val btnDelete: View = v.findViewById(R.id.btnDelete)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_row, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(h: VH, position: Int) {
            val u = users[position]
            h.title.text = "${u.name} (PW: ${u.password})"
            h.subtitle.text = "Role: ${u.role.uppercase()} | ${u.branch ?: ""}-${u.section ?: ""}"
            h.tag.text = u.course ?: "N/A"
            h.btnDelete.visibility = View.VISIBLE
            h.btnDelete.setOnClickListener { onDelete(u) }
        }

        override fun getItemCount() = users.size
    }
}
