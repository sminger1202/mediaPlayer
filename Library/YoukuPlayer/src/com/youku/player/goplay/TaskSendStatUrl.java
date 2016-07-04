package com.youku.player.goplay;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import android.os.Handler;

import com.baseproject.network.YoukuAsyncTask;
import com.baseproject.utils.Logger;

/**
 * 发送广告统计信息
 * @author yuanfang
 *
 */
public class TaskSendStatUrl extends YoukuAsyncTask<Handler, Object, Handler> {
	private static final String TAG = "TaskGetVideoUrl";
	/** 返回结果状态码 */
	public final int TIMEOUT = 30000;
	private String requrl;

	public void setRequestURL(String requrl) {
		this.requrl = requrl;
	}

	public TaskSendStatUrl(String requrl) {
		this.requrl = requrl;
	}

	@Override
	protected Handler doInBackground(Handler... params) {
		connectAPI();
		return params[0];
	}

	@Override
	protected void onPostExecute(Handler result) {
	}

	private void connectAPI() {
		URLConnection conn;
		URL url = null;
		try {
			url = new URL(requrl);
			Logger.d(TAG, "connectAPI url " + url.toString());
			conn = url.openConnection();
			conn.setConnectTimeout(TIMEOUT);
			conn.setReadTimeout(TIMEOUT);
			HttpURLConnection httpConn = (HttpURLConnection) conn;
			httpConn.setAllowUserInteraction(false);
			httpConn.setInstanceFollowRedirects(true);
			httpConn.setRequestMethod("GET");
			httpConn.connect();
		} catch (Exception e) {
		}
	}
}
