package com.example.android.architecture.blueprints.todoapp.domain

import com.example.android.architecture.blueprints.todoapp.data.Result
import com.example.android.architecture.blueprints.todoapp.data.Result.Success
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFilterType
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFilterType.DEBIT
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFilterType.ALL
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFilterType.CREDIT
import com.example.android.architecture.blueprints.todoapp.util.wrapEspressoIdlingResource

class GetTasksUseCase(
    private val tasksRepository: TasksRepository
) {
    suspend operator fun invoke(
        forceUpdate: Boolean = false,
        currentFiltering: TasksFilterType = ALL
    ): Result<List<Task>> {

        wrapEspressoIdlingResource {

            val tasksResult = tasksRepository.getTasks(forceUpdate)

            // Filter tasks
            if (tasksResult is Success && currentFiltering != ALL) {
                val tasks = tasksResult.data

                val tasksToShow = mutableListOf<Task>()
                // We filter the tasks based on the requestType
                for (task in tasks) {
                    when (currentFiltering) {
                        DEBIT -> if (!task.amount.startsWith("-")) {
                            tasksToShow.add(task)
                        }
                        CREDIT -> if (task.amount.startsWith("-")) {
                            tasksToShow.add(task)
                        }
                        else -> NotImplementedError()
                    }
                }
                return Success(tasksToShow)
            }
            return tasksResult
        }
    }

}