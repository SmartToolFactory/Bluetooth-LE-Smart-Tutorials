package com.stfactory.tutorial2_1gattadvertising;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    private static final String UUID = "CDB7950D-73F1-4D4D-8E47-C090502DBD63";

    private UUID SERVICE_UUID = java.util.UUID.fromString(UUID);

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_LOCATION = 2;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;

    // private BluetoothGattServer mGattServer;


    private boolean mAdvertising;

    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.d(TAG, "Peripheral advertising started.");
            mAdvertising = true;
            invalidateOptionsMenu();

            Toast.makeText(MainActivity.this, "Peripheral advertising started." + settingsInEffect, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.d(TAG, "Peripheral advertising failed: " + errorCode);
            mAdvertising = false;
            invalidateOptionsMenu();

            Toast.makeText(MainActivity.this, "Periphereal adverising failed: " + errorCode, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Request for location permission
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_LOCATION);

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }


        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

    }


    @Override
    protected void onResume() {
        super.onResume();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            finish();
            return;
        }

        mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();

    }


    /**
     * Start advertising data with specified settings. There are 3 components are required for simple advertising.
     *
     * <li>
     *     <b>AdvertiseSettings</b> to set features of advertising such as timout, power level and connectability
     * </li>
     *
     * <li>
     *     <b>AdvertiseData</b> to be send to clients
     * </li>
     *
     * <li>
     *     <b>AdvertiseCallback </b> to get events on {@link AdvertiseCallback#onStartFailure(int)}
     *     or {@link AdvertiseCallback#onStartSuccess(AdvertiseSettings)} events
     * </li>
     *
     */
    private void startAdvertising() {
        if (mBluetoothLeAdvertiser == null) {
            return;
        }

        // Advertising Settings
        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
                .build();

        ParcelUuid parcelUuid = new ParcelUuid(SERVICE_UUID);

        // Data to be advertised
        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .addServiceUuid(parcelUuid)
                .build();

        // Start advertising
        mBluetoothLeAdvertiser.startAdvertising(settings, data, mAdvertiseCallback);

    }

    private void stopAdvertising() {
        if (mBluetoothLeAdvertiser != null) {
            mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
            mAdvertising = false;
            invalidateOptionsMenu();
        }
    }

    protected void onPause() {
        super.onPause();
        stopAdvertising();
    }


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
        if (!mAdvertising) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_advertise).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_advertise).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_advertise:
                startAdvertising();
                break;
            case R.id.menu_stop:
                stopAdvertising();
                break;
        }
        return true;
    }
}
