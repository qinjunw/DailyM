package com.percontext.app.feature.app

import android.graphics.Bitmap
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.platform.app.InstrumentationRegistry
import com.percontext.app.MainActivity
import com.percontext.app.R
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class BrandingIntegrationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun compactHomeHeaderClearsTheStatusBarAndOpensSettings() {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("recording_controls").fetchSemanticsNodes().isNotEmpty()
        }
        listOf("PerContext", "把想法留在当下", "喵记", "DailyM").forEach {
            composeRule.onNodeWithText(it).assertDoesNotExist()
        }
        composeRule.onNodeWithTag("daily_header").assertHeightIsEqualTo(48.dp)
        val settings = composeRule.onNodeWithContentDescription("打开设置").assertIsDisplayed()
        val settingsBounds = settings.fetchSemanticsNode().boundsInRoot
        val controlsBounds = composeRule.onNodeWithTag("recording_controls")
            .assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        composeRule.runOnIdle {
            val statusBarHeight = requireNotNull(
                ViewCompat.getRootWindowInsets(composeRule.activity.window.decorView),
            ).getInsets(WindowInsetsCompat.Type.statusBars()).top
            assertTrue(settingsBounds.top >= statusBarHeight)
            assertTrue(controlsBounds.top >= settingsBounds.bottom)
        }
        saveScreenshot("home")

        settings.performClick()
        composeRule.onNodeWithText("模型服务").assertIsDisplayed()
        val activity = composeRule.activity
        val aboutTitle = activity.getString(R.string.about_title, activity.getString(R.string.app_name))
        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText(aboutTitle))
        composeRule.onNodeWithText(aboutTitle).assertIsDisplayed()
        saveScreenshot("about")
        composeRule.runOnUiThread { activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.onNodeWithContentDescription("开始录音").assertIsDisplayed()
    }

    private fun saveScreenshot(name: String) {
        val directory = File(requireNotNull(composeRule.activity.getExternalFilesDir(null)), "branding-qa")
        assertTrue(directory.isDirectory || directory.mkdirs())
        val screenshot = requireNotNull(InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot())
        File(directory, "$name.png").outputStream().use {
            assertTrue(screenshot.compress(Bitmap.CompressFormat.PNG, 100, it))
        }
        screenshot.recycle()
    }
}
