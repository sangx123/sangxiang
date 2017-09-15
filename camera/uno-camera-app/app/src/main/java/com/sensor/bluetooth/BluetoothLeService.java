package com.sensor.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.sensor.constants.ProfileInfo;
import com.sensor.ui.ISensorCommunicationListener;

import java.util.UUID;


public class BluetoothLeService extends Service {

    private static final String TAG = "BluetoothLeService";
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    protected Handler mHandler = new Handler();
    private BluetoothGatt mBluetoothGatt;
    private ISensorCommunicationListener mSensorCommListener;

    private static final int BLE_SCAN_INTERVAL = 8000;
    private int numberOfTimesOfScan = 0;

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        Log.d(TAG, "Service is in oncreate");
        initialize();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    public boolean initialize() {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                return false;
            }
        }
        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = mBluetoothManager.getAdapter();
            if (mBluetoothAdapter == null) {
                return false;
            }
        }

        return true;
    }

    public void scanLeDevice(final boolean enable) {
        mSensorCommListener = null;
        if (enable) {
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            mHandler.postDelayed(reScanRunnable, BLE_SCAN_INTERVAL);
            numberOfTimesOfScan++;
        } else {
            mHandler.removeCallbacks(reScanRunnable);
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    public void startLeScanning(final boolean enable, ISensorCommunicationListener listener) {
        mSensorCommListener = listener;
        if (enable) {
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            mHandler.postDelayed(reScanRunnable, BLE_SCAN_INTERVAL);
            numberOfTimesOfScan++;
        } else {
            mHandler.removeCallbacks(reScanRunnable);
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    public void stopLeScanning() {
        mHandler.removeCallbacks(reScanRunnable);
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
    }

    private Runnable reScanRunnable = new Runnable() {
        @Override
        public void run() {
            if (numberOfTimesOfScan < 4) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                Log.i(TAG, "Restart BLE scanning...");
                if (mSensorCommListener == null) {
                    scanLeDevice(true);
                } else {
                    startLeScanning(true, mSensorCommListener);
                }
            } else {
                numberOfTimesOfScan = 0;
                if (mSensorCommListener == null) {
                    scanLeDevice(false);
                } else {
                    stopLeScanning();
                }
                BroadcastHelper.update(com.sensor.bluetooth.IntentAction.ACTION_DEVICE_NOT_DISCOVERED, getApplicationContext());
                if (mSensorCommListener != null) {
                    mSensorCommListener.onDeviceNotDiscovered();
                }
            }
        }
    };

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            BroadcastHelper.update(IntentAction.ACTION_DEVICE_DISCOVERED, device, rssi, scanRecord, getApplicationContext());
            if (mSensorCommListener != null) {
                mSensorCommListener.onDeviceDiscovered(device, rssi, scanRecord);
            }
        }

    };

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            BluetoothDevice device = gatt.getDevice();
            String address = device.getAddress();
            mBluetoothGatt = gatt;

            if (mBluetoothGatt == null) {
                Log.i(TAG, "OnConnectionStateChange, mBluetoothGatt is null");
                return;
            }
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "OnConnectionStateChange, status? STATE_CONNECTED");
                // Attempts to discover services after successful connection.
                mBluetoothGatt.discoverServices();
                BroadcastHelper.update(IntentAction.ACTION_GATT_CONNECTED, address, status, getApplicationContext());
                if (mSensorCommListener != null) {
                    mSensorCommListener.onGattConnected(address, status);
                }

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "OnConnectionStateChange, status? STATE_DISCONNECTED");
                BroadcastHelper.update(IntentAction.ACTION_GATT_DISCONNECTED, address, status, getApplicationContext());
                if (mSensorCommListener != null) {
                    mSensorCommListener.onGattDisconnected(address, status);
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            BluetoothDevice device = gatt.getDevice();
            BroadcastHelper.update(IntentAction.ACTION_GATT_SERVICES_DISCOVERED, device.getAddress(), status, getApplicationContext());
            if (mSensorCommListener != null) {
                mSensorCommListener.onGattServiceDiscovered(device.getAddress(), status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            Log.i(TAG, "received onCharacteristicRead");
            BroadcastHelper.update(IntentAction.ACTION_DATA_READ, characteristic, status, getApplicationContext());
            if (mSensorCommListener != null) {
                mSensorCommListener.onCharacteristicRead(characteristic, status);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i(TAG, "received onCharacteristicWrite");
            BroadcastHelper.update(IntentAction.ACTION_DATA_WRITE, characteristic, status, getApplicationContext());
            if (mSensorCommListener != null) {
                mSensorCommListener.onCharacteristicWrite(characteristic, status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.i(TAG, "received onCharacteristicChanged");
            BroadcastHelper.update(IntentAction.ACTION_DATA_NOTIFY, characteristic, BluetoothGatt.GATT_SUCCESS, getApplicationContext());
            if (mSensorCommListener != null) {
                mSensorCommListener.onCharacteristicChanged(characteristic);
            }
        }
    };

    public boolean connect(final String address) {
        Log.d(TAG, "connect to BLE service");
        if (mBluetoothAdapter == null || address == null) {
            return false;
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.d(TAG, "connect device null");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, true, mGattCallback);
        return true;
    }

    public void disconnect() {
        Log.d(TAG, "disconnect to BLE service");
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }
        mSensorCommListener = null;
        mBluetoothGatt.disconnect();
    }

    /*
    * Write particular profile to Gecko
    */
    public void writeProfile(int profileType) {

        if (mBluetoothGatt != null) {
            BluetoothGattService geckoService = mBluetoothGatt.getService(GattInfo.GECKO_PROFILE);
            if (geckoService == null) {
                return;
            }

            BluetoothGattCharacteristic geckoCharacteristic = geckoService.getCharacteristic(GattInfo.GECKO_PROFILE_ALERT_CONFIGUTATION);
            if (geckoCharacteristic == null) {
                return;
            }

            boolean status = false;
            byte[] value = new byte[2];
            value[0] = (byte) profileType;
            if (profileType == ProfileInfo.MOTION) {
                value[1] = 0x04;
            }

            geckoCharacteristic.setValue(value);
            geckoCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            status = mBluetoothGatt.writeCharacteristic(geckoCharacteristic);

            if (!status) {
                writeProfileRunnable.setData(profileType);
                mHandler.postDelayed(writeProfileRunnable, 100);
            }
        }
    }

    public class WriteProfileRunnable implements Runnable {
        private int profile;

        public void setData(int _profile) {
            profile = _profile;
        }

        public void run() {
            writeProfile(profile);
        }
    }

    private WriteProfileRunnable writeProfileRunnable = new WriteProfileRunnable();

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public boolean readFirmwareVersion() {

        boolean status = false;
        if (mBluetoothGatt != null) {
            BluetoothGattService deviceInfoService = mBluetoothGatt.getService(GattInfo.DEVICE_INFORMATION);

            if (deviceInfoService == null) {
                Log.i(TAG, "readFirmwareVersion, deviceInfoService null");
                return false;
            }

            BluetoothGattCharacteristic firmwareCharacteristic = deviceInfoService.getCharacteristic(GattInfo.FIRMWARE_VERSION_UUID);
            if (firmwareCharacteristic == null) {
                Log.i(TAG, "readFirmwareVersion, firmwareCharacteristic null");
                return false;
            }

            status = mBluetoothGatt.readCharacteristic(firmwareCharacteristic);
        }
        return status;
    }

    public boolean readData(UUID serviceUuid, UUID characteristicUuid) {
        boolean status = false;
        if (mBluetoothGatt != null) {
            BluetoothGattService deviceInfoService = mBluetoothGatt.getService(serviceUuid);

            if (deviceInfoService == null) {
                Log.i(TAG, "readData, deviceInfoService null");
                return false;
            }

            BluetoothGattCharacteristic firmwareCharacteristic = deviceInfoService.getCharacteristic(characteristicUuid);
            if (firmwareCharacteristic == null) {
                Log.i(TAG, "readData, characteristic null");
                return false;
            }
            status = mBluetoothGatt.readCharacteristic(firmwareCharacteristic);
            Log.i(TAG, "Read data from BLE device: status? " + status);
        }
        return status;
    }

    /*
    * Write particular profile to Gecko
    */
    public void writeData(UUID serviceUuid, UUID characteristicUuid, int dataValue) {

        if (mBluetoothGatt != null) {
            BluetoothGattService geckoService = mBluetoothGatt.getService(serviceUuid);
            if (geckoService == null) {
                Log.i(TAG, "writeData, geckoService null");
                return;
            }

            BluetoothGattCharacteristic geckoCharacteristic = geckoService.getCharacteristic(characteristicUuid);
            if (geckoCharacteristic == null) {
                Log.i(TAG, "writeData, geckoCharacteristic null");
                return;
            }

            boolean status = false;
            byte[] value = new byte[2];
            value[0] = (byte) 0x02;
            value[1] = (byte) dataValue;

            geckoCharacteristic.setValue(value);
            geckoCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            status = mBluetoothGatt.writeCharacteristic(geckoCharacteristic);
            Log.i(TAG, "Write data to BLE device: status? " + status);
            if (!status) {
                writeProfileRunnable.setData(dataValue);
                mHandler.postDelayed(writeProfileRunnable, 100);
            }
        }
    }
}
