package com.ilustris.sagai.core.utils

import android.content.Context
import android.util.Log // Added for logging
import kotlinx.coroutines.Dispatchers // Added for IO Context
import kotlinx.coroutines.withContext // Added for IO Context
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

    fun getFileCacheDir(): File {
        val directory = File(context.cacheDir, cacheDirName)
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
            Log.e(javaClass.simpleName, "Failed to save file to cache: ${file.absolutePath}", e)
            if (file.exists()) file.delete() // Clean up on exception
            null
        }
    }

    suspend fun getFile(
        url: String,
        desiredExtension: String = "mp3",
    ): File? {
        getCachedFile(url, desiredExtension)?.let {
            Log.d(javaClass.simpleName, "File found in cache: ${it.absolutePath}")
            return it
        }

        Log.d(javaClass.simpleName, "File not in cache. Downloading from: $url")
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
                            Log.e(
                                javaClass.simpleName,
                                "Download failed: Server responded with code ${connection.responseCode} for URL $url",
                            )
                            null
                        }
                    } catch (e: Exception) {
                        Log.e(javaClass.simpleName, "Download failed for URL $url", e)
                        null
                    } finally {
                        inputStream?.close()
                        connection?.disconnect()
                    }
                }

            if (downloadedData != null && downloadedData.isNotEmpty()) {
                Log.d(javaClass.simpleName, "Download successful, ${downloadedData.size} bytes received.")
                saveFileToCache(url, downloadedData, desiredExtension)
            } else {
                Log.e(javaClass.simpleName, "Downloaded data is null or empty for URL $url.")
                null
            }
        } catch (e: Exception) {
            // Catch any unexpected errors during the process
            Log.e(javaClass.simpleName, "Exception in getFile for URL $url", e)
            null
        }
    }

    fun clearCache() {
        val cacheDir = getFileCacheDir()
        if (cacheDir.exists()) {
            val deleted = cacheDir.deleteRecursively()
            Log.d(javaClass.simpleName, "Cache cleared: $deleted")
        }
    }
}
