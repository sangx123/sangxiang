package com.nxcomm.blinkhd.ui.blur;

/**
 * Created by Sean on 14-10-27.
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.widget.ImageView;

import com.hubble.HubbleApplication;
import com.hubble.devcomm.Device;
import com.hubbleconnected.camera.R;
import com.hubbleconnected.camera.BuildConfig;
import com.squareup.picasso.Picasso;

import java.util.List;

import base.hubble.PublicDefineGlob;
import base.hubble.database.DeviceProfile;

public class Blur {

  @SuppressLint("NewApi")
  public static Bitmap fastblur(Context context, Bitmap sentBitmap, int radius, int passes) {
    if (passes == 0) return sentBitmap;
    Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
    final RenderScript rs = RenderScript.create(context);
    final Allocation input = Allocation.createFromBitmap(rs, sentBitmap, Allocation.MipmapControl.MIPMAP_NONE,
        Allocation.USAGE_SCRIPT);
    final Allocation output = Allocation.createTyped(rs, input.getType());
    final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
    script.setRadius(radius);
    script.setInput(input);
    script.forEach(output);
    output.copyTo(bitmap);
    return fastblur(context, bitmap, radius, passes - 1);
  }

  public static void blurImageBackground(ImageView backgroundView, Context context, List<Device> allCams) {
    try {
      if (BuildConfig.FLAVOR.equals("vtech")) {
        Picasso.with(context)
            .load(R.drawable.brand_bg)
            .fit()
            .centerCrop()
            .error(R.color.app_neutral_background)
            .placeholder(R.color.app_neutral_background)
            .into(backgroundView);
      } else {
        Device camToBlur = filterDevices(context, allCams);

        if (camToBlur != null) {
          String snapsUrl = camToBlur.getProfile().getSnapshotUrl();

          if (!snapsUrl.contains("hubble.png")) {
            Picasso.with(context)
                .load(snapsUrl)
                .fit()
                .centerCrop()
                .transform(new BlurPicassoTransformation(context))
                .error(R.color.app_neutral_background)
                .placeholder(R.color.app_neutral_background)
                .into(backgroundView);
          } else {
            Picasso.with(context)
                .load(R.drawable.blur_default)
                .fit()
                .centerCrop()
                .error(R.color.app_neutral_background)
                .placeholder(R.color.app_neutral_background)
                .into(backgroundView);
          }
        } else {
          Picasso.with(context)
              .load(R.drawable.blur_default)
              .fit()
              .centerCrop()
              .error(R.color.app_neutral_background)
              .placeholder(R.color.app_neutral_background)
              .into(backgroundView);
        }
      }
    } catch (Exception ignored) {
    }
  }

  private static Device filterDevices(Context context, List<Device> allCams) {
    Device camToBlur = null;
    if (allCams != null && allCams.size() > 0) {
      camToBlur = allCams.get(0);
      String previousCamMac = HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_LAST_CAMERA, "");
      for (Device cam : allCams) {
        DeviceProfile deviceProfile = cam.getProfile();
        if (deviceProfile.getMacAddress().equals(previousCamMac) && deviceProfile.getSnapshotUrl() != null) {
          camToBlur = cam;
        }
      }
    }
    return camToBlur;
  }

}