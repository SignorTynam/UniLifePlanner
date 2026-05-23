package com.example.unilifeplanner.data.local

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class LessonWithCourse(
    @Embedded val lesson: LessonEntity,
    @ColumnInfo(name = "courseName") val courseName: String,
    @ColumnInfo(name = "courseProfessor") val courseProfessor: String
)
