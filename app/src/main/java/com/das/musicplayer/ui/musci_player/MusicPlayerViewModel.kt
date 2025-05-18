package com.das.musicplayer.ui.musci_player

import android.app.Application
import android.content.ContentUris
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MusicPlayerViewModel(applicationContext: Application) : AndroidViewModel(applicationContext) {

    private val _listMusic = MutableStateFlow<List<MediaItem>>(emptyList())
    val listMusic: StateFlow<List<MediaItem>> = _listMusic

    fun fetchAllAudioFiles() {
        val context = getApplication<Application>().applicationContext
        viewModelScope.launch(Dispatchers.IO) {
            val musicList = mutableListOf<MediaItem>()

            val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DATE_MODIFIED,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATA
            )
            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
            val sortOrder = "${MediaStore.Audio.Media.DATE_MODIFIED} DESC"

            context.contentResolver.query(
                collection,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    val date = cursor.getLong(dateColumn) * 1000 // seconds to milliseconds
                    val size = cursor.getLong(sizeColumn)

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id
                    )

                    val metadata = MediaMetadata.Builder()
                        .setTitle(name.removeSuffix(".mp3"))
                        .setDescription(formatDate(date))
                        .build()

                    val mediaItem = MediaItem.Builder()
                        .setMediaId(contentUri.toString())
                        .setUri(contentUri)
                        .setMediaMetadata(metadata)
                        .setTag(formatFileSize(size))
                        .build()

                    musicList.add(mediaItem)
                }
            }

            _listMusic.value = musicList
        }
    }


    private fun formatDate(timestamp: Long): String {
        val date = Date(timestamp)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(date)
    }


    private fun formatFileSize(sizeInBytes: Long): String {
        return when {
            sizeInBytes >= 1_073_741_824 -> String.format(Locale.ROOT, "%.2f GB", sizeInBytes / 1_073_741_824.0)
            sizeInBytes >= 1_048_576 -> String.format(Locale.ROOT, "%.2f MB", sizeInBytes / 1_048_576.0)
            sizeInBytes >= 1_024 -> String.format(Locale.ROOT, "%.2f KB", sizeInBytes / 1_024.0)
            else -> String.format(Locale.ROOT, "%d bytes", sizeInBytes)
        }
    }
}