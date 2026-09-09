package com.smartroll.api

import android.content.Context
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object ApiService {

    private var _serverUrl: String? = null
    const val DEFAULT_IP = "192.168.194.186"
    val serverUrl: String
        get() = _serverUrl ?: "http://$DEFAULT_IP:5000"

    fun updateUrl(context: Context, newIp: String) {
        // Robust cleanup: strip "http://"/"https://", strip any trailing
        // slash, and strip an existing ":port" if the user already typed
        // one — THEN always append ":5000" ourselves. This avoids the
        // "192.168.1.11:5000:5000" double-port bug that happened when the
        // user typed the port themselves (matching the field's example hint).
        var cleanIp = newIp.trim()
            .removePrefix("http://")
            .removePrefix("https://")
            .trimEnd('/')

        // If it already has a ":<port>" suffix, strip it off before we add our own.
        val colonIndex = cleanIp.lastIndexOf(':')
        if (colonIndex != -1) {
            val afterColon = cleanIp.substring(colonIndex + 1)
            if (afterColon.toIntOrNull() != null) {
                cleanIp = cleanIp.substring(0, colonIndex)
            }
        }

        _serverUrl = "http://$cleanIp:5000"

        context.getSharedPreferences("smartroll_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("server_url", _serverUrl)
            .apply()
    }

    fun init(context: Context) {
        val savedUrl = context.getSharedPreferences("smartroll_prefs", Context.MODE_PRIVATE)
            .getString("server_url", null)
        
        // If never set, or still set to emulator loopback 10.0.2.2, override with real laptop IP
        if (savedUrl == null || savedUrl.contains("10.0.2.2")) {
            _serverUrl = "http://$DEFAULT_IP:5000"
            context.getSharedPreferences("smartroll_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString("server_url", _serverUrl)
                .apply()
        } else {
            _serverUrl = savedUrl
        }
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val JSON = "application/json; charset=utf-8".toMediaType()

    fun healthCheck(): JSONObject? = get("/api/health")

    fun register(name: String, password: String, role: String,
                 course: String? = null, year: String? = null, branch: String? = null, section: String? = null, subject: String? = null): JSONObject? {
        val body = JSONObject().apply {
            put("name", name)
            put("password", password)
            put("role", role)
            course?.let { put("course", it) }
            year?.let { put("year", it) }
            branch?.let { put("branch", it) }
            section?.let { put("section", it) }
            subject?.let { put("subject", it) }
        }
        return post("/api/register", body)
    }

    fun login(name: String, password: String, role: String): JSONObject? {
        val body = JSONObject().apply {
            put("name", name)
            put("password", password)
            put("role", role)
        }
        return post("/api/login", body)
    }

    fun startSession(teacherName: String, branch: String, section: String, subject: String): JSONObject? {
        val body = JSONObject().apply {
            put("teacher_name", teacherName)
            put("branch", branch)
            put("section", section)
            put("subject", subject)
        }
        return post("/api/start_session", body)
    }

    fun endSession(sessionId: Int): JSONObject? {
        val body = JSONObject().put("session_id", sessionId)
        return post("/api/end_session", body)
    }

    fun markAttendance(studentName: String, branch: String, section: String, mode: String = "Auto"): JSONObject? {
        val body = JSONObject().apply {
            put("student_name", studentName)
            put("branch", branch)
            put("section", section)
            put("mode", mode)
        }
        return post("/api/mark", body)
    }

    fun getStudents(branch: String? = null, section: String? = null): org.json.JSONArray? {
        var path = "/api/students"
        val params = mutableListOf<String>()
        branch?.let { params.add("branch=$it") }
        section?.let { params.add("section=$it") }
        if (params.isNotEmpty()) path += "?" + params.joinToString("&")
        return getArray(path)
    }

    fun getSessions(): org.json.JSONArray? = getArray("/api/sessions")

    fun getStats(): JSONObject? = get("/api/stats")

    fun getUsers(): org.json.JSONArray? = getArray("/api/admin/users")

    fun getNotes(teacher: String? = null, branch: String? = null, section: String? = null): JSONObject? {
        var path = "/api/notes"
        val params = mutableListOf<String>()
        teacher?.let { params.add("teacher=$it") }
        branch?.let { params.add("branch=$it") }
        section?.let { params.add("section=$it") }
        if (params.isNotEmpty()) path += "?" + params.joinToString("&")
        return get(path)
    }

    fun addNote(teacherName: String, branch: String, section: String, subject: String, title: String, content: String): JSONObject? {
        val body = JSONObject().apply {
            put("teacher_name", teacherName)
            put("branch", branch)
            put("section", section)
            put("subject", subject)
            put("title", title)
            put("content", content)
        }
        return post("/api/notes", body)
    }

    fun deleteNote(noteId: Int): JSONObject? {
        return try {
            val request = Request.Builder()
                .url("$serverUrl/api/notes/$noteId")
                .delete()
                .build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            if (response.isSuccessful && responseBody != null) {
                JSONObject(responseBody)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getPyqs(teacherName: String? = null): org.json.JSONArray? {
        var path = "/api/pyqs"
        val params = mutableListOf<String>()
        teacherName?.let { params.add("teacher=$it") }
        if (params.isNotEmpty()) path += "?" + params.joinToString("&")
        return getArray(path)
    }

    fun getStudentAnalytics(name: String, branch: String? = null, section: String? = null): JSONObject? {
        return try {
            var path = "/api/student/analytics?name=" + java.net.URLEncoder.encode(name, "UTF-8")
            branch?.let { path += "&branch=" + java.net.URLEncoder.encode(it, "UTF-8") }
            section?.let { path += "&section=" + java.net.URLEncoder.encode(it, "UTF-8") }
            get(path)
        } catch (e: Exception) {
            null
        }
    }

    fun updateUserPassword(userId: Int, newPassword: String): JSONObject? {
        val body = JSONObject().apply {
            put("new_password", newPassword)
        }
        return post("/api/admin/users/$userId/password", body)
    }

    fun adminCreateUser(name: String, password: String, role: String,
                        branch: String? = null, section: String? = null, subject: String? = null): JSONObject? {
        val body = JSONObject().apply {
            put("name", name)
            put("password", password)
            put("role", role)
            branch?.let { put("branch", it) }
            section?.let { put("section", it) }
            subject?.let { put("subject", it) }
        }
        return post("/api/admin/users", body)
    }



    private fun post(path: String, body: JSONObject): JSONObject? {
        return try {
            val request = Request.Builder()
                .url("$serverUrl$path")
                .post(body.toString().toRequestBody(JSON))
                .build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            if (response.isSuccessful && responseBody != null) {
                JSONObject(responseBody)
            } else {
                JSONObject().apply {
                    put("error", response.message ?: "Request failed")
                    put("code", response.code)
                }
            }
        } catch (e: Exception) {
            JSONObject().apply { put("error", e.message ?: "Connection failed") }
        }
    }

    private fun get(path: String): JSONObject? {
        return try {
            val request = Request.Builder()
                .url("$serverUrl$path")
                .get()
                .build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            if (response.isSuccessful && responseBody != null) {
                JSONObject(responseBody)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun getArray(path: String): org.json.JSONArray? {
        return try {
            val request = Request.Builder()
                .url("$serverUrl$path")
                .get()
                .build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            if (response.isSuccessful && responseBody != null) {
                org.json.JSONArray(responseBody)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}