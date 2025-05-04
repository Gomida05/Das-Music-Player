package com.das.musicplayer.dataAndNames

import android.net.Uri
import androidx.compose.ui.graphics.vector.ImageVector

object ListOfDataClasses {

    data class ListMusic(
        val title: String,
        val pathOfMusic: Uri,
        val dateTime: String,
        val fileSize: String,
    )

    data class TopAppBarNav(
        val title: String,
        val selectedIcon: ImageVector,
        val unselectedIcon: ImageVector
    )
}