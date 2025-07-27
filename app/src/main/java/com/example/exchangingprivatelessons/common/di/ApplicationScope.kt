package com.example.exchangingprivatelessons.common.di

import javax.inject.Qualifier

/** Qualifier for a global CoroutineScope that lives for the duration of the application */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
