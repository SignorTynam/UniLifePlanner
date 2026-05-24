package com.example.unilifeplanner.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.unilifeplanner.domain.lessons.nextLessonDateMillis

@Database(
    entities = [
        CourseEntity::class,
        LessonEntity::class
    ],
    version = 7,
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
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7
                    )
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

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE courses ADD COLUMN externalId TEXT")
                db.execSQL("ALTER TABLE courses ADD COLUMN sourceProvider TEXT")
                db.execSQL("ALTER TABLE courses ADD COLUMN officialUrl TEXT")
                db.execSQL("ALTER TABLE courses ADD COLUMN classroom TEXT")
                db.execSQL("ALTER TABLE lessons ADD COLUMN externalId TEXT")
                db.execSQL("ALTER TABLE lessons ADD COLUMN sourceProvider TEXT")
                db.execSQL("ALTER TABLE lessons ADD COLUMN officialUrl TEXT")
                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_courses_sourceProvider_externalId
                    ON courses(sourceProvider, externalId)
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_lessons_sourceProvider_externalId
                    ON lessons(sourceProvider, externalId)
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.addColumnIfMissing("courses", "externalId", "externalId TEXT")
                db.addColumnIfMissing("courses", "sourceProvider", "sourceProvider TEXT")
                db.addColumnIfMissing("courses", "officialUrl", "officialUrl TEXT")
                db.addColumnIfMissing("courses", "classroom", "classroom TEXT")
                db.addColumnIfMissing("lessons", "externalId", "externalId TEXT")
                db.addColumnIfMissing("lessons", "sourceProvider", "sourceProvider TEXT")
                db.addColumnIfMissing("lessons", "officialUrl", "officialUrl TEXT")
                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_courses_sourceProvider_externalId
                    ON courses(sourceProvider, externalId)
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_lessons_sourceProvider_externalId
                    ON lessons(sourceProvider, externalId)
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.addColumnIfMissing("lessons", "dateMillis", "dateMillis INTEGER")
                backfillLessonDates(db)
                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_lessons_dateMillis
                    ON lessons(dateMillis)
                    """.trimIndent()
                )
            }
        }

        private fun backfillLessonDates(db: SupportSQLiteDatabase) {
            val nowMillis = System.currentTimeMillis()
            db.query(
                """
                SELECT id, dayOfWeek, startTimeMinutes
                FROM lessons
                WHERE dateMillis IS NULL
                """.trimIndent()
            ).use { cursor ->
                val idIndex = cursor.getColumnIndex("id")
                val dayOfWeekIndex = cursor.getColumnIndex("dayOfWeek")
                val startTimeIndex = cursor.getColumnIndex("startTimeMinutes")
                while (cursor.moveToNext()) {
                    val lessonId = cursor.getInt(idIndex)
                    val dayOfWeek = cursor.getInt(dayOfWeekIndex)
                    val startTimeMinutes = cursor.getInt(startTimeIndex)
                    if (dayOfWeek !in 1..7 || startTimeMinutes !in 0..1439) continue

                    val dateMillis = nextLessonDateMillis(
                        dayOfWeek = dayOfWeek,
                        startTimeMinutes = startTimeMinutes,
                        nowMillis = nowMillis
                    )
                    db.execSQL(
                        "UPDATE lessons SET dateMillis = ? WHERE id = ?",
                        arrayOf(dateMillis, lessonId)
                    )
                }
            }
        }

        private fun SupportSQLiteDatabase.addColumnIfMissing(
            tableName: String,
            columnName: String,
            columnDefinition: String
        ) {
            val exists = query("PRAGMA table_info($tableName)").use { cursor ->
                val nameIndex = cursor.getColumnIndex("name")
                var found = false
                while (!found && cursor.moveToNext()) {
                    found = cursor.getString(nameIndex) == columnName
                }
                found
            }
            if (!exists) {
                execSQL("ALTER TABLE $tableName ADD COLUMN $columnDefinition")
            }
        }
    }
}
