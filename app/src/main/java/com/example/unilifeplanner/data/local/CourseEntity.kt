package com.example.unilifeplanner.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.unilifeplanner.domain.model.CourseStatus

@Entity(
    tableName = "courses",
    indices = [
        Index(
            value = ["sourceProvider", "externalId"],
            name = "index_courses_sourceProvider_externalId"
        )
    ]
)
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
    val classroom: String? = null,
    val notes: String? = null,
    val externalId: String? = null,
    val sourceProvider: String? = null,
    val officialUrl: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
