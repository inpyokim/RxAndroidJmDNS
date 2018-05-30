package com.hoanglm.rxandroidjmdns.network;

import com.hoanglm.rxandroidjmdns.utils.RxJmDNSLog;
import com.jakewharton.rxrelay.PublishRelay;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Code for a simple TCP "echo"-like server.
 */
public class TCPServer {
    private ServerSocket mServerSocket;
    private Socket mClientSocket;
    private volatile boolean mIsAlive;
    private final PublishRelay<Request> mTcpServerRequestRelay;

    /**
     * @return local port on which the server runs.
     */
    public int listenPort() {
        if (mServerSocket != null) {
            return mServerSocket.getLocalPort();
        } else {
            throw new IllegalStateException("ServerSocket is null, probably not closed or not initialized");
        }
    }

    /**
     * @return INetAddr of the server.
     */
    public InetAddress listenAddress() {
        if (mServerSocket != null) {
            return mServerSocket.getInetAddress();
        } else {
            throw new IllegalStateException("ServerSocket is null, probably not closed or not initialized");
        }
    }

    /**
     * Constructor that starts the server and binds it to the provided address.
     *
     * @param bindAddress Server IP address.
     * @throws IOException
     */
    public TCPServer(final InetAddress bindAddress, PublishRelay<Request> tcpServerRequestRelay) throws IOException {
        // We are not using a predefined port, it will be provided by the system and then advertised to other peers
        mServerSocket = new ServerSocket(0, 10, bindAddress);
        mIsAlive = true;
        mTcpServerRequestRelay = tcpServerRequestRelay;
        // Requests will be served from a standalone thread
        new Thread(new Runnable() {
            public void run() {
                try {
                    // As long as the serve was not killed, loop and accept requests
                    while (mIsAlive) {
                        // Wait for a connection
                        mClientSocket = mServerSocket.accept();
                        RxJmDNSLog.d("Request received from: " + mClientSocket.getRemoteSocketAddress().toString());
                        //Service the connection
                        serve(mClientSocket);
                    }
                    // When killed, release resources.
                    mServerSocket.close();
                } catch (IOException ioe) {
                    RxJmDNSLog.e(ioe, "Error in TCPServer");
                }
            }
        }).start();
    }

    /**
     * Reads a string (UTF) request and responds to it.
     *
     * @param client socket the request is originated
     * @throws IOException
     */
    public void serve(Socket client) throws IOException {
        DataInputStream inbound;
        DataOutputStream outbound;
        String request = "";
        String response;
        try {
            // Acquire the streams for IO
            inbound = new DataInputStream(client.getInputStream());
            outbound = new DataOutputStream(client.getOutputStream());

            request = inbound.readUTF();
            Request requestData = new Request(client.getInetAddress().getHostAddress(), client.getPort(), request);
            mTcpServerRequestRelay.call(requestData);
            RxJmDNSLog.d("Incoming request = %s", requestData.toString());
            client.shutdownInput();

            response = buildResponse(request);
            outbound.writeUTF(response);
            client.shutdownOutput();
        } finally {
            client.close();
            RxJmDNSLog.d("Sent response " + String.valueOf(request));
        }
    }

    /**
     * This method should be overwritten by more complicated server implementations.
     * <p>
     * It dictates what string response is sent back to a client, based on client's request.
     *
     * @param request Client's request
     * @return String representation of the response.
     */
    protected String buildResponse(String request) {
        return request;
    }

    /**
     * Gracefully signals the server to halt the loop.
     */
    public void kill() {
        mIsAlive = false;
    }
}
