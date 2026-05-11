package io.shareit.transfer.files

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class MediaItem(
    val id: Long,
    val displayName: String,
    val sizeBytes: Long,
    val uri: Uri,
)

object MediaRepository {

    suspend fun queryVideos(context: Context): List<MediaItem> =
        withContext(Dispatchers.IO) { queryMedia(context, MediaStore.Video.Media.EXTERNAL_CONTENT_URI) }

    suspend fun queryMusic(context: Context): List<MediaItem> =
        withContext(Dispatchers.IO) { queryMedia(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI) }

    suspend fun queryImages(context: Context): List<MediaItem> =
        withContext(Dispatchers.IO) { queryMedia(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI) }

    private fun queryMedia(context: Context, collection: Uri): List<MediaItem> {
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.SIZE,
        )
        val sort = "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
        val list = ArrayList<MediaItem>()
        context.contentResolver.query(collection, projection, null, null, sort)?.use { c ->
            while (c.moveToNext()) {
                val id = c.getLongId()
                val name = c.getStringOrNull(MediaStore.MediaColumns.DISPLAY_NAME) ?: "file"
                val size = c.getLongOrZero(MediaStore.MediaColumns.SIZE)
                val uri = ContentUris.withAppendedId(collection, id)
                list.add(MediaItem(id, name, size, uri))
            }
        }
        return list
    }

    private fun Cursor.getLongId(): Long =
        getColumnIndex(MediaStore.MediaColumns._ID).takeIf { it >= 0 }?.let { getLong(it) } ?: -1L

    private fun Cursor.getStringOrNull(column: String): String? {
        val idx = getColumnIndex(column)
        return if (idx >= 0) getString(idx) else null
    }

    private fun Cursor.getLongOrZero(column: String): Long {
        val idx = getColumnIndex(column)
        return if (idx >= 0) getLong(idx) else 0L
    }

    fun mediaPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                android.Manifest.permission.READ_MEDIA_VIDEO,
                android.Manifest.permission.READ_MEDIA_AUDIO,
                android.Manifest.permission.READ_MEDIA_IMAGES,
            )
        } else {
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
}
