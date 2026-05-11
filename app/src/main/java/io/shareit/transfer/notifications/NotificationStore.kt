package io.shareit.transfer.notifications

import android.content.Context
import java.io.File
import kotlin.random.Random

/**
 * Tiny append-only persistence for captured notifications. Each entry is a single
 * JSON object on its own line so writes stay cheap and the file is easy to inspect.
 * The store keeps roughly the last [MAX_ENTRIES] notifications by rotating
 * occasionally rather than rewriting on every append.
 */
object NotificationStore {

    private const val DIR_NAME = "captured_notifications"
    private const val FILE_NAME = "stream.jsonl"
    private const val MAX_ENTRIES = 5000
    private const val ROTATE_PROBABILITY_DENOM = 100

    private val lock = Any()

    fun append(context: Context, notification: CapturedNotification) {
        synchronized(lock) {
            val file = ensureFile(context)
            file.appendText(notification.toJsonLine() + "\n")
            if (Random.nextInt(ROTATE_PROBABILITY_DENOM) == 0) {
                trimIfNeeded(file)
            }
        }
    }

    fun readAll(context: Context): List<CapturedNotification> {
        synchronized(lock) {
            val file = file(context)
            if (!file.exists()) return emptyList()
            return file.bufferedReader().useLines { lines ->
                lines.mapNotNull(CapturedNotification::fromJsonLine).toList()
            }.sortedByDescending { it.postedAt }
        }
    }

    fun clear(context: Context) {
        synchronized(lock) {
            file(context).delete()
        }
    }

    fun clearByPackage(context: Context, packageName: String) {
        synchronized(lock) {
            val file = file(context)
            if (!file.exists()) return
            val kept = file.bufferedReader().useLines { lines ->
                lines.mapNotNull(CapturedNotification::fromJsonLine)
                    .filterNot { it.packageName == packageName }
                    .map(CapturedNotification::toJsonLine)
                    .toList()
            }
            if (kept.isEmpty()) {
                file.delete()
            } else {
                file.writeText(kept.joinToString(separator = "\n", postfix = "\n"))
            }
        }
    }

    fun count(context: Context): Int {
        synchronized(lock) {
            val file = file(context)
            if (!file.exists()) return 0
            return file.bufferedReader().useLines { it.count() }
        }
    }

    private fun ensureFile(context: Context): File {
        val dir = File(context.filesDir, DIR_NAME)
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, FILE_NAME)
        if (!file.exists()) file.createNewFile()
        return file
    }

    private fun file(context: Context): File = File(File(context.filesDir, DIR_NAME), FILE_NAME)

    private fun trimIfNeeded(file: File) {
        val lines = file.readLines()
        if (lines.size <= MAX_ENTRIES) return
        val kept = lines.takeLast(MAX_ENTRIES)
        file.writeText(kept.joinToString(separator = "\n", postfix = "\n"))
    }
}
