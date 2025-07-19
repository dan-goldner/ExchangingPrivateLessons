package com.example.exchangingprivatelessons.common.util

import androidx.room.TypeConverter
import org.mapstruct.Named
import java.util.Date
import kotlin.jvm.JvmStatic

object TimestampConverter {

    /* ---------- Room ---------- */
    @TypeConverter @JvmStatic @Named("toDate")
    fun toDate(epoch: Long?): Date? = epoch?.let(::Date)

    @TypeConverter @JvmStatic @Named("toEpoch")
    fun toEpoch(date: Date?): Long? = date?.time


    /* ---------- MapStruct ---------- */
    /** Long?  → Date?  */
    @JvmStatic @Named("toDateNullable")
    fun toDateNullable(epoch: Long?): Date? = toDate(epoch)

    /** Date?  → Long?  */
    @JvmStatic @Named("toEpochNullable")
    fun toEpochNullable(date: Date?): Long? = toEpoch(date)

    /** Long   → Date   */
    @JvmStatic @Named("toDateNonNull")
    fun toDateNonNull(epoch: Long): Date = Date(epoch)

    /** Date   → Long   */
    @JvmStatic @Named("toEpochNonNull")
    fun toEpochNonNull(date: Date): Long = date.time
}
