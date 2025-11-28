package com.ilustris.sagai.core.file.backup

import android.graphics.Bitmap
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.Genre

data class SagaManifest(
    val sagaId: Int,
    val title: String,
    val description: String,
    val genre: Genre,
    val iconName: String,
    val lastBackup: Long,
    val zipFileName: String,
)

data class RestorableSaga(
    val manifest: SagaManifest,
    val iconBitmap: Bitmap?,
)

fun SagaManifest.toSaga() =
    Saga(
        id = sagaId,
        title = title,
        description = description,
        icon = iconName,
    )

fun List<RestorableSaga>.filterBackups(sagas: List<Saga>) =
    filter { data ->
        sagas.none {
            it.id == data.manifest.sagaId &&
                it.title == data.manifest.title
        }
    }
