package com.nxcomm.blinkhd.util;

import android.text.TextUtils;

import com.google.common.collect.ImmutableMap;
import com.nxcomm.blinkhd.ui.CameraSettingsActivity;
import com.nxcomm.blinkhd.ui.dialog.MotionOptionDialog;

import java.util.Map;

/**
 * Created by hoang on 4/15/16.
 */
public class NotificationSettingUtils {
    private static final Map<String, String> MULTIPLE_MD_TYPE_MAP = ImmutableMap.<String, String>builder()
            .put("0877", "") // any versions
            .build();

    private static final Map<String, String> MULTIPLE_MD_TYPE_WITH_PIR_MAP = ImmutableMap.<String, String>builder()
            .put("0082", "") // any versions. NOTE: 0821 does not have PIR.
            .put("0072", "")
            .build();

    public static boolean supportMultiMotionTypes(String modelId, String fwVersion) {
        boolean support = false;
        if (!TextUtils.isEmpty(modelId)) {
            if (MULTIPLE_MD_TYPE_MAP.containsKey(modelId)) {
                if (TextUtils.isEmpty(MULTIPLE_MD_TYPE_MAP.get(modelId))) {
                    support = true;
                }
            }
        }
        return support;
    }



    public static boolean supportMultiMotionTypesPIR(String modelId, String fwVersion) {
        boolean support = false;
        if (!TextUtils.isEmpty(modelId)) {
            if (MULTIPLE_MD_TYPE_WITH_PIR_MAP.containsKey(modelId)) {
                if (TextUtils.isEmpty(MULTIPLE_MD_TYPE_WITH_PIR_MAP.get(modelId))) {
                    support = true;
                }
            }
        }
        return support;
    }

    public static String getMotionDetectionType(final int motionDetectionTypeIndex)
    {
        String mdType = "";
        switch (motionDetectionTypeIndex)
        {
            case CameraSettingsActivity.MD_TYPE_MD_INDEX:
                mdType = CameraSettingsActivity.MD_TYPE_MD;
                break;
            case CameraSettingsActivity.MD_TYPE_BSC_INDEX:
                mdType = CameraSettingsActivity.MD_TYPE_BSC;
                break;
            case CameraSettingsActivity.MD_TYPE_BSD_INDEX:
                mdType = CameraSettingsActivity.MD_TYPE_BSD;
                break;
            default:
                mdType = CameraSettingsActivity.MD_TYPE_OFF;
                break;
        }
        return mdType;
    }

    public static int getMotionDetectionTypeIndex(final String motionDetectionType) {
        int mdTypeIndex = 0;
        if (CameraSettingsActivity.MD_TYPE_MD.equalsIgnoreCase(motionDetectionType)) {
            mdTypeIndex = CameraSettingsActivity.MD_TYPE_MD_INDEX;
        } else if (CameraSettingsActivity.MD_TYPE_BSC.equalsIgnoreCase(motionDetectionType)) {
            mdTypeIndex = CameraSettingsActivity.MD_TYPE_BSC_INDEX;
        } else if (CameraSettingsActivity.MD_TYPE_BSD.equalsIgnoreCase(motionDetectionType)) {
            mdTypeIndex = CameraSettingsActivity.MD_TYPE_BSD_INDEX;
        } else {
            mdTypeIndex = CameraSettingsActivity.MD_TYPE_OFF_INDEX;
        }
        return mdTypeIndex;
    }
}
