@file:OptIn(ExperimentalWasmJsInterop::class)

package com.llamatik.app.platform

actual object LaunchPrompt {
    private var consumed = false

    actual fun consume(): String? {
        if (consumed) return null
        consumed = true
        return readLaunchPrompt()?.trim()?.take(MAX_LENGTH)?.takeIf { it.isNotEmpty() }
    }

    private const val MAX_LENGTH = 4000
}

// Reads ?prompt=... (or #prompt=...) and removes it from the address bar so a reload doesn't repeat it.
@JsFun(
    """() => {
        try {
            const url = new URL(globalThis.location.href);
            let v = url.searchParams.get('prompt');
            if (v === null && url.hash.startsWith('#prompt=')) {
                v = decodeURIComponent(url.hash.slice('#prompt='.length).replace(/\+/g, ' '));
            }
            if (v === null) return null;
            url.searchParams.delete('prompt');
            if (url.hash.startsWith('#prompt=')) url.hash = '';
            globalThis.history.replaceState(null, '', url.pathname + url.search + url.hash);
            return v;
        } catch (e) { return null; }
    }"""
)
private external fun readLaunchPrompt(): String?
