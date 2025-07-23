package com.example.exchangingprivatelessons.common.util

import androidx.room.TypeConverter
import com.google.firebase.Timestamp
import org.mapstruct.Named
import java.util.Date
import kotlin.jvm.JvmStatic

object TimestampConverter {

    /* ---------- Room (Date ↔ Long) ---------- */

    /** epoch ms? → Date? (null אם 0/‑1/null) */
    @TypeConverter @JvmStatic @Named ("toDateNullable")
    fun toDateNullable(epoch: Long?): Date? =
        epoch?.takeIf { it > 0 }?.let { Date(it) }

    @TypeConverter @JvmStatic @Named("toDateNonNull")
    fun toDateNonNull(epoch: Long): Date = Date(epoch)

    /** Date? → epoch ms (0 אם null) */
    @TypeConverter @JvmStatic @Named("toEpochNullable")
    fun toEpochNullable(date: Date?): Long = date?.time ?: 0L

    @TypeConverter @JvmStatic @Named("toEpochNonNull")
    fun toEpochNonNull(date: Date): Long = date.time


    /* ---------- Firebase (Timestamp ↔ Long / Date) ---------- */

    /** Timestamp? → Long? */
    @JvmStatic @Named("tsToEpochNullable")
    fun tsToEpochNullable(ts: Timestamp?): Long? = ts?.toDate()?.time

    /** Long? → Timestamp? */
    @JvmStatic @Named("epochToTsNullable")
    fun epochToTsNullable(epoch: Long?): Timestamp? =
        epoch?.let { Timestamp(Date(it)) }

    /** Timestamp? → Date? */
    @JvmStatic @Named("tsToDateNullable")
    fun tsToDateNullable(ts: Timestamp?): Date? = ts?.toDate()


    /* ---------- Quality‑of‑life ext ---------- */

    fun Long?.asDate(): Date? = this?.let(::Date)
    fun Date?.asEpoch(): Long? = this?.time


    /** Date? → Timestamp?  ⬅️ חדש */
    @JvmStatic @Named("dateToTsNullable")
    fun dateToTsNullable(date: Date?): Timestamp? = date?.let { Timestamp(it) }

    @JvmStatic @Named("tsToEpochNonNull")
    fun tsToEpochNonNull(ts: Timestamp?): Long = ts?.toDate()?.time ?: 0L


    @JvmStatic @Named("epochToTsNonNull")
    fun epochToTsNonNull(epoch: Long): Timestamp =
        Timestamp(Date(epoch))

}
