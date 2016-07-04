package com.youku.player.service;

import com.baseproject.image.Utils;
import com.baseproject.utils.Logger;
import com.baseproject.utils.Profile;
import com.baseproject.utils.Util;
import com.youku.player.LogTag;
import com.youku.player.util.PlayerUtil;

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
public class DisposableHttpCookieTask extends Thread {

	private String url;
	private String tag;
	private String requestSumary ;

	public DisposableHttpCookieTask(String url) {
		super("DisposableHttpTask");
		this.url = url;
	}

	/** 使用tag打印更多的内容 */
	public DisposableHttpCookieTask(String tag, String url, String requestSumary) {
		this(url);
		this.tag = tag;
		this.requestSumary = requestSumary;
	}

	/*
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		super.run();

		boolean isSuccess = false;
		String resultDetail = "unknown";

		if (Util.hasInternet()) {
			Utils.disableConnectionReuseIfNecessary();
			try {
				HttpURLConnection httpConn = null;
				URL u = new URL(url);
				httpConn = (HttpURLConnection) u.openConnection();
				httpConn.setAllowUserInteraction(false);
				httpConn.setInstanceFollowRedirects(true);
				httpConn.setRequestMethod("GET");
				httpConn.setRequestProperty("User-Agent", Profile.User_Agent);
				if(PlayerUtil.isLogin())
					httpConn.setRequestProperty("Cookie", PlayerUtil.getCookie());
				httpConn.connect();
				final int response = httpConn.getResponseCode();

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
			String result = "发送广告统计 " + requestSumary
					+ (isSuccess ? " 成功" : " 失败") + " !  resultCode = "
					+ resultDetail + " 其请求url = " + url;
			if (isSuccess) {
				Logger.d(tag, result);
				return;
			}
			Logger.d(tag, result);
		}
	}

}
