package com.hubble.registration;

import android.content.Context;
import android.util.Log;

import com.hubble.devcomm.Device;
import com.hubble.framework.service.p2p.P2pSessionSummary;
import com.localytics.android.Localytics;
import com.localytics.android.LocalyticsActivityLifecycleCallbacks;

import com.hubbleconnected.camera.BuildConfig;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import base.hubble.Models;
import base.hubble.SubscriptionWrapper;
import base.hubble.subscriptions.UserSubscription;

/**
 * Created by Sean on 9/9/2014.
 */
public class AnalyticsController {
  private static final String TAG = "AnalyticsController";
  private Context mContext;
  private static AnalyticsController instance;
  private static final String PROFILE_ATTRIBUTE_EMAIL_ADDRESS = "Email Address";
  private static final String PROFILE_ATTRIBUTE_PLAN_NAME = "Plan Name";
  private static final String PROFILE_ATTRIBUTE_NUM_OF_CAMERAS = "Number of Cameras Connected";

  final private static int CUSTOM_DIMENSION_0 = 0;//Subscription Type
  final private static int CUSTOM_DIMENSION_1 = 1;//Number of Cameras
  final private static int CUSTOM_DIMENSION_2 = 2;//Completed Free Trial
  final private static int CUSTOM_DIMENSION_3 = 3;//Offered a Free Trial Yet

  private int _numOfCameras = 0;

  /*
  * Must call this!
  */
  public static void init(Context context) {
    Log.d(TAG, "Init AnalyticsController");
    if (instance == null) {
      instance = new AnalyticsController(context);
    }
  }

  public static AnalyticsController getInstance() {
    if (instance == null) {
      Log.e(TAG, "instance is null! Ensure init(Context) has been set up in your Application.java");
    }
    //Log.d(TAG, "Getting AnalyticInstance: " + instance.toString());
    return instance;
  }

  private AnalyticsController(Context context) {
    mContext = context;
    Log.d(TAG, "created new Localytics Session");
    Localytics.integrate(context);
    //Localytics.setLoggingEnabled(true);
  }

  public LocalyticsActivityLifecycleCallbacks getLocalyticsLifecycleCallback() {
    Log.d(TAG, "get analytic lifecycle callback");
    return new LocalyticsActivityLifecycleCallbacks(mContext);
  }

  public void openMainSession(String projectNumber) {
    Log.d(TAG, "Register analytics for push with project #" + projectNumber);
    Localytics.registerPush(projectNumber);
  }

  public void setUser(String email) {
    Localytics.setCustomerId(email);
    Localytics.setCustomerEmail(email);
  }

  public void setUserPlan(Models.ApiResponse<SubscriptionWrapper> subscriptions){
    List<UserSubscription> userSubscriptions = subscriptions.getData().getPlans();
    if (userSubscriptions != null) {
      UserSubscription activeSubscription = findActiveSubscription(userSubscriptions);
      final String subscriptionPlanText;
      if (activeSubscription != null) {
        subscriptionPlanText = activeSubscription.getPlanId();
      } else {
        subscriptionPlanText = "None";
      }

      setSubscriptionType(subscriptionPlanText);
    }
  }

  public void setSubscriptionType(String planName) {
    Localytics.setCustomDimension(CUSTOM_DIMENSION_0, planName);
    setPlanNameAttribute(planName);
  }

  public void setNumOfCameras(int numOfCameras) {
    _numOfCameras = numOfCameras;
    Localytics.setCustomDimension(CUSTOM_DIMENSION_1, String.valueOf(numOfCameras));
  }

  public void onCameraAdded() {
    _numOfCameras ++;
    setNumOfCameras(_numOfCameras);
    Localytics.incrementProfileAttribute(PROFILE_ATTRIBUTE_NUM_OF_CAMERAS, 1, Localytics.ProfileScope.APPLICATION);
  }

  public void onCameraRemoved() {
    _numOfCameras --;
    if (_numOfCameras < 0) {
      _numOfCameras = 0;
    }
    setNumOfCameras(_numOfCameras);
    Localytics.decrementProfileAttribute(PROFILE_ATTRIBUTE_NUM_OF_CAMERAS, 1, Localytics.ProfileScope.APPLICATION);
  }

  public void setFreeTrialDimensions(Device device) {
    if (device!= null && device.getProfile() != null) {
      if (device.getProfile().getDeviceFreeTrial() == null) {
        setCompletedFreeTrial(false);
        setOfferedAFreeTrialYet(false);
      } else {
        setOfferedAFreeTrialYet(true);
        setCompletedFreeTrial(device.getProfile().getFreeTrialQuota() == 0);
      }
    }
  }

  public void setCompletedFreeTrial(boolean yes) {
    Localytics.setCustomDimension(CUSTOM_DIMENSION_2, yes ? "Yes" : "No");
  }

  public void setOfferedAFreeTrialYet(boolean yes) {
    Localytics.setCustomDimension(CUSTOM_DIMENSION_3, yes ? "Yes" : "No");
  }

  public void setPlanNameAttribute(String planName) {
    Localytics.setProfileAttribute(PROFILE_ATTRIBUTE_PLAN_NAME, planName, Localytics.ProfileScope.APPLICATION);
  }

  public void setUserEmail(String email) {
    Localytics.setProfileAttribute(PROFILE_ATTRIBUTE_EMAIL_ADDRESS, email, Localytics.ProfileScope.ORGANIZATION);
  }

  public void setNumOfCamerasAttribute(int numOfCameras) {
    _numOfCameras = numOfCameras;
    Localytics.setProfileAttribute(PROFILE_ATTRIBUTE_NUM_OF_CAMERAS, numOfCameras, Localytics.ProfileScope.APPLICATION);
  }

  public void trackScreen(EScreenName screen) {
    if (screen != null) {
      Log.d(TAG, "tag screen " + screen.getName());
      Localytics.tagScreen(screen.getName());
    }
  }

  //Events
  public void trackEvent(String category, String action, String label) {
    Map<String, String> values = new HashMap<String, String>();
    values.put(action, label);

    Log.d(TAG, "Track " + category);
    Localytics.tagEvent(category, values);
  }

  public void trackEvent(String event) {
    Log.d(TAG, "Track " + event);
    Localytics.tagEvent(event);
  }

  public void trackEvent(String category, Map<String, String> attributes) {
    Log.d(TAG, "Track " + category);
    Localytics.tagEvent(category, attributes);
  }

  private UserSubscription findActiveSubscription(List<UserSubscription> subscriptions) {
    for (UserSubscription sub : subscriptions) {
      if (sub.getState() != null && sub.getState().equals("active")) {
        return sub;
      }
    }
    return null;
  }

  public void sendP2pSessionSummary(P2pSessionSummary p2pSessionSummary) {
    // Just send p2p analytic info for Vtech
    if (BuildConfig.FLAVOR.equalsIgnoreCase("vtech")) {
      Log.d(TAG, "Send p2p session summary: " + p2pSessionSummary);
      if (p2pSessionSummary != null) {
        HashMap<String, String> values = new HashMap<>();
        values.put(P2pSessionSummary.ATTR_CONNECTION_TYPE, p2pSessionSummary.getConnectionType());
        values.put(P2pSessionSummary.ATTR_MOBILE_DATA, p2pSessionSummary.isMobileDataEnabled() ? "Yes" : "No");
        values.put(P2pSessionSummary.ATTR_SUCCESS, p2pSessionSummary.getIsSuccess());
        values.put(P2pSessionSummary.ATTR_FAILURE_REASON, p2pSessionSummary.getFailureReason());
        values.put(P2pSessionSummary.ATTR_CAMERA_MODEL, p2pSessionSummary.getCameraModel());
        values.put(P2pSessionSummary.ATTR_CAMERA_FW_VERSION, p2pSessionSummary.getFwVersion());
        values.put(P2pSessionSummary.ATTR_CAMERA_P2P_VERSION, p2pSessionSummary.getP2pVersion());
        Localytics.tagEvent(P2pSessionSummary.EVENT_NAME, values);
      }
    }
  }
}
