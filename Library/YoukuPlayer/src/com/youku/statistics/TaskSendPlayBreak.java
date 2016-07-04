package com.youku.statistics;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import android.os.AsyncTask;

import com.baseproject.image.Utils;
import com.baseproject.utils.Logger;
import com.baseproject.utils.Util;

public class TaskSendPlayBreak extends AsyncTask<Void, Void, Void> {
	private static final String TAG = "TaskSendPlayBreak";
	public final int TIMEOUT = 30000;
	private String mRequrl;
	private String mCdn;
	private final String URL_DOMAIN = "http://erreport.tudou.com/ce/";

	public TaskSendPlayBreak(String videoUrl) {
		mRequrl = getUrl(videoUrl);
	}

	@Override
	protected Void doInBackground(Void... params) {
		connectAPI();
		return null;
	}

	private void connectAPI() {
		URLConnection conn;
		URL url = null;
		if (Util.hasInternet()) {
			Utils.disableConnectionReuseIfNecessary();
			try {
				url = new URL(mRequrl);
				Logger.d(TAG, "connectAPI url " + url.toString());
				conn = url.openConnection();
				conn.setConnectTimeout(TIMEOUT);
				conn.setReadTimeout(TIMEOUT);
				HttpURLConnection httpConn = (HttpURLConnection) conn;
				httpConn.setAllowUserInteraction(false);
				httpConn.setInstanceFollowRedirects(true);
				httpConn.setRequestMethod("GET");
				httpConn.connect();

				final int response = httpConn.getResponseCode();
				boolean isSuccess = (response == 200) ? true : false;
				if (isSuccess) {
					Logger.d(TAG, "http connect success");
				} else {
					Logger.d(TAG, "http connect fail");
				}
			} catch (Exception e) {
				Logger.d(TAG, "http connect failed, url " + url.toString());
			}
		}
	}

	private String getUrl(String videoUrl) {
//		if (Profile.PLANTFORM == Plantform.TUDOU) {
			mCdn = "tudou";
//		} else {
//			mCdn = "youku";
//		}
		return URL_DOMAIN + "err?id=3006204" + "&cdn=" + mCdn + "&url="
				+ videoUrl;
	}
}
