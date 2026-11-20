package com.llamatik.app.feature.privacy

import com.llamatik.app.localization.AvailableLanguages
import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Records every call so the tests can assert exactly what would reach Firebase. */
private class FakeCollector : DiagnosticsCollector {
    val enabledCalls = mutableListOf<Boolean>()
    var discarded = 0

    override fun setEnabled(enabled: Boolean) {
        enabledCalls += enabled
    }

    override fun discardPending() {
        discarded++
    }
}

class DiagnosticsConsentRepositoryTest {

    @Test
    fun `diagnostics are off by default`() {
        val repo = DiagnosticsConsentRepository(MapSettings(), FakeCollector())
        assertFalse(repo.enabled.value)
    }

    @Test
    fun `a fresh install keeps collection disabled at launch`() {
        val collector = FakeCollector()
        DiagnosticsConsentRepository(MapSettings(), collector).applyStoredChoice()
        // The only thing sent to the platform at launch is "off"; nothing is ever enabled without a choice.
        assertEquals(listOf(false), collector.enabledCalls)
    }

    @Test
    fun `opting in enables collection and survives a restart`() {
        val settings = MapSettings()
        val first = FakeCollector()
        DiagnosticsConsentRepository(settings, first).setEnabled(true)
        assertEquals(listOf(true), first.enabledCalls)

        // "Restart": a new repository over the same stored settings re-applies the choice.
        val second = FakeCollector()
        val repo = DiagnosticsConsentRepository(settings, second)
        assertTrue(repo.enabled.value)
        repo.applyStoredChoice()
        assertEquals(listOf(true), second.enabledCalls)
    }

    @Test
    fun `opting out disables collection, drops pending data and survives a restart`() {
        val settings = MapSettings()
        val collector = FakeCollector()
        val repo = DiagnosticsConsentRepository(settings, collector)
        repo.setEnabled(true)
        repo.setEnabled(false)

        assertEquals(listOf(true, false), collector.enabledCalls)
        assertEquals(1, collector.discarded)
        assertFalse(repo.enabled.value)

        val restarted = FakeCollector()
        val after = DiagnosticsConsentRepository(settings, restarted)
        after.applyStoredChoice()
        assertEquals(listOf(false), restarted.enabledCalls)
    }

    @Test
    fun `turning it on never discards data`() {
        val collector = FakeCollector()
        DiagnosticsConsentRepository(MapSettings(), collector).setEnabled(true)
        assertEquals(0, collector.discarded)
    }

    @Test
    fun `platforms without a collector report unsupported and do not crash`() {
        val repo = DiagnosticsConsentRepository(MapSettings(), collector = null)
        assertFalse(repo.isSupported)
        repo.applyStoredChoice()
        repo.setEnabled(true)
        repo.setEnabled(false)
    }

    @Test
    fun `privacy policy link is localized`() {
        assertEquals("https://www.llamatik.com/privacy/", privacyPolicyUrl(AvailableLanguages.EN))
        assertEquals("https://www.llamatik.com/es/privacy/", privacyPolicyUrl(AvailableLanguages.ES))
        assertEquals("https://www.llamatik.com/zh/privacy/", privacyPolicyUrl(AvailableLanguages.CN))
        assertEquals("https://www.llamatik.com/fa/privacy/", privacyPolicyUrl(AvailableLanguages.FA))
    }
}
