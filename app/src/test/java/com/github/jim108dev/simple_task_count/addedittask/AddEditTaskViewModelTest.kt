/*
 * Copyright (C) 2021 jim108dev
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
package com.github.jim108dev.simple_task_count.addedittask

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.github.jim108dev.simple_task_count.LiveDataTestUtil.getValue
import com.github.jim108dev.simple_task_count.MainCoroutineRule
import com.github.jim108dev.simple_task_count.R.string
import com.github.jim108dev.simple_task_count.assertSnackbarMessage
import com.github.jim108dev.simple_task_count.data.Task
import com.github.jim108dev.simple_task_count.data.source.FakeRepository
import com.github.jim108dev.simple_task_count.domain.DeleteTaskUseCase
import com.github.jim108dev.simple_task_count.domain.GetTaskUseCase
import com.github.jim108dev.simple_task_count.domain.SaveTaskUseCase
import com.github.jim108dev.simple_task_count.util.DateUtil
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for the implementation of [AddEditTaskViewModel].
 */
@ExperimentalCoroutinesApi
class AddEditTaskViewModelTest {

    // Subject under test
    private lateinit var addEditTaskViewModel: AddEditTaskViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var tasksRepository: FakeRepository

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val task = Task(DateUtil.convertStringToDate("02/01/21"), "title", 15)


    @Before
    fun setupViewModel() {
        // We initialise the repository with no tasks
        tasksRepository = FakeRepository()

        // Create class under test
        addEditTaskViewModel = AddEditTaskViewModel(
                DeleteTaskUseCase(tasksRepository),
                GetTaskUseCase(tasksRepository),
                SaveTaskUseCase(tasksRepository)
        )
    }

    @Test
    fun saveNewTaskToRepository_showsSuccessMessageUi() {
        val newDate = DateUtil.convertStringToDate("02/01/21")
        val newTitle = "New Task Title"
        val newAmount = 15
        (addEditTaskViewModel).apply {
            date.value = "02/01/21"
            title.value = newTitle
            amount.value = "15"
        }
        addEditTaskViewModel.saveTask()

        val newTask = tasksRepository.tasksServiceData.values.first()

        // Then a task is saved in the repository and the view updated
        assertThat(newTask.date).isEqualTo(newDate)
        assertThat(newTask.title).isEqualTo(newTitle)
        assertThat(newTask.amount).isEqualTo(newAmount)
    }

    @Test
    fun loadTasks_loading() {
        // Pause dispatcher so we can verify initial values
        mainCoroutineRule.pauseDispatcher()

        // Load the task in the viewmodel
        addEditTaskViewModel.start(task.id)

        // Then progress indicator is shown
        assertThat(getValue(addEditTaskViewModel.dataLoading)).isTrue()

        // Execute pending coroutines actions
        mainCoroutineRule.resumeDispatcher()

        // Then progress indicator is hidden
        assertThat(getValue(addEditTaskViewModel.dataLoading)).isFalse()
    }

    @Test
    fun saveNewTaskToRepository_emptyTitle_error() {
        saveTaskAndAssertSnackbarError("", "Some Task Description")
    }

    @Test
    fun saveNewTaskToRepository_nullTitle_error() {
        saveTaskAndAssertSnackbarError(null, "Some Task Description")
    }

    @Test
    fun saveNewTaskToRepository_emptyDescription_error() {
        saveTaskAndAssertSnackbarError("Title", "")
    }

    @Test
    fun saveNewTaskToRepository_nullDescription_error() {
        saveTaskAndAssertSnackbarError("Title", null)
    }

    @Test
    fun saveNewTaskToRepository_nullDescriptionNullTitle_error() {
        saveTaskAndAssertSnackbarError(null, null)
    }

    @Test
    fun saveNewTaskToRepository_emptyDescriptionEmptyTitle_error() {
        saveTaskAndAssertSnackbarError("", "")
    }

    private fun saveTaskAndAssertSnackbarError(title: String?, description: String?) {
        (addEditTaskViewModel).apply {
            this.title.value = title
            this.amount.value = description
        }

        // When saving an incomplete task
        addEditTaskViewModel.saveTask()

        // Then the snackbar shows an error
        assertSnackbarMessage(addEditTaskViewModel.snackbarText, string.empty_task_message)
    }
}
