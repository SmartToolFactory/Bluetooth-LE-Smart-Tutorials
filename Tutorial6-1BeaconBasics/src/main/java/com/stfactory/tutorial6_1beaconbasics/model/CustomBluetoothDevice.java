package com.stfactory.tutorial6_1beaconbasics.model;

import android.bluetooth.BluetoothDevice;

public class CustomBluetoothDevice {


    public BluetoothDevice bluetoothDevice;
    public String rssi;

    public CustomBluetoothDevice(BluetoothDevice bluetoothDevice, String rssi) {
        this.bluetoothDevice = bluetoothDevice;
        this.rssi = rssi;
    }

}
