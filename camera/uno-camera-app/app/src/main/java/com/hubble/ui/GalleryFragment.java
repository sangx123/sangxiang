package com.hubble.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hubble.videobrowser.VideoCollector;
import com.hubbleconnected.camera.R;

import java.io.File;
import java.util.List;

import cz.havlena.ffmpeg.ui.FFMpegPlaybackActivity;

/**
 * Created by Admin on 21-02-2017.
 */
public class GalleryFragment extends Fragment {

	private final String TAG = "GalleryFragment";

	private Context mContext = null;

	private RelativeLayout mMenuLayout;
	private TextView mCancelTextView;
	private ImageView mDelete;
	private ImageView mShare;
	private TextView mSelectAll;
	private TextView mDeselectAll;
	private RelativeLayout mNoDataLayout;
	private ProgressBar mLoadingView;
	private GridView mGalleryGridView;

	private GalleryAdapter mGalleryAdapter;
	FetchVideosTask mFetchVideosTask;


	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getContext();

	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = (ViewGroup) inflater.inflate(R.layout.activity_gallery_fragment, container, false);

		mMenuLayout = (RelativeLayout)view.findViewById(R.id.menu_layout);
		mCancelTextView = (TextView)view.findViewById(R.id.cancel);
		mDelete  = (ImageView)view.findViewById(R.id.delete_enable_image);
		mShare = (ImageView)view.findViewById(R.id.share_enable_image);
		mSelectAll = (TextView)view.findViewById(R.id.select_all);
		mDeselectAll = (TextView)view.findViewById(R.id.deselect_all);


		mNoDataLayout = (RelativeLayout) view.findViewById(R.id.no_data_layout);
		mLoadingView = (ProgressBar) view.findViewById(R.id.gallery_progressBar);
		mGalleryGridView = (GridView) view.findViewById(R.id.gallery_view);

		mGalleryAdapter = new GalleryAdapter(mContext,new GalleryAdapter.IVideoImageLaunchListener(){
			@Override
			public void playVideo(String filePath, boolean isSummaryVideo) {
				if(isSummaryVideo){
					Uri intentUri = Uri.parse(filePath);
					Intent intent = new Intent();
					intent.setAction(Intent.ACTION_VIEW);
					intent.setDataAndType(intentUri, "video/mp4");
					startActivity(intent);
				}else {
					Intent intent = new Intent(mContext, FFMpegPlaybackActivity.class);
					intent.setData(Uri.fromFile(new File(filePath)));
					startActivity(intent);
				}
			}

			@Override
			public void launchImage(String filePath) {
				File frameFile = new File(filePath);
				Intent galleryIntent = new Intent(Intent.ACTION_VIEW, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				galleryIntent.setDataAndType(Uri.fromFile(frameFile), "image/*");
				galleryIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startActivity(galleryIntent);
			}

			@Override
			public void onLongClick() {
				mMenuLayout.setVisibility(View.VISIBLE);

			}

			@Override
			public void menuDisable(boolean isMenuDisable) {
				if(isMenuDisable){
					mSelectAll.setVisibility(View.VISIBLE);
					mDelete.setImageResource(R.drawable.delete_disabled);
					mShare.setImageResource(R.drawable.share_disabled);
				}else {
					mDelete.setImageResource(R.drawable.delete_enabled);
					mShare.setImageResource(R.drawable.share_enabled);
				}
			}

			@Override
			public void selectEnable(boolean isSelectEnable) {
				if(isSelectEnable) {
					mSelectAll.setVisibility(View.VISIBLE);
					mDeselectAll.setVisibility(View.INVISIBLE);
				}else {
					mSelectAll.setVisibility(View.INVISIBLE);
					mDeselectAll.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onVideoDeleted() {
				FetchVideosTask fetchVideosTask = new FetchVideosTask();
				fetchVideosTask.execute();
			}
		});
		mGalleryGridView.setAdapter(mGalleryAdapter);


		mCancelTextView.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				mGalleryAdapter.deselectAll();
				mGalleryAdapter.enableSelectMode(false);
				mMenuLayout.setVisibility(View.GONE);
			}
		});

		mSelectAll.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mGalleryAdapter.selectAll();
			}
		});

		mDeselectAll.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mGalleryAdapter.deselectAll();
			}
		});

		mDelete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setMessage(R.string.delete_selected_files);

				DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(which == AlertDialog.BUTTON_POSITIVE) {
							dialog.dismiss();
							mGalleryAdapter.deleteSelectedItems();
						} else {
							dialog.dismiss();
						}
					}
				};
				builder.setNegativeButton(R.string.dialog_cancel, onClickListener);
				builder.setPositiveButton(R.string.Delete, onClickListener);
				builder.show();
			}
		});

		mShare.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
                mGalleryAdapter.shareSelectedItems();
			}
		});

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		mFetchVideosTask = new FetchVideosTask();
		mFetchVideosTask.execute();
	}

	@Override
	public void onPause() {
		super.onPause();
		if(mFetchVideosTask != null && mFetchVideosTask.getStatus() == AsyncTask.Status.RUNNING){
			mFetchVideosTask.cancel(true);
		}
	}

	class FetchVideosTask extends AsyncTask<Void, Void, List<GalleryItem>> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mLoadingView.setVisibility(View.VISIBLE);
			mGalleryGridView.setVisibility(View.GONE);
			mNoDataLayout.setVisibility(View.GONE);
		}

		@Override
		protected List<GalleryItem> doInBackground(Void... params) {
			return VideoCollector.getGalleryVideos(mContext);
		}

		@Override
		protected void onPostExecute(List<GalleryItem> galleryItems) {
			super.onPostExecute(galleryItems);
			if(isAdded()) {
				mLoadingView.setVisibility(View.GONE);
				if (galleryItems.size() > 0) {
					mGalleryAdapter.deselectAll();
					mGalleryAdapter.enableSelectMode(false);
					mMenuLayout.setVisibility(View.GONE);
					mNoDataLayout.setVisibility(View.GONE);
					mGalleryGridView.setVisibility(View.VISIBLE);
					mGalleryAdapter.setVideoList(galleryItems);
					mGalleryAdapter.notifyDataSetChanged();
				} else {
					mMenuLayout.setVisibility(View.GONE);
					mNoDataLayout.setVisibility(View.VISIBLE);
					mGalleryAdapter.clearList();
					mGalleryAdapter.notifyDataSetChanged();
					mGalleryGridView.setVisibility(View.GONE);
				}
			}
		}
	}
}
