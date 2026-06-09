package com.example.unilifeplanner.university.refresh

import android.content.Context
import com.example.unilifeplanner.data.datastore.SavedUniboImportSelection
import com.example.unilifeplanner.data.datastore.UniboImportDataStore
import com.example.unilifeplanner.data.local.AppDatabase
import com.example.unilifeplanner.data.local.ExamAppealSource
import com.example.unilifeplanner.data.repository.CourseRepository
import com.example.unilifeplanner.data.repository.ExamAppealRepository
import com.example.unilifeplanner.data.repository.LessonRepository
import com.example.unilifeplanner.notifications.ExamReminderScheduler
import com.example.unilifeplanner.notifications.LessonReminderScheduler
import com.example.unilifeplanner.university.publicimport.PublicCurriculum
import com.example.unilifeplanner.university.publicimport.PublicDegreeProgram
import com.example.unilifeplanner.university.publicimport.unibo.UniboPublicImportRepository
import com.example.unilifeplanner.university.publicimport.unibo.UniboPublicImporter
import com.example.unilifeplanner.university.unibo.publicdata.UniboPublicConfig
import kotlinx.coroutines.flow.first

data class UniboRefreshResult(
    val refreshed: Boolean,
    val noSelection: Boolean = false,
    val importedTeachings: Int = 0,
    val updatedTeachings: Int = 0,
    val importedLessons: Int = 0,
    val updatedLessons: Int = 0,
    val importedExamAppeals: Int = 0,
    val updatedExamAppeals: Int = 0,
    val removedStaleLessons: Int = 0,
    val removedStaleExamAppeals: Int = 0,
    val warnings: List<String> = emptyList(),
    val message: String
)

enum class UniboRefreshSource {
    MANUAL,
    APP_OPEN
}

class UniboRefreshManager(
    context: Context
) {
    private val appContext = context.applicationContext
    private val database = AppDatabase.getDatabase(appContext)
    private val courseRepository = CourseRepository(database.courseDao())
    private val lessonRepository = LessonRepository(database.lessonDao())
    private val examAppealRepository = ExamAppealRepository(database.examAppealDao())
    private val lessonReminderScheduler = LessonReminderScheduler(appContext)
    private val examReminderScheduler = ExamReminderScheduler(appContext)
    private val dataStore = UniboImportDataStore(appContext)
    private val importRepository = UniboPublicImportRepository(
        importer = UniboPublicImporter(
            courseRepository = courseRepository,
            lessonRepository = lessonRepository,
            lessonReminderScheduler = lessonReminderScheduler,
            examAppealRepository = examAppealRepository,
            examReminderScheduler = examReminderScheduler
        )
    )

    suspend fun refreshImportedUniboData(
        source: UniboRefreshSource,
        force: Boolean = true
    ): UniboRefreshResult {
        val selection = dataStore.savedSelectionFlow.first()
            ?: return UniboRefreshResult(
                refreshed = false,
                noSelection = true,
                message = "Prima importa i dati da Universita."
            )

        val preview = importRepository.loadPreview(
            degreeProgram = selection.toDegreeProgram(),
            curriculum = selection.toCurriculum(),
            forceRefresh = force
        )
        val importResult = importRepository.importPreview(preview)
        val cleanup = removeStaleImportedItems(
            previewTeachingExternalIds = preview.teachings.map { it.externalId }.toSet(),
            lessonExternalIds = preview.lessons.map { it.externalId }.toSet(),
            examExternalIds = preview.examAppeals.map { it.externalId }.toSet(),
            allowExamCleanup = preview.warnings.none {
                it.contains("Appelli pubblici non disponibili", ignoreCase = true)
            }
        )

        when (source) {
            UniboRefreshSource.MANUAL -> dataStore.updateManualRefreshTimestamp()
            UniboRefreshSource.APP_OPEN -> dataStore.updateAutoRefreshTimestamp()
        }

        return UniboRefreshResult(
            refreshed = true,
            importedTeachings = importResult.importedTeachings,
            updatedTeachings = importResult.updatedTeachings,
            importedLessons = importResult.importedLessons,
            updatedLessons = importResult.updatedLessons,
            importedExamAppeals = importResult.importedExamAppeals,
            updatedExamAppeals = importResult.updatedExamAppeals,
            removedStaleLessons = cleanup.removedLessons,
            removedStaleExamAppeals = cleanup.removedExamAppeals,
            warnings = importResult.warnings,
            message = buildRefreshMessage(importResult.updatedTeachings, importResult.updatedLessons, importResult.updatedExamAppeals)
        )
    }

    private suspend fun removeStaleImportedItems(
        previewTeachingExternalIds: Set<String>,
        lessonExternalIds: Set<String>,
        examExternalIds: Set<String>,
        allowExamCleanup: Boolean
    ): CleanupResult {
        val courseIds = previewTeachingExternalIds.mapNotNull { externalId ->
            courseRepository.getCourseByExternalId(UniboPublicConfig.PROVIDER, externalId)?.id
        }
        if (courseIds.isEmpty()) return CleanupResult()

        val staleLessons = lessonRepository
            .getImportedLessonsForCourseIds(UniboPublicConfig.PROVIDER, courseIds)
            .filter { lesson -> lesson.externalId !in lessonExternalIds }
        staleLessons.forEach { lesson -> lessonReminderScheduler.cancelLessonReminder(lesson.id) }
        lessonRepository.deleteLessonsByIds(staleLessons.map { it.id })

        val staleExamAppeals = if (allowExamCleanup) {
            examAppealRepository
                .getImportedExamAppealsForCourseIds(ExamAppealSource.UNIBO.name, courseIds)
                .filter { exam -> exam.externalId !in examExternalIds }
        } else {
            emptyList()
        }
        staleExamAppeals.forEach { exam ->
            examReminderScheduler.cancelExamAppealReminders(exam.id)
        }
        examAppealRepository.deleteExamAppealsByIds(staleExamAppeals.map { it.id })

        return CleanupResult(
            removedLessons = staleLessons.size,
            removedExamAppeals = staleExamAppeals.size
        )
    }

    private fun SavedUniboImportSelection.toDegreeProgram(): PublicDegreeProgram =
        PublicDegreeProgram(
            externalId = degreeProgramExternalId,
            name = degreeProgramName,
            campus = degreeProgramCampus,
            degreeType = degreeProgramType,
            academicYear = academicYear,
            officialUrl = degreeProgramOfficialUrl
        )

    private fun SavedUniboImportSelection.toCurriculum(): PublicCurriculum? {
        val externalId = curriculumExternalId ?: return null
        val name = curriculumName ?: return null
        val officialUrl = curriculumOfficialUrl ?: return null
        return PublicCurriculum(
            externalId = externalId,
            name = name,
            academicYear = academicYear,
            degreeProgramExternalId = degreeProgramExternalId,
            officialUrl = officialUrl
        )
    }

    private fun buildRefreshMessage(
        updatedTeachings: Int,
        updatedLessons: Int,
        updatedExamAppeals: Int
    ): String {
        return "Aggiornamento completato: $updatedTeachings corsi aggiornati, " +
            "$updatedLessons lezioni aggiornate, $updatedExamAppeals appelli aggiornati."
    }

    private data class CleanupResult(
        val removedLessons: Int = 0,
        val removedExamAppeals: Int = 0
    )
}
