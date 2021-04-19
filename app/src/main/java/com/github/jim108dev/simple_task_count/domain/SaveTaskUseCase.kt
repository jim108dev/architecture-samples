package com.github.jim108dev.simple_task_count.domain

import com.github.jim108dev.simple_task_count.data.Task
import com.github.jim108dev.simple_task_count.data.source.TasksRepository
import com.github.jim108dev.simple_task_count.util.wrapEspressoIdlingResource

class SaveTaskUseCase(
    private val tasksRepository: TasksRepository
) {
    suspend operator fun invoke(task: Task) {

        wrapEspressoIdlingResource {
            return tasksRepository.saveTask(task)
        }
    }

}