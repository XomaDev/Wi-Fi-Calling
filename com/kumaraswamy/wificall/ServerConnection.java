package com.kumaraswamy.wificall;

import java.io.IOException;
import java.lang.Thread;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerConnection extends Thread {
    private final WIFIActivity wifiActivity;

    public ServerConnection(WIFIActivity wifiActivity) {
        this.wifiActivity = wifiActivity;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(8888);
            Socket socket = serverSocket.accept();

            WIFIActivity.dataHandler = new DataHandler(socket, wifiActivity);
            WIFIActivity.dataHandler.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
