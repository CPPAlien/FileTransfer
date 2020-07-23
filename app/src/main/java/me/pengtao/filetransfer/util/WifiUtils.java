package me.pengtao.filetransfer.util;

import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @author chris
 */
public class WifiUtils {
    private static final String TAG = "WifiUtils";

    public static String getDeviceIpAddress() {
        String deviceIpAddress = "###.###.###.###";

        try {
            for (Enumeration<NetworkInterface> enumeration =
                 NetworkInterface.getNetworkInterfaces(); enumeration.hasMoreElements(); ) {
                NetworkInterface networkInterface = enumeration.nextElement();

                for (Enumeration<InetAddress> enumerationIpAddr =
                     networkInterface.getInetAddresses(); enumerationIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumerationIpAddr.nextElement();

                    if (!inetAddress.isLoopbackAddress() && inetAddress.getAddress().length == 4) {
                        deviceIpAddress = inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            Log.e(TAG, "SocketException:" + e.getMessage());
        }

        return deviceIpAddress;
    }
}
