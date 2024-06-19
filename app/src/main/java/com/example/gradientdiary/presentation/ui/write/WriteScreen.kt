package com.example.gradientdiary.presentation.ui.write


import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.gradientdiary.data.DiaryModel
import com.example.gradientdiary.data.database.entity.DiaryEntity
import com.example.gradientdiary.presentation.theme.GradientDiaryTheme
import com.example.gradientdiary.presentation.ui.component.ContentBlock
import com.example.gradientdiary.presentation.ui.component.EditableText
import com.example.gradientdiary.presentation.viewModel.ContentBlockViewModel
import com.example.gradientdiary.presentation.viewModel.WriteViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun WriteScreen(
    date: String,
    content: DiaryEntity? = null,
    writeViewModel: WriteViewModel,
    contentBlockViewModel: ContentBlockViewModel,
    handleBackButtonClick: () -> Unit
) {
    val contentsState by remember { mutableStateOf(contentBlockViewModel.contentBlocks) }


    val inputFormat = DateTimeFormatter.ofPattern("yyyyMMdd", Locale.getDefault())
    val outputFormat = DateTimeFormatter.ofPattern("yyyy년 M월 d일", Locale.getDefault())
    val formatDate: LocalDate = LocalDate.parse(date, inputFormat)!!
    val outputDateString: String = outputFormat.format(formatDate)

    val handleSaveDiary = {
        CoroutineScope(Dispatchers.IO).launch {
            val newMemoModel = content?.let {
                it.copy().convertToDiaryModel().apply {
                    title = contentBlockViewModel.title
                    contents = contentsState.value
                }
            } ?: DiaryModel(
                contents = contentsState.value,
                category = writeViewModel.getCategory(),
                title = contentBlockViewModel.title,
                updateDate = date
            )

            val contentsCount = contentsState.value.count {
                it.content.toString().isNotBlank() or it.content.toString().isNotEmpty()
            }

            if (contentsCount > 0) {
                Timber.e("save 예정$newMemoModel")
                writeViewModel.saveDiary(diaryModel = newMemoModel)
            }
        }
    }
/*
    val handleAddImage = {

    }*/

    val contentValue = contentsState.collectAsState()
    WriteScreenContent(
        outputDateString,
        contentValue.value,
        contentBlockViewModel,
        handleSaveDiary,
        handleBackButtonClick
    )
}

@Composable
fun keyboardAsState(): State<Boolean> {
    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    return rememberUpdatedState(isImeVisible)
}

@Composable
private fun WriteScreenContent(
    outputDateString: String,
    contents: List<ContentBlock<*>>,
    contentBlockViewModel: ContentBlockViewModel,
    handleSaveDiary: () -> Job,
    handleBackButtonClick: () -> Unit
) {
    val hint = "제목"
    var diaryTitle by rememberSaveable { mutableStateOf(contentBlockViewModel.title) }
    val isKeyboardOpen by keyboardAsState()
    val focusManager = LocalFocusManager.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycle = lifecycleOwner.lifecycle
    val contentsState by contentBlockViewModel.contentBlocks.collectAsState()
    var previousKeyboardState by remember { mutableStateOf(false) }

    //top 에 삭제버튼 추가 필요

    LaunchedEffect(key1 = diaryTitle) {
        contentBlockViewModel.title = diaryTitle
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                        handleSaveDiary()  // 앱 백그라운드 전환시에도 save
                    }
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    val handleBackClickSave = {
        handleSaveDiary()
        handleBackButtonClick()
    }

    //사용자가 키보드를 직접 내릴경우 ,
    // focus clear 를 해줘야 정상적인 backHandler 가 동작하기 때문에 clear 해준다.
    LaunchedEffect(isKeyboardOpen) {
        if (previousKeyboardState && !isKeyboardOpen) {
            Timber.e("keyboard clear")
            focusManager.clearFocus()
        }
        previousKeyboardState = isKeyboardOpen
    }

    BackHandler {
        // back 버튼 클릭시 save
        handleBackClickSave()
    }


    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.size(50.dp))
            Text(
                outputDateString,
                style = MaterialTheme.typography.titleMedium
            )

            EditableText(
                value = diaryTitle,
                hint = hint,
                style = MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.Center)
            ) {
                diaryTitle = it
            }
        }
        ContentBlockScreen(
            contentBlockViewModel = contentBlockViewModel,
            contents = contentsState
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewWriteScreen() {
    GradientDiaryTheme {
        val date = "20220202"

     /*   WriteScreen(
            date = date,
            contentBlockViewModel = ContentBlockViewModel(emptyList()),
            content = null ,
            writeViewModel = null
        ){}*/
    }
}