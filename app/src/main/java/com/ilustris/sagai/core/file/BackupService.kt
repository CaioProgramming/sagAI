package com.ilustris.sagai.core.file

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.google.gson.Gson
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.datastore.DataStorePreferences
import com.ilustris.sagai.core.file.backup.RestorableSaga
import com.ilustris.sagai.core.file.backup.SagaManifest
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatChapters
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class BackupService(
    private val context: Context,
    private val preferences: DataStorePreferences,
    private val fileHelper: FileHelper,
) {
    companion object {
        private const val MANIFEST_FILE_NAME = "sagai_manifest.json"
        private const val SAGA_JSON_FILE = "saga.json"
        private const val IMAGES_FOLDER = "images"
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun backupEnabled() =
        preferences.getString(BACKUP_PREFRENCE_KEY).flatMapLatest {
            flowOf(it.isNotEmpty())
        }



    private suspend fun getBackupRoot(): DocumentFile? =
        try {
            val path =
                preferences.getStringNow(BACKUP_PREFRENCE_KEY).ifEmpty {
                    error("Path not defined.")
                }
            Log.i(javaClass.simpleName, "backup Path: $path")
            val pathUri = path.toUri()

            val parentFolder = DocumentFile.fromTreeUri(context, pathUri)

            parentFolder ?: error("Could not access backup directory")

            if (parentFolder.exists().not()) error("Backup directory does not exist.")
            if (parentFolder.canWrite().not()) error("Backup directory is not writable.")
            parentFolder
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }







    suspend fun exportSagaToCache(saga: SagaContent): RequestResult<Uri> =
        executeRequest {
            val cacheDir = File(context.cacheDir, "exports")
            if (!cacheDir.exists()) cacheDir.mkdirs()

            // Clean up old exports to avoid clutter
            cacheDir.listFiles()?.forEach { it.delete() }

            val zipFileName = "${saga.data.title.replace(" ", "_")}.saga"
            val zipFile = File(cacheDir, zipFileName)

            zipFile.outputStream().use { outputStream ->
                writeSagaToZip(saga, outputStream)
            }

            // Use FileProvider to get a shareable URI
            androidx.core.content.FileProvider.getUriForFile(
                context,
                "com.ilustris.sagai.fileprovider",
                zipFile,
            )
        }

    suspend fun createFullBackup(backupName: String, sagas: List<SagaContent>): RequestResult<Uri> =
        executeRequest {
            val backupRoot = getBackupRoot() ?: error("Could not access backup directory")
            val zipFileName = "$backupName.sgs"
            var sagaZipFile = backupRoot.findFile(zipFileName)

            sagaZipFile?.delete()

            sagaZipFile =
                backupRoot.createFile("application/octet-stream", zipFileName)
                    ?: error("Could not create zip file in backup directory.")

            Log.d(javaClass.simpleName, "createFullBackup: Attempting to write to URI: ${sagaZipFile.uri}")
            context.contentResolver.openOutputStream(sagaZipFile.uri, "w")?.use { outputStream ->
                try {
                    ZipOutputStream(outputStream).use { zipStream ->
                        if (sagas.isEmpty()) {
                            Log.w(javaClass.simpleName, "createFullBackup: No sagas to backup. Creating empty zip.")
                        }

                        val manifest = sagas.map {
                            SagaManifest(
                                sagaId = it.data.id,
                                title = it.data.title,
                                description = it.data.description,
                                genre = it.data.genre,
                                iconName = File(it.data.icon).name,
                                lastBackup = System.currentTimeMillis(),
                                zipFileName = "saga_${it.data.id}"
                            )
                        }
                        val manifestJson = Gson().toJson(manifest)
                        zipStream.putNextEntry(ZipEntry(MANIFEST_FILE_NAME))
                        zipStream.write(manifestJson.toByteArray())
                        zipStream.closeEntry()

                        sagas.forEach { saga ->
                            val sagaFolder = "saga_${saga.data.id}/"
                            zipStream.putNextEntry(ZipEntry(sagaFolder))
                            zipStream.closeEntry() // Close the directory entry after creating it

                            val backedSaga = normalizeSagaContentPaths(saga)
                            val sagaJson = Gson().toJson(backedSaga)
                            zipStream.putNextEntry(ZipEntry("${sagaFolder}${SAGA_JSON_FILE}"))
                            zipStream.write(sagaJson.toByteArray())
                            zipStream.closeEntry()

                            val imagePaths = getAllImageFiles(saga)
                            imagePaths.forEach { (path, file) ->
                                if (file.exists()) {
                                    zipStream.putNextEntry(ZipEntry("$sagaFolder$path"))
                                    FileInputStream(file).use { it.copyTo(zipStream) }
                                    zipStream.closeEntry()
                                }
                            }
                        }
                        zipStream.flush() // Explicitly flush before closing
                    }
                    outputStream.flush() // Explicitly flush the underlying output stream
                } catch (e: Exception) {
                    Log.e(javaClass.simpleName, "createFullBackup: Error writing zip content", e)
                    throw e // Re-throw to propagate the error
                }
            } ?: error("Could not open output stream for destination URI")
            sagaZipFile.uri
        }

    suspend fun restoreFullBackup(uri: Uri): RequestResult<List<SagaContent>> =
        executeRequest {
            val sagas = mutableListOf<SagaContent>()
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zipStream ->
                    var entry = zipStream.nextEntry
                    while (entry != null) {
                        if (entry.name.endsWith(SAGA_JSON_FILE)) {
                            val jsonString = zipStream.bufferedReader().use { it.readText() }
                            val saga = Gson().fromJson(jsonString, SagaContent::class.java)
                            sagas.add(saga)
                        }
                        entry = zipStream.nextEntry
                    }
                }
            }
            sagas
        }

    private fun writeSagaToZip(
        saga: SagaContent,
        outputStream: java.io.OutputStream,
    ) {
        ZipOutputStream(outputStream).use { zipStream ->
            val backedSaga = normalizeSagaContentPaths(saga)
            val sagaJson = Gson().toJson(backedSaga)
            zipStream.putNextEntry(ZipEntry(SAGA_JSON_FILE))
            zipStream.write(sagaJson.toByteArray())
            zipStream.closeEntry()

            val imagePaths = getAllImageFiles(saga)
            imagePaths.forEach { (path, file) ->
                if (file.exists()) {
                    zipStream.putNextEntry(ZipEntry(path))
                    FileInputStream(file).use { it.copyTo(zipStream) }
                    zipStream.closeEntry()
                }
            }
        }
    }

    suspend fun writeExportToUri(
        saga: SagaContent,
        destinationUri: Uri,
    ): RequestResult<Unit> =
        executeRequest {
            val documentFile = DocumentFile.fromSingleUri(context, destinationUri)
                ?: error("Could not get document file from URI")

            val displayName = documentFile.name ?: "${saga.data.title.replace(" ", "_")}.saga"
            val newDisplayName = if (displayName.endsWith(".saga")) displayName else "$displayName.saga"

            val parentFolder = documentFile.parentFile
                ?: error("Could not get parent folder from document file")

            var newDocumentFile = parentFolder.findFile(newDisplayName)
            if (newDocumentFile == null) {
                newDocumentFile = parentFolder.createFile("application/octet-stream", newDisplayName)
            } else {
                newDocumentFile.delete()
                newDocumentFile = parentFolder.createFile("application/octet-stream", newDisplayName)
            }


            context.contentResolver.openOutputStream(newDocumentFile?.uri ?: destinationUri, "rwt")?.use { outputStream ->
                writeSagaToZip(saga, outputStream)
            } ?: error("Could not open output stream for destination URI")
        }

    private fun normalizeSagaContentPaths(saga: SagaContent): SagaContent =
        saga.copy(
            data =
                saga.data.copy(
                    icon = getFileRelativePath(saga.data.icon, saga.data.id),
                ),
            characters =
                saga.characters.map {
                    it.copy(data = it.data.copy(image = getFileRelativePath(it.data.image, saga.data.id)))
                },
            acts =
                saga.acts.map {
                    it.copy(
                        chapters =
                            it.chapters.map {
                                it.copy(
                                    data = it.data.copy(coverImage = getFileRelativePath(it.data.coverImage, saga.data.id)),
                                )
                            },
                    )
                },
        )

    private fun getFileRelativePath(
        path: String,
        sagaId: Int,
    ): String {
        val sagaBasePath = File(context.filesDir, "sagas/$sagaId").absolutePath + File.separator
        val relativePath = { absolutePath: String ->
            absolutePath.removePrefix(sagaBasePath)
        }

        return relativePath(path)
    }

    private fun getAllImageFiles(saga: SagaContent): List<Pair<String, File>> =
        buildList {
            val sagaId = saga.data.id
            if (saga.data.icon.isNotBlank()) {
                add(getFileRelativePath(saga.data.icon, sagaId) to File(saga.data.icon))
            }
            addAll(
                saga.characters.mapNotNull {
                    if (it.data.image.isNotBlank()) getFileRelativePath(it.data.image, sagaId) to File(it.data.image) else null
                },
            )
            addAll(
                saga.flatChapters().mapNotNull {
                    if (it.data.coverImage.isNotBlank()) {
                        getFileRelativePath(
                            it.data.coverImage,
                            sagaId,
                        ) to File(it.data.coverImage)
                    } else {
                        null
                    }
                },
            )
        }

    suspend fun enableBackup(uri: Uri?) =
        executeRequest {
            releaseOldPermission()
            if (uri == null) {
                preferences.setString(BACKUP_PREFRENCE_KEY, emptyString())
                error("Backup URI cannot be null.")
            }
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)

            preferences.setString(BACKUP_PREFRENCE_KEY, uri.toString())
        }

    private suspend fun releaseOldPermission() =
        executeRequest {
            val oldUriString = preferences.getStringNow(BACKUP_PREFRENCE_KEY)

            if (oldUriString.isNotEmpty()) {
                val oldUri = oldUriString.toUri()
                val releaseFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.releasePersistableUriPermission(oldUri, releaseFlags)
                Log.i(javaClass.simpleName, "Successfully released permission for old URI: $oldUriString")
            }
        }

    fun saveExtractedImages(
        newSagaId: Int,
        imageByteMap: List<Pair<String, ByteArray>>,
    ): MutableList<Pair<String, String>> {
        val pathTranslationMap = mutableListOf<Pair<String, String>>()
        val sagaRoot = File(context.filesDir, "sagas/$newSagaId")
        if (!sagaRoot.exists()) sagaRoot.mkdirs()

        imageByteMap.forEach { (path, bytes) ->

            val destinationFile = File(sagaRoot, path)

            // 2. Ensure the parent directory (e.g., ".../sagas/4/characters/") exists
            val destinationDir = destinationFile.parentFile
            if (destinationDir != null && !destinationDir.exists()) {
                destinationDir.mkdirs()
            }

            // 3. Save the file's byte array to the correct destination
            destinationDir?.path ?: return@forEach
            val newFile = fileHelper.saveFile(bytes, destinationDir.path, destinationFile.name) ?: return@forEach

            pathTranslationMap.add(path to newFile.absolutePath)
        }
        return pathTranslationMap
    }

    fun unzipImageBytes(zipUri: Uri): List<Pair<String, ByteArray>> {
        val imageByteMap = mutableListOf<Pair<String, ByteArray>>()
        try {
            context.contentResolver.openInputStream(zipUri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zipStream ->
                    var entry = zipStream.nextEntry
                    while (entry != null) {
                        if (!entry.isDirectory && entry.name != SAGA_JSON_FILE) {
                            val filePair = entry.name to zipStream.readBytes()
                            imageByteMap.add(filePair)
                        }
                        entry = zipStream.nextEntry
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return imageByteMap
    }

    fun unzipAndParseSaga(zipUri: Uri): SagaContent? {
        try {
            context.contentResolver.openInputStream(zipUri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zipStream ->
                    var entry = zipStream.nextEntry
                    while (entry != null) {
                        if (entry.name == SAGA_JSON_FILE) {
                            // We found the saga.json file!
                            // Read its content as text.
                            val jsonString = zipStream.bufferedReader().use { it.readText() }
                            // Parse the JSON into our SagaContent object.
                            return Gson().fromJson(jsonString, SagaContent::class.java)
                        }
                        entry = zipStream.nextEntry
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun updateSagaPaths(
        saga: SagaContent,
        pathMap: Map<String, String>,
    ): SagaContent {
        val updatePath = { path: String ->
            pathMap[path] ?: path
        }

        return saga.copy(
            data = saga.data.copy(icon = updatePath(saga.data.icon)),
            characters =
                saga.characters.map {
                    it.copy(data = it.data.copy(image = updatePath(it.data.image)))
                },
            acts =
                saga.acts.map { act ->
                    act.copy(
                        chapters =
                            act.chapters.map { chapter ->
                                chapter.copy(
                                    data = chapter.data.copy(coverImage = updatePath(chapter.data.coverImage)),
                                )
                            },
                    )
                },
        )
    }
}

const val BACKUP_PREFRENCE_KEY = "BACKUP_PATH"
const val BACKUP_PERMISSION = "BACKUP_SERVICE"
