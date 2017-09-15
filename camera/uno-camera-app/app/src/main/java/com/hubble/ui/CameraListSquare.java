package com.hubble.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridView;

import com.hubble.devcomm.Device;
import com.hubbleconnected.camera.R;

import java.util.List;

public class CameraListSquare extends FrameLayout {

  private List<Device> cameras = null;

  private GridView cameraListSquare = null;
  private CameraListSquareAdapter adapter = null;

  public CameraListSquare(Context context) {
    super(context);
    init();
  }

  public CameraListSquare(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public CameraListSquare(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init();
  }

  private void init() {
    if (!isInEditMode()) {
      View view = inflate(getContext(), R.layout.camera_list_square, null);
      addView(view);
      cameraListSquare = (GridView) view.findViewById(R.id.camListSquare);
    }
  }

  private void updateUI() {
    if (adapter == null) {
      adapter = new CameraListSquareAdapter(getContext(), cameras);
      adapter.setListener(mListener);
      cameraListSquare.setAdapter(adapter);
    } else {
      adapter.setCamera(cameras);
    }
  }

  public List<Device> getCameras() {
    return cameras;
  }

  public void setCameras(List<Device> cameras) {
    this.cameras = cameras;
    updateUI();
  }

  public interface Listener {
    void onDeviceSelected(Device device);
  }

  private Listener mListener;

  public void setListener(Listener listener) {
    mListener = listener;
    if (adapter != null) {
      adapter.setListener(mListener);
    }
  }

}
