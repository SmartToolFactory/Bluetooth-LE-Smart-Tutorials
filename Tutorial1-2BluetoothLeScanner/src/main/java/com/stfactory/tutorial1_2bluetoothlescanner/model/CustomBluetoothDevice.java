package com.stfactory.tutorial1_2bluetoothlescanner.model;

import android.bluetooth.BluetoothDevice;

public class CustomBluetoothDevice {


    public BluetoothDevice bluetoothDevice;
    public int rssi;

    public CustomBluetoothDevice(BluetoothDevice bluetoothDevice, int rssi) {
        this.bluetoothDevice = bluetoothDevice;
        this.rssi = rssi;
    }

}
