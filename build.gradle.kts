plugins {
    alias(notation = libs.plugins.androidApplication) apply false
    alias(notation = libs.plugins.androidLibrary) apply false
    alias(notation = libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(notation = libs.plugins.jetbrainsKotlinParcelize) apply false
    alias(notation = libs.plugins.kotlin.serialization) apply false
    alias(notation = libs.plugins.compose.compiler) apply false
    alias(notation = libs.plugins.googlePlayServices) apply false
    alias(notation = libs.plugins.googleFirebase) apply false
    alias(notation = libs.plugins.devToolsKsp) apply false
    alias(notation = libs.plugins.about.libraries) apply true
    alias(notation = libs.plugins.mannodermaus) apply false
    id("com.google.android.gms.oss-licenses-plugin") version "0.10.9" apply false
}