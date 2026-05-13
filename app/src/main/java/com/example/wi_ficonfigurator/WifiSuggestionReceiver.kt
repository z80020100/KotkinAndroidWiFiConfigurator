package com.example.wi_ficonfigurator

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.util.Log
import androidx.core.content.getSystemService

class WifiSuggestionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val ssid = intent.getStringExtra("ssid")
        val password = intent.getStringExtra("password")
        val hidden = intent.getBooleanExtra("hidden", false)

        if (ssid.isNullOrEmpty()) {
            Log.w(TAG, "Missing 'ssid' extra")
            return
        }

        val suggestion = WifiNetworkSuggestion.Builder()
            .setSsid(ssid)
            .setIsHiddenSsid(hidden)
            .apply { if (!password.isNullOrEmpty()) setWpa2Passphrase(password) }
            .build()

        val wifiManager = context.getSystemService<WifiManager>()!!

        val suggestions = listOf(suggestion)
        wifiManager.removeNetworkSuggestions(suggestions)
        val status = wifiManager.addNetworkSuggestions(suggestions)

        val statusName = when (status) {
            WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS -> "SUCCESS"
            WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_INTERNAL -> "ERROR_INTERNAL"
            WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_APP_DISALLOWED -> "ERROR_APP_DISALLOWED"
            WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_ADD_DUPLICATE -> "ERROR_ADD_DUPLICATE"
            WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_ADD_EXCEEDS_MAX_PER_APP -> "ERROR_ADD_EXCEEDS_MAX_PER_APP"
            WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_ADD_INVALID -> "ERROR_ADD_INVALID"
            WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_ADD_NOT_ALLOWED -> "ERROR_ADD_NOT_ALLOWED"
            else -> "UNKNOWN($status)"
        }
        Log.i(TAG, "addNetworkSuggestions ssid='$ssid' hidden=$hidden status=$statusName")
    }

    companion object {
        private const val TAG = "WifiSuggestionReceiver"
    }
}
