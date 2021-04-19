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

package com.github.jim108dev.simple_task_count.data.source.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.github.jim108dev.simple_task_count.MainCoroutineRule
import com.github.jim108dev.simple_task_count.data.Task
import com.github.jim108dev.simple_task_count.util.DateUtil
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class TasksDaoTest {

    private lateinit var database: ToDoDatabase

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        // using an in-memory database because the information stored here disappears when the
        // process is killed
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            ToDoDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun insertTaskAndGetById() = runBlockingTest {
        // GIVEN - insert a task
        val task = Task(DateUtil.convertStringToDate("02/01/21"),"title", 15)
        database.taskDao().insertTask(task)

        // WHEN - Get the task by id from the database
        val loaded = database.taskDao().getTaskById(task.id)

        // THEN - The loaded data contains the expected values
        assertThat<Task>(loaded as Task, notNullValue())
        assertThat(loaded.id, `is`(task.id))
        assertThat(loaded.date, `is`(task.date))
        assertThat(loaded.title, `is`(task.title))
        assertThat(loaded.amount, `is`(task.amount))
    }

    @Test
    fun insertTaskReplacesOnConflict() = runBlockingTest {
        // Given that a task is inserted
        val task = Task(DateUtil.convertStringToDate("02/01/21"),"title", 15)
        database.taskDao().insertTask(task)

        // When a task with the same id is inserted
        val newTask = Task(DateUtil.convertStringToDate("02/01/21"),"title2", 15, task.id)
        database.taskDao().insertTask(newTask)

        // THEN - The loaded data contains the expected values
        val loaded = database.taskDao().getTaskById(task.id)
        assertThat(loaded?.id, `is`(task.id))
        assertThat(loaded?.date, `is`(newTask.date))
        assertThat(loaded?.title, `is`(newTask.title))
        assertThat(loaded?.amount, `is`(newTask.amount))
    }

    @Test
    fun insertTaskAndGetTasks() = runBlockingTest {
        // GIVEN - insert a task
        val task = Task(DateUtil.convertStringToDate("02/01/21"),"title", 15)
        database.taskDao().insertTask(task)

        // WHEN - Get tasks from the database
        val tasks = database.taskDao().getTasks()

        // THEN - There is only 1 task in the database, and contains the expected values
        assertThat(tasks.size, `is`(1))
        val actual = tasks[0]

        assertThat(actual.id, `is`(task.id))
        assertThat(actual.date, `is`(task.date))
        assertThat(actual.title, `is`(task.title))
        assertThat(actual.amount, `is`(task.amount))
    }

    @Test
    fun updateTaskAndGetById() = runBlockingTest {
        // When inserting a task
        val originalTask =  Task(DateUtil.convertStringToDate("02/01/21"),"title", 15)
        database.taskDao().insertTask(originalTask)

        // When the task is updated
        val updatedTask =  Task(DateUtil.convertStringToDate("03/01/21"),"new title", 20, originalTask.id)
        database.taskDao().updateTask(updatedTask)

        // THEN - The loaded data contains the expected values
        val loaded = database.taskDao().getTaskById(originalTask.id)
        assertThat(loaded?.id, `is`(originalTask.id))
        assertThat(loaded?.date, `is` (DateUtil.convertStringToDate("03/01/21")))
        assertThat(loaded?.title, `is`("new title"))
        assertThat(loaded?.amount, `is`(20))
    }

    @Test
    fun deleteTaskByIdAndGettingTasks() = runBlockingTest {
        // Given a task inserted
        val task = Task(DateUtil.convertStringToDate("02/01/21"),"title", 15)
        database.taskDao().insertTask(task)

        // When deleting a task by id
        database.taskDao().deleteTaskById(task.id)

        // THEN - The list is empty
        val tasks = database.taskDao().getTasks()
        assertThat(tasks.isEmpty(), `is`(true))
    }

    @Test
    fun deleteAllTasksAndGettingTasks() = runBlockingTest {
        // Given a task inserted
        database.taskDao().insertTask(Task(DateUtil.convertStringToDate("02/01/21"),"title", 15))

        // When deleting all tasks
        database.taskDao().deleteAllTasks()

        // THEN - The list is empty
        val tasks = database.taskDao().getTasks()
        assertThat(tasks.isEmpty(), `is`(true))
    }

}
