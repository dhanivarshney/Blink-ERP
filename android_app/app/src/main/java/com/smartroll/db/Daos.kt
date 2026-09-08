package com.smartroll.db

import androidx.room.*

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE name = :name AND password = :password AND role = :role LIMIT 1")
    suspend fun login(name: String, password: String, role: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE name = :name LIMIT 1")
    suspend fun getUserByName(name: String): UserEntity?

    @Query("SELECT * FROM users WHERE synced = 0")
    suspend fun getUnsyncedUsers(): List<UserEntity>

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>

    @Query("DELETE FROM users WHERE name = :name")
    suspend fun deleteUserByName(name: String)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("UPDATE users SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)
}

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getSessionById(id: String): SessionEntity?

    @Query("SELECT * FROM sessions WHERE syncStatus = 'PENDING'")
    suspend fun getUnsyncedSessions(): List<SessionEntity>

    @Query("SELECT * FROM sessions ORDER BY startTime DESC")
    suspend fun getAllSessions(): List<SessionEntity>
}

@Dao
interface AttendanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AttendanceRecordEntity)

    @Query("SELECT * FROM attendance_records WHERE studentName = :studentName AND sessionId = :sessionId LIMIT 1")
    suspend fun getRecord(studentName: String, sessionId: String): AttendanceRecordEntity?

    @Query("SELECT * FROM attendance_records WHERE syncStatus = 'PENDING'")
    suspend fun getUnsyncedRecords(): List<AttendanceRecordEntity>

    @Update
    suspend fun updateRecord(record: AttendanceRecordEntity)
    
    @Query("SELECT * FROM attendance_records ORDER BY timestamp DESC")
    suspend fun getAllRecords(): List<AttendanceRecordEntity>
}
