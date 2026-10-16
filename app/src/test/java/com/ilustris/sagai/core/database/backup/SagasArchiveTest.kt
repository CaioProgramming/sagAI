package com.ilustris.sagai.core.database.backup

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class SagasArchiveTest {
    @get:Rule
    val tmp = TemporaryFolder()

    private val manifest =
        ArchiveManifest(
            formatVersion = SagasArchive.FORMAT_VERSION,
            dbVersion = 42,
            appVersion = "1.0",
            createdAt = 1L,
            sagaCount = 2,
            messageCount = 10,
            totalBytes = 100,
        )

    private fun buildArchive(): ByteArray {
        val filesRoot = tmp.newFolder("files")
        val db = tmp.newFile("SagaDatabase").apply { writeText("db-bytes") }
        val saga = filesRoot.resolve("sagas/2").apply { mkdirs() }
        saga.resolve("cover.png").writeBytes(byteArrayOf(1, 2, 3, 4))
        saga.resolve("audios").mkdirs()
        saga.resolve("audios/line.wav").writeBytes(ByteArray(2000) { 7 })
        filesRoot.resolve("backups").apply { mkdirs() }.resolve("old.db").writeText("must not ship")

        val out = ByteArrayOutputStream()
        SagasArchive.write(out, manifest, db, filesRoot, "sagas")
        return out.toByteArray()
    }

    @Test
    fun `round trips the database and the saga media, and leaves other folders out`() {
        val staging = tmp.newFolder("staging")

        val restored = SagasArchive.extract(ByteArrayInputStream(buildArchive()), staging)

        assertEquals(manifest, restored)
        assertEquals("db-bytes", staging.resolve("db/SagaDatabase").readText())
        assertArrayEquals(byteArrayOf(1, 2, 3, 4), staging.resolve("files/sagas/2/cover.png").readBytes())
        assertEquals(2000, staging.resolve("files/sagas/2/audios/line.wav").length())
        assertFalse(staging.resolve("files/backups").exists())
    }

    @Test
    fun `peeks the manifest without extracting`() {
        assertEquals(manifest, SagasArchive.readManifest(ByteArrayInputStream(buildArchive())))
    }

    @Test
    fun `rejects a file that is not a sagas backup`() {
        val zip = ByteArrayOutputStream()
        ZipOutputStream(zip).use {
            it.putNextEntry(ZipEntry("something.txt"))
            it.write(1)
            it.closeEntry()
        }

        assertThrows(IllegalStateException::class.java) {
            SagasArchive.readManifest(ByteArrayInputStream(zip.toByteArray()))
        }
    }

    @Test
    fun `rejects entries that escape the staging folder`() {
        val zip = ByteArrayOutputStream()
        ZipOutputStream(zip).use {
            it.putNextEntry(ZipEntry("files/../../evil.txt"))
            it.write(1)
            it.closeEntry()
        }

        val staging = tmp.newFolder("staging")
        assertThrows(IllegalStateException::class.java) {
            SagasArchive.extract(ByteArrayInputStream(zip.toByteArray()), staging)
        }
        assertFalse(staging.parentFile.resolve("evil.txt").exists())
    }

    @Test
    fun `rejects a truncated backup`() {
        val bytes = buildArchive()
        val staging = tmp.newFolder("staging")

        assertThrows(Exception::class.java) {
            SagasArchive.extract(ByteArrayInputStream(bytes.copyOf(bytes.size / 2)), staging)
        }
    }

    @Test
    fun `rejects a backup whose content no longer matches the checksums`() {
        val bytes = buildArchive()
        // flip a byte of the (stored, uncompressed) png payload
        val needle = byteArrayOf(1, 2, 3, 4)
        val at = (0..bytes.size - needle.size).first { i -> needle.indices.all { bytes[i + it] == needle[it] } }
        bytes[at] = 9
        val staging = tmp.newFolder("staging")

        assertThrows(Exception::class.java) {
            SagasArchive.extract(ByteArrayInputStream(bytes), staging)
        }
    }
}
