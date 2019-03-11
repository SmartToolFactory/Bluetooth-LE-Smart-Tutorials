package com.stfactory.tutorial5_1gatt_centralandperipheral;

import java.util.UUID;

public class Constants {

    /* Current Time Service UUID */
    public static UUID TIME_SERVICE = UUID.fromString("00001805-0000-1000-8000-00805f9b34fb");

    /* Mandatory Current Time Information Characteristic */
    public static UUID CURRENT_TIME = UUID.fromString("00002a2b-0000-1000-8000-00805f9b34fb");
    /* Mandatory Client Characteristic Config Descriptor */
    public static UUID CLIENT_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    /*ETS Service UUID*/
    public static final UUID nordicUART = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    /*ETS Characteristic UUID*/
    public static final UUID nordicUARTTX = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
    public static final UUID nordicUARTRX = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");

    public static final UUID UUID_ESTIMOTE_PURPLE1 = UUID.fromString("b9407f30-f5f8-466e-aff9-25556b57fe6d");

    public static final long SCAN_PERIOD = 5000;
}
