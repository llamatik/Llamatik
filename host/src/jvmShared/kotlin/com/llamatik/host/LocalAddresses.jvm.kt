package com.llamatik.host

import java.net.Inet4Address
import java.net.NetworkInterface

internal actual fun localIpAddresses(): List<String> = runCatching {
    NetworkInterface.getNetworkInterfaces().toList()
        .filter { it.isUp && !it.isLoopback && !it.isVirtual }
        .flatMap { it.inetAddresses.toList() }
        .filterIsInstance<Inet4Address>()
        .filter { !it.isLoopbackAddress }
        .mapNotNull { it.hostAddress }
}.getOrDefault(emptyList())
