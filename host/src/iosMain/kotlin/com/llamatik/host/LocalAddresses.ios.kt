@file:OptIn(ExperimentalForeignApi::class)

package com.llamatik.host

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CArrayPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import platform.darwin.freeifaddrs
import platform.darwin.getifaddrs
import platform.darwin.ifaddrs
import platform.posix.AF_INET
import platform.posix.IFF_LOOPBACK
import platform.posix.IFF_UP
import platform.posix.NI_MAXHOST
import platform.posix.NI_NUMERICHOST
import platform.posix.getnameinfo
import platform.posix.sockaddr_in

/** IPv4 addresses of the up, non-loopback interfaces (Wi-Fi `en0`, hotspot `bridge100`, ...). */
internal actual fun localIpAddresses(): List<String> = memScoped {
    val result = mutableListOf<String>()
    val head = alloc<CPointerVar<ifaddrs>>()
    if (getifaddrs(head.ptr) != 0) return@memScoped emptyList()
    try {
        val host: CArrayPointer<ByteVar> = allocArray(NI_MAXHOST)
        var cursor = head.value
        while (cursor != null) {
            val entry = cursor.pointed
            val addr = entry.ifa_addr
            val flags = entry.ifa_flags.toInt()
            if (addr != null && addr.pointed.sa_family.toInt() == AF_INET &&
                flags and IFF_UP != 0 && flags and IFF_LOOPBACK == 0
            ) {
                val rc = getnameinfo(
                    addr, sizeOf_sockaddr_in, host, NI_MAXHOST.toUInt(), null, 0u, NI_NUMERICHOST,
                )
                if (rc == 0) result += host.toKString()
            }
            cursor = entry.ifa_next
        }
    } finally {
        freeifaddrs(head.value)
    }
    result
}

private val sizeOf_sockaddr_in: UInt = kotlinx.cinterop.sizeOf<sockaddr_in>().toUInt()
