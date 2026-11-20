package com.llamatik.app.feature.privacy

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Platform hook that actually switches crash reporting and usage statistics on or off
 * (Firebase Crashlytics + Analytics on Android). Platforms that collect nothing simply do not
 * register one, and the opt-in switch is not shown there.
 */
interface DiagnosticsCollector {
    fun setEnabled(enabled: Boolean)

    /** Called when the user turns diagnostics off: drop anything collected but not yet sent. */
    fun discardPending() {}
}

/** Settings key holding the user's choice. Absent means "never opted in", i.e. off. */
const val DIAGNOSTICS_OPT_IN_KEY = "llamatik_diagnostics_opt_in"

/**
 * Single source of truth for the diagnostics opt-in.
 *
 * Nothing is collected until the user turns it on: the default is **off**, the Android manifest
 * ships with collection disabled, and [applyStoredChoice] re-applies the stored choice at every launch.
 */
class DiagnosticsConsentRepository(
    private val settings: Settings,
    private val collector: DiagnosticsCollector?,
) {
    /** True on platforms that have something to switch (so the Settings row is shown). */
    val isSupported: Boolean get() = collector != null

    private val _enabled = MutableStateFlow(settings.getBoolean(DIAGNOSTICS_OPT_IN_KEY, false))
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    /** Re-applies the stored choice (default: off). Call once at app start. */
    fun applyStoredChoice() {
        collector?.setEnabled(_enabled.value)
    }

    fun setEnabled(enabled: Boolean) {
        settings.putBoolean(DIAGNOSTICS_OPT_IN_KEY, enabled)
        _enabled.value = enabled
        collector?.setEnabled(enabled)
        if (!enabled) collector?.discardPending()
    }
}
