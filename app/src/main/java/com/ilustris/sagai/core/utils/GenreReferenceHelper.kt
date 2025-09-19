package com.ilustris.sagai.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import coil3.BitmapImage
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.toBitmap
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.asError
import com.ilustris.sagai.core.data.asSuccess
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.defaultHeaderImage

class GenreReferenceHelper(
    private val context: Context,
    private val firebaseRemoteConfig: FirebaseRemoteConfig,
    private val imageLoader: ImageLoader,
) {
    suspend fun getGenreStyleReference(genre: Genre) =
        executeRequest {
            BitmapFactory.decodeResource(
                context.resources,
                genre.defaultHeaderImage(),
            )
        }

    suspend fun getIconReference(genre: Genre): RequestResult<Bitmap> =
        try {
            val flag = "${genre.name.lowercase()}$ICON_FLAG"
            Log.d(javaClass.simpleName, "getIconReference: fetching flag from firebase $flag")
            val flagValue = firebaseRemoteConfig.getString(flag)
            Log.d(javaClass.simpleName, "getIconReference: flag value is $flagValue")
            val iconUrl = flagValue
            val request =
                imageLoader.execute(
                    ImageRequest
                        .Builder(context)
                        .data(iconUrl)
                        .build(),
                )

            Log.d(javaClass.simpleName, "getIconReference: Coil request is ${request.javaClass.simpleName}")

            (request.image as BitmapImage).bitmap.asSuccess()
        } catch (e: Exception) {
            e.asError()
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
