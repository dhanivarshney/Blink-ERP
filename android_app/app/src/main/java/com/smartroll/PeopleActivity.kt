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
import com.smartroll.db.UserEntity
import com.smartroll.repository.MainRepository
import kotlinx.coroutines.launch

class PeopleActivity : AppCompatActivity() {

    private lateinit var repository: MainRepository
    private lateinit var rvUsers: RecyclerView
    private lateinit var loader: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_people)

        repository = MainRepository(this)
        rvUsers = findViewById(R.id.rvUsers)
        loader = findViewById(R.id.loader)

        rvUsers.layoutManager = LinearLayoutManager(this)

        loadUsers()
    }

    private fun loadUsers() {
        loader.visibility = View.VISIBLE
        lifecycleScope.launch {
            val currentUser = repository.getCurrentUser()
            // Branch aur Section ke according filter karo
            val users = repository.getRegisteredUsers(currentUser?.branch, currentUser?.section)
            rvUsers.adapter = UserAdapter(users)
            loader.visibility = View.GONE
        }
    }

    class UserAdapter(private val users: List<UserEntity>) : RecyclerView.Adapter<UserAdapter.VH>() {
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
            val u = users[position]
            h.icon.text = if (u.role == "teacher") "👨‍🏫" else "🎓"
            h.title.text = u.name
            h.subtitle.text = "${u.course} · ${u.branch}-${u.section}"
            h.tag.text = u.role.uppercase()
            h.btnDelete.visibility = View.GONE
        }

        override fun getItemCount() = users.size
    }
}
