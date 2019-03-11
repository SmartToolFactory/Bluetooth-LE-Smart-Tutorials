package com.stfactory.tutorial1_ble_basics;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_LOCATION = 100;
    public static final int REQUEST_ENABLE_BT = 101;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    private BluetoothAdapter bluetoothAdapter;
    // TODO bluetoothAdapter.startLeScan() is deprecated
    private BluetoothLeScanner bluetoothLeScanner;

    private boolean mScanning;
    private Handler handler = new Handler();

    // For getting device list
    List<BluetoothDevice> bluetoothDeviceList = new ArrayList<>();
    private StringBuilder stringBuilder = new StringBuilder();

    private TextView tvDevices;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Bluetooth LE is not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Location Permission is required for getting Bluetooth devices
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_LOCATION);

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        // TODO This is the new BLE scanner object
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        }

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        tvDevices = findViewById(R.id.tvDevices);
        button = findViewById(R.id.button);

        button.setOnClickListener(v -> {

            mScanning = !mScanning;

            if (mScanning) {
                button.setText("Stop Scanning");
                scanLeDevice(mScanning);
            } else {
                button.setText("Scan");
            }

        });
    }

    private void scanLeDevice(final boolean enable) {

        if (enable) {
            // Stops scanning after a pre-defined scan period.
            handler.postDelayed(() -> {

                mScanning = false;
                bluetoothAdapter.stopLeScan(leScanCallback);
                Toast.makeText(MainActivity.this, "Scan is finished.", Toast.LENGTH_SHORT).show();
                button.setText("Scan");

            }, SCAN_PERIOD);

            mScanning = true;
            bluetoothDeviceList.clear();
            bluetoothAdapter.startLeScan(leScanCallback);
            button.setText("Stop scan");


        } else {
            mScanning = false;
            bluetoothAdapter.stopLeScan(leScanCallback);
            button.setText("Scan");

        }

    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback leScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {

                    // This is Main thread by default
                    System.out.println("MainActivity leScanCallback onLeScan() thread: "
                            + Thread.currentThread().getName() + ", device: " + device + ", rssi: " + rssi);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (!bluetoothDeviceList.contains(device)) {

                                bluetoothDeviceList.add(device);
                                stringBuilder.setLength(0);

                                for (BluetoothDevice bluetoothDevice : bluetoothDeviceList) {
                                    stringBuilder.append("name: " + bluetoothDevice.getName()
                                            + ", address: " + bluetoothDevice.getAddress()
                                            + ", rssi: " + rssi + "\n");

                                }

                                tvDevices.setText(stringBuilder.toString());
                            }

                        }
                    });
                }
            };
}
