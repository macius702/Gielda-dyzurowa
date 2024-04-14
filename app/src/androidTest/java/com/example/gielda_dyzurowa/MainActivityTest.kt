package com.example.gielda_dyzurowa

import android.util.Log
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.Role

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
    fun testLoginPage() {
        // Click on the navigation item
        composeTestRule.onNodeWithText("Login").performClick()

        val username = "abba"
        val password = "alamakota"

        // Enter the username
        composeTestRule.onNodeWithText("Username")
            .performTextInput(username)

        // Enter the password
        composeTestRule.onNodeWithText("Password")
            .performTextInput(password)

        // Initiate the login process by clicking the login button.
        // Note that there are two elements with the text "Login" in the current UI.
        // However, the one located in the drawer is a ListItem, not a Button.
        // Therefore, the click action will be performed on the Button element with the text "Login".
        composeTestRule.onNode(
            matcher = hasText("Login") and hasButton()
        ).performClick()

        // Wait until the logged text is displayed
        composeTestRule.waitUntil {
            try {
                composeTestRule.onNodeWithText("Logged in as abba").assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }

    // This function logs all nodes in the Compose hierarchy that have any text.
    fun logAllNodesWithAnyText() {
        // Find all nodes that have any text
        val nodes = composeTestRule.onAllNodes(hasAnyText())

        nodes.fetchSemanticsNodes().forEach { node ->
            Log.d("MainActivityTest", "Node:")
            node.config.iterator().forEach { entry ->
                // Extract the key and value
                val key = entry.key.name
                val value = entry.value
                Log.d("MainActivityTest", "\t $key: $value")
            }
        }
    }
}

// Define a SemanticsMatcher that matches nodes with any text
fun hasAnyText() = SemanticsMatcher("has any text") {
    it.config.contains(SemanticsProperties.Text) }

// Define a SemanticsMatcher that matches nodes with a button role, without using 'hasSemanticNodeWithRole'
fun hasButton() = SemanticsMatcher("has button") { node ->
    node.config.any { entry ->
        val key = entry.key.name
        val value = entry.value
        Log.d("MainActivityTest", "\t $key: $value")
        key == "Role" && value == Role.Button
    }
}
