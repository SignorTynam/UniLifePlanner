package com.example.unilifeplanner.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.unilifeplanner.domain.model.CourseStatus

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val professor: String,
    val examDate: Long? = null,
    val credits: Int,
    val status: String = CourseStatus.TO_STUDY.name,
    val isFavorite: Boolean = false,
    val reminderEnabled: Boolean = false,
    val notes: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
