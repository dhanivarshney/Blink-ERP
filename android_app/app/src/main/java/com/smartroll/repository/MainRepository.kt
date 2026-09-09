package com.smartroll.repository

import android.content.Context
import com.smartroll.api.ApiService
import com.smartroll.db.AppDatabase
import com.smartroll.db.AttendanceRecordEntity
import com.smartroll.db.NoteEntity
import com.smartroll.db.PyqEntity
import com.smartroll.db.SessionEntity
import com.smartroll.db.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject

class MainRepository(context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val userDao = db.userDao()
    private val sessionDao = db.sessionDao()
    private val attendanceDao = db.attendanceDao()
    private val pyqDao = db.pyqDao()
    private val noteDao = db.noteDao()

    companion object {
        // Persist session across fragment changes
        var currentSession: SessionEntity? = null
        var isScanningOrAdvertising: Boolean = false
        val detectedDevices = mutableSetOf<String>()

        // UI feedback: set to "marked" when a student's attendance is synced to the server
        val attendanceStatus = MutableStateFlow<String?>(null)
    }

    private val prefs = context.getSharedPreferences("smartroll_prefs", Context.MODE_PRIVATE)

    // ── Auth ─────────────────────────────────────────────────
    fun saveUserSession(user: UserEntity) {
        prefs.edit()
            .putString("current_user_id", user.id)
            .putString("user_name", user.name)
            .putString("user_role", user.role)
            .putString("user_course", user.course ?: "")
            .putString("user_year", user.year ?: "")
            .putString("user_branch", user.branch ?: "")
            .putString("user_section", user.section ?: "")
            .putString("user_subject", user.subject ?: "")
            .putBoolean("is_logged_in", true)
            .apply()
    }

    suspend fun login(name: String, password: String, role: String): Result<UserEntity> {
        // Try local first
        val localUser = userDao.login(name, password, role)
        if (localUser != null) {
            saveUserSession(localUser)
            return Result.success(localUser)
        }

        // Try remote
        val response = withContext(Dispatchers.IO) {
            try { ApiService.login(name, password, role) } catch (e: Exception) { null }
        }
        
        return if (response != null && response.has("user")) {
            val u = response.getJSONObject("user")
            val user = UserEntity(
                name = u.getString("name"),
                password = password, 
                role = u.getString("role"),
                course = u.optString("course", ""),
                year = u.optString("year", ""),
                branch = u.optString("branch", ""),
                section = u.optString("section", ""),
                subject = u.optString("subject", ""),
                synced = true
            )
            userDao.deleteUserByName(name)
            userDao.insertUser(user)
            saveUserSession(user)
            Result.success(user)
        } else {
            Result.failure(Exception("Login failed: Invalid credentials or Server unreachable"))
        }
    }

    suspend fun register(name: String, password: String, role: String,
                         course: String, year: String, branch: String, section: String, subject: String?): Result<UserEntity> {
        val response = withContext(Dispatchers.IO) {
            try { ApiService.register(name, password, role, course, year, branch, section, subject) } catch (e: Exception) { null }
        }

        // If user already registered on the server, automatically log them in!
        if (response != null && response.has("error")) {
            val errorMsg = response.optString("error", "")
            if (errorMsg.contains("Already registered", ignoreCase = true)) {
                val loginRes = login(name, password, role)
                if (loginRes.isSuccess) {
                    return loginRes
                }
            }
            return Result.failure(Exception(errorMsg))
        }
        
        val user = UserEntity(
            name = name,
            password = password,
            role = role,
            course = course,
            year = year,
            branch = branch,
            section = section,
            subject = subject,
            synced = response != null && response.has("user")
        )
        userDao.deleteUserByName(name)
        userDao.insertUser(user)
        saveUserSession(user)

        return Result.success(user)
    }

    private fun saveCurrentUserId(id: String) {
        prefs.edit().putString("current_user_id", id).apply()
    }

    suspend fun getCurrentUser(): UserEntity? {
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        val id = prefs.getString("current_user_id", null)
        if (!isLoggedIn && id == null) return null

        val localUser = if (id != null) userDao.getUserById(id) else null
        if (localUser != null) return localUser

        // Permanent fallback to SharedPreferences cached login
        val name = prefs.getString("user_name", null) ?: return null
        val role = prefs.getString("user_role", "student") ?: "student"
        val restored = UserEntity(
            id = id ?: java.util.UUID.randomUUID().toString(),
            name = name,
            password = "",
            role = role,
            course = prefs.getString("user_course", ""),
            year = prefs.getString("user_year", ""),
            branch = prefs.getString("user_branch", ""),
            section = prefs.getString("user_section", ""),
            subject = prefs.getString("user_subject", ""),
            synced = true
        )
        userDao.insertUser(restored)
        return restored
    }

    fun logout() {
        prefs.edit().clear().commit()
    }

    // ── Data Retrieval ───────────────────────────────────────
    suspend fun getPyqs(teacherName: String? = null, branch: String? = null, subject: String? = null): List<PyqEntity> {
        val response = withContext(Dispatchers.IO) {
            // Updated ApiService call could take more params if we had it, 
            // for now let's reuse getPyqs and assume it returns what we need or filter.
            try { ApiService.getPyqs(teacherName) } catch (e: Exception) { null }
        }
        
        if (response != null) {
            val list = mutableListOf<PyqEntity>()
            for (i in 0 until response.length()) {
                val p = response.getJSONObject(i)
                val pBranch = p.optString("branch", "")
                val pSubject = p.optString("subject", "")
                
                // Filter logic
                if ((branch == null || pBranch == branch) && (subject == null || pSubject == subject)) {
                    list.add(PyqEntity(
                        teacherName = p.optString("teacher_name", ""),
                        branch = pBranch,
                        subject = pSubject,
                        title = p.optString("title", ""),
                        semester = p.optString("semester", ""),
                        year = p.optString("year", ""),
                        examType = p.optString("exam_type", "PYQ"),
                        driveLink = p.optString("drive_link", ""),
                        createdAt = p.optString("created_at", "")
                    ))
                }
            }
            // Sync local cache
            // pyqDao.clearAll() // If we had a clearAll for pyqs
            pyqDao.insertPyqs(list)
            return list
        }
        
        // HACKATHON MODE: return empty if server fails
        val local = pyqDao.getAllPyqsList()
        return local.filter { it.teacherName != "System" && (branch == null || it.branch == branch) }
    }

    suspend fun getNotes(teacherName: String? = null, branch: String? = null, section: String? = null): List<NoteEntity> {
        val response = withContext(Dispatchers.IO) {
            try { ApiService.getNotes(teacher = teacherName, branch = branch, section = section) } catch (e: Exception) { null }
        }
        
        if (response != null) {
            val list = mutableListOf<NoteEntity>()
            val arr = if (response.toString().startsWith("[")) org.json.JSONArray(response.toString()) else response.optJSONArray("notes")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(NoteEntity(
                        id = obj.optInt("id", 0),
                        teacherName = obj.optString("teacher_name", ""),
                        branch = obj.optString("branch", ""),
                        section = obj.optString("section", ""),
                        subject = obj.optString("subject", ""),
                        title = obj.getString("title"),
                        content = obj.optString("content", ""),
                        createdAt = obj.optString("created_at", "")
                    ))
                }
            }
            // Sync local cache: clear and insert new
            noteDao.clearAll() 
            noteDao.insertNotes(list)
            return list
        }
        
        val local = noteDao.getAllNotes()
        // Filter out old demo data if it exists in DB
        return local.filter { it.teacherName != "System" }
    }

    suspend fun saveNote(note: NoteEntity) {
        val response = withContext(Dispatchers.IO) {
            try {
                ApiService.addNote(note.teacherName, note.branch, note.section, note.subject, note.title, note.content)
            } catch (e: Exception) { null }
        }
        if (response != null && response.has("note_id")) {
            val savedNote = note.copy(id = response.getInt("note_id"))
            noteDao.insertNote(savedNote)
        } else {
            noteDao.insertNote(note)
        }
    }

    suspend fun deleteNote(noteId: Int) {
        noteDao.deleteNoteById(noteId)
        withContext(Dispatchers.IO) {
            try { ApiService.deleteNote(noteId) } catch (e: Exception) {}
        }
    }

    suspend fun getRegisteredUsers(branch: String? = null, section: String? = null): List<UserEntity> {
        val response = withContext(Dispatchers.IO) {
            try { ApiService.getStudents(branch, section) } catch (e: Exception) { null }
        }
        
        android.util.Log.d("REPO_DEBUG", "Raw Response: $response")
        
        val list = mutableListOf<UserEntity>()
        if (response != null) {
            // Flask returns a plain JSON array from /api/students
            for (i in 0 until response.length()) {
                val u = response.getJSONObject(i)
                val uBranch = u.optString("branch", "")
                val uSection = u.optString("section", "")
                
                list.add(UserEntity(
                    id = u.optString("id", java.util.UUID.randomUUID().toString()),
                    name = u.getString("name"),
                    password = u.optString("password", "N/A"),
                    role = u.optString("role", "student"),
                    course = u.optString("course", ""),
                    year = u.optString("year", ""),
                    branch = uBranch,
                    section = uSection,
                    subject = u.optString("subject", ""),
                    synced = true
                ))
            }
        }
        return list
    }

    suspend fun getAllLocalUsers(): List<UserEntity> {
        return userDao.getAllUsers()
    }

    suspend fun deleteUser(user: UserEntity) {
        userDao.deleteUser(user)
    }

    suspend fun getAttendanceHistory(): List<AttendanceRecordEntity> {
        return attendanceDao.getAllRecords()
    }

    // ── Sessions ─────────────────────────────────────────────
    suspend fun getActiveSession(branch: String, section: String): SessionEntity? {
        // Use the session/live endpoint to find active session for this branch/section
        // First get all sessions, then filter locally
        val response = withContext(Dispatchers.IO) {
            ApiService.getSessions()
        }
        if (response != null) {
            // Flask returns a JSON array directly: [{"id":1,...}, ...]
            for (i in 0 until response.length()) {
                val s = response.getJSONObject(i)
                val sBranch = s.optString("branch", "")
                val sSection = s.optString("section", "")
                val sStatus = s.optString("status", "")
                if (sBranch == branch && sSection == section && (sStatus == "active" || s.optBoolean("active", false))) {
                    return SessionEntity(
                        id = s.optString("id", ""),
                        remoteId = s.optInt("id"),
                        teacherName = s.optString("teacher_name", ""),
                        branch = sBranch,
                        section = sSection,
                        subject = s.optString("subject", ""),
                        startTime = System.currentTimeMillis(),
                        endTime = null,
                        syncStatus = "SYNCED"
                    )
                }
            }
        }
        return null
    }

    suspend fun startSession(teacherName: String, branch: String, section: String, subject: String): SessionEntity {
        val session = SessionEntity(
            teacherName = teacherName,
            branch = branch,
            section = section,
            subject = subject
        )
        sessionDao.insertSession(session)

        // Attempt sync (on IO thread)
        val response = withContext(Dispatchers.IO) {
            ApiService.startSession(teacherName, branch, section, subject)
        }
        if (response != null && response.has("session_id")) {
            val updated = session.copy(
                remoteId = response.getInt("session_id"),
                syncStatus = "SYNCED"
            )
            sessionDao.updateSession(updated)
            return updated
        }
        
        return session
    }

    suspend fun endSession(session: SessionEntity) {
        val updated = session.copy(endTime = System.currentTimeMillis())
        sessionDao.updateSession(updated)

        if (session.remoteId != null) {
            withContext(Dispatchers.IO) {
                ApiService.endSession(session.remoteId)
            }
        }
    }

    // ── Attendance ───────────────────────────────────────────
    suspend fun markAttendance(studentName: String, branch: String, section: String, mode: String = "Auto", sessionId: String? = null): Result<AttendanceRecordEntity> {
        val record = AttendanceRecordEntity(
            sessionId = sessionId ?: "auto",
            studentName = studentName,
            branch = branch,
            section = section,
            mode = mode
        )
        attendanceDao.insertRecord(record)

        // Try remote (on IO thread) calling exact POST /api/mark endpoint
        val response = withContext(Dispatchers.IO) {
            ApiService.markAttendance(studentName, branch, section, mode)
        }
        if (response != null && response.has("status")) {
            val status = response.getString("status")
            if (status == "marked" || status == "already_marked") {
                val remoteSessionId = response.optInt("session_id", 0).toString()
                val syncedRecord = record.copy(
                    sessionId = if (remoteSessionId != "0") remoteSessionId else record.sessionId,
                    syncStatus = "SYNCED"
                )
                attendanceDao.updateRecord(syncedRecord)
                return Result.success(syncedRecord)
            }
        }

        return Result.success(record)
    }

    // ── Sync ─────────────────────────────────────────────────
    suspend fun syncPendingData(): SyncResult {
        var syncedCount = 0
        var failedCount = 0

        // 1. Sync Users
        val unsyncedUsers = userDao.getUnsyncedUsers()
        for (user in unsyncedUsers) {
            val res = withContext(Dispatchers.IO) {
                ApiService.register(
                    user.name, user.password, user.role, 
                    user.course, user.year, user.branch, user.section, user.subject
                )
            }
            if (res != null && res.has("user")) {
                userDao.markSynced(user.id)
                syncedCount++
            } else {
                failedCount++
            }
        }

        // 2. Sync Sessions
        val unsyncedSessions = sessionDao.getUnsyncedSessions()
        for (session in unsyncedSessions) {
            val res = withContext(Dispatchers.IO) {
                ApiService.startSession(session.teacherName, session.branch, session.section, session.subject)
            }
            if (res != null && res.has("session_id")) {
                val remoteId = res.getInt("session_id")
                sessionDao.updateSession(session.copy(remoteId = remoteId, syncStatus = "SYNCED"))
                syncedCount++
                
                // If session ended locally, sync endSession too
                if (session.endTime != null) {
                    withContext(Dispatchers.IO) {
                        ApiService.endSession(remoteId)
                    }
                }
            } else {
                failedCount++
            }
        }

        // 3. Sync Attendance Records
        val unsyncedRecords = attendanceDao.getUnsyncedRecords()
        for (record in unsyncedRecords) {
            val res = withContext(Dispatchers.IO) {
                ApiService.markAttendance(record.studentName, record.branch, record.section, record.mode)
            }
            if (res != null && res.has("status")) {
                attendanceDao.updateRecord(record.copy(syncStatus = "SYNCED"))
                syncedCount++
            } else {
                failedCount++
            }
        }

        return SyncResult(syncedCount, failedCount)
    }
}

data class SyncResult(val synced: Int, val failed: Int)
