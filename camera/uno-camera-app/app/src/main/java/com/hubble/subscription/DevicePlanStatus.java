package com.hubble.subscription;

import base.hubble.meapi.Device;

/**
 * Created by Admin on 30-01-2017.
 */
public class DevicePlanStatus {

	private String mCameraName;
	private String mRegistrationId;
	private int mPlanType;
	private String mPlanName;
	private String mExpiryDate;

	public DevicePlanStatus(String cameraName, String registrationId, int planType, String planName){
		mCameraName = cameraName;
		mRegistrationId = registrationId;
		mPlanType = planType;
		mPlanName = planName;
	}

	public String getCameraName() {
		return mCameraName;
	}

	public void setCameraName(String cameraName) {
		mCameraName = cameraName;
	}

	public String getRegistrationId() {
		return mRegistrationId;
	}

	public void setRegistrationId(String registrationId) {
		mRegistrationId = registrationId;
	}

	public int getPlanType() {
		return mPlanType;
	}

	public void setPlanType(int planType) {
		mPlanType = planType;
	}

	public String getPlanName() {
		return mPlanName;
	}

	public void setPlanName(String planName) {
		mPlanName = planName;
	}

	public String getExpiryDate() {
		return mExpiryDate;
	}

	public void setExpiryDate(String expiryDate) {
		mExpiryDate = expiryDate;
	}

}
