package com.stfactory.tutorial1_2bluetoothlescanner.model;

import android.bluetooth.BluetoothDevice;

public class CustomBluetoothDevice {


    public BluetoothDevice bluetoothDevice;
    public String rssi;

    public CustomBluetoothDevice(BluetoothDevice bluetoothDevice, String rssi) {
        this.bluetoothDevice = bluetoothDevice;
        this.rssi = rssi;
    }

}
