package com.hubble.videobrowser;

import android.content.Context;
import android.util.Log;

import com.hubble.HubbleApplication;
import com.hubble.ui.GalleryItem;
import com.hubble.ui.eventsummary.EventSummary;

import java.io.File;
import java.io.FilenameFilter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Son Nguyen on 11/12/2015.
 */
public class VideoCollector {
	public static final String TAG = "VideoCollector";
	private static SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
	private static SimpleDateFormat downloadDf = new SimpleDateFormat("MMM dd");

	public static List<VideoItem> getRecordedVideos() {
		final String recordFolder = HubbleApplication.getVideoFolder().getAbsolutePath(); //Util.getDownloadDirectory(HubbleApplication.AppContext.getString(R.string.app_brand_application_name) + HubbleApplication.AppContext.getString(com.vtech.vtechconnect.R.string.videos));
		Log.i(TAG, "Collect video in folder: " + recordFolder);
		File dir = new File(recordFolder);
		File[] validVideoFiles = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				Pattern pattern = Pattern.compile("(.*)(@)(\\d+)_(.*)(\\.)((mp4|flv|png)$)");
				Matcher matcher = pattern.matcher(name);
				boolean result = matcher.matches();
				return result;
			}
		});
		List<VideoItem> videoItems = new ArrayList<>();
		Pattern pattern = Pattern.compile("(.*)(@)(\\d+)_(.*)(\\.)((mp4|flv|png)$)");
		if (validVideoFiles != null && validVideoFiles.length > 0) {
			for (int i = 0; i < validVideoFiles.length; i++) {
				Matcher matcher = pattern.matcher(validVideoFiles[i].getName());
				if (matcher.matches()) {
					videoItems.add(new VideoItem(matcher.group(1), matcher.group(3), validVideoFiles[i].getAbsolutePath()));
				}
			}
		}

		Collections.reverse(videoItems);
		return videoItems;
	}

	public static List<GalleryItem> getGalleryVideos(Context context) {
		final String recordFolder = HubbleApplication.getVideoFolder().getAbsolutePath(); //Util.getDownloadDirectory(HubbleApplication.AppContext.getString(R.string.app_brand_application_name) + HubbleApplication.AppContext.getString(com.vtech.vtechconnect.R.string.videos));
		Log.i(TAG, "Collect video in folder: " + recordFolder);
		final Pattern pattern = Pattern.compile("(.*)(@)(\\d+)_(.*)(\\.)((mp4|flv|png)$)");
		final Pattern patternSummary = Pattern.compile("(.*)(@)(\\d+)(@)(.*)(\\.)((mp4)$)");
		File dir = new File(recordFolder);
		File[] validVideoFiles = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				Matcher matcher = pattern.matcher(name);
				Matcher matcherSummary = patternSummary.matcher(name);
				boolean result = matcher.matches()|| matcherSummary.matches();
				return result;
			}
		});
		List<GalleryItem> galleryItems = new ArrayList<>();
		//Pattern pattern = Pattern.compile("(.*)(@)(\\d+)_(.*)(\\.)((mp4|flv|png)$)");
		if (validVideoFiles != null && validVideoFiles.length > 0) {
			for (int i = 0; i < validVideoFiles.length; i++) {
				Matcher matcher = pattern.matcher(validVideoFiles[i].getName());
				Matcher matcherSummary = patternSummary.matcher(validVideoFiles[i].getName());
				Matcher finalMatcher = null;
				if (matcher.matches()) {
					finalMatcher = matcher;
				}else if(matcherSummary.matches()) {
					finalMatcher = matcherSummary;
				}
				if(finalMatcher != null && finalMatcher.matches()){
					String path = validVideoFiles[i].getAbsolutePath();
					Date date = null ;
						try {
							date = df.parse(finalMatcher.group(3));
						} catch (ParseException e) {

						}
				    GalleryItem galleryItem = new GalleryItem(finalMatcher.group(1), date , validVideoFiles[i].getAbsolutePath());
					galleryItems.add(galleryItem);
				    if(matcherSummary.matches() && matcherSummary.group(5).equalsIgnoreCase("SV")){
					    galleryItem.setSummaryVideo(true);
				    }
				}
			}
		}
		Collections.sort(galleryItems, new Comparator<GalleryItem>() {
			@Override
			public int compare(GalleryItem e1, GalleryItem e2) {
				return e2.getDate().compareTo(e1.getDate());
			}
		});
		return galleryItems;
	}
}
