package org.odk.collect.android.projects

import android.content.Context
import androidx.core.content.ContextCompat
import org.json.JSONObject
import org.odk.collect.android.R
import org.odk.collect.android.configure.qr.AppConfigurationKeys
import org.odk.collect.projects.Project
import java.net.URL
import java.util.*
import java.util.regex.Pattern
import kotlin.math.abs

class ProjectDetailsCreator(private val context: Context) {

    fun getProject(url: String, projectDetailsJson: JSONObject = JSONObject()): Project {
        val projectName = if (projectDetailsJson.has(AppConfigurationKeys.PROJECT_NAME) && projectDetailsJson.get(AppConfigurationKeys.PROJECT_NAME).toString().isNotBlank()) {
            projectDetailsJson.get(AppConfigurationKeys.PROJECT_NAME).toString()
        } else {
            getProjectNameFromUrl(url)
        }

        val projectIcon = if (projectDetailsJson.has(AppConfigurationKeys.PROJECT_ICON) && projectDetailsJson.get(AppConfigurationKeys.PROJECT_ICON).toString().isNotBlank()) {
            val value = projectDetailsJson.get(AppConfigurationKeys.PROJECT_ICON).toString()
            if (Character.codePointCount(value, 0, value.length) == 1) {
                value.toUpperCase(Locale.US)
            } else {
                getFirstSing(value)
            }
        } else {
            projectName.first().toUpperCase().toString()
        }

        val projectColor = if (projectDetailsJson.has(AppConfigurationKeys.PROJECT_COLOR) && isProjectColorValid(projectDetailsJson.get(AppConfigurationKeys.PROJECT_COLOR).toString())) {
            projectDetailsJson.get(AppConfigurationKeys.PROJECT_COLOR).toString()
        } else {
            getProjectColorFromProjectName(projectName)
        }

        return Project.New(projectName, projectIcon, projectColor)
    }

    private fun getProjectNameFromUrl(url: String): String {
        return if (url.isBlank() || url.startsWith(context.getString(R.string.default_server_url))) {
            Project.DEMO_PROJECT_NAME
        } else {
            try {
                URL(url).host
            } catch (e: Exception) {
                "Project"
            }
        }
    }

    private fun getProjectColorFromProjectName(projectName: String): String {
        if (projectName == Project.DEMO_PROJECT_NAME) {
            return Project.DEMO_PROJECT_COLOR
        }

        val colorId = (abs(projectName.hashCode()) % 15) + 1
        val colorName = "color$colorId"
        val colorValue = context.resources.getIdentifier(colorName, "color", context.packageName)

        return "#${Integer.toHexString(ContextCompat.getColor(context, colorValue)).substring(2)}"
    }

    private fun isProjectColorValid(hexColor: String): Boolean {
        return Pattern
            .compile("^#([a-fA-F0-9]{6}|[a-fA-F0-9]{3})\$", Pattern.CASE_INSENSITIVE)
            .matcher(hexColor)
            .matches()
    }

    private fun getFirstSing(value: String): String {
        return if (Character.codePointCount(value, 0, value.length) == 1) {
            value
        } else {
            getFirstSing(value.substring(0, value.length - 1))
        }
    }
}
