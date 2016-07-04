package com.youku.local;

import java.io.File;
import java.text.Collator;
import java.util.HashSet;
import java.util.Locale;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.youku.android.player.R;
import com.youku.thumbnailer.UThumbnailer;
import com.youku.uplayer.UMediaPlayer;

public class Media implements Comparable<Media> {

	public final static String TAG = "MediaItem";

	public final static HashSet<String> VIDEO_EXTENSIONS;
	// public final static HashSet<String> AUDIO_EXTENSIONS;
	public final static HashSet<String> FOLDER_BLACKLIST;
	
	static int mDefaultThumbnailID = R.drawable.scan_default;

	static {
		String[] video_extensions;
		if (Scanner.isUplayerSupported) {
			String[] uplayerExtensions = { ".3gp", ".avi", ".f4v", ".flv",
					".mkv", ".mov", ".mp4", ".rmvb", };
			video_extensions = uplayerExtensions;
		} else {
			String[] systemPlayerExtensions = { ".3gp", ".mp4" };
			video_extensions = systemPlayerExtensions;
		}
		VIDEO_EXTENSIONS = new HashSet<String>();
		for (String item : video_extensions)
			VIDEO_EXTENSIONS.add(item);
		String[] folder_blacklist = { "/alarms", "/notifications",
				"/ringtones", "/media/alarms", "/media/notifications",
				"/media/ringtones", "/media/audio/alarms",
				"/media/audio/notifications", "/media/audio/ringtones",
				"/Android/data/", "/log", "/tudou/offlinedata" };
		FOLDER_BLACKLIST = new HashSet<String>();
		for (String item : folder_blacklist)
			FOLDER_BLACKLIST.add(android.os.Environment
					.getExternalStorageDirectory().getPath() + item);
	}

	private String mTitle; // 视频文件名，不带视频格式

	private String mThumbnailPath; // 缩略图路径

	private final String mLocation; // 视频路径
	private long mDuration = 0; // 获取视频长度，单位毫秒
	private long mProgress = 0;

	private Collator mCollatorChina = Collator.getInstance(Locale.CHINA);
	private Collator mCollatorUS = Collator.getInstance(Locale.US);

	/**
	 * 创建一个新的Media
	 * 
	 * @param context
	 *            Application context
	 * @param media
	 *            uri
	 * @param addToDb
	 *            是否写入数据库
	 */
	public Media(Context context, String uri, String fileName, Boolean addToDb) {
		mLocation = uri;
		int end = fileName.lastIndexOf(".");
		mTitle = end <= 0 ? fileName : fileName.substring(0, end);
		if (Scanner.isUplayerSupported) {
			mDuration = UMediaPlayer.getFileDuration(mLocation);
			String path = UThumbnailer.getThumailPath(mLocation);
			mThumbnailPath = path;
		} else {
			getDurationFromMediaStore(context);
		}
		if (addToDb) {
			MediaDatabase db = MediaDatabase.getInstance(context);
			db.addMedia(this);
		}
	}

	/**
	 * 低端机从系统取duration
	 */
	private void getDurationFromMediaStore(Context context) {
		ContentResolver resolver = context.getContentResolver();
		Cursor cursor = resolver.query(
				MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
				new String[] { MediaStore.Video.Media.DURATION },
				MediaStore.Video.Media.DATA + "=?", new String[] { mLocation },
				null);
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			mDuration = cursor.getInt(0);
		}
	}

	public Media(String uri, int position) {
		mLocation = uri;
	}

	public Media(Context context, String location, long time, long length,
			String title, String thumbnailPath) {
		mLocation = location;
		mDuration = time;
		mProgress = length;
		mThumbnailPath = thumbnailPath;
		mTitle = title;
	}

	/**
	 * 根据文件名排序
	 */
	@Override
	public int compareTo(Media another) {
		if (mTitle.length() > 0 && another.mTitle.length() > 0) {
			if (String.valueOf(mTitle.charAt(0)).getBytes().length != 1
					&& String.valueOf(another.mTitle.charAt(0)).getBytes().length != 1) {
				// 中文排序
				return mCollatorChina.getCollationKey(mTitle).compareTo(
						mCollatorChina.getCollationKey(another.getTitle()));
			}
		}
		// 其他排序
		return mCollatorUS.getCollationKey(mTitle).compareTo(
				mCollatorUS.getCollationKey(another.getTitle()));
	}

	public String getLocation() {
		return mLocation;
	}

	public long getDuration() {
		return mDuration;
	}

	public void setDuration(long time) {
		mDuration = time;
	}

	public long getProgress() {
		return mProgress;
	}

	public void setProgress(long progress) {
		mProgress = progress;
	}

	/**
	 * 获取存储在sd卡上的缩略图，没有返回null
	 */
	public Drawable getDrawable(Context context) {
		if (Scanner.isUplayerSupported) {
			Drawable d = UThumbnailer.getDrawable(mLocation);
			if (d == null) {
				return context.getResources().getDrawable(mDefaultThumbnailID);
			} else
				return d;
		} else
			return context.getResources().getDrawable(mDefaultThumbnailID);
	}

	public String getTitle() {
		return mTitle;
	}

	/**
	 * 判断sd卡上的缩略图是否存在
	 */
	public boolean isThumbnailExist() {
		if (TextUtils.isEmpty(mThumbnailPath))
			return false;
		return new File(mThumbnailPath).exists();
	}

	/**
	 * 返回缩略图存储路径
	 */
	public String getThumbnailPath() {
		return mThumbnailPath;
	}
	
}
