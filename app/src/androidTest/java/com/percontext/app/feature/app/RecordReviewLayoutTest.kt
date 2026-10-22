package com.percontext.app.feature.app

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStore
import androidx.test.core.app.ApplicationProvider
import com.percontext.app.core.media.PlaybackController
import com.percontext.app.core.media.PlaybackState
import com.percontext.app.domain.appearance.AppTheme
import com.percontext.app.domain.appearance.AppearanceSettings
import com.percontext.app.domain.appearance.AppearanceSettingsRepository
import com.percontext.app.domain.appearance.ThemeMode
import com.percontext.app.domain.dailycontext.DailyContext
import com.percontext.app.domain.dailycontext.DailyContextContent
import com.percontext.app.domain.dailycontext.DailyContextRequestResult
import com.percontext.app.domain.dailycontext.DailyContextStatus
import com.percontext.app.domain.dailycontext.TranscribedRecordStamp
import com.percontext.app.domain.dailycontext.localDayRange
import com.percontext.app.domain.llm.LlmProviderPresets
import com.percontext.app.domain.llm.LlmSettings
import com.percontext.app.domain.model.RecordStatus
import com.percontext.app.domain.model.TranscriptStatus
import com.percontext.app.domain.model.VoiceRecord
import com.percontext.app.domain.recording.RecordingFeatureGateway
import com.percontext.app.domain.recording.RecordingSession
import com.percontext.app.feature.record.RecordViewModel
import com.percontext.app.feature.review.ReviewFeatureGateway
import com.percontext.app.feature.review.ReviewRecordingSummary
import com.percontext.app.feature.review.ReviewTimeProvider
import com.percontext.app.feature.review.ReviewTimeSnapshot
import com.percontext.app.feature.review.ReviewViewModel
import com.percontext.app.feature.settings.AppearanceViewModel
import com.percontext.app.feature.settings.ProviderSettingsGateway
import com.percontext.app.feature.settings.SettingsViewModel
import com.percontext.app.ui.theme.PerContextTheme
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class RecordReviewLayoutTest {
    @get:Rule
    val composeRule = createComposeRule()
    private val viewModels = ViewModelStore()
    private var density = 1f
    private var fontScale = 1f
    private lateinit var fixture: LayoutFixture

    @After
    fun clearViewModels() = composeRule.runOnUiThread { viewModels.clear() }

    @Test
    fun standardScreenPreservesCrossPageAnchorsAndBothScrolledLists() {
        showApp(width = 392, height = 800, fontScale = 1f, withContext = true)
        val original = anchors()
        evidence("normal-01-record", original)
        composeRule.onNodeWithTag("recent_records_list")
            .performScrollToNode(hasTestTag("recording_card_layout_8"))
        val recordPosition = bounds("recording_card_layout_8")
        assertAnchors(original, anchors())

        selectTab("回顾")
        assertAnchors(original, anchors())
        evidence("normal-02-review", anchors())
        val label = bounds("review_section_label")
        assertTrue(label.top >= bounds("pidan_perch").bottom)
        composeRule.onNodeWithText("刷新回顾").performScrollTo().assertIsDisplayed()
        val reviewPosition = scrollPosition("review_details_list")
        assertTrue(reviewPosition > 0f)
        assertAnchors(original, anchors())
        evidence("normal-03-review-scrolled", anchors())

        selectTab("记录")
        assertAnchors(original, anchors())
        assertRect(recordPosition, bounds("recording_card_layout_8"))
        evidence("normal-04-record-return", anchors())
        selectTab("回顾")
        assertAnchors(original, anchors())
        assertEquals(reviewPosition, scrollPosition("review_details_list"), 0.001f)
        evidence("normal-05-review-return", anchors())
        composeRule.onNodeWithTag("review_day_2026-08-27").performScrollTo().performClick()
        composeRule.onNodeWithTag("selected_day_date", useUnmergedTree = true).assertTextEquals("2026 年 8 月 27 日")
        selectTab("记录")
        selectTab("回顾")
        composeRule.onNodeWithTag("selected_day_date", useUnmergedTree = true).assertTextEquals("2026 年 8 月 27 日")
        assertAnchors(original, anchors())
    }

    @Test
    fun compactLargeTextKeepsSixthWeekAndDateSourceGenerateActionsReachable() {
        showApp(width = 360, height = 480, fontScale = 1.6f, withContext = false, today = 27)
        val original = anchors()
        evidence("compact-01-record", original)
        composeRule.onNodeWithTag("recent_records_list")
            .performScrollToNode(hasTestTag("recording_card_layout_8"))
        val recordPosition = bounds("recording_card_layout_8")
        selectTab("回顾")
        assertAnchors(original, anchors())
        evidence("compact-02-review-top", anchors())
        composeRule.onNodeWithTag("previous_month").performScrollTo().performClick()
        composeRule.onNodeWithText("2026 年 7 月").assertExists()
        composeRule.onNodeWithTag("next_month").performScrollTo().performClick()
        composeRule.onNodeWithText("2026 年 8 月").assertExists()
        composeRule.onNodeWithTag("review_day_2026-08-31").performScrollTo().performClick()
        assertFullyVisible("review_day_2026-08-31")
        composeRule.onNodeWithTag("selected_day_date", useUnmergedTree = true).assertTextEquals("2026 年 8 月 31 日")
        composeRule.onNodeWithTag("calendar_legend").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("已有回顾").assertIsDisplayed()
        composeRule.onNodeWithText("有录音").assertIsDisplayed()
        assertAnchors(original, anchors())
        evidence("compact-03-review-sixth-week", anchors())
        composeRule.onNodeWithText("生成回顾").performScrollTo().assertIsDisplayed().performClick()
        composeRule.runOnIdle { assertEquals(listOf("2026-08-31"), fixture.reviewRequests) }
        composeRule.onNodeWithTag("selected_day_recordings").performScrollTo().performClick()
        composeRule.onNodeWithTag("day_recordings_dialog").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("关闭当天录音").performClick()
        composeRule.onNodeWithContentDescription("打开设置").performClick()
        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText("模型服务"))
        composeRule.onNodeWithText("模型服务").assertIsDisplayed()
        androidx.test.espresso.Espresso.pressBack()
        assertAnchors(original, anchors())
        selectTab("记录")
        assertAnchors(original, anchors())
        assertRect(recordPosition, bounds("recording_card_layout_8"))
        evidence("compact-04-record-return", anchors())
        selectTab("回顾")
        assertAnchors(original, anchors())
        composeRule.onNodeWithTag("selected_day_date", useUnmergedTree = true).assertTextEquals("2026 年 8 月 31 日")
    }

    @Test
    fun compactHeroStatesWrapTextAndContainHighVolumeRippleWithoutMovingTheFrame() {
        showApp(width = 360, height = 480, fontScale = 1.6f, withContext = false)
        val original = anchors()
        val states = listOf(
            "starting" to RecordingSession.Starting,
            "recording" to RecordingSession.Recording(System.currentTimeMillis() - 360_000_000L),
            "stopping" to RecordingSession.Stopping,
            "failed" to RecordingSession.Failed("合成错误：这是一段需要完整换行并可以滚动查看的错误说明。".repeat(12)),
            "idle" to RecordingSession.Idle,
        )
        states.forEach { (name, state) ->
            composeRule.runOnIdle {
                fixture.recordingInputLevel.value = 1f
                fixture.recordingSession.value = state
            }
            composeRule.waitForIdle()
            assertAnchors(original, anchors())
            listOf("recording_timer", "recording_status").forEach { tag ->
                val layouts = mutableListOf<TextLayoutResult>()
                composeRule.onNodeWithTag(tag).performSemanticsAction(SemanticsActions.GetTextLayoutResult) {
                    it(layouts)
                }
                assertFalse("$name $tag clips text", layouts.single().hasVisualOverflow)
            }
            composeRule.onNodeWithTag("recording_action_button").performScrollTo()
            assertFullyVisible("recording_action_button")
            if (state is RecordingSession.Recording) {
                val ripple = composeRule.onNodeWithTag("recording_ripple").fetchSemanticsNode()
                val area = composeRule.onNodeWithTag("recording_action_area").fetchSemanticsNode()
                assertEquals(area.size, ripple.size)
                evidence("hero-recording-high-volume", anchors())
            }
            if (state is RecordingSession.Failed) {
                assertTrue(scrollMaximum("daily_panel_scroll") > 0f)
                evidence("hero-failed-long-message", anchors())
            }
        }
        composeRule.onNodeWithTag("recording_action_button").performClick()
        composeRule.runOnIdle { assertEquals(1, fixture.startRequests) }
    }

    private fun showApp(width: Int, height: Int, fontScale: Float, withContext: Boolean, today: Int = 31) {
        this.fontScale = fontScale
        lateinit var record: RecordViewModel
        lateinit var review: ReviewViewModel
        lateinit var settings: SettingsViewModel
        lateinit var appearance: AppearanceViewModel
        composeRule.runOnUiThread {
            fixture = LayoutFixture(withContext)
            record = RecordViewModel(fixture, fixture)
            review = ReviewViewModel(fixture, ReviewTimeProvider {
                ReviewTimeSnapshot(localDayRange("2026-08-$today", "Asia/Hong_Kong").startMillis + 43_200_000L, "Asia/Hong_Kong")
            })
            settings = SettingsViewModel(fixture)
            appearance = AppearanceViewModel(fixture)
            viewModels.put("record", record)
            viewModels.put("review", review)
            viewModels.put("settings", settings)
            viewModels.put("appearance", appearance)
        }
        composeRule.setContent {
            val screenDensity = LocalDensity.current
            density = screenDensity.density
            CompositionLocalProvider(LocalDensity provides Density(density, fontScale)) {
                PerContextTheme {
                    Box(Modifier.requiredSize(width.dp, height.dp).testTag("layout_test_viewport")) {
                        PerContextApp(record, review, settings, appearance, fixture::startRecording)
                    }
                }
            }
        }
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithText("12 条").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun selectTab(label: String) {
        composeRule.onNode(hasText(label) and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab))
            .performClick()
        composeRule.waitForIdle()
    }

    private fun bounds(tag: String): Rect = composeRule.onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot

    private fun anchors(): Map<String, Rect> = listOf(
        "app_header_settings", "daily_main_panel", "pidan_perch", "pidan_platform",
    ).associateWith { tag ->
        composeRule.onNodeWithTag(tag).assertIsDisplayed()
        assertFullyVisible(tag)
        bounds(tag)
    }

    private fun assertAnchors(expected: Map<String, Rect>, actual: Map<String, Rect>) {
        expected.forEach { (tag, rect) -> assertRect(rect, requireNotNull(actual[tag])) }
    }

    private fun assertRect(expected: Rect, actual: Rect) {
        listOf(expected.left to actual.left, expected.top to actual.top,
            expected.right to actual.right, expected.bottom to actual.bottom).forEach { (a, b) ->
            assertEquals(a, b, density)
        }
    }

    private fun assertFullyVisible(tag: String) {
        val node = composeRule.onNodeWithTag(tag).fetchSemanticsNode()
        assertEquals("$tag width", node.size.width.toFloat(), node.boundsInRoot.width, 1f)
        assertEquals("$tag height", node.size.height.toFloat(), node.boundsInRoot.height, 1f)
    }

    private fun scrollPosition(tag: String): Float = composeRule.onNodeWithTag(tag).fetchSemanticsNode()
        .config[SemanticsProperties.VerticalScrollAxisRange].value()

    private fun scrollMaximum(tag: String): Float = composeRule.onNodeWithTag(tag).fetchSemanticsNode()
        .config[SemanticsProperties.VerticalScrollAxisRange].maxValue()

    private fun evidence(name: String, anchors: Map<String, Rect>) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val directory = File(requireNotNull(context.getExternalFilesDir(null)), "layout-align-qa")
        assertTrue(directory.isDirectory || directory.mkdirs())
        val bitmap = composeRule.onNodeWithTag("layout_test_viewport").captureToImage().asAndroidBitmap()
        File(directory, "$name.png").outputStream().use {
            assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
        }
        val viewport = composeRule.onNodeWithTag("layout_test_viewport").fetchSemanticsNode().size
        val values = JSONObject().put("density", density).put("fontScale", fontScale)
            .put("viewportDp", JSONArray(listOf(viewport.width / density, viewport.height / density)))
            .put("recordingState", fixture.recordingSession.value::class.simpleName)
        anchors.forEach { (tag, r) ->
            values.put(tag, JSONArray(listOf(r.left / density, r.top / density, r.right / density, r.bottom / density)))
        }
        File(directory, "$name-bounds.json").writeText(values.toString(2))
    }
}

private class LayoutFixture(withContext: Boolean) : RecordingFeatureGateway, PlaybackController,
    ReviewFeatureGateway, ProviderSettingsGateway, AppearanceSettingsRepository {
    private val epoch = localDayRange("2026-08-31", "Asia/Hong_Kong").startMillis + 43_200_000L
    override val records = MutableStateFlow((1..12).map { i ->
        VoiceRecord("layout_$i", epoch + i * 60_000L, 60_000L, "/synthetic/layout_$i.m4a", "AAC-LC/M4A",
            RecordStatus.RECORDED, 1024L, TranscriptStatus.SUCCEEDED, "合成转写内容 $i")
    })
    override val recordingSession = MutableStateFlow<RecordingSession>(RecordingSession.Idle)
    override val recordingInputLevel = MutableStateFlow(0f)
    override val state = MutableStateFlow(PlaybackState())
    override val dailyContexts = MutableStateFlow(if (withContext) listOf(DailyContext(
        dayKey = "2026-08-31", zoneId = "Asia/Hong_Kong",
        content = DailyContextContent("合成回顾", "用于验证详情区域滚动和切页后状态保留的合成摘要。".repeat(15),
            List(4) { "合成主题 $it" }, emptyList(), emptyList(), emptyList(), emptyList()),
        structuredJson = "{}", providerId = "deepseek", model = "deepseek-v4-flash",
        status = DailyContextStatus.SUCCEEDED, inputFingerprint = "synthetic", sourceCount = 12,
        generatedAtMillis = epoch, updatedAtMillis = epoch,
    )) else emptyList())
    override val transcribedRecords = MutableStateFlow(records.value.map { TranscribedRecordStamp(it.id, it.createdAtMillis) })
    override val recordingSummaries = MutableStateFlow(records.value.map {
        ReviewRecordingSummary(it.id, it.createdAtMillis, it.durationMillis, it.transcriptStatus)
    })
    override val providerPresets = LlmProviderPresets.all
    override val providerSettings = MutableStateFlow(LlmSettings())
    override val settings = MutableStateFlow(AppearanceSettings())
    val reviewRequests = mutableListOf<String>()
    var startRequests = 0
    override fun startRecording() { startRequests++ }
    override fun stopRecording() = Unit
    override fun isTranscriptionAvailable() = true
    override suspend fun requestTranscription(recordId: String) = true
    override suspend fun deleteRecording(recordId: String) = false
    override fun toggle(record: VoiceRecord) = Unit
    override fun seekTo(recordId: String, fraction: Float) = Unit
    override fun stop(recordId: String) = Unit
    override fun release() = Unit
    override suspend fun requestDailyContext(dayKey: String, zoneId: String, allowChangedInput: Boolean): DailyContextRequestResult {
        reviewRequests += dayKey
        return DailyContextRequestResult.QUEUED
    }
    override suspend fun saveProviderSettings(providerId: String, apiKey: String?, baseUrl: String, model: String) = Unit
    override suspend fun providerApiKey(providerId: String): String? = null
    override suspend fun clearProviderApiKey(providerId: String) = Unit
    override suspend fun setTheme(theme: AppTheme) { settings.value = settings.value.copy(theme = theme) }
    override suspend fun setMode(mode: ThemeMode) { settings.value = settings.value.copy(mode = mode) }
}
