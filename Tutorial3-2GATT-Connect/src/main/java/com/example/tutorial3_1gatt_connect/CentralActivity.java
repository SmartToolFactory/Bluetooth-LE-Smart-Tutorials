package com.example.tutorial3_1gatt_connect;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.example.tutorial3_1gatt_connect.constant.Constants.UUID_ADVERTISE_SERVICE;
import static com.example.tutorial3_1gatt_connect.constant.Constants.nordicUART;
import static com.example.tutorial3_1gatt_connect.constant.Constants.nordicUARTRX;
import static com.example.tutorial3_1gatt_connect.constant.Constants.nordicUARTTX;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CentralActivity extends BluetoothScanActivity implements DeviceListAdapter.OnRecyclerViewClickListener {

    private static final String TAG = CentralActivity.class.getName();
    int counter = 0;


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

    private BluetoothDevice bluetoothDeviceKiosk;

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
                    BluetoothGattService service = mBluetoothGatt.getService(nordicUART);
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(nordicUARTRX);

                    if (characteristic == null) {
                        characteristic  = service.getCharacteristic(nordicUARTTX);
                    }

                    // TOKEN
//                    message = "85e0f5ac-7115-4855-ba94-b486b3324572_2722019";

//                    message = "Message "+ counter;
//                    characteristic.setValue(message);

                    boolean isWrite = false;
                    // TODO Write Characteristic
                     isWrite = mBluetoothGatt.writeCharacteristic(characteristic);

                    if (isWrite) {
                        logDataSent(message);
                        Toast.makeText(CentralActivity.this, "Message sent: " + message + ", SUCCESS: " + isWrite, Toast.LENGTH_SHORT).show();
                    } else {
                        logDataSent("writeCharacteristic NOT initiated");
                    }

//                     counter ++;
//                    message = "Message "+ counter;
//                    isWrite = mBluetoothGatt.writeCharacteristic(characteristic);
//
//                    if (isWrite) {
//                        logDataSent(message);
//                        Toast.makeText(CentralActivity.this, "Message sent: " + message + ", SUCCESS: " + isWrite, Toast.LENGTH_SHORT).show();
//
//                    } else {
//                        logDataSent("writeCharacteristic NOT initiated");
//                    }


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

            }
        });


    }


    @Override
    protected void onResume() {
        super.onResume();

        // Create Filters for Bluetooth states and actions
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

        // Register BroadcastReceiver for Bluetooth
        registerReceiver(broadcastReceiver, intentFilter);

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

        unregisterReceiver(broadcastReceiver);

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

            BluetoothGattService service = gatt.getService(nordicUART);
            BluetoothGattCharacteristic characteristic = null;

            if (service != null) {
                characteristic = service.getCharacteristic(nordicUARTRX);
            } else {
                service = gatt.getService(nordicUART);

                if (service != null) {
                    characteristic = service.getCharacteristic(nordicUARTRX);
                }
            }


            // IMPORTANT: Characteristic write type should be with No Response to invoke onCharacteristicWrite immediately.
            // If default type is selected Server should send a response via sendResponse(),
            // otherwise devices disconnect from each other after a timeout period.

            // Note: This is not needed for enabling notifications, only example here
//            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);

            // TODO Enable Notifications

            boolean isNotified = gatt.setCharacteristicNotification(characteristic, true);
            showToast("onServicesDiscovered() characteristic: " + characteristic + ", isNotified SUCCESS: " + isNotified);


            // TODO Request MTU
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

        getNearestKiosk(result);

    }

    @Override
    public void onBLEBatchScanResults(List<ScanResult> results) {

    }

    @Override
    public void onBLEScanFailed(int errorCode) {
        showToast("onBLEScanFailed errorCode: " + errorCode);
    }

    private void getNearestKiosk(ScanResult scanResult) {

        List<ParcelUuid> parcelUuids = scanResult.getScanRecord().getServiceUuids();

        if (bluetoothDeviceKiosk != null || parcelUuids == null) return;

        for (int i = 0; i < parcelUuids.size(); i++) {
            UUID serviceUUID = parcelUuids.get(i).getUuid();

            if (serviceUUID.equals(UUID_ADVERTISE_SERVICE)) {
                bluetoothDeviceKiosk = scanResult.getDevice();

                showToast("Kiosk is discovered");

                break;
            }
        }

    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Toast.makeText(context, "Found Bluetooth device: " + device.getName(), Toast.LENGTH_SHORT)
                        .show();

            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Toast.makeText(context, "Bluetooth discovery started.", Toast.LENGTH_SHORT).show();

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Toast.makeText(context, "Bluetooth discovery finished.", Toast.LENGTH_SHORT).show();

            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {

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

                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

            }
        }
    };

}
