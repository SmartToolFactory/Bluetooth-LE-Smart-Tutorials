package com.example.tutorial8_blebeaconservice;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Service;
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
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import uk.co.alt236.bluetoothlelib.device.BluetoothLeDevice;
import uk.co.alt236.bluetoothlelib.device.beacon.BeaconType;
import uk.co.alt236.bluetoothlelib.device.beacon.BeaconUtils;
import uk.co.alt236.bluetoothlelib.device.beacon.ibeacon.IBeaconDevice;

public class BLEService extends Service {

    /*
     * Available Beacon Addresses
     */
    private static final String MAC_ESTIMOTE_GREEN = "D7:F7:DD:D4:9C:37";
    private static final String MAC_ESTIMOTE_BLUE = "DF:AA:AD:47:5E:50";
    private static final String MAC_ESTIMOTE_PURPLE = "E7:7E:57:AF:C4:A0";

    public static final String SERVICE_STARTED = "SERVICE_STARTED";
    public static final String SCAN_RESULT = "SCAN_RESULT";
    public static final String BLE_NOT_ENABLED = "BLE_NOT_ENABLED";
    public static final String BLE_SCAN_SUCCESS = "BLE_SCAN_SUCCESS";
    public static final String BLE_SCAN_FAILED = "BLE_SCAN_FAILED";
    public static final String BEACON_ACTIVE = "BEACON_ACTIVE";
    public static final String BEACON_IN_PROXIMITY = "BEACON_IN_PROXIMITY";

    public final static String EXTRA_DATA = "EXTRA_DATA";

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;

    private boolean mScanning = false;

    private boolean isCentralReady = true;

    private List<String> beaconDeviceMACList;


    @Override
    public void onCreate() {
        super.onCreate();

        beaconDeviceMACList = new ArrayList<String>();

        beaconDeviceMACList.add(MAC_ESTIMOTE_GREEN);
        beaconDeviceMACList.add(MAC_ESTIMOTE_BLUE);
        beaconDeviceMACList.add(MAC_ESTIMOTE_PURPLE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            isBluetoothSupported();

            // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
            // BluetoothAdapter through BluetoothManager.
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();


            // Checks if Bluetooth is supported on the device.
            if (mBluetoothAdapter == null) {
                Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
                stopSelf();
                return;
            }

            bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

            if (isBluetoothEnabled()) {
                startBTScan();
            } else {
                IntentFilter intentFilter = new IntentFilter(BLE_NOT_ENABLED);
                sendBroadcast(new Intent());
            }

        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            stopSelf();
            return Service.START_NOT_STICKY;
        }

        broadcastUpdate(BEACON_ACTIVE);

        return Service.START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopBTScan();
        Toast.makeText(this, "BLEService onDestroy()", Toast.LENGTH_SHORT).show();
    }


    private void isBluetoothSupported() {
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            stopSelf();
        }
    }

    private boolean isLocationPermissionGranted() {
        return ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }


    /**
     * ScanCallback for advanced BT LE scan. It requires Android 21+
     */
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            mScanning = true;
            checkBeaconProximity(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
//            onBLEScanFailed(errorCode);
            broadcastUpdate(BLE_SCAN_FAILED);
            mScanning = false;
        }
    };

    private boolean isBluetoothEnabled() {
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startBTScan() {

        if (bluetoothLeScanner != null && mScanning) {
            bluetoothLeScanner.stopScan(mScanCallback);
        }

        List<ScanFilter> scanFilters = new ArrayList<>();

        ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        bluetoothLeScanner.startScan(scanFilters, scanSettings, mScanCallback);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void stopBTScan() {
        if (bluetoothLeScanner != null) {
            bluetoothLeScanner.stopScan(mScanCallback);
        }
    }


    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final String data) {

        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_DATA, data);
        sendBroadcast(intent);

    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void checkBeaconProximity(ScanResult result) {
        BluetoothDevice device = result.getDevice();

        final BluetoothLeDevice deviceLe = new BluetoothLeDevice(device, result.getRssi(), result.getScanRecord().getBytes(), System.currentTimeMillis());

        if (BeaconUtils.getBeaconType(deviceLe) == BeaconType.IBEACON) {
            IBeaconDevice beaconDevice = new IBeaconDevice(deviceLe);

            if (isCentralReady) {

                boolean userInBeaconRange = false;
                double beaconProximity = beaconDevice.getAccuracy();

                if (beaconProximity < 1) {
                    userInBeaconRange = true;
                }

                if (userInBeaconRange) {

                    broadcastUpdate(BEACON_IN_PROXIMITY);

                    isCentralReady = false;

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isCentralReady = true;
                            broadcastUpdate(BEACON_ACTIVE);

                        }
                    }, 10000);
                }
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
