package com.percontext.app.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.percontext.app.ui.theme.InkColor

@Composable
internal fun CompactPageHeader(onOpenSettings: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("daily_header"),
        horizontalArrangement = Arrangement.End,
    ) {
        HeaderSettingsButton(onClick = onOpenSettings)
    }
}

@Composable
internal fun HeaderSettingsButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp).testTag("app_header_settings"),
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "打开设置",
            tint = InkColor.copy(alpha = 0.78f),
        )
    }
}
