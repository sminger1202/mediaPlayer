package com.youku.player.service;

import android.text.TextUtils;

import com.baseproject.image.Utils;
import com.baseproject.utils.Logger;
import com.baseproject.utils.Profile;
import com.baseproject.utils.Util;
import com.youku.player.LogTag;
import com.youku.player.util.DisposableStatsUtils;

import java.net.HttpURLConnection;
import java.net.URL;


/**
 * 此类用于一些不关心结果的一次性的Http任务
 * 
 * @author 张宇
 * @create-time Mar 25, 2013 5:28:06 PM
 * @version $Id
 * 
 */
public class DisposableHttpTask extends Thread {

	private String url;
	private String tag;
	private String requestSumary ;
	private String requestMethod;
	
	public static final String METHOD_GET = "GET";
	public static final String METHOD_POST = "POST";

	public DisposableHttpTask(String url) {
		super("DisposableHttpTask");
		this.url = url;
	}
	
	/** 使用tag打印更多的内容 */
	public DisposableHttpTask(String tag, String url, String requestSumary) {
		this(url);
		this.tag = tag;
		this.requestSumary = requestSumary;
	}
	
	public void setRequestMethod(String requestMethod) {
		this.requestMethod = requestMethod;
	}

	/*
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		super.run();

		boolean isSuccess = false;
		String resultDetail = "unknown";
		Logger.d(LogTag.TAG_STATISTIC, "DisposableHttpTask:" + url);

		//TODO
		if (Util.hasInternet()) {
			Utils.disableConnectionReuseIfNecessary();
			try {
				URL uri = new URL(url);

				HttpURLConnection conn = (HttpURLConnection) uri
						.openConnection();
				if (!TextUtils.isEmpty(requestMethod))
					conn.setRequestMethod(requestMethod);
				conn.setRequestProperty("User-Agent", Profile.User_Agent);
				conn.connect();
				final int response = conn.getResponseCode();

				isSuccess = (response == 200) ? true : false;
				resultDetail = "" + response;

				Logger.d(LogTag.TAG_PLAYER, "url:"+url+":"+String.valueOf(response));
			} catch (Exception e) {
				resultDetail = "got Exception e : " + e.getMessage();
				Logger.e(LogTag.TAG_PLAYER, e);
			} finally {
			}

		}

		if (requestSumary != null) {
			String result = requestSumary
					+ (isSuccess ? " 成功" : " 失败") + " !  resultCode = "
					+ resultDetail + " 其请求url = " + url;
			if (isSuccess) {
				DisposableStatsUtils.logDebug(result);
				return;
			}
			DisposableStatsUtils.logError(result);
		}
	}

}
