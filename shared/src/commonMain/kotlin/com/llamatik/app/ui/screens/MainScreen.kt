package com.llamatik.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.llamatik.app.navigation.ChatBotTab
import com.llamatik.app.navigation.ChatHistoryTab
import com.llamatik.app.navigation.SettingsTab
import com.llamatik.app.platform.RootNavigatorRepository
import com.llamatik.app.platform.RootSnackbarHostStateRepository
import com.llamatik.app.ui.components.LLamatikNavigationBar
import com.llamatik.app.ui.components.LlamatikNavigationBarItem
import com.llamatik.app.ui.components.LlamatikNavigationDefaults
import com.llamatik.app.ui.theme.LlamatikTheme
import com.llamatik.app.ui.theme.Typography
import org.koin.core.parameter.ParametersHolder
import org.koin.mp.KoinPlatform

class MainScreen : Screen {
    @Composable
    override fun Content() {
        LlamatikTheme {
            MainScreenView()
        }
    }
}

@Composable
fun MainScreenView() {
    val snackbarHostState = remember { SnackbarHostState() }
    val rootNavigator = LocalNavigator.currentOrThrow
    TabNavigator(
        ChatBotTab,
        disposeNestedNavigators = false,
    ) { _ ->
        val rootNavigatorRepository = setupRootNavigator(rootNavigator, LocalTabNavigator.current)
        val rootSnackbarHostStateRepository = setupRootSnackbarHostState(snackbarHostState)

        // Scaffold fills the whole window; only the *content* and nav items are capped
        // and centered, so wide desktop/web windows don't look like a cropped phone screen.
        Scaffold(
            modifier = androidx.compose.ui.Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(rootSnackbarHostStateRepository.snackbarHostState) },
            bottomBar = {
                Box(
                    modifier = androidx.compose.ui.Modifier
                        .fillMaxWidth()
                        .background(LlamatikNavigationDefaults.navigationContainerColor()),
                    contentAlignment = Alignment.Center,
                ) {
                    LLamatikNavigationBar(
                        modifier = androidx.compose.ui.Modifier.widthIn(max = NAV_BAR_MAX_WIDTH),
                    ) {
                        TabNavigationItem(ChatBotTab, rootNavigatorRepository)
                        TabNavigationItem(ChatHistoryTab, rootNavigatorRepository)
                        //TabNavigationItem(CompanionTab, rootNavigatorRepository)
                        TabNavigationItem(SettingsTab, rootNavigatorRepository)
                    }
                }
            },
        ) { paddingValues ->
            Box(
                modifier = androidx.compose.ui.Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.TopCenter,
            ) {
                Box(
                    modifier = androidx.compose.ui.Modifier
                        .widthIn(max = CONTENT_MAX_WIDTH)
                        .fillMaxHeight(),
                ) {
                    SlideTransition(LocalNavigator.currentOrThrow) { screen ->
                        screen.Content()
                    }
                }
            }
        }
    }
}

private val CONTENT_MAX_WIDTH = 1280.dp
private val NAV_BAR_MAX_WIDTH = 600.dp

fun setupRootNavigator(
    rootNavigator: Navigator,
    tabNavigator: TabNavigator,
): RootNavigatorRepository {
    val koin = KoinPlatform.getKoin()
    return koin.get(
        null,
        parameters = {
            ParametersHolder(
                listOf(rootNavigator, tabNavigator).toMutableList(),
                false
            )
        }
    )
}

fun setupRootSnackbarHostState(snackbarHostState: SnackbarHostState): RootSnackbarHostStateRepository {
    val koin = KoinPlatform.getKoin()
    return koin.get(
        null,
        parameters = { ParametersHolder(listOf(snackbarHostState).toMutableList(), false) })
}

@Composable
private fun RowScope.TabNavigationItem(tab: Tab, rootNavigator: RootNavigatorRepository) {
    val tabNavigator = LocalTabNavigator.current
    val currentDestination = tabNavigator.current.key == tab.key

    LlamatikNavigationBarItem(
        selected = currentDestination,
        onClick = {
            tabNavigator.current = tab
        },
        icon = {
            tab.options.icon?.let {
                Icon(
                    painter = it,
                    contentDescription = tab.options.title
                )
            }
        },
        label = {
            Text(
                text = tab.options.title,
                style = Typography.get().labelSmall
            )
        }
    )
}
