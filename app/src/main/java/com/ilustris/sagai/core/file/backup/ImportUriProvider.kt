package com.ilustris.sagai.core.file.backup

import android.net.Uri
import androidx.compose.runtime.compositionLocalOf

val ImportUriProvider = compositionLocalOf<Uri?> { { null }() }