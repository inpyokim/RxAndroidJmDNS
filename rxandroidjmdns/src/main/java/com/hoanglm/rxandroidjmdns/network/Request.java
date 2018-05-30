package com.hoanglm.rxandroidjmdns.network;

public class Request {
    private String ipAddress;
    private int port;
    private String data;

    public Request(String ipAddress,
                   int port,
                   String data) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.data = data;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }

    public String getData() {
        return data;
    }

    @Override
    public String toString() {
        return "Request = { " +
                "ipAddress: " + ipAddress +
                "port: " + port +
                "data: " + data +
                " }";
    }
}
