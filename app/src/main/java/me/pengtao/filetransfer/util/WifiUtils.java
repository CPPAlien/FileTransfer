package me.pengtao.filetransfer.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;

public class WifiUtils {
    public static String getWifiIp(@NonNull Context context) {
        WifiManager wifimanager = (WifiManager) context.getApplicationContext().getSystemService
                (Context.WIFI_SERVICE);
        if (wifimanager == null) {
            return null;
        }
        WifiInfo wifiInfo = wifimanager.getConnectionInfo();
        if (wifiInfo != null) {
            return intToIp(wifiInfo.getIpAddress());
        }
        return null;
    }

    private static String intToIp(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 24)
                & 0xFF);
    }

    public static NetworkInfo.State getWifiConnectState(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context
                .CONNECTIVITY_SERVICE);
        if (manager == null) {
            return NetworkInfo.State.DISCONNECTED;
        }
        NetworkInfo mWiFiNetworkInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (mWiFiNetworkInfo != null) {
            return mWiFiNetworkInfo.getState();
        }
        return NetworkInfo.State.DISCONNECTED;
    }
}
