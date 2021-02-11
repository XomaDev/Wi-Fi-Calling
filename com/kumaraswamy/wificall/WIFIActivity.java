package com.kumaraswamy.wificall;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.*;
import android.widget.Toast;
import com.google.appinventor.components.runtime.util.AsynchUtil;
import com.kumaraswamy.wificall.listeners.ActivityListeners;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION;

public class WIFIActivity {
    public static Activity activity;

    private final WifiP2pManager wifiP2pManager;
    private final WifiP2pManager.Channel wifiP2pChannel;

    private final ActivityListeners.DiscoveryStartedListener discoveryStartedListener;
    private final ActivityListeners.DeviceListUpdateListener deviceListUpdateListener;
    private final ActivityListeners.DiscoveryFailedListener discoveryFailedListener;
    private final ActivityListeners.ConnectionListener connectionListener;

    private static final ArrayList<WifiP2pDevice> wifiP2pDevices = new ArrayList<>();
    private static final ArrayList<String> deviceNames = new ArrayList<>();
    private static final ArrayList<String> deviceAddress = new ArrayList<>();

    public static DataHandler dataHandler;

    private static int connectionType;

    public static final int CONNECTION_TYPE_HOST = 0;
    public static final int CONNECTION_TYPE_CLIENT = 1;
    public final ActivityListeners.ReceivedNewMessageListener newMessage;

    public WIFIActivity(Activity activity, ActivityListeners.DiscoveryStartedListener discoveryStartedListener,
                        ActivityListeners.DeviceListUpdateListener deviceListUpdateListener,
                        ActivityListeners.DiscoveryFailedListener discoveryFailedListener,
                        ActivityListeners.ConnectionListener connectionListener,
                        ActivityListeners.ReceivedNewMessageListener newMessage) {
        WIFIActivity.activity = activity;
        this.discoveryStartedListener = discoveryStartedListener;
        this.deviceListUpdateListener = deviceListUpdateListener;
        this.discoveryFailedListener = discoveryFailedListener;
        this.connectionListener = connectionListener;
        this.newMessage = newMessage;

        wifiP2pManager = (WifiP2pManager) activity.getSystemService(Context.WIFI_P2P_SERVICE);
        wifiP2pChannel = wifiP2pManager.initialize(activity, activity.getMainLooper(), null);

        WifiBroadcastReceiver wifiBroadcastReceiver = new WifiBroadcastReceiver(this, wifiP2pManager, wifiP2pChannel);

        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WIFI_P2P_CONNECTION_CHANGED_ACTION);

        activity.registerReceiver(wifiBroadcastReceiver, intentFilter);
    }

    public void startDeviceDiscovery() {
        if(wifiP2pManager != null) {
            wifiP2pManager.discoverPeers(wifiP2pChannel, discoveryListener);
        }
    }

    public void connectDevice(int index) {
        if(wifiP2pManager != null) {
            WifiP2pDevice wifiP2pDevice = wifiP2pDevices.get(index);

            WifiP2pConfig wifiP2pConfig = new WifiP2pConfig();
            wifiP2pConfig.deviceAddress = wifiP2pDevice.deviceAddress;

            wifiP2pManager.connect(wifiP2pChannel, wifiP2pConfig, connectionStatusListener);
        }
    }

    public void startStream(final byte[] data) {
       AsynchUtil.runAsynchronously(new Runnable() {
           @Override
           public void run() {
               dataHandler.writeBytes(data);
           }
       });
    }

    private void initializeControllers(InetAddress inetAddress) {
        if(connectionType == CONNECTION_TYPE_HOST) {
            ServerConnection serverConnection = new ServerConnection(this);
            serverConnection.start();
        } else {
            ClientConnection clientConnection = new ClientConnection(inetAddress, this);
            clientConnection.start();
        }
    }

    private final WifiP2pManager.ActionListener discoveryListener = new WifiP2pManager.ActionListener() {
        @Override
        public void onSuccess() {
            discoveryStartedListener.DiscoveryStarted();
        }

        @Override
        public void onFailure(int errorCode) {
            discoveryFailedListener.DiscoveryFailed(errorCode);
        }
    };

    public final WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
            Collection<WifiP2pDevice> devices = wifiP2pDeviceList.getDeviceList();

            if (!devices.equals(wifiP2pDevices)) {
                wifiP2pDevices.clear();
                deviceNames.clear();
                deviceAddress.clear();
                wifiP2pDevices.addAll(devices);

                for (WifiP2pDevice device : devices) {
                    deviceNames.add(device.deviceName);
                    deviceAddress.add(device.deviceAddress);
                }
                deviceListUpdateListener.DeviceListUpdated(deviceNames, deviceAddress);
            }
            if (devices.size() == 0) {
                deviceListUpdateListener.DeviceListUpdated(Collections.EMPTY_LIST, Collections.EMPTY_LIST);
            }
        }
    };

    private final WifiP2pManager.ActionListener connectionStatusListener = new WifiP2pManager.ActionListener() {
        @Override
        public void onSuccess() {
            Toast.makeText(activity, "Request sent", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailure(int errorCode) {
            Toast.makeText(activity, "Failed to connect, error code: " + errorCode, Toast.LENGTH_SHORT).show();
        }
    };

    public final WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            if(wifiP2pInfo.groupFormed) {
                connectionListener.DeviceConnected(wifiP2pInfo.isGroupOwner);

                connectionType = wifiP2pInfo.isGroupOwner ?
                        CONNECTION_TYPE_HOST :
                        CONNECTION_TYPE_CLIENT;

                initializeControllers(wifiP2pInfo.groupOwnerAddress);
            }
        }
    };
}
