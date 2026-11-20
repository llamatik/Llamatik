package com.llamatik.app.feature.privacy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.llamatik.app.localization.AvailableLanguages
import com.llamatik.app.localization.getCurrentLanguage
import com.llamatik.app.localization.getCurrentLocalization
import com.llamatik.app.ui.icon.LlamatikIcons
import com.llamatik.app.ui.theme.Typography

/** Where the privacy policy lives, in the user's language (the site serves English at the root). */
fun privacyPolicyUrl(language: AvailableLanguages = getCurrentLanguage()): String {
    val prefix = when (language) {
        AvailableLanguages.EN -> ""
        AvailableLanguages.CN -> "zh/"
        else -> language.name.lowercase() + "/"
    }
    return "https://www.llamatik.com/${prefix}privacy/"
}

/**
 * Opt-in switch for crash reports and basic usage statistics. Off by default; only rendered on
 * platforms that actually have a collector (see [DiagnosticsConsentRepository.isSupported]).
 */
@Composable
fun DiagnosticsSettingsRow(repository: DiagnosticsConsentRepository) {
    if (!repository.isSupported) return
    val localization = getCurrentLocalization()
    val enabled by repository.enabled.collectAsState()
    val uriHandler = LocalUriHandler.current

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = LlamatikIcons.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp),
                )
                Column {
                    Text(text = localization.diagnosticsTitle, style = Typography.get().bodyMedium)
                    Text(
                        text = localization.diagnosticsDescription,
                        style = Typography.get().labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
            }
            Switch(
                checked = enabled,
                onCheckedChange = { repository.setEnabled(it) },
                thumbContent = if (enabled) {
                    {
                        Icon(
                            imageVector = LlamatikIcons.Check,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                } else {
                    null
                },
            )
        }
        TextButton(onClick = { uriHandler.openUri(privacyPolicyUrl()) }) {
            Text(text = localization.privacyPolicy, style = Typography.get().labelMedium)
        }
    }
}
