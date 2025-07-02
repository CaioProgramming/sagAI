package com.ilustris.sagai.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.File

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
        data: String,
        path: String? = null,
    ): File? {
        val decodedBytes = Base64.decode(data, Base64.DEFAULT)
        return saveFile(fileName, decodedBytes, "sagas/$path")
    }

    fun readFile(
        fileName: String,
        path: String? = null,
    ): ByteArray? {
        val directory = path?.let { context.filesDir.resolve(it) } ?: context.filesDir
        val file = directory.resolve(fileName.removeBlankSpace())
        return if (file.exists()) {
            file.readBytes()
        } else {
            null
        }
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
}
