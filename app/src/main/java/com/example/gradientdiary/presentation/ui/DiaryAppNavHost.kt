package com.example.gradientdiary.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.gradientdiary.data.database.entity.DiaryEntity
import com.example.gradientdiary.presentation.ui.Key.DIARY_ARGS_KEY
import com.example.gradientdiary.presentation.ui.component.LoadingScreen
import com.example.gradientdiary.presentation.ui.home.DiaryScreen
import com.example.gradientdiary.presentation.ui.listview.ListViewScreen
import com.example.gradientdiary.presentation.ui.search.SearchScreen
import com.example.gradientdiary.presentation.ui.write.WriteScreen
import com.example.gradientdiary.presentation.viewModel.CategoryViewModel
import com.example.gradientdiary.presentation.viewModel.ContentBlockViewModel
import com.example.gradientdiary.presentation.viewModel.ListViewViewModel
import com.example.gradientdiary.presentation.viewModel.SearchViewModel
import com.example.gradientdiary.presentation.viewModel.WriteViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun DiaryAppNavHost(
    writeViewModel: WriteViewModel,
    categoryViewModel: CategoryViewModel,
    searchViewModel: SearchViewModel,
    listViewViewModel: ListViewViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {

    NavHost(
        navController = navController,
        modifier = modifier,
        startDestination = DiaryAppScreen.Home.name
    ) {
        val handleClickCalendarColumn = { date: String ->
            navController.navigate("${DiaryAppScreen.Write.name}/${date}") {
                popUpTo(DiaryAppScreen.Home.name)
            }
        }
        val handleClickAddDiaryButton = { date: String ->
            navController.navigate("${DiaryAppScreen.Write.name}/${date}") {
                popUpTo(DiaryAppScreen.Home.name)
            }
        }
        val handleBackButtonClick = {
            navController.navigateUp()
        }
        val handleSearchIconClick = {
            navController.navigate(DiaryAppScreen.Search.name)
        }
        val handleListViewClick = {
            navController.navigate(DiaryAppScreen.ListView.name)
        }
        val handleSettingClick = {
            //todo
        }
        composable(DiaryAppScreen.Home.name) {
            DiaryScreen(
                // memoViewModel = memoViewModel,
                categoryViewModel = categoryViewModel,
                handleClickAddDiaryButton = handleClickAddDiaryButton,
                handleClickCalendarColumn = handleClickCalendarColumn,
                handleSearchIconClick = handleSearchIconClick,
                handleListViewClick = handleListViewClick,
                handleSettingClick = handleSettingClick
            )
        }
        composable(DiaryAppScreen.Search.name){
            SearchScreen(
                searchViewModel,
                categoryViewModel,
                handleBackButtonClick = { handleBackButtonClick() },
            )
        }
        composable(DiaryAppScreen.ListView.name){
            ListViewScreen(
                listViewViewModel,
                categoryViewModel,
                handleBackButtonClick ={}
            )
        }
        composable(
            route = "${DiaryAppScreen.Write.name}/{${DIARY_ARGS_KEY}}",
            arguments = listOf(navArgument(DIARY_ARGS_KEY) {
                type = NavType.StringType
            })
        ) { entry ->
            Timber.e("ARGUMENT : ${entry.arguments?.getString(DIARY_ARGS_KEY)}")
            val date = entry.arguments?.getString(DIARY_ARGS_KEY) ?: ""
            var content by remember { mutableStateOf<DiaryEntity?>(null) }
            var isLoading by remember { mutableStateOf(true) }

            LaunchedEffect(date) {
                // room db 에서 데이터 get 완료될 때까지 대기 process
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val categoryId = categoryViewModel.getCategoryId()
                        val task: Deferred<DiaryEntity?> =
                            async { writeViewModel.getDiaryByDateAndCategory(categoryId,date) }
                        content = task.await()
                    } finally {
                        isLoading = false
                    }
                }
            }

            if (isLoading) {
                LoadingScreen()
            } else {
                Timber.e("navhost get content : ${content?.contents}")
                val contentBlockViewModel = remember {
                    mutableStateOf(ContentBlockViewModel(content))
                }

                WriteScreen(
                    date = date,
                    content = content,
                    writeViewModel = writeViewModel,
                    categoryViewModel = categoryViewModel,
                    contentBlockViewModel = contentBlockViewModel.value,
                    handleBackButtonClick = { handleBackButtonClick() }
                )
            }
        }
    }
}

object Key {
    const val DIARY_ARGS_KEY = "date"
}
