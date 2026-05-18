package com.example.unilifeplanner.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.unilifeplanner.ui.auth.AuthViewModel
import com.example.unilifeplanner.ui.auth.LoginScreen
import com.example.unilifeplanner.ui.auth.RegisterScreen
import com.example.unilifeplanner.ui.courses.AddEditCourseScreen
import com.example.unilifeplanner.ui.courses.CourseDetailScreen
import com.example.unilifeplanner.ui.courses.CoursesScreen
import com.example.unilifeplanner.ui.home.HomeScreen
import com.example.unilifeplanner.ui.map.MapScreen
import com.example.unilifeplanner.ui.profile.ProfileScreen
import com.example.unilifeplanner.ui.settings.SettingsScreen
import com.example.unilifeplanner.ui.statistics.StatisticsScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel()
) {
    val startDestination = if (authViewModel.uiState.value.isAuthenticated) {
        Screen.Home.route
    } else {
        Screen.Login.route
    }

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
                onOpenCourses = { navController.navigate(Screen.Courses.route) },
                onAddCourse = { navController.navigate(Screen.AddEditCourse.createRoute()) },
                onOpenStatistics = { navController.navigate(Screen.Statistics.route) },
                onOpenMap = { navController.navigate(Screen.Map.route) },
                onOpenProfile = { navController.navigate(Screen.Profile.route) },
                onOpenSettings = { navController.navigate(Screen.Settings.route) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0)
                        launchSingleTop = true
                    }
                }
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
                onBackClick = {
                    navController.popBackStack()
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
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Map.route) {
            MapScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Statistics.route) {
            StatisticsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
