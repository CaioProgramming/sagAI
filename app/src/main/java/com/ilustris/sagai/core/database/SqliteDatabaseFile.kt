package com.ilustris.sagai.core.database

import java.io.File
import java.io.InputStream

private const val SQLITE_HEADER = "SQLite format 3\u0000"
private const val MIN_SQLITE_BYTES = 512L

/**
 * Validates that [file] looks like a SQLite database before replacing the live DB file.
 */
fun requireSqliteDatabaseFile(file: File) {
    check(file.exists() && file.length() >= MIN_SQLITE_BYTES) {
        "Selected file is not a valid SQLite database"
    }
    file.inputStream().use { input ->
        requireSqliteHeader(input)
    }
}

private fun requireSqliteHeader(input: InputStream) {
    val header = ByteArray(SQLITE_HEADER.length)
    val read = input.read(header)
    check(read == header.size) { "Selected file is not a valid SQLite database" }
    check(String(header, Charsets.US_ASCII) == SQLITE_HEADER) {
        "Selected file is not a valid SQLite database"
    }
}
