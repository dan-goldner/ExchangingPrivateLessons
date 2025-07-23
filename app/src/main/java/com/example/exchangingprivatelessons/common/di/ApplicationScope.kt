package com.example.exchangingprivatelessons.common.di

import javax.inject.Qualifier

/** Qualifier ל‑CoroutineScope גלובלי החי כל זמן חיי‑האפליקציה */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
