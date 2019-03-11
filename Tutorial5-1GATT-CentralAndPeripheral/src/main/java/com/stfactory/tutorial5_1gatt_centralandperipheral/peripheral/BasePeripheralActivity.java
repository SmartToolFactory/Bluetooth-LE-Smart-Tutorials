package com.stfactory.tutorial5_1gatt_centralandperipheral.peripheral;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.stfactory.tutorial5_1gatt_centralandperipheral.Constants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BasePeripheralActivity extends BluetoothLEActivity {

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private BluetoothGattServer mBluetoothGattServer;

    private boolean mAdvertising = false;

    private Map<Integer, BluetoothDevice> connectedDevices = new HashMap<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBluetoothManager = getBluetoothManager();
        mBluetoothAdapter = getBluetoothAdapter();
        mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();

    }

    public void startGATTServer(BluetoothGattServerCallback callback) {
        mBluetoothGattServer = mBluetoothManager.openGattServer(this, callback);
    }


    public void startGATTServer(BluetoothGattServerCallback callback, List<BluetoothGattService> serviceList) {

        mBluetoothGattServer = mBluetoothManager.openGattServer(this, callback);

        if (serviceList != null && serviceList.size() > 0) {

            for (BluetoothGattService service : serviceList) {
                if (!mBluetoothGattServer.getServices().contains(service)) {
                    mBluetoothGattServer.addService(service);
                }
            }
        }
    }

    public void addGATTService(BluetoothGattService bluetoothGattService) {
        List<BluetoothGattService> serviceList = mBluetoothGattServer.getServices();

        if (!serviceList.contains(bluetoothGattService)) {
            serviceList.add(bluetoothGattService);
        }
    }


    public void stopServer() {
        if (mBluetoothGattServer != null) {
            mBluetoothGattServer.close();
        }
    }


    public void startAdvertising() {

        if (mBluetoothLeAdvertiser == null) {
            return;
        }

        Toast.makeText(this, "startAdvertising()", Toast.LENGTH_SHORT).show();

        // Advertising Settings
        AdvertiseSettings advertiseSettings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
                .build();

        ParcelUuid parcelUuid = new ParcelUuid(Constants.TIME_SERVICE);

        // Data to be advertised
        AdvertiseData advertiseData = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .addServiceUuid(parcelUuid)
                .build();

        // Start advertising
        mBluetoothLeAdvertiser.startAdvertising(advertiseSettings, advertiseData, mAdvertiseCallback);

    }

    public void startAdvertising(AdvertiseSettings advertiseSettings, AdvertiseData advertiseData) {

        Toast.makeText(this, "startAdvertising()", Toast.LENGTH_SHORT).show();

        mBluetoothLeAdvertiser.startAdvertising(advertiseSettings, advertiseData, mAdvertiseCallback);

    }

    public void stopAdvertising() {
        setAdvertising(false);

        if (mBluetoothLeAdvertiser != null) {
            mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
            mAdvertising = false;
            invalidateOptionsMenu();
        }
    }

    public BluetoothGattServer getBluetoothGattServer() {
        return mBluetoothGattServer;
    }

    public boolean isAdvertising() {
        return mAdvertising;
    }

    public void setAdvertising(boolean advertising) {
        mAdvertising = advertising;
    }

    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            setAdvertising(true);
            onAdvertisingStartSuccess(settingsInEffect);

        }

        @Override
        public void onStartFailure(int errorCode) {
            setAdvertising(false);
            onAdvertisingStartFailure(errorCode);
        }
    };

    protected void onPause() {
        super.onPause();
        stopAdvertising();
        stopServer();
    }


    protected abstract void onAdvertisingStartSuccess(AdvertiseSettings settingsInEffect);

    protected abstract void onAdvertisingStartFailure(int errorCode);

}
