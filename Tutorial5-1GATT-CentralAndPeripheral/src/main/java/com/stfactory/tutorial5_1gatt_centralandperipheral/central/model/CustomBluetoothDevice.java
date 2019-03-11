package com.stfactory.tutorial5_1gatt_centralandperipheral.central.model;

import android.bluetooth.BluetoothDevice;

public class CustomBluetoothDevice {


    public BluetoothDevice bluetoothDevice;

    public String getRssi() {
        return rssi;
    }

    public void setRssi(String rssi) {
        this.rssi = rssi;
    }

    public String rssi;

    public CustomBluetoothDevice(BluetoothDevice bluetoothDevice, String rssi) {
        this.bluetoothDevice = bluetoothDevice;
        this.rssi = rssi;
    }

}
