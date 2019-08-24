package com.example.tutorial3_1gatt_connect.model;

import android.bluetooth.BluetoothDevice;

import java.text.DecimalFormat;

public class PeripheralDeviceItem {

    public BluetoothDevice bluetoothDevice;

    public int txPower = 0;

    public int rssi;

    public PeripheralDeviceItem() {

    }

    public PeripheralDeviceItem(BluetoothDevice bluetoothDevice, int rssi, int txPower) {
        this.bluetoothDevice = bluetoothDevice;
        this.rssi = rssi;
        this.txPower = txPower;
    }


    public double getCalculatedAccuracy() {
        return calculateAccuracy(txPower, rssi);
    }

    protected double calculateAccuracy(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }

        double ratio = rssi * 1.0 / txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio, 10);
        } else {
            return (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
        }
    }

}
