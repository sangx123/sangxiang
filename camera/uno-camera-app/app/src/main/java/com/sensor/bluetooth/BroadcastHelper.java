package com.sensor.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;


public class BroadcastHelper {

  public static void update(final String action, Context mContext) {
    final Intent intent = new Intent(action);
    mContext.sendBroadcast(intent);
  }

  public static void update(final String action, final String address, final int status, Context mContext) {
    final Intent intent = new Intent(action);
    intent.putExtra(IntentAction.EXTRA_ADDRESS, address);
    intent.putExtra(IntentAction.EXTRA_STATUS, status);
    mContext.sendBroadcast(intent);
  }

  public static void update(String action, BluetoothDevice device, int rssi, byte[] scanRecord, Context mContext) {
    Intent intent = new Intent(action);
    intent.putExtra(IntentAction.EXTRA_ADDRESS, device.getAddress());
    intent.putExtra(IntentAction.EXTRA_RSSI, rssi);
    intent.putExtra(IntentAction.EXTRA_DATA, scanRecord);
    mContext.sendBroadcast(intent);
  }

  public static void update(String action, BluetoothGattCharacteristic characteristic, int status, Context mContext) {
    final Intent intent = new Intent(action);
    intent.putExtra(IntentAction.EXTRA_UUID, characteristic.getUuid().toString());
    intent.putExtra(IntentAction.EXTRA_DATA, characteristic.getValue());
    intent.putExtra(IntentAction.EXTRA_STATUS, status);
    mContext.sendBroadcast(intent);
  }


}