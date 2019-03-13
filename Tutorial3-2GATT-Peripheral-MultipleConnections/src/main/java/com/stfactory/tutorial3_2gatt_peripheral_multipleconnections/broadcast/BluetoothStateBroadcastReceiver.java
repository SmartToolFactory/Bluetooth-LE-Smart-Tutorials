package com.stfactory.tutorial3_2gatt_peripheral_multipleconnections.broadcast;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class BluetoothStateBroadcastReceiver extends BroadcastReceiver {


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
}
