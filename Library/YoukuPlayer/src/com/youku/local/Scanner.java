package com.youku.local;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;

import com.baseproject.utils.Logger;
import com.youku.thumbnailer.UThumbnailer;
import com.youku.uplayer.MediaPlayerProxy;
import com.youku.uplayer.UMediaPlayer;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Scanner {
	private static Scanner mInstance;

	// 文件夹的最大文件数，超过该值则跳过当前文件夹
	private static final int DIR_MAX_SIZE = MediaPlayerProxy.freq > 1400 ? 1000
			: 500;

	public static boolean isUplayerSupported = MediaPlayerProxy
			.isUplayerSupported();

	private Scanner(Context context) {
		mInstance = this;
		mContext = context;
	}

	public static Scanner getInstance(Context context) {
		if (mInstance == null) {
			synchronized (Scanner.class) {
				if (mInstance == null) {
					mInstance = new Scanner(context.getApplicationContext());
					if (isUplayerSupported) {
						UThumbnailer.setThumbnailerPath(context);
						UMediaPlayer.registerAVcodec();
					}
				}
			}
		}
		return mInstance;
	}

	private IScanListener mScanListener;
	private Thumbnailer mThumbnailer;

	private Thread mLoadingThread;
	public Context mContext;
	private final ReadWriteLock mItemListLock = new ReentrantReadWriteLock();
	private final ArrayList<Media> mItemList = new ArrayList<Media>();
	private Handler mHandler;
	MediaItemFilter mediaFileFilter = new MediaItemFilter();

	private static final String TAG = "LOCAL_SCAN";
	private boolean isStopping = false;

	public static final int MEDIA_ITEMS_UPDATED = 100;

	public void loadMediaItems() {
		if (mLoadingThread == null
				|| mLoadingThread.getState() == State.TERMINATED) {
			isStopping = false;
			mLoadingThread = new Thread(new GetMediaItemsRunnable());
			mLoadingThread.start();
		}

		if (isUplayerSupported) {
			if (mThumbnailer == null)
				mThumbnailer = new Thumbnailer(mContext);
			mThumbnailer.start();
		}
	}

	public void stop() {
		Logger.d(TAG, "stop");
		isStopping = true;
		if (mThumbnailer != null)
			mThumbnailer.stop();
	}

	private class GetMediaItemsRunnable implements Runnable {
		private final Stack<File> directories = new Stack<File>();
		private final HashSet<String> directoriesScanned = new HashSet<String>();

		@Override
		public void run() {
			Logger.d(TAG, "scan start");
			if (mHandler != null && mScanListener != null) {
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						if (mScanListener != null)
							mScanListener.onScanStart();
					}
				});
			}
			final MediaDatabase DBManager = MediaDatabase.getInstance(mContext);
			List<File> mediaDirs = new ArrayList<File>();
			String storageDirs[] = Util.getMediaDirectories();
			for (String dir : storageDirs) {
				File f = new File(dir);
				if (f.exists())
					mediaDirs.add(f);
			}
			directories.addAll(mediaDirs);
			HashMap<String, Media> existingMedias = DBManager
					.getMedias(mContext);
			HashSet<String> addedLocations = new HashSet<String>();

			mItemListLock.writeLock().lock();
			mItemList.clear();
			mItemListLock.writeLock().unlock();

			int count = 0;
			final ArrayList<File> mediaToScan = new ArrayList<File>();
			try {

				// Count total files, and stack them
				while (!directories.isEmpty()) {
					File dir = directories.pop();
					Logger.d(TAG, "current dir:" + dir.getAbsolutePath());
					String dirPath = dir.getAbsolutePath();
					File[] f = null;

					// Skip some system folders
					if (dirPath.startsWith("/proc/")
							|| dirPath.startsWith("/sys/")
							|| dirPath.startsWith("/dev/"))
						continue;

					// Do not scan again if same canonical path
					try {
						dirPath = dir.getCanonicalPath();
					} catch (IOException e) {
						Logger.e(TAG, e);
					}
					if (directoriesScanned.contains(dirPath))
						continue;
					else
						directoriesScanned.add(dirPath);

					// Do no scan media in .nomedia folders
					if (new File(dirPath + "/.nomedia").exists()) {
						continue;
					}

					// Filter the extensions and the folders
					Logger.d(TAG, "listFiles");
					try {
						String[] nameList = dir.list();
						if (nameList.length > DIR_MAX_SIZE
								&& !mediaDirs.contains(dir)
								&& !dirPath.contains("/Camera")) {
							Logger.e(TAG,
									"exceeds max file size,skip this dir.");
							continue;
						}

						if ((f = dir.listFiles(mediaFileFilter)) != null) {
							for (File file : f) {
								Logger.d(TAG, "scan:" + file.getAbsolutePath());
								if (file.isFile()) {
									mediaToScan.add(file);
								} else if (file.isDirectory()) {
									directories.push(file);
								}
							}
						}
					} catch (Exception e) {
						// listFiles can fail in OutOfMemoryError or Timeout, go
						// to the next folder
						Logger.e(TAG, "scan exception", e);
						continue;
					}
					Logger.d(TAG, "scan next");
					if (isStopping) {
						Logger.d(TAG, "Stopping scan");
						return;
					}
				}

				// Process the stacked items
				for (File file : mediaToScan) {
					String fileURI = file.getAbsolutePath();
					final int notifyCount = count;
					if (mHandler != null && mScanListener != null) {
						mHandler.post(new Runnable() {

							@Override
							public void run() {
								if (mScanListener != null)
									mScanListener.onItemAdded(notifyCount,
											mediaToScan.size());
							}
						});
					}
					count++;
					Media item = null;
					if (existingMedias.containsKey(fileURI)) {
						if (!addedLocations.contains(fileURI)) {
							mItemListLock.writeLock().lock();
							item = existingMedias.get(fileURI);
							mItemList.add(item);
							mItemListLock.writeLock().unlock();
							addedLocations.add(fileURI);
						}
					} else {
						mItemListLock.writeLock().lock();
						item = new Media(mContext, fileURI, file.getName(),
								true);
						mItemList.add(item);
						mItemListLock.writeLock().unlock();
					}

					if (mThumbnailer != null && item != null
							&& !item.isThumbnailExist())
						mThumbnailer.addJob(item);
					if (isStopping) {
						Logger.d(TAG, "Stopping scan");
						return;
					}
				}
			} finally {
				final List<Media> itemList = getVideoItems();
				if (mHandler != null && mScanListener != null) {
					mHandler.post(new Runnable() {

						@Override
						public void run() {
							mScanListener.onScanStop(itemList);
						}
					});
				}
				Logger.d(TAG, "scan stop");
				Logger.d(TAG, "itemList:" + itemList.size());
				// 删除不存在的记录和缩略图
				if (!isStopping
						&& Environment.getExternalStorageState().equals(
								Environment.MEDIA_MOUNTED)) {
					for (String fileURI : addedLocations) {
						existingMedias.remove(fileURI);
					}
					DBManager.removeMedias(existingMedias.keySet());
					for (String fileName : existingMedias.keySet())
						Util.deleteFile(new File(fileName));
				}
			}
		}
	}

	class MediaItemFilter implements FileFilter {

		@Override
		public boolean accept(File f) {
			boolean accepted = false;
			if (!f.isHidden()) {
				if (f.isDirectory()
						&& !Media.FOLDER_BLACKLIST.contains(f.getPath()
								.toLowerCase())) {
					accepted = true;
				} else {
					String fileName = f.getName().toLowerCase();
					int dotIndex = fileName.lastIndexOf(".");
					if (dotIndex != -1) {
						String fileExt = fileName.substring(dotIndex);
						accepted = Media.VIDEO_EXTENSIONS.contains(fileExt);
					}
				}
			}
			return accepted;
		}
	}

	public ArrayList<Media> getVideoItems() {
		ArrayList<Media> videoItems = new ArrayList<Media>();
		mItemListLock.readLock().lock();
		for (int i = 0; i < mItemList.size(); i++) {
			Media item = mItemList.get(i);
			if (item != null) {
				videoItems.add(item);
			}
		}
		mItemListLock.readLock().unlock();
		return videoItems;
	}

	public void setScanListener(IScanListener listener, Handler handler) {
		mScanListener = listener;
		mHandler = handler;
	}

	public Handler getHandler() {
		return mHandler;
	}

	public IScanListener getScanListener() {
		return mScanListener;
	}

	public void clearListener() {
		mScanListener = null;
		mHandler = null;
	}

	/**
	 * 设置默认缩略图的图片id
	 * 
	 * @param id
	 */
	public void setDefaultThumbnailID(int id) {
		Media.mDefaultThumbnailID = id;
	}

}
