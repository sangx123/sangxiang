package com.nest.common;

/**
 * Created by dasari on 30/01/17.
 */

public class NestDevice {

    private String mName;
    private String mDeviceID;
    private DEVICE_TYPE mDeviceType;

    public enum DEVICE_TYPE {
        CAMERA, THERMOSTAT, SMOKE_DETECTOR
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getDeviceID() {
        return mDeviceID;
    }

    public void setDeviceID(String mDeviceID) {
        this.mDeviceID = mDeviceID;
    }

    public DEVICE_TYPE getDeviceType() {
        return mDeviceType;
    }

    public void setDeviceType(DEVICE_TYPE mDeviceType) {
        this.mDeviceType = mDeviceType;
    }
}
