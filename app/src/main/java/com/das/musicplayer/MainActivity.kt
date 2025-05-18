package com.das.musicplayer

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WatchLater
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.util.Consumer
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.state.rememberNextButtonState
import androidx.media3.ui.compose.state.rememberPlayPauseButtonState
import androidx.media3.ui.compose.state.rememberPreviousButtonState
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.das.musicplayer.PlayerHolder.exoPlayer
import com.das.musicplayer.dataAndNames.ListOfDataClasses.TopAppBarNav
import com.das.musicplayer.ui.homepage.HomePageComposable
import com.das.musicplayer.ui.musci_player.MarqueeText
import com.das.musicplayer.ui.musci_player.MusicPlayerScreen
import com.das.musicplayer.ui.settings.SettingsComposable

class MainActivity: ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CustomTheme {
                MusicPlayerScreen(rememberNavController())
            }
        }
    }

    @Composable
    fun MainLauncherPageComposable() {

        val navController = rememberNavController()


        val mContext = LocalContext.current

        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route


        val activity = (mContext.getActivity() as ComponentActivity)
        val listener = Consumer<Intent> {
            listenNewIntent(navController, it)
        }


        val lifecycleOwner = LocalLifecycleOwner.current

        LaunchedEffect(lifecycleOwner) {
            activity.addOnNewIntentListener(listener)
        }


        CustomTheme {
            val bottomNavigationItems = listOf(
                TopAppBarNav(
                    title = "Home",
                    selectedIcon = Icons.Filled.Home,
                    unselectedIcon = Icons.Outlined.Home
                ),
                TopAppBarNav(
                    title = "Recently Watched",
                    selectedIcon = Icons.Filled.WatchLater,
                    unselectedIcon = Icons.Outlined.WatchLater
                ),
                TopAppBarNav(
                    title = "Setting",
                    selectedIcon = Icons.Filled.Settings,
                    unselectedIcon = Icons.Outlined.Settings
                )
            )

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    if (currentRoute in listOf("Home", "Setting", "ExoPlayerUI")) {
                        NavigationBar(
                            windowInsets = NavigationBarDefaults.windowInsets,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12))
                                .background(MaterialTheme.colorScheme.background)
                        ) {
                            bottomNavigationItems.forEachIndexed { _, items ->
                                NavigationBarItem(
                                    selected = currentRoute == items.title,
                                    onClick = {
                                        if (currentRoute != items.title) {
                                            navController.navigate(items.title) {
                                                // Avoid multiple copies of the same destination
                                                popUpTo(navController.graph.startDestinationId) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentRoute == items.title) items.selectedIcon else items.unselectedIcon,
                                            contentDescription = items.title
                                        )
                                    },
                                    label = {
                                        Text(text = items.title)
                                    }
                                )
                            }

                        }
                    }
                },
                bottomBar = {
                    MiniPlayerUI("nothing", "null")
                }
            ) { paddingValues ->

                NavHost(
                    navController = navController, startDestination = "ExoPlayerUI",
                    modifier = Modifier.padding(paddingValues)
                ) {
                    composable("Home") {
                        HomePageComposable(navController)
                    }
                    composable("Setting") {
                        SettingsComposable(navController)
                    }

                    composable("ExoPlayerUI") {
                        MusicPlayerScreen(navController)
//                        bundles.getString(PLAY_HERE_VIDEO).toString()
                    }

                }
            }

        }



        DisposableEffect(Unit) {

            onDispose {
//                activity.removeOnNewIntentListener(listener)
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


    private fun listenNewIntent(
        navController: NavController,
        newIntent: Intent
    ) {
        if (newIntent.action == Intent.ACTION_SEND) {
            val intentType = newIntent.type.toString()
            if (intentType.startsWith("audio/")) {
                TODO()
            }
        } else if (newIntent.action == Intent.ACTION_VIEW) {
            TODO()
        }
    }

    private fun Context.getActivity(): Activity {
        if (this is Activity) return this
        return if (this is ContextWrapper) baseContext.getActivity() else getActivity()
    }


}