package com.desafiolatam.coroutines.view.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.desafiolatam.coroutines.data.TaskEntity
import com.desafiolatam.coroutines.databinding.ActivityMainBinding
import com.desafiolatam.coroutines.view.TaskViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val newTask: TaskEntity = TaskEntity(8,"Break","Coffee Break una vez que terminemos el ejercicio")

    lateinit var binding: ActivityMainBinding
    private val viewModel: TaskViewModel by viewModels()

    @Inject
    lateinit var adapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launchWhenCreated { viewModel.getTasks() }

        getTaskList()
        getUIState()
        insertTask(newTask)
    }

    private fun getUIState() {
        lifecycleScope.launchWhenCreated {
            viewModel.uiState.collectLatest { state ->
                state?.let {
                    when (it) {
                        is TaskViewModel.UIState.Success ->
                            Toast.makeText(this@MainActivity, "OK", Toast.LENGTH_SHORT).show()
                        is TaskViewModel.UIState.Error ->
                            Toast.makeText(this@MainActivity, "Error", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun getTaskList() {
        lifecycleScope.launchWhenCreated {
            viewModel.taskListStateFlow.collectLatest {
                initRecyclerView(it)
            }
        }
    }

    private fun initRecyclerView(taskList: List<TaskEntity>) {
        adapter = TaskAdapter()
        adapter.taskList = taskList
        binding.rvTask.layoutManager = LinearLayoutManager(this)
        binding.rvTask.adapter = adapter

        adapter.onLongClick = {
            deleteTask(it)
        }
    }

    private fun deleteTask(task: TaskEntity) {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.deleteTask(task)
            runOnUiThread {
                Toast.makeText(this@MainActivity,"Tarea eliminada", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun insertTask(task: TaskEntity) {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.addTask(task)
            runOnUiThread {
                Toast.makeText(this@MainActivity,"Tarea agregada", Toast.LENGTH_SHORT).show()
            }
        }
    }


}