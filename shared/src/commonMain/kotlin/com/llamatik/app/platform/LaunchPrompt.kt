package com.llamatik.app.platform

/**
 * A prompt handed to the app by whatever launched it (on the web: `/prompt/?prompt=...` from the
 * llamatik.com home page). Returned once, then cleared, so a recomposition or navigation back to
 * the chat never re-inserts it. Null on platforms with no launch hand-off.
 */
expect object LaunchPrompt {
    fun consume(): String?
}
