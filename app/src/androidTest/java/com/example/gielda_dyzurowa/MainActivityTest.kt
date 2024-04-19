package com.example.gielda_dyzurowa

import android.util.Log
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gieldadyzurowa.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationDrawerTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testLoginPageOK() {
        navigateToLoginPage()

        val username = "abba"
        val password = "alamakota"

        enterCredentials(username, password)
        submitLoginForm()

        assertErrorMessageDisplayed("Logged in as $username")
    }

    @Test
    fun testLoginFailureWrongPassword() {
        navigateToLoginPage()

        val username = "abba"
        val wrongPassword = "wrong_password"

        enterCredentials(username, wrongPassword)
        submitLoginForm()

        assertErrorMessageDisplayed("Invalid username or password.")
    }

    @Test
    fun testLoginFailureWrongUsername() {
        navigateToLoginPage()

        val wrongUsername = "wrong_username"
        val password = "alamakota"

        enterCredentials(wrongUsername, password)
        submitLoginForm()

        assertErrorMessageDisplayed("Invalid username or password.")
    }

    // test on Remove Duty Vacancy:
    @Test
    fun testRemoveDutyVacancy() {
        navigateToLoginPage()

        val username = "H1"
        val password = "alamakota"

        enterCredentials(username, password)
        submitLoginForm()

        // Wait for the login to complete and show the Duty slots automatically
        Thread.sleep(1000)

        composeTestRule.waitUntil {
            try {
                composeTestRule.onNodeWithText("Required Specialty: Choroby wewnętrzne").assertExists()
                true
            } catch (e: AssertionError) {
                // If the node is not found, we will add it
                addDutyVacancy()

                Thread.sleep(3000)

                logAllNodesWithAnyText()
                composeTestRule.onNodeWithText("Required Specialty: Choroby wewnętrzne").assertExists()

                true
            }
        }
        composeTestRule.onNodeWithText("Remove").performClick()
        composeTestRule.onNodeWithText("Required Specialty: Choroby wewnętrzne").assertDoesNotExist()
        //TODO(mtlk): add Snack or Alert after  assertErrorMessageDisplayed("Duty Vacancy removed.")

        addDutyVacancy()

    }

    private fun addDutyVacancy() {
        publishDutyVacancy("2024-12-20", "20-8", "Choroby wewnętrzne")
    }

    private fun publishDutyVacancy(date: String, dutyHours: String, requiredSpecialty: String) {
        composeTestRule.onNodeWithText("Publish Duty Vacancy").performClick()
        composeTestRule.onNodeWithText("Date").performTextInput(date)
        composeTestRule.onNodeWithText("Duty Hours").performTextInput(dutyHours)
        composeTestRule.onNodeWithContentDescription("Dropdown menu").performClick()
        Thread.sleep(1000)
        composeTestRule.onNodeWithText(requiredSpecialty).performClick()
        Thread.sleep(1000)
        logAllNodesWithAnyText()

        composeTestRule.onNodeWithText("Publish").performClick()
        composeTestRule.waitForIdle()
    }

    private fun navigateToLoginPage() {
        composeTestRule.onNodeWithText("Login").performClick()
    }

    private fun enterCredentials(username: String, password: String) {
        composeTestRule.onNodeWithText("Username").performTextInput(username)
        composeTestRule.onNodeWithText("Password").performTextInput(password)
    }

    private fun submitLoginForm() {
        composeTestRule.onNode(
            matcher = hasText("Login") and hasButton()
        ).performClick()
    }

    private fun assertErrorMessageDisplayed(errorMessage: String) {
        composeTestRule.waitUntil {
            try {
                composeTestRule.onNodeWithText(errorMessage).assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }

    // This function logs all nodes in the Compose hierarchy that have any text.
    private fun logAllNodesWithAnyText() {
        // Find all nodes that have any text
        val nodes = composeTestRule.onAllNodes(hasAnyText())

        nodes.fetchSemanticsNodes().forEach { node ->
            Log.d("MainActivityTest", "Node:")
            node.config.iterator().forEach { entry ->
                val key = entry.key.name
                val value = entry.value
                Log.d("MainActivityTest", "\t $key: $value")
            }
        }
    }
}

// Define a SemanticsMatcher that matches nodes with any text
fun hasAnyText() = SemanticsMatcher("has any text") {
    it.config.contains(SemanticsProperties.Text)
}

// Define a SemanticsMatcher that matches nodes with a button role, without using 'hasSemanticNodeWithRole'
fun hasButton() = SemanticsMatcher("has button") { node ->
    node.config.any { entry ->
        entry.key.name == "Role" && entry.value == Role.Button
    }
}
