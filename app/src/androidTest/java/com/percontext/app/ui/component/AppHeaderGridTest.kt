package com.percontext.app.ui.component

import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.percontext.app.ui.theme.PerContextTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AppHeaderGridTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun compactHeaderKeepsTheSettingsTouchTargetAndClickAction() {
        var opened = false
        composeRule.setContent {
            PerContextTheme { CompactPageHeader(onOpenSettings = { opened = true }) }
        }
        composeRule.onNodeWithTag("daily_header").assertHeightIsEqualTo(48.dp)
        composeRule.onNodeWithTag("app_header_settings")
            .assertWidthIsEqualTo(48.dp).assertHeightIsEqualTo(48.dp)
        composeRule.onNodeWithContentDescription("打开设置").performClick()
        composeRule.runOnIdle { assertTrue(opened) }
    }
}
