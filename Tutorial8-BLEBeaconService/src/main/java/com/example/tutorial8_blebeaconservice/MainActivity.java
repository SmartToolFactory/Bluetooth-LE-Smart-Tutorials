package com.example.tutorial8_blebeaconservice;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_LOCATION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!isLocationPermissionGranted()) {
            requestLocationPermission();
        } else {
            Intent intent = new Intent(MainActivity.this, BLEService.class);
            startService(intent);
            registerBluetoothReceiver();
        }

    }


    @Override
    protected void onPause() {
        super.onPause();

        try {
            Intent intent = new Intent(MainActivity.this, BLEService.class);
            stopService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            unregisterReceiver(bleBroadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void requestLocationPermission() {
        // Request for location permission
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_LOCATION);
    }


    public boolean isLocationPermissionGranted() {
        return ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(MainActivity.this, BLEService.class);
            startService(intent);
        }
    }


    private BroadcastReceiver bleBroadcastReceiver = new BroadcastReceiver() {
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action == null) return;


            switch (action) {
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Toast.makeText(context, "Found Bluetooth device: " + device.getName(), Toast.LENGTH_SHORT)
                            .show();

                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    Toast.makeText(context, "Bluetooth discovery started.", Toast.LENGTH_SHORT).show();

                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    Toast.makeText(context, "Bluetooth discovery finished.", Toast.LENGTH_SHORT).show();

                    break;
                case BluetoothAdapter.ACTION_STATE_CHANGED:

                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    String message = "Empty";

                    switch (state) {
                        case BluetoothAdapter.STATE_OFF:
                            message = "Bluetooth is off";
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            message = "Bluetooth is turning off...";
                            break;
                        case BluetoothAdapter.STATE_ON:
                            message = "Bluetooth is on";
                            break;
                        case BluetoothAdapter.STATE_TURNING_ON:
                            message = "Bluetooth is turning on...";
                            break;
                    }
                    break;

                case BLEService.BEACON_ACTIVE:
                    Toast.makeText(MainActivity.this, "User device is eligible for proximity beacon.", Toast.LENGTH_SHORT).show();
                    break;

                case BLEService.BEACON_IN_PROXIMITY:
                    Toast.makeText(MainActivity.this, "Welcome to TAPPZ", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private void registerBluetoothReceiver() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            IntentFilter intentFilter = makeIntentFilter();
            if (intentFilter != null) {
                registerReceiver(bleBroadcastReceiver, intentFilter);
            }
        }
    }

    private IntentFilter makeIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

            intentFilter.addAction(BLEService.BEACON_ACTIVE);
            intentFilter.addAction(BLEService.BEACON_IN_PROXIMITY);
            intentFilter.addAction(BLEService.SERVICE_STARTED);
            intentFilter.addAction(BLEService.BLE_SCAN_SUCCESS);
            intentFilter.addAction(BLEService.SCAN_RESULT);
            intentFilter.addAction(BLEService.BLE_SCAN_FAILED);

        }

        return intentFilter;
    }

}
