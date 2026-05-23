package com.example.unilifeplanner.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        CourseEntity::class,
        LessonEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun lessonDao(): LessonDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "unilife_planner_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()

                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE courses ADD COLUMN reminderEnabled INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS lessons (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        courseId INTEGER NOT NULL,
                        dayOfWeek INTEGER NOT NULL,
                        startTimeMinutes INTEGER NOT NULL,
                        endTimeMinutes INTEGER NOT NULL,
                        classroom TEXT,
                        building TEXT,
                        notes TEXT,
                        reminderEnabled INTEGER NOT NULL DEFAULT 1,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        FOREIGN KEY(courseId) REFERENCES courses(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_lessons_courseId ON lessons(courseId)"
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE courses_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        professor TEXT NOT NULL,
                        examDate INTEGER,
                        credits INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        isFavorite INTEGER NOT NULL,
                        reminderEnabled INTEGER NOT NULL,
                        notes TEXT,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    INSERT INTO courses_new (
                        id,
                        name,
                        professor,
                        examDate,
                        credits,
                        status,
                        isFavorite,
                        reminderEnabled,
                        notes,
                        createdAt,
                        updatedAt
                    )
                    SELECT
                        id,
                        name,
                        professor,
                        examDate,
                        credits,
                        status,
                        isFavorite,
                        reminderEnabled,
                        notes,
                        createdAt,
                        updatedAt
                    FROM courses
                    """.trimIndent()
                )

                db.execSQL("DROP TABLE courses")
                db.execSQL("ALTER TABLE courses_new RENAME TO courses")
                db.execSQL("ALTER TABLE lessons ADD COLUMN locationQuery TEXT")
            }
        }
    }
}
