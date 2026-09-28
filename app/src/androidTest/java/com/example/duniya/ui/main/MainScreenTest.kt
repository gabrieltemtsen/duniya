package com.example.duniya.ui.main

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.duniya.theme.DuniyaTheme
import org.junit.Rule
import org.junit.Test

class MainScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun mainScreen_displaysAirgappedTelemetryAndResearchHeader() {
        composeTestRule.setContent {
            DuniyaTheme {
                MainScreen(onItemClick = {})
            }
        }

        composeTestRule.onNodeWithText("DUNIYA • OFFLINE AI LAB", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("AIRGAPPED • 0 NET", substring = true).assertIsDisplayed()
    }
}
