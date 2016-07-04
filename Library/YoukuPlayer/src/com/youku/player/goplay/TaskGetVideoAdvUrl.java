package com.youku.player.goplay;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.baseproject.network.YoukuAsyncTask;
import com.baseproject.utils.Logger;
import com.baseproject.utils.Util;
import com.youku.player.LogTag;
import com.youku.player.Track;
import com.youku.player.util.PlayCode;
import com.youku.player.util.PlayerPreference;
import com.youku.player.util.PlayerUtil;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.List;

/**
 * 获取视频广告
 * 
 * @author yuanfang
 * 
 */
public class TaskGetVideoAdvUrl extends
		YoukuAsyncTask<Handler, Object, Handler> {
	/** 返回结果状态码 */
	public final int TIMEOUT = 5000;
	private int success;
	private int fail;
	private String requrl;
	private int message;
	private String exceptionString = null, responseString = null;
	private String userAgent;

	private static final String TAG = LogTag.TAG_PREFIX + TaskGetVideoAdvUrl.class.getSimpleName();

	public void setRequestURL(String requrl) {
		this.requrl = requrl;
	}

	public void setSuccess(int success) {
		this.success = success;
	}

	public void setFail(int fail) {
		this.fail = fail;
	}

	public TaskGetVideoAdvUrl(String requrl, String userAgent) {
		this.requrl = requrl;
		success = -1;
		fail = -1;
		this.userAgent = userAgent;
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
			message.obj = new VideoAdvInfoResult(responseString,
					exceptionString);
		} catch (Exception e) {
			exceptionString += e.toString();
		} finally {
			if (result != null)
				result.sendMessage(message);
		}
		super.onPostExecute(result);
	}

	private void connectAPI() {
		exceptionString = null;
		responseString = null;
        if(TextUtils.isEmpty(requrl)){
            return ;
        }
		try {
			InputStream is = null;
			HttpParams httpParams = new BasicHttpParams();
			// 建立链接的时间
			HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT);
			// 等数据的时间
			HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT);
			DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
			HttpGet httpGet = new HttpGet(requrl);
			// setHeader addHeader后无法获取暂停广告数据
			httpGet.setHeader("User-Agent", Profile.USER_AGENT);
			if (!TextUtils.isEmpty(PlayerPreference
					.getPreference("ad_cookie"))) {
				if (PlayerUtil.isLogin()) {
					httpGet.addHeader(
							"Cookie",
							PlayerUtil.getCookie()
									+ PlayerPreference
											.getPreference("ad_cookie"));
				} else {
					httpGet.addHeader("Cookie",
							PlayerPreference.getPreference("ad_cookie"));
				}
			} else if (PlayerUtil.isLogin()) {
				httpGet.addHeader("Cookie", PlayerUtil.getCookie());
			}
			HttpResponse httpResponse = httpClient.execute(httpGet);
			is = httpResponse.getEntity().getContent();
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				// 获取联网数据
				String jsonString = Util.convertStreamToString(is);
				Header[] headers = httpResponse.getHeaders("Set-Cookie");
				StringBuffer advBuffer = new StringBuffer();
				for (Header header : headers) {
					String cookie = header.getValue();
					advBuffer.append(cookie);
					advBuffer.append(";");
				}
				Logger.d(TAG + advBuffer.toString());
				if (!TextUtils.isEmpty(advBuffer.toString())) {
					PlayerPreference.savePreference("ad_cookie",
							advBuffer.toString());
				}
				responseString = jsonString;
				message = success;
			} else {
				message = fail;
				Track.setAdReqError(PlayCode.SERVER_ERROR);
			}
		} catch (MalformedURLException e) {
			exceptionString += e.toString();
			message = fail;
			Track.setAdReqError(PlayCode.CONNECT_ERROR);
			Logger.e(LogTag.TAG_PLAYER, LogTag.MSG_EXCEPTION, e);
		} catch (IOException e) {
			exceptionString += e.toString();
			message = fail;
			Track.setAdReqError(PlayCode.CONNECT_ERROR);
			Logger.e(LogTag.TAG_PLAYER, LogTag.MSG_EXCEPTION, e);
		} catch (Exception e) {
			exceptionString += e.toString();
			message = fail;
			Track.setAdReqError(PlayCode.CONNECT_ERROR);
			Logger.e(LogTag.TAG_PLAYER, LogTag.MSG_EXCEPTION, e);
		}
	}

	/**
	 * 获取标准 Cookie 并存储
	 * 
	 * @param httpClient
	 */
	private String getCookie(DefaultHttpClient httpClient) {
		final List<Cookie> cookies = httpClient.getCookieStore().getCookies();
		final StringBuilder s = new StringBuilder();
		for (int i = 0, n = cookies.size(); i < n; i++) {
			final Cookie cookie = cookies.get(i);
			final String cookieName = cookie.getName();
			final String cookieValue = cookie.getValue();
			if (cookieName != null && cookieValue != null
					&& cookieName.length() != 0 && cookieValue.length() != 0) {
				s.append(cookieName).append("=").append(cookieValue)
						.append(";");
			}
		}
		return s.toString();
	}

}
