/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp.addedittask

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.architecture.blueprints.todoapp.Event
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.Result.Success
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.domain.DeleteTaskUseCase
import com.example.android.architecture.blueprints.todoapp.domain.GetTaskUseCase
import com.example.android.architecture.blueprints.todoapp.domain.SaveTaskUseCase
import com.example.android.architecture.blueprints.todoapp.util.DateUtil
import kotlinx.coroutines.launch
import java.util.*

/**
 * ViewModel for the Add/Edit screen.
 */
class AddEditTaskViewModel(
        private val deleteTaskUseCase: DeleteTaskUseCase,
        private val getTaskUseCase: GetTaskUseCase,
        private val saveUseCase: SaveTaskUseCase
) : ViewModel() {

    val date = MutableLiveData<String>()

    val title = MutableLiveData<String>()

    val amount = MutableLiveData<String>()

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarText: LiveData<Event<Int>> = _snackbarText

    private val _taskUpdatedEvent = MutableLiveData<Event<Unit>>()
    val taskUpdatedEvent: LiveData<Event<Unit>> = _taskUpdatedEvent

    private var taskId: String? = null

    private var isNewTask: Boolean = false

    private var isDataLoaded = false

    fun start(taskId: String?) {
        if (_dataLoading.value == true) {
            return
        }

        this.taskId = taskId
        if (taskId == null) {
            // No need to populate, it's a new task
            isNewTask = true
            return
        }
        if (isDataLoaded) {
            // No need to populate, already have data.
            return
        }

        isNewTask = false
        _dataLoading.value = true

        viewModelScope.launch {
            getTaskUseCase(taskId).let { result ->
                if (result is Success) {
                    onTaskLoaded(result.data)
                } else {
                    onDataNotAvailable()
                }
            }
        }
    }

    private fun onTaskLoaded(task: Task) {
        date.value = DateUtil.convertDateToString(task.date)
        title.value = task.title
        amount.value = task.amount.toString()
        _dataLoading.value = false
        isDataLoaded = true
    }

    private fun onDataNotAvailable() {
        _dataLoading.value = false
    }

    // Called when clicking on fab.
    fun saveTask() {
        val currentDateS = date.value
        val currentTitle = title.value
        val currentAmountS= amount.value

        if (currentDateS == null || currentTitle == null || currentAmountS== null) {
            _snackbarText.value = Event(R.string.empty_task_message)
            return
        }

        val currentDate = DateUtil.convertStringToDate(currentDateS)
        val currentAmount = currentAmountS.toInt()
        val currentTaskId = taskId
        if (isNewTask || currentTaskId == null) {
            createTask(Task(currentDate,currentTitle, currentAmount))
        } else {
            val task = Task(currentDate,currentTitle, currentAmount, currentTaskId)
            updateTask(task)
        }
    }

    private fun createTask(newTask: Task) = viewModelScope.launch {
        saveUseCase(newTask)
        _taskUpdatedEvent.value = Event(Unit)
    }

    private fun updateTask(task: Task) {
        if (isNewTask) {
            throw RuntimeException("updateTask() was called but task is new.")
        }
        viewModelScope.launch {
            saveUseCase(task)
            _taskUpdatedEvent.value = Event(Unit)
        }
    }

    fun deleteTask() = viewModelScope.launch {
        taskId?.let {
            deleteTaskUseCase(it)
            _taskUpdatedEvent.value = Event(Unit)
        }
    }
}
