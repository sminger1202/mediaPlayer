package com.android.playerdemo;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.multidex.MultiDexApplication;

import com.baseproject.image.ImageLoaderManager;
import com.baseproject.utils.DeviceInfo;
import com.baseproject.utils.Logger;
import com.baseproject.utils.Profile;
import com.baseproject.utils.Util;
import com.youku.analytics.AnalyticsAgent;
import com.youku.player.PlayerNetCache;

/**
 * Created by sminger on 2016/6/20.
 */
public class PlayerDemoApp  extends MultiDexApplication {

    public static final String TAG = "TudouApp";
    public static String versionName;
    public static String appName;
    public static String User_Agent;
    public static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        Profile.mContext = this;
        initUserAgent();
        Profile.initProfile("TudouLite", User_Agent, this);
        DeviceInfo.init(this);
        initVersionInfo();
        initPlayer();
        initImageLoader();
        initAnalyticsAgent();
    }

    private void initVersionInfo() {
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(),
                    PackageManager.GET_META_DATA).versionName;
            Profile.VER_CODE = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA).versionCode;
            appName = getPackageManager().getPackageInfo(getPackageName(),
                    PackageManager.GET_META_DATA).applicationInfo.name;
            Profile.VER = versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "1.0.0";
            Profile.VER = versionName;
            Profile.VER_CODE = 1;
        }
    }

    private void initUserAgent() {
        User_Agent = "TudouLite;"
                + Util.getVersionName(this)
                + ";Android;"
                + Build.VERSION.RELEASE
                + ";" + Build.MODEL;
    }

    private void initPlayer() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                String path = "";
                long size = 0;
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) && getExternalCacheDir() != null) {
                    path = getExternalCacheDir().getAbsolutePath() + "/youku_video_cache";
                    Logger.d(TAG, "getExternalCacheDir().getAbsolutePath():" + getExternalCacheDir().getAbsolutePath());
                    size = (long) (Util.getSDCardInfo()[1] * 0.1 / 1024 / 1024);
                    Logger.d(TAG, "size:" + size);
                } else
                    Logger.d(TAG, "not mounted");
                Logger.d(TAG, "Youku.User_Agent:" + User_Agent);
                PlayerNetCache.getInstance().setUserAgent(User_Agent);
                PlayerNetCache.getInstance().dnsPreParse();
                PlayerNetCache.getInstance().start(path, size);

            }
        };
        thread.start();
    }


    private void initImageLoader() {
        ImageLoaderManager.initImageLoaderConfigurationTudou(context);
    }

    private void initAnalyticsAgent() {
        AnalyticsAgent.setDebugMode(Profile.DEBUG);
        AnalyticsAgent.setTestHost(Profile.DEBUG);
        AnalyticsAgent.setTest(Profile.DEBUG);// 置为true不进行加密并将日志写到本地,测试完成后置为false
        AnalyticsAgent.init(context, User_Agent, Profile.getPid(),
                "TudouLite");
        AnalyticsAgent.setContinueSessionMillis(300000);
    }

}
