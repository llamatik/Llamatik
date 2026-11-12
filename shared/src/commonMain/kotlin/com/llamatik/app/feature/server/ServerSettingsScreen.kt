package com.llamatik.app.feature.server

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.llamatik.app.feature.server.viewmodel.ServerSettingsViewModel
import com.llamatik.app.localization.Localization
import com.llamatik.app.localization.getCurrentLocalization
import com.llamatik.app.ui.icon.LlamatikIcons
import com.llamatik.app.ui.theme.LlamatikTheme
import com.llamatik.app.ui.theme.Typography
import com.llamatik.network.discovery.DiscoveredNode

class ServerSettingsScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<ServerSettingsViewModel>()
        val state by viewModel.state.collectAsState()
        val loc = getCurrentLocalization()
        val snackbarHostState = remember { SnackbarHostState() }
        // Connection is deferred until the user acknowledges the unencrypted-traffic warning.
        var pendingConnect by remember { mutableStateOf<(() -> Unit)?>(null) }

        pendingConnect?.let { connect ->
            AlertDialog(
                onDismissRequest = { pendingConnect = null },
                title = { Text(loc.serverUnencryptedWarningTitle) },
                text = { Text(loc.serverUnencryptedWarningMessage) },
                confirmButton = {
                    TextButton(onClick = {
                        pendingConnect = null
                        connect()
                    }) { Text(loc.serverUnencryptedWarningConfirm) }
                },
                dismissButton = {
                    TextButton(onClick = { pendingConnect = null }) { Text(loc.serverFallbackDismiss) }
                },
            )
        }

        LaunchedEffect(state.connectionError) {
            state.connectionError?.let { err ->
                snackbarHostState.showSnackbar(err)
                viewModel.clearConnectionError()
            }
        }

        LlamatikTheme {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    TopAppBar(
                        title = { Text(loc.serverSettingsTitle, style = Typography.get().titleLarge) },
                        navigationIcon = {
                            IconButton(onClick = { navigator.pop() }) {
                                Icon(LlamatikIcons.ArrowBack, contentDescription = loc.backLabel)
                            }
                        },
                        colors = TopAppBarDefaults.mediumTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                        ),
                    )
                }
            ) { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(MaterialTheme.colorScheme.background),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item { UnencryptedWarningCard(loc) }

                    (state.pairing as? PairingProgress.Waiting)?.let { waiting ->
                        item {
                            Card(Modifier.fillMaxWidth()) {
                                Row(
                                    Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                                    Text("${loc.serverWaitingForApproval} (${waiting.serverName})")
                                }
                            }
                        }
                    }

                    // Connected server section
                    if (state.activeServer != null) {
                        item {
                            ConnectedServerCard(
                                server = state.activeServer!!,
                                loc = loc,
                                onDisconnect = { viewModel.disconnectFromServer() }
                            )
                        }
                    }

                    // Discover section
                    item {
                        SectionCard(title = loc.serverDiscoverSection) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = if (state.isDiscovering) loc.serverDiscovering else loc.serverDiscoverDescription,
                                    style = Typography.get().bodySmall,
                                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                if (state.isDiscovering) {
                                    OutlinedButton(onClick = { viewModel.stopDiscovery() }) {
                                        Text(loc.stop)
                                    }
                                } else {
                                    Button(onClick = { viewModel.startDiscovery() }) {
                                        Text(loc.serverScan)
                                    }
                                }
                            }

                            if (state.isDiscovering) {
                                Spacer(Modifier.height(8.dp))
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            }

                            if (state.discoveredNodes.isNotEmpty()) {
                                Spacer(Modifier.height(12.dp))
                                state.discoveredNodes.forEach { node ->
                                    DiscoveredNodeRow(
                                        node = node,
                                        isConnecting = state.isConnecting,
                                        isConnected = state.activeServer?.host == node.host && state.activeServer?.port == node.port,
                                        onConnect = { pendingConnect = { viewModel.connectToDiscoveredNode(node) } },
                                        loc = loc,
                                    )
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                }
                            } else if (!state.isDiscovering) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = loc.serverNoNodesFound,
                                    style = Typography.get().bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    // Manual entry section
                    item {
                        SectionCard(title = loc.serverManualSection) {
                            OutlinedTextField(
                                value = state.manualHost,
                                onValueChange = viewModel::onManualHostChanged,
                                label = { Text(loc.serverHostLabel) },
                                placeholder = { Text("192.168.1.100") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = state.manualPort,
                                    onValueChange = viewModel::onManualPortChanged,
                                    label = { Text(loc.serverPortLabel) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                )
                                OutlinedTextField(
                                    value = state.manualName,
                                    onValueChange = viewModel::onManualNameChanged,
                                    label = { Text(loc.serverNameLabel) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { pendingConnect = { viewModel.connectToManual() } },
                                enabled = state.manualHost.isNotBlank() && !state.isConnecting,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                if (state.isConnecting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                    )
                                    Spacer(Modifier.width(8.dp))
                                }
                                Text(if (state.isConnecting) loc.serverConnecting else loc.serverConnect)
                            }
                        }
                    }

                    // Saved servers
                    if (state.savedServers.isNotEmpty()) {
                        item {
                            SectionCard(title = loc.serverSavedSection) {
                                state.savedServers.forEach { saved ->
                                    SavedServerRow(
                                        server = saved,
                                        isConnected = state.activeServer?.host == saved.host && state.activeServer?.port == saved.port,
                                        isConnecting = state.isConnecting,
                                        onConnect = { pendingConnect = { viewModel.connectToSavedServer(saved.host, saved.port, saved.name) } },
                                        onDelete = { viewModel.removeSavedServer(saved) },
                                        loc = loc,
                                    )
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UnencryptedWarningCard(loc: Localization) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = loc.serverUnencryptedWarningTitle,
                style = Typography.get().titleSmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = loc.serverUnencryptedWarningMessage,
                style = Typography.get().bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    }
}

@Composable
private fun ConnectedServerCard(
    server: ConnectedServer,
    loc: Localization,
    onDisconnect: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        LlamatikIcons.Server,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Column {
                        Text(
                            text = server.name,
                            style = Typography.get().titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            text = "${server.host}:${server.port}",
                            style = Typography.get().bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        )
                    }
                }
                TextButton(onClick = onDisconnect) {
                    Text(loc.serverDisconnect, color = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = loc.serverCapabilitiesLabel,
                style = Typography.get().labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
            )
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                CapabilityChip(label = loc.textGeneration, enabled = server.capabilities.hasText)
                CapabilityChip(label = loc.imageGeneration, enabled = server.capabilities.hasImage)
                CapabilityChip(label = loc.serverCapabilityVision, enabled = server.capabilities.hasVision)
            }
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CapabilityChip(label = loc.videoGeneration, enabled = server.capabilities.hasVideo)
                CapabilityChip(label = loc.serverCapabilityAudio, enabled = server.capabilities.hasAudio)
            }
        }
    }
}

@Composable
private fun CapabilityChip(label: String, enabled: Boolean) {
    val containerColor = if (enabled) MaterialTheme.colorScheme.secondary
    else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (enabled) MaterialTheme.colorScheme.onSecondary
    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    Surface(
        shape = MaterialTheme.shapes.small,
        color = containerColor,
    ) {
        Text(
            text = label,
            style = Typography.get().labelSmall,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = Typography.get().titleSmall)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun DiscoveredNodeRow(
    node: DiscoveredNode,
    isConnecting: Boolean,
    isConnected: Boolean,
    onConnect: () -> Unit,
    loc: Localization,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = node.name, style = Typography.get().bodyMedium)
            Text(
                text = "${node.host}:${node.port}",
                style = Typography.get().bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (isConnected) {
            Text(
                text = loc.serverConnected,
                color = MaterialTheme.colorScheme.primary,
                style = Typography.get().labelMedium,
            )
        } else {
            OutlinedButton(onClick = onConnect, enabled = !isConnecting) {
                Text(loc.serverConnect)
            }
        }
    }
}

@Composable
private fun SavedServerRow(
    server: SavedServer,
    isConnected: Boolean,
    isConnecting: Boolean,
    onConnect: () -> Unit,
    onDelete: () -> Unit,
    loc: Localization,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = server.name, style = Typography.get().bodyMedium)
            Text(
                text = "${server.host}:${server.port}",
                style = Typography.get().bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (isConnected) {
            Text(
                text = loc.serverConnected,
                color = MaterialTheme.colorScheme.primary,
                style = Typography.get().labelMedium,
                modifier = Modifier.padding(end = 8.dp),
            )
        } else {
            OutlinedButton(
                onClick = onConnect,
                enabled = !isConnecting,
                modifier = Modifier.padding(end = 4.dp),
            ) {
                Text(loc.serverConnect)
            }
        }
        IconButton(onClick = onDelete) {
            Icon(
                LlamatikIcons.Delete,
                contentDescription = loc.delete,
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}
