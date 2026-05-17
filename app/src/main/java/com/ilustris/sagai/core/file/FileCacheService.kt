package com.ilustris.sagai.core.file

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

class FileCacheService(
    private val context: Context,
    private val downloadClient: OkHttpClient,
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
        url: String,
        extension: String,
    ): File? {
        val file = cacheFileFor(url, extension)
        return if (file.exists() && file.length() > 0) file else null
    }

    fun invalidateCachedFile(
        url: String,
        extension: String,
    ) {
        val file = cacheFileFor(url, extension)
        if (file.exists()) {
            file.delete()
            Timber.d("Invalidated cache file: ${file.name}")
        }
    }

    private fun cacheFileFor(
        url: String,
        extension: String,
    ): File = File(getFileCacheDir(), generateFileName(url, extension))

    fun saveFileToCache(
        url: String,
        data: ByteArray,
        extension: String,
    ): File? {
        val file = cacheFileFor(url, extension)
        return try {
            FileOutputStream(file).use { outputStream ->
                outputStream.write(data)
            }
            if (file.exists() && file.length() > 0) {
                file
            } else {
                if (file.exists()) file.delete()
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to save file to cache: ${file.absolutePath}")
            if (file.exists()) file.delete()
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
            withContext(Dispatchers.IO) {
                downloadToCache(url, desiredExtension)
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception in getFile for URL $url")
            null
        }
    }

    private fun downloadToCache(
        url: String,
        extension: String,
    ): File? {
        val dest = cacheFileFor(url, extension)
        val temp = File(dest.parentFile, "${dest.name}.part")

        repeat(MAX_DOWNLOAD_ATTEMPTS) { attempt ->
            temp.delete()
            try {
                val request =
                    Request
                        .Builder()
                        .url(url)
                        .get()
                        .header("User-Agent", DOWNLOAD_USER_AGENT)
                        .header("Accept", "*/*")
                        .build()

                downloadClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Timber.e(
                            "Download failed: HTTP ${response.code} for $url (attempt ${attempt + 1})",
                        )
                        return@repeat
                    }

                    val body = response.body
                    if (body == null) {
                        Timber.e("Download failed: empty body for $url (attempt ${attempt + 1})")
                        return@repeat
                    }

                    body.byteStream().use { input ->
                        FileOutputStream(temp).use { output ->
                            input.copyTo(output)
                        }
                    }

                    val bytes = temp.length()
                    if (bytes <= 0L) {
                        Timber.e("Download failed: 0 bytes written for $url (attempt ${attempt + 1})")
                        temp.delete()
                        return@repeat
                    }

                    if (dest.exists()) dest.delete()
                    if (!temp.renameTo(dest)) {
                        temp.copyTo(dest, overwrite = true)
                        temp.delete()
                    }

                    Timber.d("Download successful: $bytes bytes → ${dest.absolutePath}")
                    return dest
                }
            } catch (e: Exception) {
                temp.delete()
                Timber.e(e, "Download failed for $url (attempt ${attempt + 1})")
            }
        }

        if (dest.exists() && dest.length() == 0L) dest.delete()
        return null
    }

    fun clearCache() {
        val cacheDir = getFileCacheDir()
        if (cacheDir.exists()) {
            val deleted = cacheDir.deleteRecursively()
            Timber.d("Cache cleared: $deleted")
        }
    }

    companion object {
        private const val MAX_DOWNLOAD_ATTEMPTS = 2
        private const val DOWNLOAD_USER_AGENT = "Sagas/1.0 (Android; +https://ilustris.com)"
    }
}
