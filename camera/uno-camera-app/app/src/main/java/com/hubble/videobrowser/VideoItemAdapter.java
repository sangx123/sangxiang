package com.hubble.videobrowser;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hubble.HubbleApplication;
import com.hubbleconnected.camera.R;
import com.nxcomm.blinkhd.ui.VideoFragment;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Son Nguyen on 11/12/2015
 */
public class VideoItemAdapter extends RecyclerView.Adapter<VideoItemAdapter.ViewHolder> {
  private Context mContext;
  private List<VideoItem> videoItems;
  private static final String TAG = "VideoItemAdapter";
  private boolean choiceMode = false;

  public void enableChoiceMode() {
    this.choiceMode = true;
    notifyDataSetChanged();
  }

  public void disableChoiceMode() {
    this.choiceMode = false;
    notifyDataSetChanged();
  }

  public boolean isChoiceModeEnable() {
    return choiceMode;
  }

  public void selectAll() {
    for (VideoItem videoItem : videoItems) {
      videoItem.setSelected(true);
    }
    notifyDataSetChanged();
  }

  public void deselectAll() {
    for (VideoItem videoItem : videoItems) {
      videoItem.setSelected(false);
    }
    notifyDataSetChanged();
  }

  public boolean isSelectedAll() {
    for (VideoItem videoItem : videoItems) {
      if (videoItem.isSelected() == false) {
        return false;
      }
    }
    return true;
  }

  public void deleteSelectedItems(VideoFragment.OnVideoDeletedListener listener) {
    int numberOfDeleteItem = 0;

    for (Iterator<VideoItem> iterator = videoItems.iterator(); iterator.hasNext(); ) {
      VideoItem videoItem = iterator.next();
      if (videoItem.isSelected()) {
        File file = new File(videoItem.getFilePath());
        if (file.exists()) {
          file.delete();
        }
        iterator.remove();
        numberOfDeleteItem++;
      }
    }
    if (numberOfDeleteItem == 0) {
      Toast.makeText(mContext, R.string.please_select_item_to_delete, Toast.LENGTH_SHORT).show();
    } else {
      Log.i(TAG, "Deleted " + numberOfDeleteItem + " videos");
      notifyDataSetChanged();
      if (listener != null) {
        listener.onVideoDeleted();
      }
    }
  }


  public void shareSelectedItems() {
    int numberOfDeleteItem = 0;
    ArrayList<Uri> files = new ArrayList<Uri>();
    Intent intent = new Intent();
    intent.setAction(Intent.ACTION_SEND_MULTIPLE);
    intent.putExtra(Intent.EXTRA_SUBJECT, "Here are some files.");
    intent.setType("image/jpeg"); /* This example is sharing jpeg images. */

    for (Iterator<VideoItem> iterator = videoItems.iterator(); iterator.hasNext(); ) {
      VideoItem videoItem = iterator.next();
      if (videoItem.isSelected()) {
        File file = new File(videoItem.getFilePath());
        if (file.exists()) {

          Uri uri = Uri.fromFile(file);
          files.add(uri);
        }
        iterator.remove();
        numberOfDeleteItem++;
      }
    }
    if (numberOfDeleteItem == 0) {
      Toast.makeText(mContext, R.string.please_select_item_to_share, Toast.LENGTH_SHORT).show();
    } else {
      Log.i(TAG, "Deleted " + numberOfDeleteItem + " videos");
      notifyDataSetChanged();

    }
    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
    mContext.startActivity(intent);
  }

  public interface IPlayListener {
    void openVideo(String filePath);
  }

  private IPlayListener listener;

  public VideoItemAdapter(Context context, List<VideoItem> videoItems, IPlayListener listener) {
    mContext = context;
    this.videoItems = videoItems;
    this.listener = listener;
  }

  public List<VideoItem> getVideoItems() {
    return videoItems;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_item_layout, null);
    ViewHolder viewHolder = new ViewHolder(view);
    return viewHolder;
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, int position) {
    final VideoItem videoItem = videoItems.get(position);
    holder.itemView.setTag(videoItem);

//    holder.cameraNameTextView.setText(videoItem.getCameraName());
//    holder.timeTextView.setText(videoItem.getFormattedDate());

    String filePath = videoItem.getFilePath();
    if(filePath.contains(".flv") || filePath.contains(".mp4")) {
      File videoFile = new File(videoItem.getFilePath());
      long timemodified = videoFile.lastModified();
      Date videoCreatedDate = new Date(timemodified);
      String stringDate = DateFormat.getDateTimeInstance().format(videoCreatedDate);
      holder.timeTextView.setText(stringDate);
      holder.playImageView.setVisibility(View.VISIBLE);

      if (VideoUtils.isCache(VideoUtils.md5(videoItem.getFilePath()))) {
        String cachedFile = HubbleApplication.AppContext.getCacheFile(VideoUtils.md5(videoItem.getFilePath())).getAbsolutePath();
        Drawable d = Drawable.createFromPath(videoItem.getFilePath().replace(".flv",".png"));
        Log.i(TAG, "Cache file" + cachedFile);
        Picasso.with(mContext)
                .load(new File(cachedFile))
                .resize(160, 90)
                .placeholder(d)
        .into(holder.snapshotImageView);

        ;
        File frameFile = new File(videoItem.getFilePath().replace(".flv",".png"));
       // holder.snapshotImageView.setImageURI(Uri.fromFile(frameFile));
      } else {
        VideoUtils.generateThumbnailAsync(videoItem.getFilePath());

      }
    }else{
      holder.playImageView.setVisibility(View.GONE);
      File frameFile = new File(videoItem.getFilePath());
      holder.snapshotImageView.setImageURI(Uri.fromFile(frameFile));
    }
    if (choiceMode) {
      holder.mSolvedCheckBox.setVisibility(View.VISIBLE);
      holder.mSolvedCheckBox.setChecked(videoItem.isSelected());
    } else {
      holder.mSolvedCheckBox.setVisibility(View.GONE);
    }

    holder.snapshotImageView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if(videoItem.getFilePath().contains(".png")){
          File frameFile = new File(videoItem.getFilePath());
          Intent galleryIntent = new Intent(Intent.ACTION_VIEW, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
          galleryIntent.setDataAndType(Uri.fromFile(frameFile), "image/*");
          galleryIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          mContext.startActivity(galleryIntent);
        }
      }
    });
    holder.playImageView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if(videoItem.getFilePath().contains(".flv") || videoItem.getFilePath().contains(".mp4"))
           listener.openVideo(videoItem.getFilePath());
        else{
          File frameFile = new File(videoItem.getFilePath());
          Intent galleryIntent = new Intent(Intent.ACTION_VIEW, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
          galleryIntent.setDataAndType(Uri.fromFile(frameFile), "image/*");
          galleryIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          mContext.startActivity(galleryIntent);
        }
      }
    });
    holder.mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.i(TAG, "Checked change on item " + videoItem.getCameraName() + " " + videoItem.getFormattedDate() + " to " + isChecked);
        videoItem.setSelected(isChecked);
      }
    });
  }

  @Override
  public int getItemCount() {
    return videoItems.size();
  }

  public void updateVideoList(List<VideoItem> videoItems) {
    this.videoItems = videoItems;
    notifyDataSetChanged();
  }

  class ViewHolder extends RecyclerView.ViewHolder {
    TextView cameraNameTextView, timeTextView;
    ImageView playImageView;
    ImageView snapshotImageView;
    private final CheckBox mSolvedCheckBox;

    public ViewHolder(View itemView) {
      super(itemView);
      cameraNameTextView = (TextView) itemView.findViewById(R.id.cameraNameTextView);
      timeTextView = (TextView) itemView.findViewById(R.id.timeTextView);
      playImageView = (ImageView) itemView.findViewById(R.id.playImageView);
      snapshotImageView = (ImageView) itemView.findViewById(R.id.snapshotImageView);
      mSolvedCheckBox = (CheckBox) itemView.findViewById(R.id.checkBox);
      if (choiceMode) {
        mSolvedCheckBox.setVisibility(View.VISIBLE);
      } else {
        mSolvedCheckBox.setVisibility(View.GONE);
      }
    }
  }
}
