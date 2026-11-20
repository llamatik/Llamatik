package com.llamatik.app.android.core.di

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.google.firebase.BuildConfig
import com.llamatik.app.android.privacy.FirebaseDiagnosticsCollector
import com.llamatik.app.di.appModule
import com.llamatik.app.feature.privacy.DiagnosticsCollector
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.module.Module
import org.koin.dsl.module

object DependencyContainer {
    @VisibleForTesting
    var testModules: MutableList<Module> = mutableListOf()

    private val internalModules =
        sequence {
            yieldAll(appModule())
            yield(module { single<DiagnosticsCollector> { FirebaseDiagnosticsCollector(get<Context>()) } })
            yieldAll(testModules)
        }

    @JvmStatic
    fun initialize(context: Context) {
        startKoin {
            if (BuildConfig.DEBUG) {
                androidLogger(Level.ERROR)
            }
            androidContext(context)
            modules(internalModules.toList())
        }
    }
}
