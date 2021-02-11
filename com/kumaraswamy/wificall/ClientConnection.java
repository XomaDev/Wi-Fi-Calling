package com.kumaraswamy.wificall;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientConnection extends Thread {
    private final Socket socket;
    private final String hostAddress;
    private final WIFIActivity wifiMaster;

    public ClientConnection(InetAddress inetAddress, WIFIActivity wifiActivity) {
        hostAddress = inetAddress.getHostAddress();
        socket = new Socket();
        this.wifiMaster = wifiActivity;
    }

    @Override
    public void run() {
        try {
            socket.connect(new InetSocketAddress(hostAddress, 8888), 700);

            WIFIActivity.dataHandler = new DataHandler(socket, wifiMaster);
            WIFIActivity.dataHandler.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
