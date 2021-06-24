package org.odk.collect.android.projects

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.configure.qr.AppConfigurationKeys
import org.odk.collect.projects.Project

@RunWith(AndroidJUnit4::class)
class ProjectDetailsCreatorTest {

    val projectDetailsCreator = ProjectDetailsCreator(ApplicationProvider.getApplicationContext())

    @Test
    fun `If project name is included in project details should be used`() {
        val projectDetails = JSONObject()
            .put(AppConfigurationKeys.PROJECT_NAME, "Project X")

        assertThat(projectDetailsCreator.getProject("", projectDetails).name, `is`("Project X"))
    }

    @Test
    fun `If project name is not included in project details or empty should be generated based on url`() {
        // Case1: not included
        assertThat(projectDetailsCreator.getProject("https://my-server.com").name, `is`("my-server.com"))

        // Case2: empty
        val projectDetails = JSONObject()
            .put(AppConfigurationKeys.PROJECT_NAME, " ")

        assertThat(projectDetailsCreator.getProject("https://my-server.com", projectDetails).name, `is`("my-server.com"))
    }

    @Test
    fun `If project icon is included in project details should be used`() {
        // Case1: normal chars
        var projectDetails = JSONObject()
            .put(AppConfigurationKeys.PROJECT_ICON, "X")

        assertThat(projectDetailsCreator.getProject("", projectDetails).icon, `is`("X"))

        // Case2: emoticons
        projectDetails = JSONObject()
            .put(AppConfigurationKeys.PROJECT_ICON, "\uD83D\uDC22")

        assertThat(projectDetailsCreator.getProject("", projectDetails).icon, `is`("\uD83D\uDC22"))
    }

    @Test
    fun `If project icon is not included in project details or empty should be generated based on project name`() {
        // Case1: not included
        var projectDetails = JSONObject()
            .put(AppConfigurationKeys.PROJECT_NAME, "Project X")

        assertThat(projectDetailsCreator.getProject("", projectDetails).icon, `is`("P"))

        // Case2: empty
        projectDetails = JSONObject()
            .put(AppConfigurationKeys.PROJECT_NAME, "My Project X")
            .put(AppConfigurationKeys.PROJECT_ICON, " ")

        assertThat(projectDetailsCreator.getProject("", projectDetails).icon, `is`("M"))
    }

    @Test
    fun `If project icon is included in project details but longer than one sign, only the first sign should be used`() {
        // Case1: normal chars
        var projectDetails = JSONObject()
            .put(AppConfigurationKeys.PROJECT_ICON, "XX")

        assertThat(projectDetailsCreator.getProject("https://my-server.com", projectDetails).icon, `is`("X"))

        // Case2: emoticons
        projectDetails = JSONObject()
            .put(AppConfigurationKeys.PROJECT_ICON, "\uD83D\uDC22XX")

        assertThat(projectDetailsCreator.getProject("https://my-server.com", projectDetails).icon, `is`("\uD83D\uDC22"))
    }

    @Test
    fun `If project color is included in project details should be used`() {
        val projectDetails = JSONObject()
            .put(AppConfigurationKeys.PROJECT_COLOR, "#cccccc")

        assertThat(projectDetailsCreator.getProject("", projectDetails).color, `is`("#cccccc"))
    }

    @Test
    fun `If project color is not included in project details should be generated based on project name`() {
        // Case1: any project name
        var projectDetails = JSONObject()
            .put(AppConfigurationKeys.PROJECT_NAME, "Project X")

        assertThat(projectDetailsCreator.getProject("", projectDetails).color, `is`("#9f50b0"))

        // Case2: demo project
        projectDetails = JSONObject()
            .put(AppConfigurationKeys.PROJECT_NAME, Project.DEMO_PROJECT_NAME)

        assertThat(projectDetailsCreator.getProject("", projectDetails).color, `is`(Project.DEMO_PROJECT_COLOR))
    }

    @Test
    fun `If project color is included in project details but invalid should be generated based on project name`() {
        val projectDetails = JSONObject()
            .put(AppConfigurationKeys.PROJECT_NAME, "Project X")
            .put(AppConfigurationKeys.PROJECT_COLOR, "#cc")

        assertThat(projectDetailsCreator.getProject("", projectDetails).color, `is`("#9f50b0"))
    }

    @Test
    fun `Test generating project name from various urls`() {
        assertThat(projectDetailsCreator.getProject("https://my-project.com").name, `is`("my-project.com"))
        assertThat(projectDetailsCreator.getProject("https://your-project.com/one").name, `is`("your-project.com"))
        assertThat(projectDetailsCreator.getProject("http://www.my-project.com").name, `is`("www.my-project.com"))
        assertThat(projectDetailsCreator.getProject("https://demo.getodk.org").name, `is`(Project.DEMO_PROJECT_NAME))
        assertThat(projectDetailsCreator.getProject("").name, `is`(Project.DEMO_PROJECT_NAME))
        assertThat(projectDetailsCreator.getProject("something").name, `is`("Project")) // default project name for invalid urls
    }

    @Test
    fun `Generated project color should be the same for identical project names`() {
        assertThat(projectDetailsCreator.getProject("https://my-project.com").color, `is`(projectDetailsCreator.getProject("https://my-project.com").color))
        assertThat(projectDetailsCreator.getProject("https://your-project.com/one").color, `is`(projectDetailsCreator.getProject("https://your-project.com/one").color))
        assertThat(projectDetailsCreator.getProject("http://www.my-project.com").color, `is`(projectDetailsCreator.getProject("http://www.my-project.com").color))
        assertThat(projectDetailsCreator.getProject("qwerty").color, `is`(projectDetailsCreator.getProject("something").color)) // default project color for invalid urls
    }

    @Test
    fun `Generated project color should be different for different project names`() {
        assertThat(projectDetailsCreator.getProject("https://my-project.com").color, not(projectDetailsCreator.getProject("http://www.my-project.com").color))
        assertThat(projectDetailsCreator.getProject("https://your-project.com/one").color, not(projectDetailsCreator.getProject("http://www.my-project.com").color))
        assertThat(projectDetailsCreator.getProject("http://www.my-project.com").color, not(projectDetailsCreator.getProject("https://your-project.com/one").color))
    }
}
