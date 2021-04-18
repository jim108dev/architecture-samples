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
package com.example.android.architecture.blueprints.todoapp.addedittask

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.android.architecture.blueprints.todoapp.EventObserver
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.databinding.AddEditTaskFragBinding
import com.example.android.architecture.blueprints.todoapp.tasks.ADD_EDIT_RESULT_OK
import com.example.android.architecture.blueprints.todoapp.util.DateUtil
import com.example.android.architecture.blueprints.todoapp.util.getViewModelFactory
import com.example.android.architecture.blueprints.todoapp.util.setupRefreshLayout
import com.example.android.architecture.blueprints.todoapp.util.setupSnackbar
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.add_edit_task_frag.*
import java.text.SimpleDateFormat
import java.util.*


/**
 * Main UI for the add task screen. Users can enter a task title and description.
 */
class AddEditTaskFragment : Fragment() {

    private lateinit var viewDataBinding: AddEditTaskFragBinding

    private val args: AddEditTaskFragmentArgs by navArgs()

    private val viewModel by viewModels<AddEditTaskViewModel> { getViewModelFactory() }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.add_edit_task_frag, container, false)
        viewDataBinding = AddEditTaskFragBinding.bind(root).apply {
            this.viewmodel = viewModel
        }
        // Set the lifecycle owner to the lifecycle of the view
        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner
        setHasOptionsMenu(true)
        return viewDataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupSnackbar()
        setupNavigation()
        this.setupRefreshLayout(viewDataBinding.refreshLayout)
        viewModel.start(args.taskId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        openPicker()
        add_position_date.setOnClickListener{
            openPicker()
        }
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setupSnackbar() {
        view?.setupSnackbar(this, viewModel.snackbarText, Snackbar.LENGTH_SHORT)
    }

    private fun setupNavigation() {
        viewModel.taskUpdatedEvent.observe(this, EventObserver {
            val action = AddEditTaskFragmentDirections
                    .actionAddEditTaskFragmentToTasksFragment(ADD_EDIT_RESULT_OK)
            findNavController().navigate(action)
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_delete -> {
                viewModel.deleteTask()
                true
            }
            else -> false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (args.taskId != null) {
            inflater.inflate(R.menu.edit_task_fragment_menu, menu)
        }
    }

    private fun openPicker() {
        val myCalendar = Calendar.getInstance()
        val date = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->

            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, monthOfYear)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            add_position_date.text = DateUtil.convertDateToString(myCalendar.time)
        }

        DatePickerDialog(context, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show()
    }
}
