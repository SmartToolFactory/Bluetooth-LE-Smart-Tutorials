package com.example.tutorial3_1gatt_connect;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
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

import com.example.tutorial3_1gatt_connect.adapter.DeviceListAdapter;
import com.example.tutorial3_1gatt_connect.model.PeripheralDeviceItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.example.tutorial3_1gatt_connect.constant.Constants.CURRENT_TIME;
import static com.example.tutorial3_1gatt_connect.constant.Constants.TIME_SERVICE;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CentralActivity extends BluetoothScanActivity implements DeviceListAdapter.OnRecyclerViewClickListener {

    private static final String TAG = CentralActivity.class.getName();


    /**
     * Public API for Bluetooth GATT Profile
     */
    private BluetoothGatt mBluetoothGatt;

    private DeviceListAdapter mLeDeviceListAdapter;

    private List<BluetoothDevice> bluetoothDeviceList = new ArrayList<>();

    /* Collection of notification subscribers */
//    private Set<BluetoothDevice> mDiscoveredDevices = new LinkedHashSet<>();

    private List<PeripheralDeviceItem> peripheralDeviceItemList = new ArrayList<>();

    private final Map<String, PeripheralDeviceItem> mDeviceMap = new LinkedHashMap<>();


    double previousDistance = -1;
    double currentDistance = -1;

    private boolean mConnected = false;

    private String message;

    private TextView tvConnectionStatus, tvDataSent, tvDataReceived, tvBeacon;
    private EditText etMessage;


    boolean isConnectionPermitted = true;

    boolean isConnectionEstablished = false;

    boolean isClosingToSensor = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_central);
        initViews();

    }


    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        mLeDeviceListAdapter = new DeviceListAdapter(this, peripheralDeviceItemList);
        mLeDeviceListAdapter.setClickListener(this);

        recyclerView.setAdapter(mLeDeviceListAdapter);

        tvConnectionStatus = findViewById(R.id.tvConnectionStatus);
        tvDataSent = findViewById(R.id.tvDataSent);
        tvDataReceived = findViewById(R.id.tvDataReceived);
        tvBeacon = findViewById(R.id.tvBeacon);

        etMessage = findViewById(R.id.etMessage);

        Button btnSend = findViewById(R.id.btnSend);
        Button btnRead = findViewById(R.id.btnRead);
        Button btnResetBeacon = findViewById(R.id.btnResetBeacon);


        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mConnected && mBluetoothGatt != null) {

                    message = etMessage.getText().toString();
                    BluetoothGattService service = mBluetoothGatt.getService(TIME_SERVICE);
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(CURRENT_TIME);

                    characteristic.setValue(message);

                    // TODO Write Characteristic
                    boolean isWrite = mBluetoothGatt.writeCharacteristic(characteristic);

                    if (isWrite) {
                        logDataSent(message);
                    } else {
                        logDataSent("writeCharacteristic NOT initiated");
                    }


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

//                    BluetoothGattService service = mBluetoothGatt.getService(TIME_SERVICE);
//                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(CURRENT_TIME);

                    // TODO Read Characteristic

//                    boolean isRead = mBluetoothGatt.readCharacteristic(characteristic);

                    List<BluetoothGattService> serviceList = mBluetoothGatt.getServices();

                    // TODO Read for Multiple Services
                    for (BluetoothGattService service : serviceList) {
                        List<BluetoothGattCharacteristic> characteristicList = service.getCharacteristics();
                        for (BluetoothGattCharacteristic characteristic : characteristicList) {
                            mBluetoothGatt.readCharacteristic(characteristic);
                        }

                    }

//                    showToast("isReadChar: " + isRead);


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


    }


    @Override
    protected void onResume() {
        super.onResume();

        if (!isBluetoothEnabled()) {
//            enableBluetooth();
            promptForEnableBluetooth();
        }
        
        if (isBluetoothEnabled()) {
            bluetoothDeviceList.clear();
            mDeviceMap.clear();
            peripheralDeviceItemList.clear();
            mLeDeviceListAdapter.updateList(peripheralDeviceItemList);

            scanBTDevice(true);
        } else {
            Toast.makeText(this, "Bluetooth is NOT enabled!", Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    protected void onPause() {
        super.onPause();

        scanBTDevice(false);

        bluetoothDeviceList.clear();
        mDeviceMap.clear();
        peripheralDeviceItemList.clear();
        mLeDeviceListAdapter.updateList(peripheralDeviceItemList);

    }

    private void connectDevice(BluetoothDevice device) {
        logStatus("Connecting to " + device.getAddress());
        GattClientCallback gattClientCallback = new GattClientCallback();
        mBluetoothGatt = device.connectGatt(this, false, gattClientCallback);
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
                peripheralDeviceItemList.clear();
                mDeviceMap.clear();
                mLeDeviceListAdapter.updateList(peripheralDeviceItemList);
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
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(CURRENT_TIME);

            // IMPORTANT: Characteristic write type should be with No Response to invoke onCharacteristicWrite immediately.
            // If default type is selected Server should send a response via sendResponse(),
            // otherwise devices disconnect from each other after a timeout period.

            // Note: This is not needed for enabling notifications, only example here
//            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);

            boolean isMtuSuccess = gatt.requestMtu(64);

            showToast("onServicesDiscovered() characteristic: " + characteristic + ", isMtuSuccess SUCCESS: " + isMtuSuccess);
        }

        /*
         *** CHARACTERISTIC CALLBACKS ***
         */

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);

            System.out.println("onCharacteristicRead(): " + new String(characteristic.getValue()));
            logDataReceived("onCharacteristicRead(): " + new String(characteristic.getValue()));
            showToast("onCharacteristicRead(): " + new String(characteristic.getValue()));

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
            showToast("onCharacteristicWrite() " + message);

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

    @Override
    public void onBLEScanResult(int callbackType, ScanResult result) {


        //                if (currentDistance < previousDistance) {
//                    tvBeacon.setText(beaconDevice.getName() + " CLOSING current distance: " + decimalFormat.format(currentDistance));
//                    isClosingToSensor = true;
//                } else {
//                    tvBeacon.setText(beaconDevice.getName() + " GETTING AWAY current distance: " + decimalFormat.format(currentDistance));
//                    isClosingToSensor = false;
//                }
//
//                // TODO Beacon Connection Conditions
//                if (isClosingToSensor && currentDistance < 0.5 && isConnectionPermitted) {
//                    showToast("Connected to Server after BEACON notified");
//                    isConnectionPermitted = false;
//
//                }
//
//                previousDistance = currentDistance;


        ScanRecord scanRecord = result.getScanRecord();
        if (scanRecord != null) {
            int txPower = scanRecord.getTxPowerLevel();

            int rssi = result.getRssi();
            BluetoothDevice bluetoothDevice = result.getDevice();


            if (mDeviceMap.containsKey(bluetoothDevice.getAddress())) {
                PeripheralDeviceItem peripheralDeviceItem = mDeviceMap.get(bluetoothDevice.getAddress());
                if (peripheralDeviceItem != null) {
                    peripheralDeviceItem.rssi = rssi;
                    peripheralDeviceItem.txPower = txPower;
                }
            } else {
                PeripheralDeviceItem peripheralDeviceItem = new PeripheralDeviceItem(bluetoothDevice, rssi, txPower);
                mDeviceMap.put(bluetoothDevice.getAddress(), peripheralDeviceItem);
                bluetoothDeviceList.add(bluetoothDevice);
            }

            peripheralDeviceItemList.clear();
            peripheralDeviceItemList.addAll(mDeviceMap.values());

            mLeDeviceListAdapter.updateList(peripheralDeviceItemList);
        }

    }

    @Override
    public void onBLEBatchScanResults(List<ScanResult> results) {

    }

    @Override
    public void onBLEScanFailed(int errorCode) {
        showToast("onBLEScanFailed errorCode: " + errorCode);
    }
}
