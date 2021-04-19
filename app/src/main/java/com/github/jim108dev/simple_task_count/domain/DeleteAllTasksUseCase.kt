package com.github.jim108dev.simple_task_count.domain

import com.github.jim108dev.simple_task_count.data.source.TasksRepository
import com.github.jim108dev.simple_task_count.util.wrapEspressoIdlingResource

class DeleteAllTasksUseCase(
    private val tasksRepository: TasksRepository
) {
    suspend operator fun invoke() {

        wrapEspressoIdlingResource {
            return tasksRepository.deleteAllTasks()
        }
    }

}