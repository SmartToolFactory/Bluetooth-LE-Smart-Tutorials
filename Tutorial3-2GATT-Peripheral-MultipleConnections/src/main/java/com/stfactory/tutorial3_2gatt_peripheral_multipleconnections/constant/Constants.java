package com.stfactory.tutorial3_2gatt_peripheral_multipleconnections.constant;

import java.util.UUID;

public class Constants {

    // TODO These are from Time Service demo
    public static UUID TIME_SERVICE = UUID.fromString("00001805-0000-1000-8000-00805f9b34fb");
    /* Mandatory Current Time Information Characteristic */
    public static UUID CURRENT_TIME = UUID.fromString("00002a2b-0000-1000-8000-00805f9b34fb");

    /* Mandatory Client Characteristic Config Descriptor */
    public static UUID CLIENT_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");


    /* Service UUIDs */
    public static final UUID nordicUART = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    /* Notify-Write Characteristic*/
    public static final UUID nordicUARTTX = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
    /* Descriptor for nordicUARTTX */
    public static UUID CONFIG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    /* Read-Write-Write No Response Characteristic */
    public static final UUID nordicUARTRX = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");

    /* Advertisement UUIDs */
    public static UUID UUID_ADVERTISE_SERVICE = UUID.fromString("5A412301-A4A1-E192-F1A6-E55A23DAAA6C");
    public static UUID UUID_ADVERTISE_RESPONSE_DATA = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");

    public static final long SCAN_PERIOD = 5000;

}
