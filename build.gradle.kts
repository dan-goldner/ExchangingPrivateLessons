// File: build.gradle.kts (Project level)

// הגדרת הפלאגינים באמצעות כינויים מקובץ ה-TOML
// אין צורך לציין כאן גרסאות, הן מגיעות מ-libs.versions.toml
plugins {
    alias(libs.plugins.android.application)      apply false
    alias(libs.plugins.kotlin.android)           apply false
    alias(libs.plugins.compose.compiler)         apply false
    alias(libs.plugins.ksp)                      apply false
    alias(libs.plugins.hilt.android)             apply false
    alias(libs.plugins.google.services)          apply false
    alias(libs.plugins.navigation.safeargs)      apply false
    alias(libs.plugins.kotlin.serialization)     apply false
}
