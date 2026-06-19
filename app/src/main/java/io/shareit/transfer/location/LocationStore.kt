package io.shareit.transfer.location

import android.content.Context
import java.io.File
import kotlin.random.Random

object LocationStore {

    private const val DIR_NAME = "location_history"
    private const val FILE_NAME = "points.jsonl"
    private const val MAX_ENTRIES = 2000
    private const val ROTATE_PROBABILITY_DENOM = 50

    private val lock = Any()

    fun append(context: Context, point: LocationPoint) {
        synchronized(lock) {
            val file = ensureFile(context)
            file.appendText(point.toJsonLine() + "\n")
            if (Random.nextInt(ROTATE_PROBABILITY_DENOM) == 0) {
                trimIfNeeded(file)
            }
        }
    }

    fun readAll(context: Context): List<LocationPoint> {
        synchronized(lock) {
            val file = file(context)
            if (!file.exists()) return emptyList()
            return file.bufferedReader().useLines { lines ->
                lines.mapNotNull(LocationPoint::fromJsonLine).toList()
            }.sortedByDescending { it.capturedAt }
        }
    }

    fun latest(context: Context): LocationPoint? = readAll(context).firstOrNull()

    fun clear(context: Context) {
        synchronized(lock) {
            file(context).delete()
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
