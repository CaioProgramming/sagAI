package com.ilustris.sagai.features.widgets.glance

import android.content.Context
import com.ilustris.sagai.core.datastore.DataStorePreferences
import com.ilustris.sagai.features.widgets.WidgetUpdater
import com.ilustris.sagai.features.widgets.data.WidgetDataSource
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/** Glance widgets and receivers are not Hilt-injectable, so they reach their dependencies here. */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun dataSource(): WidgetDataSource

    fun updater(): WidgetUpdater

    fun dataStore(): DataStorePreferences
}

fun Context.widgetEntryPoint(): WidgetEntryPoint = EntryPointAccessors.fromApplication(applicationContext, WidgetEntryPoint::class.java)
