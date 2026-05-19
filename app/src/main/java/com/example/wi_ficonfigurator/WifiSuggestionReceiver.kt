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
        val wifiManager = context.getSystemService<WifiManager>()!!

        if (intent.action == ACTION_CLEAR_WIFI) {
            val status = wifiManager.removeNetworkSuggestions(emptyList())
            Log.i(TAG, "removeNetworkSuggestions(all) status=${statusName(status)}")
            return
        }

        val ssid = intent.getStringExtra("ssid")
        val password = intent.getStringExtra("password")
        val hidden = intent.getBooleanExtra("hidden", false)

        if (ssid.isNullOrEmpty()) {
            Log.w(TAG, "Missing 'ssid' extra")
            return
        }

        fun base() = WifiNetworkSuggestion.Builder()
            .setSsid(ssid)
            .setIsHiddenSsid(hidden)
        val suggestions = if (password.isNullOrEmpty()) {
            listOf(
                base().build(),
                base().setIsEnhancedOpen(true).build(),
            )
        } else {
            listOf(
                base().setWpa2Passphrase(password).build(),
                base().setWpa3Passphrase(password).build(),
            )
        }

        wifiManager.removeNetworkSuggestions(suggestions)
        val status = wifiManager.addNetworkSuggestions(suggestions)
        Log.i(TAG, "addNetworkSuggestions ssid='$ssid' hidden=$hidden status=${statusName(status)}")
    }

    private fun statusName(status: Int): String = when (status) {
        WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS -> "SUCCESS"
        WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_INTERNAL -> "ERROR_INTERNAL"
        WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_APP_DISALLOWED -> "ERROR_APP_DISALLOWED"
        WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_ADD_DUPLICATE -> "ERROR_ADD_DUPLICATE"
        WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_ADD_EXCEEDS_MAX_PER_APP -> "ERROR_ADD_EXCEEDS_MAX_PER_APP"
        WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_ADD_INVALID -> "ERROR_ADD_INVALID"
        WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_ADD_NOT_ALLOWED -> "ERROR_ADD_NOT_ALLOWED"
        else -> "UNKNOWN($status)"
    }

    companion object {
        private const val TAG = "WifiSuggestionReceiver"
        private const val ACTION_CLEAR_WIFI = "com.example.wi_ficonfigurator.action.CLEAR_WIFI"
    }
}
