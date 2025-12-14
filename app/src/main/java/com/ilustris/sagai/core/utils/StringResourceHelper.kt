package com.ilustris.sagai.core.utils

import android.content.Context
import androidx.annotation.StringRes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton helper class for accessing string resources from non-Compose contexts.
 *
 * This class provides a bridge between data classes (like ViewModels) and Android string resources,
 * enabling proper localization of error messages, status messages, and other user-facing text
 * that needs to be generated outside of Composable functions.
 *
 * Usage in ViewModels:
 * ```kotlin
 * @HiltViewModel
 * class MyViewModel @Inject constructor(
 *     private val stringHelper: StringResourceHelper
 * ) : ViewModel() {
 *
 *     fun showError() {
 *         val errorMessage = stringHelper.getString(R.string.error_message)
 *         // Use errorMessage
 *     }
 *
 *     fun showFormattedMessage(count: Int) {
 *         val message = stringHelper.getString(R.string.items_count, count)
 *         // Use message
 *     }
 * }
 * ```
 */
@Singleton
class StringResourceHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Retrieves a localized string from resources.
     *
     * @param resId The string resource ID
     * @return The localized string
     */
    fun getString(@StringRes resId: Int): String {
        return context.getString(resId)
    }

    /**
     * Retrieves a localized string from resources with format arguments.
     *
     * @param resId The string resource ID
     * @param formatArgs Variable number of format arguments to be inserted into the string
     * @return The formatted localized string
     *
     * Example:
     * ```kotlin
     * // For string resource: <string name="welcome_user">Welcome, %s!</string>
     * val message = stringHelper.getString(R.string.welcome_user, userName)
     *
     * // For string resource: <string name="items_count">%d of %d items</string>
     * val count = stringHelper.getString(R.string.items_count, current, total)
     * ```
     */
    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String {
        return context.getString(resId, *formatArgs)
    }

    /**
     * Retrieves a quantity string (plural) from resources.
     *
     * @param resId The plural resource ID
     * @param quantity The number used to get the correct string for the current language's plural rules
     * @return The localized quantity string
     *
     * Example:
     * ```kotlin
     * // For plural resource:
     * // <plurals name="saga_count">
     * //     <item quantity="one">%d saga</item>
     * //     <item quantity="other">%d sagas</item>
     * // </plurals>
     * val message = stringHelper.getQuantityString(R.plurals.saga_count, count, count)
     * ```
     */
    fun getQuantityString(@StringRes resId: Int, quantity: Int): String {
        return context.resources.getQuantityString(resId, quantity)
    }

    /**
     * Retrieves a quantity string (plural) from resources with format arguments.
     *
     * @param resId The plural resource ID
     * @param quantity The number used to get the correct string for the current language's plural rules
     * @param formatArgs Variable number of format arguments to be inserted into the string
     * @return The formatted localized quantity string
     */
    fun getQuantityString(@StringRes resId: Int, quantity: Int, vararg formatArgs: Any): String {
        return context.resources.getQuantityString(resId, quantity, *formatArgs)
    }

    /**
     * Retrieves a string array from resources.
     *
     * @param resId The string array resource ID
     * @return Array of localized strings
     *
     * Example:
     * ```kotlin
     * // For string array resource:
     * // <string-array name="genre_names">
     * //     <item>Fantasy</item>
     * //     <item>Sci-Fi</item>
     * // </string-array>
     * val genres = stringHelper.getStringArray(R.array.genre_names)
     * ```
     */
    fun getStringArray(@StringRes resId: Int): Array<String> {
        return context.resources.getStringArray(resId)
    }
}
