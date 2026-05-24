package com.example.unilifeplanner.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exam_appeals",
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
        Index(value = ["dateMillis"]),
        Index(
            value = ["source", "externalId"],
            name = "index_exam_appeals_source_externalId"
        )
    ]
)
data class ExamAppealEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val courseId: Int,
    val dateMillis: Long,
    val timeMinutes: Int? = null,
    val location: String? = null,
    val notes: String? = null,
    val type: String? = null,
    val reminderEnabled: Boolean = false,
    val reminderDateTimeMillis: Long? = null,
    val source: String = ExamAppealSource.MANUAL.name,
    val externalId: String? = null,
    val officialUrl: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)

enum class ExamAppealSource {
    MANUAL,
    UNIBO
}
