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

package com.github.jim108dev.simple_task_count.tasks

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.github.jim108dev.simple_task_count.LiveDataTestUtil
import com.github.jim108dev.simple_task_count.MainCoroutineRule
import com.github.jim108dev.simple_task_count.R
import com.github.jim108dev.simple_task_count.assertLiveDataEventTriggered
import com.github.jim108dev.simple_task_count.assertSnackbarMessage
import com.github.jim108dev.simple_task_count.data.Task
import com.github.jim108dev.simple_task_count.data.source.FakeRepository
import com.github.jim108dev.simple_task_count.domain.DeleteAllTasksUseCase
import com.github.jim108dev.simple_task_count.domain.GetTasksUseCase
import com.github.jim108dev.simple_task_count.util.DateUtil
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for the implementation of [TasksViewModel]
 */
@ExperimentalCoroutinesApi
class TasksViewModelTest {

    // Subject under test
    private lateinit var tasksViewModel: TasksViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var tasksRepository: FakeRepository

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() {
        // We initialise the tasks to 3, with one active and two completed
        tasksRepository = FakeRepository()
        val date = DateUtil.convertStringToDate("01/01/21")
        val task1 = Task(date, "Title1", 10)
        val task2 = Task(date,"Title2", 20)
        val task3 = Task(date,"Title3", 30)
        tasksRepository.addTasks(task1, task2, task3)

        tasksViewModel = TasksViewModel(
                DeleteAllTasksUseCase(tasksRepository),
                GetTasksUseCase(tasksRepository)
        )
    }

    @Test
    fun loadAllTasksFromRepository_loadingTogglesAndDataLoaded() {
        // Pause dispatcher so we can verify initial values
        mainCoroutineRule.pauseDispatcher()

        // Given an initialized TasksViewModel with initialized tasks
        // When loading of Tasks is requested
        tasksViewModel.setFiltering(TasksFilterType.ALL)

        // Trigger loading of tasks
        tasksViewModel.loadTasks(true)

        // Then progress indicator is shown
        assertThat(LiveDataTestUtil.getValue(tasksViewModel.dataLoading)).isTrue()

        // Execute pending coroutines actions
        mainCoroutineRule.resumeDispatcher()

        // Then progress indicator is hidden
        assertThat(LiveDataTestUtil.getValue(tasksViewModel.dataLoading)).isFalse()

        // And data correctly loaded
        assertThat(LiveDataTestUtil.getValue(tasksViewModel.items)).hasSize(3)
    }

    @Test
    fun loadActiveTasksFromRepositoryAndLoadIntoView() {
        // Given an initialized TasksViewModel with initialized tasks
        // When loading of Tasks is requested
        tasksViewModel.setFiltering(TasksFilterType.DEBIT)

        // Load tasks
        tasksViewModel.loadTasks(true)

        // Then progress indicator is hidden
        assertThat(LiveDataTestUtil.getValue(tasksViewModel.dataLoading)).isFalse()

        // And data correctly loaded
        assertThat(LiveDataTestUtil.getValue(tasksViewModel.items)).hasSize(1)
    }

    @Test
    fun loadCompletedTasksFromRepositoryAndLoadIntoView() {
        // Given an initialized TasksViewModel with initialized tasks
        // When loading of Tasks is requested
        tasksViewModel.setFiltering(TasksFilterType.CREDIT)

        // Load tasks
        tasksViewModel.loadTasks(true)

        // Then progress indicator is hidden
        assertThat(LiveDataTestUtil.getValue(tasksViewModel.dataLoading)).isFalse()

        // And data correctly loaded
        assertThat(LiveDataTestUtil.getValue(tasksViewModel.items)).hasSize(2)
    }

    @Test
    fun loadTasks_error() {
        // Make the repository return errors
        tasksRepository.setReturnError(true)

        // Load tasks
        tasksViewModel.loadTasks(true)

        // Then progress indicator is hidden
        assertThat(LiveDataTestUtil.getValue(tasksViewModel.dataLoading)).isFalse()

        // And the list of items is empty
        assertThat(LiveDataTestUtil.getValue(tasksViewModel.items)).isEmpty()

        // And the snackbar updated
        assertSnackbarMessage(tasksViewModel.snackbarText, R.string.loading_tasks_error)
    }

    @Test
    fun clickOnFab_showsAddTaskUi() {
        // When adding a new task
        tasksViewModel.addNewTask()

        // Then the event is triggered
        val value = LiveDataTestUtil.getValue(tasksViewModel.newTaskEvent)
        assertThat(value.getContentIfNotHandled()).isNotNull()
    }

    @Test
    fun clickOnOpenTask_setsEvent() {
        // When opening a new task
        val taskId = "42"
        tasksViewModel.openTask(taskId)

        // Then the event is triggered
        assertLiveDataEventTriggered(tasksViewModel.openTaskEvent, taskId)
    }

    @Test
    fun clearCompletedTasks_clearsTasks() = mainCoroutineRule.runBlockingTest {
        // When completed tasks are cleared
        tasksViewModel.deleteAllTasks()

        // Fetch tasks
        tasksViewModel.loadTasks(true)

        // Fetch tasks
        val allTasks = LiveDataTestUtil.getValue(tasksViewModel.items)

        // Verify there are no completed tasks left
        assertThat(allTasks).isEmpty()

        // Verify snackbar is updated
        assertSnackbarMessage(
                tasksViewModel.snackbarText, R.string.delete_all_tasks
        )
    }

    @Test
    fun showEditResultMessages_editOk_snackbarUpdated() {
        // When the viewmodel receives a result from another destination
        tasksViewModel.showEditResultMessage(EDIT_RESULT_OK)

        // The snackbar is updated
        assertSnackbarMessage(
                tasksViewModel.snackbarText, R.string.successfully_saved_task_message
        )
    }

    @Test
    fun showEditResultMessages_addOk_snackbarUpdated() {
        // When the viewmodel receives a result from another destination
        tasksViewModel.showEditResultMessage(ADD_EDIT_RESULT_OK)

        // The snackbar is updated
        assertSnackbarMessage(
                tasksViewModel.snackbarText, R.string.successfully_added_task_message
        )
    }

    @Test
    fun showEditResultMessages_deleteOk_snackbarUpdated() {
        // When the viewmodel receives a result from another destination
        tasksViewModel.showEditResultMessage(DELETE_RESULT_OK)

        // The snackbar is updated
        assertSnackbarMessage(
                tasksViewModel.snackbarText, R.string.successfully_deleted_task_message
        )
    }

    @Test
    fun getTasksAddViewVisible() {
        // When the filter type is ALL
        tasksViewModel.setFiltering(TasksFilterType.ALL)

        // Then the "Add task" action is visible
        assertThat(LiveDataTestUtil.getValue(tasksViewModel.tasksAddViewVisible)).isTrue()
    }
}
