package com.example.unilifeplanner.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.unilifeplanner.ui.auth.AuthViewModel
import com.example.unilifeplanner.ui.auth.LoginScreen
import com.example.unilifeplanner.ui.auth.RegisterScreen
import com.example.unilifeplanner.ui.courses.AddEditCourseScreen
import com.example.unilifeplanner.ui.courses.CourseDetailScreen
import com.example.unilifeplanner.ui.courses.CoursesScreen
import com.example.unilifeplanner.ui.exams.AddEditExamScreen
import com.example.unilifeplanner.ui.exams.ExamsScreen
import com.example.unilifeplanner.ui.home.HomeScreen
import com.example.unilifeplanner.ui.lessons.AddEditLessonScreen
import com.example.unilifeplanner.ui.lessons.LessonsScreen
import com.example.unilifeplanner.ui.map.MapScreen
import com.example.unilifeplanner.ui.navigation.AppNavigationDrawer
import com.example.unilifeplanner.ui.profile.ProfileScreen
import com.example.unilifeplanner.ui.settings.SettingsScreen
import com.example.unilifeplanner.ui.statistics.StatisticsScreen
import com.example.unilifeplanner.ui.university.PublicUniboImportScreen
import com.example.unilifeplanner.ui.university.UniversityScreen
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel(),
    initialCourseId: Int? = null
) {
    var pendingCourseId by rememberSaveable { mutableStateOf(initialCourseId) }
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val startDestination = if (authUiState.isAuthenticated) {
        Screen.Home.route
    } else {
        Screen.Login.route
    }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val topLevelRoutes = setOf(
        Screen.Home.route,
        Screen.Courses.route,
        Screen.Exams.route,
        Screen.Exams.createRoute(),
        Screen.Lessons.route,
        Screen.Lessons.createRoute(),
        Screen.Statistics.route,
        Screen.University.route,
        Screen.PublicUniboImport.route,
        Screen.Map.route,
        Screen.Profile.route,
        Screen.Settings.route
    )
    val isTopLevelRoute = currentRoute in topLevelRoutes ||
        isLessonsRoute(currentRoute) ||
        isExamsRoute(currentRoute)
    val onOpenDrawer: () -> Unit = {
        coroutineScope.launch {
            drawerState.open()
        }
    }
    val navigateToTopLevel: (String, Boolean) -> Unit = { route, restoreState ->
        coroutineScope.launch {
            drawerState.close()
            navController.navigate(route) {
                popUpTo(Screen.Home.route) {
                    saveState = true
                }
                launchSingleTop = true
                this.restoreState = restoreState
            }
        }
    }
    val logoutFromDrawer: () -> Unit = {
        coroutineScope.launch {
            drawerState.close()
            authViewModel.logout()
            navController.navigate(Screen.Login.route) {
                popUpTo(0)
                launchSingleTop = true
            }
        }
    }

    BackHandler(enabled = drawerState.isOpen) {
        coroutineScope.launch {
            drawerState.close()
        }
    }

    AppNavigationDrawer(
        drawerState = drawerState,
        currentRoute = currentRoute,
        isAuthenticated = authUiState.isAuthenticated,
        gesturesEnabled = isTopLevelRoute,
        onNavigateHome = { navigateToTopLevel(Screen.Home.route, true) },
        onNavigateCourses = { navigateToTopLevel(Screen.Courses.route, true) },
        onNavigateExams = { navigateToTopLevel(Screen.Exams.createRoute(), false) },
        onNavigateLessons = { navigateToTopLevel(Screen.Lessons.createRoute(), true) },
        onNavigateStatistics = { navigateToTopLevel(Screen.Statistics.route, true) },
        onNavigatePublicUniboImport = { navigateToTopLevel(Screen.PublicUniboImport.route, true) },
        onNavigateMap = { navigateToTopLevel(Screen.Map.route, true) },
        onNavigateProfile = { navigateToTopLevel(Screen.Profile.route, true) },
        onNavigateSettings = { navigateToTopLevel(Screen.Settings.route, true) },
        onLogout = logoutFromDrawer
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    authViewModel = authViewModel,
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    authViewModel = authViewModel,
                    onRegisterSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToLogin = {
                        navController.popBackStack(Screen.Login.route, inclusive = false)
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onMenuClick = onOpenDrawer
                )
            }

            composable(Screen.Courses.route) {
                CoursesScreen(
                    onAddCourseClick = {
                        navController.navigate(Screen.AddEditCourse.createRoute())
                    },
                    onCourseClick = { courseId ->
                        navController.navigate(Screen.CourseDetail.createRoute(courseId))
                    },
                    onMenuClick = onOpenDrawer
                )
            }

            composable(
                route = Screen.Exams.route,
                arguments = listOf(
                    navArgument(Screen.Exams.ARG_COURSE_ID) {
                        type = NavType.IntType
                        defaultValue = -1
                    }
                )
            ) { backStackEntry ->
                val courseId = backStackEntry.arguments
                    ?.getInt(Screen.Exams.ARG_COURSE_ID)
                    ?.takeIf { it > 0 }

                ExamsScreen(
                    initialCourseId = courseId,
                    onMenuClick = onOpenDrawer,
                    onAddExamClick = { selectedCourseId ->
                        navController.navigate(Screen.AddEditExam.createRoute(selectedCourseId))
                    },
                    onEditExamClick = { examAppealId ->
                        navController.navigate(
                            Screen.AddEditExam.createRoute(examAppealId = examAppealId)
                        )
                    },
                    onOpenCourseClick = { selectedCourseId ->
                        navController.navigate(Screen.CourseDetail.createRoute(selectedCourseId))
                    }
                )
            }

            composable(
                route = Screen.Lessons.route,
                arguments = listOf(
                    navArgument(Screen.Lessons.ARG_COURSE_ID) {
                        type = NavType.IntType
                        defaultValue = -1
                    }
                )
            ) { backStackEntry ->
                val courseId = backStackEntry.arguments
                    ?.getInt(Screen.Lessons.ARG_COURSE_ID)
                    ?.takeIf { it > 0 }

                LessonsScreen(
                    initialCourseId = courseId,
                    onMenuClick = onOpenDrawer,
                    onLessonClick = { selectedCourseId, lessonId ->
                        navController.navigate(
                            Screen.AddEditLesson.createRoute(selectedCourseId, lessonId)
                        )
                    },
                    onAddLessonClick = { selectedCourseId ->
                        navController.navigate(Screen.AddEditLesson.createRoute(selectedCourseId))
                    },
                    onOpenCourseClick = { selectedCourseId ->
                        navController.navigate(Screen.CourseDetail.createRoute(selectedCourseId))
                    }
                )
            }

            composable(
                route = Screen.CourseDetail.route,
                arguments = listOf(
                    navArgument(Screen.CourseDetail.ARG_COURSE_ID) {
                        type = NavType.IntType
                    }
                )
            ) { backStackEntry ->
                val courseId = backStackEntry.arguments
                    ?.getInt(Screen.CourseDetail.ARG_COURSE_ID)
                    ?: 0

                CourseDetailScreen(
                    courseId = courseId,
                    onEditCourseClick = {
                        navController.navigate(Screen.AddEditCourse.createRoute(courseId))
                    },
                    onOpenCourseLessonsClick = { selectedCourseId ->
                        navController.navigate(Screen.Lessons.createRoute(selectedCourseId))
                    },
                    onOpenCourseExamsClick = { selectedCourseId ->
                        navController.navigate(Screen.Exams.createRoute(selectedCourseId))
                    },
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onCourseDeleted = {
                        navController.navigate(Screen.Courses.route) {
                            popUpTo(Screen.Courses.route) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(
                route = Screen.AddEditLesson.route,
                arguments = listOf(
                    navArgument(Screen.AddEditLesson.ARG_COURSE_ID) {
                        type = NavType.IntType
                    }
                )
            ) { backStackEntry ->
                val courseId = backStackEntry.arguments
                    ?.getInt(Screen.AddEditLesson.ARG_COURSE_ID)
                    ?: 0

                AddEditLessonScreen(
                    courseId = courseId,
                    lessonId = null,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.AddEditLesson.routeWithLessonId,
                arguments = listOf(
                    navArgument(Screen.AddEditLesson.ARG_COURSE_ID) {
                        type = NavType.IntType
                    },
                    navArgument(Screen.AddEditLesson.ARG_LESSON_ID) {
                        type = NavType.IntType
                    }
                )
            ) { backStackEntry ->
                val courseId = backStackEntry.arguments
                    ?.getInt(Screen.AddEditLesson.ARG_COURSE_ID)
                    ?: 0
                val lessonId = backStackEntry.arguments
                    ?.getInt(Screen.AddEditLesson.ARG_LESSON_ID)

                AddEditLessonScreen(
                    courseId = courseId,
                    lessonId = lessonId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.AddEditExam.route,
                arguments = listOf(
                    navArgument(Screen.AddEditExam.ARG_COURSE_ID) {
                        type = NavType.IntType
                        defaultValue = -1
                    },
                    navArgument(Screen.AddEditExam.ARG_EXAM_APPEAL_ID) {
                        type = NavType.IntType
                        defaultValue = -1
                    }
                )
            ) { backStackEntry ->
                val courseId = backStackEntry.arguments
                    ?.getInt(Screen.AddEditExam.ARG_COURSE_ID)
                    ?.takeIf { it > 0 }
                val examAppealId = backStackEntry.arguments
                    ?.getInt(Screen.AddEditExam.ARG_EXAM_APPEAL_ID)
                    ?.takeIf { it > 0 }

                AddEditExamScreen(
                    courseId = courseId,
                    examAppealId = examAppealId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.AddEditCourse.route) {
                AddEditCourseScreen(
                    courseId = null,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.AddEditCourse.routeWithCourseId,
                arguments = listOf(
                    navArgument(Screen.AddEditCourse.ARG_COURSE_ID) {
                        type = NavType.IntType
                    }
                )
            ) { backStackEntry ->
                val courseId = backStackEntry.arguments
                    ?.getInt(Screen.AddEditCourse.ARG_COURSE_ID)

                AddEditCourseScreen(
                    courseId = courseId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLogoutSuccess = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0)
                            launchSingleTop = true
                        }
                    },
                    onMenuClick = onOpenDrawer
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onMenuClick = onOpenDrawer
                )
            }

            composable(Screen.Map.route) {
                MapScreen(
                    onMenuClick = onOpenDrawer
                )
            }

            composable(Screen.Statistics.route) {
                StatisticsScreen(
                    onMenuClick = onOpenDrawer
                )
            }

            composable(Screen.University.route) {
                UniversityScreen(
                    onMenuClick = onOpenDrawer,
                    onOpenPublicUniboImportClick = {
                        navController.navigate(Screen.PublicUniboImport.route)
                    }
                )
            }

            composable(Screen.PublicUniboImport.route) {
                PublicUniboImportScreen(
                    onMenuClick = onOpenDrawer,
                    onGoToCoursesClick = {
                        navController.navigate(Screen.Courses.route) {
                            popUpTo(Screen.Courses.route) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }

    LaunchedEffect(pendingCourseId) {
        val courseId = pendingCourseId
        if (courseId != null && authViewModel.uiState.value.isAuthenticated) {
            navController.navigate(Screen.CourseDetail.createRoute(courseId))
            pendingCourseId = null
        }
    }
}

private fun isLessonsRoute(currentRoute: String?): Boolean {
    return currentRoute?.startsWith("lessons") == true || currentRoute == Screen.Lessons.route
}

private fun isExamsRoute(currentRoute: String?): Boolean {
    return currentRoute?.startsWith("exams") == true || currentRoute == Screen.Exams.route
}
