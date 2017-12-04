package com.hoanglm.rxandroidjmdns.network;

import android.content.Context;
import android.net.wifi.WifiManager;

import com.hoanglm.rxandroidjmdns.utils.RxJmDNSLog;
import com.hoanglm.rxandroidjmdns.utils.SetupServiceException;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class AndroidWiFiTCPServer extends TCPServer {
    private static final String NOT_DETECTED_IP = "0:0:0:0";

    public AndroidWiFiTCPServer(InetAddress address) throws IOException {
        super(address);
    }

    public static AndroidWiFiTCPServer build(Context androidContext) throws IOException {
        /**
         * We need to know our identity inside the local WiFi network.
         */
        WifiManager wifi = (android.net.wifi.WifiManager)
                androidContext.getSystemService(android.content.Context.WIFI_SERVICE);

        // Get the IP the server will be bound to.
        InetAddress deviceIpAddress = InetAddress.getByAddress(
                ByteBuffer.allocate(4).putInt(
                        Integer.reverseBytes(wifi.getConnectionInfo().getIpAddress())).array());

        if (deviceIpAddress == null)
            throw new SetupServiceException(SetupServiceException.Reason.NOT_DETECTED_IP, "No IP address can be found");
        RxJmDNSLog.i("My address is " + deviceIpAddress.getHostAddress());
        if (NOT_DETECTED_IP.equalsIgnoreCase(deviceIpAddress.getHostAddress())) {
            throw new SetupServiceException(SetupServiceException.Reason.NOT_DETECTED_IP, "Not detect IP address " + NOT_DETECTED_IP);
        }
        // Start the server
        return new AndroidWiFiTCPServer(deviceIpAddress);
    }
}
