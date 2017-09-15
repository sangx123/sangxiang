package com.hubble.util;

/**
 * Created by sonikas on 21/09/16.
 */
public class CommonConstants {

    public static final String DEFAULT_BASIC_AUTH_USR = "camera";
    public static final String DEFAULT_BASIC_AUTH_PASS = "000000";
    public static String DEVICE_PORT = "80";
    public static String DEFAULT_DEVICE_IP = "192.168.193.1";
    public static final int CLICK_TYPE_VIEW_FINDER = 0;
    public static final int CLICK_TYPE_EVENT_HISTORY = 1;

    public static final int LAUNCH_SUMMARY_FROM_VF = 2;
    public static final int LAUNCH_SUMMARY_FROM_EH = 3;
    public static final int LAUNCH_SUMMARY_FROM_MA = 4;

    public static final int ACTION_TYPE_SHARE = 0;
    public static final int ACTION_TYPE_DOWNLOAD = 1;

    public static final int EVENT_PAGE_SIZE = 50;

    public static final int DEVICE_NOTIFICATION_STATUS = 1;
    public static final int DEVICE_WAKEUP_STATUS = 2;
    public static final int DEVICE_CAMERA_STATUS = 3;

    public static final String EVENT_VIDEO_TAG = "event_video";
    public static final String EVENT_IMAGE_TAG = "event_image";
    public static final String EVENT_TYPE = "event_type";
    public static final String EVENT_VIDEO = "video";
    public static final String EVENT_IMAGE = "image";

    public static final String VIEW_FINDER_LAUNCH_TAG = "vf_launch_reason";
    public static final String VIEW_FINDER_GOTO_EVENT = "event";
    public static final String VIEW_FINDER_GOTO_SUMMARY = "summary";
    public static final String VIEW_FINDER_GOTO_STREAM = "stream";
    public static final String VIEW_FINDER_CAMERA_REG_ID = "reg_id";

    public static final String FILE_PROVIDER_AUTHORITY_HUBBLE = "com.hubbleconnected.camera.fileprovider";

    public static final int CONFIG_EVENT_HISTORY = 0;
    public static final int CONFIG_NOTIFICATION = 1;
    public static final String ACCOUNT_PROFILE_ID = "account_profile_id";
    public static final String ACCOUNT_PROFILE_IMAGE = "account_profile_image";
    public static final String SHAREDPREFERENCES_NAME = "hubble_home_preferences";

    public static final String REGISTRATION_ID = "registration_id";
    public static final String APP_REGISTRATION_TIME = "registration_time";
    public static final String SETTINGS_CLICKED_TIME = "settings_clicked_time";

    public static final String OFFER_TYPE_FLAG = "offer_type";
    public static final String OFFER_TYPE_VA = "VIDEO_ANALYTICS";

    public static final int DEVICE_TYPE_ORBIT=1;
    public static final int DEVICE_TYPE_73=2;
    public static final int DEVICE_TYPE_72=3;
    public static final int DEVICE_TYPE_OTHER=4;
    public static int SHOULD_EXIT_NOW_YES = 0x0001FFFF;
    public static final String shouldNotAutoLogin = "shouldNotAutoLogin";
    public static final String CALL_FROM_OTHER_APP = "call_from_other_app";
    public static final String isCreateUserAccount = "createUserAccount";
    public static final int STOP_PLEASE = 0x2020;


}
