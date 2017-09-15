package com.hubble.ui;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hubble.devcomm.Device;
import com.hubbleconnected.camera.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;
import com.hubbleconnected.camera.BuildConfig;

public class CameraListSquareAdapter extends ArrayAdapter<Device> {

  public static final String TAG = "CameraListSquareAdapter";

  private Context mContext = null;
  private List<Device> cameras = null;
  private ColorMatrixColorFilter grayScaleFilter;
  private CameraListSquare.Listener mListener;

  public CameraListSquareAdapter(Context context, List<Device> cameras) {
    super(context, R.layout.camera_list_square_adapter);
    this.mContext = context;
    this.cameras = cameras;
    ColorMatrix cm = new ColorMatrix();
    cm.setSaturation(0);
    grayScaleFilter = new ColorMatrixColorFilter(cm);
  }

  public void setListener(CameraListSquare.Listener listener) {
    mListener = listener;
  }

  public void setCamera(List<Device> cameras) {
    this.cameras = cameras;
    notifyDataSetChanged();
  }

  @Override
  public int getCount() {
    if (cameras == null) {
      return 0;
    }
    return this.cameras.size();
  }

  public static class ViewHolder {
    TextView textView_CameraName;
    ImageView imageView_CameraAvatar;
    ImageView imageView_CameraStatus;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    if (mContext == null) {
      return null;
    }
    ViewHolder holder;
    if (convertView == null) {
      holder = new ViewHolder();
      LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      convertView = inflater.inflate(R.layout.camera_list_square_adapter, parent, false);
      holder.textView_CameraName = (TextView) convertView.findViewById(R.id.txtCamName);
      holder.imageView_CameraAvatar = (ImageView) convertView.findViewById(R.id.imgCam);
      holder.imageView_CameraStatus = (ImageView) convertView.findViewById(R.id.imageViewCameraStatus);
      convertView.setTag(holder);
    } else {
      holder = (ViewHolder) convertView.getTag();
    }

    final String imageLink = cameras.get(position).getProfile().getSnapshotUrl();
    if (imageLink != null && !imageLink.isEmpty() && !imageLink.contains("hubble.png")) {
      ImageLoader.getInstance().displayImage(imageLink, holder.imageView_CameraAvatar);
    } else {
      holder.imageView_CameraAvatar.setImageResource(R.drawable.default_cam_with_tran_bg);
    }

    holder.textView_CameraName.setText(cameras.get(position).getProfile().getName());

    if (!cameras.get(position).getProfile().isAvailable()) {
      if (BuildConfig.FLAVOR.equals("hubble") || BuildConfig.FLAVOR.equalsIgnoreCase("hubblenew")) {
        holder.imageView_CameraAvatar.setColorFilter(grayScaleFilter);
      }
      holder.imageView_CameraStatus.setImageResource(R.drawable.settings_circle_disable);
    } else {
      if (BuildConfig.FLAVOR.equals("hubble") || BuildConfig.FLAVOR.equalsIgnoreCase("hubblenew")) {
        holder.imageView_CameraAvatar.clearColorFilter();
      }
      holder.imageView_CameraStatus.setImageResource(R.drawable.settings_circle_green);
    }

    /*if (cameras.get(position).getProfile().getRegistrationId().startsWith("070004")) {
      // This is open sensor
      holder.cameraStatus.setVisibility(View.INVISIBLE);
    } else {
      holder.cameraStatus.setVisibility(View.VISIBLE);
    }*/

    final Device current = cameras.get(position);

    holder.imageView_CameraAvatar.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (current.getProfile().isAvailable()) {
          if (mListener != null) {
            mListener.onDeviceSelected(current);
          }
        } else {
          Log.d(TAG, "Device is offline");
        }
      }
    });
    return convertView;
  }
}
