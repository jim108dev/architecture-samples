package com.github.jim108dev.simple_task_count.domain

import com.github.jim108dev.simple_task_count.data.Result
import com.github.jim108dev.simple_task_count.data.Task
import com.github.jim108dev.simple_task_count.data.source.TasksRepository
import com.github.jim108dev.simple_task_count.util.wrapEspressoIdlingResource

class GetTaskUseCase(
    private val tasksRepository: TasksRepository
) {
    suspend operator fun invoke(taskId: String, forceUpdate: Boolean = false): Result<Task> {

        wrapEspressoIdlingResource {
            return tasksRepository.getTask(taskId, forceUpdate)
        }
    }

}