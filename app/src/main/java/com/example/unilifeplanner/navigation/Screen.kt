package com.example.unilifeplanner.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Home : Screen("home")
    data object Courses : Screen("courses")
    data object Exams : Screen("exams?courseId={courseId}") {
        const val ARG_COURSE_ID = "courseId"

        fun createRoute(courseId: Int? = null): String =
            courseId?.let { "exams?courseId=$it" } ?: "exams"
    }

    data object Profile : Screen("profile")
    data object Settings : Screen("settings")
    data object Map : Screen("map")
    data object Statistics : Screen("statistics")
    data object University : Screen("university")
    data object PublicUniboImport : Screen("public_unibo_import")

    data object Lessons : Screen("lessons?courseId={courseId}") {
        const val ARG_COURSE_ID = "courseId"

        fun createRoute(courseId: Int? = null): String =
            courseId?.let { "lessons?courseId=$it" } ?: "lessons"
    }

    data object CourseDetail : Screen("course_detail/{courseId}") {
        const val ARG_COURSE_ID = "courseId"

        fun createRoute(courseId: Int): String = "course_detail/$courseId"
    }

    data object AddEditCourse : Screen("add_edit_course") {
        const val ARG_COURSE_ID = "courseId"
        const val routeWithCourseId = "add_edit_course/{courseId}"

        fun createRoute(courseId: Int? = null): String =
            courseId?.let { "add_edit_course/$it" } ?: route
    }

    data object AddEditLesson : Screen("add_edit_lesson/{courseId}") {
        const val ARG_COURSE_ID = "courseId"
        const val ARG_LESSON_ID = "lessonId"

        const val routeWithLessonId = "add_edit_lesson/{courseId}/{lessonId}"

        fun createRoute(courseId: Int, lessonId: Int? = null): String =
            lessonId?.let { "add_edit_lesson/$courseId/$it" } ?: "add_edit_lesson/$courseId"
    }

    data object AddEditExam : Screen("add_edit_exam?courseId={courseId}&examAppealId={examAppealId}") {
        const val ARG_COURSE_ID = "courseId"
        const val ARG_EXAM_APPEAL_ID = "examAppealId"

        fun createRoute(
            courseId: Int? = null,
            examAppealId: Int? = null
        ): String {
            val query = buildList {
                courseId?.let { add("courseId=$it") }
                examAppealId?.let { add("examAppealId=$it") }
            }.joinToString("&")
            return if (query.isBlank()) {
                "add_edit_exam"
            } else {
                "add_edit_exam?$query"
            }
        }
    }
}
