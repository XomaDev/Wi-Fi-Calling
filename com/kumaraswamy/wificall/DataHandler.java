package com.kumaraswamy.wificall;

import java.io.*;
import java.net.Socket;

public class DataHandler extends Thread {

    private final Socket socket;

    private InputStream inputStream = null;
    private OutputStream outputStream;
    private final WIFIActivity wifiActivity;
    private FileOutputStream fileOutputStream;

    public DataHandler(Socket socket, WIFIActivity wifiActivity) {
        this.socket = socket;
        this.wifiActivity = wifiActivity;
        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (socket != null && inputStream != null && WiFiCalling.condition) {
                byte[] data = new byte[inputStream.available()];

                if(inputStream.read(data) > 0) {
                    final String message = new String(data);
                    WiFiCalling.activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            wifiActivity.newMessage.NewMessage(message);
                        }
                    });
                }
            }

            fileOutputStream = new FileOutputStream(WiFiCalling.path);
            assert socket != null;
            byte[] buffer = new byte[socket.getReceiveBufferSize()];
            int read;
            while((read = inputStream.read(buffer)) != -1 && !WiFiCalling.condition) {
                fileOutputStream.write(buffer, 0, read);
            }

            WIFIActivity.dataHandler = new DataHandler(socket, wifiActivity);
            WIFIActivity.dataHandler.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeBytes(final byte[] data) {
        if(inputStream != null) {
            try {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(data);

                int read;
                while((read = inputStream.read(data)) != -1){
                    outputStream.write(data, 0, read);
                }

                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void close() {
        try {
            if(inputStream != null) inputStream.close();
            if(outputStream != null) outputStream.close();
            if(socket != null) socket.close();
            if(fileOutputStream != null) fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
