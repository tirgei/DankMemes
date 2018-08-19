package com.gelostech.dankmemes.utils;

import android.content.Context
import android.telephony.TelephonyManager
import android.net.ConnectivityManager
import android.net.NetworkInfo

object Connectivity {

    /**
     * Get the network info
     * @param context
     * @return
     */
    private fun getNetworkInfo(context: Context): NetworkInfo? {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo
    }

    /**
     * Check if there is any connectivity
     * @param context
     * @return
     */
    fun isConnected(context: Context): Boolean {
        val info = getNetworkInfo(context)
        return info != null && info.isConnected
    }

    /**
     * Check if there is any connectivity to a Wifi network
     * @param context
     * @param type
     * @return
     */
    fun isConnectedWifi(context: Context): Boolean {
        val info = getNetworkInfo(context)
        return info != null && info.isConnected && info.type == ConnectivityManager.TYPE_WIFI
    }

    /**
     * Check if there is any connectivity to a mobile network
     * @param context
     * @param type
     * @return
     */
    fun isConnectedMobile(context: Context): Boolean {
        val info = getNetworkInfo(context)
        return info != null && info.isConnected && info.type == ConnectivityManager.TYPE_MOBILE
    }

    /**
     * Check if there is fast connectivity
     * @param context
     * @return
     */
    fun isConnectedFast(context: Context): Boolean {
        val info = getNetworkInfo(context)
        return info != null && info.isConnected && isConnectionFast(info.type, info.subtype)
    }

    /**
     * Check if the connection is fast
     * @param type
     * @param subType
     * @return
     */
    private fun isConnectionFast(type: Int, subType: Int): Boolean =
            when (type) {
                ConnectivityManager.TYPE_WIFI -> true
                ConnectivityManager.TYPE_MOBILE -> when (subType) {
                    TelephonyManager.NETWORK_TYPE_1xRTT -> false // ~ 50-100 kbps
                    TelephonyManager.NETWORK_TYPE_CDMA -> false // ~ 14-64 kbps
                    TelephonyManager.NETWORK_TYPE_EDGE -> false // ~ 50-100 kbps
                    TelephonyManager.NETWORK_TYPE_EVDO_0 -> true // ~ 400-1000 kbps
                    TelephonyManager.NETWORK_TYPE_EVDO_A -> true // ~ 600-1400 kbps
                    TelephonyManager.NETWORK_TYPE_GPRS -> false // ~ 100 kbps
                    TelephonyManager.NETWORK_TYPE_HSDPA -> true // ~ 2-14 Mbps
                    TelephonyManager.NETWORK_TYPE_HSPA -> true // ~ 700-1700 kbps
                    TelephonyManager.NETWORK_TYPE_HSUPA -> true // ~ 1-23 Mbps
                    TelephonyManager.NETWORK_TYPE_UMTS -> true // ~ 400-7000 kbps

                    TelephonyManager.NETWORK_TYPE_EHRPD // API level 11
                    -> true // ~ 1-2 Mbps
                    TelephonyManager.NETWORK_TYPE_EVDO_B // API level 9
                    -> true // ~ 5 Mbps
                    TelephonyManager.NETWORK_TYPE_HSPAP // API level 13
                    -> true // ~ 10-20 Mbps
                    TelephonyManager.NETWORK_TYPE_IDEN // API level 8
                    -> false // ~25 kbps
                    TelephonyManager.NETWORK_TYPE_LTE // API level 11
                    -> true // ~ 10+ Mbps

                    TelephonyManager.NETWORK_TYPE_UNKNOWN -> false
                    else -> false
                }
                else -> false
            }

}