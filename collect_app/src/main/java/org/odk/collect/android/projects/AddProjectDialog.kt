package org.odk.collect.android.projects

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import org.odk.collect.android.R
import org.odk.collect.android.databinding.AddProjectDialogLayoutBinding
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.androidshared.OneSignTextWatcher
import org.odk.collect.material.MaterialFullScreenDialogFragment
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository
import javax.inject.Inject

class AddProjectDialog : MaterialFullScreenDialogFragment() {

    @Inject
    lateinit var projectsRepository: ProjectsRepository

    private lateinit var binding: AddProjectDialogLayoutBinding

    private var listener: AddProjectDialogListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)

        if (context is AddProjectDialogListener) {
            listener = context
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = AddProjectDialogLayoutBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()

        binding.projectIconInputText.addTextChangedListener(OneSignTextWatcher(binding.projectIconInputText))

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        binding.addButton.setOnClickListener {
            projectsRepository.add(Project(getProjectName(), getProjectIcon(), getProjectColor()))
            listener?.onProjectAdded()
            dismiss()
        }
    }

    override fun onCloseClicked() {
    }

    override fun onBackPressed() {
        dismiss()
    }

    override fun getToolbar(): Toolbar? {
        return binding.addProjectToolbar.toolbar
    }

    private fun setUpToolbar() {
        toolbar?.setTitle(R.string.add_project)
        toolbar?.navigationIcon = null
    }

    private fun getProjectName() = binding.projectName.editText?.text?.trim().toString()

    private fun getProjectIcon() = binding.projectIcon.editText?.text?.trim().toString()

    private fun getProjectColor() = binding.projectColor.editText?.text?.trim().toString()

    interface AddProjectDialogListener {
        fun onProjectAdded()
    }
}
