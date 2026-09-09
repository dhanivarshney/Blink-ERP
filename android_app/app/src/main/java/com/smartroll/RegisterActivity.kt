package com.smartroll

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.smartroll.api.ApiService
import com.smartroll.repository.MainRepository
import kotlinx.coroutines.launch

/**
 * BlinkERP — Registration Screen
 * Teacher: name, password, course, branch, section, subject
 * Student: name, password, course, branch, section
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var repository: MainRepository
    private var role = "teacher"

    private val courses = listOf("B.Tech", "B.Pharma", "BCA", "MBA")
    private val years = listOf("1st Year", "2nd Year", "3rd Year", "4th Year")
private val btechBranches = listOf("CSE", "CSE AIML", "ECE", "IT", "MECH", "CIVIL", "EE")
private val otherBranches = listOf("General", "Marketing", "Finance", "HR")
    private val sections = listOf("A", "B", "C", "D")

    private val subjects2ndYear = listOf("COA", "Data Structures", "Maths", "Cyber Security", "UHV", "DSTL", "Python", "SI")
    private val subjectsDefault = listOf("General Studies", "Communication Skills", "Project", "Seminar")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        ApiService.init(this)
        repository = MainRepository(this)
        role = intent.getStringExtra("role") ?: "teacher"

        val titleText = findViewById<TextView>(R.id.registerTitle)
        val spYear = findViewById<Spinner>(R.id.spYear)
        val spCourse = findViewById<Spinner>(R.id.spCourse)
        val spBranch = findViewById<Spinner>(R.id.spBranch)
        val spSection = findViewById<Spinner>(R.id.spSection)
        val spSubject = findViewById<Spinner>(R.id.spSubject)
        val tvSubjectLabel = findViewById<TextView>(R.id.tvSubjectLabel)

        titleText.text = if (role == "teacher") "Teacher Registration" else "Student Registration"
        
        val tvRegServerIp = findViewById<TextView>(R.id.tvRegServerIp)
        tvRegServerIp.text = "🌐 Server: ${ApiService.serverUrl} (Tap to change)"
        tvRegServerIp.setOnClickListener {
            val input = EditText(this)
            input.hint = "192.168.194.186"
            val current = ApiService.serverUrl.replace("http://", "").replace(":5000", "")
            input.setText(current)

            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Set Laptop IP")
                .setMessage("Your Laptop WiFi IP is 192.168.194.186\n(Port :5000 will be added automatically)")
                .setView(input)
                .setPositiveButton("Save") { _, _ ->
                    val ip = input.text.toString().trim()
                    ApiService.updateUrl(this, ip)
                    tvRegServerIp.text = "🌐 Server: ${ApiService.serverUrl} (Tap to change)"
                    Toast.makeText(this, "Server updated to ${ApiService.serverUrl}!", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        
        // Hide subject for students
        val teacherOnlyVisibility = if (role == "teacher") android.view.View.VISIBLE else android.view.View.GONE
        findViewById<android.view.View>(R.id.llSubject).visibility = teacherOnlyVisibility
        tvSubjectLabel.visibility = teacherOnlyVisibility

        // Setup Year Spinner
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spYear.adapter = yearAdapter

        // Setup Course Spinner
        val courseAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, courses)
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCourse.adapter = courseAdapter

        // Setup Section Spinner
        val sectionAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sections)
        sectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spSection.adapter = sectionAdapter

        // Default Subject Adapter
        val subAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, subjectsDefault)
        subAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spSubject.adapter = subAdapter

        // Dynamic Subjects based on Year
        spYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedYear = years[position]
                val subList = if (selectedYear == "2nd Year") subjects2ndYear else subjectsDefault
                val subAdapter = ArrayAdapter(this@RegisterActivity, android.R.layout.simple_spinner_item, subList)
                subAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spSubject.adapter = subAdapter
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Setup Branch Spinner (Dynamic based on Course)
        spCourse.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedCourse = courses[position]
                val branches = if (selectedCourse == "B.Tech") btechBranches else otherBranches
                val branchAdapter = ArrayAdapter(this@RegisterActivity, android.R.layout.simple_spinner_item, branches)
                branchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spBranch.adapter = branchAdapter
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val etName = findViewById<EditText>(R.id.etName)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvSwitch = findViewById<TextView>(R.id.tvSwitchToLogin)

        btnRegister.text = "Register as ${role.replaceFirstChar { it.uppercase() }}"

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val year = spYear.selectedItem.toString()
            val course = spCourse.selectedItem.toString()
            val branch = spBranch.selectedItem.toString()
            val section = spSection.selectedItem.toString()
            val subject = if (role == "teacher") spSubject.selectedItem.toString() else null

            if (name.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val result = repository.register(name, password, role, course, year, branch, section, subject)

                if (result.isSuccess) {
                    Toast.makeText(this@RegisterActivity, "Registration successful! Welcome, $name", Toast.LENGTH_SHORT).show()

                    // Registration is permanent: skip straight to the Student / Teacher home screen without re-registering
                    val intent = Intent(this@RegisterActivity, DashboardActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Registration failed"
                    Toast.makeText(this@RegisterActivity, error, Toast.LENGTH_LONG).show()
                }
            }
        }


        tvSwitch.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java).apply {
                putExtra("role", role)
            })
        }
    }
}
