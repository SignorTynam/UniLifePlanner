package com.example.unilifeplanner.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lessons",
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["courseId"]),
        Index(
            value = ["sourceProvider", "externalId"],
            name = "index_lessons_sourceProvider_externalId"
        )
    ]
)
data class LessonEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val courseId: Int,
    val dayOfWeek: Int,
    val startTimeMinutes: Int,
    val endTimeMinutes: Int,
    val classroom: String? = null,
    val building: String? = null,
    val locationQuery: String? = null,
    val notes: String? = null,
    val reminderEnabled: Boolean = true,
    val externalId: String? = null,
    val sourceProvider: String? = null,
    val officialUrl: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
