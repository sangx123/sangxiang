package com.sensor.ui;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TextView;

import com.hubble.devcomm.Device;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.framework.service.analytics.AnalyticsInterface;
import com.hubble.framework.service.analytics.EventData;
import com.hubble.framework.service.analytics.GeAnalyticsInterface;
import com.hubble.framework.service.analytics.zaius.ZaiusEventManager;
import com.hubble.registration.PublicDefine;
import com.hubble.subscription.PlanFragment;
import com.hubble.ui.GalleryFragment;
import com.hubble.util.LogZ;

import com.nxcomm.blinkhd.ui.CameraListFragment;
import com.util.AppEvents;
import com.zaius.androidsdk.ZaiusEvent;
import com.zaius.androidsdk.ZaiusException;

import java.util.List;

import base.hubble.database.DeviceProfile;
import com.hubbleconnected.camera.R;

public class DeviceTabSupportFragment extends Fragment {

  private Activity activity;
  private FragmentTabHost mTabHost;
  private boolean mIsNotificationTab = false;
  private boolean mIsGalleryTab=false;
  private boolean mIsPlanTab=false;
  private EventData eventData;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    eventData =  new EventData();
    mTabHost = new FragmentTabHost(activity);
    mTabHost.setup(activity, getChildFragmentManager(), R.id.main_view_holder);

    mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.tab_cameras)).setIndicator(getString(R.string.tab_cameras),
            activity.getResources().getDrawable(R.drawable.camera)), CameraListFragment.class, null);

    mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.videos)).setIndicator(getString(R.string.tab_videos),
            activity.getResources().getDrawable(R.drawable.camera)), GalleryFragment.class, null);

    mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.tab_notifications)).setIndicator(getString(R.string.tab_notifications),
            activity.getResources().getDrawable(R.drawable.camera)), NotificationFragment.class, null);

    mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.tab_plans)).setIndicator(getString(R.string.tab_plans),
            activity.getResources().getDrawable(R.drawable.camera)), PlanFragment.class, null);


    // check for hiding tab Sensor
    List<Device> devices = DeviceSingleton.getInstance().getDevices();
    if (devices == null || devices.size() == 0) {
      new Thread(new Runnable() {
        @Override
        public void run() {
          long startTime = System.currentTimeMillis();
          try {
            List<Device> devices = null;
            do {
              try {
                Thread.sleep(2000);
              } catch (InterruptedException e) {
              }
              devices = DeviceSingleton.getInstance().getDevices();
            }
            while (System.currentTimeMillis() - startTime < 60000 && (devices == null || devices.size() == 0));
            if (devices != null && devices.size() > 0) {
              checkForShowingSensorTab(devices);
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }).start();
    } else {
      checkForShowingSensorTab(devices);
    }
    setTabColor(mTabHost);
    mTabHost.setBackgroundResource(R.color.tab_color);
    mTabHost.getTabWidget().setStripEnabled(true);
    mTabHost.getTabWidget().setRightStripDrawable(R.color.transperent_color);
    mTabHost.getTabWidget().setLeftStripDrawable(R.color.transperent_color);
    mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
      @Override
      public void onTabChanged(String tabId) {
        for (int i = 0; i < mTabHost.getTabWidget().getChildCount(); i++) {
          mTabHost.getTabWidget().getChildAt(i).setBackgroundColor(Color.TRANSPARENT); //unselected
        }
        mTabHost.getTabWidget().getChildAt(mTabHost.getCurrentTab()).setBackgroundResource(R.color.over_lay_light); // selected
        if (mTabHost.getCurrentTab() == 0) {
          //((MainActivity) activity).setDeviceType(false);
          AnalyticsInterface.getInstance().trackEvent("cameraTab","cameraTab_clicked",eventData);
          GeAnalyticsInterface.getInstance().trackEvent(AppEvents.DASHBOARD,AppEvents.TAB_CAMERA_CLICKED,AppEvents.TAB_CAMERA_CLICKED);
          ZaiusEvent cameraTabEvt = new ZaiusEvent(AppEvents.DASHBOARD);
          cameraTabEvt.action(AppEvents.TAB_CAMERA_CLICKED);
          try {
            ZaiusEventManager.getInstance().trackCustomEvent(cameraTabEvt);
          } catch (ZaiusException e) {
            e.printStackTrace();
          }
        } else if (mTabHost.getCurrentTab() == 1) {
         // ((MainActivity) activity).setDeviceType(true);
          AnalyticsInterface.getInstance().trackEvent("galleryTab","galleryTab_clicked",eventData);
          GeAnalyticsInterface.getInstance().trackEvent(AppEvents.GALLERY_TAB,AppEvents.TAB_GALLERY_CLICKED,AppEvents.TAB_GALLERY_CLICKED);

          ZaiusEvent gallerytTabEvt = new ZaiusEvent(AppEvents.GALLERYTAB);
          gallerytTabEvt.action(AppEvents.TAB_GALLERY_CLICKED);
          try {
            ZaiusEventManager.getInstance().trackCustomEvent(gallerytTabEvt);
          } catch (ZaiusException e) {
            e.printStackTrace();
          }

        }else if(mTabHost.getCurrentTab()==2){
          AnalyticsInterface.getInstance().trackEvent("eventsTab","events_tab_clicked",eventData);
          GeAnalyticsInterface.getInstance().trackEvent(AppEvents.EVENT_TAB,AppEvents.TAB_EVENTS_CLICKED,AppEvents.TAB_EVENTS_CLICKED);

          ZaiusEvent eventsTabEvt = new ZaiusEvent(AppEvents.EVENTSTAB);
          eventsTabEvt.action(AppEvents.TAB_EVENTS_CLICKED);
          try {
            ZaiusEventManager.getInstance().trackCustomEvent(eventsTabEvt);
          } catch (ZaiusException e) {
            e.printStackTrace();
          }

        }else if(mTabHost.getCurrentTab()==3){
          AnalyticsInterface.getInstance().trackEvent("plansTab","plans_tab_clicked",eventData);
          GeAnalyticsInterface.getInstance().trackEvent(AppEvents.PLAN_TAB,AppEvents.TAB_PLANS_CLICKED,AppEvents.TAB_PLANS_CLICKED);

          ZaiusEvent planTabEvt = new ZaiusEvent(AppEvents.PLANSTAB);
          planTabEvt.action(AppEvents.TAB_PLANS_CLICKED);
          try {
            ZaiusEventManager.getInstance().trackCustomEvent(planTabEvt);
          } catch (ZaiusException e) {
            e.printStackTrace();
          }
        }
      }
    });
    LogZ.i("DeviceTab Support Fragment on created.");
    if(mIsNotificationTab){
      mTabHost.setCurrentTabByTag(getString(R.string.tab_notifications));
      mIsNotificationTab = false;
    }else if(mIsGalleryTab){
      mTabHost.setCurrentTabByTag(getString(R.string.videos));
      mIsGalleryTab = false;
    }else if(mIsPlanTab){
      mTabHost.setCurrentTabByTag(getString(R.string.tab_plans));
      mIsPlanTab = false;
    }
    return mTabHost;
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    this.activity = activity;
  }

  private  void setTabColor(TabHost tabhost) {
    for (int i = 0; i < tabhost.getTabWidget().getChildCount(); i++) {
      // tabhost.getTabWidget().getChildAt(i).setBackgroundColor(Color.TRANSPARENT); //unselected
      TextView mTextView = (TextView) tabhost.getTabWidget().getChildAt(i).findViewById(android.R.id.title); //Unselected Tabs
      mTextView.setTextColor(Color.WHITE);
      mTextView.setAllCaps(false);
      //if(CommonUtil.isOrbit(activity)) {
        mTextView.setTextSize(12);
        mTextView.setTypeface(null, Typeface.BOLD);
      /*} else {
        mTextView.setTextSize(10);
        mTextView.setTypeface(null, Typeface.NORMAL);
      }*/
    }
    tabhost.getTabWidget().getChildAt(tabhost.getCurrentTab()).setBackgroundResource(R.color.over_lay_light); // selected
  }

  public boolean switchToTab(int which) {
    if (mTabHost != null) {
      mTabHost.setCurrentTab(which);
      return true;
    } else {
      return false;
    }
  }

  public void setTabHostVisibiliy(boolean visible) {
    if (mTabHost != null) {
      if (visible) {
        for (int i = 0; i < mTabHost.getTabWidget().getChildCount(); i++) {
          mTabHost.getTabWidget().getChildAt(i).setVisibility(View.VISIBLE);
        }
      } else {
        for (int i = 0; i < mTabHost.getTabWidget().getChildCount(); i++) {
          mTabHost.getTabWidget().getChildAt(i).setVisibility(View.GONE);
        }
      }
    }
  }

  public int getCurrentTab() {
    return mTabHost.getCurrentTab();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    mTabHost = null;
  }

  private void checkForShowingSensorTab(List<Device> devices) {
    boolean hasSensor = false;
    for (Device item : devices) {
      if (item.getProfile() != null) {
        DeviceProfile profile = item.getProfile();
        if (!TextUtils.isEmpty(profile.getParentId()) || // focus tag
            profile.registrationId.startsWith(PublicDefine.DEFAULT_REGID_OPEN_SENSOR)) { // open close sensor
          hasSensor = true;
          break;
        }
      }
    }
    if (hasSensor && getActivity() != null) {
      getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {
//          mTabHost.addTab(mTabHost.newTabSpec("Sensor").setIndicator(getString(R.string.sensor),
//              activity.getResources().getDrawable(R.drawable.camera)), CameraListFragment.class, null);
        }
      });
    }
  }

  public void setIsNotificationTab(boolean isNotificationTab){
    mIsNotificationTab = isNotificationTab;
  }

  public void setIsGalleryTab(boolean isGalleryTab){
    mIsGalleryTab = isGalleryTab;
  }

  public void setIsPlanTab(boolean isPlanTab){
    mIsPlanTab = isPlanTab;
  }

}
