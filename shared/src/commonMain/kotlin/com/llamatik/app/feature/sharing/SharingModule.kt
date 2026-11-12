package com.llamatik.app.feature.sharing

import com.llamatik.app.feature.sharing.viewmodel.ShareDeviceViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/** "Share this device": needs only app [com.russhwolf.settings.Settings]. */
val sharingModule = module {
    singleOf(::ShareDeviceRepository)
    factoryOf(::ShareDeviceViewModel)
}
