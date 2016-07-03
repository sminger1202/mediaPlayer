package com.youku.libmanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import com.baseproject.utils.Logger;

public class SoUpgradeStatics {

	public static final String PREFS_NAME = "library_manager";
	
	public static final String DOWNLOAD_PATH = "download_path";
	
	public static final String LIB_FFMPEG_SO_NAME = "libuffmpeg.so";
	
	public static final String LIB_UPLAYER_22_SO_NAME = "libuplayer22.so";
	
	public static final String LIB_UPLAYER_23_SO_NAME = "libuplayer23.so";
	
	public static final String LIB_ACCSTUB_SO_NAME = "libaccstub.so";


	public static final String DOWNLOAD_FOLDER = "/app_libs/";

	public static final String INDEPENDENT_DOWNLOAD_FOLDER = "/independent_libs/";
	
	public static void saveDownloadPath(Context context, String path) {
		Editor editor = context.getSharedPreferences(PREFS_NAME, 0).edit();
		editor.putString(DOWNLOAD_PATH, path);
		
		editor.commit();
	}
	
	public static String getDownloadPath(Context context) {
		Logger.d(SoUpgradeService.TAG, "context = " + context);
		
		SharedPreferences savedata = context.getSharedPreferences(PREFS_NAME, 0);
		String path = savedata.getString(DOWNLOAD_PATH, "");
		
		Logger.d(SoUpgradeService.TAG, "path = " + path);
		return path;
	}
	
	public static String getFfmpegSo(Context context) {
		
		return getDownloadPath(context) + LIB_FFMPEG_SO_NAME;
	}
	
	
	public static String getUplayer22So(Context context) {
		if (isUpgraded(context)) {
			return null;
		}
		
		return getDownloadPath(context) + LIB_UPLAYER_22_SO_NAME;
	}

	public static String getUplayer23So(Context context) {
		if (isUpgraded(context)) {
			return "";
		}
		
		return getDownloadPath(context) + LIB_UPLAYER_23_SO_NAME;
	}

	public static String getAccSo(Context context) {
		if (isUpgraded(context)) {
			return "";
		}
		return getDownloadPath(context) + LIB_ACCSTUB_SO_NAME;
	}

	public static String getDRMSo(Context context) {
		return "/data/data/" + context.getPackageName() + INDEPENDENT_DOWNLOAD_FOLDER + SoUpgradeService.LIB_DRM_SO_NAME;
	}

	public static Boolean isUpgraded(Context context) {
		Boolean upgrade = false;
		String curVersionName = "";
		String savedVersionName;
		int curVersionCode = 0;
		int savedVersionCode;
		
		PackageManager manager = context.getPackageManager();
		PackageInfo info;
		
		try {
			info = manager.getPackageInfo(context.getPackageName(), 0);
			curVersionName = info.versionName;
			curVersionCode = info.versionCode;
			
			savedVersionName = SoUpgradeService.getVersionName(context);
			savedVersionCode = SoUpgradeService.getVersionCode(context);
			
			Logger.d(SoUpgradeService.TAG, "savedVersionName = " + savedVersionName + ", savedVersionCode = " + savedVersionCode
					+ ", curVersionName = " + curVersionName + ", curVersionCode= " + curVersionCode);

			if (savedVersionCode == curVersionCode && savedVersionName.equals(curVersionName)) {
				upgrade = false;
			} else {
				upgrade = true;
			}
			 
		} catch (NameNotFoundException e) {
			Logger.e(SoUpgradeService.TAG, e);
			return upgrade;
		}
		
		return upgrade;
	}

	
}
