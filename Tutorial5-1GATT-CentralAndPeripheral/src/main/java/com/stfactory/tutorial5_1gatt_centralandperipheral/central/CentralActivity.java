package com.stfactory.tutorial5_1gatt_centralandperipheral.central;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
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
import android.os.ParcelUuid;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.stfactory.tutorial5_1gatt_centralandperipheral.R;
import com.stfactory.tutorial5_1gatt_centralandperipheral.central.adapter.DeviceListAdapter;
import com.stfactory.tutorial5_1gatt_centralandperipheral.central.broadcast.BluetoothStateBroadcastReceiver;
import com.stfactory.tutorial5_1gatt_centralandperipheral.central.model.CustomBluetoothDevice;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import uk.co.alt236.bluetoothlelib.device.BluetoothLeDevice;
import uk.co.alt236.bluetoothlelib.device.beacon.BeaconType;
import uk.co.alt236.bluetoothlelib.device.beacon.BeaconUtils;
import uk.co.alt236.bluetoothlelib.device.beacon.ibeacon.IBeaconDevice;

import static com.stfactory.tutorial5_1gatt_centralandperipheral.Constants.CURRENT_TIME;
import static com.stfactory.tutorial5_1gatt_centralandperipheral.Constants.TIME_SERVICE;
import static com.stfactory.tutorial5_1gatt_centralandperipheral.Constants.UUID_ESTIMOTE_PURPLE1;
import static com.stfactory.tutorial5_1gatt_centralandperipheral.Constants.nordicUART;
import static com.stfactory.tutorial5_1gatt_centralandperipheral.Constants.nordicUARTRX;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CentralActivity extends AppCompatActivity implements DeviceListAdapter.OnRecyclerViewClickListener {

    private static final String TAG = CentralActivity.class.getName();


    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_LOCATION = 2;

    // Stops scanning after 100 seconds.
    private static final long SCAN_PERIOD = 100_000;

    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothLeScanner bluetoothLeScanner;

    /**
     * Public API for Bluetooth GATT Profile
     */
    private BluetoothGatt mBluetoothGatt;

    private boolean mScanning;
    private Handler mHandler;

    private DeviceListAdapter mLeDeviceListAdapter;

    private List<BluetoothDevice> bluetoothDeviceList = new ArrayList<>();

    private List<CustomBluetoothDevice> customBluetoothDevices = new ArrayList<>();

    private BluetoothStateBroadcastReceiver bluetoothStateBroadcastReceiver = new BluetoothStateBroadcastReceiver();


    private boolean mConnected = false;

    private String connectionStatus = "Not connected";

    private String message;

    private TextView tvConnectionStatus, tvDataSent, tvDataReceived, tvBeacon;
    private EditText etMessage;

    double previousDistance = -1;
    double currentDistance = -1;


    boolean isConnectionPermitted = true;

    boolean isConnectionEstablished = false;

    boolean isClosingToSensor = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_central);
        initViews();

        mHandler = new Handler();

        // Request for location permission
        if (!isLocationPermissionGranted()) {
            requestLocationPermission();
        }

        isBluetoothLESupported();

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

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvConnectionStatus = findViewById(R.id.tvConnectionStatus);
        tvDataSent = findViewById(R.id.tvDataSent);
        tvDataReceived = findViewById(R.id.tvDataReceived);
        tvBeacon = findViewById(R.id.tvBeacon);

        etMessage = findViewById(R.id.etMessage);
        Button btnSend = findViewById(R.id.btnWrite);
        Button btnRead = findViewById(R.id.btnRead);
        Button btnResetBeacon = findViewById(R.id.btnResetBeacon);


        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mConnected && mBluetoothGatt != null) {

                    message = etMessage.getText().toString();

                    BluetoothGattService service = mBluetoothGatt.getService(TIME_SERVICE);
                    BluetoothGattCharacteristic characteristic;

                    if (service != null) {
                        characteristic = service.getCharacteristic(CURRENT_TIME);
                    } else {
                        service = mBluetoothGatt.getService(nordicUART);
                        characteristic = service.getCharacteristic(nordicUARTRX);
                    }

                    // TODO Write Characteristic
                    boolean isWrite = false;

                    if (characteristic != null) {
                        characteristic.setValue(message);
                        isWrite = mBluetoothGatt.writeCharacteristic(characteristic);
                    }

                    if (isWrite) {
                        logDataSent(message);
                    } else {
                        logDataSent("writeCharacteristic NOT initiated");
                    }

                    boolean isMtuSuccess = mBluetoothGatt.requestMtu(64);
                    showToast("Mtu success: " + isMtuSuccess);

                    Toast.makeText(CentralActivity.this, "Message sent: " + message + ", SUCCESS: " + isWrite, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CentralActivity.this, "Connection is NOT established!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (mConnected && mBluetoothGatt != null) {

                    BluetoothGattService service = mBluetoothGatt.getService(TIME_SERVICE);

                    BluetoothGattCharacteristic characteristic = null;

                    if (service != null) {
                        characteristic = service.getCharacteristic(CURRENT_TIME);
                    } else {
                        service = mBluetoothGatt.getService(nordicUART);
                        characteristic = service.getCharacteristic(nordicUARTRX);
                    }

                    // TODO Read Characteristic
                    boolean isRead = mBluetoothGatt.readCharacteristic(characteristic);

                    showToast("isReadChar: " + isRead);


                } else {
                    Toast.makeText(CentralActivity.this, "Connection is NOT established!", Toast.LENGTH_SHORT).show();
                }
            }
        });


        btnResetBeacon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isConnectionPermitted = true;
            }
        });


        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        mLeDeviceListAdapter = new DeviceListAdapter(this, customBluetoothDevices);
        mLeDeviceListAdapter.setClickListener(this);

        recyclerView.setAdapter(mLeDeviceListAdapter);

    }

    private void isBluetoothLESupported() {
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

    private boolean isLocationPermissionGranted() {
        return ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
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


        bluetoothDeviceList.clear();
        customBluetoothDevices.clear();
        mLeDeviceListAdapter.updateList(customBluetoothDevices);
//                scanLeDevice(true);
        scanBTDevice(true);

    }

    private boolean isBluetoothEnabled() {
        return mBluetoothAdapter.isEnabled();
    }

    private void enableBluetooth() {
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
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

    private void connectDevice(BluetoothDevice device) {
        logStatus("Connecting to " + device.getAddress());
        GattClientCallback gattClientCallback = new GattClientCallback();
        mBluetoothGatt = device.connectGatt(this, false, gattClientCallback);
    }

//    private void scanLeDevice(final boolean enable) {
//
//        if (enable) {
//
//            logStatus("SCANNING");
//            mScanning = true;
//            mBluetoothAdapter.startLeScan(mLeScanCallback);
//
//            stopLeDeviceScanAfterAPeriod();
//
//        } else {
//            mScanning = false;
//            mBluetoothAdapter.stopLeScan(mLeScanCallback);
//        }
//
//
//        invalidateOptionsMenu();
//    }

//    private void stopLeDeviceScanAfterAPeriod() {
//        // Stops scanning after a pre-defined scan period.
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                mScanning = false;
//                mBluetoothAdapter.stopLeScan(mLeScanCallback);
//                invalidateOptionsMenu();
//            }
//        }, SCAN_PERIOD);
//    }


    // Device scan callback.
//    private BluetoothAdapter.LeScanCallback mLeScanCallback =
//            new BluetoothAdapter.LeScanCallback() {
//
//                @Override
//                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
//
//                    System.out.println("DeviceScanActivity LeScanCallback onLeScan() device: " + device + ", thread: " + Thread.currentThread().getName() + ", record: " + scanRecord);
//
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//
//                            if (!bluetoothDeviceList.contains(device)) {
//                                bluetoothDeviceList.add(device);
//
//                                //   CustomBluetoothDevice customBluetoothDevice = new CustomBluetoothDevice(device, rssi);
//
//                                CustomBluetoothDevice customBluetoothDevice = null;
//
//                                customBluetoothDevice = new CustomBluetoothDevice(device, String.valueOf(rssi));
//                                customBluetoothDevices.add(customBluetoothDevice);
//                                mLeDeviceListAdapter.updateList(customBluetoothDevices);
//
//
//                            } else {
//
//                                int index = bluetoothDeviceList.indexOf(device);
//                                bluetoothDeviceList.set(index, device);
//
//                                CustomBluetoothDevice customBluetoothDevice = new CustomBluetoothDevice(device, String.valueOf(rssi));
//
//                                customBluetoothDevices.set(index, customBluetoothDevice);
//                                mLeDeviceListAdapter.updateList(customBluetoothDevices);
//                            }
//                        }
//                    });
//                }
//
//            };


    /**
     * Advanced scanning method for Android 21+
     *
     * @param enable
     */
    private void scanBTDevice(boolean enable) {
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

    private void startBTScan() {

        logStatus("SCANNING");

        List<ScanFilter> scanFilters = new ArrayList<>();


        // Test Time Filter
        ScanFilter filterTimeService = new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(TIME_SERVICE))
                .build();


        // Estimote Filters
        ScanFilter filterEstimotePurple1 = new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(UUID_ESTIMOTE_PURPLE1))
                .build();

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

        // TODO Filters for scanning Services
//        scanFilters.add(filterTimeService);
//        scanFilters.add(filter1);
//        scanFilters.add(filter2);
//        scanFilters.add(nordicService);

        ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
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


            System.out.println("ScanCallback onScanResult() callbackType: " + callbackType + ", result: " + result.getScanRecord() + ", data: " + result.getScanRecord().getBytes());

            BluetoothDevice device = result.getDevice();

            if (!bluetoothDeviceList.contains(device)) {
                bluetoothDeviceList.add(device);

                CustomBluetoothDevice customBluetoothDevice = new CustomBluetoothDevice(device, String.valueOf(result.getRssi()));
                customBluetoothDevices.add(customBluetoothDevice);
                mLeDeviceListAdapter.updateList(customBluetoothDevices);

            } else {

                int index = bluetoothDeviceList.indexOf(device);
                bluetoothDeviceList.set(index, device);

                CustomBluetoothDevice customBluetoothDevice = new CustomBluetoothDevice(device, String.valueOf(result.getRssi()));

                customBluetoothDevices.set(index, customBluetoothDevice);
                mLeDeviceListAdapter.updateList(customBluetoothDevices);
            }

            final BluetoothLeDevice deviceLe = new BluetoothLeDevice(device, result.getRssi(), result.getScanRecord().getBytes(), System.currentTimeMillis());

            if (BeaconUtils.getBeaconType(deviceLe) == BeaconType.IBEACON) {
                IBeaconDevice beaconDevice = new IBeaconDevice(deviceLe);

                currentDistance = beaconDevice.getAccuracy();

                DecimalFormat decimalFormat = new DecimalFormat("##.##");

                if (currentDistance < previousDistance) {
                    tvBeacon.setText(beaconDevice.getName() + " CLOSING current distance: " + decimalFormat.format(currentDistance));
                    isClosingToSensor = true;
                } else {
                    tvBeacon.setText(beaconDevice.getName() + " GETTING AWAY current distance: " + decimalFormat.format(currentDistance));
                    isClosingToSensor = false;
                }

                // TODO Beacon Connection Conditions
                if (isClosingToSensor && currentDistance < 0.5 && isConnectionPermitted) {
                    showToast("Connected to Server after BEACON notified");
                    isConnectionPermitted = false;

                }

                previousDistance = currentDistance;

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

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(CentralActivity.this, "onScanFailed errorCode: " + errorCode, Toast.LENGTH_SHORT).show();
                }
            });

        }
    };


    protected static double calculateAccuracy(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }

        double ratio = rssi * 1.0 / txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio, 10);
        } else {
            double accuracy = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
            return accuracy;
        }
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
        getMenuInflater().inflate(R.menu.menu_central, menu);
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

    @Override
    public void onItemClicked(View view, int position) {

        BluetoothDevice device = bluetoothDeviceList.get(position);

        connectDevice(device);

    }

    public void setConnected(boolean connected) {
        mConnected = connected;
    }

    public void disconnectGattServer() {

        setConnected(false);
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
        }
    }

    private class GattClientCallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            // Log Connection State
            String stateString = "Unknown";
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    stateString = "STATE_CONNECTED";

                    break;

                case BluetoothProfile.STATE_CONNECTING:
                    stateString = "STATE_CONNECTED";
                    break;

                case BluetoothProfile.STATE_DISCONNECTING:
                    stateString = "STATE_DISCONNECTING";
                    break;

                case BluetoothProfile.STATE_DISCONNECTED:
                    stateString = "STATE_DISCONNECTED";
                    break;
            }

            logStatus("onConnectionStateChange() newState: " + stateString);


            if (status == BluetoothGatt.GATT_FAILURE) {
//                logError("onConnectionStateChange() Gatt failure status " + status);
                disconnectGattServer();
                return;

            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                // handle anything not SUCCESS as failure
//                logStatus("onConnectionStateChange() Gatt Success status: " + status);
                disconnectGattServer();
                return;

            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();
                setConnected(true);

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                disconnectGattServer();
                setConnected(false);
            }


        }


        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            if (status != BluetoothGatt.GATT_SUCCESS) {
                return;
            }

            // TODO Enable Notifications

            BluetoothGattService service = gatt.getService(TIME_SERVICE);
            BluetoothGattCharacteristic characteristic;

            if (service != null) {
                characteristic = service.getCharacteristic(CURRENT_TIME);
            } else {
                service = gatt.getService(nordicUART);
                characteristic = service.getCharacteristic(nordicUARTRX);
            }

            // IMPORTANT: Characteristic write type should be with No Response to invoke onCharacteristicWrite immediately.
            // If default type is selected Server should send a response via sendResponse(),
            // otherwise devices disconnect from each other after a timeout period.

            // Note: This is not needed for enabling notifications, only example here
//            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);

            boolean isNotified = false;

            if (characteristic != null) {
                isNotified = gatt.setCharacteristicNotification(characteristic, true);
            }

            showToast("onServicesDiscovered() characteristic: " + characteristic + ", isNotified SUCCESS: " + isNotified);

        }

        /*
         *** CHARACTERISTIC CALLBACKS ***
         */

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);

            System.out.println("onCharacteristicRead(): " + new String(characteristic.getValue()));
            logDataReceived("onCharacteristicRead(): " + new String(characteristic.getValue()));
            showToast("onCharacteristicRead(): " + new String(characteristic.getValue()) + ", characteristic: " + characteristic);

        }


        /*
         * onCharacteristicWrite is queued to be invoked after calls gatt.writeCharacteristic(characteristic).
         * sendResponse() method of server calls this method without wait time if write type is not NO_RESPONSE.
         *
         * IMPORTANT: If sendResponse() is not called by Server, this device calls
         * this method after a timeout and device is REMOVED from server. And write method fail before this method is called
         *
         * PROPERTY_WRITE_NO_RESPONSE of characteristic should be set and descriptor should have
         * BluetoothGattDescriptor.PERMISSION_WRITE to invoke this method just after
         * mBluetoothGatt.writeCharacteristic(characteristic) is called
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            logDataSent("onCharacteristicWrite() " + message);
            showToast("onCharacteristicWrite() " + message + ", characteristic: " + characteristic);

        }


        /*
         * This method is invoked if a notification is send from server with
         *   mGattServer.notifyCharacteristicChanged(mDeviceConnected, characteristic, true)
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            logDataReceived("onCharacteristicChanged()" + new String(characteristic.getValue()));
            showToast("onCharacteristicChanged()" + new String(characteristic.getValue()));

        }

        /*
         *** DESCRIPTOR CALLBACKS ***
         */

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);

            logDataReceived("onDescriptorWrite() onDescriptorRead: " + descriptor + ", status: " + status);
            showToast("onDescriptorWrite() onDescriptorRead: " + descriptor + ", status: " + status);

        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);

            logDataReceived("onDescriptorWrite() descriptor: " + descriptor + ", status: " + status);

            showToast("onDescriptorWrite() gatt: " + gatt
                    + ", descriptor: " + descriptor + ", status: " + status);
        }


        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);

            logStatus("onReadRemoteRssi() rssi: " + rssi + ", status: " + status);

            showToast("onReadRemoteRssi() rssi: " + rssi + ", status: " + status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            showToast("onMtuChanged() mtu: " + mtu + ", status: " + status);
        }
    }

    public void logStatus(String msg) {
        Log.d(TAG, msg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvConnectionStatus.setText(msg);
            }
        });

    }

    public void logDataSent(String msg) {
        Log.d(TAG, msg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvDataSent.setText(msg);
            }
        });
    }

    public void logDataReceived(String msg) {
        Log.d(TAG, msg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvDataReceived.setText(msg);
            }
        });
    }

    public void logError(String msg) {
        logStatus("Error: " + msg);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvConnectionStatus.setText(msg);
            }
        });
    }

    public void showToast(String message) {
        runOnUiThread(() -> {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
    }
}
