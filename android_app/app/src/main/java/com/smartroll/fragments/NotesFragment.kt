package com.smartroll.fragments

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smartroll.NotesActivity
import com.smartroll.R
import com.smartroll.db.NoteEntity
import com.smartroll.db.UserEntity
import com.smartroll.repository.MainRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotesFragment : Fragment() {

    private lateinit var repository: MainRepository
    private lateinit var rvNotes: RecyclerView
    private lateinit var loader: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var addNoteForm: LinearLayout
    private var currentUser: UserEntity? = null
    private var selectedPdfUri: Uri? = null

    private val pdfPickerLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedPdfUri = uri
            view?.findViewById<TextView>(R.id.tvPdfName)?.text = getFileName(uri)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_notes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        repository = MainRepository(requireContext())
        rvNotes = view.findViewById(R.id.rvNotes)
        loader = view.findViewById(R.id.loader)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        addNoteForm = view.findViewById(R.id.addNoteForm)

        rvNotes.layoutManager = LinearLayoutManager(context)

        lifecycleScope.launch {
            currentUser = repository.getCurrentUser()
            loadNotes()
        }

        view.findViewById<Button>(R.id.btnAddNote).setOnClickListener {
            addNoteForm.visibility = if (addNoteForm.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        view.findViewById<Button>(R.id.btnCancelNote).setOnClickListener {
            addNoteForm.visibility = View.GONE
            resetForm(view)
        }

        view.findViewById<Button>(R.id.btnAttachPdf).setOnClickListener {
            pdfPickerLauncher.launch("application/pdf")
        }

        view.findViewById<Button>(R.id.btnSaveNote).setOnClickListener {
            saveNote(view)
        }
    }

    private fun saveNote(view: View) {
        val title = view.findViewById<EditText>(R.id.etNoteTitle).text.toString().trim()
        val content = view.findViewById<EditText>(R.id.etNoteContent).text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(context, "Title is required", Toast.LENGTH_SHORT).show()
            return
        }

        val user = currentUser ?: return
        lifecycleScope.launch {
            val note = NoteEntity(
                teacherName = user.name,
                branch = user.branch ?: "",
                section = user.section ?: "",
                subject = user.subject ?: "",
                title = title,
                content = content,
                pdfPath = selectedPdfUri?.toString(),
                createdAt = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
            )
            repository.saveNote(note)
            Toast.makeText(context, "Note saved!", Toast.LENGTH_SHORT).show()
            addNoteForm.visibility = View.GONE
            resetForm(view)
            loadNotes()
        }
    }

    private fun resetForm(view: View) {
        view.findViewById<EditText>(R.id.etNoteTitle).text.clear()
        view.findViewById<EditText>(R.id.etNoteContent).text.clear()
        view.findViewById<TextView>(R.id.tvPdfName).text = "No PDF attached"
        selectedPdfUri = null
    }

    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) result = cursor.getString(index)
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1 && result != null) {
                result = result.substring(cut + 1)
            }
        }
        return result ?: "unknown.pdf"
    }

    private fun loadNotes() {
        loader.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE
        
        lifecycleScope.launch {
            val user = currentUser
            val notes = withContext(Dispatchers.IO) {
                try {
                    if (user?.role == "teacher") {
                        repository.getNotes(teacherName = user.name)
                    } else {
                        repository.getNotes(branch = user?.branch, section = user?.section)
                    }
                } catch (e: Exception) {
                    emptyList()
                }
            }
            
            val noteItems = notes.map {
                NotesActivity.NoteItem(
                    id = it.id,
                    title = it.title,
                    content = it.content,
                    subject = it.subject,
                    createdAt = it.createdAt,
                    pdfPath = it.pdfPath
                )
            }

            loader.visibility = View.GONE
            tvEmpty.visibility = if (noteItems.isEmpty()) View.VISIBLE else View.GONE
            
            rvNotes.adapter = NotesActivity.NotesAdapter(
                notes = noteItems,
                isTeacher = user?.role == "teacher",
                onDelete = { deleteNote(it) }
            )
            
            // Animation
            rvNotes.alpha = 0f
            rvNotes.animate().alpha(1f).setDuration(600).start()
        }
    }

    private fun deleteNote(noteId: Int) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Note")
            .setMessage("Are you sure you want to delete this note?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    repository.deleteNote(noteId)
                    Toast.makeText(context, "Note deleted", Toast.LENGTH_SHORT).show()
                    loadNotes()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}