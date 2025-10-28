package com.ilustris.sagai.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.util.Calendar
import android.util.Base64
import android.util.Log
import java.io.File
import java.io.FileOutputStream

class FileHelper(
    private val context: Context,
) {
    fun saveFile(
        fileName: String,
        data: ByteArray,
        path: String? = null,
    ): File? {
        val directory =
            if (path != null) {
                context.filesDir.resolve("sagas/$path")
            } else {
                context.filesDir
            }
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = directory.resolve(fileName.plus(".png").removeBlankSpace())
        file.writeBytes(data)
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

    fun saveFile(
        fileName: String,
        data: String,
        path: String? = null,
    ): File? {
        val decodedBytes = Base64.decode(data, Base64.DEFAULT)
        return saveFile(fileName, decodedBytes, "sagas/$path")
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
}
