package com.llamatik.app.feature.sharing

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.llamatik.app.localization.Localization
import com.llamatik.app.ui.icon.LlamatikIcons

private val ConnectedGreen = Color(0xFF2E7D32)

/** Subtitle under the "Share this device" row in Settings. */
fun SharingStatus.subtitle(localization: Localization): String? = when {
    !isOn -> null
    isGeneratingForOthers ->
        localization.shareDeviceStatusGenerating.replace("{count}", connectedClients.coerceAtLeast(1).toString())
    connectedClients > 0 -> localization.shareDeviceStatusConnected.replace("{count}", connectedClients.toString())
    else -> localization.shareDeviceStatusIdle
}

private fun SharingStatus.description(localization: Localization): String = when {
    isGeneratingForOthers -> localization.shareDeviceStatusChipGenerating
    connectedClients > 0 -> localization.shareDeviceStatusChipConnected.replace("{count}", connectedClients.toString())
    else -> localization.shareDeviceStatusChipIdle
}

/**
 * Small indicator for the chat top bar, shown only while this device is sharing:
 * grey = nobody connected, green = devices connected, pulsing orange = generating for another device.
 */
@Composable
fun SharingStatusIndicator(status: SharingStatus, localization: Localization, modifier: Modifier = Modifier) {
    if (!status.isOn) return

    val tint = when {
        status.isGeneratingForOthers -> MaterialTheme.colorScheme.primary
        status.connectedClients > 0 -> ConnectedGreen
        else -> MaterialTheme.colorScheme.outline
    }
    val pulse by rememberInfiniteTransition().animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
    )
    Row(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .alpha(if (status.isGeneratingForOthers) pulse else 1f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = LlamatikIcons.Share,
            contentDescription = status.description(localization),
            tint = tint,
            modifier = Modifier.size(18.dp),
        )
        if (status.connectedClients > 0) {
            Text(
                text = status.connectedClients.toString(),
                color = tint,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
    }
}
