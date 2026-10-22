package com.percontext.app.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.percontext.app.ui.theme.AcrylicEdgeColor
import com.percontext.app.ui.theme.CanvasColor
import com.percontext.app.ui.theme.PerContextTheme

internal data class DailyPanelMetrics(
    val monthHeaderHeight: Dp,
    val weekdayHeight: Dp,
    val dayHeight: Dp,
    val legendHeight: Dp,
    val recordContentHeight: Dp,
) {
    val contentHeight: Dp
        get() = maxOf(
            350.dp,
            recordContentHeight,
            20.dp + monthHeaderHeight + weekdayHeight + 4.dp + dayHeight * 6 + 5.dp + legendHeight,
        )
}

@Composable
private fun dailyPanelMetrics(width: Dp): DailyPanelMetrics {
    val density = LocalDensity.current
    val typography = MaterialTheme.typography
    val title = rememberTextMeasurer().measure(
        text = "8888 年 12 月",
        style = typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        constraints = Constraints(maxWidth = with(density) { (width - 124.dp).roundToPx().coerceAtLeast(1) }),
    )
    return with(density) {
        DailyPanelMetrics(
            monthHeaderHeight = maxOf(48.dp, title.size.height.toDp() + 8.dp, 32.sp.toDp()),
            weekdayHeight = typography.labelSmall.lineHeight.toDp(),
            dayHeight = maxOf(40.dp, typography.bodyMedium.lineHeight.toDp() + 11.dp),
            legendHeight = typography.labelSmall.lineHeight.toDp(),
            recordContentHeight = 60.sp.toDp() + typography.bodyLarge.lineHeight.toDp() + 264.dp,
        )
    }
}

@Composable
internal fun DailyPageScaffold(
    snackbarHostState: SnackbarHostState,
    onOpenSettings: () -> Unit,
    animatePerch: Boolean = true,
    panel: @Composable (DailyPanelMetrics) -> Unit,
    details: @Composable (Modifier) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CanvasColor,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { contentPadding ->
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().padding(contentPadding)
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 10.dp),
        ) {
            val metrics = dailyPanelMetrics(maxWidth)
            val available = (maxHeight - 98.dp).coerceAtLeast(0.dp)
            val detailsReserve = minOf(160.dp, available / 3f)
            val panelHeight = minOf(metrics.contentHeight, available - detailsReserve)
            Column(Modifier.fillMaxSize().testTag("daily_page")) {
                CompactPageHeader(onOpenSettings)
                Spacer(Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().height(panelHeight).testTag("daily_main_panel"),
                    shape = RoundedCornerShape(30.dp),
                    colors = CardDefaults.cardColors(containerColor = PerContextTheme.colors.hero),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = BorderStroke(1.dp, AcrylicEdgeColor),
                ) {
                    Box(
                        Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                            .testTag("daily_panel_scroll"),
                    ) {
                        Box(Modifier.fillMaxWidth().heightIn(min = metrics.contentHeight)) {
                            panel(metrics)
                        }
                    }
                }
                PidanPerch(animate = animatePerch)
                Box(Modifier.fillMaxWidth().weight(1f).testTag("daily_lower_pane")) {
                    details(Modifier.fillMaxSize())
                }
            }
        }
    }
}
