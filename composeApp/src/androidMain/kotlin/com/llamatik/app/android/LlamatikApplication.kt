package com.llamatik.app.android

import android.app.Application
import com.llamatik.app.android.core.di.DependencyContainer
import com.llamatik.app.feature.privacy.DiagnosticsConsentRepository
import com.llamatik.app.feature.reviews.ReviewEntryPoint
import com.llamatik.app.platform.AndroidContextHolder
import org.koin.java.KoinJavaComponent.getKoin

class LlamatikApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AndroidContextHolder.appContext = this
        initDependencyContainer()

        // Crash reports and usage statistics are opt-in: re-apply what the user chose (default: off).
        getKoin().get<DiagnosticsConsentRepository>().applyStoredChoice()

        // Provide ReviewRequestManager to ReviewEntryPoint (Koin 4.x compatible).
        ReviewEntryPoint.setManager(getKoin().get())

        // Track app launch for in-app review heuristics.
        ReviewEntryPoint.notifyAppLaunched()
    }

    private fun initDependencyContainer() {
        DependencyContainer.initialize(this)
    }
}
