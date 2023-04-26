package org.odk.collect.android.feature.external

import android.content.Intent
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.android.external.InstancesContract
import org.odk.collect.android.support.ContentProviderUtils
import org.odk.collect.android.support.pages.AppClosedPage
import org.odk.collect.android.support.pages.FormHierarchyPage
import org.odk.collect.android.support.pages.OkDialog
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain

@RunWith(AndroidJUnit4::class)
class InstanceEditActionTest {

    private val rule = CollectTestRule()

    @get:Rule
    val chain: RuleChain = TestRuleChain.chain()
        .around(rule)

    @Test
    fun editingInstance_andSaving_returnsInstanceURI() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .swipeToEndScreen()
            .clickFinalize()

        val instanceId = ContentProviderUtils.getInstanceDatabaseId("DEMO", "one_question")
        val uri = InstancesContract.getUri("DEMO", instanceId)

        val intent = Intent(Intent.ACTION_EDIT).also { it.data = uri }
        val result = rule.launchForResult(intent, FormHierarchyPage("One Question")) {
            it.clickGoToStart()
                .answerQuestion("what is your age", "32")
                .swipeToEndScreen()
                .clickFinalize(AppClosedPage())
        }

        assertThat(result.resultData.data, equalTo(uri))
    }

    @Test
    fun editingInstance_andIgnoringChanges_returnsInstanceURI() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .swipeToEndScreen()
            .clickFinalize()

        val instanceId = ContentProviderUtils.getInstanceDatabaseId("DEMO", "one_question")
        val uri = InstancesContract.getUri("DEMO", instanceId)

        val intent = Intent(Intent.ACTION_EDIT).also { it.data = uri }
        val result = rule.launchForResult(intent, FormHierarchyPage("One Question")) {
            it.clickGoToStart()
                .pressBackAndDiscardChanges(AppClosedPage())
        }

        assertThat(result.resultData.data, equalTo(uri))
    }

    @Test
    fun whenInstanceIsNotCurrentProject_showsWarningAndExits() {
        rule.startAtMainMenu()
            .copyAndSyncForm("one-question.xml")
            .startBlankForm("One Question")
            .swipeToEndScreen()
            .clickFinalize()
            .addAndSwitchToProject("https://example.com")

        val instanceId = ContentProviderUtils.getInstanceDatabaseId("DEMO", "one_question")
        val uri = InstancesContract.getUri("DEMO", instanceId)

        val intent = Intent(Intent.ACTION_EDIT).also { it.data = uri }
        rule.launch(intent, OkDialog())
            .assertText(R.string.wrong_project_selected_for_form)
            .clickOK(AppClosedPage())
    }

    @Test
    fun whenUriDoesNotHaveProjectId_andCurrentProjectIsFirstOne_opensForm() {
        rule.startAtMainMenu()
            .copyAndSyncForm("one-question.xml")
            .startBlankForm("One Question")
            .swipeToEndScreen()
            .clickFinalize()
            .addAndSwitchToProject("https://example.com")
            .openProjectSettingsDialog()
            .selectProject("Demo project")

        val instanceId = ContentProviderUtils.getInstanceDatabaseId("DEMO", "one_question")
        val uri = InstancesContract.getUri("DEMO", instanceId)
        val uriWithoutProjectId = Uri.Builder()
            .scheme(uri.scheme)
            .authority(uri.authority)
            .path(uri.path)
            .query(null)
            .build()

        val intent = Intent(Intent.ACTION_EDIT).also { it.data = uriWithoutProjectId }
        rule.launch(intent, FormHierarchyPage("One Question"))
    }

    @Test
    fun whenUriDoesNotHaveProjectId_andCurrentProjectIsNotFirstOne_showsWarningAndExits() {
        rule.startAtMainMenu()
            .copyAndSyncForm("one-question.xml")
            .startBlankForm("One Question")
            .swipeToEndScreen()
            .clickFinalize()
            .addAndSwitchToProject("https://example.com")

        val instanceId = ContentProviderUtils.getInstanceDatabaseId("DEMO", "one_question")
        val uri = InstancesContract.getUri("DEMO", instanceId)
        val uriWithoutProjectId = Uri.Builder()
            .scheme(uri.scheme)
            .authority(uri.authority)
            .path(uri.path)
            .query(null)
            .build()

        val intent = Intent(Intent.ACTION_EDIT).also { it.data = uriWithoutProjectId }
        rule.launch(intent, OkDialog())
            .assertText(R.string.wrong_project_selected_for_form)
            .clickOK(AppClosedPage())
    }
}
