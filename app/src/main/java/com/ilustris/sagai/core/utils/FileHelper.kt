package com.ilustris.sagai.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

class FileHelper(
    private val context: Context,
) {
    fun getCacheFilePath(fileName: String): String = context.cacheDir.resolve(fileName).absolutePath

    fun saveToCache(
        fileName: String,
        data: ByteArray,
    ): File? {
        val file = context.cacheDir.resolve(fileName.plus(".png").trim())
        file.writeBytes(data)
        return file.takeIf { it.exists() }
    }

    fun readFromCache(fileName: String): ByteArray? {
        val file = context.cacheDir.resolve(fileName)
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
