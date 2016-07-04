package com.youku.thumbnailer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.baseproject.utils.Logger;
import com.youku.local.Util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;

public class UThumbnailer {

	public static String mThumbnailFolderPath;
	public static final String THUMBNAIL_FOLDER = ".local_thumbnail";
	public static final String TAG = "UThumbnailer";
	public static final String PATH_BREAK = "/";
	public static final String REPLACE_BREAK = "-_-";
	public static final String FILE_EXTENSION = ".jpeg";
	public static Context mContext;

	static {
		System.loadLibrary("jpeg");

		System.loadLibrary("uffmpeg");

		System.loadLibrary("thumbnailer");
	}

	/**
	 * @brief 生成视频略缩图
	 * @author Baowang
	 * @param[in] inputFile, 输入文件路径 outputFile, 输出文件路径 imageFormat,
	 *            输出图片的格式，暂时只支持Jpeg width, height, 略缩图的宽高 seekT, 把视频的第几秒作为略缩图
	 *            filmStripOverlay, 是否为略缩图加上胶片效果
	 */
	public static synchronized native int genThumbnail(String inputFile,
			String outputFile, String imageType, int width, int height,
			int seekTime, boolean filmStripOverlay);

	public static Boolean creatThumbnailFolder() {
		File dir = new File(mThumbnailFolderPath);
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				Logger.e(TAG, dir + " mkdir fail!");
				return false;
			}
		}

		return true;
	}

	public static void saveThumbnail(Bitmap bm, String filePath) {
		Logger.d(TAG, "saveThumbnail");
		String fileName = filePath.replace(PATH_BREAK, REPLACE_BREAK)
				+ FILE_EXTENSION;
		Logger.e(TAG + "mThumbnailFolderPath:" + mThumbnailFolderPath);
		File dir = new File(mThumbnailFolderPath);
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				Logger.e(TAG, dir + " mkdir fail!");
			}
		}

		try {
			File file = new File(mThumbnailFolderPath, fileName);
			file.createNewFile();
			FileOutputStream fOut = new FileOutputStream(file);
			bm.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
			fOut.flush();
			fOut.close();
		} catch (FileNotFoundException e) {
			Logger.e(TAG, e);
		} catch (IOException e) {
			Logger.e(TAG, e);
		}

	}

	public static class ThumbnailRegion {
		public int width;
		public int height;
	}

	public static Bitmap getThumbnail(String filePath) {
		Logger.d(TAG, "getThumbnail");
		Bitmap bm = null;
		String fileName = filePath.replace(PATH_BREAK, REPLACE_BREAK)
				+ FILE_EXTENSION;

		Logger.d(TAG, "fileName = " + fileName);
		File file = new File(mThumbnailFolderPath, fileName);
		if (file.exists()) {
			Logger.d(TAG, "filePath = " + file.getAbsolutePath());
			bm = BitmapFactory.decodeFile(file.getAbsolutePath());
		} else {
			Logger.d(TAG, file + " is not exists!");
		}

		return bm;
	}

	public static Drawable getDrawable(String filePath) {
		BitmapDrawable d = null;
		String fileName = filePath.replace(PATH_BREAK, REPLACE_BREAK)
				+ FILE_EXTENSION;

		File file = new File(mThumbnailFolderPath, fileName);
		if (file.exists()) {
			d = new BitmapDrawable(file.getPath());
			if (d.getBitmap() == null)
				return null;
		} else {
			Logger.d(TAG, file + " is not exists!");
		}

		return d;
	}

	public static String getThumailPath(String path) {
		String pathName = path.replace(PATH_BREAK, REPLACE_BREAK)
				+ FILE_EXTENSION;
		path = mThumbnailFolderPath + PATH_BREAK + pathName;
		return path;
	}

	public static String getVideoPath(String path) {
		String videoPath = null;
		path = path.replace(REPLACE_BREAK, PATH_BREAK);
		videoPath = path.substring(0, path.length() - FILE_EXTENSION.length());
		return videoPath;

	}

	public static void setThumbnailerPath(Context context) {
		mThumbnailFolderPath = Environment.getExternalStorageDirectory()
				.getPath()
				+ "/Android/data/"
				+ context.getPackageName()
				+ File.separator + "files" + File.separator + THUMBNAIL_FOLDER;
	}

	public static void deleteThumbnailerFolder() {
		File thumbnailFolder = new File(mThumbnailFolderPath);
		if (thumbnailFolder.exists() && thumbnailFolder.isDirectory()) {
			Util.deleteFile(thumbnailFolder);
		}
	}

}