package com.example.unilifeplanner.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Home : Screen("home")
    data object Courses : Screen("courses")
    data object Profile : Screen("profile")
    data object Settings : Screen("settings")
    data object Map : Screen("map")
    data object Statistics : Screen("statistics")

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
}
