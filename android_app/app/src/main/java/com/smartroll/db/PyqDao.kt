package com.smartroll.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PyqDao {
    @Query("SELECT * FROM pyqs ORDER BY createdAt DESC")
    fun getAllPyqs(): Flow<List<PyqEntity>>

    @Query("SELECT * FROM pyqs ORDER BY createdAt DESC")
    suspend fun getAllPyqsList(): List<PyqEntity>

    @Query("SELECT * FROM pyqs WHERE teacherName = :teacherName ORDER BY createdAt DESC")
    fun getPyqsByTeacher(teacherName: String): Flow<List<PyqEntity>>

    @Query("SELECT * FROM pyqs WHERE id = :id")
    suspend fun getPyqById(id: Long): PyqEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPyq(pyq: PyqEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPyqs(pyqs: List<PyqEntity>)

    @Update
    suspend fun updatePyq(pyq: PyqEntity)

    @Delete
    suspend fun deletePyq(pyq: PyqEntity)
}
