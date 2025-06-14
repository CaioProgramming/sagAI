package com.ilustris.sagai.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.File

class FileHelper(
    private val context: Context,
) {
    fun getCacheFilePath(fileName: String): String = context.cacheDir.resolve(fileName).absolutePath

    fun saveToCache(
        fileName: String,
        data: ByteArray,
    ): File? {
        val file = context.cacheDir.resolve(fileName.plus(".png").removeBlankSpace())
        file.writeBytes(data)
        return file.takeIf { it.exists() }
    }

    fun saveBase64ToCache(
        fileName: String,
        binary: String,
    ): File? {
        val decodedBytes = Base64.decode(binary, Base64.DEFAULT)
        val file = context.cacheDir.resolve(fileName.removeBlankSpace().plus(".png"))
        file.writeBytes(decodedBytes)
        return file.takeIf { it.exists() }
    }

    fun readFromCache(fileName: String): ByteArray? {
        val file = context.cacheDir.resolve(fileName.removeBlankSpace())
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
}
