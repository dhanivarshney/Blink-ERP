package com.smartroll.api

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object ApiService {

    private var _serverUrl: String? = null
    const val DEFAULT_URL = "https://actors-usgs-prairie-entertaining.trycloudflare.com"
    val serverUrl: String
        get() = _serverUrl ?: DEFAULT_URL

    fun updateUrl(context: Context, newIp: String) {
        val trimmed = newIp.trim()
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            _serverUrl = trimmed.trimEnd('/')
        } else if (trimmed.contains(".loca.lt") || trimmed.contains(".ngrok") || trimmed.contains(".trycloudflare.com")) {
            _serverUrl = "https://" + trimmed.trimEnd('/')
        } else {
            var cleanIp = trimmed
                .removePrefix("http://")
                .removePrefix("https://")
                .trimEnd('/')

            val colonIndex = cleanIp.lastIndexOf(':')
            if (colonIndex != -1) {
                val afterColon = cleanIp.substring(colonIndex + 1)
                if (afterColon.toIntOrNull() != null) {
                    cleanIp = cleanIp.substring(0, colonIndex)
                }
            }

            _serverUrl = "http://$cleanIp:5000"
        }

        context.getSharedPreferences("smartroll_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("server_url", _serverUrl)
            .apply()
    }

    fun init(context: Context) {
        val savedUrl = context.getSharedPreferences("smartroll_prefs", Context.MODE_PRIVATE)
            .getString("server_url", null)
        
        // Auto-upgrade old / invalid URLs to current Cloudflare URL
        if (savedUrl == null || savedUrl.contains("10.0.2.2") || savedUrl.contains("192.168.") || savedUrl.contains(".loca.lt")) {
            _serverUrl = DEFAULT_URL
            context.getSharedPreferences("smartroll_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString("server_url", _serverUrl)
                .apply()
        } else {
            _serverUrl = savedUrl
        }
    }

    fun testConnection(targetUrl: String? = null): Pair<Boolean, String> {
        val url = (targetUrl ?: serverUrl).trimEnd('/') + "/api/health"
        return try {
            val req = Request.Builder()
                .url(url)
                .addHeader("bypass-tunnel-reminder", "true")
                .get()
                .build()
            val resp = client.newCall(req).execute()
            if (resp.isSuccessful) {
                Pair(true, "Connected to server (HTTP ${resp.code})")
            } else {
                Pair(false, "Server responded with HTTP ${resp.code}")
            }
        } catch (e: Exception) {
            Pair(false, e.message ?: "Connection failed")
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

    fun uploadNoteFile(
        context: Context,
        teacherName: String,
        branch: String,
        section: String,
        subject: String,
        title: String,
        content: String,
        fileUri: Uri
    ): JSONObject? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(fileUri) ?: return null
            val fileBytes = inputStream.readBytes()
            inputStream.close()

            var fileName = "note_${System.currentTimeMillis()}.pdf"
            if (fileUri.scheme == "content") {
                val cursor = contentResolver.query(fileUri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (idx != -1) {
                            val name = it.getString(idx)
                            if (!name.isNullOrBlank()) fileName = name
                        }
                    }
                }
            } else if (!fileUri.lastPathSegment.isNullOrBlank()) {
                fileName = fileUri.lastPathSegment!!
            }

            val mediaType = "application/pdf".toMediaTypeOrNull()
            val fileBody = fileBytes.toRequestBody(mediaType)

            val multipartBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("teacher_name", teacherName)
                .addFormDataPart("branch", branch)
                .addFormDataPart("section", section)
                .addFormDataPart("subject", subject)
                .addFormDataPart("title", title)
                .addFormDataPart("content", content)
                .addFormDataPart("file", fileName, fileBody)
                .build()

            val request = Request.Builder()
                .url("$serverUrl/api/notes/upload")
                .addHeader("bypass-tunnel-reminder", "true")
                .post(multipartBody)
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

    fun getTeacherAnalytics(teacher: String? = null, branch: String? = null, section: String? = null): JSONObject? {
        return try {
            var path = "/api/teacher/analytics?"
            val params = mutableListOf<String>()
            teacher?.let { params.add("teacher=" + java.net.URLEncoder.encode(it, "UTF-8")) }
            branch?.let { params.add("branch=" + java.net.URLEncoder.encode(it, "UTF-8")) }
            section?.let { params.add("section=" + java.net.URLEncoder.encode(it, "UTF-8")) }
            path += params.joinToString("&")
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



    fun getLiveSession(sessionId: Int): JSONObject? = get("/api/session/$sessionId/live")

    private fun post(path: String, body: JSONObject): JSONObject? {
        return try {
            val request = Request.Builder()
                .url("$serverUrl$path")
                .addHeader("bypass-tunnel-reminder", "true")
                .post(body.toString().toRequestBody(JSON))
                .build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            if (response.isSuccessful && responseBody != null) {
                JSONObject(responseBody)
            } else if (responseBody != null && responseBody.trim().startsWith("{")) {
                try {
                    JSONObject(responseBody).apply { put("code", response.code) }
                } catch (e: Exception) {
                    JSONObject().apply {
                        put("error", response.message.ifEmpty { "Request failed (HTTP ${response.code})" })
                        put("code", response.code)
                    }
                }
            } else {
                JSONObject().apply {
                    put("error", response.message.ifEmpty { "Request failed (HTTP ${response.code})" })
                    put("code", response.code)
                }
            }
        } catch (e: Exception) {
            JSONObject().apply { put("error", "Cannot connect to $serverUrl: ${e.message}") }
        }
    }

    private fun get(path: String): JSONObject? {
        return try {
            val request = Request.Builder()
                .url("$serverUrl$path")
                .addHeader("bypass-tunnel-reminder", "true")
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
                .addHeader("bypass-tunnel-reminder", "true")
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