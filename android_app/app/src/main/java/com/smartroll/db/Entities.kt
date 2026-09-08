package com.smartroll.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val password: String,
    val role: String,
    val course: String?,
    val year: String?,
    val branch: String?,
    val section: String?,
    val subject: String?,
    val synced: Boolean = false
)

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val remoteId: Int? = null,
    val teacherName: String,
    val branch: String,
    val section: String,
    val subject: String,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val syncStatus: String = "PENDING" // PENDING, SYNCED, FAILED
)

@Entity(tableName = "attendance_records")
data class AttendanceRecordEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val remoteId: Int? = null,
    val sessionId: String, // local session id
    val studentName: String,
    val branch: String,
    val section: String,
    val mode: String,
    val timestamp: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING" // PENDING, SYNCED, FAILED
)
