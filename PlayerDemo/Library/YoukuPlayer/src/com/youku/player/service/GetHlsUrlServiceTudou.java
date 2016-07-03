package com.youku.player.service;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;

import com.baseproject.utils.Logger;
import com.youku.android.player.R;
import com.youku.player.LogTag;
import com.youku.player.goplay.GoplayException;
import com.youku.player.goplay.IVideoInfoCallBack;
import com.youku.player.goplay.TaskGetVideoUrl;
import com.youku.player.goplay.VideoInfoReasult;
import com.youku.player.module.LiveInfo;
import com.youku.player.module.VideoUrlInfo;
import com.youku.player.util.PlayerUtil;
import com.youku.player.util.URLContainer;

import org.json.JSONException;
import org.json.JSONObject;

public class GetHlsUrlServiceTudou implements NetService {

	private Context mContext;
	// 回调函数的监听变量
	private IVideoInfoCallBack mListener;
	private VideoUrlInfo mVideoUrlInfo;
	private String mLiveId;

	public GetHlsUrlServiceTudou(Context mContext) {
		this.mContext = mContext;
	};

	public void getHlsUrl(VideoUrlInfo mVideoUrlInfo, String liveid,
			String stream, IVideoInfoCallBack mListener) {
		String url = URLContainer.getHlsUrl(liveid, stream);
		mLiveId = liveid;
		this.mListener = mListener;
		this.mVideoUrlInfo = mVideoUrlInfo;
		Logger.d(LogTag.TAG_PLAYER, "请求播放地址 GetVideoUrlServiceTudou getVideoUrl:"
				+ url);
		TaskGetVideoUrl taskGetVideoUrl = new TaskGetVideoUrl(url);
		taskGetVideoUrl.setSuccess(SUCCESS);
		taskGetVideoUrl.setFail(FAIL);
		taskGetVideoUrl.execute(handler);
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SUCCESS:
				setVideoUrlInfo(mVideoUrlInfo);
				if (mVideoUrlInfo.mLiveInfo != null
						&& mVideoUrlInfo.mLiveInfo.errorCode != 0) {
					GoplayException mException = new GoplayException();
					mException.setErrorCode(mVideoUrlInfo.mLiveInfo.errorCode);
					mException.setErrorInfo(mVideoUrlInfo.mLiveInfo.errorMsg);
					mListener.onFailed(mException);
					Logger.d(LogTag.TAG_PLAYER, "获取正片信息 失败");
					break;
				}

				mListener.onSuccess(mVideoUrlInfo);
				Logger.d(LogTag.TAG_PLAYER, "获取正片信息 成功");
				break;
			case FAIL:
				GoplayException mException = new GoplayException();
				setVideoUrlFailReason(mException);
				mListener.onFailed(mException);
				Logger.d(LogTag.TAG_PLAYER, "获取正片信息 失败");
				break;
			}
		}
	};

	public void setVideoUrlInfo(VideoUrlInfo mResult) {
		try {
			JSONObject object = new JSONObject(
					VideoInfoReasult.getResponseString());
			// 目前是优酷Url地址，则只设置优酷相关字段信息
			Logger.d(LogTag.TAG_PLAYER, "解析服务器返回的视频信息 setVideoUrlInfo");

			String status = PlayerUtil.getJsonValue(object, "status");
			mVideoUrlInfo.setStatus(status);
			int code = PlayerUtil.getJsonInit(object, "code", 0);
			String data = object.getString("data");
			String decrypt = new String(PlayerUtil.decrypt(
					Base64.decode(data.getBytes(), Base64.DEFAULT),
					"094b2a34e812a4282f25c7ca1987789f"));
			Logger.d(LogTag.TAG_PLAYER, "解析服务器返回的视频信息:" + decrypt);
			mVideoUrlInfo.setCode(code);
			object = new JSONObject(decrypt);

			JSONObject playData = object.optJSONObject("play");
			if (playData != null) {
				String token = playData.optString("token");
				String oip = playData.optString("ip");
				String sid = playData.optString("sid");
				mVideoUrlInfo.token = token;
				mVideoUrlInfo.oip = oip;
				mVideoUrlInfo.sid = sid;
				mVideoUrlInfo.bps = playData.optString("bps");
				mVideoUrlInfo.channel = playData.optString("channel");
				mVideoUrlInfo.setUrl(playData.optString("url"));
			}
			mVideoUrlInfo.isHLS = true;
			mVideoUrlInfo.mLiveInfo = new LiveInfo();
			mVideoUrlInfo.mLiveInfo.liveId = mLiveId;

			JSONObject live = object.optJSONObject("live");
			if (live != null) {
				mVideoUrlInfo.mLiveInfo.status = live.optInt("status");
				mVideoUrlInfo.mLiveInfo.title = live.optString("title");
				mVideoUrlInfo.mLiveInfo.desc = live.optString("desc");

				JSONObject set = live.optJSONObject("set");
				if (set != null) {
					mVideoUrlInfo.mLiveInfo.front_adid = set
							.optString("front_adid");
					mVideoUrlInfo.mLiveInfo.picurl = set.optString("picurl");
				}
				JSONObject error = live.optJSONObject("error");
				if (error != null) {
					mVideoUrlInfo.mLiveInfo.errorCode = error.optInt("code");
					mVideoUrlInfo.mLiveInfo.errorMsg = error.optString("msg");
				}
			}
			JSONObject controller = object.optJSONObject("controller");
			if (controller != null) {
				mVideoUrlInfo.mLiveInfo.autoplay = controller
						.optInt("autoplay");
			}

			// TODO 设置vid
			mVideoUrlInfo.setVid(mLiveId);

		} catch (JSONException e) {
			Logger.e(LogTag.TAG_PLAYER, "解析服务器返回的视频信息 setVideoUrlInfo 出错");
			Logger.e(LogTag.TAG_PLAYER, e);
		}
	}

	/**
	 * 获取Url失败时设置失败原因
	 * 
	 * @param mResult
	 *            ：存储解析结果的GoplayException对象 msg：联网获取的数据结果
	 * 
	 * @return 无返回值
	 * 
	 * */
	protected void setVideoUrlFailReason(GoplayException mResult) {
		try {
			int code = 0;
			if (VideoInfoReasult.getResponseString() != null) {
				JSONObject object = new JSONObject(
						VideoInfoReasult.getResponseString());
				code = PlayerUtil.getJsonInit(object, "error_code", 0);
				if (code == 0)
					code = PlayerUtil.getJsonInit(object, "code", 0);
				mVideoUrlInfo.setCode(code);
				mVideoUrlInfo.setWebViewUrl(object.optString("webviewurl"));
				mResult.itemCode = object.optString("itemCode");
				mResult.setErrorCode(code);
				if (code == -104) {
					mResult.webUrl = object.optString("webviewurl");
				}
			}

			if (mVideoUrlInfo.isCached()) {
				mResult.setErrorInfo(mContext.getText(
						R.string.player_error_native).toString());
			} else {
				switch (code) {
				case -101:
					mResult.setErrorInfo(mContext.getText(
							R.string.player_error_f101).toString());
					break;
				case -102:
					mResult.setErrorInfo(mContext.getText(
							R.string.player_error_f102).toString());
					break;
				case -104:
					mResult.setErrorInfo(mContext
							.getText(R.string.no_copyright).toString());
					break;
				case -105:
					// TODO:土豆加密视频的播放需要等待服务器端接口调试，此时暂时提示用户去看其他视频
					mResult.setErrorInfo(mContext.getText(
							R.string.player_error_f105_see_others).toString());
					break;
				case -106:
					mResult.setErrorInfo(mContext.getText(
							R.string.player_error_f106).toString());
					break;
				case -107:
					mResult.setErrorInfo(mContext.getText(
							R.string.player_error_f107).toString());
				case -202:
					mResult.setErrorInfo(mContext.getText(
							R.string.player_error_url_is_nul).toString());
					break;
				case -100:
					mResult.setErrorInfo(mContext.getText(
							R.string.Player_error_f100).toString());
					break;
				case -112:
					mResult.setErrorInfo(mContext.getText(
							R.string.player_error_no_pay).toString());
					break;
				default:
					mResult.setErrorInfo(mContext.getText(
							R.string.Player_error_timeout).toString());
					break;
				}
			}
		} catch (JSONException e) {
			// 在非登录的cmcc网络下返回html页面导致json解析失败，使用网络错误提示
			mResult.setErrorInfo(mContext
					.getText(R.string.Player_error_timeout).toString());
			Logger.e(LogTag.TAG_PLAYER, e);
		}
	}
}
