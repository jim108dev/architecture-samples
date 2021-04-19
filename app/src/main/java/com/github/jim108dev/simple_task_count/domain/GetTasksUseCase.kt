package com.github.jim108dev.simple_task_count.domain

import com.github.jim108dev.simple_task_count.data.Result
import com.github.jim108dev.simple_task_count.data.Result.Success
import com.github.jim108dev.simple_task_count.data.Task
import com.github.jim108dev.simple_task_count.data.source.TasksRepository
import com.github.jim108dev.simple_task_count.tasks.TasksFilterType
import com.github.jim108dev.simple_task_count.tasks.TasksFilterType.DEBIT
import com.github.jim108dev.simple_task_count.tasks.TasksFilterType.ALL
import com.github.jim108dev.simple_task_count.tasks.TasksFilterType.CREDIT
import com.github.jim108dev.simple_task_count.util.wrapEspressoIdlingResource

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

                for (task in tasks) {
                    when (currentFiltering) {
                        DEBIT -> if (task.amount >= 0) {
                            tasksToShow.add(task)
                        }
                        CREDIT -> if (task.amount < 0) {
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