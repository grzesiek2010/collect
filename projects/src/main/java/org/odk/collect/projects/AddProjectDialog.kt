package org.odk.collect.projects

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import org.odk.collect.material.MaterialFullScreenDialogFragment
import org.odk.collect.projects.databinding.AddProjectDialogLayoutBinding
import org.odk.collect.shared.Validator
import javax.inject.Inject

class AddProjectDialog : MaterialFullScreenDialogFragment() {

    @Inject
    lateinit var projectsRepository: ProjectsRepository

    private lateinit var binding: AddProjectDialogLayoutBinding

    private var listener: AddProjectDialogListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val provider = context.applicationContext as ProjectsDependencyComponentProvider
        provider.projectsDependencyComponent.inject(this)

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

        binding.urlInputText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (Validator.isUrlValid(s.toString())) {
                    binding.urlInputText.error = null
                    binding.addButton.isEnabled = true
                } else {
                    binding.urlInputText.error = getString(R.string.url_error)
                    binding.addButton.isEnabled = false
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        binding.addButton.setOnClickListener {
            val generatedProjectDetails = ProjectDetailsGenerator.generateProjectDetails(getUrl())
            val newProject = Project.New(generatedProjectDetails.projectName, generatedProjectDetails.projectIcon, generatedProjectDetails.projectColor)
            val savedProject = projectsRepository.save(newProject)

            listener?.onProjectAdded(savedProject)
            dismiss()
        }
    }

    override fun onCloseClicked() {
    }

    override fun onBackPressed() {
        dismiss()
    }

    override fun getToolbar(): Toolbar? {
        return binding.toolbar
    }

    private fun setUpToolbar() {
        toolbar?.setTitle(R.string.add_project)
        toolbar?.navigationIcon = null
    }

    private fun getUrl() = binding.url.editText?.text?.trim().toString()

    interface AddProjectDialogListener {
        fun onProjectAdded(project: Project.Saved)
    }
}
