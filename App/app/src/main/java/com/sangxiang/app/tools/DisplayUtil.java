package com.sangxiang.app.tools;

/**
 * Created by sangxiang on 26/4/17.
 */

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;

public class DisplayUtil {
    private static final String TAG = "DisplayUtil";

    public static final int RESIZE_SMALL_WIDTH = 50;
    public static final int RESIZE_SMALL_HEIGHT = 50;
    public static final int RESIZE_MEDIUM_WIDTH = 80;
    public static final int RESIZE_MEDIUM_HEIGHT = 80;
    public static final int RESIZE_LARGE_WIDTH = 250;
    public static final int RESIZE_LARGE_HEIGHT = 250;
    public static final int RESIZE_XLARGE_WIDTH = 360;
    public static final int RESIZE_XLARGE_HEIGHT = 360;
    public static final int RESIZE_XLARGE_WIDTH_16_9 = 640;
    public static final int RESIZE_XLARGE_HEIGHT_16_9 = 360;
    private Context context;

    public DisplayUtil(Context context) {
        this.context = context;
    }

    public int dpToPixel(float dp) {
        final float scale = getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public int pixelToDp(float px) {
        final float scale = getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    public int getScreenWidth() {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        return width;
    }

    public int getDensityDpi() {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int densityDpi = dm.densityDpi;
        return densityDpi;
    }


    public DisplayMetrics getDisplayMetrics() {
        return context.getResources().getDisplayMetrics();
    }



    public static int calculateInSampleSize(BitmapFactory.Options options) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        final int IMAGE_MAX_HEIGHT = 7680;
        final int IMAGE_MAX_WIDTH = 1440;

        if (width > IMAGE_MAX_WIDTH || height > IMAGE_MAX_HEIGHT) {
            while ((height / inSampleSize) > IMAGE_MAX_HEIGHT || (width / inSampleSize) > IMAGE_MAX_WIDTH) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
