package com.ilustris.sagai.core.file

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers // Added for IO Context
import kotlinx.coroutines.withContext // Added for IO Context
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream // Added for InputStream
import java.net.HttpURLConnection // Added for HttpURLConnection
import java.net.URL // Added for URL
import java.security.MessageDigest

class FileCacheService(
    private val context: Context,
) {
    private val cacheDirName = "file_cache"

    fun getFileCacheDir(customPath: String? = null): File {
        val directory = File(context.cacheDir, if (customPath == null) cacheDirName else "$cacheDirName/$customPath")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        return directory
    }

    private fun generateFileName(
        url: String,
        extension: String,
    ): String {
        val digest = MessageDigest.getInstance("MD5")
        digest.update(url.toByteArray())
        val hash = digest.digest().joinToString("") { "%02x".format(it) }
        return "$hash.$extension"
    }

    fun getCachedFile(
        musicUrl: String,
        extension: String,
    ): File? {
        val fileName = generateFileName(musicUrl, extension)
        val file = File(getFileCacheDir(), fileName)
        return if (file.exists() && file.length() > 0) {
            file
        } else {
            null
        }
    }

    fun saveFileToCache(
        musicUrl: String,
        data: ByteArray,
        extension: String,
    ): File? {
        val fileName = generateFileName(musicUrl, extension)
        val file = File(getFileCacheDir(), fileName)
        return try {
            FileOutputStream(file).use { outputStream ->
                outputStream.write(data)
            }
            if (file.exists() && file.length() > 0) {
                file
            } else {
                if (file.exists()) file.delete() // Clean up if file is empty after write attempt
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to save file to cache: ${file.absolutePath}")
            if (file.exists()) file.delete() // Clean up on exception
            null
        }
    }

    fun saveFile(
        path: String,
        fileName: String,
        data: Bitmap,
    ): File? {
        val directory = getFileCacheDir(path)
        val currentDateTime = System.currentTimeMillis()
        val file = directory.resolve(fileName.plus(currentDateTime).plus(".png").replace(" ", ""))
        return try {
            FileOutputStream(file).use { fileOutputStream ->
                data.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            }
            if (file.exists() && file.length() > 0) {
                file
            } else {
                if (file.exists()) file.delete()
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to save bitmap to cache: ${file.absolutePath}")
            if (file.exists()) file.delete()
            null
        }
    }

    suspend fun getFile(
        url: String,
        desiredExtension: String = "mp3",
    ): File? {
        getCachedFile(url, desiredExtension)?.let {
            Timber.d("File found in cache: ${it.absolutePath}")
            return it
        }

        Timber.d("File not in cache. Downloading from: $url")
        return try {
            // Ensure network operations are on a background thread
            val downloadedData: ByteArray? =
                withContext(Dispatchers.IO) {
                    var connection: HttpURLConnection? = null
                    var inputStream: InputStream? = null
                    try {
                        val connectionUrl = URL(url)
                        connection = connectionUrl.openConnection() as HttpURLConnection
                        connection.connectTimeout = 15000 // 15 seconds
                        connection.readTimeout = 15000 // 15 seconds
                        connection.requestMethod = "GET"
                        connection.connect()

                        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                            inputStream = connection.inputStream
                            inputStream.readBytes()
                        } else {
                            Timber.e(
                                "Download failed: Server responded with code ${connection.responseCode} for URL $url",
                            )
                            null
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Download failed for URL $url")
                        null
                    } finally {
                        inputStream?.close()
                        connection?.disconnect()
                    }
                }

            if (downloadedData != null && downloadedData.isNotEmpty()) {
                Timber.d("Download successful, ${downloadedData.size} bytes received.")
                saveFileToCache(url, downloadedData, desiredExtension)
            } else {
                Timber.e("Downloaded data is null or empty for URL $url.")
                null
            }
        } catch (e: Exception) {
            // Catch any unexpected errors during the process
            Timber.e(e, "Exception in getFile for URL $url")
            null
        }
    }

    fun clearCache() {
        val cacheDir = getFileCacheDir()
        if (cacheDir.exists()) {
            val deleted = cacheDir.deleteRecursively()
            Timber.d("Cache cleared: $deleted")
        }
    }
}
