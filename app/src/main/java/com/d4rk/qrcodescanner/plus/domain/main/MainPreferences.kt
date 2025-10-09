package com.d4rk.qrcodescanner.plus.domain.main

data class MainPreferences(
    val theme : ThemePreference ,
    val languageTag : String ,
    val bottomNavigationLabels : BottomNavigationLabelsPreference ,
    val startDestination : StartDestinationPreference
)

enum class ThemePreference {
    FOLLOW_SYSTEM ,
    LIGHT ,
    DARK ,
    AUTO_BATTERY
}

enum class BottomNavigationLabelsPreference {
    LABELED ,
    SELECTED ,
    UNLABELED
}

enum class StartDestinationPreference {
    SCAN ,
    CREATE ,
    HISTORY
}

interface MainPreferencesRepository {
    fun getMainPreferences() : MainPreferences
}
