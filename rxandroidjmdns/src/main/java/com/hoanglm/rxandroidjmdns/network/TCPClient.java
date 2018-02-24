package com.hoanglm.rxandroidjmdns.network;

import com.hoanglm.rxandroidjmdns.utils.RxJmDNSLog;
import com.hoanglm.rxandroidjmdns.utils.StringUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Code for sending a string request to a serve via a TCP client socket and
 * receiving its response.
 */
public class TCPClient {

    /**
     * Sends a string via a TCP socket (in UTF format), waits for a response and returns it (also UTF string).
     *
     * @param str         Request
     * @param destination IP address
     * @param port        Port
     * @return Response from the other host.
     * @throws IOException
     */
    public static String sendTo(String str, InetAddress destination, int port) throws IOException {
        Socket socket = null;
        DataOutputStream writer;
        DataInputStream reader;

        try {
            RxJmDNSLog.d("startService send request: %s", str);
            socket = new Socket(destination, port);
            writer = new DataOutputStream(socket.getOutputStream());
            writer.writeUTF(str);
            // Close the output stream to signal there is no more data to be send.
            socket.shutdownOutput();
            // Read response.
            reader = new DataInputStream(socket.getInputStream());
            return reader.readUTF();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    /**
     * Convenience wrapper method, it builds an INetAddr form a string host name.
     *
     * @param str
     * @param host
     * @param port
     * @return
     * @throws IOException
     */
    public static String sendTo(String str, String host, int port) throws IOException {
        return sendTo(str, InetAddress.getByName(host), port);
    }

    public static String sendTo(byte[] data, String host, int port) throws IOException {
        return sendTo(StringUtil.convertByteToString(data), InetAddress.getByName(host), port);
    }
}
