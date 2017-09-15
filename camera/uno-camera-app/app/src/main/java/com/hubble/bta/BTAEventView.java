package com.hubble.bta;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.hubbleconnected.camera.R;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import base.hubble.constants.Streaming;
import base.hubble.database.TimelineEvent;
import cz.havlena.ffmpeg.ui.FFMpegPlaybackActivity;

/**
 * TODO: document your custom view class.
 */
public class BTAEventView extends LinearLayout implements View.OnClickListener {
  private static final String TAG = BTAEventView.class.getSimpleName();
  private ImageView imageView;
  private TextView textViewDate, textViewValue;
  private TextView tvLoading;
  private TimelineEvent timelineEvent;
  private Context context;
  private ImageView imageViewPlay;

  public BTAEventView(Context context) {
    super(context);
    this.context = context;
    init();
  }

  public BTAEventView(Context context, AttributeSet attrs) {
    super(context, attrs);
    this.context = context;
    init();
  }

  public BTAEventView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    this.context = context;
    init();
  }

  private void init() {
    LayoutInflater inflater = (LayoutInflater.from(context));
    inflater.inflate(R.layout.bta_event_view, this);
    if (!isInEditMode()) {
      this.imageView = (ImageView) findViewById(R.id.imageViewSnapshot);
      this.textViewDate = (TextView) findViewById(R.id.textViewText);
      this.imageViewPlay = (ImageView) findViewById(R.id.imageViewPlay);
      this.textViewValue = (TextView) findViewById(R.id.textViewBTAValue);
      this.tvLoading = (TextView) findViewById(R.id.tv_loading);
      this.textViewDate.setVisibility(View.INVISIBLE);
      this.textViewValue.setVisibility(View.INVISIBLE);
      this.imageViewPlay.setVisibility(View.INVISIBLE);
    }
  }

  public void setTimelineEvent(TimelineEvent timelineEvent) {
    this.timelineEvent = timelineEvent;
    updateUI();
  }

  private void updateUI() {
    this.imageView.setOnClickListener(this);
    if (this.timelineEvent != null && timelineEvent.getCachedData() != null) {
      Log.i(TAG, "Update BTA Snapshot: " + timelineEvent.getCachedData().get(0).getImage());
      DateTime dateTime = new DateTime(timelineEvent.getTimestamp().getTime());
      textViewDate.setText(dateTime.toString(DateTimeFormat.forPattern("YYYY-MM-dd HH:mm")));
      textViewDate.setVisibility(View.VISIBLE);

      textViewValue.setText(context.getString(R.string.average_level) + timelineEvent.getValue());
      textViewValue.setVisibility(View.INVISIBLE);
      Picasso.with(BTAEventView.this.getContext()).load(timelineEvent.getCachedData().get(0).getImage()).into(imageView, new Callback() {
        @Override
        public void onSuccess() {
          Log.e(TAG, "Snapshot got");
          BTAEventView.this.post(new Runnable() {
            @Override
            public void run() {
              tvLoading.setVisibility(View.GONE);
              if (timelineEvent.getCachedData().get(0).getFile() != null && timelineEvent.getCachedData().get(0).getFile().trim().length() > 0) {
                imageViewPlay.setVisibility(View.VISIBLE);
              } else {
                imageViewPlay.setVisibility(View.GONE);
              }
            }
          });
        }

        @Override
        public void onError() {
          BTAEventView.this.post(new Runnable() {
            @Override
            public void run() {
              tvLoading.setVisibility(View.GONE);
              Log.e(TAG, "Error when load image");
              if (timelineEvent.getCachedData().get(0).getFile() != null && timelineEvent.getCachedData().get(0).getFile().trim().length() > 0) {
                imageViewPlay.setVisibility(View.VISIBLE);
              } else {
                imageViewPlay.setVisibility(View.GONE);
              }
              imageView.setImageResource(R.drawable.no_snap);
            }
          });
        }
      });
    }
  }

  @Override
  public void onClick(View v) {
    if (timelineEvent != null && timelineEvent.getCachedData() != null) {
      String clipUrl = timelineEvent.getCachedData().get(0).getFile();
      if (clipUrl == null || clipUrl.trim().length() == 0) {
        Log.w(TAG, "There is no clip to play");
      } else {
        Log.i(TAG, "Play clip url: " + clipUrl);
        Intent intent = new Intent(context, FFMpegPlaybackActivity.class);
        intent.putExtra(Streaming.EXTRA_ONE_CLIP, clipUrl);
        context.startActivity(intent);
      }
    }
  }
}
