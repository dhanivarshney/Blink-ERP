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

    private val remoteIdMap = mutableMapOf<String, Int>()

    private fun filterUsers() {
        val role = spinnerRole.selectedItem.toString()
        val branch = spinnerBranch.selectedItem.toString()
        val section = spinnerSection.selectedItem.toString()

        val filtered = allUsers.filter { u ->
            (role == "All Roles" || u.role.equals(role, true)) &&
            (branch == "All Branches" || (u.branch != null && u.branch.equals(branch, true))) &&
            (section == "All Sections" || (u.section != null && u.section.equals(section, true)))
        }
        rvUsers.adapter = AdminUserAdapter(
            users = filtered,
            onItemClick = { showUserActions(it) },
            onDelete = { showDeleteConfirm(it) }
        )
    }

    private fun loadAllUsers() {
        loader.visibility = View.VISIBLE
        lifecycleScope.launch {
            val localUsers = repository.getAllLocalUsers()
            val remoteUsersJson = try { ApiService.getUsers() } catch (e: Exception) { null }
            
            val remoteUsers = mutableListOf<UserEntity>()
            if (remoteUsersJson != null) {
                for (i in 0 until remoteUsersJson.length()) {
                    val u = remoteUsersJson.getJSONObject(i)
                    val name = u.getString("name")
                    val uid = u.optInt("id", 0)
                    if (uid > 0) remoteIdMap[name] = uid
                    remoteUsers.add(UserEntity(
                        name = name,
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

    private fun showUserActions(user: UserEntity) {
        val options = arrayOf("🔑 Reset Password", "🗑️ Delete User")
        AlertDialog.Builder(this)
            .setTitle(user.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> promptResetPassword(user)
                    1 -> showDeleteConfirm(user)
                }
            }
            .show()
    }

    private fun promptResetPassword(user: UserEntity) {
        val input = android.widget.EditText(this).apply {
            hint = "Enter new password (min 4 chars)"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        val container = android.widget.FrameLayout(this).apply {
            setPadding(50, 30, 50, 20)
            addView(input)
        }

        AlertDialog.Builder(this)
            .setTitle("Reset Password: ${user.name}")
            .setView(container)
            .setPositiveButton("Update") { _, _ ->
                val newPass = input.text.toString().trim()
                if (newPass.length < 4) {
                    Toast.makeText(this, "Password must be at least 4 characters", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val serverId = remoteIdMap[user.name]
                lifecycleScope.launch {
                    val success = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        try {
                            if (serverId != null && serverId > 0) {
                                val res = ApiService.updateUserPassword(serverId, newPass)
                                res != null && res.optString("status") == "updated"
                            } else {
                                true
                            }
                        } catch (e: Exception) { false }
                    }
                    if (success) {
                        Toast.makeText(this@AdminActivity, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                        loadAllUsers()
                    } else {
                        Toast.makeText(this@AdminActivity, "Failed to update password", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
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
        private val onItemClick: (UserEntity) -> Unit,
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
            h.itemView.setOnClickListener { onItemClick(u) }
        }

        override fun getItemCount() = users.size
    }
}

