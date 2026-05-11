package io.shareit.transfer.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Minimal Bluetooth file pipe: sender writes [int nameLen][name utf8][long size][raw bytes],
 * receiver reads the same. Both sides use [TRANSFER_UUID]. Receiver must be listening
 * ([acceptOneFileToDir]) before the sender connects.
 */
object BluetoothTransfer {

    val TRANSFER_UUID: UUID = UUID.fromString("6f7a8b9c-0d1e-2f3a-4b5c-6d7e8f90a1b2")

    private const val MAX_BYTES = 48L * 1024 * 1024 // 48 MB safety cap

    private val serverLock = Any()
    private var activeServer: BluetoothServerSocket? = null

    /** Unblocks [acceptOneFileToDir] if it is blocked on [BluetoothServerSocket.accept]. */
    fun cancelListen() {
        synchronized(serverLock) {
            runCatching { activeServer?.close() }
            activeServer = null
        }
    }

    fun defaultAdapter(context: Context): BluetoothAdapter? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.getSystemService(BluetoothManager::class.java)?.adapter
        } else {
            @Suppress("DEPRECATION")
            BluetoothAdapter.getDefaultAdapter()
        }
    }

    suspend fun sendFileBytes(
        device: BluetoothDevice,
        fileName: String,
        bytes: ByteArray,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val socket: BluetoothSocket =
                device.createInsecureRfcommSocketToServiceRecord(TRANSFER_UUID)
            socket.connect()
            DataOutputStream(socket.outputStream).use { out ->
                val nameBytes = fileName.toByteArray(Charsets.UTF_8)
                out.writeInt(nameBytes.size)
                out.write(nameBytes)
                out.writeLong(bytes.size.toLong())
                out.write(bytes)
                out.flush()
            }
            socket.close()
        }
    }

    suspend fun acceptOneFileToDir(
        adapter: BluetoothAdapter,
        outputDir: File,
        onProgress: (String) -> Unit,
    ): Result<File> = withContext(Dispatchers.IO) {
        var server: BluetoothServerSocket? = null
        try {
            if (!outputDir.exists()) outputDir.mkdirs()
            server = adapter.listenUsingInsecureRfcommWithServiceRecord("ShareReceive", TRANSFER_UUID)
            synchronized(serverLock) { activeServer = server }
            onProgress("Waiting for sender…")
            val socket = server.accept()
            onProgress("Receiving…")
            DataInputStream(socket.inputStream).use { din ->
                val nameLen = din.readInt()
                if (nameLen <= 0 || nameLen > 4096) error("Invalid name length")
                val nameBytes = ByteArray(nameLen)
                din.readFully(nameBytes)
                val rawName = nameBytes.toString(Charsets.UTF_8).trim().ifBlank { "received.bin" }
                val safeName = rawName.replace(Regex("[^a-zA-Z0-9._-]"), "_").take(120)
                val size = din.readLong()
                if (size <= 0L || size > MAX_BYTES) error("Invalid payload size")
                val outFile = File(outputDir, "${System.currentTimeMillis()}_$safeName")
                FileOutputStream(outFile).use { fos ->
                    val buf = ByteArray(8192)
                    var remaining = size
                    while (remaining > 0) {
                        val toRead = minOf(buf.size.toLong(), remaining).toInt()
                        val read = din.read(buf, 0, toRead)
                        if (read < 0) error("Unexpected EOF")
                        fos.write(buf, 0, read)
                        remaining -= read
                    }
                }
                socket.close()
                Result.success(outFile)
            }
        } catch (e: Throwable) {
            Result.failure(e)
        } finally {
            runCatching { server?.close() }
            synchronized(serverLock) {
                if (activeServer === server) activeServer = null
            }
        }
    }
}
