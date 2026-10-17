package com.ilustris.sagai.core.database.backup

import com.google.gson.Gson
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Layout of a `.sagas` file:
 *
 * - `manifest.json` (first entry, so it can be peeked without reading the whole file)
 * - `db/<name>` database snapshot
 * - `files/<relative path>` mirror of the saga media folder under `filesDir`
 * - `checksums.json` (last entry) SHA-256 of every db/files entry
 */
data class ArchiveManifest(
    val formatVersion: Int,
    val dbVersion: Int,
    val appVersion: String,
    val createdAt: Long,
    val sagaCount: Int,
    val messageCount: Int,
    val totalBytes: Long,
)

private data class ArchiveChecksums(
    val entries: Map<String, String>,
)

object SagasArchive {
    const val FORMAT_VERSION = 1
    const val EXTENSION = "sagas"
    const val MANIFEST_ENTRY = "manifest.json"
    const val CHECKSUMS_ENTRY = "checksums.json"
    const val DB_PREFIX = "db/"
    const val FILES_PREFIX = "files/"

    private val STORED_EXTENSIONS = setOf("png", "jpg", "jpeg", "webp", "mp3", "mp4", "m4a", "aac", "ogg")
    private val gson = Gson()

    fun suggestedFileName() = "sagas_${java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())}.$EXTENSION"

    /** Files under [filesRoot]'s [mediaDirName] folder plus the database snapshot, in zip entry order. */
    fun write(
        output: OutputStream,
        manifest: ArchiveManifest,
        database: File,
        filesRoot: File,
        mediaDirName: String,
        onProgress: (written: Long) -> Unit = {},
    ) {
        val checksums = linkedMapOf<String, String>()
        var written = 0L
        ZipOutputStream(output.buffered(BUFFER_SIZE)).use { zip ->
            zip.putNextEntry(ZipEntry(MANIFEST_ENTRY))
            zip.write(gson.toJson(manifest).toByteArray())
            zip.closeEntry()

            fun add(
                name: String,
                file: File,
            ) {
                checksums[name] = putFile(zip, name, file) { written += it; onProgress(written) }
            }

            add(DB_PREFIX + database.name, database)
            File(filesRoot, mediaDirName)
                .walkTopDown()
                .filter { it.isFile }
                .forEach { add(FILES_PREFIX + it.relativeTo(filesRoot).invariantSeparatorsPath, it) }

            zip.putNextEntry(ZipEntry(CHECKSUMS_ENTRY))
            zip.write(gson.toJson(ArchiveChecksums(checksums)).toByteArray())
            zip.closeEntry()
        }
    }

    /** Reads only the manifest, which is the first entry. */
    fun readManifest(input: InputStream): ArchiveManifest {
        ZipInputStream(input).use { zip ->
            val entry = zip.nextEntry
            check(entry?.name == MANIFEST_ENTRY) { "Not a Sagas backup" }
            val manifest = gson.fromJson(zip.readBytes().decodeToString(), ArchiveManifest::class.java)
            check(manifest != null) { "Not a Sagas backup" }
            return manifest
        }
    }

    /**
     * Extracts every db/files entry into [staging] (keeping their entry names as relative paths)
     * and verifies them against the archive checksums. Throws if the archive is corrupted or
     * tries to escape [staging].
     */
    fun extract(
        input: InputStream,
        staging: File,
        onProgress: (read: Long) -> Unit = {},
    ): ArchiveManifest {
        val root = staging.canonicalFile
        val actual = mutableMapOf<String, String>()
        var manifest: ArchiveManifest? = null
        var expected: Map<String, String>? = null
        var read = 0L

        ZipInputStream(input.buffered(BUFFER_SIZE)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                when {
                    entry.isDirectory -> Unit

                    entry.name == MANIFEST_ENTRY ->
                        manifest = gson.fromJson(zip.readBytes().decodeToString(), ArchiveManifest::class.java)

                    entry.name == CHECKSUMS_ENTRY ->
                        expected = gson.fromJson(zip.readBytes().decodeToString(), ArchiveChecksums::class.java).entries

                    entry.name.startsWith(DB_PREFIX) || entry.name.startsWith(FILES_PREFIX) -> {
                        val target = File(root, entry.name).canonicalFile
                        check(target.path.startsWith(root.path + File.separator)) { "Unsafe entry: ${entry.name}" }
                        target.parentFile?.mkdirs()
                        val digest = MessageDigest.getInstance("SHA-256")
                        target.outputStream().use { out ->
                            val buffer = ByteArray(BUFFER_SIZE)
                            var count = zip.read(buffer)
                            while (count != -1) {
                                digest.update(buffer, 0, count)
                                out.write(buffer, 0, count)
                                read += count
                                onProgress(read)
                                count = zip.read(buffer)
                            }
                        }
                        actual[entry.name] = digest.hex()
                    }
                }
                entry = zip.nextEntry
            }
        }

        val checksums = checkNotNull(expected) { "Backup is incomplete" }
        check(actual == checksums) { "Backup file corrupted" }
        return checkNotNull(manifest) { "Not a Sagas backup" }
    }

    private fun putFile(
        zip: ZipOutputStream,
        name: String,
        file: File,
        onBytes: (Long) -> Unit,
    ): String {
        val entry = ZipEntry(name)
        if (file.extension.lowercase() in STORED_EXTENSIONS) {
            entry.method = ZipEntry.STORED
            entry.size = file.length()
            entry.compressedSize = file.length()
            entry.crc = file.crc()
        }
        zip.putNextEntry(entry)
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(BUFFER_SIZE)
            var count = input.read(buffer)
            while (count != -1) {
                digest.update(buffer, 0, count)
                zip.write(buffer, 0, count)
                onBytes(count.toLong())
                count = input.read(buffer)
            }
        }
        zip.closeEntry()
        return digest.hex()
    }

    private fun File.crc(): Long {
        val crc = CRC32()
        inputStream().use { input ->
            val buffer = ByteArray(BUFFER_SIZE)
            var count = input.read(buffer)
            while (count != -1) {
                crc.update(buffer, 0, count)
                count = input.read(buffer)
            }
        }
        return crc.value
    }

    private fun MessageDigest.hex() = digest().joinToString("") { "%02x".format(it) }

    private const val BUFFER_SIZE = 64 * 1024
}
