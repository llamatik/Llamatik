package com.llamatik.app.feature.chatbot

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinNavigatorScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.llamatik.app.feature.chatbot.viewmodel.ChatBotViewModel
import com.llamatik.app.feature.chatbot.viewmodel.VideoData
import com.llamatik.app.localization.getCurrentLocalization
import com.llamatik.app.platform.rgbaToImageBitmap
import com.llamatik.app.ui.icon.LlamatikIcons
import com.llamatik.app.ui.theme.LlamatikTheme
import kotlinx.coroutines.delay

class VideoPlayerScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val localization = getCurrentLocalization()
        val viewModel = navigator.koinNavigatorScreenModel<ChatBotViewModel>()
        val state by viewModel.state.collectAsState()
        val videoData = state.selectedVideoForPlayback

        DisposableEffect(Unit) {
            onDispose {
                viewModel.clearSelectedVideo()
            }
        }

        LlamatikTheme {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {},
                        navigationIcon = {
                            IconButton(onClick = { navigator.pop() }) {
                                Icon(
                                    imageVector = LlamatikIcons.ArrowBack,
                                    contentDescription = localization.backLabel
                                )
                            }
                        },
                        actions = {
                            if (videoData != null) {
                                IconButton(
                                    onClick = {
                                        viewModel.onSaveVideo(
                                            videoData.frames,
                                            videoData.width,
                                            videoData.height,
                                            videoData.fps
                                        )
                                    }
                                ) {
                                    Icon(
                                        imageVector = LlamatikIcons.Save,
                                        contentDescription = localization.saveVideoToGallery
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    if (videoData != null) {
                        VideoPlayer(videoData = videoData)
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoPlayer(videoData: VideoData) {
    val frames = videoData.frames
    val fps = videoData.fps.coerceAtLeast(1)
    val frameDelayMs = 1000L / fps

    var currentFrameIndex by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(true) }

    val currentBitmap: ImageBitmap? = remember(currentFrameIndex) {
        if (frames.isEmpty()) return@remember null
        rgbaToImageBitmap(videoData.width, videoData.height, frames[currentFrameIndex])
    }

    LaunchedEffect(isPlaying) {
        if (!isPlaying || frames.isEmpty()) return@LaunchedEffect
        while (isPlaying) {
            delay(frameDelayMs)
            currentFrameIndex = (currentFrameIndex + 1) % frames.size
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.weight(1f, fill = true),
            contentAlignment = Alignment.Center
        ) {
            if (currentBitmap != null) {
                Image(
                    bitmap = currentBitmap,
                    contentDescription = "Video frame",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        if (frames.isNotEmpty()) {
            val progress = (currentFrameIndex + 1).toFloat() / frames.size
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .width(280.dp)
            )
        }

        Row(
            modifier = Modifier.padding(bottom = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    currentFrameIndex = 0
                    isPlaying = true
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = LlamatikIcons.Stop,
                    contentDescription = "Restart",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.width(16.dp))
            IconButton(
                onClick = { isPlaying = !isPlaying },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = if (isPlaying) LlamatikIcons.Stop else LlamatikIcons.PlayCircle,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}
