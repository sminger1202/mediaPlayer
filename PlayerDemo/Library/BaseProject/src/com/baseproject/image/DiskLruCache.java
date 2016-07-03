/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.baseproject.image;

import java.io.File;
import java.io.FilenameFilter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
//import com.youku.util.Logger;

/**
 * A simple disk LRU bitmap cache to illustrate how a disk cache would be used
 * for bitmap caching. A much more robust and efficient disk LRU cache solution
 * can be found in the ICS source code
 * (libcore/luni/src/main/java/libcore/io/DiskLruCache.java) and is preferable
 * to this simple implementation.
 */
public class DiskLruCache {
	private static final String TAG = "DiskLruCache";
	private static final String CACHE_FILENAME_PREFIX = "cache_";
	private static final int MAX_REMOVALS = 4;
	// private static final int INITIAL_CAPACITY = 32;
	// private static final float LOAD_FACTOR = 0.75f;
	private static final int INITIAL_CAPACITY;
	private static final float LOAD_FACTOR;

	private final File mCacheDir;
	private int cacheSize = 0;
	private int cacheByteSize = 0;
	private final int maxCacheItemSize = 128; // 128 item default
	private long maxCacheByteSize = 1024 * 1024 * 10; // 10MB default
	private CompressFormat mCompressFormat = CompressFormat.PNG;
	private int mCompressQuality = 70;

	static {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			INITIAL_CAPACITY = 0;
			LOAD_FACTOR = 0.75f;
		} else {
			INITIAL_CAPACITY = 10;
			LOAD_FACTOR = 1.1f;
		}
	}

	private final Map<String, String> mLinkedHashMap = Collections
			.synchronizedMap(new LinkedHashMap<String, String>(
					INITIAL_CAPACITY, LOAD_FACTOR, true));

	/**
	 * A filename filter to use to identify the cache filenames which have
	 * CACHE_FILENAME_PREFIX prepended.
	 */
	private static final FilenameFilter cacheFileFilter = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String filename) {
			return filename.startsWith(CACHE_FILENAME_PREFIX);
		}
	};

	/**
	 * Used to fetch an instance of DiskLruCache.
	 * 
	 * @param context
	 * @param cacheDir
	 * @param maxByteSize
	 * @return
	 */
	public static DiskLruCache openCache(Context context, File cacheDir,
			long maxByteSize) {
		if (!cacheDir.exists()) {
			cacheDir.mkdir();
		}

		if (cacheDir.isDirectory() && cacheDir.canWrite()) {
			if (Utils.getUsableSpace(cacheDir) > maxByteSize) {
				return new DiskLruCache(cacheDir, maxByteSize);
			} else {
				return new DiskLruCache(cacheDir, maxByteSize / 2);
			}
		}

		return null;
	}

	/**
	 * Constructor that should not be called directly, instead use
	 * {@link DiskLruCache#openCache(Context, File, long)} which runs some extra
	 * checks before creating a DiskLruCache instance.
	 * 
	 * @param cacheDir
	 * @param maxByteSize
	 */
	private DiskLruCache(File cacheDir, long maxByteSize) {
		mCacheDir = cacheDir;
		maxCacheByteSize = maxByteSize;
	}

	/**
	 * Add a bitmap to the disk cache.
	 * 
	 * @param key
	 *            A unique identifier for the bitmap.
	 * @param data
	 *            The bitmap to store.
	 */
	public void put(String key, Bitmap data) {
		synchronized (mLinkedHashMap) {
			if (mLinkedHashMap.get(key) == null) {
				// try {
				final String file = createFilePath(mCacheDir, key);
				// if (writeBitmapToFile(data, file)) {
				put(key, file);
				flushCache();
				// }
			}
			// catch (final FileNotFoundException e) {
			// Log.e(TAG, "Error in put: " + e.getMessage());
			// } catch (final IOException e) {
			// Log.e(TAG, "Error in put: " + e.getMessage());
			// }
			// }
		}
	}

	private void put(String key, String file) {
		final long fileLength = new File(file).length();
		if (fileLength == 0) {
			mLinkedHashMap.remove(key);
			cacheSize = mLinkedHashMap.size();
			return;
		}
		mLinkedHashMap.put(key, file);
		cacheSize = mLinkedHashMap.size();
		cacheByteSize += fileLength;

	}

	/**
	 * Flush the cache, removing oldest entries if the total size is over the
	 * specified cache size. Note that this isn't keeping track of stale files
	 * in the cache directory that aren't in the HashMap. If the images and keys
	 * in the disk cache change often then they probably won't ever be removed.
	 */
	private void flushCache() {
		Entry<String, String> eldestEntry;
		File eldestFile;
		long eldestFileSize;
		int count = 0;

		while (count < MAX_REMOVALS
				&& (cacheSize > maxCacheItemSize || cacheByteSize > maxCacheByteSize)) {
			eldestEntry = mLinkedHashMap.entrySet().iterator().next();
			eldestFile = new File(eldestEntry.getValue());
			eldestFileSize = eldestFile.length();
			mLinkedHashMap.remove(eldestEntry.getKey());
			eldestFile.delete();
			cacheSize = mLinkedHashMap.size();
			cacheByteSize -= eldestFileSize;
			count++;
//			Logger.d(TAG, "flushCache - Removed cache file, " + eldestFile
//					+ ", " + eldestFileSize);
		}
	}

	/**
	 * 在Cache中移除指定的文件
	 */
	public void removeFile(String key) {
		final String file = mLinkedHashMap.get(key);
		if (null != file) {
			final File f = new File(file);
			final long fLength = f.length();
			if (f.exists()) {
				if (f.delete()) {
					mLinkedHashMap.remove(file);
					cacheSize = mLinkedHashMap.size();
					cacheByteSize -= fLength;
				}
			}
		}
	}

	/**
	 * Get an image from the disk cache.
	 * 
	 * @param key
	 *            The unique identifier for the bitmap
	 * @return The bitmap or null if not found
	 */
	public Bitmap get(String key) {
		synchronized (mLinkedHashMap) {
			try {
				final String file = mLinkedHashMap.get(key);
				if (file != null) {
//					Logger.d(TAG, "Disk cache hit");
					return BitmapFactory.decodeFile(file);
				} else {
					final String existingFile = createFilePath(mCacheDir, key);
					if (new File(existingFile).exists()) {
						put(key, existingFile);
//						Logger.d(TAG, "Disk cache hit (existing file)");
						return BitmapFactory.decodeFile(existingFile);
					}
				}
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
				clearCache();
				System.gc();
				return null;
			}
			return null;
		}
	}

	/**
	 * Checks if a specific key exist in the cache.
	 * 
	 * @param key
	 *            The unique identifier for the bitmap
	 * @return true if found, false otherwise
	 */
	public boolean containsKey(String key) {
		// See if the key is in our HashMap
		if (mLinkedHashMap.containsKey(key)) {
			return true;
		}

		// Now check if there's an actual file that exists based on the key
		// final String existingFile = createFilePath(mCacheDir, key);
		// if (new File(existingFile).exists()) {
		// // File found, add it to the HashMap for future use
		// put(key, existingFile);
		// return true;
		// }
		return false;
	}

	/**
	 * Removes all disk cache entries from this instance cache dir
	 */
	public void clearCache() {
		DiskLruCache.clearCache(mCacheDir);
	}

	/**
	 * Removes all disk cache entries from the application cache directory in
	 * the uniqueName sub-directory.
	 * 
	 * @param context
	 *            The context to use
	 * @param uniqueName
	 *            A unique cache directory name to append to the app cache
	 *            directory
	 */
	public static void clearCache(Context context, String uniqueName) {
		File cacheDir = getDiskCacheDir(context, uniqueName);
		clearCache(cacheDir);
	}

	/**
	 * Removes all disk cache entries from the given directory. This should not
	 * be called directly, call {@link DiskLruCache#clearCache(Context, String)}
	 * or {@link DiskLruCache#clearCache()} instead.
	 * 
	 * @param cacheDir
	 *            The directory to remove the cache files from
	 */
	private static void clearCache(File cacheDir) {
		final File[] files = cacheDir.listFiles(cacheFileFilter);
		if (null != files) {
			for (int i = 0; i < files.length; i++) {
				files[i].delete();
			}
		}
	}

	/**
	 * Get a usable cache directory (external if available, internal otherwise).
	 * 
	 * @param context
	 *            The context to use
	 * @param uniqueName
	 *            A unique directory name to append to the cache dir
	 * @return The cache dir
	 */
	public static File getDiskCacheDir(Context context, String uniqueName) {

		// Check if media is mounted or storage is built-in, if so, try and use
		// external cache dir
		// otherwise use internal cache dir
		final String cachePath = ((Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState()) || !Utils
				.isExternalStorageRemovable()) && (null != Utils
				.getExternalCacheDir(context))) ? Utils.getExternalCacheDir(
				context).getPath() : context.getCacheDir().getPath();

		return new File(cachePath + File.separator + uniqueName);
	}

	/**
	 * Creates a constant cache file path given a target cache directory and an
	 * image key.
	 * 
	 * @param cacheDir
	 * @param key
	 * @return
	 */
	public static String createFilePath(File cacheDir, String key) {
		try {
			// Use URLEncoder to ensure we have a valid filename, a tad hacky
			// but it will do for
			// this example
			return cacheDir.getAbsolutePath() + File.separator
					+ CACHE_FILENAME_PREFIX
					+ URLEncoder.encode(key.replace("*", ""), "UTF-8");
		} catch (final UnsupportedEncodingException e) {
			Log.e(TAG, "createFilePath - " + e);
		}

		return null;
	}

	/**
	 * Create a constant cache file path using the current cache directory and
	 * an image key.
	 * 
	 * @param key
	 * @return
	 */
	public String createFilePath(String key) {
		return createFilePath(mCacheDir, key);
	}

	/**
	 * Sets the target compression format and quality for images written to the
	 * disk cache.
	 * 
	 * @param compressFormat
	 * @param quality
	 */
	public void setCompressParams(CompressFormat compressFormat, int quality) {
		mCompressFormat = compressFormat;
		mCompressQuality = quality;
	}
	
	private long getFileSize(File cacheDir) {
		long size = 0;
		File flist[] = cacheDir.listFiles();
		if (flist == null) {
			return size;
		}
		for (int i = 0; i < flist.length; i++) {
			if (flist[i].isDirectory()) {
				size = size + getFileSize(flist[i]);
			} else {
				size = size + flist[i].length();
			}
		}
		return size;
	}
	
	public String getFileSize(){
		return Utils.formatSizeM(getFileSize(mCacheDir));
	}

}
