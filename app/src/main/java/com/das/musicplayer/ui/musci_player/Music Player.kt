package com.das.musicplayer.ui.musci_player

import android.annotation.SuppressLint
import android.content.Intent
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.OptIn
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.state.rememberNextButtonState
import androidx.media3.ui.compose.state.rememberPlayPauseButtonState
import androidx.media3.ui.compose.state.rememberPreviousButtonState
import androidx.navigation.NavController
import com.das.musicplayer.PlayerHolder.exoPlayer
import com.das.musicplayer.PlayerHolder.mediaSession
import com.das.musicplayer.PlayerHolder.requestAudioFocusFromMain
import com.das.musicplayer.mediacontrol.MediaActionsListener
import com.das.musicplayer.notification.MusicNotificationForegroundService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds


@Composable
fun MusicPlayerScreen(navController: NavController) {


    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val screenHeightPx = with(LocalDensity.current) { screenHeight.toPx() }
    val mContext = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val miniPlayerHeight = 64.dp
    val miniPlayerHeightPx = with(LocalDensity.current) { miniPlayerHeight.toPx() }

    val collapsedOffset = screenHeightPx - miniPlayerHeightPx
    val expandedOffset = 0f


    // Define the vertical offset
    val offsetY = remember { Animatable(0f) }

    // States to track expanded/collapsed
    val isExpanded = remember { derivedStateOf { offsetY.value < collapsedOffset / 2 } }


    val viewModel: MusicPlayerViewModel = viewModel()



    val musicListFiles by viewModel.listMusic.collectAsState()



    val musicsPath = "/storage/emulated/0/Music/ForUI"

    exoPlayer = remember(mContext) {
        ExoPlayer.Builder(mContext).build()
    }

    mediaSession = MediaSessionCompat(mContext, "ms").apply {
        isActive = true
        setCallback(MediaActionsListener())
    }
    LaunchedEffect(Unit) {
        viewModel.fetchDataFromDatabase(musicsPath)
    }



    var currentTitle by remember { mutableStateOf("None") }
    var currentArtists by remember { mutableStateOf("None" ) }
    var currentValue by remember { mutableLongStateOf(0L) }
    var isPlayingExo by remember { mutableStateOf(false) }


    if (musicListFiles.isNotEmpty()){
        exoPlayer?.addMediaItems(musicListFiles)
    }

    DisposableEffect(Unit) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                isPlayingExo = isPlaying
            }
        }
        exoPlayer?.addListener(listener)
        onDispose {
            exoPlayer?.removeListener(listener)
        }
    }

    if (isPlayingExo) {
        LaunchedEffect(Unit) {
            while(true) {
                currentValue = exoPlayer?.currentPosition!!
                delay(1.seconds / 30)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Main content (e.g. your app's main screen)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp)
        ){
            YourMainContent(
                musicListFiles
            ) {index, title, artist ->
                currentTitle = title
                currentArtists = artist
                coroutineScope.launch {
                    offsetY.animateTo(expandedOffset)
                }
                exoPlayer?.seekTo(index, 0)
                exoPlayer?.prepare()
                exoPlayer?.play()

                requestAudioFocusFromMain(mContext, exoPlayer)

                mContext.startService(
                    Intent(mContext, MusicNotificationForegroundService::class.java)
                )


            }
        }


        // Music Player UI
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(screenHeight)
                .offset { IntOffset(0, offsetY.value.roundToInt()) }
                .draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta ->
                        coroutineScope.launch {
                            offsetY.snapTo(
                                (offsetY.value + delta).coerceIn(
                                    expandedOffset,
                                    collapsedOffset
                                )
                            )
                        }
                    },
                    onDragStopped = {
                        coroutineScope.launch {
                            if (offsetY.value > collapsedOffset / 2) {
                                // Collapse
                                offsetY.animateTo(collapsedOffset)
                            } else {
                                // Expand
                                offsetY.animateTo(expandedOffset)
                            }
                        }
                    }
                )
//                .background(Color.DarkGray)
        ) {
            if (isExpanded.value) {
                FullPlayerUI(currentTitle, currentArtists)
            } else {
                MiniPlayerUI(currentTitle, currentArtists)
            }
        }
    }
}


@SuppressLint("UnsafeOptInUsageError")
@Composable
fun FullPlayerUI(currentTitle: String, currentArtists: String) {

    var currentPosition by remember { mutableLongStateOf(0L) }
    var totalDuration by remember { mutableLongStateOf(0L) }
    var bufferedPercent by remember { mutableIntStateOf(0) }

    val isPaused = rememberPlayPauseButtonState(exoPlayer!!)

    val nextButtonState = rememberNextButtonState(exoPlayer!!)
    val previousButtonState = rememberPreviousButtonState(exoPlayer!!)

    LaunchedEffect(exoPlayer) {
        while (true) {
            currentPosition = exoPlayer?.currentPosition!!
            totalDuration = exoPlayer?.duration?.takeIf { it > 0 } ?: 0L
            bufferedPercent = exoPlayer?.bufferedPercentage!!
            delay(500L) // Update every half second
        }
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar or drag handle
        Box(
            modifier = Modifier
                .size(width = 40.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.Gray)
        )

        Spacer(modifier = Modifier.height(44.dp))

        // Album Art
        Box(
            modifier = Modifier
                .size(300.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.DarkGray)
        ){
            Icon(
                imageVector = Icons.Default.MusicNote,
                "",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Song Title and Artist
        MarqueeText(
            currentTitle,
            textStyle = TextStyle(
                color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold
            )
        )
        Text(currentArtists, color = Color.Gray, fontSize = 16.sp)

        Spacer(modifier = Modifier.height(32.dp))

        // Playback progress (interactive)
        if (totalDuration > 0) {
            Slider(
                value = currentPosition.coerceIn(0, totalDuration).toFloat(),
                onValueChange = {
                    exoPlayer?.seekTo(it.toLong())
                },
                valueRange = 0f..totalDuration.toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFBB86FC),
                    activeTrackColor = Color(0xFFBB86FC),
                    inactiveTrackColor = Color.LightGray
                ),
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
        else{
            LinearProgressIndicator(
                modifier = Modifier.width(64.dp),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = formatTimeForDisplay(currentPosition),
                maxLines = 1,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.CenterStart)
            )
            Text(
                text = formatTimeForDisplay(totalDuration),
                maxLines = 1,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Playback controls
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            IconButton(
                onClick = previousButtonState::onClick,
                enabled = previousButtonState.isEnabled
            ) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = Color.White)
            }

            IconButton(onClick = isPaused::onClick, enabled = isPaused.isEnabled) {
                Icon(
                    if (isPaused.showPlay) Icons.Default.PlayArrow
                    else Icons.Default.Pause, contentDescription = if (isPaused.showPlay) "Play" else "Pause", tint = Color.White, modifier = Modifier.size(48.dp)
                )
            }

            IconButton(
                onClick = nextButtonState::onClick,
                enabled = nextButtonState.isEnabled
            ) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White)
            }
        }
    }
}


@OptIn(UnstableApi::class)
@Composable
fun MiniPlayerUI(currentTitle: String, currentArtists: String) {

    val previousButtonState = rememberPreviousButtonState(exoPlayer!!)
    val nextButtonState = rememberNextButtonState(exoPlayer!!)
    val playPauseButtonState = rememberPlayPauseButtonState(exoPlayer!!)

    val playerTitle = (exoPlayer?.currentMediaItem?.mediaMetadata?.title ?: currentTitle).toString()
    val playerArtists = (exoPlayer?.currentMediaItem?.mediaMetadata?.title ?: currentArtists).toString()

    var currentPosition by remember { mutableLongStateOf(0L) }
    var totalDuration by remember { mutableLongStateOf(0L) }
    var progress by remember { mutableFloatStateOf(0f) }


    LaunchedEffect(exoPlayer) {
        if (exoPlayer != null) {
            currentPosition = exoPlayer?.currentPosition!!
            totalDuration = exoPlayer?.duration?.takeIf { it > 0 } ?: 0L

            progress = if (totalDuration > 0L) currentPosition.toFloat() / totalDuration.toFloat() else 0f

        }
    }


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
//            .background(Color(0xFF1C1C1C))
            .clip(RoundedCornerShape(33))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
                .padding(end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

                // Album art
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Gray)
                ){
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        "",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Song info
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                ) {
                    MarqueeText(
                        playerTitle,
                        textStyle = TextStyle(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        playerArtists,
                        color = Color.Gray,
                        fontSize = 10.sp,
                        maxLines = 1
                    )


                    if (totalDuration > 0) {

                        LinearProgressIndicator(
                            progress = {progress.coerceIn(0f, 1f)},
                            color = Color(0xFFBB86FC),
                            trackColor = Color.LightGray,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                        )
                    }
                }
            Spacer(modifier = Modifier.width(5.dp))
                IconButton(
                    onClick = previousButtonState::onClick,
                    enabled = previousButtonState.isEnabled
                ) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = Color.White)
                }

                IconButton(
                    onClick = playPauseButtonState::onClick,
                    enabled = playPauseButtonState.isEnabled
                ) {
                    Icon(
                        if (playPauseButtonState.showPlay) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (playPauseButtonState.showPlay) "Play" else "Pause", tint = Color.White
                    )
                }

                IconButton(
                    onClick = nextButtonState::onClick,
                    enabled = nextButtonState.isEnabled
                ) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White)
                }
            }

    }
}

@Composable
fun YourMainContent(musicListFiles: List<MediaItem>, onSongClick: (index: Int, title: String, artist: String) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(bottom = 64.dp), // Leave room for mini player
        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 16.dp)
    ) {
        itemsIndexed(musicListFiles) { index, searchItem ->
            Card(
                onClick = {
                    onSongClick(index, searchItem.mediaMetadata.title.toString(), searchItem.mediaMetadata.description.toString())
                },
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12))
                ){
                SongListItem(
                    title = searchItem.mediaMetadata.title.toString(),
                    artist = searchItem.mediaMetadata.description.toString()
                )
            }
        }
    }
}


@Composable
fun SongListItem(title: String, artist: String) {

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.CenterStart)
        ) {
            // Album art placeholder
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.DarkGray)
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    "",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Song info
            Column {
                Text(text = title, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 2)
                Text(text = artist, color = Color.Gray, fontSize = 10.sp, maxLines = 1)
            }
            Spacer(modifier = Modifier.width(4.dp))
        }

        IconButton(
            onClick = { },
            modifier = Modifier
                .align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                ""
            )
        }

    }
}


@Composable
fun MarqueeText(
    text: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = TextStyle.Default,
    velocity: Float = 80f
) {
    val scrollState = rememberScrollState()


    LaunchedEffect(Unit) {
        while (true) {
            scrollState.scrollTo(0)
            val distance = scrollState.maxValue.toFloat()
            val durationMillis = (distance / velocity * 1000).toInt()
            delay(500)
            if (durationMillis > 0) {
                scrollState.animateScrollTo(
                    scrollState.maxValue,
                    animationSpec = tween(durationMillis, easing = LinearEasing)
                )
            }
        }
    }

    Box(
        modifier = modifier
            .horizontalScroll(scrollState, enabled = false) // Disable user scroll
            .clipToBounds()
    ) {

        Text(
            text = text,
            style = textStyle,
            softWrap = false,
            maxLines = 1,
            overflow = TextOverflow.Visible,
        )
    }
}



fun formatTimeForDisplay(ms: Long): String {
    val totalSeconds = ms / 1000
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = (totalSeconds / 3600) % 24
    val days = totalSeconds / (3600 * 24)

    return if (days > 0) {
        String.format(Locale.ENGLISH, "%02d:%02d :%02d:%02d", days, hours, minutes, seconds)
    } else if(hours>0) {
        String.format(Locale.ENGLISH, "%02d:%02d:%02d", hours, minutes, seconds)
    }else{
        String.format(Locale.ENGLISH, "%02d:%02d", minutes, seconds)
    }
}


