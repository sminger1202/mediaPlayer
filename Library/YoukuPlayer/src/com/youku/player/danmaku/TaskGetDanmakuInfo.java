package com.youku.player.danmaku;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;

import com.baseproject.network.YoukuAsyncTask;
import com.baseproject.utils.Logger;
import com.baseproject.utils.Util;
import com.youku.player.config.MediaPlayerConfiguration;
import com.youku.player.goplay.Profile;
import com.youku.player.util.PlayerUtil;

public class TaskGetDanmakuInfo extends
		YoukuAsyncTask<Handler, Object, Handler> {
	private static final String TAG = "TaskGetVideoUrl";
	/** 返回结果状态码 */
	public final int TIMEOUT = 15000;
	private int success;
	private int fail;
	private String requrl;
	private int message;
	private String responseString = null;

	public void setRequestUrl(String requrl) {
		this.requrl = requrl;
	}

	public void setSuccess(int success) {
		this.success = success;
	}

	public void setFail(int fail) {
		this.fail = fail;
	}

	public TaskGetDanmakuInfo(String requrl) {
		this.requrl = requrl;
		success = -1;
		fail = -1;
	}

	public String getRequestUrl() {
		return requrl;
	}

	@Override
	protected Handler doInBackground(Handler... params) {
		connectAPI();
		return params[0];
	}

	@Override
	protected void onPostExecute(Handler result) {
		Message message = Message.obtain();
		try {
			message.what = this.message;
			message.obj = responseString;
		} catch (Exception e) {

		} finally {
			if (result != null)
				result.sendMessage(message);
		}
		super.onPostExecute(result);
	}

	public void connectAPI() {
		responseString = null;
		URLConnection conn;
		URL url = null;
		try {
			InputStream is = null;
			int response = -1;
			url = new URL(requrl);
			Logger.d(TAG, "connectAPI url " + url.toString());
			conn = url.openConnection();
			conn.setConnectTimeout(TIMEOUT);
			conn.setReadTimeout(TIMEOUT);
			HttpURLConnection httpConn = (HttpURLConnection) conn;
			httpConn.setAllowUserInteraction(false);
			httpConn.setInstanceFollowRedirects(true);
			httpConn.setRequestMethod("GET");
			if (PlayerUtil.isLogin()) {
				httpConn.setRequestProperty("Cookie", PlayerUtil.getCookie());
			}
			httpConn.setRequestProperty("User-Agent", Profile.USER_AGENT);

			httpConn.connect();
			response = httpConn.getResponseCode();

			if (response == HttpURLConnection.HTTP_OK) {
				// 获取联网数据
				is = httpConn.getInputStream();
				String jsonString = Util.convertStreamToString(is);
				responseString = MediaPlayerConfiguration.getInstance().mPlantformController
						.processRawData(jsonString);
				int code = getDanmakuError(responseString);
				if (code == -1) {
					message = fail;
				} else {
					message = success;
				}
			} else {

			}
		} catch (MalformedURLException e) {
			message = fail;
		} catch (IOException e) {
			message = fail;
		} catch (Exception e) {
			message = fail;
		}
	}

	private int getDanmakuError(String responseString) {
		int data = -1;
		try {
			JSONObject json = new JSONObject(responseString);
			if (json.has("error")) {
				data = json.optInt("error", -1);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return data;
	}
}
