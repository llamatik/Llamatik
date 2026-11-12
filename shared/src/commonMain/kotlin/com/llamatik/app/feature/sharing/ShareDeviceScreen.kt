package com.llamatik.app.feature.sharing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.llamatik.app.feature.sharing.viewmodel.ShareDeviceState
import com.llamatik.app.feature.sharing.viewmodel.ShareDeviceViewModel
import com.llamatik.app.localization.Localization
import com.llamatik.app.localization.getCurrentLocalization
import com.llamatik.app.ui.icon.LlamatikIcons
import com.llamatik.app.ui.theme.LlamatikTheme
import com.llamatik.app.ui.theme.Typography
import com.llamatik.host.EmbeddedServerState

class ShareDeviceScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<ShareDeviceViewModel>()
        val state by viewModel.state.collectAsState()
        val loc = getCurrentLocalization()

        // The owner decides, one device at a time. Blocks interaction until answered.
        state.pendingRequests.firstOrNull()?.let { request ->
            AlertDialog(
                onDismissRequest = { viewModel.deny(request.id) },
                title = { Text(loc.shareDevicePendingTitle) },
                text = { Text("${request.clientName} ${loc.shareDevicePendingMessage}") },
                confirmButton = {
                    TextButton(onClick = { viewModel.approve(request.id) }) { Text(loc.shareDeviceAllow) }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.deny(request.id) }) { Text(loc.shareDeviceDeny) }
                },
            )
        }

        LlamatikTheme {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(loc.shareDeviceTitle, style = Typography.get().titleLarge) },
                        navigationIcon = {
                            IconButton(onClick = { navigator.pop() }) {
                                Icon(LlamatikIcons.ArrowBack, contentDescription = loc.backLabel)
                            }
                        },
                        colors = TopAppBarDefaults.mediumTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                        ),
                    )
                },
            ) { padding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(MaterialTheme.colorScheme.background),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item { Text(loc.shareDeviceDescription, style = Typography.get().bodyMedium) }
                    if (!state.isSupported) {
                        item { Text(loc.shareDeviceUnsupported, color = MaterialTheme.colorScheme.error) }
                        return@LazyColumn
                    }
                    item { SwitchCard(state, loc, viewModel) }
                    item { ModelsCard(state, loc) }
                    item { Text(loc.shareDeviceApprovedSection, style = Typography.get().titleMedium) }
                    if (state.devices.isEmpty()) {
                        item { Text(loc.shareDeviceNoDevices, style = Typography.get().bodySmall) }
                    }
                    items(state.devices, key = { it.clientId }) { device ->
                        Card(Modifier.fillMaxWidth()) {
                            Row(
                                Modifier.padding(start = 16.dp, end = 8.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(device.name, style = Typography.get().bodyLarge)
                                TextButton(onClick = { viewModel.revoke(device.clientId) }) {
                                    Text(loc.shareDeviceRevoke)
                                }
                            }
                        }
                    }
                    item {
                        Text(
                            loc.shareDeviceWarning,
                            style = Typography.get().bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SwitchCard(state: ShareDeviceState, loc: Localization, viewModel: ShareDeviceViewModel) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(loc.shareDeviceSwitch, style = Typography.get().titleMedium)
                Switch(checked = state.isOn, onCheckedChange = viewModel::setSharing)
            }
            OutlinedTextField(
                value = state.portText,
                onValueChange = viewModel::onPortChanged,
                label = { Text(loc.shareDevicePortLabel) },
                enabled = !state.isOn,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            when (val s = state.serverState) {
                is EmbeddedServerState.Running -> {
                    Text(loc.shareDeviceAddress, style = Typography.get().bodySmall)
                    s.addresses.ifEmpty { listOf("?") }.forEach {
                        Text("$it:${s.port}", style = Typography.get().titleMedium)
                    }
                    Text("${loc.shareDeviceRequests}: ${state.totalRequests} · ${loc.shareDeviceActiveRequests}: ${state.activeRequests}", style = Typography.get().bodySmall)
                }
                EmbeddedServerState.Starting -> Text(loc.shareDeviceStarting)
                is EmbeddedServerState.Error ->
                    Text("${loc.shareDeviceError}: ${s.message}", color = MaterialTheme.colorScheme.error)
                EmbeddedServerState.Stopped -> Unit
            }
        }
    }
}

@Composable
private fun ModelsCard(state: ShareDeviceState, loc: Localization) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(loc.shareDeviceModelsSection, style = Typography.get().titleMedium)
            if (state.textModelName == null && state.embeddingModelName == null) {
                Text(loc.shareDeviceNoModel, style = Typography.get().bodySmall)
            }
            state.textModelName?.let { Text("${loc.shareDeviceTextModel}: $it", style = Typography.get().bodyMedium) }
            state.embeddingModelName?.let {
                Text("${loc.shareDeviceEmbeddingModel}: $it", style = Typography.get().bodyMedium)
            }
        }
    }
}
