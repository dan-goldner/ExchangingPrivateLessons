package com.example.exchangingprivatelessons.common.util

import android.content.Context
import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import com.google.firebase.Timestamp

/** פורמטרים נפרדים בתוך object לשימוש בטוח ואחיד */
object TimeFormatters {
    val date: SimpleDateFormat = SimpleDateFormat("d MMM yyyy", Locale.US)
    val time: SimpleDateFormat = SimpleDateFormat("HH:mm", Locale.US)
}

/** Firebase Timestamp → millis */
val Timestamp.millis: Long
    get() = seconds * 1_000L + nanoseconds / 1_000_000L

/** “5 min ago” / “Yesterday” / “12 Mar 2024” */
fun Long.pretty(): String {
    val now = System.currentTimeMillis()
    val diff = now - this

    return when {
        diff < TimeUnit.MINUTES.toMillis(1)  -> "Just now"
        diff < TimeUnit.HOURS.toMillis(1)    -> "${TimeUnit.MILLISECONDS.toMinutes(diff)} min ago"
        diff < TimeUnit.DAYS.toMillis(1)     -> "${TimeUnit.MILLISECONDS.toHours(diff)} h ago"
        diff < TimeUnit.DAYS.toMillis(2)     -> "Yesterday"
        else                                 -> TimeFormatters.date.format(Date(this))
    }
}

/** Firebase Timestamp → תצוגת זמן יחסית */
fun Timestamp.pretty(): String = millis.pretty()

/** תצוגת שעה אם היום, תאריך מלא אם לא */
fun Timestamp.asClockOrDate(): String {
    val then = Date(millis)
    val now = Date()
    return if (TimeFormatters.date.format(then) == TimeFormatters.date.format(now)) {
        TimeFormatters.time.format(then)
    } else {
        TimeFormatters.date.format(then)
    }
}

fun Long.asClockOrDate(): String {
    val then = Date(this)
    val now = Date()
    return if (TimeFormatters.date.format(then) == TimeFormatters.date.format(now)) {
        TimeFormatters.time.format(then)
    } else {
        TimeFormatters.date.format(then)
    }
}

fun Long.toRelativeTime(ctx: Context): String =
    DateUtils.getRelativeTimeSpanString(
        this, System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE
    ).toString()


private val fullDf by lazy {                         // ‑24 Jul 2025 · 17:30
    SimpleDateFormat("d MMM yyyy · HH:mm", Locale.getDefault())
}

fun Long.asFullDateTime(): String = fullDf.format(Date(this))

