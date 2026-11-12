package com.llamatik.app.feature.server.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.touchlab.kermit.Logger
import com.llamatik.app.feature.server.ConnectedServer
import com.llamatik.app.feature.server.SavedServer
import com.llamatik.app.feature.server.ServerConnectionRepository
import com.llamatik.app.localization.getCurrentLocalization
import com.llamatik.network.discovery.DiscoveredNode
import com.llamatik.network.discovery.DiscoveryEvent
import com.llamatik.network.discovery.NodeDiscovery
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ServerSettingsState(
    val isDiscovering: Boolean = false,
    val discoveredNodes: List<DiscoveredNode> = emptyList(),
    val savedServers: List<SavedServer> = emptyList(),
    val activeServer: ConnectedServer? = null,
    val isConnecting: Boolean = false,
    val connectionError: String? = null,
    val manualHost: String = "",
    val manualPort: String = "8080",
    val manualName: String = "",
)

class ServerSettingsViewModel(
    private val serverConnectionRepository: ServerConnectionRepository,
) : ScreenModel {

    private val _state = MutableStateFlow(ServerSettingsState())
    val state: StateFlow<ServerSettingsState> = _state.asStateFlow()

    private var discoveryJob: Job? = null

    init {
        _state.value = _state.value.copy(
            activeServer = serverConnectionRepository.activeServer.value,
            savedServers = serverConnectionRepository.getSavedServers(),
        )
        screenModelScope.launch {
            serverConnectionRepository.activeServer.collect { server ->
                _state.value = _state.value.copy(activeServer = server)
            }
        }
    }

    fun startDiscovery() {
        if (_state.value.isDiscovering) return
        _state.value = _state.value.copy(isDiscovering = true, discoveredNodes = emptyList())
        discoveryJob = screenModelScope.launch {
            runCatching {
                NodeDiscovery.startDiscovering().collect { event ->
                    when (event) {
                        is DiscoveryEvent.NodeFound ->
                            _state.value = _state.value.copy(
                                discoveredNodes = _state.value.discoveredNodes
                                    .filter { it.id != event.node.id } + event.node
                            )
                        is DiscoveryEvent.NodeLost ->
                            _state.value = _state.value.copy(
                                discoveredNodes = _state.value.discoveredNodes
                                    .filter { it.id != event.nodeId }
                            )
                    }
                }
            }.onFailure { e ->
                Logger.e(e) { "Discovery error" }
                _state.value = _state.value.copy(isDiscovering = false)
            }
        }
    }

    fun stopDiscovery() {
        NodeDiscovery.stopDiscovering()
        discoveryJob?.cancel()
        discoveryJob = null
        _state.value = _state.value.copy(isDiscovering = false)
    }

    fun connectToDiscoveredNode(node: DiscoveredNode) {
        connectToServer(node.host, node.port, node.name)
    }

    fun connectToManual() {
        val host = _state.value.manualHost.trim()
        val port = _state.value.manualPort.trim().toIntOrNull() ?: 8080
        val name = _state.value.manualName.trim().ifBlank { host }
        if (host.isBlank()) return
        connectToServer(host, port, name)
    }

    fun connectToSavedServer(host: String, port: Int, name: String) {
        connectToServer(host, port, name)
    }

    private fun connectToServer(host: String, port: Int, name: String) {
        if (_state.value.isConnecting) return
        _state.value = _state.value.copy(isConnecting = true, connectionError = null)
        screenModelScope.launch {
            val server = serverConnectionRepository.connectToServer(host, port, name)
            if (server != null) {
                serverConnectionRepository.addSavedServer(host, port, name)
                _state.value = _state.value.copy(
                    isConnecting = false,
                    connectionError = null,
                    activeServer = server,
                    savedServers = serverConnectionRepository.getSavedServers(),
                )
            } else {
                _state.value = _state.value.copy(
                    isConnecting = false,
                    connectionError = "${getCurrentLocalization().serverCouldNotConnect} $host:$port",
                )
            }
        }
    }

    fun disconnectFromServer() {
        serverConnectionRepository.disconnectFromServer()
        _state.value = _state.value.copy(activeServer = null, connectionError = null)
    }

    fun removeSavedServer(server: SavedServer) {
        serverConnectionRepository.removeSavedServer(server.host, server.port)
        _state.value = _state.value.copy(savedServers = serverConnectionRepository.getSavedServers())
    }

    fun onManualHostChanged(value: String) {
        _state.value = _state.value.copy(manualHost = value, connectionError = null)
    }

    fun onManualPortChanged(value: String) {
        _state.value = _state.value.copy(manualPort = value)
    }

    fun onManualNameChanged(value: String) {
        _state.value = _state.value.copy(manualName = value)
    }

    fun clearConnectionError() {
        _state.value = _state.value.copy(connectionError = null)
    }

    override fun onDispose() {
        stopDiscovery()
    }
}
