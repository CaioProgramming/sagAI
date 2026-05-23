package com.ilustris.sagai.core.file

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.net.toFile
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

data class ContentUriInfo(
    val displayName: String?,
    val reportedSizeBytes: Long?,
)

/**
 * Copies a document [uri] to [dest] using the most reliable API available.
 * Some OEMs / cloud providers return empty streams from [ContentResolver.openInputStream]
 * while [openFileDescriptor] works (and vice versa).
 */
fun copyContentUriToFile(
    context: Context,
    uri: Uri,
    dest: File,
): ContentUriInfo {
    dest.parentFile?.mkdirs()
    if (dest.exists()) dest.delete()

    val info = queryContentUriInfo(context.contentResolver, uri)

    // file:// (rare) — direct copy
    if (uri.scheme.equals(ContentResolver.SCHEME_FILE, ignoreCase = true)) {
        val source = uri.toFile()
        if (!source.exists()) error("Could not read selected file")
        source.copyTo(dest, overwrite = true)
        return info
    }

    val pfd =
        context.contentResolver.openFileDescriptor(uri, "r")
    if (pfd != null) {
        pfd.use {
            FileInputStream(it.fileDescriptor).use { input ->
                FileOutputStream(dest).use { output ->
                    input.copyTo(output)
                }
            }
        }
    } else {
        context.contentResolver.openInputStream(uri)
            ?: error("Could not read selected file")
            .use { input ->
                FileOutputStream(dest).use { output ->
                    input.copyTo(output)
                }
            }
    }

    val copied = dest.length()
    val expected = info.reportedSizeBytes
    if (expected != null && expected > 0L && copied != expected) {
        error(
            "Incomplete file read ($copied of $expected bytes). " +
                "Download the file on this device first, then try again.",
        )
    }
    if (copied == 0L) {
        error("Selected file is empty or could not be read")
    }

    return info
}

fun queryContentUriInfo(
    resolver: ContentResolver,
    uri: Uri,
): ContentUriInfo {
    var displayName: String? = null
    var size: Long? = null
    resolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0) displayName = cursor.getString(nameIndex)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                size = cursor.getLong(sizeIndex)
            }
        }
    }
    return ContentUriInfo(displayName, size)
}
