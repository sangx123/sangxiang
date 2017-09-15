package com.hubble.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

/**
 * Created by Son Nguyen on 15/10/2015.
 */
public class MyVideoView extends VideoView {

  private PlayPauseListener mListener;

  public MyVideoView(Context context) {
    super(context);
  }

  public MyVideoView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public MyVideoView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public void setPlayPauseListener(PlayPauseListener listener) {
    mListener = listener;
  }

  @Override
  public void pause() {
    super.pause();
    if (mListener != null) {
      mListener.onPause();
    }
  }

  @Override
  public void start() {
    super.start();
    if (mListener != null) {
      mListener.onPlay();
    }
  }

  public interface PlayPauseListener {
    void onPlay();

    void onPause();
  }

}