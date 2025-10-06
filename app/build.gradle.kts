import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(notation = libs.plugins.androidApplication)
    alias(notation = libs.plugins.jetbrainsKotlinAndroid)
    alias(notation = libs.plugins.jetbrainsKotlinParcelize)
    alias(notation = libs.plugins.kotlin.serialization)
    alias(notation = libs.plugins.googlePlayServices)
    alias(notation = libs.plugins.googleFirebase)
    alias(notation = libs.plugins.compose.compiler)
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
        minSdk = 26
        targetSdk = 36
        versionCode = 31
        versionName = "3.1.1"
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
            multiDexEnabled = true
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile(name = "proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
        compose = true
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
    // App Core
    implementation(dependencyNotation = "com.github.D4rK7355608:AppToolkit:1.0.38") {
        isTransitive = true
    }

    // KSP TODO: Make them bundle
    ksp(dependencyNotation = libs.androidx.room.compiler)
    implementation(dependencyNotation = libs.androidx.room.ktx)
    implementation(dependencyNotation = libs.androidx.room.runtime)
    implementation(dependencyNotation = libs.androidx.room.paging)


    // AndroidX
    api(dependencyNotation = libs.bundles.androidx.core)

    // Lifecycle
    api(dependencyNotation = libs.bundles.androidx.lifecycle)

    // Firebase
    api(dependencyNotation = platform(libs.firebase.bom))
    api(dependencyNotation = libs.bundles.firebase)

    // Google Play services & Play Store APIs
    api(dependencyNotation = libs.bundles.google.play)

    // Image loading
    api(dependencyNotation = libs.bundles.coil)

    // Kotlin Coroutines & Serialization
    api(dependencyNotation = libs.bundles.kotlinx)

    // Networking (Ktor)
    api(dependencyNotation = platform(libs.ktor.bom))
    api(dependencyNotation = libs.bundles.ktor)

    // Unit Tests
    testImplementation(dependencyNotation = libs.bundles.unitTest)
    testRuntimeOnly(dependencyNotation = libs.bundles.unitTestRuntime)

    // Instrumentation Tests
    androidTestImplementation(dependencyNotation = libs.bundles.instrumentationTest)
    debugImplementation(dependencyNotation = libs.androidx.ui.test.manifest)

    // TODO: Delete soon
    implementation("androidx.paging:paging-runtime-ktx:3.3.6")
    implementation("com.googlecode.ez-vcard:ez-vcard:0.12.1")
    implementation("com.google.android.gms:play-services-oss-licenses:17.3.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.5")
    implementation("androidx.gridlayout:gridlayout:1.1.0")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("com.github.yuriy-budiyev:code-scanner:2.3.2")
    implementation("com.airbnb.android:lottie:6.6.10")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("commons-codec:commons-codec:1.19.0")
    implementation("dev.turingcomplete:kotlin-onetimepassword:2.4.1")
    implementation("me.zhanghai.android.fastscroll:library:1.3.0")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
}