package com.smartroll

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smartroll.api.ApiService
import com.smartroll.db.UserEntity
import com.smartroll.repository.MainRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

class NotesActivity : AppCompatActivity() {

    private lateinit var repository: MainRepository
    private lateinit var rvNotes: RecyclerView
    private lateinit var loader: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var addNoteForm: LinearLayout
    private var currentUser: UserEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

        ApiService.init(this)
        repository = MainRepository(this)
        rvNotes = findViewById(R.id.rvNotes)
        loader = findViewById(R.id.loader)
        tvEmpty = findViewById(R.id.tvEmpty)
        addNoteForm = findViewById(R.id.addNoteForm)

        rvNotes.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            currentUser = repository.getCurrentUser()
        }

        // Add Note Button
        findViewById<Button>(R.id.btnAddNote).setOnClickListener {
            addNoteForm.visibility = if (addNoteForm.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        // Cancel
        findViewById<Button>(R.id.btnCancelNote).setOnClickListener {
            addNoteForm.visibility = View.GONE
            findViewById<EditText>(R.id.etNoteTitle).text.clear()
            findViewById<EditText>(R.id.etNoteContent).text.clear()
        }

        // Save Note
        findViewById<Button>(R.id.btnSaveNote).setOnClickListener {
            val title = findViewById<EditText>(R.id.etNoteTitle).text.toString().trim()
            val content = findViewById<EditText>(R.id.etNoteContent).text.toString().trim()

            if (title.isEmpty()) {
                Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = currentUser ?: return@setOnClickListener
            lifecycleScope.launch {
                val success = withContext(Dispatchers.IO) {
                    try {
                        val res = ApiService.addNote(
                            user.name, user.branch ?: "", user.section ?: "",
                            user.subject ?: "", title, content
                        )
                        res != null && res.has("note_id")
                    } catch (e: Exception) { false }
                }
                if (success) {
                    Toast.makeText(this@NotesActivity, "Note saved!", Toast.LENGTH_SHORT).show()
                    addNoteForm.visibility = View.GONE
                    findViewById<EditText>(R.id.etNoteTitle).text.clear()
                    findViewById<EditText>(R.id.etNoteContent).text.clear()
                    loadNotes()
                } else {
                    Toast.makeText(this@NotesActivity, "Failed to save note", Toast.LENGTH_SHORT).show()
                }
            }
        }

        loadNotes()
    }

    private fun loadNotes() {
        loader.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE

        val user = currentUser
        lifecycleScope.launch {
            val notesArray = withContext(Dispatchers.IO) {
                try {
                    ApiService.getNotes(teacher = user?.name)
                } catch (e: Exception) { null }
            }

            val notes = mutableListOf<NoteItem>()
            if (notesArray != null) {
                // Flask returns a bare JSON array: [{"id":1,...}, ...]
                val jsonStr = notesArray.toString()
                val arr = if (jsonStr.startsWith("[")) org.json.JSONArray(jsonStr) else notesArray.optJSONArray("notes")
                if (arr != null) {
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        notes.add(NoteItem(
                            id = obj.getInt("id"),
                            title = obj.getString("title"),
                            content = obj.optString("content", ""),
                            subject = obj.optString("subject", ""),
                            createdAt = obj.optString("created_at", "")
                        ))
                    }
                }
            }

            loader.visibility = View.GONE
            if (notes.isEmpty()) {
                tvEmpty.visibility = View.VISIBLE
            }
        val isTeacher = currentUser?.role == "teacher"
        rvNotes.adapter = NotesAdapter(notes, isTeacher) { noteId ->
            lifecycleScope.launch {
                val success = withContext(Dispatchers.IO) {
                    try { ApiService.deleteNote(noteId) != null } catch (e: Exception) { false }
                }
                if (success) {
                    Toast.makeText(this@NotesActivity, "Note deleted!", Toast.LENGTH_SHORT).show()
                    loadNotes()
                }
            }
        }
        }
    }

    data class NoteItem(val id: Int, val title: String, val content: String, val subject: String, val createdAt: String, val pdfPath: String? = null)

    class NotesAdapter(
        private val notes: List<NoteItem>, 
        private val isTeacher: Boolean = false,
        private val onDelete: ((Int) -> Unit)? = null
    ) : RecyclerView.Adapter<NotesAdapter.VH>() {
        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tvTitle: TextView = v.findViewById(R.id.title)
            val tvContent: TextView = v.findViewById(R.id.subtitle)
            val tvTag: TextView = v.findViewById(R.id.tag)
            val tvIcon: TextView = v.findViewById(R.id.icon)
            val tvPdfIndicator: TextView = v.findViewById(R.id.tvPdfIndicator)
            val btnDelete: TextView = v.findViewById(R.id.btnDelete)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_row, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(h: VH, position: Int) {
            val note = notes[position]
            h.tvIcon.text = "📝"
            h.tvTitle.text = note.title
            h.tvContent.text = if (note.content.length > 80) note.content.take(80) + "..." else note.content
            h.tvTag.text = note.subject
            h.tvPdfIndicator.visibility = if (note.pdfPath != null) View.VISIBLE else View.GONE
            
            h.btnDelete.visibility = if (isTeacher) View.VISIBLE else View.GONE
            h.btnDelete.setOnClickListener {
                onDelete?.invoke(note.id)
            }
            
            h.itemView.setOnClickListener {
                if (!note.pdfPath.isNullOrBlank()) {
                    try {
                        val uri = Uri.parse(note.pdfPath)
                        if (note.pdfPath.startsWith("http://") || note.pdfPath.startsWith("https://")) {
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            h.itemView.context.startActivity(intent)
                        } else {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "application/pdf")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            h.itemView.context.startActivity(intent)
                        }
                    } catch (e: Exception) {
                        try {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(note.pdfPath))
                            h.itemView.context.startActivity(browserIntent)
                        } catch (e2: Exception) {
                            Toast.makeText(h.itemView.context, "Cannot open PDF", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        override fun getItemCount() = notes.size
    }
}
