package com.ilustris.sagai.core.database.backup

import kotlinx.coroutines.sync.Mutex

/** Serializes backup / restore / import so only one exclusive DB file operation runs at a time. */
internal val databaseBackupLock = Mutex()
