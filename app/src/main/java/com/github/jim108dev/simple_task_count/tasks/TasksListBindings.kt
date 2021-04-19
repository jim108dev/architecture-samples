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

import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.jim108dev.simple_task_count.data.Task
import com.github.jim108dev.simple_task_count.util.DateUtil
import java.util.*


@BindingAdapter("app:items")
fun setItems(listView: RecyclerView, items: List<Task>) {
    (listView.adapter as TasksAdapter).submitList(items)
}

@BindingAdapter("app:date")
fun setDate(view: TextView, date: Date) {
    view.text = DateUtil.convertDateToString(date)
}

@BindingAdapter("app:int")
fun setInt(view: TextView, n: Int) {
    view.text = n.toString()
}


