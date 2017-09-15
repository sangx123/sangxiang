package com.sensor.ui;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;

/**
 * Created by hoang on 10/24/15.
 */
public interface ISensorCommunicationListener {
  void onDeviceDiscovered (BluetoothDevice device, int rssi, byte[] record);
  void onGattConnected (String deviceAddress, int status);
  void onGattDisconnected (String deviceAddress, int status);
  void onGattServiceDiscovered (String deviceAddress, int status);
  void onCharacteristicChanged (BluetoothGattCharacteristic characteristic);
  void onCharacteristicRead (BluetoothGattCharacteristic characteristic, int status);
  void onCharacteristicWrite (BluetoothGattCharacteristic characteristic, int status);
  void onDeviceNotDiscovered ();
}
