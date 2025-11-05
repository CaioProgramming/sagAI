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
import com.ilustris.sagai.BuildConfig
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
import kotlin.collections.emptyList

class BackupService(
    private val context: Context,
    private val preferences: DataStorePreferences,
    private val fileHelper: FileHelper,
) {
    companion object {
        private const val BACKUP_FOLDER_NAME = "sagai_backups"
        private const val MANIFEST_FILE_NAME = "sagai_manifest.json"
        private const val SAGA_JSON_FILE = "saga.json"
        private const val IMAGES_FOLDER = "images"
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun backupEnabled() =
        preferences.getString(BACKUP_PREFRENCE_KEY).flatMapLatest {
            flowOf(getBackupRoot() != null)
        }

    suspend fun deleteBackup() {
        preferences.setString(BACKUP_PREFRENCE_KEY, emptyString())
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

            val sagaiBackupFolder =
                parentFolder.findFile(BACKUP_FOLDER_NAME) ?: return parentFolder.createDirectory(
                    BACKUP_FOLDER_NAME,
                )

            if (sagaiBackupFolder.isDirectory.not()) error("Backup folder is not a directory.")

            sagaiBackupFolder
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    private suspend fun getBackupManifests(backupRoot: DocumentFile): List<SagaManifest> {
        val manifestFile = backupRoot.findFile(MANIFEST_FILE_NAME) ?: return emptyList()
        val json = readTextFromUri(manifestFile.uri) ?: return emptyList()
        return Gson()
            .fromJson(json, Array<SagaManifest>::class.java)
            ?.toList() ?: emptyList()
    }

    private fun getIconFromZip(
        backupRoot: DocumentFile,
        zipFileName: String,
        iconFileName: String,
    ): Bitmap? {
        if (iconFileName.isBlank()) return null
        val zipFile = backupRoot.findFile(zipFileName) ?: return null

        try {
            context.contentResolver.openInputStream(zipFile.uri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zipStream ->
                    var entry = zipStream.nextEntry
                    while (entry != null) {
                        if (entry.name == "$IMAGES_FOLDER/$iconFileName") {
                            return BitmapFactory.decodeStream(zipStream)
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

    suspend fun getBackedUpSagas() =
        executeRequest {
            val backupRoot = getBackupRoot() ?: return@executeRequest emptyList()
            val manifests = getBackupManifests(backupRoot)

            manifests.map {
                val icon = getIconFromZip(backupRoot, it.zipFileName, it.iconName)
                RestorableSaga(
                    it,
                    icon,
                )
            }
        }

    private fun readTextFromUri(uri: Uri): String? =
        try {
            context.contentResolver
                .openInputStream(uri)
                ?.bufferedReader()
                ?.use { it.readText() }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    private suspend fun updateManifest(
        backupRoot: DocumentFile,
        saga: SagaContent,
        zipFileName: String,
    ) {
        val gson = Gson()
        var manifestFile = backupRoot.findFile(MANIFEST_FILE_NAME)

        val currentManifest: MutableList<SagaManifest> =
            if (manifestFile != null) {
                val json = readTextFromUri(manifestFile.uri)
                gson.fromJson<Array<SagaManifest>>(json, Array<SagaManifest>::class.java)?.toMutableList() ?: mutableListOf()
            } else {
                manifestFile = backupRoot.createFile("application/json", MANIFEST_FILE_NAME)
                    ?: error("Could not create manifest file.")
                mutableListOf()
            }

        currentManifest.removeAll { it.sagaId == saga.data.id }

        val newEntry =
            SagaManifest(
                sagaId = saga.data.id,
                title = saga.data.title,
                description = saga.data.description,
                genre = saga.data.genre,
                iconName = File(saga.data.icon).name,
                lastBackup = System.currentTimeMillis(),
                zipFileName = zipFileName,
            )
        currentManifest.add(newEntry)

        val updatedJson = gson.toJson(currentManifest)
        context.contentResolver.openOutputStream(manifestFile.uri, "w")?.use { outputStream ->
            outputStream.write(updatedJson.toByteArray())
        }
    }

    suspend fun backupSaga(saga: SagaContent) =
        executeRequest {
            val backupRoot = getBackupRoot() ?: error("Could not access backup directory")
            val zipFileName = "saga_${saga.data.id}.zip"
            var sagaZipFile = backupRoot.findFile(zipFileName)
            if (sagaZipFile == null) {
                sagaZipFile = backupRoot.createFile("application/zip", zipFileName)
                    ?: error("Could not create zip file in backup directory.")
            }

            context.contentResolver.openOutputStream(sagaZipFile.uri, "w")?.use { outputStream ->
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

            updateManifest(backupRoot, saga, zipFileName)
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
                deleteBackup()
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
            val dirPath = destinationDir?.path ?: return@forEach
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
}

const val BACKUP_PREFRENCE_KEY = "BACKUP_PATH"
const val BACKUP_PERMISSION = "BACKUP_SERVICE"
