package com.ilustris.sagai.features.playthrough

import android.icu.text.MeasureFormat
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import java.util.Locale

private const val MS_PER_MINUTE = 60_000L
private const val MS_PER_HOUR = 60 * MS_PER_MINUTE
private const val MS_PER_DAY = 24 * MS_PER_HOUR
private const val MS_PER_WEEK = 7 * MS_PER_DAY
private const val MS_PER_MONTH = 30 * MS_PER_DAY

data class PlaytimeUnit(
    val value: Int,
    val unit: MeasureUnit,
)

data class PlaytimeBreakdown(
    val primary: PlaytimeUnit,
    val secondary: PlaytimeUnit,
) {
    /** Locale-aware rendering (unit names, plurals, and separators follow [locale]'s conventions). */
    fun format(locale: Locale = Locale.getDefault()): String =
        MeasureFormat
            .getInstance(locale, MeasureFormat.FormatWidth.NARROW)
            .formatMeasures(
                Measure(primary.value, primary.unit),
                Measure(secondary.value, secondary.unit),
            )
}

/**
 * Splits playtime in milliseconds into its two most significant units, escalating
 * from hours/minutes up through days, weeks, and months as the total grows.
 */
fun Long.toPlaytimeBreakdown(): PlaytimeBreakdown =
    when {
        this >= MS_PER_MONTH -> {
            val months = this / MS_PER_MONTH
            val weeks = (this % MS_PER_MONTH) / MS_PER_WEEK
            PlaytimeBreakdown(
                PlaytimeUnit(months.toInt(), MeasureUnit.MONTH),
                PlaytimeUnit(weeks.toInt(), MeasureUnit.WEEK),
            )
        }
        this >= MS_PER_WEEK -> {
            val weeks = this / MS_PER_WEEK
            val days = (this % MS_PER_WEEK) / MS_PER_DAY
            PlaytimeBreakdown(
                PlaytimeUnit(weeks.toInt(), MeasureUnit.WEEK),
                PlaytimeUnit(days.toInt(), MeasureUnit.DAY),
            )
        }
        this >= MS_PER_DAY -> {
            val days = this / MS_PER_DAY
            val hours = (this % MS_PER_DAY) / MS_PER_HOUR
            PlaytimeBreakdown(
                PlaytimeUnit(days.toInt(), MeasureUnit.DAY),
                PlaytimeUnit(hours.toInt(), MeasureUnit.HOUR),
            )
        }
        else -> {
            val totalMinutes = this / MS_PER_MINUTE
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            PlaytimeBreakdown(
                PlaytimeUnit(hours.toInt(), MeasureUnit.HOUR),
                PlaytimeUnit(minutes.toInt(), MeasureUnit.MINUTE),
            )
        }
    }

/**
 * Formats playtime in milliseconds to a locale-aware string, e.g. "2h 30min",
 * "3d 5h", "2w 4d", or "1mo 2w" once the total is large enough.
 */
fun Long.toPlaytimeFormat(locale: Locale = Locale.getDefault()): String = toPlaytimeBreakdown().format(locale)
