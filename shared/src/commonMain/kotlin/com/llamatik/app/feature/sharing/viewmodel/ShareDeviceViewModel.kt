package com.llamatik.app.feature.sharing.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.llamatik.app.feature.sharing.ShareDeviceRepository
import com.llamatik.host.ApprovedDeviceInfo
import com.llamatik.host.EmbeddedServerState
import com.llamatik.host.LoadedModels
import com.llamatik.host.PairingRequestInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ShareDeviceState(
    val isSupported: Boolean = true,
    val serverState: EmbeddedServerState = EmbeddedServerState.Stopped,
    val portText: String = "8080",
    val pendingRequests: List<PairingRequestInfo> = emptyList(),
    val devices: List<ApprovedDeviceInfo> = emptyList(),
    val activeRequests: Int = 0,
    val totalRequests: Long = 0,
    val textModelName: String? = null,
    val embeddingModelName: String? = null,
) {
    val isOn: Boolean
        get() = serverState is EmbeddedServerState.Running || serverState is EmbeddedServerState.Starting
}

class ShareDeviceViewModel(
    private val repository: ShareDeviceRepository,
) : ScreenModel {

    private val _state = MutableStateFlow(
        ShareDeviceState(isSupported = repository.isSupported, portText = repository.port.toString()),
    )
    val state: StateFlow<ShareDeviceState> = _state.asStateFlow()

    init {
        val server = repository.server
        screenModelScope.launch {
            combine(
                server.state, server.pendingRequests, server.devices, server.activeRequests, server.totalRequests,
            ) { st, pending, devices, active, total ->
                _state.update {
                    it.copy(
                        serverState = st, pendingRequests = pending, devices = devices,
                        activeRequests = active, totalRequests = total,
                    )
                }
            }.collect {}
        }
        screenModelScope.launch {
            combine(LoadedModels.generation, LoadedModels.embedding) { g, e -> g to e }
                .collect { (generation, embedding) ->
                    _state.update { it.copy(textModelName = generation, embeddingModelName = embedding) }
                }
        }
    }

    fun onPortChanged(value: String) {
        if (value.length <= 5 && value.all { it.isDigit() }) _state.update { it.copy(portText = value) }
    }

    fun setSharing(enabled: Boolean) {
        if (enabled) {
            val port = _state.value.portText.toIntOrNull()?.takeIf { it in 1024..65535 } ?: return
            repository.port = port
            repository.start()
        } else {
            repository.stop()
        }
    }

    fun approve(id: String) = repository.server.approve(id)
    fun deny(id: String) = repository.server.deny(id)
    fun revoke(clientId: String) = repository.server.revoke(clientId)
}
