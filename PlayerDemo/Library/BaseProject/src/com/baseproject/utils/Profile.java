/*
 * @(#)Constant.java	 2013-3-28
 *
 * Copyright 2005-2013 YOUKU.com
 * All rights reserved.
 * 
 * YOUKU.com PROPRIETARY/CONFIDENTIAL.
 */

package com.baseproject.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Profile {
    /**
     * @param tag       TAG
     * @param useragent
     * @param context
     */
    public static void initProfile(String tag, String useragent, Context context) {
        TAG = tag;
        User_Agent = useragent;
        mContext = context;
    }

    public static void initProfile(int from, String tag, String useragent,
                                   Context context) {
        initProfile(tag, useragent, context);
        FROM = from;
    }

    /**
     * 设置登陆状态
     *
     * @param bLogined
     * @param cookie
     */
    public static void setLoginState(Boolean bLogined, String cookie) {
        isLogined = bLogined;
        COOKIE = cookie;
    }

    public static Context mContext;
    public static final int TIMEOUT = 30000;// 网络访问超时
    public static String TAG;// log默认标签
    public static String User_Agent;// UA
    public static String COOKIE = null;// 服务器返回的 Cookie 串
    public static boolean isLogined;// 登录状态
    public static int FROM;// 0 youku; 1 tudou 2,tudou hds
    // 用来控制播放器接口是否是测试接口
    public static boolean DEBUG = true;
    // 控制日志开关
    public static boolean LOG = true;

    public static int FROM_YOUKU = 0;
    public static int FROM_TUDOU = 1;
    public static int FROM_TUDOU_HD = 2;

    private static final String Wireless_pid = "6ff9033404d2710b";

    private static String channel = null;

    public static String getPid() {
        if (TextUtils.isEmpty(channel)) {
            try {
                AssetManager assetManager = mContext.getAssets();
                channel = convertStreamToString(assetManager.open("channel_name"));
            } catch (Exception e) {
                channel = Wireless_pid;
            }
        }
        if (TextUtils.isEmpty(channel)) {
            channel = Wireless_pid;
        }
        return channel;
    }

    public static String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static String VER;
    public static int VER_CODE;

    public static long TIMESTAMP = 0;
    public static String SECRET_TYPE = "md5";
    public static String NEWSECRET = "6b72db72a6639e1d5a2488ed485192f6";
}
