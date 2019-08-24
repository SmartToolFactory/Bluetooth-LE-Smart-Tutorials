package com.example.myapplication;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.scan.ScanSettings;

import java.text.DecimalFormat;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import uk.co.alt236.bluetoothlelib.device.BluetoothLeDevice;
import uk.co.alt236.bluetoothlelib.device.beacon.BeaconType;
import uk.co.alt236.bluetoothlelib.device.beacon.BeaconUtils;
import uk.co.alt236.bluetoothlelib.device.beacon.ibeacon.IBeaconDevice;

public class MainActivity extends AppCompatActivity {

    private static final String MAC_ESTIMOTE_GREEN = "D7:F7:DD:D4:9C:37";
    private static final String MAC_ESTIMOTE_BLUE = "DF:AA:AD:47:5E:50";
    private static final String MAC_ESTIMOTE_PURPLE = "E7:7E:57:AF:C4:A0";

    public static final int REQUEST_LOCATION = 2;
    private RxBleClient rxBleClient;
    private Disposable scanSubscription;

    private DecimalFormat decimalFormat = new DecimalFormat("#0.00");

    private boolean isCentralReady = true;

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial7_1_rx_ble1);

        textView = findViewById(R.id.textBLE);

        rxBleClient = RxBleClient.create(this);

        checkBluetoothAndPermissonStatus();

    }


    private void checkBluetoothAndPermissonStatus() {
        rxBleClient.observeStateChanges()
                .switchMap(state -> { // switchMap makes sure that if the state will change the rxBleClient.scanBleDevices() will dispose and thus end the scan
                    switch (state) {

                        case READY:
                            // everything should work
                            Toast.makeText(this, "BLE READY", Toast.LENGTH_SHORT).show();
                        case BLUETOOTH_NOT_AVAILABLE:
                            // basically no functionality will work here
                            Toast.makeText(this, "BLUETOOTH_NOT_AVAILABLE", Toast.LENGTH_SHORT).show();
                            textView.setText("BLUETOOTH_NOT_AVAILABLE");

                        case LOCATION_PERMISSION_NOT_GRANTED:
                            // scanning and connecting will not work
                            Toast.makeText(this, "LOCATION_PERMISSION_NOT_GRANTED", Toast.LENGTH_SHORT).show();
                            textView.setText("LOCATION_PERMISSION_NOT_GRANTED");
                        case BLUETOOTH_NOT_ENABLED:
                            // scanning and connecting will not work
                            Toast.makeText(this, "BLUETOOTH_NOT_ENABLED", Toast.LENGTH_SHORT).show();
                            textView.setText("BLUETOOTH_NOT_ENABLED");

                        case LOCATION_SERVICES_NOT_ENABLED:
                            // scanning will not work
                            Toast.makeText(this, "LOCATION_SERVICES_NOT_ENABLED", Toast.LENGTH_SHORT).show();
                            textView.setText("LOCATION_SERVICES_NOT_ENABLED");

                        default:
                            textView.setText("DEFAULT");
                            return Observable.empty();
                    }
                })
                .subscribe(
                        rxBleScanResult -> {
                            // Process scan result here.
                        },
                        throwable -> {
                            // Handle an error here.
                        }
                );
    }

    private void scanBLEDevices() {


        scanSubscription = rxBleClient.scanBleDevices(
                new ScanSettings.Builder()
                        // .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY) // change if needed
                        // .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES) // change if needed
                        .build()
                // add filters if needed
        )
                .subscribe(

                        scanResult -> {
                            // Process scan result here.

                            RxBleDevice rxBleDevice = scanResult.getBleDevice();

                            BluetoothDevice bluetoothDevice = rxBleDevice.getBluetoothDevice();

                            final BluetoothLeDevice deviceLe
                                    = new BluetoothLeDevice(bluetoothDevice, scanResult.getRssi(), scanResult.getScanRecord().getBytes(), System.currentTimeMillis());

                            if (BeaconUtils.getBeaconType(deviceLe) == BeaconType.IBEACON) {
                                IBeaconDevice beaconDevice = new IBeaconDevice(deviceLe);
                                System.out.println("😜 Beacon: " + beaconDevice.getAddress() + ", " + decimalFormat.format(beaconDevice.getAccuracy()) + "m, thread: " + Thread.currentThread().getName());

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        textView.setText("😜 Beacon: " + beaconDevice.getAddress() + ", " + decimalFormat.format(beaconDevice.getAccuracy()) + "m, thread: " + Thread.currentThread().getName());
                                    }
                                });

                                if (isCentralReady) {

                                    boolean userInBeaconRange = false;
                                    double beaconProxmity = beaconDevice.getAccuracy();

                                    if (beaconProxmity < 1) {
                                        userInBeaconRange = true;
                                    }


                                    if (userInBeaconRange) {

                                        isCentralReady = false;
                                        Toast.makeText(MainActivity.this, "Welcome.", Toast.LENGTH_SHORT).show();


                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                isCentralReady = true;
                                                Toast.makeText(MainActivity.this, "User device is eligible for proximity beacon.", Toast.LENGTH_SHORT).show();
                                            }
                                        }, 10_000);
                                    }

                                }
                            }


                        },
                        throwable -> {
                            // Handle an error here.
                        }
                );

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
    protected void onResume() {
        super.onResume();
        if (isLocationPermissionGranted()) {
            scanBLEDevices();
        } else {
            requestLocationPermission();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

        // When done, just dispose.
        if (scanSubscription != null) scanSubscription.dispose();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            scanBLEDevices();
        }
    }


}
