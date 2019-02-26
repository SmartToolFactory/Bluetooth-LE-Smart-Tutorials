package com.stfactory.tutorial1_2bluetoothlescanner;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.stfactory.tutorial1_2bluetoothlescanner.adapter.DeviceListAdapter;
import com.stfactory.tutorial1_2bluetoothlescanner.broadcast.BluetoothStateBroadcastReceiver;
import com.stfactory.tutorial1_2bluetoothlescanner.model.CustomBluetoothDevice;

import java.util.ArrayList;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_LOCATION = 2;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;

    private RecyclerView recyclerView;
    private DeviceListAdapter mLeDeviceListAdapter;

    private List<BluetoothDevice> bluetoothDeviceList = new ArrayList<>();

    private List<CustomBluetoothDevice> customBluetoothDevices = new ArrayList<>();

    private BluetoothStateBroadcastReceiver bluetoothStateBroadcastReceiver = new BluetoothStateBroadcastReceiver();

    private BluetoothLeScanner bluetoothLeScanner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mLeDeviceListAdapter = new DeviceListAdapter(this, customBluetoothDevices);

        recyclerView.setAdapter(mLeDeviceListAdapter);

        mHandler = new Handler();

        requestLocationPermission();

        isBluetoothSupported();

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();


        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();


    }

    private void isBluetoothSupported() {
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void requestLocationPermission() {
        // Request for location permission
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_LOCATION);
    }


    @Override
    protected void onResume() {
        super.onResume();

        enableBluetooth();

        // Create Filters for Bluetooth states and actions
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

        // Register BroadcastReceiver for Bluetooth
        registerReceiver(bluetoothStateBroadcastReceiver, intentFilter);


    }

    private boolean isBluetoothEnabled() {
        return mBluetoothAdapter.isEnabled();
    }

    private void enableBluetooth() {
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        scanLeDevice(false);
        scanBTDevice(false);
        bluetoothDeviceList.clear();
        customBluetoothDevices.clear();
        mLeDeviceListAdapter.updateList(customBluetoothDevices);

        unregisterReceiver(bluetoothStateBroadcastReceiver);
    }


    private void scanLeDevice(final boolean enable) {

        if (enable) {

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);

            stopLeDeviceScanAfterAPeriod();

        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }


        invalidateOptionsMenu();
    }

    private void stopLeDeviceScanAfterAPeriod() {
        // Stops scanning after a pre-defined scan period.
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScanning = false;
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                invalidateOptionsMenu();
            }
        }, SCAN_PERIOD);
    }


    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {

                    System.out.println("DeviceScanActivity LeScanCallback onLeScan() device: " + device + ", thread: " + Thread.currentThread().getName());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (!bluetoothDeviceList.contains(device)) {
                                bluetoothDeviceList.add(device);

                                CustomBluetoothDevice customBluetoothDevice = new CustomBluetoothDevice(device, rssi);
                                customBluetoothDevices.add(customBluetoothDevice);
                                mLeDeviceListAdapter.updateList(customBluetoothDevices);
                            }
                        }
                    });
                }
            };


    /**
     * Advanced scanning method for Android 21+
     *
     * @param enable
     */
    private void scanBTDevice(boolean enable) {
        if (enable) {


            mScanning = true;

            starBTScan();

            stopBTDeviceScanAfterAPeriod();

        } else {
            mScanning = false;
            bluetoothLeScanner.stopScan(mScanCallback);
        }

        invalidateOptionsMenu();

    }

    private void starBTScan() {

        List<ScanFilter> scanFilters = new ArrayList<>();

        ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build();

        bluetoothLeScanner.startScan(scanFilters, scanSettings, mScanCallback);
    }

    private void stopBTDeviceScanAfterAPeriod() {
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

            System.out.println("ScanCallback onScanResult() callbackType: " + callbackType + ", result: " + result.getScanRecord());

            BluetoothDevice device = result.getDevice();

            if (device != null && !bluetoothDeviceList.contains(device)) {
                bluetoothDeviceList.add(device);

                CustomBluetoothDevice customBluetoothDevice = new CustomBluetoothDevice(device, result.getRssi());
                customBluetoothDevices.add(customBluetoothDevice);
                mLeDeviceListAdapter.updateList(customBluetoothDevices);
            }

        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            System.out.println("ScanCallback onBatchScanResults() results: " + results);

        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            System.out.println("ScanCallback onScanFailed() errorCode: " + errorCode);

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                bluetoothDeviceList.clear();
                customBluetoothDevices.clear();
                mLeDeviceListAdapter.updateList(customBluetoothDevices);
//                scanLeDevice(true);
                scanBTDevice(true);
                break;
            case R.id.menu_stop:
//                scanLeDevice(false);
                scanBTDevice(false);
                break;
        }
        return true;
    }


}
