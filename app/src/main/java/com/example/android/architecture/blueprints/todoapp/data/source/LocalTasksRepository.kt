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
package com.example.android.architecture.blueprints.todoapp.data.source

import com.example.android.architecture.blueprints.todoapp.data.Result
import com.example.android.architecture.blueprints.todoapp.data.Result.Error
import com.example.android.architecture.blueprints.todoapp.data.Result.Success
import com.example.android.architecture.blueprints.todoapp.data.Task
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * Concrete implementation to load tasks from the data sources into a cache.
 *
 * To simplify the sample, this repository only uses the local data source only if the remote
 * data source fails. Remote is the source of truth.
 */
class LocalTasksRepository(
    private val tasksRemoteDataSource: TasksDataSource,
    private val tasksLocalDataSource: TasksDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : TasksRepository {

    private var cachedTasks: ConcurrentMap<String, Task>? = null

    override suspend fun getTasks(forceUpdate: Boolean): Result<List<Task>> {

        return withContext(ioDispatcher) {
            // Respond immediately with cache if available and not dirty
            if (!forceUpdate) {
                cachedTasks?.let { cachedTasks ->
                    return@withContext Success(cachedTasks.values.sortedBy { it.date })
                }
            }

            val newTasks = fetchTasksFromLocal(forceUpdate)

            (newTasks as? Success)?.let { refreshCache(it.data) }

            cachedTasks?.values?.let { tasks ->
                return@withContext Success(tasks.sortedBy { it.date })
            }

            (newTasks as? Success)?.let {
                if (it.data.isEmpty()) {
                    return@withContext Success(it.data)
                }
            }

            return@withContext Error(Exception("Illegal state"))
        }
    }

    private suspend fun fetchTasksFromLocal(forceUpdate: Boolean): Result<List<Task>> {
        val localTasks = tasksLocalDataSource.getTasks()
        if (localTasks is Success) return localTasks
        return Error(Exception("Error fetching from  local"))
    }

    /**
     * Relies on [getTasks] to fetch data and picks the task with the same ID.
     */
    override suspend fun getTask(taskId: String, forceUpdate: Boolean): Result<Task> {

        return withContext(ioDispatcher) {
            // Respond immediately with cache if available
            if (!forceUpdate) {
                getTaskWithId(taskId)?.let {
                    return@withContext Success(it)
                }
            }

            val newTask = fetchTaskFromLocal(taskId)

            // Refresh the cache with the new tasks
            (newTask as? Success)?.let { cacheTask(it.data) }

            return@withContext newTask
        }
    }

    private suspend fun fetchTaskFromLocal(
        taskId: String
    ): Result<Task> {

        val localTasks = tasksLocalDataSource.getTask(taskId)
        if (localTasks is Success) return localTasks
        return Error(Exception("Error fetching from remote and local"))
    }

    override suspend fun saveTask(task: Task) {
        // Do in memory cache update to keep the app UI up to date
        cacheAndPerform(task) {
            coroutineScope {
                launch { tasksLocalDataSource.saveTask(it) }
            }
        }
    }

    override suspend fun deleteAllTasks() {
        withContext(ioDispatcher) {
            coroutineScope {
                launch { tasksLocalDataSource.deleteAllTasks() }
            }
        }
        cachedTasks?.clear()
    }

    override suspend fun deleteTask(taskId: String) {
        coroutineScope {
            launch { tasksLocalDataSource.deleteTask(taskId) }
        }

        cachedTasks?.remove(taskId)
    }

    private fun refreshCache(tasks: List<Task>) {
        cachedTasks?.clear()
        tasks.sortedBy { it.id }.forEach {
            cacheAndPerform(it) {}
        }
    }

    private fun getTaskWithId(id: String) = cachedTasks?.get(id)

    private fun cacheTask(task: Task): Task {
        val cachedTask = Task(task.date, task.title, task.amount, task.id)
        // Create if it doesn't exist.
        if (cachedTasks == null) {
            cachedTasks = ConcurrentHashMap()
        }
        cachedTasks?.put(cachedTask.id, cachedTask)
        return cachedTask
    }

    private inline fun cacheAndPerform(task: Task, perform: (Task) -> Unit) {
        val cachedTask = cacheTask(task)
        perform(cachedTask)
    }
}
