package com.baseproject.utils;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import java.util.UUID;

public class DeviceInfo {
    /**
     * 设备 制造商
     */
    public static String MANUFACTURER = "";
    /**
     * 设备品牌
     */
    public static String BRAND = "";
    /**
     * 设备型号
     */
    public static String MODEL = "";
    /**
     * 操作系统版本
     */
    public static String OS = "";
    /**
     * 屏幕宽（像素）
     */
    public static int WIDTH = 0;
    /**
     * 屏幕高 （像素）
     */
    public static int HEIGHT = 0;
    /**
     * 屏幕密度
     */
    public static float DENSITY;
    /**
     * 屏幕密度DPI
     */
    public static float DENSITYDPI;
    /**
     * 设备串号
     */
    public static String IMEI = "";
    /**
     * SIM卡号
     */
    public static String IMSI = "";
    /**
     * 运营商
     */
    public static String OPERATOR = "";
    /**
     * mac地址
     */
    public static String MAC = "";
    /**
     * UUID
     */
    public static String UUID = "";
    /**
     * GUID
     */
    public static String GUID = "";
    /**
     * 手机号码
     */
    public static String MOBILE = "";
    /**
     * CPU信息
     */
    public static String CPU = "";
    /**
     * 当前网络信息
     */
    public static String NETWORKINFO = "";
    /**
     * 当前网络类型 for example "WIFI" or "MOBILE".
     */
    public static String NETWORKTYPE = "";
    /**
     * 总内存
     */
    public static String MEM_TOTAL = "";
    /**
     * 空闲内存
     */
    public static String MEM_FREE = "";
    /**
     * ROM总量 (KB)
     */
    public static String ROM_TOTAL = "";
    /**
     * ROM空闲(KB)
     */
    public static String ROM_FREE = "";
    /**
     * SD总量(KB)
     */
    public static String SDCARD_TOTAL = "";
    /**
     * SD空闲(KB)
     */
    public static String SDCARD_FREE = "";
    /**
     * DEVICEID
     */
    public static String DEVICEID;

    // public static String OS_VER;
    /**
     * 激活时间
     */
    public static String ACTIVE_TIME = "";

    public static String HOST_IP = "";

    public static Context CONTEXT;

    private DeviceInfo() {// 单例
    }

    public static void init(Context context) {
        CONTEXT = context;
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        DeviceInfo.DEVICEID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
//        DeviceInfo.ACTIVE_TIME = Youku.getPreference("active_time");
        DeviceInfo.MOBILE = Util.URLEncoder(tm.getLine1Number());
        DeviceInfo.IMEI = Util.URLEncoder(tm.getDeviceId());
//		DeviceInfo.IMSI = Util.URLEncoder(tm.getSimSerialNumber());
        DeviceInfo.IMSI = "";
        if (!TextUtils.isEmpty(tm.getSimOperatorName()) || !TextUtils.isEmpty(tm.getSimOperator())) {
            DeviceInfo.OPERATOR = Util.URLEncoder(tm.getSimOperatorName() + "_" + tm.getSimOperator());
        } else {
            DeviceInfo.OPERATOR = "";
        }

//        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//        if (wifi.getConnectionInfo().getMacAddress() != null) {
//            DeviceInfo.MAC = wifi.getConnectionInfo().getMacAddress();
//        }

        if (!DeviceInfo.IMEI.trim().equals("") || !DeviceInfo.MAC.trim().equals("")) {
            // 如果能取到这两个中间任意一值，则不需要传UUID
        } else {
            DeviceInfo.UUID = getUUID();
        }

        DisplayMetrics dm;
        dm = context.getResources().getDisplayMetrics();
        DeviceInfo.WIDTH = dm.widthPixels;
        DeviceInfo.HEIGHT = dm.heightPixels;
        DeviceInfo.DENSITY = dm.density;
        DeviceInfo.DENSITYDPI = dm.densityDpi;

    }

    private static String getUUID() {
        try {
            final String tmDevice, tmSerial, androidId;
            TelephonyManager tm = (TelephonyManager) CONTEXT.getSystemService(Context.TELEPHONY_SERVICE);
            tmDevice = tm.getDeviceId();
            tmSerial = tm.getSimSerialNumber();
            androidId = Settings.Secure.getString(CONTEXT.getContentResolver(), Settings.Secure.ANDROID_ID);
            java.util.UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
            return deviceUuid.toString();
        } catch (Exception e) {
            return java.util.UUID.randomUUID().toString();
        }
    }
}
