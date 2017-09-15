package com.hubble.videobrowser;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.hubble.framework.service.analytics.AnalyticsInterface;
import com.hubble.framework.service.analytics.EventData;
import com.hubble.registration.EScreenName;
import com.hubble.registration.AnalyticsController;
import com.hubbleconnected.camera.R;

import java.io.File;
import java.util.List;

import cz.havlena.ffmpeg.ui.FFMpegPlaybackActivity;
import de.greenrobot.event.EventBus;

public class RecordVideoBrowserActivity extends ActionBarActivity implements VideoItemAdapter.IPlayListener, ActionMode.Callback {

  private MyRecyclerView recyclerView;
  private VideoItemAdapter adapter;
  private ActionMode mActionMode;
  private Toolbar toolbar;
  private FetchVideos fetchVideosTask;
  private View emptyView;
  private View loadingView;
  private EventData eventData;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_record_video_browser);
    eventData = new EventData();
    toolbar = (Toolbar) findViewById(R.id.my_toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setDisplayShowTitleEnabled(false);
    getSupportActionBar().setIcon(getResources().getDrawable(R.drawable.actionbar_logo));

    loadingView = findViewById(R.id.loading_view);
    emptyView = findViewById(R.id.list_empty);
    recyclerView = (MyRecyclerView) findViewById(R.id.recyclerView);
    recyclerView.setEmptyView(emptyView);
  }


  @Override
  protected void onResume() {
    super.onResume();
    //AA-1480
    AnalyticsController.getInstance().trackScreen(EScreenName.Videos);
    AnalyticsInterface.getInstance().trackEvent("Videos","Videos",eventData);

    fetchVideoFromRecordFolder();
    if (mActionMode != null) {
      mActionMode.finish();
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    EventBus.getDefault().register(this);
  }

  public void onEventMainThread(ThumbnailCreatedEvent event) {
    adapter.notifyDataSetChanged();
  }

  @Override
  protected void onStop() {
    super.onStop();
    EventBus.getDefault().unregister(this);
  }

  @Override
  protected void onDestroy() {
    if (fetchVideosTask != null) {
      fetchVideosTask.cancel(true);
      fetchVideosTask = null;
    }
    super.onDestroy();
  }

  private void fetchVideoFromRecordFolder() {
    //quaychenh: do in background to avoid hang UI
    if (fetchVideosTask != null) {
      fetchVideosTask.cancel(true);
    }
    fetchVideosTask = new FetchVideos();
    fetchVideosTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void)null);
  }

  @Override
  public void openVideo(String filePath) {
    Intent intent = new Intent(this, FFMpegPlaybackActivity.class);
    intent.setData(Uri.fromFile(new File(filePath)));
    startActivity(intent);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.edit_all, menu);
    MenuItem item = menu.findItem(R.id.menu_edit);
    //quaychenh: in case of there is no recorded videos, hide Edit icon
    if (adapter == null || adapter.getItemCount() == 0) {
      item.setVisible(false);
    } else {
      item.setVisible(true);
    }
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.menu_edit) {
      adapter.enableChoiceMode();
      mActionMode = startSupportActionMode(this);
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {

    // Inflate a menu resource providing context menu items
    MenuInflater inflater = actionMode.getMenuInflater();
    inflater.inflate(R.menu.action_mode_video_browser_menu, menu);
    return true;
  }

  @Override
  public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
    return false;
  }

  @Override
  public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {

    if (menuItem.getItemId() == R.id.menu_select_all) {
      if (adapter.isSelectedAll()) {
        adapter.deselectAll();
      } else {
        adapter.selectAll();
      }
      return true;
    } else if(menuItem.getItemId() == R.id.menu_delete) {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setMessage(R.string.delete_selected_videos);

      DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          if(which == AlertDialog.BUTTON_POSITIVE) {
            dialog.dismiss();
            //ARUNA removed it
//            adapter.deleteSelectedItems(new OnVideoDeletedListener() {
//              @Override
//              public void onVideoDeleted() {
//                if (adapter.getItemCount() == 0) {
//                  //quaychenh: in case of delete all videos successful, finish action mode
//                  if (mActionMode != null) {
//                    mActionMode.finish();
//                  }
//                  //quaychenh: in case of there is no recorded videos, hide Edit icon
//                  RecordVideoBrowserActivity.this.supportInvalidateOptionsMenu();
//                }
//              }
//            });
          } else {
            dialog.dismiss();
          }
        }
      };

      builder.setNegativeButton(R.string.dialog_cancel, onClickListener);
      builder.setPositiveButton(R.string.Delete, onClickListener);
      builder.show();
      return true;
    }
    return false;
  }

  @Override
  public void onDestroyActionMode(ActionMode actionMode) {
    mActionMode = null;
    if (adapter != null) {
      adapter.disableChoiceMode();
    }
  }

  private void refreshVideoListView(List<VideoItem> videoItems) {
    if (adapter == null) {
      adapter = new VideoItemAdapter(this, videoItems, this);
      recyclerView.setAdapter(adapter);
    } else {
      adapter.updateVideoList(videoItems);
    }
    //quaychenh: refresh option menu items
    supportInvalidateOptionsMenu();
  }

  class FetchVideos extends AsyncTask<Void, Void, List<VideoItem>> {
    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      loadingView.setVisibility(View.VISIBLE);
      recyclerView.setVisibility(View.GONE);
      emptyView.setVisibility(View.GONE);
    }

    @Override
    protected List<VideoItem> doInBackground(Void... params) {
      return VideoCollector.getRecordedVideos();
    }

    @Override
    protected void onPostExecute(List<VideoItem> videoItems) {
      super.onPostExecute(videoItems);
      loadingView.setVisibility(View.GONE);
      refreshVideoListView(videoItems);
    }
  }

  public interface OnVideoDeletedListener {
    void onVideoDeleted();
  }
}
