package com.desafiolatam.coroutines.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desafiolatam.coroutines.data.TaskEntity
import com.desafiolatam.coroutines.repository.TaskRepositoryImp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(private val repository: TaskRepositoryImp) : ViewModel() {

    private val _data: MutableStateFlow<List<TaskEntity>> = MutableStateFlow(emptyList())
    val taskListStateFlow: StateFlow<List<TaskEntity>> =_data.asStateFlow()

    private val _uiState: MutableStateFlow<UIState?> = MutableStateFlow(null)
    val uiState: StateFlow<UIState?> = _uiState.asStateFlow()

    private val job: Job = Job()
    private val dispatcherIO : CoroutineDispatcher = Dispatchers.IO
    private val coroutineScope: CoroutineScope = CoroutineScope(job + dispatcherIO)

    suspend fun getTasks() {
        val getTasksJob = coroutineScope.launch {
            repository.getTasks().collectLatest {
                _data.value = it
            }
        }

        when {
            getTasksJob.isActive -> _uiState.value = UIState.Success(true)
            getTasksJob.isCompleted -> _uiState.value =UIState.Success(true)
            getTasksJob.isCancelled -> _uiState.value =
                UIState.Error(Throwable("Cancelado"))
            else -> _uiState.value=UIState.Error(Throwable("algo salio mal!"))
        }

    }

    sealed class UIState {
        data class Success(val state: Boolean) : UIState()
        data class Error(val ex: Throwable) : UIState()
    }

    suspend fun deleteTask(task: TaskEntity) {
        viewModelScope.launch(dispatcherIO) {
            repository.deleteTask(task)
        }
    }

    suspend fun addTask(task: TaskEntity) {
        viewModelScope.launch(dispatcherIO) {
            repository.addTask(task)
        }
    }

}