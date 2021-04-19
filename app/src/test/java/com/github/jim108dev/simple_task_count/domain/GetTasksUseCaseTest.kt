package com.github.jim108dev.simple_task_count.domain

import com.github.jim108dev.simple_task_count.data.Result.Error
import com.github.jim108dev.simple_task_count.data.Result.Success
import com.github.jim108dev.simple_task_count.data.Task
import com.github.jim108dev.simple_task_count.data.source.FakeRepository
import com.github.jim108dev.simple_task_count.tasks.TasksFilterType.DEBIT
import com.github.jim108dev.simple_task_count.tasks.TasksFilterType.CREDIT
import com.github.jim108dev.simple_task_count.util.DateUtil
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [GetTasksUseCase].
 */
@ExperimentalCoroutinesApi
class GetTasksUseCaseTest {

    private val tasksRepository = FakeRepository()

    private val useCase = GetTasksUseCase(tasksRepository)

    @Test
    fun loadTasks_noFilter_empty() = runBlockingTest {
        // Given an empty repository

        // When calling the use case
        val result = useCase()

        // Verify the result is a success and empty
        assertTrue(result is Success)
        assertTrue((result as Success).data.isEmpty())
    }

    @Test
    fun loadTasks_error() = runBlockingTest {
        // Make the repository return errors
        tasksRepository.setReturnError(true)

        // Load tasks
        val result = useCase()

        // Verify the result is an error
        assertTrue(result is Error)
    }

    @Test
    fun loadTasks_noFilter() = runBlockingTest {
        // Given a repository with 1 active and 2 completed tasks:
        tasksRepository.addTasks(
                Task(DateUtil.convertStringToDate("02/01/21"), "title", 10),
                Task(DateUtil.convertStringToDate("02/01/21"), "title", 20),
                Task(DateUtil.convertStringToDate("02/01/21"), "title", -5)
        )

        // Load tasks
        val result = useCase()

        // Verify the result is filtered correctly
        assertTrue(result is Success)
        assertEquals((result as Success).data.size, 3)
    }

    @Test
    fun loadTasks_completedFilter() = runBlockingTest {
        // Given a repository with 1 active and 2 completed tasks:
        tasksRepository.addTasks(
                Task(DateUtil.convertStringToDate("02/01/21"), "title", 10),
                Task(DateUtil.convertStringToDate("02/01/21"), "title", 20),
                Task(DateUtil.convertStringToDate("02/01/21"), "title", -5)
        )

        // Load tasks
        val result = useCase(currentFiltering = CREDIT)

        // Verify the result is filtered correctly
        assertTrue(result is Success)
        assertEquals((result as Success).data.size, 1)
    }

    @Test
    fun loadTasks_activeFilter() = runBlockingTest {
        // Given a repository with 1 active and 2 completed tasks:
        tasksRepository.addTasks(
                Task(DateUtil.convertStringToDate("02/01/21"), "title", 10),
                Task(DateUtil.convertStringToDate("02/01/21"), "title", 20),
                Task(DateUtil.convertStringToDate("02/01/21"), "title", -5)
        )

        // Load tasks
        val result = useCase(currentFiltering = DEBIT)

        // Verify the result is filtered correctly
        assertTrue(result is Success)
        assertEquals((result as Success).data.size, 2)
    }
}