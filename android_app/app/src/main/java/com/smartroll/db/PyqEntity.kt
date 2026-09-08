package com.smartroll.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * SmartRoll — PYQ (Previous Year Questions) Entity
 */
@Entity(tableName = "pyqs")
data class PyqEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val teacherName: String,
    val branch: String,
    val subject: String,
    val title: String,
    val semester: String? = null,
    val year: String? = null,
    val examType: String? = null,
    val content: String? = null,
    val filePath: String? = null,
    val fileName: String? = null,
    val driveLink: String? = null,
    val createdAt: String = ""
)

