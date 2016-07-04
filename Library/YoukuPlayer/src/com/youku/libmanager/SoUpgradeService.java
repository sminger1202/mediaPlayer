package com.youku.libmanager;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.baseproject.utils.Logger;
import com.baseproject.utils.UIUtils;
import com.baseproject.utils.Util;
import com.youku.analytics.data.Device;
import com.youku.player.network.HttpIntent;
import com.youku.player.network.HttpRequestManager;
import com.youku.player.network.IHttpRequest;
import com.youku.player.network.IHttpRequest.IHttpRequestCallBack;
import com.youku.player.network.YoukuService;
import com.youku.service.acc.AcceleraterServiceManager;
import com.youku.uplayer.MediaPlayerProxy;
import com.youku.uplayer.UMediaPlayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SoUpgradeService extends Service {

	public static final String TAG = "SoUpgradeService";

	public static final String YOUKU_PHONE_PACKAGE_NAME = "com.youku.phone";

	public static final String TUDOU_PHONE_PACKAGE_NAME = "com.tudou.android";

	public static final String TUDOU_PAD_PACKAGE_NAME = "com.tudou.xoom.android";

	public static final String SMART_TV_PACKAGE_NAME =  "com.youku.tv";

	public static final String PREFS_NAME = "library_manager";

	public static final String CHECK_TIME = "check_time";

	public static final String UPGRADE_FLAG = "upgrade_flag";

	public static final String VERSION_NAME = "version_name";

	public static final String VERSION_CODE = "version_code";

	public static final String WIRELESS_PID = "wireless_pid";

	public static final String TIME_STAMP = "time_stamp";

	public static final String SECRET = "secret";

	public static final String SO_VERSION = "so_version_";

	public static final int INTERVAL_CHECK_TIME = 24;//升级检查间隔为24小时
	//public static final int INTERVAL_CHECK_MIN_TIME = 2;//测试间隔时间2分钟

	public static final int SPECIFY_VERSION_UPGRADE_TYPE = 0;

	public static final int ALL_VERSION_UPGRADE_TYPE = 1;

	public static final String LIB_FFMPEG_SO_NAME = "libuffmpeg.so";

	public static final String LIB_UPLAYER_22_SO_NAME = "libuplayer22.so";

	public static final String LIB_UPLAYER_23_SO_NAME = "libuplayer23.so";

	public static final String LIB_ACCSTUB_SO_NAME = "libaccstub.so";

    public static final String LIB_DRM_SO_NAME = "libWasabiJni.so";

	public static final String TEMP_SO_SUFFIX = ".tmp";

	private String mRequestUrl;

	String mPackageName = null;

	private int mTryTimes = 3;

	private int SINGLE_TASK_TRY_TIMES = 5;

	private String os = "android";

	public  String product = "";

	private String arch;

	private String archPlatform; //arm ? x86 ?...

	public static long timestamp;

	public static String secret;

	public static String pid;

	private String guid;

	public static String mDownloadPath = null;

	private static String mIndependentDownloadPath = null;

	private Thread mDownloadThread;

	private boolean isFirstReceive = true;

	private boolean isNetworkReceiverRegist = false;

	private boolean enableDownload = true;

	private Map<String, SingleSoDownloadTask> mSingleSoDownloadTasks = new HashMap<>();

    public static final int FLAG_START_SERVICE_FROM_MANAGER = 1;

    private Handler mHandler = new Handler();

	private ISoUpgradeCallback mSoUpgradeCallback;

	private ISoUpgradeService.Stub mBinder = new ISoUpgradeService.Stub() {
		@Override
		public void startDownloadSo(final String soName) throws RemoteException {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					try {
						if (isSoDownloaded(soName))
							return;
					} catch (RemoteException e) {
						Logger.e(TAG, e);
					}
					SingleSoDownloadTask singleSoDownloadTask = mSingleSoDownloadTasks.get(soName);
					if (singleSoDownloadTask != null) {
						if (singleSoDownloadTask.mDownloadState == DownloadSate.DOWNLOADING) {
							Logger.d(TAG, "startDownloadSo mDownloadState==DownloadSate.DOWNLOADING return");
							return;
						} else
							singleSoDownloadTask.startDownload();
					} else {
						if (mOnlineSoInfos.isEmpty()) {
							Util.TIME_STAMP = getTimeStamp(getApplicationContext());
							initRequestUrl();
							IHttpRequest httpRequest = YoukuService.getService(
									IHttpRequest.class, true);
							String url = UrlUtils.getRequestUrl(mRequestUrl, product, os, arch, pid, guid);
							Logger.d(TAG, "url = " + url);
							HttpIntent httpIntent = new HttpIntent(url);
							if (httpRequest == null)
								return;
							httpRequest.request(httpIntent, new IHttpRequestCallBack() {

								@Override
								public void onFailed(String failReason) {
									Logger.d(TAG, "startDownloadSo onFailed : " + failReason);
									Logger.d(TAG, "startDownloadSo fail");
									try {
										if (mSoUpgradeCallback != null)
											mSoUpgradeCallback.onDownloadFailed(soName);
									} catch (RemoteException e) {
										Logger.e(TAG, e);
									}
								}

								@Override
								public void onSuccess(HttpRequestManager httpRequestManager) {
									String jsonString = httpRequestManager.getDataString();
									Logger.d(TAG, "onSuccess");
									if (!TextUtils.isEmpty(jsonString)) {
										parseJson(jsonString);
										downloadSpecifiedSo(soName);
									} else
										Logger.d(TAG, "no response string");
								}
							});
						} else {
							downloadSpecifiedSo(soName);
						}
					}
				}
			});
		}

		private void downloadSpecifiedSo(String soName) {
			if (mOnlineSoInfos != null && mOnlineSoInfos.containsKey(soName)) {
				List<SoInfo> infoList = mOnlineSoInfos.get(soName);
				if (infoList != null) {
					for (SoInfo info : infoList) {
						SingleSoDownloadTask soDownloadTask = new SingleSoDownloadTask(info);
						mSingleSoDownloadTasks.put(info.name, soDownloadTask);
						soDownloadTask.startDownload();
					}
				} else
					Logger.d(TAG, "no " + soName + " info");
			} else
				Logger.d(TAG, "no " + soName + " in OnlineSoInfo");
		}

		@Override
		public boolean isSoDownloaded(String soName) throws RemoteException {
			SingleSoDownloadTask singleSoDownloadTask = mSingleSoDownloadTasks.get(soName);
			Logger.d(TAG, "getSingleDownloadSoVersion:" + getSingleDownloadSoVersion(soName));
			if ((singleSoDownloadTask != null && singleSoDownloadTask.mDownloadState == DownloadSate.DOWNLOADED)) {
				Logger.d(TAG, "isSoDownloaded:" + singleSoDownloadTask.mDownloadState);
				return true;
			} else if (FileUtils.isFileExist(mIndependentDownloadPath + soName))
				return true;
			else
				return false;
		}

		@Override
		public void registerCallback(ISoUpgradeCallback callback) throws RemoteException {
			mSoUpgradeCallback = callback;
		}
	};

	private BroadcastReceiver networkReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (isFirstReceive) {
				isFirstReceive = false;
				return;
			}
            boolean hasInternet = Util.hasInternet();
            if (hasInternet && Util.isWifi() && !mSingleSoDownloadTasks.isEmpty()) {
                for (Entry<String, SingleSoDownloadTask> entry : mSingleSoDownloadTasks.entrySet()) {
                    if (entry.getValue().mDownloadState == DownloadSate.STOP) {
                        Logger.d(TAG, "continue downloading:" + entry.getKey());
                        entry.getValue().startDownload();
                    }
                }
            }

			if (!hasInternet || hasInternet && !Util.isWifi()) {
                Logger.d(TAG, "wifi does not available!");
                enableDownload = false;
                stopService();
            }
        }

	};

    @Override
    public IBinder onBind(Intent arg0) {
        Logger.d(TAG, "onBind");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Logger.d(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

	private boolean initData() {

		pid = Device.pid;

		guid = Device.guid;

		Util.TIME_STAMP = getTimeStamp(this);

		Util.SECRET = getSecret(this);

		Logger.d(TAG, "pid = " + pid + ", guid = " + guid + ", timestamp = " + Util.TIME_STAMP + ", secret = " + Util.SECRET);

		if (initProduct() && initRequestUrl() && initDownloadPath() && initArch()) {
			return true;
		}

		return false;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Logger.d(TAG,"onCreate soupgrade service");
		if (!initData()) {
			stopService();
			return;
		}
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(networkReceiver, intentFilter);
		isNetworkReceiverRegist = true;
	}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.d(TAG, "onStartCommand:" + intent);
        if (intent != null && intent.getIntExtra("flag", 0) == FLAG_START_SERVICE_FROM_MANAGER)
            return START_STICKY;

        //检查网络是否为wifi，不是则退出该服务
        if (!Util.hasInternet() || Util.hasInternet() && !Util.isWifi()) {
            stopService();
            return START_STICKY;
        }


        if (!needStartService()) {
            stopService();
            return START_STICKY;
        }

        checkLibrary();
        return START_STICKY;
    }

    @Override
	public void onDestroy() {
		super.onDestroy();

		if (isNetworkReceiverRegist) {
			unregisterReceiver(networkReceiver);
		}

		Logger.d(TAG, "onDestroy soupgrade service");
	}


	private boolean initRequestUrl() {

		if (product.contains("youku")) {
			mRequestUrl = UrlUtils.getYoukuRequestUrl();
		} else if (product.contains("tudou")){
			mRequestUrl = UrlUtils.getTudouRequestUrl();
		} else {
			return false;
		}

		return true;

	}

	private void setUpgradeFlag() {
		if (mUpgradeSoInfos.size() == mDownloadedSoInfos.size()) {
			Logger.d(TAG, "so upgrade success");
			saveUpgradeFlag(true);
		} else {
			Logger.d(TAG, "so upgrade fail");
			saveUpgradeFlag(false);
		}
	}


	private void saveUpgradeFlag(Boolean flag) {
		Editor editor = getSharedPreferences(PREFS_NAME, MODE_MULTI_PROCESS).edit();
		editor.putBoolean(UPGRADE_FLAG, flag);

		editor.commit();
	}

	private Boolean getUpgradeFlag() {
		SharedPreferences savedata = getSharedPreferences(PREFS_NAME, MODE_MULTI_PROCESS);
		Boolean flag = savedata.getBoolean(UPGRADE_FLAG, false);

		return flag;
	}

	public Boolean isApkUpgraded(Context context) {
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

			savedVersionName = getVersionName(context);
			savedVersionCode = getVersionCode(context);

			Logger.d(TAG, "savedVersionName = " + savedVersionName + ", savedVersionCode = " + savedVersionCode
					+ ", curVersionName = " + curVersionName + ", curVersionCode= " + curVersionCode);

			if (savedVersionCode == curVersionCode && savedVersionName.equals(curVersionName)) {
				upgrade = false;
			} else {
				upgrade = true;
				saveVersionName(context, curVersionName);
				saveVersionCode(context, curVersionCode);
			}

		} catch (NameNotFoundException e) {
			Logger.e(TAG, e);
			return upgrade;
		}

		return upgrade;
	}

	public static void saveVersionName(Context context, String versionName) {
		Editor editor = context.getSharedPreferences(PREFS_NAME, MODE_MULTI_PROCESS).edit();
		editor.putString(VERSION_NAME, versionName);

		editor.commit();
	}

	public static String getVersionName(Context context) {
		SharedPreferences savedata = context.getSharedPreferences(PREFS_NAME, MODE_MULTI_PROCESS);

		return savedata.getString(VERSION_NAME, "");
	}

	public static void saveVersionCode(Context context, int versionCode) {
		Editor editor = context.getSharedPreferences(PREFS_NAME, MODE_MULTI_PROCESS).edit();
		editor.putInt(VERSION_CODE, versionCode);

		editor.commit();
	}

	public static int getVersionCode(Context context) {
		SharedPreferences savedata = context.getSharedPreferences(PREFS_NAME, MODE_MULTI_PROCESS);

		return savedata.getInt(VERSION_CODE, -1);
	}

	public static void savePid(Context context, String pid) {
		Editor editor = context.getSharedPreferences(PREFS_NAME, MODE_MULTI_PROCESS).edit();
		editor.putString(WIRELESS_PID, pid);

		editor.commit();
	}

	public static String getPid(Context context) {
		SharedPreferences savedata = context.getSharedPreferences(PREFS_NAME, MODE_MULTI_PROCESS);
		return savedata.getString(WIRELESS_PID, "");
	}

	public static void saveTimeStamp(Context context, long timeStamp) {
		Editor editor = context.getSharedPreferences(PREFS_NAME, MODE_MULTI_PROCESS).edit();
		editor.putLong(TIME_STAMP, timeStamp);

		editor.commit();
	}

	public static long getTimeStamp(Context context) {
		SharedPreferences savedata = context.getSharedPreferences(PREFS_NAME, MODE_MULTI_PROCESS);
		long timeStamp = savedata.getLong(TIME_STAMP, 0);
		Logger.d(TAG, "getTimeStamp:" + timeStamp);
		return timeStamp;
	}

	public static void saveSecret(Context context, String secret) {
		Editor editor = context.getSharedPreferences(PREFS_NAME, MODE_MULTI_PROCESS).edit();
		editor.putString(SECRET, secret);

		editor.commit();
	}

	public static String getSecret(Context context) {
		SharedPreferences savedata = context.getSharedPreferences(PREFS_NAME, MODE_MULTI_PROCESS);
		return savedata.getString(SECRET, "");
	}

	private void setCheckTime(long time) {
		Editor editor = getSharedPreferences(PREFS_NAME, MODE_MULTI_PROCESS).edit();
		editor.putLong(CHECK_TIME, time);

		editor.commit();
	}

	private long getLastCheckTime() {
		SharedPreferences savedata = getSharedPreferences(PREFS_NAME, MODE_MULTI_PROCESS);
		long checkTime = savedata.getLong(CHECK_TIME, 0);

		return checkTime;
	}

	private void saveSingleDownloadSoVersion(String soName, int version) {
		Editor editor = getSharedPreferences(PREFS_NAME, MODE_MULTI_PROCESS).edit();
		editor.putInt(SO_VERSION + soName, version);
		editor.commit();
	}

	private int getSingleDownloadSoVersion(String soName) {
		SharedPreferences saveDate = getSharedPreferences(PREFS_NAME, MODE_MULTI_PROCESS);
		int version = saveDate.getInt(SO_VERSION + soName, 0);
		return version;
	}

	public String convertSecondToHour(long second) {
		long hour, min, sec;

		hour = second / 3600;
		second = second % 3600;
		min = second / 60;
		sec = second % 60;

		return hour + ":" + min + ":" +sec;
	}

	public Boolean needStartService() {
		Logger.d(TAG, "needStartService");

		long currentTime = System.currentTimeMillis();
		long lastCheckTime = getLastCheckTime();

		if (lastCheckTime == 0) {
			Logger.d(TAG, "need start service");
			setCheckTime(currentTime);
			return true;
		}

		Logger.d(TAG, "intervalTime = " + convertSecondToHour((currentTime - lastCheckTime) / 1000));

		/*if (!getUpgradeFlag() || (currentTime > lastCheckTime) && (currentTime - lastCheckTime)/1000/60 > INTERVAL_CHECK_MIN_TIME) {
			Logger.d(TAG, "need start service");
			setCheckTime(currentTime);
			return true;
		}*/

		if (!getUpgradeFlag() || (currentTime > lastCheckTime) && (currentTime - lastCheckTime)/1000/60/60 > INTERVAL_CHECK_TIME) {
			Logger.d(TAG, "need start service");
			setCheckTime(currentTime);
			return true;
		}

		Logger.d(TAG, "do not need start service");
		return false;

	}

	public boolean initProduct() {
		Logger.d(TAG, "getProduct()");

		PackageInfo info;

		try {
			info = getPackageManager().getPackageInfo(getPackageName(), 0);
			mPackageName = info.packageName;
			Logger.d(TAG, "packageNames = " + mPackageName);
		} catch (NameNotFoundException e) {
			Logger.e(TAG, e);
		}

		if (mPackageName == null) {
			return false;
		}

		if (mPackageName.equalsIgnoreCase(YOUKU_PHONE_PACKAGE_NAME) || mPackageName.contains(YOUKU_PHONE_PACKAGE_NAME)) {
			if (UIUtils.isTablet(this)) {
				product = "youku%20pad";
			} else {
				product = "youku%20phone";
			}
		} else if (mPackageName.equalsIgnoreCase(TUDOU_PHONE_PACKAGE_NAME) || mPackageName.contains(TUDOU_PHONE_PACKAGE_NAME) ) {

			product = "tudou%20phone";

		} else if (mPackageName.equalsIgnoreCase(TUDOU_PAD_PACKAGE_NAME)) {

			product = "tudou%20pad";

		} else if (mPackageName.equalsIgnoreCase(SMART_TV_PACKAGE_NAME)) {
			product = "youku%20tv";
		} else {
			return false;
		}

		Logger.d(TAG, "product = " + product);

		return true;
	}

	public boolean initArch() {
		Logger.d(TAG, "initArch()");

		String strLine = null;
		boolean hasNeon = false;

		arch = android.os.Build.CPU_ABI.toLowerCase();

		if (arch.equals("x86")){
			archPlatform = "x86";
			return true;
		} else if (arch.contains("arm")) {
			archPlatform = "arm";
		}

		try {
			BufferedReader cpuinfoReader = new BufferedReader(new FileReader(
					"/proc/cpuinfo"));
			while ((strLine = cpuinfoReader.readLine()) != null) {
				if (strLine.startsWith("Features")) {
					int idx = strLine.indexOf(':');
					if (idx != -1) {
						strLine = strLine.substring(idx + 1);
						hasNeon = (strLine.indexOf("neon") != -1);
					}
				}
			}
			cpuinfoReader.close();
			cpuinfoReader = null;
		} catch (IOException e) {
			Logger.e(TAG, e);
			return false;
		}

		if (hasNeon) {
			arch += "-neon";
		}

		Logger.d(TAG, "arch = " + arch);

		return true;
	}

	private boolean checkMD5(String path, String md5) {
		Logger.d(TAG, "checkMD5");
		Logger.d(TAG, "path = " + path + ", md5 = " + md5);

		String fileMD5;
		File file = new File(path);
		if(!file.exists() || md5 == null || md5.equals("")) {
			return false;
		}

		fileMD5 = MD5.getMD5(file);
		Logger.d(TAG, "fileMD5 = " + fileMD5);
		if (fileMD5.equalsIgnoreCase(md5)) {
			return true;
		}

		return false;
	}

	private void processDownloadFail(SoInfo so) {
		Logger.d(TAG, "processDownloadFail : " + so.name);

		SoInfo soInfo;
		String relativeSo = so.relativeSo;

		if (relativeSo == null || (relativeSo != null && relativeSo.equals(""))) {
			return;
		}

		String []relativeArray = relativeSo.split(",");

		//删除与他相关的库
		for (int i = 0; i < relativeArray.length; i++) {
			FileUtils.deleteFile(mDownloadPath + relativeArray[i] + TEMP_SO_SUFFIX);
			if (mUpgradeSoInfos.containsKey(relativeArray[i])) {
				soInfo = mUpgradeSoInfos.get(relativeArray[i]);
				soInfo.needDownload = false;

				if (mDownloadedSoInfos.containsKey(relativeArray[i])) {
					mDownloadedSoInfos.remove(relativeArray[i]);
				}
			}

			Logger.d(TAG, so.name + " relative so : " + relativeArray[i] + " does not need download!");
		}

	}

	/**
	 * 下载so库线程
	 */
	private Runnable mdownloadRunnable = new Runnable() {

		@Override
		public void run() {

			int flag = 0;
			HttpDownloader httpDownloader = new HttpDownloader(SoUpgradeService.this);
			Logger.d(TAG, "========== mdownloadRunnable start ============");

			for (Entry<String, SoInfo> entry: mUpgradeSoInfos.entrySet()) {

				if (!enableDownload) {
					setUpgradeFlag();
					Logger.d(TAG, "========== mdownloadRunnable end ============");
					return;
				}

				SoInfo so = entry.getValue();
				Logger.d(TAG, "name = " + so.name + ", download_url = " + so.download_url);

				for (int i = 0; so.needDownload && i < mTryTimes; i++) {

					flag = httpDownloader.downloadFile(so.download_url, mDownloadPath, so.name + TEMP_SO_SUFFIX);

					if (flag == 0) {
						if (!checkMD5(mDownloadPath + so.name + TEMP_SO_SUFFIX, so.md5)) {
							flag = -1;
							FileUtils.deleteFile(mDownloadPath + so.name + TEMP_SO_SUFFIX);
							continue;
						} else {
							break;
						}
					}
				}

				//下载失败
				if (flag < 0) {
					processDownloadFail(so);
				} else {
					mDownloadedSoInfos.put(so.name, so);
				}

			}

			replaceSo();

			setUpgradeFlag();

			Logger.d(TAG, "========== mdownloadRunnable end ============");

			stopService();
		}

	};

	private void replaceSo() {
		Logger.d(TAG, "replaceSo()");

		SoInfo downloadedSo;

		for (Entry<String, SoInfo> entry: mDownloadedSoInfos.entrySet()) {
			downloadedSo = entry.getValue();
			//删除旧库
			FileUtils.deleteFile(mDownloadPath + downloadedSo.name);

			//将下载成功的临时文件改名
			FileUtils.renameFile(mDownloadPath + downloadedSo.name + TEMP_SO_SUFFIX, mDownloadPath + downloadedSo.name);
			Logger.d(TAG, "rename " + downloadedSo.name + TEMP_SO_SUFFIX + " ---> " + downloadedSo.name);

			if (downloadedSo.name.equals(LIB_ACCSTUB_SO_NAME)) {
				killAcceleraterProcess();
			}
		}

	}

	public boolean initDownloadPath() {
        mDownloadPath = "/data/data/" + mPackageName + SoUpgradeStatics.DOWNLOAD_FOLDER;
        mIndependentDownloadPath = "/data/data/" + mPackageName + SoUpgradeStatics.INDEPENDENT_DOWNLOAD_FOLDER;
        FileUtils.creatDir(mIndependentDownloadPath);
        if (isApkUpgraded(this)) {
            Logger.d(TAG, "apk upgraded!");
            FileUtils.deleteDirectory(mDownloadPath);
        }

        if (FileUtils.creatDir(mDownloadPath) == null) {
            return false;
        }

        SoUpgradeStatics.saveDownloadPath(this, mDownloadPath);

        return true;
    }

	public void makeRequestUrl() {
		mRequestUrl += "os=" + os + "&product=" + product;
	}

	/** 存放需要升级的so信息*/
	private Map<String, SoInfo> mUpgradeSoInfos = new HashMap<String, SoInfo>();

	/** 存放成功下载的so信息*/
	private Map<String, SoInfo> mDownloadedSoInfos = new HashMap<String, SoInfo>();

	/** 存放本地so信息*/
	ArrayList<SoInfo> mLocalSoInfos = new ArrayList<SoInfo>();

	/** 存放在线so信息*/
	Map<String, ArrayList<SoInfo>> mOnlineSoInfos = new HashMap<String, ArrayList<SoInfo>>();

	public void clear() {

		mUpgradeSoInfos.clear();

		mDownloadedSoInfos.clear();

		mLocalSoInfos.clear();

		mOnlineSoInfos.clear();
	}

	public void checkLibrary() {
		if (!mUpgradeSoInfos.isEmpty())
			return;

		clear();

		getLocalSo();

		if (mLocalSoInfos.size() == 0) {
			Logger.d(TAG, "no local libs to upgrade!");
			stopService();
		}
		Util.TIME_STAMP = getTimeStamp(getApplicationContext());
		initRequestUrl();
		IHttpRequest httpRequest = YoukuService.getService(
				IHttpRequest.class, true);
		String url = UrlUtils.getRequestUrl(mRequestUrl, product, os, arch, pid, guid);
		Logger.d(TAG, "url = " + url);
		HttpIntent httpIntent = new HttpIntent(url);

		httpRequest.request(httpIntent, new IHttpRequestCallBack() {

			@Override
			public void onFailed(String failReason) {
				Logger.d(TAG, "onFailed : " + failReason);
				Logger.d(TAG, "so upgrade fail");
				saveUpgradeFlag(false);
				stopService();
			}

			@Override
			public void onSuccess(HttpRequestManager httpRequestManager) {
				String jsonString = httpRequestManager.getDataString();
				Logger.d(TAG, "onSuccess");
				if (!TextUtils.isEmpty(jsonString)) {
					parseJson(jsonString);
					if (isNeedUpgrade()) {
						saveUpgradeFlag(false);
						downloadLibs();
					} else {
						saveUpgradeFlag(true);
						stopService();
					}
				}

			}
		});

	}

	private void getLocalSo() {
		SoInfo so;

		if (MediaPlayerProxy.isUplayerSupported()) {
			String ffmpegVersionName;
			String uplayerVersionName;
			int ffmpegVersionCode;
			int uplayerVersionCode;

			ffmpegVersionName = UMediaPlayer.getFFmpegVersionName();
			ffmpegVersionCode = UMediaPlayer.getFFmpegVersionCode();
			so = new SoInfo();
			so.versionName = ffmpegVersionName;
			so.versionCode = ffmpegVersionCode;
			so.name = LIB_FFMPEG_SO_NAME;
			so.os = os;
			so.arch = arch;
			so.product = product;
			mLocalSoInfos.add(so);

			uplayerVersionName = UMediaPlayer.getUplayerVersionName();
			uplayerVersionCode = UMediaPlayer.getUplayerVersionCode();
			so = new SoInfo();
			so.versionName = uplayerVersionName;
			so.versionCode = uplayerVersionCode;
			so.name = LIB_UPLAYER_22_SO_NAME;
			so.os = os;
			so.arch = arch;
			so.product = product;
			mLocalSoInfos.add(so);

			so = new SoInfo();
			so.versionName = uplayerVersionName;
			so.versionCode = uplayerVersionCode;
			so.name = LIB_UPLAYER_23_SO_NAME;
			so.os = os;
			so.arch = arch;
			so.product = product;
			mLocalSoInfos.add(so);
		}
		if (AcceleraterServiceManager.isACCEnable()) {
			so = new SoInfo();
			so.versionName = AcceleraterServiceManager.getAccVersionName();
			so.versionCode = AcceleraterServiceManager.getAccVersionCode();
			so.name = LIB_ACCSTUB_SO_NAME;
			so.os = os;
			so.arch = arch;
			so.product = product;
			mLocalSoInfos.add(so);
		}

		Logger.d(TAG, "============= local libs ===============");
		for (SoInfo soInfo : mLocalSoInfos) {
			Logger.d(TAG, "name = " + soInfo.name
					+ ", versionName = " + soInfo.versionName
					+ ", versionCode = " + soInfo.versionCode
					+ ", os = " + soInfo.os
					+ ", arch = " + soInfo.arch
					+ ", product = " + soInfo.product);
		}
		Logger.d(TAG, "============= local libs ===============");
	}

	private void parseJson(String json) {
		Logger.d(TAG,"parseJson");
		Logger.d(TAG,"json = " + json);

		try {
			JSONObject object = new JSONObject(json);

			if (object.has("result")) {
				Logger.d(TAG,"has result");
				JSONArray segsArray = object.optJSONArray("result");
				if (segsArray != null)
					for (int i = 0; i < segsArray.length(); i++) {
						JSONObject segObject = segsArray.optJSONObject(i);
						if (segObject != null) {
							SoInfo soInfo = new SoInfo();
							soInfo.name = segObject.optString("name");
							soInfo.versionName = segObject.optString("versionName");
							soInfo.versionCode = segObject.optInt("versionCode");
							soInfo.arch = segObject.optString("arch");
							soInfo.os = segObject.optString("os");
							soInfo.product = segObject.optString("product");
							soInfo.download_url = segObject.optString("download_url");
							soInfo.md5 = segObject.optString("md5");
							soInfo.relativeSo = segObject.optString("relativeSo");

							if (segObject.optString("is_forced_upgrade").equals("1")) {
								soInfo.is_forced_upgrade = true;
							} else {
								soInfo.is_forced_upgrade = false;
							}

							Logger.d(TAG,"=================================");
							Logger.d(TAG, "name = " + soInfo.name);
							Logger.d(TAG, "versionName = " + soInfo.versionName);
							Logger.d(TAG, "versionCode = " + soInfo.versionCode);
							Logger.d(TAG, "arch = " + soInfo.arch);
							Logger.d(TAG, "os = " + soInfo.os);
							Logger.d(TAG, "product = " + soInfo.product);
							Logger.d(TAG, "download_url = " + soInfo.download_url);
							Logger.d(TAG, "relativeSo = " + soInfo.relativeSo);
							Logger.d(TAG, "is_forced_upgrade = " + soInfo.is_forced_upgrade);

							if (mOnlineSoInfos.containsKey(soInfo.name)) {
								ArrayList<SoInfo> value = mOnlineSoInfos.get(soInfo.name);
								value.add(soInfo);
							} else {
								ArrayList<SoInfo> value = new ArrayList<SoInfo>();
								value.add(soInfo);
								mOnlineSoInfos.put(soInfo.name, value);
							}

						}
					}
			}
		} catch (JSONException e) {
			Logger.e(TAG, e);
		}
	}

	private Boolean compareArch(String local, String online) {
		if (local == null || online == null) {
			return false;
		}

		String[] archs = online.split(";");
		for (int i = 0; i < archs.length; i++) {
			if (archs[i].equals(local)) {
				return true;
			}
		}


		return false;
	}

	private Boolean compareSo(SoInfo local, SoInfo online) {
		if (local == null || online == null) {
			return false;
		}

		if (!local.name.equals(online.name)
				|| !local.versionName.equals(online.versionName)
				//|| !compareArch(local.arch, online.arch)
				//|| !local.product.equals(online.product)
				//|| !local.os.equals(online.os)
				|| local.versionCode != online.versionCode
				) {
			return false;
		}

		return true;
	}

	private Boolean isNeedUpgrade() {
		Map<String, SoInfo> upgradeSoInfos = new HashMap<String, SoInfo>();
		ArrayList<SoInfo> onlineSo;
		for (SoInfo local : mLocalSoInfos) {
			onlineSo = mOnlineSoInfos.get(local.name);
			if (onlineSo != null) {
				for (SoInfo online : onlineSo) {
					if (compareSo(local, online)) {
						upgradeSoInfos.put(local.name, online);
					}
				}
			}
		}

		Logger.d(TAG, "====upgrade so initially====");
		for (Entry<String, SoInfo> entry: upgradeSoInfos.entrySet()) {
			SoInfo so = entry.getValue();
			Logger.d(TAG, so.name);
		}
		Logger.d(TAG, "====upgrade so initially====");
		Logger.d(TAG, "====upgrade so finally====");
		for (Entry<String, SoInfo> entry: upgradeSoInfos.entrySet()) {
			SoInfo so = entry.getValue();
			String relativeSo = so.relativeSo;

			if (relativeSo == null || (relativeSo != null && relativeSo.equals(""))) {
				Logger.d(TAG, so.name);
				mUpgradeSoInfos.put(so.name, so);
			} else {
				int i;
				String []relativeArray = relativeSo.split(",");
				for (i = 0; i < relativeArray.length; i++) {
					if (!upgradeSoInfos.containsKey(relativeArray[i])) {
						break;
					}
				}

				if (i == relativeArray.length) {
					mUpgradeSoInfos.put(so.name, so);
					Logger.d(TAG, so.name);
				}
			}
		}
		Logger.d(TAG, "====upgrade so finally====");

		return mUpgradeSoInfos.size() > 0 ? true : false ;
	}


	public void downloadLibs() {
		mDownloadThread = new Thread(mdownloadRunnable);
		mDownloadThread.start();
	}

	/**
	 * 杀掉:accelerater进程
	 */
	public void killAcceleraterProcess() {
		Logger.d(TAG, "killAcceleraterProcess");
		ActivityManager myActivityManager = (ActivityManager) SoUpgradeService.this
				.getSystemService(ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> mRunningPros = myActivityManager
				.getRunningAppProcesses();
		for (RunningAppProcessInfo amPro : mRunningPros) {

			if (amPro.processName.contains(":accelerater")) {
				Log.d(TAG, amPro.processName + " is running!");
				android.os.Process.killProcess(amPro.pid);
				break;
			}
		}

	}

	class SingleSoDownloadTask {
		private SoInfo mInfo;

		public SingleSoDownloadTask(SoInfo info) {
			mInfo = info;
		}

		private volatile DownloadSate mDownloadState = DownloadSate.STOP;


		public void startDownload() {
			mDownloadState = DownloadSate.DOWNLOADING;
			new Thread(new Runnable() {
				@Override
				public void run() {
					int flag = 0;
					HttpDownloader httpDownloader = new HttpDownloader(SoUpgradeService.this);
					Logger.d(TAG, "========== SingleSoDownloadTask start ============");

					if (!Util.hasInternet()) {
						Logger.d(TAG, "==========network error SingleSoDownloadTask end ============");
						mDownloadState = DownloadSate.STOP;
						try {
							if (mSoUpgradeCallback != null)
								mSoUpgradeCallback.onDownloadFailed(mInfo.name);
						} catch (RemoteException e) {
							Logger.e(TAG, e);
						}
						return;
					}

					Logger.d(TAG, "SingleSoDownloadTask name = " + mInfo.name + ", download_url = " + mInfo.download_url);

					for (int i = 0; mInfo.needDownload && i < SINGLE_TASK_TRY_TIMES; i++) {
						flag = httpDownloader.downloadFile(mInfo.download_url, mIndependentDownloadPath, mInfo.name + TEMP_SO_SUFFIX);
						if (flag == 0) {
							if (!checkMD5(mIndependentDownloadPath + mInfo.name + TEMP_SO_SUFFIX, mInfo.md5)) {
								flag = -1;
								FileUtils.deleteFile(mIndependentDownloadPath + mInfo.name + TEMP_SO_SUFFIX);
								continue;
							} else {
								break;
							}
						} else {
							if (!Util.hasInternet()) {
								break;
							}
						}
					}

					//下载失败
					if (flag < 0) {
						Logger.d(TAG, "SingleSoDownloadTask Download failed");
//						processDownloadFail(mInfo);
						mDownloadState = DownloadSate.STOP;
						try {
							if (mSoUpgradeCallback != null)
								mSoUpgradeCallback.onDownloadFailed(mInfo.name);
						} catch (RemoteException e) {
							Logger.e(TAG, e);
						}
					} else {
						Logger.d(TAG, "replaceSo()");
						//删除旧库
						FileUtils.deleteFile(mIndependentDownloadPath + mInfo.name);
						//将下载成功的临时文件改名
						FileUtils.renameFile(mIndependentDownloadPath + mInfo.name + TEMP_SO_SUFFIX, mIndependentDownloadPath + mInfo.name);
						Logger.d(TAG, "rename " + mIndependentDownloadPath + mInfo.name + TEMP_SO_SUFFIX + " ---> " + mIndependentDownloadPath + mInfo.name);
						mDownloadState = DownloadSate.DOWNLOADED;
						saveSingleDownloadSoVersion(mInfo.name, mInfo.versionCode);
						try {
							if (mSoUpgradeCallback != null)
								mSoUpgradeCallback.onDownloadEnd(mInfo.name);
						} catch (RemoteException e) {
							Logger.e(TAG, e);
						}
					}
					Logger.d(TAG, "==========SingleSoDownloadTask mdownloadRunnable end ============");
				}
			}).start();
		}
	}

	public enum DownloadSate {
		STOP, DOWNLOADING, DOWNLOADED
	}

	private void stopService() {
		if (!mSingleSoDownloadTasks.isEmpty())
			return;
		stopSelf();
	}

}
