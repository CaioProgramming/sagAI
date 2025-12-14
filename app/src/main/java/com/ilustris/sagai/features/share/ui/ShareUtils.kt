package com.ilustris.sagai.features.share.ui

import android.content.Context
import android.content.Intent
import android.net.Uri

fun launchShareActivity(
    uri: Uri,
    context: Context,
) {
    val shareIntent =
        Intent().apply {
            action = Intent.ACTION_SEND
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(uri, context.contentResolver.getType(uri))
            putExtra(
                Intent.EXTRA_STREAM,
                uri,
            )
        }
    context.startActivity(Intent.createChooser(shareIntent, "Share your saga"))
}

