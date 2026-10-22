package com.percontext.app.feature.record

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.percontext.app.domain.model.RecordStatus
import com.percontext.app.domain.model.VoiceRecord
import com.percontext.app.ui.component.DailyPageScaffold
import com.percontext.app.domain.recording.RecordingSession
import com.percontext.app.ui.theme.InkColor
import com.percontext.app.ui.theme.LocalColor
import com.percontext.app.ui.theme.PerContextTheme

@Composable
fun RecordRoute(
    viewModel: RecordViewModel,
    onStartRecording: () -> Unit,
    onOpenSettings: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingDelete by remember { mutableStateOf<VoiceRecord?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.messages.collect(snackbarHostState::showSnackbar)
    }

    RecordScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onStartRecording = onStartRecording,
        onStopRecording = viewModel::stopRecording,
        onTogglePlayback = viewModel::togglePlayback,
        onSeekPlayback = viewModel::seekPlayback,
        onRequestTranscription = viewModel::requestTranscription,
        onRequestDelete = { pendingDelete = it },
        onOpenSettings = onOpenSettings,
    )

    pendingDelete?.let { record ->
        DeleteRecordingDialog(
            record = record,
            onConfirm = {
                viewModel.delete(record)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
fun RecordScreen(
    state: RecordUiState,
    snackbarHostState: SnackbarHostState,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onTogglePlayback: (VoiceRecord) -> Unit,
    onSeekPlayback: (String, Float) -> Unit,
    onRequestTranscription: (VoiceRecord) -> Unit,
    onRequestDelete: (VoiceRecord) -> Unit,
    onOpenSettings: () -> Unit = {},
) {
    val recordsListState = rememberLazyListState()
    val firstRecordId = state.records.firstOrNull()?.id
    var previousFirstRecordId by remember { mutableStateOf(firstRecordId) }
    var previousRecordCount by remember { mutableIntStateOf(state.records.size) }
    var openedTranscript by remember { mutableStateOf<VoiceRecord?>(null) }

    LaunchedEffect(firstRecordId, state.records.size) {
        val newRecordInserted = previousFirstRecordId != null &&
            firstRecordId != previousFirstRecordId &&
            state.records.size > previousRecordCount
        if (newRecordInserted) recordsListState.scrollToItem(0)

        previousFirstRecordId = firstRecordId
        previousRecordCount = state.records.size
    }

    DailyPageScaffold(
        snackbarHostState = snackbarHostState,
        onOpenSettings = onOpenSettings,
        animatePerch = state.recordingSession !is RecordingSession.Recording,
        panel = { metrics ->
            RecordingHero(
                session = state.recordingSession,
                inputLevel = state.recordingInputLevel,
                onStartRecording = onStartRecording,
                onStopRecording = onStopRecording,
                modifier = Modifier.fillMaxWidth().heightIn(min = metrics.contentHeight)
                    .testTag("recording_controls"),
            )
        },
        details = { modifier ->
            LazyColumn(
                state = recordsListState,
                modifier = modifier.testTag("recent_records_list"),
                contentPadding = PaddingValues(top = 8.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item(key = "recent_header") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "最近记录",
                            style = MaterialTheme.typography.titleLarge,
                            color = InkColor,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "${state.records.size} 条",
                            color = LocalColor,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                if (state.records.isEmpty()) {
                    item(key = "empty_timeline") { EmptyTimeline() }
                } else {
                    items(state.records, key = VoiceRecord::id) { record ->
                        RecordingCard(
                            record = record,
                            playback = state.playback,
                            onTogglePlayback = { onTogglePlayback(record) },
                            onSeekPlayback = { onSeekPlayback(record.id, it) },
                            onRequestTranscription = { onRequestTranscription(record) },
                            onRequestDelete = { onRequestDelete(record) },
                            onOpenTranscript = { openedTranscript = record },
                        )
                    }
                }
            }
        },
    )

    openedTranscript?.let { record ->
        TranscriptDialog(
            record = record,
            onDismiss = { openedTranscript = null },
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun RecordScreenPreview() {
    PerContextTheme {
        RecordScreen(
            state = RecordUiState(
                records = listOf(
                    VoiceRecord(
                        id = "record_preview",
                        createdAtMillis = 1_788_000_000_000L,
                        durationMillis = 127_000L,
                        audioLocation = "/recordings/record_preview.m4a",
                        audioCodec = "AAC-LC/M4A",
                        status = RecordStatus.RECORDED,
                        fileSizeBytes = 1_024_000L,
                    ),
                ),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onStartRecording = {},
            onStopRecording = {},
            onTogglePlayback = {},
            onSeekPlayback = { _, _ -> },
            onRequestTranscription = {},
            onRequestDelete = {},
            onOpenSettings = {},
        )
    }
}
