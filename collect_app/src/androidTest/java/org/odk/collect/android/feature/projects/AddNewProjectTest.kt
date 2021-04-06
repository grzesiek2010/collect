package org.odk.collect.android.feature.projects

import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.projects.Project
import org.odk.collect.android.support.CollectTestRule
import org.odk.collect.android.support.TestRuleChain

class AddNewProjectTest {

    val rule = CollectTestRule()

    @get:Rule var chain: RuleChain = TestRuleChain.chain().around(rule)

    @Test
    fun addProjectTest() {
        rule.mainMenu()
            .openProjectSettingsDialog()
            .clickAddProject()
            .inputProjectName("Project 1")
            .inputProjectIcon("X")
            .inputProjectColor("#0000FF")
            .addProject()

        rule.mainMenu()
            .openProjectSettingsDialog()
            .assertInactiveProject(Project("Project 1", "X", "#0000FF", "1"))
    }
}
