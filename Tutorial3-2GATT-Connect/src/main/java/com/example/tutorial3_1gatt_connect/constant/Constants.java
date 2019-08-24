package com.example.tutorial3_1gatt_connect.constant;

import java.util.UUID;

public class Constants {

    /*
          TOP UP UUIDs
   */
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


    /*
           PASS CORRIDOR UUIDs
     */
    public static UUID UUID_ADVERTISE_CORRIDOR = UUID.fromString("ba3fb882-4a0e-11e9-8646-d663bd873d93");
    public static UUID UUID_RESPONSE_CORRIDOR = UUID.fromString("cb254f86-4a0e-11e9-8646-d663bd873d93");


    // Beacon MAC Addresses
    public static final String MAC_ESTIMOTE_GREEN = "D7:F7:DD:D4:9C:37";
    public static final String MAC_ESTIMOTE_BLUE = "DF:AA:AD:47:5E:50";
    public static final String MAC_ESTIMOTE_PURPLE = "E7:7E:57:AF:C4:A0";

    public static final long SCAN_PERIOD = 5000;

}
