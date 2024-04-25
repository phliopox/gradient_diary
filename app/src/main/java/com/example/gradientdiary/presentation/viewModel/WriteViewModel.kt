package com.example.gradientdiary.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gradientdiary.data.database.entity.ContentEntity
import com.example.gradientdiary.data.database.entity.DiaryEntity
import com.example.gradientdiary.domain.DeleteDiaryUseCase
import com.example.gradientdiary.domain.GetDiaryByDateUseCase
import com.example.gradientdiary.domain.GetDiaryUseCase
import com.example.gradientdiary.domain.SaveDiaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class WriteViewModel @Inject constructor(
    private val saveDiaryUseCase: SaveDiaryUseCase,
    private val getDiaryUseCase: GetDiaryUseCase,
    private val getDiaryByDateUseCase: GetDiaryByDateUseCase,
    private val deleteDiaryUseCase: DeleteDiaryUseCase
) : ViewModel() {

    private val _diary = MutableStateFlow<DiaryEntity?>(null)
    var diary: StateFlow<DiaryEntity?> = _diary

    // var content = emptyList<ContentEntity>()
    fun getDiaryByDate(date: LocalDate) {
        Timber.e("viewModel getDiaryByDate 호출")
        viewModelScope.launch {
            getDiaryByDateUseCase.invoke(date).collectLatest {
                _diary.value = it
                Timber.e("getDiaryByDate 호출 : $it")
            }
        }
    }


    fun saveDiary(diaryEntity: DiaryEntity) {
        saveDiaryUseCase.invoke(diaryEntity)
    }


}