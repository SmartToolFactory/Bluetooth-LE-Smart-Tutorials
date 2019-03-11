package com.stfactory.tutorial3_1gatt_peripheral.constant;

import java.util.UUID;

public class Constants {

    public static String SERVICE_STRING = "7D2EA28A-F7BD-485A-BD9D-92AD6ECFE93E";
    public static UUID SERVICE_UUID = UUID.fromString(SERVICE_STRING);

    public static String CHARACTERISTIC_ECHO_STRING = "7D2EBAAD-F7BD-485A-BD9D-92AD6ECFE93E";
    public static UUID CHARACTERISTIC_ECHO_UUID = UUID.fromString(CHARACTERISTIC_ECHO_STRING);

    // TODO These are from Time Service demo
    public static UUID TIME_SERVICE = UUID.fromString("00001805-0000-1000-8000-00805f9b34fb");
    /* Mandatory Current Time Information Characteristic */
    public static UUID CURRENT_TIME = UUID.fromString("00002a2b-0000-1000-8000-00805f9b34fb");

    /* Optional Local Time Information Characteristic */
    public static UUID LOCAL_TIME_INFO = UUID.fromString("00002a0f-0000-1000-8000-00805f9b34fb");

    /* Mandatory Client Characteristic Config Descriptor */
    public static UUID CLIENT_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    /* Advertisement UUIDs */
    public static UUID UUID_ADVERTISE_SERVICE = UUID.fromString("5A412301-A4A1-E192-F1A6-E55A23DAAA6C");
    public static UUID UUID_ADVERTISE_RESPONSE_DATA = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");



}
