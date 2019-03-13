package com.stfactory.tutorial3_2gatt_peripheral_multipleconnections;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.stfactory.tutorial3_2gatt_peripheral_multipleconnections.adapter.CentralDeviceListAdapter;
import com.stfactory.tutorial3_2gatt_peripheral_multipleconnections.constant.Constants;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.stfactory.tutorial3_2gatt_peripheral_multipleconnections.constant.Constants.CLIENT_CONFIG;
import static com.stfactory.tutorial3_2gatt_peripheral_multipleconnections.constant.Constants.CONFIG_DESCRIPTOR;
import static com.stfactory.tutorial3_2gatt_peripheral_multipleconnections.constant.Constants.CURRENT_TIME;
import static com.stfactory.tutorial3_2gatt_peripheral_multipleconnections.constant.Constants.TIME_SERVICE;
import static com.stfactory.tutorial3_2gatt_peripheral_multipleconnections.constant.Constants.nordicUART;
import static com.stfactory.tutorial3_2gatt_peripheral_multipleconnections.constant.Constants.nordicUARTRX;
import static com.stfactory.tutorial3_2gatt_peripheral_multipleconnections.constant.Constants.nordicUARTTX;

/**
 * This example demonstrates multiple connections to a single Peripheral
 * by multiple Central devices. Connected devices can be updated individually or all at once.
 * <p>
 * Sending response is mandatory for callbacks on Central side to be triggered. If response is not
 * send when service is not WRITE_NO_RESPONE central disconnects after a timeout period.
 * </p>
 */
public class PeripheralActivity extends BasePeripheralActivity implements CentralDeviceListAdapter.OnRecyclerViewClickListener {

    private static final String TAG = PeripheralActivity.class.getName();

    private RecyclerView recyclerView;
    private CentralDeviceListAdapter mLeDeviceListAdapter;
    private List<BluetoothDevice> mConnectedDeviceList = new ArrayList<>();

    private TextView tvConnectionStatus, tvDataSent, tvDataReceived;
    private EditText etMessage;

    /* Collection of notification subscribers */
    private Set<BluetoothDevice> mRegisteredDevices = new HashSet<>();


    private int sentMesageCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peripheral);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        mLeDeviceListAdapter = new CentralDeviceListAdapter(this, mConnectedDeviceList);
        mLeDeviceListAdapter.setClickListener(this);

        recyclerView.setAdapter(mLeDeviceListAdapter);

        tvConnectionStatus = findViewById(R.id.tvConnectionStatus);

        tvDataSent = findViewById(R.id.tvDataSent);
        tvDataReceived = findViewById(R.id.tvDataReceived);

        etMessage = findViewById(R.id.etMessage);
        Button btnSend = findViewById(R.id.btnSend);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected()) {
                    for (BluetoothDevice device : mRegisteredDevices) {
                        notifyDevice(device);
                    }
                } else {
                    showToast("There are no CONNECTED devices!");
                }
            }
        });
    }

    private void updateRegisteredDevices() {

        mConnectedDeviceList.clear();
        mConnectedDeviceList.addAll(mRegisteredDevices);
        mLeDeviceListAdapter.updateList(mConnectedDeviceList);

        // TODO Method 2 to get connected devices, might be returning more than devices that connected
//        mConnectedDeviceList = getBluetoothManager().getConnectedDevices(BluetoothGatt.GATT_SERVER);
//        mLeDeviceListAdapter.updateList(mConnectedDeviceList);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (isBluetoothEnabled()) {
            startServer();
            startAdvertising();
        } else {
            enableBluetooth();
        }

    }

    protected void onPause() {
        super.onPause();
        stopAdvertising();
        closeServer();
    }


    public void startAdvertising() {
        super.startAdvertising();
        invalidateOptionsMenu();
    }

    public void stopAdvertising() {
        super.stopAdvertising();
        invalidateOptionsMenu();
    }

    public void startServer() {
        GattServerCallback gattServerCallback = new GattServerCallback();
        startGATTServer(gattServerCallback);

        BluetoothGattService timeService = createTimeService();
        addGATTService(timeService);

        // TODO Adding 2nd service crashes sometimes on first run and always on second run after paused
        //        BluetoothGattService cardService = createCardService();
//        addGATTService(cardService);
    }


    public static BluetoothGattService createTimeService() {

        BluetoothGattService service = new BluetoothGattService(TIME_SERVICE,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);

        // Current Time characteristic
        BluetoothGattCharacteristic currentTime = new BluetoothGattCharacteristic(CURRENT_TIME,
                //Read-only characteristic, supports notifications
                BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE | BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY
                , BluetoothGattCharacteristic.PERMISSION_WRITE | BluetoothGattCharacteristic.PERMISSION_READ);

        BluetoothGattDescriptor configDescriptor = new BluetoothGattDescriptor(Constants.CLIENT_CONFIG,
                //Read/write descriptor
                BluetoothGattDescriptor.PERMISSION_WRITE | BluetoothGattDescriptor.PERMISSION_READ);

        // Add descriptor to Characteristic
        currentTime.addDescriptor(configDescriptor);
        service.addCharacteristic(currentTime);

        return service;
    }


    public static BluetoothGattService createCardService() {
        BluetoothGattService service = new BluetoothGattService(nordicUART,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);

        BluetoothGattCharacteristic nordicTX = new BluetoothGattCharacteristic(nordicUARTTX,
                //Read-only characteristic
                BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_WRITE);


        BluetoothGattDescriptor desc = new BluetoothGattDescriptor(CONFIG_DESCRIPTOR, 1);
        desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

        nordicTX.addDescriptor(desc);

        BluetoothGattCharacteristic nordicRX = new BluetoothGattCharacteristic(nordicUARTRX,
                //Read-only characteristic
                BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE | BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_WRITE | BluetoothGattCharacteristic.PERMISSION_READ);


//        service.addCharacteristic(nordicRX);
        service.addCharacteristic(nordicTX);

        return service;
    }

    @Override
    public void onItemClicked(View view, int position) {
        notifyDevice(mConnectedDeviceList.get(position));
    }

    /**
     * Sends a notification to device on a specific bluetooth GATT characteristic has been updated
     *
     * @param device The remote device to receive notification
     */
    private void notifyDevice(BluetoothDevice device) {
        if (device != null && isConnected()) {

            String message = etMessage.getText().toString();

            // TODO Notifies Central that listens changes on this characteristic

            // Time Service
            mBluetoothGattServer.getServices();
            BluetoothGattService service = mBluetoothGattServer.getService(TIME_SERVICE);
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(CURRENT_TIME);
            characteristic.setValue(message);

            // Nordic UART Service
//                    BluetoothGattService serviceCard = mBluetoothGattServer.getService(nordicUART);
//                    BluetoothGattCharacteristic characteristicCard = service.getCharacteristic(Constants.nordicUARTTX);
//                    characteristic.setValue(message);


            boolean isNotified = mBluetoothGattServer.notifyCharacteristicChanged(device, characteristic, true);
            if (isNotified) {
                logDataSent(message);
            }


        } else {
            Toast.makeText(PeripheralActivity.this, "BLE Connection is NOT Available", Toast.LENGTH_SHORT).show();
        }
    }


    private class GattServerCallback extends BluetoothGattServerCallback {

        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);

            // TODO  This is a Binder Thread, doing UI operations here causes silent Exception
            // IMPORTANT: App does not crash but handles exception silently here with a message

            String statusString = "UNKNOWN";

            switch (status) {
                case BluetoothGatt.GATT_SUCCESS:
                    statusString = "GATT_SUCCESS";
                    showToast("onConnectionStateChange() BluetoothGatt.GATT_SUCCESS");
                    break;

                case BluetoothGatt.GATT_FAILURE:
                    statusString = "GATT_FAILURE";
                    showToast("onConnectionStateChange() BluetoothGatt.GATT_FAILURE");
                    break;

                case BluetoothGatt.GATT_CONNECTION_CONGESTED:
                    statusString = "GATT_CONNECTION_CONGESTED";
                    showToast("onConnectionStateChange() BluetoothGatt.GATT_CONNECTION_CONGESTED");
                    break;

                case BluetoothGatt.GATT_READ_NOT_PERMITTED:
                    statusString = "GATT_READ_NOT_PERMITTED";
                    showToast("onConnectionStateChange() BluetoothGatt.GATT_READ_NOT_PERMITTED");
                    break;

                case BluetoothGatt.GATT_WRITE_NOT_PERMITTED:
                    statusString = "GATT_WRITE_NOT_PERMITTED";
                    showToast("onConnectionStateChange() BluetoothGatt.GATT_WRITE_NOT_PERMITTED");
                    break;

                case BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH:
                    statusString = "GATT_INVALID_ATTRIBUTE_LENGTH";
                    showToast("onConnectionStateChange() BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH");
                    break;
            }


            System.out.println("GattServerCallback onConnectionStateChange() device: " + device);

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                System.out.println("GattServerCallback onConnectionStateChange() STATE_CONNECTED device: " + device);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                System.out.println("GattServerCallback onConnectionStateChange() STATE_DISCONNECTED device: " + device);
            }


            // Log Connection State
            String stateString = "Unknown";
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    stateString = "STATE_CONNECTED";
                    mRegisteredDevices.add(device);
                    System.out.println("GattServerCallback onConnectionStateChange() STATE_CONNECTED device: " + device);
                    break;

                case BluetoothProfile.STATE_CONNECTING:
                    stateString = "STATE_CONNECTED";
                    break;

                case BluetoothProfile.STATE_DISCONNECTING:
                    stateString = "STATE_DISCONNECTING";
                    break;

                case BluetoothProfile.STATE_DISCONNECTED:
                    stateString = "STATE_DISCONNECTED";
                    mRegisteredDevices.remove(device);
                    break;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateRegisteredDevices();

                }
            });

            logStatus("onConnectionStateChange() newState: " + stateString + ", status: " + statusString + ", registered devices: " + mRegisteredDevices.size());

            setConnected(mRegisteredDevices.size() > 0);

        }

        /*
         *** CHARACTERISTIC REQUESTS ***
         */

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);

            System.out.println("onCharacteristicReadRequest() device: " + device + ", characteristic: " + characteristic);

            String response = "Hello #" + sentMesageCount;
            sentMesageCount++;

            byte[] field = new byte[10];

            try {
                field = response.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            characteristic.setValue(field);

            // TODO Sending response is required for Central to call onCharacteristicRead(), otherwise device timeouts and devices disconnect
            mBluetoothGattServer.sendResponse(device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    0,
                    characteristic.getValue());

            showToast("onCharacteristicReadRequest() device: " + device.getName() + ", value: "
                    + characteristic.getStringValue(0));

            logDataSent("onCharacteristicReadRequest() " + characteristic.getStringValue(0));
        }

        /*
         * This method gets called after client calls mBluetoothGatt.writeCharacteristic(characteristic).
         * An application should call sendResponse() to complete the request
         */
        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);

            // TODO Sending response is required for Central to call onCharacteristicWrite(), otherwise device timeouts and devices disconnect

            if (responseNeeded) {
                mBluetoothGattServer.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        0,
                        characteristic.getValue());
            }

            System.out.println("onCharacteristicWriteRequest() device: " + device + ", value: " + value);

            showToast("onCharacteristicWriteRequest() device: " + device.getName()
                    + ", value: " + value + ", responseNeeded: " + responseNeeded);

            try {
                String received = new String(value, "UTF-8");
                logDataReceived("onCharacteristicWriteRequest() " + received);

            } catch (UnsupportedEncodingException e) {
                logDataReceived("onCharacteristicWriteRequest() Error");
            }

        }

        /*
         *** DESCRIPTOR REQUESTS ***
         */

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
            super.onDescriptorReadRequest(device, requestId, offset, descriptor);

            System.out.println("GattServerCallback onDescriptorReadRequest() device: " + device.getName());
            showToast("onDescriptorReadRequest() device: " + device.getName());

            mBluetoothGattServer.sendResponse(device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    0,
                    null);

            if (CLIENT_CONFIG.equals(descriptor.getUuid())) {
                Log.d(TAG, "Config descriptor read");
                byte[] returnValue;
                if (mRegisteredDevices.contains(device)) {
                    returnValue = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                } else {
                    returnValue = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                }
                mBluetoothGattServer.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_FAILURE,
                        0,
                        returnValue);
            } else {
                Log.w(TAG, "Unknown descriptor read request");
                mBluetoothGattServer.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_FAILURE,
                        0,
                        null);
            }
        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);

            System.out.println("GattServerCallback onDescriptorWriteRequest() device: " + device.getName());
            showToast("onDescriptorWriteRequest() device: " + device);

            if (responseNeeded) {
                mBluetoothGattServer.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        0,
                        null);
            }
        }


        /*
         *** NOTIFICATION REQUEST ***
         */

        // Invoked after notifyCharacteristicChanged() is called
        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            super.onNotificationSent(device, status);
            System.out.println("onNotificationSent() device: " + device);
            showToast("onNotificationSent() device: " + device);
        }

        @Override
        public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
            super.onExecuteWrite(device, requestId, execute);
            System.out.println("onExecuteWrite() device: " + device);
            showToast("onExecuteWrite() device: " + device);
        }

        /*
         *** SERVICE ADDED ***
         */

        // This one is invoked when services are added to BluetoothGATTServer
        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            super.onServiceAdded(status, service);
            System.out.println("GattServerCallback onServiceAdded() service: " + service);
            showToast("onServiceAdded() service: " + service + ", status: " + status);
        }

        @Override
        public void onMtuChanged(BluetoothDevice device, int mtu) {
            super.onMtuChanged(device, mtu);
            System.out.println("GattServerCallback onServiceAdded() mtu: " + mtu);
            showToast("onMtuChanged() device: " + device + ", mtu: " + mtu);
        }

    }


    @Override
    protected void onAdvertisingStartSuccess(AdvertiseSettings settingsInEffect) {
        Log.d(TAG, "Peripheral advertising started.");
        setAdvertising(true);
        invalidateOptionsMenu();
        Toast.makeText(PeripheralActivity.this, "AdvertiseCallback onStartSuccess() " + settingsInEffect, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onAdvertisingStartFailure(int errorCode) {
        Log.d(TAG, "Peripheral advertising failed: " + errorCode);
        setAdvertising(false);
        invalidateOptionsMenu();
        Toast.makeText(PeripheralActivity.this, "AdvertiseCallback onStartFailure() " + errorCode, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);

        Toast.makeText(this, "onActiviyResult()", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_peripheral, menu);
        if (!isAdvertising()) {
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
                startServer();
                startAdvertising();
                break;
            case R.id.menu_stop:
                stopAdvertising();
                closeServer();
                break;
        }
        return true;
    }


    public void logStatus(String msg) {
        Log.d(TAG, msg);
        runOnUiThread(() -> tvConnectionStatus.setText(msg));
    }


    public void logDataSent(String msg) {
        Log.d(TAG, msg);
        runOnUiThread(() -> tvDataSent.setText(msg));
    }


    public void logDataReceived(String msg) {
        Log.d(TAG, msg);
        runOnUiThread(() -> tvDataReceived.setText(msg));
    }


    public void logError(String msg) {
        logStatus("Error: " + msg);

        runOnUiThread(() -> tvConnectionStatus.setText(msg));
    }

    public void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }
}
