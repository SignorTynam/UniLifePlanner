package com.example.unilifeplanner.data.local

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class ExamAppealWithCourse(
    @Embedded val exam: ExamAppealEntity,
    @ColumnInfo(name = "courseName") val courseName: String,
    @ColumnInfo(name = "courseProfessor") val courseProfessor: String
)
