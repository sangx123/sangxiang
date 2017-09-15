package com.nxcomm.blinkhd.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.hubble.videobrowser.MyRecyclerView;
import com.hubble.videobrowser.VideoCollector;
import com.hubble.videobrowser.VideoItem;
import com.hubble.videobrowser.VideoItemAdapter;
import com.hubbleconnected.camera.R;

import java.io.File;
import java.util.List;

import cz.havlena.ffmpeg.ui.FFMpegPlaybackActivity;

/**
 * Created by connovatech on 10/14/2016.
 */
public class VideoFragment extends BaseFragment implements VideoItemAdapter.IPlayListener, ActionMode.Callback{
	private Activity mActivity = null;
	private Context mContext = null;
	private MyRecyclerView recyclerView;
	private VideoItemAdapter adapter;
	private ActionMode mActionMode;
	private Toolbar toolbar;
	private FetchVideos fetchVideosTask;
	private View emptyView;
	private View loadingView;
	MenuItem menuEdititem;
	@Override
	public void onCheckNowOfflineMode() {

	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
		 super.onCreateView(inflater, container, savedInstanceState);
		View mView = inflater.inflate(R.layout.activity_record_video_browser, container, false);
		setHasOptionsMenu(true);
		return mView;
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.mActivity = activity;
	}
	@Override
	public void onStart() {
		super.onStart();

	//	toolbar = (Toolbar) mActivity.findViewById(R.id.my_toolbar);


		loadingView = mActivity.findViewById(R.id.loading_view);
		emptyView = mActivity.findViewById(R.id.list_empty);
		recyclerView = (MyRecyclerView) mActivity.findViewById(R.id.recyclerView);
		recyclerView.setEmptyView(emptyView);
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
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.edit_all, menu);
		menuEdititem = menu.findItem(R.id.menu_edit);
		//quaychenh: in case of there is no recorded videos, hide Edit icon
	//	if (adapter == null || adapter.getItemCount() == 0) {
		//	item.setVisible(false);
	//	} else {
		menuEdititem.setVisible(false);
	//	}
		 super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_edit) {
			adapter.enableChoiceMode();
			adapter.notifyDataSetChanged();
			ActionBarActivity activity=(ActionBarActivity)mActivity;

			mActionMode = activity.startSupportActionMode(this);
		}

		return super.onOptionsItemSelected(item);
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
		} else if(menuItem.getItemId() == R.id.menu_share) {
			AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
			builder.setMessage(R.string.share_selected);

			DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(which == AlertDialog.BUTTON_POSITIVE) {
						dialog.dismiss();
						adapter.shareSelectedItems();
					} else {
						dialog.dismiss();
					}
				}
			};

			builder.setNegativeButton(R.string.dialog_cancel, onClickListener);
			builder.setPositiveButton(R.string.share, onClickListener);
			builder.show();
			return true;
		}else if(menuItem.getItemId() == R.id.menu_delete) {
			AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
			builder.setMessage(R.string.delete_selected_files);

			DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(which == AlertDialog.BUTTON_POSITIVE) {
						dialog.dismiss();
						adapter.deleteSelectedItems(new OnVideoDeletedListener() {
							@Override
							public void onVideoDeleted() {
								if (adapter.getItemCount() == 0) {
									//quaychenh: in case of delete all videos successful, finish action mode
									if (mActionMode != null) {
										mActionMode.finish();
									}
									// After deleting all the items hide the menu item
									menuEdititem.setVisible(false);
									//quaychenh: in case of there is no recorded videos, hide Edit icon
									//mActivity..supportInvalidateOptionsMenu();
								}
							}
						});
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
	public void onResume() {
		super.onResume();
		fetchVideoFromRecordFolder();
		if (mActionMode != null) {
			mActionMode.finish();
		}
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
	public void onDestroyActionMode(ActionMode mode) {
		mActionMode = null;
		if (adapter != null) {
			adapter.disableChoiceMode();
		}
	}

	@Override
	public void openVideo(String filePath) {
		Intent intent = new Intent(mActivity, FFMpegPlaybackActivity.class);
		intent.setData(Uri.fromFile(new File(filePath)));
		startActivity(intent);
	}
	private void refreshVideoListView(List<VideoItem> videoItems) {
		adapter = null;
		if (adapter == null) {
			adapter = new VideoItemAdapter(mActivity, videoItems, this);
			recyclerView.setAdapter(adapter);
		} else {
			adapter.notifyDataSetChanged();
			adapter.updateVideoList(videoItems);

		}

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
			if(videoItems.size() > 0) {
				refreshVideoListView(videoItems);
				if(menuEdititem != null)
					menuEdititem.setVisible(true);
			}
			else {
				emptyView.setVisibility(View.VISIBLE);
				menuEdititem.setVisible(false);
			}
		}
	}



	@Override
	public void onDestroyView() {
		if (mActionMode != null) {
			mActionMode.finish();
		}
		super.onDestroyView();
	}



	

	public interface OnVideoDeletedListener {
		void onVideoDeleted();
	}
}
