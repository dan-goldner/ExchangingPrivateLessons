// Top-level build file where you can add configuration options common to all
// sub-projects/modules.

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        val nav_version = "2.7.7"
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:$nav_version")
        classpath("com.google.gms:google-services:4.4.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.21")
    }
}

plugins {
    id("com.android.application") version "8.10.1" apply false
    id("com.android.library") version "8.10.1" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    alias(libs.plugins.kotlin.compose) apply false // הפלאגין הזה נשאר
}