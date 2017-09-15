package com.msc3;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.hubbleconnected.camera.R;


public class LeftSideMenuImageAdapter extends BaseAdapter {

  private Context mContext;
  private boolean[] status;

  // /*
  // * to keep track position with types
  // */
  // private int position_pan;
  // private int position_mic;
  // private int position_rec;
  // private int position_melody;
  // private int position_temp;

  static public final int pos_pan = 0;
  static public final int pos_mic = 1;
  static public final int pos_rec = 2;
  static public final int pos_melody = 3;
  static public final int pos_temp = 4;

  static public int grid_pos_pan = -1;
  static public int grid_pos_mic = -1;
  static public int grid_pos_rec = -1;
  static public int grid_pos_melody = -1;
  static public int grid_pos_temp = -1;

  private Integer[] mThumbIds_full = {R.drawable.video_action_pan, R.drawable.video_action_mic, R.drawable.video_action_video, R.drawable.video_action_music, R.drawable.video_action_temp};

  private Integer[] mThumbIds_full_disabled = {R.drawable.video_action_pan_pressed, R.drawable.video_action_mic_pressed, R.drawable.video_action_video_pressed, R.drawable.video_action_music_pressed, R.drawable.video_action_temp_pressed};

  private Integer[] mThumbIds_without_mic = {R.drawable.video_action_pan, R.drawable.video_action_video, R.drawable.video_action_music, R.drawable.video_action_temp};

  private Integer[] mThumbIds_without_mic_disabled = {R.drawable.video_action_pan_pressed, R.drawable.video_action_video_pressed, R.drawable.video_action_music_pressed, R.drawable.video_action_temp_pressed};

  private Integer[] mThumbIds_without_pan = {R.drawable.video_action_mic, R.drawable.video_action_video, R.drawable.video_action_music, R.drawable.video_action_temp};

  private Integer[] mThumbIds_without_pan_disabled = {R.drawable.video_action_mic_pressed, R.drawable.video_action_video_pressed, R.drawable.video_action_music_pressed, R.drawable.video_action_temp_pressed};

  private Integer[] mThumbIds_without_pan_mic = {R.drawable.video_action_video, R.drawable.video_action_music, R.drawable.video_action_temp};

  private Integer[] mThumbIds_without_pan_mic_disabled = {R.drawable.video_action_video_pressed, R.drawable.video_action_music_pressed, R.drawable.video_action_temp_pressed};

  private Integer[] mThumbIds_without_pan_mic_melody = {R.drawable.video_action_video, R.drawable.video_action_temp};

  private Integer[] mThumbIds_without_pan_mic_melody_disabled = {R.drawable.video_action_video_pressed, R.drawable.video_action_temp_pressed};

  final float scale;

  boolean enableMic = false;
  boolean enablePanTilt = false;
  boolean enableMelody = false;
  int numberOfItems;
  private Integer[] mThumbIds;
  private Integer[] mThumbIds_disabled;

  public LeftSideMenuImageAdapter(Context c, boolean enableMicFeature, boolean enablePanTiltFeature, boolean enableMelodyFeature) {
    mContext = c;
    scale = mContext.getResources().getDisplayMetrics().density;
    enableMic = enableMicFeature;
    enablePanTilt = enablePanTiltFeature;
    enableMelody = enableMelodyFeature;
    numberOfItems = getNumberOfItems();
    status = new boolean[numberOfItems];
    for (int i = 0; i < numberOfItems; i++) {
      status[i] = false;
    }
  }

  private int getNumberOfItems() {
    int numItems = 2;
    if (enableMic && enablePanTilt && enableMelody) {
      // mbp83
      numItems = 5;
      mThumbIds = mThumbIds_full;
      mThumbIds_disabled = mThumbIds_full_disabled;

			/*
       * Change position of each icon
			 */
      grid_pos_pan = 0;
      grid_pos_mic = 1;
      grid_pos_rec = 2;
      grid_pos_melody = 3;
      grid_pos_temp = 4;
    } else if (enableMic && !enablePanTilt && enableMelody) {
      // focus66
      numItems = 4;
      mThumbIds = mThumbIds_without_pan;
      mThumbIds_disabled = mThumbIds_without_pan_disabled;

			/*
       * Change position of each icon
			 */
      grid_pos_pan = -1;
      grid_pos_mic = 0;
      grid_pos_rec = 1;
      grid_pos_melody = 2;
      grid_pos_temp = 3;

    } else if (!enableMic && enablePanTilt && enableMelody) {
      // shared cam 36/41 on Windows
      numItems = 4;
      mThumbIds = mThumbIds_without_mic;
      mThumbIds_disabled = mThumbIds_without_mic_disabled;

			/*
       * Change position of each icon
			 */
      grid_pos_pan = 0;
      grid_pos_mic = -1;
      grid_pos_rec = 1;
      grid_pos_melody = 2;
      grid_pos_temp = 3;

    } else if (!enableMic && !enablePanTilt && enableMelody) {
      // shared cam 33 on Windows
      numItems = 3;
      mThumbIds = mThumbIds_without_pan_mic;
      mThumbIds_disabled = mThumbIds_without_pan_mic_disabled;

			/*
<<<<<<< HEAD
       * Change position of each icon
=======
             * Change position of each icon
>>>>>>> 20150910_sonnguyen_release_temp
			 */
      grid_pos_pan = -1;
      grid_pos_mic = -1;
      grid_pos_rec = 0;
      grid_pos_melody = 1;
      grid_pos_temp = 2;

    } else if (!enableMic && !enablePanTilt && !enableMelody) {
      // shared cam on Mac
      numItems = 2;
      mThumbIds = mThumbIds_without_pan_mic_melody;
      mThumbIds_disabled = mThumbIds_without_pan_mic_melody_disabled;

      grid_pos_pan = -1;
      grid_pos_mic = -1;
      grid_pos_rec = 0;
      grid_pos_melody = -1;
      grid_pos_temp = 1;
    }

    return numItems;
  }

  public boolean isEnablePan() {
    return grid_pos_pan != -1 && status[grid_pos_pan];
  }

  public void setEnablePan(boolean enablePan) {
    if (!(grid_pos_pan == -1)) {
      status[grid_pos_pan] = enablePan;
    }
  }

  public void toggleItem(int position) {
    status[position] = !status[position];
  }

  public boolean isEnableMic() {
    return grid_pos_mic != -1 && status[grid_pos_mic];
  }

  public void setEnableMic(boolean enableMic) {
    if (grid_pos_mic != -1) {
      status[grid_pos_mic] = enableMic;
    }
  }

  public boolean isEnableRec() {
    return grid_pos_rec != -1 && status[grid_pos_rec];
  }

  public void setEnableRec(boolean enableRec) {
    if (grid_pos_rec != -1) {
      status[grid_pos_rec] = enableRec;
    }
  }

  public boolean isEnableMelody() {
    return grid_pos_melody != -1 && status[grid_pos_melody];
  }

  public void setEnableMelody(boolean enableMelody) {
    if (grid_pos_melody != -1) {
      status[grid_pos_melody] = enableMelody;
    }
  }

  public boolean isEnableTemp() {
    return grid_pos_temp != -1 && status[grid_pos_temp];
  }

  public void setEnableTemp(boolean enableTemp) {
    if (grid_pos_temp != -1) {
      status[grid_pos_temp] = enableTemp;
    }
  }

  public int getCount() {
    return numberOfItems;
  }

  public Object getItem(int position) {
    return null;
  }

  public long getItemId(int position) {
    return 0;
  }

  public void clearAllItemSettings() {
    for (int i = 0; i < numberOfItems; i++) {
      status[i] = false;
    }
  }

  public void clearOtherSettings(int position) {
    for (int i = 0; i < numberOfItems; i++) {
      if (i != position) {
        status[i] = false;
      }
    }
  }

  public boolean areAllItemsEnabled() {
    return false;
  }

  public boolean isShownEnabled(int position) {
    if (position >= 0 && position < getCount()) {
      return status[position];
    }

    return true;
  }

  @Override
  public boolean isEnabled(int position) {
    switch (position) {
      default: // else
        return true;
    }
  }

  // /SUPPORT lanscape mode first

  // create a new ImageView for each item referenced by the Adapter
  public View getView(int position, View convertView, ViewGroup parent) {
    ImageView imageView;
    int width, height, padding;

		/*
     * 4 icons visible
		 */
    // int height_dp = 84;//dp
    int padding_dp = 1;// dp
    padding = (int) (padding_dp * scale + 0.5f);
    // Convert to system pixel -- this value should be different from screen
    // to screen
    // height =(int) (height_dp * scale + 0.5f);
    // width = height;

    if (convertView == null) { // if it's not recycled, initialize some attributes
      imageView = new ImageView(mContext);
      // imageView.setLayoutParams(new
      // GridView.LayoutParams(width,height));
      imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
      imageView.setPadding(padding, padding, padding, padding);

    } else {
      imageView = (ImageView) convertView;
      // imageView.setLayoutParams(new
      // GridView.LayoutParams(width,height));
      imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
      imageView.setPadding(padding, padding, padding, padding);
    }

    if (!isShownEnabled(position)) {
      imageView.setBackgroundResource(android.R.color.transparent);
      imageView.setImageResource(mThumbIds[position]);
      imageView.setOnTouchListener(new ButtonTouchListener(mContext.getResources().getDrawable(mThumbIds[position]), mContext.getResources().getDrawable(mThumbIds_disabled[position])));
    } else {
      imageView.setBackgroundResource(android.R.color.transparent);
      imageView.setImageResource(mThumbIds_disabled[position]);
      imageView.setOnTouchListener(new ButtonTouchListener(mContext.getResources().getDrawable(mThumbIds_disabled[position]), mContext.getResources().getDrawable(mThumbIds[position])
      ));
    }

    return imageView;
  }

}

