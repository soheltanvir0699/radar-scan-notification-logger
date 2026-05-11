package io.shareit.transfer.util

import android.content.Context

object PhotoLoader {

    private const val PHOTO_FOLDER = "photos"
    private const val VIDEO_FOLDER = "videos"
    private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp")
    private val VIDEO_EXTENSIONS = setOf("mp4", "m4v", "webm", "mkv")

    fun listPhotoUris(context: Context): List<String> =
        listAssetUris(context, PHOTO_FOLDER, IMAGE_EXTENSIONS)

    fun listVideoAssetPaths(context: Context): List<String> {
        return try {
            context.assets.list(VIDEO_FOLDER)
                ?.filter { name -> name.substringAfterLast('.', "").lowercase() in VIDEO_EXTENSIONS }
                ?.sorted()
                ?.map { "$VIDEO_FOLDER/$it" }
                ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun listAssetUris(context: Context, folder: String, exts: Set<String>): List<String> {
        return try {
            context.assets.list(folder)
                ?.filter { name -> name.substringAfterLast('.', "").lowercase() in exts }
                ?.sorted()
                ?.map { "file:///android_asset/$folder/$it" }
                ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }
}
