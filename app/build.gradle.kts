import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(notation = libs.plugins.androidApplication)
    alias(notation = libs.plugins.jetbrainsKotlinAndroid)
    alias(notation = libs.plugins.jetbrainsKotlinParcelize)
    alias(notation = libs.plugins.kotlin.serialization)
    alias(notation = libs.plugins.googlePlayServices)
    alias(notation = libs.plugins.googleFirebase)
    alias(notation = libs.plugins.devToolsKsp)
    alias(notation = libs.plugins.about.libraries)
    alias(notation = libs.plugins.mannodermaus)
    id("com.google.android.gms.oss-licenses-plugin")
}
android {
    compileSdk = 36
    namespace = "com.d4rk.qrcodescanner.plus"
    defaultConfig {
        applicationId = "com.d4rk.qrcodescanner.plus"
        minSdk = 23
        targetSdk = 36
        versionCode = 41
        versionName = "4.0.0-beta2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        @Suppress("UnstableApiUsage")
        androidResources.localeFilters += listOf(
            "ar-rEG",
            "bg-rBG",
            "bn-rBD",
            "de-rDE",
            "en",
            "es-rGQ",
            "es-rMX",
            "fil-rPH",
            "fr-rFR",
            "hi-rIN",
            "hu-rHU",
            "in-rID",
            "it-rIT",
            "ja-rJP",
            "ko-rKR",
            "pl-rPL",
            "pt-rBR",
            "ro-rRO",
            "ru-rRU",
            "sv-rSE",
            "th-rTH",
            "tr-rTR",
            "uk-rUA",
            "ur-rPK",
            "vi-rVN",
            "zh-rTW"
        )
        vectorDrawables {
            useSupportLibrary = true
        }
        val githubProps = Properties()
        val githubFile = rootProject.file("github.properties")
        val githubToken = if (githubFile.exists()) {
            githubProps.load(githubFile.inputStream())
            githubProps["GITHUB_TOKEN"].toString()
        } else {
            ""
        }
        buildConfigField("String", "GITHUB_TOKEN", "\"$githubToken\"")
    }

    signingConfigs {
        create("release")

        val signingProps = Properties()
        val signingFile = rootProject.file("signing.properties")

        if (signingFile.exists()) {
            signingProps.load(signingFile.inputStream())

            signingConfigs.getByName("release").apply {
                storeFile = file(signingProps["STORE_FILE"].toString())
                storePassword = signingProps["STORE_PASSWORD"].toString()
                keyAlias = signingProps["KEY_ALIAS"].toString()
                keyPassword = signingProps["KEY_PASSWORD"].toString()
            }
        } else {
            android.buildTypes.getByName("release").signingConfig = null
        }
    }

    buildTypes {
        release {
            val signingFile = rootProject.file("signing.properties")
            signingConfig = if (signingFile.exists()) {
                signingConfigs.getByName("release")
            } else {
                null
            }
            isDebuggable = false
        }
        debug {
            isDebuggable = true
        }
    }

    buildTypes.forEach { buildType ->
        with(receiver = buildType) {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile(name = "proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    bundle {
        storeArchive {
            enable = true
        }
    }
}

dependencies {
    // Core AndroidX building blocks
    implementation(dependencyNotation = libs.bundles.androidx.core)

    // Lifecycle-aware AndroidX components
    implementation(dependencyNotation = libs.bundles.androidx.lifecycle)

    // Firebase services
    implementation(dependencyNotation = platform(libs.firebase.bom))
    implementation(dependencyNotation = libs.bundles.firebase.services)

    // Google Play services & Play Store APIs
    implementation(dependencyNotation = libs.bundles.google.core)

    // Kotlin Coroutines & Serialization helpers
    implementation(dependencyNotation = libs.bundles.kotlinx)

    // Networking (Ktor)
    implementation(dependencyNotation = platform(libs.ktor.bom))
    implementation(dependencyNotation = libs.bundles.networking)

    // Shared UI tooling and visuals
    implementation(dependencyNotation = libs.bundles.ui.toolkit)

    // CameraX
    implementation(dependencyNotation = libs.bundles.androidx.camera)

    // Barcode & QR feature utilities
    implementation(dependencyNotation = libs.bundles.barcode.stack)

    // Dependency Injection
    api(dependencyNotation = libs.bundles.koin)

    // Annotation processors (handled via KSP)
    ksp(dependencyNotation = libs.androidx.room.compiler)

    // Unit Tests
    testImplementation(dependencyNotation = libs.bundles.unitTest)
    testRuntimeOnly(dependencyNotation = libs.bundles.unitTestRuntime)

    // Instrumentation Tests
    androidTestImplementation(dependencyNotation = libs.bundles.instrumentationTest)
    debugImplementation(dependencyNotation = libs.androidx.ui.test.manifest)

    coreLibraryDesugaring(libs.android.desugarJdkLibs)
}