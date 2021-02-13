package com.kumaraswamy.wificall;

import android.app.Activity;

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.kumaraswamy.wificall.listeners.ActivityListeners;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@UsesPermissions(permissionNames = "android.permission.ACCESS_WIFI_STATE, android.permission.CHANGE_WIFI_STATE, android.permission.INTERNET, android.permission.ACCESS_NETWORK_STATE, android.permission.ACCESS_FINE_LOCATION, android.permission.ACCESS_COARSE_LOCATION, android.permission.WRITE_EXTERNAL_STORAGE, android.permission.READ_EXTERNAL_STORAGE")
@DesignerComponent(version = 1,
        category = ComponentCategory.EXTENSION,
        description = "",
        nonVisible = true,
        iconName = "")

@SimpleObject(external = true)

public class WiFiCalling extends AndroidNonvisibleComponent implements ActivityListeners.DeviceListUpdateListener,
        ActivityListeners.DiscoveryStartedListener, ActivityListeners.DiscoveryFailedListener,
        ActivityListeners.ConnectionListener, ActivityListeners.ReceivedNewMessageListener {

    public static Activity activity;
    public static WIFIActivity wifiActivity;

    public WiFiCalling(ComponentContainer container) {
        super(container.$form());

        activity = container.$context();
        wifiActivity = new WIFIActivity(activity, this, this, this, this, this);
    }


    @SimpleFunction(description = "Search for devices nearby")
    public void StartDiscovery() {
        if(wifiActivity != null) wifiActivity.startDeviceDiscovery();
    }

    @SimpleFunction(description = "Stop searching for devices")
    public void StopDiscovery() {
        if(wifiActivity != null) wifiActivity.stopDiscovery();
    }

    @SimpleFunction
    public int BytesAvailableToReceive() {
        return wifiActivity == null ? 0 : wifiActivity.bytesAvailableToReceive();
    }

    @SimpleFunction(description = "Read text from the given number of bytes")
    public void ReadText(int bytes) {
        if(wifiActivity != null) wifiActivity.readText(bytes);
    }

    @SimpleFunction(description = "Connect to device using the device address")
    public void Connect(int index) {
        wifiActivity.connectDevice(index - 1);
    }

    @SimpleFunction(description = "Start message to the connected device")
    public void SendMessage(String message) {
        if(wifiActivity != null) wifiActivity.sendBytes(message.getBytes());
    }

// DO NOTE USE THIS

    @SimpleFunction(description = "Send file to the connected device")
    public void SendFile(String fileName) {
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(fileName);
            byte[] data = new byte[inputStream.available()];
            if(inputStream.read(data) > 0) {
                wifiActivity.sendBytes(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SimpleEvent(description = "Event fired when device list updated")
    public void DevicesAvailable(List names, List addresses) {
        EventDispatcher.dispatchEvent(this, "DevicesAvailable", names, addresses);
    }

    @SimpleEvent(description = "Event fired when discovery started")
    public void StartedDiscovery() {
        EventDispatcher.dispatchEvent(this, "StartedDiscovery");
    }

    @SimpleEvent(description = "Event fired when discovery failed")
    public void StartFailed(int errorCode) {
        EventDispatcher.dispatchEvent(this, "StartFailed", errorCode);
    }

    @SimpleEvent(description = "Event fired when connection is made")
    public void Connected(boolean isHost) {
        EventDispatcher.dispatchEvent(this, "Connected", isHost);
    }

    @SimpleEvent(description = "Event fired when received new message")
    public void MessageReceived(String message) {
        EventDispatcher.dispatchEvent(this, "MessageReceived", message);
    }

    @Override
    public void DeviceListUpdated(List<String> deviceNames, List<String> deviceAddresses) {
        DevicesAvailable(deviceNames, deviceAddresses);
    }

    @Override
    public void DiscoveryStarted() {
        StartedDiscovery();
    }

    @Override
    public void DiscoveryFailed(int errorCode) {
        StartFailed(errorCode);
    }

    @Override
    public void DeviceConnected(boolean isHost) {
        Connected(isHost);
    }

    @Override
    public void NewMessage(String message) {
        MessageReceived(message);
    }
}
