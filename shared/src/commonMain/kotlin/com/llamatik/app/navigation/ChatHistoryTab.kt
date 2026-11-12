package com.llamatik.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import cafe.adriel.voyager.transitions.SlideTransition
import com.llamatik.app.feature.chathistory.ChatHistoryTabScreen
import com.llamatik.app.localization.getCurrentLocalization
import com.llamatik.app.ui.icon.LlamatikIcons

internal object ChatHistoryTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(LlamatikIcons.ChatHistory)
            val title = getCurrentLocalization().historyTitle
            return remember(title) {
                TabOptions(index = 1u, title = title, icon = icon)
            }
        }

    @Composable
    override fun Content() {
        Navigator(ChatHistoryTabScreen()) {
            SlideTransition(it)
        }
    }
}
