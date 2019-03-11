package com.example.tutorial3_1gatt_connect;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import com.example.tutorial3_1gatt_connect.adapter.DeviceListAdapter;
import com.example.tutorial3_1gatt_connect.model.PeripheralDeviceItem;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.example.tutorial3_1gatt_connect.constant.Constants.TIME_SERVICE;
import static com.example.tutorial3_1gatt_connect.constant.Constants.nordicUART;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public abstract class BluetoothScanActivity extends BluetoothLEActivity {


    // Stops scanning after 100 seconds.
    private static final long SCAN_PERIOD = 100_000;

    protected boolean mScanning = false;
    private Handler mHandler;



    private BluetoothLeScanner bluetoothLeScanner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();

        // Request for location permission
        if (!isLocationPermissionGranted()) {
            requestLocationPermission();
        }

        bluetoothLeScanner = getBluetoothAdapter().getBluetoothLeScanner();

    }


    /**
     * Advanced scanning method for Android 21+
     *
     * @param enable
     */
    public void scanBTDevice(boolean enable) {
        if (enable) {

            mScanning = true;

            startBTScan();

            stopBTDeviceScanAfterAPeriod();

        } else {
            mScanning = false;
            bluetoothLeScanner.stopScan(mScanCallback);
        }

        invalidateOptionsMenu();

    }

    public void startBTScan() {

        List<ScanFilter> scanFilters = new ArrayList<>();


        // Test Time Filter
        ScanFilter filterTimeService = new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(TIME_SERVICE))
                .build();


        // Estimote Filters
//        ScanFilter filterEstimotePurple1 = new ScanFilter.Builder()
//                .setServiceUuid(new ParcelUuid(ESTIMOTE))
//                .build();

        // Filters for ETS
        UUID uuidTicket1 = UUID.fromString("5A412301-A4A1-E192-F1A6-E55A23DAAA6C");
        UUID uuidTicket2 = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");


        ScanFilter filter1 = new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(uuidTicket1))
                .build();

        ScanFilter filter2 = new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(uuidTicket2))
                .build();

        ScanFilter nordicService = new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(nordicUART))
                .build();

        // Ticket Filter1

        ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .build();

        bluetoothLeScanner.startScan(scanFilters, scanSettings, mScanCallback);
    }

    protected void stopBTDeviceScanAfterAPeriod() {
        // Stops scanning after a pre-defined scan period.
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScanning = false;
                bluetoothLeScanner.stopScan(mScanCallback);
                invalidateOptionsMenu();
            }
        }, SCAN_PERIOD);
    }

    /**
     * ScanCallback for advanced BT LE scan. It requires Android 21+
     */
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            onBLEScanResult(callbackType, result);

        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            System.out.println("ScanCallback onBLEBatchScanResults() results: " + results);
            onBLEBatchScanResults(results);

        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            System.out.println("ScanCallback onBLEScanFailed() errorCode: " + errorCode);
            onBLEScanFailed(errorCode);

        }
    };

    public abstract void onBLEScanResult(int callbackType, ScanResult result);

    public abstract void onBLEBatchScanResults(List<ScanResult> results);

    public abstract void onBLEScanFailed(int errorCode);

}
