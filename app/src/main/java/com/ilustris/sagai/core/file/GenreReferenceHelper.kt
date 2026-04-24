package com.ilustris.sagai.core.file

import android.content.Context
import android.graphics.Bitmap
import coil3.BitmapImage
import coil3.ImageLoader
import timber.log.Timber
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.file.model.ReferenceCollection
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.features.newsaga.data.model.Genre

class GenreReferenceHelper(
    private val context: Context,
    private val firebaseRemoteConfig: RemoteConfigService,
    private val imageLoader: ImageLoader,
) {
    suspend fun getIconReference(genre: Genre): RequestResult<Bitmap> =
        executeRequest {
            val flag = "${genre.name.lowercase()}$ICON_FLAG"
            Timber.d("getIconReference: fetching flag from firebase $flag")
            val flagValue = firebaseRemoteConfig.getString(flag)
            Timber.d("getIconReference: flag value is $flagValue")
            flagValue
            val request =
                imageLoader.execute(
                    ImageRequest
                        .Builder(context)
                        .data(flagValue)
                        .allowHardware(false)
                        .build(),
                )

            Timber.d("getIconReference: Coil request is ${request.javaClass.simpleName}")
            (request as SuccessResult).image.toBitmap()
        }

    suspend fun getCoverReference(genre: Genre): RequestResult<Bitmap> =
        try {
            val flag = "${genre.name.lowercase()}$COVER_FLAG"
            Timber.d("getCoverReference: fetching flag from firebase $flag")
            val flagValue = firebaseRemoteConfig.getString(flag)
            Timber.d("getCoverReference: flag value is $flagValue")
            val coverUrl = flagValue
            val request =
                imageLoader.execute(
                    ImageRequest
                        .Builder(context)
                        .data(coverUrl)
                        .build(),
                )

            Timber.d("getCoverReference: Coil request is ${request.javaClass.simpleName}")

            (request.image as BitmapImage).bitmap.asSuccess()
        } catch (e: Exception) {
            Timber.w(
                "getCoverReference: failed, falling back to icon reference. Error: ${e.message}",
            )
            getIconReference(genre)
        }

    suspend fun getRandomCompositionReference(genre: Genre) =
        executeRequest {
            val multiFlag = "${genre.name}$COMPOSITION_REFERENCES".lowercase()
            Timber.d(
                "Attempting to fetch unified composition references: $multiFlag",
            )

            val flagValue =
                firebaseRemoteConfig.getJson<ReferenceCollection>(multiFlag)

            val referenceUrl =
                flagValue?.references?.random()
                    ?: firebaseRemoteConfig.getString("${genre.name.lowercase()}$COVER_FLAG")

            val request =
                ImageRequest
                    .Builder(context)
                    .data(referenceUrl!!)
                    .build()
            val imageResult = (imageLoader.execute(request) as SuccessResult)
            (imageResult.image as BitmapImage).bitmap to referenceUrl
        }

    suspend fun getRandomPortraitReference() =
        executeRequest {
            val multiFlag = PORTRAIT_REFERENCES
            Timber.d("Attempting to fetch multi-reference flag $multiFlag")

            val flagValue =
                firebaseRemoteConfig.getJson<ReferenceCollection>(multiFlag)

            val referenceUrl =
                flagValue?.references?.random() ?: firebaseRemoteConfig.getString(
                    PORTRAIT_REFERENCE,
                )

            Timber.d("Using reference ${referenceUrl!!}")

            val request =
                ImageRequest
                    .Builder(context)
                    .data(referenceUrl)
                    .build()
            val imageResult = (imageLoader.execute(request) as SuccessResult)
            (imageResult.image as BitmapImage).bitmap to referenceUrl
        }

    suspend fun getFileBitmap(path: String) =
        try {
            val request =
                ImageRequest
                    .Builder(context)
                    .data(path)
                    .build()
            val imageResult = (imageLoader.execute(request) as SuccessResult)
            (imageResult.image as BitmapImage).bitmap.asSuccess()
        } catch (e: Exception) {
            e.asError()
        }
}

private const val ICON_FLAG = "_icon_reference"
private const val COVER_FLAG = "_cover_reference"
private const val PORTRAIT_REFERENCE = "portrait_reference"
private const val PORTRAIT_REFERENCES = "portrait_references"
private const val COMPOSITION_REFERENCES = "_composition_references"
