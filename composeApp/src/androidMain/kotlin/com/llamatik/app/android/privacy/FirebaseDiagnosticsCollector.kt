package com.llamatik.app.android.privacy

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.llamatik.app.feature.privacy.DiagnosticsCollector

/**
 * Switches Firebase Crashlytics and Analytics on or off.
 *
 * The manifest ships with both disabled, so nothing is collected until the user opts in. Every call
 * is wrapped because builds without google-services.json (some CI jobs, forks) have no FirebaseApp.
 */
class FirebaseDiagnosticsCollector(private val context: Context) : DiagnosticsCollector {

    override fun setEnabled(enabled: Boolean) {
        // Global default first (covers Firebase components that have no switch of their own).
        runCatching { FirebaseApp.getInstance().setDataCollectionDefaultEnabled(enabled) }
        runCatching { FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(enabled) }
        runCatching { FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(enabled) }
    }

    override fun discardPending() {
        // Drops queued events and resets the pseudonymous app-instance ID, and removes crash reports
        // that were written but not uploaded yet.
        runCatching { FirebaseAnalytics.getInstance(context).resetAnalyticsData() }
        runCatching { FirebaseCrashlytics.getInstance().deleteUnsentReports() }
    }
}
