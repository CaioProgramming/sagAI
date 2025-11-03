package com.ilustris.sagai.core.file

import android.Manifest
import android.content.Context
import android.os.Environment
import com.google.gson.Gson
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.permissions.PermissionService
import com.ilustris.sagai.core.permissions.PermissionStatus
import com.ilustris.sagai.features.home.data.model.SagaContent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.io.File
import java.io.FileWriter

class BackupService(
    private val context: Context,
    private val permissionService: PermissionService,
) {
    companion object {
        private const val BACKUP_FOLDER = "sagai_backups"
        private const val SAGA_JSON_FILE = "saga.json"
    }

    private fun getBackupRoot(): File? {
        val externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        return externalFilesDir?.let {
            val backupDir = File(it, BACKUP_FOLDER)
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }
            backupDir
        }
    }

    private fun getSagaBackupDirs(): List<File> {
        val root = getBackupRoot()
        return root?.listFiles { file -> file.isDirectory }?.toList() ?: emptyList()
    }

    suspend fun getBackedUpSagas() =
        executeRequest {
            val gson = Gson()
            getSagaBackupDirs().mapNotNull { sagaDir ->
                try {
                    val sagaJsonFile = File(sagaDir, SAGA_JSON_FILE)
                    if (!sagaJsonFile.exists()) return@mapNotNull null

                    val json = sagaJsonFile.readText()
                    val sagaContent = gson.fromJson(json, SagaContent::class.java)

                    val iconName = File(sagaContent.data.icon).name
                    val newIconPath =
                        if (iconName.isNotBlank()) sagaDir.absolutePath + File.separator + iconName else ""

                    val updatedSaga = sagaContent.data.copy(icon = newIconPath)

                    val updatedCharacters =
                        sagaContent.characters.map { characterContent ->
                            val characterImageName = File(characterContent.data.image).name
                            val newCharacterImage =
                                if (characterImageName.isNotBlank()) sagaDir.absolutePath + File.separator + characterImageName else ""
                            characterContent.copy(data = characterContent.data.copy(image = newCharacterImage))
                        }

                    val updatedActs =
                        sagaContent.acts.map { actContent ->
                            val updatedChapters =
                                actContent.chapters.map { chapterContent ->
                                    val chapterImageName = File(chapterContent.data.coverImage).name
                                    val newChapterImage =
                                        if (chapterImageName.isNotBlank()) sagaDir.absolutePath + File.separator + chapterImageName else ""
                                    chapterContent.copy(data = chapterContent.data.copy(coverImage = newChapterImage))
                                }
                            actContent.copy(chapters = updatedChapters)
                        }

                    sagaContent.copy(
                        data = updatedSaga,
                        characters = updatedCharacters,
                        acts = updatedActs,
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        }

    suspend fun backupSaga(saga: SagaContent) =
        executeRequest {
            val rootDir = getBackupRoot() ?: error("Could not access backup directory")
            val sagaDir = File(rootDir, saga.data.id.toString())
            if (!sagaDir.exists()) {
                sagaDir.mkdirs()
            }

            val newSagaIconPath = copyFileToBackupDir(saga.data.icon, sagaDir)?.name ?: ""

            val newCharacters =
                saga.characters.map { characterContent ->
                    val newImagePath =
                        copyFileToBackupDir(characterContent.data.image, sagaDir)?.name ?: ""
                    characterContent.copy(data = characterContent.data.copy(image = newImagePath))
                }

            val newActs =
                saga.acts.map { actContent ->
                    val newChapters =
                        actContent.chapters.map { chapterContent ->
                            val newCoverPath =
                                copyFileToBackupDir(chapterContent.data.coverImage, sagaDir)?.name
                                    ?: ""
                            chapterContent.copy(data = chapterContent.data.copy(coverImage = newCoverPath))
                        }
                    actContent.copy(chapters = newChapters)
                }

            val backupSagaContent =
                saga.copy(
                    data = saga.data.copy(icon = newSagaIconPath),
                    characters = newCharacters,
                    acts = newActs,
                )

            val gson = Gson()
            val json = gson.toJson(backupSagaContent)
            val file = File(sagaDir, SAGA_JSON_FILE)
            FileWriter(file).use { it.write(json) }

            sagaDir.absolutePath
        }

    private fun copyFileToBackupDir(
        sourcePath: String,
        destinationDir: File,
    ): File? {
        return try {
            if (sourcePath.isBlank()) return null
            val sourceFile = File(sourcePath)
            if (!sourceFile.exists()) return null

            val destinationFile = File(destinationDir, sourceFile.name)
            sourceFile.copyTo(destinationFile, overwrite = true)
            destinationFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun backupEnabled(): Flow<Boolean> {
        val writeFlow = permissionService.observePermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val readFlow = permissionService.observePermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        return combine(writeFlow, readFlow) { writeStatus, readStatus ->
            writeStatus == PermissionStatus.GRANTED && readStatus == PermissionStatus.GRANTED
        }
    }
}
