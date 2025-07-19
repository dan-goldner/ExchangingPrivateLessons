/* build.gradle.kts – app module */

plugins {
    // Android & Kotlin
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    // Jetpack Compose
    alias(libs.plugins.kotlin.compose)

    // KSP – used for Room, Hilt (KSP variant), Glide‑KSP, etc.
    alias(libs.plugins.ksp)

    // Dagger‑Hilt plugin (needed for @HiltAndroidApp, etc.)
    alias(libs.plugins.hilt.android)

    // Google Services / Firebase
    alias(libs.plugins.google.services)

    // Navigation Safe‑Args (Kotlin)
    alias(libs.plugins.navigation.safeargs)

    // Kotlinx‑serialization
    alias(libs.plugins.kotlin.serialization)

    // **KAPT** – for MapStruct (and any legacy processors)
    kotlin("kapt")
}

/* ---------- KSP options (Room schema, etc.) ---------- */
ksp {
    val roomSchemas = "$projectDir/schemas"
    arg("room.schemaLocation", roomSchemas)
    arg("room.incremental", "true")
}

/* ---------- KAPT options ---------- */
kapt {
    // Helpful for Hilt / MapStruct error messages
    correctErrorTypes = true
}

android {
    namespace = "com.example.exchangingprivatelessons"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.example.exchangingprivatelessons"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
}

/* ---------- Dependencies ---------- */
dependencies {

    /* Core / UI */
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.google.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.swiperefreshlayout)

    /* Lifecycle */
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    /* Navigation */
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.compose)

    /* Room – runtime + compiler (KSP) */
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    /* Paging */
    implementation(libs.androidx.paging.runtime.ktx)

    /* Firebase */
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.functions)
    implementation(libs.firebase.firestore.ktx)

    /* Hilt – Android + KSP processor */
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    /* Glide (KSP flavour) */
    implementation(libs.glide)
    ksp(libs.glide.compiler)

    /* Jetpack Compose */
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)

    /* Coroutines helpers */
    implementation(libs.kotlinxCoroutinesPlayServices)
    testImplementation(libs.kotlinxCoroutinesTest)

    /* Preferences, extra views */
    implementation(libs.androidx.preference.ktx)
    implementation(libs.circleimageview)

    /* Vector drawables */
    implementation(libs.androidx.vectordrawable)
    implementation(libs.androidx.vectordrawable.animated)

    /* Coil – images */
    implementation(libs.coil.compose)
    implementation(libs.coil.svg)
    implementation(libs.coil.gif)

    /* ---------- MapStruct ---------- */
    implementation(libs.mapstruct)       // API
    kapt(libs.mapstruct.ksp)             // Annotation processor (mapstruct‑processor)

    /* Serialization */
    implementation(libs.kotlinx.serialization.json)

    /* Tests */
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    testImplementation(libs.androidx.core.testing)
}
