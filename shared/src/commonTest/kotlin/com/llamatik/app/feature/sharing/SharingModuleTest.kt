package com.llamatik.app.feature.sharing

import com.llamatik.app.feature.sharing.viewmodel.ShareDeviceViewModel
import com.llamatik.host.LoadedModels
import com.llamatik.host.LocalInferenceActivity
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SharingModuleTest {

    // Voyager's screenModelScope runs on Dispatchers.Main, which a JVM test does not have.
    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
        LoadedModels.generation.value = null
        LoadedModels.embedding.value = null
        LoadedModels.isGenerating.value = false
    }

    // Regression: the screen crashed because the module asked Koin for a dependency
    // (Assistant) that needs a runtime parameter. Resolving the real definitions catches that.
    @Test
    fun `ShareDeviceViewModel resolves from the real sharing module`() {
        val koin = startKoin { modules(module { single<Settings> { MapSettings() } }, sharingModule) }.koin

        val viewModel = koin.get<ShareDeviceViewModel>()

        assertEquals("8080", viewModel.state.value.portText)
        assertFalse(viewModel.state.value.isOn)
    }

    @Test
    fun `repository exposes the models and busy state the main chat publishes`() {
        val koin = startKoin { modules(module { single<Settings> { MapSettings() } }, sharingModule) }.koin
        val repository = koin.get<ShareDeviceRepository>()

        LoadedModels.isGenerating.value = true
        assertTrue(LoadedModels.isGenerating.value)
        LoadedModels.isGenerating.value = false

        LocalInferenceActivity.begin()
        assertTrue(LocalInferenceActivity.isBusy)
        LocalInferenceActivity.end()
        assertFalse(LocalInferenceActivity.isBusy)

        repository.port = 9090
        assertEquals(9090, repository.port)
    }
}
