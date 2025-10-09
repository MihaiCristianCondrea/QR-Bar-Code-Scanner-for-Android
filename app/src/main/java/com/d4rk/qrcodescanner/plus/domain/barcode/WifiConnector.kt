@file:Suppress("DEPRECATION")

package com.d4rk.qrcodescanner.plus.domain.barcode

import android.content.Context
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiEnterpriseConfig
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import androidx.annotation.RequiresApi
import com.d4rk.qrcodescanner.plus.utils.extension.toCaps
import com.d4rk.qrcodescanner.plus.utils.extension.wifiManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object WifiConnector {
    private val hexRegex = """^[\da-f]+$""".toRegex(RegexOption.IGNORE_CASE)

    suspend fun connect(
        context : Context , authType : String , name : String , password : String , isHidden : Boolean , anonymousIdentity : String , identity : String , eapMethod : String , phase2Method : String
    ) : Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            tryToConnect(
                context , authType , name , password , isHidden , anonymousIdentity , identity , eapMethod.toEapMethod() , phase2Method.toPhase2Method()
            )
        }
    }

    private fun tryToConnect(
        context : Context , authType : String , name : String , password : String , isHidden : Boolean , anonymousIdentity : String , identity : String , eapMethod : Int? , phase2Method : Int?
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tryToConnectNewApi(context , authType , name , password , anonymousIdentity , identity , eapMethod , phase2Method)
        }
        else {
            tryToConnectOldApi(context , authType , name , password , isHidden , anonymousIdentity , identity , eapMethod , phase2Method)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun tryToConnectNewApi(
        context : Context , authType : String , name : String , password : String , anonymousIdentity : String , identity : String , eapMethod : Int? , phase2Method : Int?
    ) {
        when (authType.toCaps()) {
            "" , "NOPASS" -> connectToOpenNetworkNewApi(context , name)
            "WPA" , "WPA2" -> connectToWpa2NetworkNewApi(context , name , password)
            "WPA2-EAP" -> connectToWpa2EapNetworkNewApi(context , name , password , anonymousIdentity , identity , eapMethod , phase2Method)
            "WPA3" -> connectToWpa3NetworkNewApi(context , name , password)
            "WPA3-EAP" -> connectToWpa3EapNetworkNewApi(context , name , password , anonymousIdentity , identity , eapMethod , phase2Method)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun connectToOpenNetworkNewApi(context : Context , name : String) {
        val builder = WifiNetworkSuggestion.Builder().setSsid(name)
        connect(context , builder)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun connectToWpa2NetworkNewApi(context : Context , name : String , password : String) {
        val builder = WifiNetworkSuggestion.Builder().setSsid(name).setWpa2Passphrase(password)
        connect(context , builder)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun connectToWpa2EapNetworkNewApi(
        context : Context , name : String , password : String , anonymousIdentity : String , identity : String , eapMethod : Int? , phase2Method : Int?
    ) {
        val config = WifiEnterpriseConfig().also { config ->
            config.anonymousIdentity = anonymousIdentity
            config.identity = identity
            config.password = password
            eapMethod?.apply {
                config.eapMethod = this
            }
            phase2Method?.apply {
                config.phase2Method = this
            }
        }
        val builder = WifiNetworkSuggestion.Builder().setSsid(name).setWpa2Passphrase(password) // Note: setWpa2Passphrase might not be needed for EAP if password is in enterprise config
                .setWpa2EnterpriseConfig(config)

        connect(context , builder)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun connectToWpa3NetworkNewApi(context : Context , name : String , password : String) {
        val builder = WifiNetworkSuggestion.Builder().setSsid(name).setWpa3Passphrase(password)

        connect(context , builder)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun connectToWpa3EapNetworkNewApi(
        context : Context , name : String , password : String , anonymousIdentity : String , identity : String , eapMethod : Int? , phase2Method : Int?
    ) {
        val config = WifiEnterpriseConfig().also { config ->
            config.anonymousIdentity = anonymousIdentity
            config.identity = identity
            config.password = password
            eapMethod?.apply {
                config.eapMethod = this
            }
            phase2Method?.apply {
                config.phase2Method = this
            }
        }
        val builder = WifiNetworkSuggestion.Builder().setSsid(name)
                // Note: setWpa3Passphrase might not be needed for EAP if password is in enterprise config
                .setWpa3EnterpriseConfig(config)
        connect(context , builder)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun connect(context : Context , builder : WifiNetworkSuggestion.Builder) {
        val suggestions = listOf(builder.build())
        // These operations can throw SecurityException if permissions are missing
        // or other exceptions. Consider more specific error handling if needed.
        context.wifiManager?.removeNetworkSuggestions(suggestions)
        context.wifiManager?.addNetworkSuggestions(suggestions)
    }

    private fun tryToConnectOldApi(
        context : Context , authType : String , name : String , password : String , isHidden : Boolean , anonymousIdentity : String , identity : String , eapMethod : Int? , phase2Method : Int?
    ) {
        enableWifiIfNeeded(context)
        when (authType.toCaps()) {
            "" , "NOPASS" -> connectToOpenNetworkOldApi(context , name , isHidden)
            "WPA" , "WPA2" -> connectToWpaNetworkOldApi(context , name , password , isHidden)
            "WPA2-EAP" -> connectToWpa2EapNetworkOldApi(context , name , password , isHidden , anonymousIdentity , identity , eapMethod , phase2Method)
            "WEP" -> connectToWepNetworkOldApi(context , name , password , isHidden)
        }
    }

    private fun enableWifiIfNeeded(context : Context) {
        context.wifiManager?.apply {
            if (! isWifiEnabled) {
                @Suppress("MissingPermission") // Ensure you have CHANGE_WIFI_STATE permission
                isWifiEnabled = true
            }
        }
    }

    private fun connectToOpenNetworkOldApi(context : Context , name : String , isHidden : Boolean) {
        val wifiConfiguration = WifiConfiguration().apply {
            SSID = name.quote()
            hiddenSSID = isHidden
            allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
            allowedProtocols.set(WifiConfiguration.Protocol.RSN)
            allowedProtocols.set(WifiConfiguration.Protocol.WPA)
            allowedAuthAlgorithms.clear()
            allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
            allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
            allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
            allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104)
            allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
            allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
        }
        connect(context , wifiConfiguration)
    }

    private fun connectToWpaNetworkOldApi(context : Context , name : String , password : String , isHidden : Boolean) {
        val wifiConfiguration = WifiConfiguration().apply {
            SSID = name.quote()
            preSharedKey = password.quoteIfNotHex()
            hiddenSSID = isHidden
            allowedProtocols.set(WifiConfiguration.Protocol.RSN)
            allowedProtocols.set(WifiConfiguration.Protocol.WPA)
            allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
            allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
            allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
            allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
            allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104)
            allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
            allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
        }
        connect(context , wifiConfiguration)
    }

    private fun connectToWpa2EapNetworkOldApi(
        context : Context , name : String , password : String , isHidden : Boolean , anonymousIdentity : String , identity : String , eapMethod : Int? , phase2Method : Int?
    ) {
        val wifiConfiguration = WifiConfiguration().apply {
            SSID = name.quote()
            // For EAP, preSharedKey is usually not set directly. The password is part of enterpriseConfig.
            // However, some configurations might require it. Double-check the specific EAP type requirements.
            // preSharedKey = password.quoteIfNotHex()
            hiddenSSID = isHidden
            allowedProtocols.set(WifiConfiguration.Protocol.RSN)
            allowedProtocols.set(WifiConfiguration.Protocol.WPA)
            allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP) // Use WPA_EAP for enterprise
            allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X)
            allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
            allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
            allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
            allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104)
            allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
            allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
            enterpriseConfig.anonymousIdentity = anonymousIdentity
            enterpriseConfig.identity = identity
            enterpriseConfig.password = password
            eapMethod?.apply {
                enterpriseConfig.eapMethod = this
            }
            phase2Method?.apply {
                enterpriseConfig.phase2Method = this
            }
        }
        connect(context , wifiConfiguration)
    }

    private fun connectToWepNetworkOldApi(context : Context , name : String , password : String , isHidden : Boolean) {
        val wifiConfiguration = WifiConfiguration().apply {
            SSID = name.quote()
            wepKeys[0] = password.quoteIfNotHex() // Ensure WEP key format is correct (ASCII or hex)
            hiddenSSID = isHidden
            wepTxKeyIndex = 0
            allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
            allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
            allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED)
            allowedProtocols.set(WifiConfiguration.Protocol.RSN) // RSN/WPA not typical for WEP, but often included
            allowedProtocols.set(WifiConfiguration.Protocol.WPA)
            allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP) // Not typical for WEP
            allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP) // TKIP is more common with WEP transition
            allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
            allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104)
            // It's often good to set specific ciphers for WEP:
            // allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP) // if using TKIP with WEP
            // allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP) // if using CCMP with WEP (less common)
        }
        connect(context , wifiConfiguration)
    }

    private fun connect(context : Context , wifiConfiguration : WifiConfiguration) {
        context.wifiManager?.apply {
            @Suppress("MissingPermission") // Ensure you have CHANGE_WIFI_STATE and ACCESS_WIFI_STATE
            val id = addNetwork(wifiConfiguration)
            if (id != - 1) {
                disconnect()
                enableNetwork(id , true)
                reconnect()
            }
            else {
                // Handle error: network could not be added
                throw Exception("Failed to add Wi-Fi network configuration.")
            }
        } ?: throw Exception("WifiManager not available.")
    }

    private fun String.quote() : String {
        return if (startsWith("\"") && endsWith("\"")) this else "\"$this\""
    }

    private fun String.quoteIfNotHex() : String {
        return if (isHex()) {
            this
        }
        else {
            quote()
        }
    }

    private fun String.isHex() : Boolean {
        // WEP keys can be 10, 26, or 58 hex digits (for 40, 104, 256 bit WEP respectively)
        // or 5 or 13 ASCII characters.
        // This check is for a generic 64-character hex string, which might be for WPA PSK.
        // Adjust if needed for WEP hex key validation.
        return length == 64 && matches(hexRegex)
    }

    private fun String.toEapMethod() : Int? {
        return when (this.uppercase()) { // Use uppercase for case-insensitive matching
            "AKA" -> WifiEnterpriseConfig.Eap.AKA
            "AKA_PRIME" -> requireApiLevel(Build.VERSION_CODES.M) { WifiEnterpriseConfig.Eap.AKA_PRIME }
            "NONE" -> WifiEnterpriseConfig.Eap.NONE // Note: EAP.NONE might not be a valid connectable method.
            "PEAP" -> WifiEnterpriseConfig.Eap.PEAP
            "PWD" -> WifiEnterpriseConfig.Eap.PWD
            "SIM" -> WifiEnterpriseConfig.Eap.SIM
            "TLS" -> WifiEnterpriseConfig.Eap.TLS
            "TTLS" -> WifiEnterpriseConfig.Eap.TTLS
            "UNAUTH_TLS" -> requireApiLevel(Build.VERSION_CODES.N) { WifiEnterpriseConfig.Eap.UNAUTH_TLS }
            else -> null
        }
    }

    private fun String.toPhase2Method() : Int? {
        return when (this.uppercase()) { // Use uppercase for case-insensitive matching
            "AKA" -> requireApiLevel(Build.VERSION_CODES.O) { WifiEnterpriseConfig.Phase2.AKA }
            "AKA_PRIME" -> requireApiLevel(Build.VERSION_CODES.O) { WifiEnterpriseConfig.Phase2.AKA_PRIME }
            "GTC" -> WifiEnterpriseConfig.Phase2.GTC
            "MSCHAP" -> WifiEnterpriseConfig.Phase2.MSCHAP
            "MSCHAPV2" -> WifiEnterpriseConfig.Phase2.MSCHAPV2
            "NONE" -> WifiEnterpriseConfig.Phase2.NONE
            "PAP" -> WifiEnterpriseConfig.Phase2.PAP
            "SIM" -> requireApiLevel(Build.VERSION_CODES.O) { WifiEnterpriseConfig.Phase2.SIM }
            else -> WifiEnterpriseConfig.Phase2.NONE // Defaulting to NONE, consider if this is appropriate
        }
    }

    private inline fun <T> requireApiLevel(version : Int , block : () -> T) : T? {
        return if (Build.VERSION.SDK_INT >= version) block() else null
    }
}