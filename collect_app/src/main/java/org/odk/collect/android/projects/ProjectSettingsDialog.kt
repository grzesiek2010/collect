package org.odk.collect.android.projects

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import org.odk.collect.android.R
import org.odk.collect.android.activities.AboutActivity
import org.odk.collect.android.databinding.ProjectSettingsDialogLayoutBinding
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.preferences.dialogs.AdminPasswordDialogFragment
import org.odk.collect.android.preferences.keys.MetaKeys
import org.odk.collect.android.preferences.screens.AdminPreferencesActivity
import org.odk.collect.android.preferences.screens.GeneralPreferencesActivity
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.utilities.AdminPasswordProvider
import org.odk.collect.android.utilities.DialogUtils
import org.odk.collect.android.utilities.ToastUtils
import javax.inject.Inject

class ProjectSettingsDialog : DialogFragment() {

    @Inject
    lateinit var adminPasswordProvider: AdminPasswordProvider

    @Inject
    lateinit var projectsRepository: ProjectsRepository

    @Inject
    lateinit var settingsProvider: SettingsProvider

    private lateinit var binding: ProjectSettingsDialogLayoutBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding = ProjectSettingsDialogLayoutBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inflateListOfInActiveProjects()

        binding.closeIcon.setOnClickListener {
            dismiss()
        }

        binding.generalSettingsButton.setOnClickListener {
            startActivity(Intent(requireContext(), GeneralPreferencesActivity::class.java))
            dismiss()
        }

        binding.adminSettingsButton.setOnClickListener {
            if (adminPasswordProvider.isAdminPasswordSet) {
                val args = Bundle().also {
                    it.putSerializable(AdminPasswordDialogFragment.ARG_ACTION, AdminPasswordDialogFragment.Action.ADMIN_SETTINGS)
                }
                DialogUtils.showIfNotShowing(AdminPasswordDialogFragment::class.java, args, requireActivity().supportFragmentManager)
            } else {
                startActivity(Intent(requireContext(), AdminPreferencesActivity::class.java))
            }
            dismiss()
        }

        binding.addProjectButton.setOnClickListener {
            DialogUtils.showIfNotShowing(AddProjectDialog::class.java, requireActivity().supportFragmentManager)
            dismiss()
        }

        binding.aboutButton.setOnClickListener {
            startActivity(Intent(requireContext(), AboutActivity::class.java))
            dismiss()
        }
    }

    private fun inflateListOfInActiveProjects() {
        binding.topDivider.visibility = if (projectsRepository.getAll().isEmpty()) GONE else VISIBLE

        projectsRepository.getAll().forEach { project ->
            val projectView = LayoutInflater.from(context).inflate(R.layout.project_list_item, null)

            projectView.setOnClickListener {
                switchProject(project)
            }

            projectView.findViewById<TextView>(R.id.project_icon).apply {
                (background as GradientDrawable).setColor(Color.parseColor(project.color))
                text = project.icon
            }
            projectView.findViewById<TextView>(R.id.project_name).text = project.name

            binding.projectList.addView(projectView)
        }
    }

    private fun switchProject(project: Project) {
        settingsProvider.getMetaSettings().save(MetaKeys.CURRENT_PROJECT_ID, project.uuid)
        dismiss()
        ToastUtils.showLongToast(getString(R.string.switched_project, project.name))
    }
}
