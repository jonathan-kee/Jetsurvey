package com.manasmalla.jetsurvey.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.manasmalla.jetsurvey.ui.onboarding.WelcomeScreen
import com.manasmalla.jetsurvey.ui.survey.SurveyScaffold
import com.manasmalla.jetsurvey.ui.survey.SurveySummaryScreen
import com.manasmalla.jetsurvey.ui.survey.SurveyViewModel
import com.manasmalla.jetsurvey.ui.survey.util.SurveyDbHelper

object Destinations {
    const val WELCOME_ROUTE = "welcome"
    const val SURVEY_ROUTE = "survey"
    const val SURVEY_RESULT_ROUTE = "result"

    const val SUMMARY_RESULT_ROUTE = "sumary"
}

@Composable
fun JetsurveyApp(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Destinations.WELCOME_ROUTE) {

        // "welcome"
        composable(Destinations.WELCOME_ROUTE) {
            WelcomeScreen(
                onNavigateToSignIn = {
                    navController.navigate("signin/$it")
                },
                onNavigateToSurvey = {
                    navController.navigate(Destinations.SURVEY_ROUTE)
                }
            )
        }

        // "survey"
        composable(Destinations.SURVEY_ROUTE) {
            SurveyScaffold(
                onNavigateUp = {
                    navController.navigateUp()
                },
                onNavigateToResults = {
                    navController.navigate(Destinations.SURVEY_RESULT_ROUTE)
                }
            )
        }

        composable(Destinations.SURVEY_RESULT_ROUTE){
            SurveyResultScreen (
                onDonePressed = { navController.popBackStack(Destinations.WELCOME_ROUTE, false)},
                onSummaryPressed = { navController.navigate(Destinations.SUMMARY_RESULT_ROUTE)}
            )
        }

        // "result"
        composable(Destinations.SUMMARY_RESULT_ROUTE) {
            // 1. Get Application Context
            val context = LocalContext.current.applicationContext

            // 2. Instantiate ViewModel for Summary Screen
            val summaryViewModel: SurveyViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        val dbHelper = SurveyDbHelper(context)
                        SurveyViewModel(dbHelper)
                    }
                }
            )

            // 3. Render Summary Screen
            SurveySummaryScreen(
                viewModel = summaryViewModel,
                onDone = {
                    // Pop all survey screens and return to Welcome screen
                    navController.popBackStack(Destinations.WELCOME_ROUTE, inclusive = false)
                }
            )
        }
    }
}