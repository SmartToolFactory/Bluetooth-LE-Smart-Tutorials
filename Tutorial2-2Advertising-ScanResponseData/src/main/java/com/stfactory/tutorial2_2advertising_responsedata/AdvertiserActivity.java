package com.stfactory.tutorial2_2advertising_responsedata;

import android.app.Activity;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class AdvertiserActivity extends BluetoothLEActivity {

    private static final String TAG = AdvertiserActivity.class.getName();


    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;

    private boolean mAdvertising;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toast.makeText(this, "Advertising Support: " + isMultipleAdvertisementSupported(), Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onResume() {
        super.onResume();

        mBluetoothLeAdvertiser = getBluetoothAdapter().getBluetoothLeAdvertiser();

        startAdvertising();

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
    public void startAdvertising() {

        if (mBluetoothLeAdvertiser == null) {
            return;
        }


        // Advertising Settings
        AdvertiseSettings advertiseSettings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .build();


        ParcelUuid pUuid = new ParcelUuid(Constants.UUID_ADVERTISE_SERVICE);

        ParcelUuid pUuidRx = new ParcelUuid(Constants.UUID_ADVERTISE_RESPONSE_DATA);

        // Advertising Packet
        AdvertiseData advertiseData = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(false)
                .addServiceUuid(pUuid)
                .build();


        // TODO This is the scan response data that sends when a central makes a scan request

        final byte[] toSendData = new byte[7];
        toSendData[0] = 13;
        toSendData[1] = 6;
        toSendData[2] = 6;
        toSendData[3] = 6;
        toSendData[4] = 6;
        toSendData[5] = 6;
        toSendData[6] = 13;

        // Scan Response Packet
        AdvertiseData datatoSend = new AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .addServiceUuid(pUuid)
                .setIncludeTxPowerLevel(false)
                .addServiceData(pUuidRx, toSendData)
                .build();


        // Start advertising
        mBluetoothLeAdvertiser.startAdvertising(advertiseSettings, advertiseData, datatoSend, mAdvertiseCallback);

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


    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.d(TAG, "Peripheral advertising started.");
            mAdvertising = true;
            invalidateOptionsMenu();
            Toast.makeText(AdvertiserActivity.this, "Advertising started." + settingsInEffect, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.d(TAG, "Peripheral advertising failed: " + errorCode);
            mAdvertising = false;
            invalidateOptionsMenu();
            Toast.makeText(AdvertiserActivity.this, "Advertising failed: " + errorCode, Toast.LENGTH_SHORT).show();
        }
    };


}
