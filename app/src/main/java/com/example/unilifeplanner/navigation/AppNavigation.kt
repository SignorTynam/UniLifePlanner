package com.example.unilifeplanner.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onRegisterClick = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onCreateAccountClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onLoginClick = {
                    navController.popBackStack(Screen.Login.route, inclusive = false)
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onCoursesClick = { navController.navigate(Screen.Courses.route) },
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                onMapClick = { navController.navigate(Screen.Map.route) },
                onStatisticsClick = { navController.navigate(Screen.Statistics.route) },
                onLogoutClick = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
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
                onDemoCourseClick = {
                    navController.navigate(Screen.CourseDetail.createRoute(courseId = 1))
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
                }
            )
        }

        composable(Screen.AddEditCourse.route) {
            AddEditCourseScreen(
                courseId = null,
                onSaveClick = {
                    navController.popBackStack()
                },
                onBackClick = {
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
                onSaveClick = {
                    navController.popBackStack()
                },
                onBackClick = {
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
