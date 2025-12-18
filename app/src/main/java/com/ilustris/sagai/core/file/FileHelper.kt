package com.ilustris.sagai.core.file

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.util.Calendar
import android.util.Base64
import android.util.Log
import com.ilustris.sagai.core.utils.removeBlankSpace
import java.io.File
import java.io.FileOutputStream

class FileHelper(
    private val context: Context,
) {
    fun saveFile(
        byteArray: ByteArray,
        path: String,
        fileName: String,
    ): File? {
        val directory = context.filesDir.resolve(path)

        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = directory.resolve(fileName.plus(".png").removeBlankSpace())
        file.writeBytes(byteArray)
        return file.takeIf { it.exists() }
    }

    fun saveFile(
        fileName: String,
        data: Bitmap?,
        path: String? = null,
    ): File? {
        if (data == null) return null
        val directory =
            if (path != null) {
                context.filesDir.resolve("sagas/$path")
            } else {
                context.filesDir
            }
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val currentDateTime = Calendar.getInstance().timeInMillis
        val file = directory.resolve(fileName.plus(currentDateTime).plus(".png").removeBlankSpace())
        val fileOutputStream = FileOutputStream(file)
        data.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        fileOutputStream.flush()
        fileOutputStream.close()
        return file.takeIf {
            Log.d(javaClass.simpleName, "saveFile: File saved at ${file.absolutePath}")
            it.exists()
        }
    }

    fun readFile(path: String?): Bitmap? {
        if (path == null) return null
        val file = File(path)
        return if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
        } else {
            null
        }
    }

    fun readFile(
        fileName: String,
        path: String? = null,
    ): ByteArray? =
        try {
            val directory = path?.let { context.filesDir.resolve(it) } ?: context.filesDir
            val file = directory.resolve(fileName.removeBlankSpace())
            if (file.exists()) {
                file.readBytes()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    fun decodeByteArrayToBitmap(data: ByteArray): Bitmap? =
        try {
            BitmapFactory.decodeByteArray(data, 0, data.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    fun deletePath(id: Int) {
        val directory = context.filesDir.resolve("sagas/$id")
        if (directory.exists()) {
            directory.deleteRecursively()
        }
    }

    fun getDirectorySize(directory: File?): Long {
        if (directory == null || !directory.exists()) return 0L
        return directory
            .walkTopDown()
            .filter { it.isFile }
            .map { it.length() }
            .sum()
    }

    fun getDirectorySize(path: String): Long = getDirectorySize(File(path))

    fun saveAudioFile(
        audioByteArray: ByteArray,
        sagaId: Int,
        fileName: String,
    ): File? {
        val directory = context.filesDir.resolve("sagas/$sagaId/audio")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = directory.resolve(fileName.plus(".mp3").removeBlankSpace())
        return try {
            file.writeBytes(audioByteArray)
            file.takeIf { it.exists() }.also {
                Log.d(
                    javaClass.simpleName,
                    "saveAudioFile: Audio file saved at ${file.absolutePath}",
                )
            }
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "saveAudioFile: Error saving audio file", e)
            null
        }
    }

    /**
     * Decodes a Base64 string and saves it to a file.
     * Follows the same pattern as image saving with configurable extension and path.
     *
     * @param base64Data The Base64 encoded data to decode and save
     * @param sagaId The saga ID for directory organization
     * @param messageId The message ID for file naming
     * @param extension The file extension (e.g., "mp3", "m4a", "wav")
     * @param subDirectory The subdirectory within the saga folder (e.g., "audio", "media")
     * @return The saved File or null if failed
     */
    fun decodeAndSaveBase64(
        base64Data: ByteArray,
        path: String,
        fileName: String,
        extension: String,
    ): File? {
        val directory = context.filesDir.resolve(path)
        if (!directory.exists()) {
            directory.mkdirs()
        }

        val timestamp = Calendar.getInstance().timeInMillis
        val fileName = fileName.plus(timestamp).removeBlankSpace().plus(".$extension")
        val file = directory.resolve(fileName)

        return try {
            val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
            file.writeBytes(decodedBytes)
            file.takeIf { it.exists() }.also {
                Log.d(
                    javaClass.simpleName,
                    "decodeAndSaveBase64: File saved at ${file.absolutePath}",
                )
            }
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "decodeAndSaveBase64: Error decoding/saving file", e)
            null
        }
    }

    fun readAudioFile(audioPath: String): ByteArray? =
        try {
            val file = File(audioPath)
            if (file.exists() && file.isFile) {
                file.readBytes()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "readAudioFile: Error reading audio file", e)
            null
        }
}
