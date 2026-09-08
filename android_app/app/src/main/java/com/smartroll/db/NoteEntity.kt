package com.smartroll.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val teacherName: String,
    val branch: String,
    val section: String,
    val subject: String,
    val title: String,
    val content: String,
    val pdfPath: String? = null,
    val createdAt: String = ""
)
