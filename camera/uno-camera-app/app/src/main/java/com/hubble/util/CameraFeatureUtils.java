package com.hubble.util;

import android.util.Log;

import android.text.TextUtils;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import com.hubble.registration.PublicDefine;

/**
 * Created by hoang on 9/1/16.
 */
public class CameraFeatureUtils {
    private static final String TAG = CameraFeatureUtils.class.getSimpleName();
    public static final String FW86_SUPPORT_SDCARD_STREAMING_JOB_BASED = "01.19.36";

    public static boolean doesSupportP2pFileStream(String modelId, String fwVersion) {
        boolean support = false;
        //TODO check again
        /*if (FlavorUtils.isVtechApp() || FlavorUtils.isBtApp()) {
            support = true;
        }*/
        return support;
    }

    public static boolean doesSupportRtmpFileStreamJobBased(String modelId, String fwVersion) {
        boolean support = false;
        if (modelId != null && modelId.equals("0086")) {
            if (fwVersion != null) {
                int fwVersionInt = -1;
                try {
                    fwVersionInt = Integer.parseInt(fwVersion.replace(".", ""));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                final int fwVersionTarget = Integer.parseInt(FW86_SUPPORT_SDCARD_STREAMING_JOB_BASED.replace(".", ""));

                if (fwVersionInt >= fwVersionTarget) {
                    support = true;
                }
            }
        }
        return support;
    }

    private static Map<String, String> mModelNeedOpenHttpPort = ImmutableMap.<String, String>builder()
            .put("0931", "01.19.68")
            .put("0921", "01.19.68")
            .build();
    public static boolean shouldOpenHttpPort(final String modelId, final String fwVersion) {
        boolean support = false;
        if (mModelNeedOpenHttpPort.containsKey(modelId)) {
            if (TextUtils.isEmpty(mModelNeedOpenHttpPort.get(modelId))) { // any versions
            support = true;
            } else {
                if (fwVersion != null) {
                    int currentFw = -1;
                    try {
                        currentFw = Integer.parseInt(fwVersion.replace(".", ""));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    String targetFwVersion = mModelNeedOpenHttpPort.get(modelId);
                    support = (currentFw >= Integer.valueOf(targetFwVersion.replace(".", "")));
                }
            }
        }
        return support;
    }
}
