package com.hubble.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hubble.videobrowser.VideoItem;
import com.hubbleconnected.camera.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Admin on 21-02-2017.
 */
public class GalleryAdapter extends BaseAdapter {

	private Context mContext;
	private List<GalleryItem> mVideoImageList = new ArrayList<GalleryItem>();
	private IVideoImageLaunchListener miVideoImageLaunchListener;
	private boolean mSelectMode = false;
	private int mSelectItemCount = 0;

	public GalleryAdapter(Context context, IVideoImageLaunchListener iVideoImageLaunchListener) {
		mContext = context;
		miVideoImageLaunchListener = iVideoImageLaunchListener;
	}

	public void setVideoList(List<GalleryItem> videoImageList) {
		if (mVideoImageList != null) {
			mVideoImageList.clear();
			mSelectItemCount = 0;
			mVideoImageList.addAll(videoImageList);
		}
	}

	public void clearList() {
		if (mVideoImageList != null) {
			mVideoImageList.clear();
			mSelectItemCount = 0;
		}
	}

	public void selectAll() {
		miVideoImageLaunchListener.menuDisable(false);
		mSelectItemCount = 0;
		for (GalleryItem galleryItem : mVideoImageList) {
			galleryItem.setSelected(true);
			mSelectItemCount++;
		}
		miVideoImageLaunchListener.menuDisable(false);
		miVideoImageLaunchListener.selectEnable(false);
		notifyDataSetChanged();
	}

	public void deselectAll() {
		for (GalleryItem galleryItem : mVideoImageList) {
			galleryItem.setSelected(false);
		}
		mSelectItemCount = 0;
		miVideoImageLaunchListener.menuDisable(true);
		miVideoImageLaunchListener.selectEnable(true);
		notifyDataSetChanged();
	}

	public void enableSelectMode(boolean selectMode) {
		this.mSelectMode = selectMode;
		if(!selectMode){
			deselectAll();
		}
	}

	public void deleteSelectedItems() {
		if(mSelectMode && mSelectItemCount > 0) {
			int numberOfDeleteItem = 0;
			for (GalleryItem galleryItem : mVideoImageList) {
				if (galleryItem.isSelected()) {
					String filePath = galleryItem.getFilePath();
					if (filePath.contains(".flv")){
						deleteFile(filePath.replace(".flv", ".jpg"));
					}else if(filePath.contains(".mp4")){
						deleteFile(filePath.replace(".mp4", ".jpg"));
					}
					deleteFile(filePath);
				}
				numberOfDeleteItem++;
			}
			if(numberOfDeleteItem > 0){
				miVideoImageLaunchListener.onVideoDeleted();
                notifyDataSetChanged();
				numberOfDeleteItem = 0;
			}
		}
	}

	private void deleteFile(String filePath){
		File file = new File(filePath);
		if (file.exists()) {
			file.delete();
		}
	}


	public void shareSelectedItems() {
		if(mSelectMode && mSelectItemCount > 0) {
			int numberOfSharedItem = 0;
			ArrayList<Uri> files = new ArrayList<Uri>();
			for (GalleryItem galleryItem : mVideoImageList) {
				if (galleryItem.isSelected()) {
					File file = new File(galleryItem.getFilePath());
					if (file.exists()) {
						Uri uri = Uri.fromFile(file);
						files.add(uri);
						numberOfSharedItem++;
					}
				}
			}
			if(numberOfSharedItem > 0) {
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_SEND_MULTIPLE);
				intent.setType("video/");
				intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
				mContext.startActivity(intent);
				numberOfSharedItem = 0;
			}
		}
	}

	@Override
	public int getCount() {
		return mVideoImageList.size();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public Object getItem(int position) {
		return mVideoImageList.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.gallery_item_layout, null);
			holder.timeView = (TextView) convertView.findViewById(R.id.gallery_time_view);
			holder.galleryImageView = (ImageView) convertView.findViewById(R.id.gallery_image_view);
			holder.summaryImageView = (ImageView) convertView.findViewById(R.id.gallery_summary_view);
			holder.playImageView = (ImageView) convertView.findViewById(R.id.gallery_play_view);
			holder.gallerySelectViewHolder = (ImageView)convertView.findViewById(R.id.gallery_select_holder);
			holder.gallerySelectView = (ImageView) convertView.findViewById(R.id.gallery_select_tickMark);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		setUpGalleryView(position, holder);
		return convertView;
	}

	private void setUpGalleryView(final int position, final ViewHolder holder) {
		final GalleryItem galleryItem = mVideoImageList.get(position);
		holder.timeView.setText(galleryItem.getTime());
		final String filePath = galleryItem.getFilePath();
		if (filePath.contains(".flv") || filePath.contains(".mp4")) {
			if(galleryItem.isSummaryVideo()){
				holder.playImageView.setVisibility(View.GONE);
				holder.summaryImageView.setVisibility(View.VISIBLE);
			}else {
				holder.playImageView.setVisibility(View.VISIBLE);
				holder.summaryImageView.setVisibility(View.GONE);
			}

			File f = null;
			if (filePath.contains(".flv")) {
				f = new File(filePath.replace(".flv", ".jpg"));
			} else {
				f = new File(filePath.replace(".mp4", ".jpg"));
			}
			Picasso.with(mContext)
					.load(f)
					.resize(160, 90)
					.placeholder(R.drawable.no_snap)
					.error(R.drawable.no_snap)
					.into(holder.galleryImageView);
			holder.galleryImageView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mSelectMode) {
						setSelectView(position, holder);
					} else {
						miVideoImageLaunchListener.playVideo(filePath, galleryItem.isSummaryVideo());
					}
				}
			});
		} else {
			holder.summaryImageView.setVisibility(View.INVISIBLE);
			holder.playImageView.setVisibility(View.INVISIBLE);
			File f = new File(filePath);
			Picasso.with(mContext).
					load(f).
					placeholder(R.drawable.no_snap).
					error(R.drawable.no_snap).
					resize(160, 90).
					into(holder.galleryImageView);
			holder.galleryImageView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mSelectMode) {
						setSelectView(position, holder);
					} else {
						miVideoImageLaunchListener.launchImage(filePath);
					}
				}
			});

		}
		if(mSelectMode){
			if(mVideoImageList.get(position).isSelected()){
				holder.gallerySelectViewHolder.setVisibility(View.INVISIBLE);
				holder.gallerySelectView.setVisibility(View.VISIBLE);
			}else {
				holder.gallerySelectView.setVisibility(View.INVISIBLE);
				holder.gallerySelectViewHolder.setVisibility(View.VISIBLE);
			}
		}else {
			holder.gallerySelectView.setVisibility(View.INVISIBLE);
			holder.gallerySelectViewHolder.setVisibility(View.INVISIBLE);
		}
		holder.galleryImageView.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				enableSelectMode(true);
				mSelectItemCount++;
				holder.gallerySelectView.setVisibility(View.VISIBLE);
				mVideoImageList.get(position).setSelected(true);
				miVideoImageLaunchListener.menuDisable(false);
				miVideoImageLaunchListener.selectEnable(true);
				miVideoImageLaunchListener.onLongClick();
				return true;
			}
		});
	}

	private void setSelectView(int position, ViewHolder holder){
		if(mVideoImageList.get(position).isSelected()) {
			mSelectItemCount--;
			holder.gallerySelectView.setVisibility(View.INVISIBLE);
			mVideoImageList.get(position).setSelected(false);
		}else {
			mSelectItemCount++;
			holder.gallerySelectView.setVisibility(View.VISIBLE);
			mVideoImageList.get(position).setSelected(true);
		}
		setMenu(position, holder);
	}

	private void setMenu(int position, ViewHolder holder){
		if(mSelectItemCount == mVideoImageList.size()){
			miVideoImageLaunchListener.menuDisable(false);
			miVideoImageLaunchListener.selectEnable(false);
		}else if(mSelectItemCount == 0){
			miVideoImageLaunchListener.menuDisable(true);
			miVideoImageLaunchListener.selectEnable(true);
		}else {
			miVideoImageLaunchListener.menuDisable(false);
			miVideoImageLaunchListener.selectEnable(true);
		}
	}

	public class ViewHolder {
		TextView timeView;
		ImageView galleryImageView;
		ImageView summaryImageView;
		ImageView playImageView;
		ImageView gallerySelectViewHolder;
		ImageView gallerySelectView;
	}

	public interface IVideoImageLaunchListener {
		void playVideo(String filePath, boolean isSummaryVideo);

		void launchImage(String filePath);

		void onLongClick();

		void menuDisable(boolean isMenuDisable);

		void selectEnable(boolean isSelectEnable);

		void onVideoDeleted();
	}

}
