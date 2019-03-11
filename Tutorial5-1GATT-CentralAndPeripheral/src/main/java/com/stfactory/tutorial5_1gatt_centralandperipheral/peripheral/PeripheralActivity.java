package com.stfactory.tutorial5_1gatt_centralandperipheral.peripheral;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
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

import com.stfactory.tutorial5_1gatt_centralandperipheral.Constants;
import com.stfactory.tutorial5_1gatt_centralandperipheral.R;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.stfactory.tutorial5_1gatt_centralandperipheral.Constants.nordicUART;
import static com.stfactory.tutorial5_1gatt_centralandperipheral.Constants.nordicUARTRX;
import static com.stfactory.tutorial5_1gatt_centralandperipheral.Constants.nordicUARTTX;

public class PeripheralActivity extends AppCompatActivity {

    private static final String TAG = PeripheralActivity.class.getName();


    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_LOCATION = 2;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private BluetoothGattServer mGattServer;

    private boolean mAdvertising;

    private boolean mConnected = false;

    private int sentMesageCount;

    private String connectionStatus = "Not connected";

    private String message;

    private TextView tvConnectionStatus, tvDataSent, tvDataReceived;
    private EditText etMessage;


    /* Collection of notification subscribers */
    private Set<BluetoothDevice> mRegisteredDevices = new HashSet<>();

    // Device connected to peripheral
    private BluetoothDevice mRegisteredDevice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peripheral);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvConnectionStatus = findViewById(R.id.tvConnectionStatus);

        tvDataSent = findViewById(R.id.tvDataSent);
        tvDataReceived = findViewById(R.id.tvDataReceived);

        etMessage = findViewById(R.id.etMessage);
        Button btnSend = findViewById(R.id.btnWrite);


        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mConnected) {

                    if (mRegisteredDevices == null) {
                        Toast.makeText(PeripheralActivity.this, "Device is NULL", Toast.LENGTH_SHORT).show();
                    }

                    String message = etMessage.getText().toString();

                    // TODO Notifies Central that listens changes on this characteristic
                    BluetoothGattService service = mGattServer.getService(Constants.TIME_SERVICE);

                    BluetoothGattCharacteristic characteristic = null;
                    if (service != null) {
                        characteristic = service.getCharacteristic(Constants.CURRENT_TIME);
                    } else {
                        service = mGattServer.getServices().get(0);
                        characteristic = service.getCharacteristics().get(0);
                    }

                    if (characteristic != null) {
                        characteristic.setValue(message);

                        for (BluetoothDevice device : mRegisteredDevices) {
                            boolean isNotified = mGattServer.notifyCharacteristicChanged(device, characteristic, true);
                            if (isNotified) {
                                logDataSent(message);
                            }
                        }
                    } else {
                        showToast("Service or Characteristic does not EXIST!");
                    }

//                    boolean isNotified = mGattServer.notifyCharacteristicChanged(mRegisteredDevice, characteristic, true);
//
//                    if (isNotified) {
//                        logDataSent(message);
//                    }

                } else {
                    Toast.makeText(PeripheralActivity.this, "BLE Connection is NOT Available", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Use this check to determine whether BLE is supported on the device.
        // Then you can selectively disable BLE-related features.
        isBluetoothLESupported();

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        Toast.makeText(this, "onCreate() isMultipleAdvertisementSupported(): " + mBluetoothAdapter.isMultipleAdvertisementSupported(), Toast.LENGTH_SHORT).show();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

    }


    private void isBluetoothLESupported() {

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();

//        Toast.makeText(this, "onResume()", Toast.LENGTH_SHORT).show();

        startServer();
        startAdvertising();

    }

    @Override
    protected void onPause() {
        super.onPause();
//        Toast.makeText(this, "onPause()", Toast.LENGTH_SHORT).show();

    }

    /**
     * Start advertising data with specified settings. There are 3 components are required for simple advertising.
     *
     * <li>
     * <b>AdvertiseSettings</b> to set features of advertising such as timout, power level and connectability
     * </li>
     *
     * <li>
     * <b>AdvertiseData</b> to be send to clients
     * </li>
     *
     * <li>
     * <b>AdvertiseCallback </b> to get events on {@link AdvertiseCallback#onStartFailure(int)}
     * or {@link AdvertiseCallback#onStartSuccess(AdvertiseSettings)} events
     * </li>
     */
    private void startAdvertising() {
        if (mBluetoothLeAdvertiser == null) {
            return;
        }

//        Toast.makeText(this, "startAdvertising()", Toast.LENGTH_SHORT).show();

        // Advertising Settings
        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
                .build();

        ParcelUuid parcelUuid = new ParcelUuid(Constants.TIME_SERVICE);

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

    private void startServer() {

        GattServerCallback gattServerCallback = new GattServerCallback();
        mGattServer = mBluetoothManager.openGattServer(this, gattServerCallback);

        // TODO Adding second service here results an error
//        BluetoothGattService service = createTimeService();
//        mGattServer.addService(service);

        BluetoothGattService serviceCard = createCardService();
        mGattServer.addService(serviceCard);

    }


    public static BluetoothGattService createTimeService() {

        BluetoothGattService service = new BluetoothGattService(Constants.TIME_SERVICE,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);

        // Current Time characteristic
        BluetoothGattCharacteristic currentTime = new BluetoothGattCharacteristic(Constants.CURRENT_TIME,
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

        UUID CONFIG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

        BluetoothGattDescriptor desc = new BluetoothGattDescriptor(CONFIG_DESCRIPTOR, 1);
        desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

        nordicTX.addDescriptor(desc);

        BluetoothGattCharacteristic nordicRX = new BluetoothGattCharacteristic(nordicUARTRX,
                //Read-only characteristic
                BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE | BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_WRITE | BluetoothGattCharacteristic.PERMISSION_READ);




        service.addCharacteristic(nordicRX);
//        service.addCharacteristic(nordicTX);

        return service;
    }

    private void stopServer() {
        if (mGattServer != null) {
            mGattServer.close();
        }
    }


    private class GattServerCallback extends BluetoothGattServerCallback {

        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);

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


            //  This is Binder Thread
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

            // TODO Sending response is required for Central to call onCharacteristicRead(), otherwise device timeouts and devices disconnect
            mGattServer.sendResponse(device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    0,
                    field);

            showToast("onCharacteristicReadRequest() device: " + device.getName() + ", value: "
                    + characteristic.getStringValue(0) + ", characteristic: " + characteristic);

            logDataSent("onCharacteristicReadRequest() " + response);
        }

        /*
         * This method gets called after client calls mBluetoothGatt.writeCharacteristic(characteristic).
         * An application should call sendResponse() to complete the request
         */
        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);

            // TODO Sending response is required for Central to call onCharacteristicWrite(), otherwise device timeouts and devices disconnect
            mGattServer.sendResponse(device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    0,
                    null);


            System.out.println("onCharacteristicWriteRequest() device: " + device + ", value: " + value);


            String received = "";

            try {
                received = new String(value, "UTF-8");
                logDataReceived("onCharacteristicWriteRequest()" + received);


            } catch (UnsupportedEncodingException e) {
                logDataReceived("onCharacteristicWriteRequest() Error");
            }

            showToast("onCharacteristicWriteRequest() device: " + device.getName()
                    + ", value: " + received + ", responseNeeded: " + responseNeeded + ", characteristic: " + characteristic);

        }

        /*
         *** DESCRIPTOR REQUESTS ***
         */

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
            super.onDescriptorReadRequest(device, requestId, offset, descriptor);

            System.out.println("GattServerCallback onDescriptorReadRequest() device: " + device.getName());
            showToast("onDescriptorReadRequest() device: " + device.getName());
        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);

            System.out.println("GattServerCallback onDescriptorWriteRequest() device: " + device.getName());
            showToast("onDescriptorWriteRequest() device: " + device);
        }


        /*
         *** NOTIFICATION REQUESTS ***
         */

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

        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            super.onServiceAdded(status, service);
            System.out.println("GattServerCallback onServiceAdded() service: " + service);

        }

    }


    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.d(TAG, "Peripheral advertising started.");
            mAdvertising = true;
            invalidateOptionsMenu();
            Toast.makeText(PeripheralActivity.this, "AdvertiseCallback onStartSuccess() " + settingsInEffect, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.d(TAG, "Peripheral advertising failed: " + errorCode);
            mAdvertising = false;
            invalidateOptionsMenu();
            Toast.makeText(PeripheralActivity.this, "AdvertiseCallback onStartFailure() " + errorCode, Toast.LENGTH_SHORT).show();
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

        Toast.makeText(this, "onActiviyResult()", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_peripheral, menu);
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
                startServer();
                startAdvertising();
                break;
            case R.id.menu_stop:
                stopAdvertising();
                stopServer();
                break;
        }
        return true;
    }


    public void setConnected(boolean connected) {
        mConnected = connected;
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
