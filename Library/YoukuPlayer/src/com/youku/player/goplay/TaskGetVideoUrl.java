package com.youku.player.goplay;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.baseproject.network.YoukuAsyncTask;
import com.baseproject.utils.Logger;
import com.baseproject.utils.Util;
import com.youku.player.LogTag;
import com.youku.player.Track;
import com.youku.player.config.MediaPlayerConfiguration;
import com.youku.player.plugin.MediaPlayerDelegate;
import com.youku.player.util.PlayCode;
import com.youku.player.util.PlayerUtil;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class TaskGetVideoUrl extends YoukuAsyncTask<Handler, Object, Handler> {
	private static final String TAG = "TaskGetVideoUrl";
	/** 返回结果状态码 */
	public final int TIMEOUT = 15000;
	private int success;
	private int fail;
	private String requrl;
	private int message;
	private String exceptionString = null, responseString = null;

	public void setRequestUrl(String requrl) {
		this.requrl = requrl;
	}

	public void setSuccess(int success) {
		this.success = success;
	}

	public void setFail(int fail) {
		this.fail = fail;
	}

	public TaskGetVideoUrl(String requrl) {
		this.requrl = requrl;
		success = -1;
		fail = -1;
	}
	
	public String getRequestUrl(){
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
			message.obj = new VideoInfoReasult(responseString, exceptionString);
			Logger.d(LogTag.TAG_PLAYER, "请求视频数据返回:" + responseString);
		} catch (Exception e) {
			exceptionString += e.toString();
		} finally {
			if (result != null)
				result.sendMessage(message);
		}
		super.onPostExecute(result);
	}

	public void connectAPI() {
		exceptionString = null;
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
			Logger.d(TAG, "before httpConn.connect()");
			httpConn.connect();
			Logger.d(TAG, "after httpConn.connect()");
			response = httpConn.getResponseCode();
			
			if (response == HttpURLConnection.HTTP_OK) {
				// 获取联网数据
				is = httpConn.getInputStream();
				String jsonString = Util.convertStreamToString(is);
				responseString = MediaPlayerConfiguration.getInstance().mPlantformController
						.processRawData(jsonString);
				JSONObject object = new JSONObject(responseString);
				int code = PlayerUtil.getJsonInit(object, "error_code", 0);
				if (code == 0)
					code = PlayerUtil.getJsonInit(object, "code", 0);
				// 联网成功，但有异常情况：比如视频加密，加密视频密码错误，无版权等情况
				if (code == -100 || code == -101 || code == -102 || code == -104 || code == -105
						|| code == -106 || code == -107 || code == -108 || code == -112
						|| code == -125 || code == -126 || code == -127 || code == -128
						|| code == -202 || code == -204 || code == -301 || code == -308 || code == 400) {
					MediaPlayerDelegate.playCode = Integer.toString(code);					
					message = fail;
				} else {
					message = success;
					MediaPlayerDelegate.playCode = PlayCode.PLAY_SUCC;
				}
				if (code == 400)
					Track.setVideoReqError(PlayCode.SERVER_ERROR);
			} else {
				Track.setVideoReqError(PlayCode.SERVER_ERROR);
				MediaPlayerConfiguration.getInstance().mPlantformController
						.processHttpError(this, response, httpConn);
			}
		} catch (MalformedURLException e) {
			MediaPlayerDelegate.playCode = PlayCode.USER_RETURN;
			exceptionString += e.toString();
			message = fail;
			Track.setVideoReqError(PlayCode.CONNECT_ERROR);
			Logger.e(LogTag.TAG_PLAYER, Log.getStackTraceString(e));
		} catch (IOException e) {
			MediaPlayerDelegate.playCode = PlayCode.USER_RETURN;
			exceptionString += e.toString();
			message = fail;
			Track.setVideoReqError(PlayCode.CONNECT_ERROR);
			Logger.e(LogTag.TAG_PLAYER, Log.getStackTraceString(e));
		} catch (Exception e) {
			MediaPlayerDelegate.playCode = PlayCode.USER_RETURN;
			exceptionString += e.toString();
			message = fail;
			Track.setVideoReqError(PlayCode.CONNECT_ERROR);
			Logger.e(LogTag.TAG_PLAYER, Log.getStackTraceString(e));
		}
	}
}
