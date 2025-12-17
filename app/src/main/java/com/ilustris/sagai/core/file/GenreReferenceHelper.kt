package com.ilustris.sagai.core.file

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import coil3.BitmapImage
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.data.executeRequest
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
            Log.d(javaClass.simpleName, "getIconReference: fetching flag from firebase $flag")
            val flagValue = firebaseRemoteConfig.getString(flag)
            Log.d(javaClass.simpleName, "getIconReference: flag value is $flagValue")
            flagValue
            val request =
                imageLoader.execute(
                    ImageRequest
                        .Builder(context)
                        .data(flagValue)
                        .allowHardware(false)
                        .build(),
                )

            Log.d(javaClass.simpleName, "getIconReference: Coil request is ${request.javaClass.simpleName}")
            (request as SuccessResult).image.toBitmap()
        }

    suspend fun getCoverReference(genre: Genre): RequestResult<Bitmap> =
        try {
            val flag = "${genre.name.lowercase()}$COVER_FLAG"
            Log.d(javaClass.simpleName, "getCoverReference: fetching flag from firebase $flag")
            val flagValue = firebaseRemoteConfig.getString(flag)
            Log.d(javaClass.simpleName, "getCoverReference: flag value is $flagValue")
            val coverUrl = flagValue
            val request =
                imageLoader.execute(
                    ImageRequest
                        .Builder(context)
                        .data(coverUrl)
                        .build(),
                )

            Log.d(javaClass.simpleName, "getCoverReference: Coil request is ${request.javaClass.simpleName}")

            (request.image as BitmapImage).bitmap.asSuccess()
        } catch (e: Exception) {
            e.asError()
        }

    suspend fun getPortraitReference(): RequestResult<Bitmap> =
        try {
            val flag = PORTRAIT_REFERENCE
            Log.d(javaClass.simpleName, "getPortraitReference: fetching flag from firebase $flag")
            val flagValue = firebaseRemoteConfig.getString(flag)
            Log.d(javaClass.simpleName, "getPortraitReference: flag value is $flagValue")
            val portraitUrl = flagValue

            val request =
                ImageRequest
                    .Builder(context)
                    .data(portraitUrl)
                    .build()
            val imageResult = (imageLoader.execute(request) as SuccessResult)
            (imageResult.image as BitmapImage).bitmap.asSuccess()
        } catch (e: Exception) {
            e.asError()
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
