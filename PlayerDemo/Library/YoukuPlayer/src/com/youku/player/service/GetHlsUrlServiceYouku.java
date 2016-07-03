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
import com.youku.player.goplay.Profile;
import com.youku.player.goplay.TaskGetVideoUrl;
import com.youku.player.goplay.VideoInfoReasult;
import com.youku.player.module.LiveInfo;
import com.youku.player.module.VideoUrlInfo;
import com.youku.player.util.PlayerUtil;
import com.youku.player.util.URLContainer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GetHlsUrlServiceYouku implements NetService {

	private Context mContext;
	// 回调函数的监听变量
	private IVideoInfoCallBack mListener;
	private VideoUrlInfo mVideoUrlInfo;
	private String mLiveId;
	private final String COMBINE_URL_PRE="http://l.youku.com/securelive";

	public GetHlsUrlServiceYouku(Context context) {
		mContext = context;
	};

	public void getHlsUrl(VideoUrlInfo videoUrlInfo, String liveId,
			IVideoInfoCallBack listener) {
		String url = URLContainer.getYoukuHlsUrl(liveId);
		mLiveId = liveId;
		mListener = listener;
		mVideoUrlInfo = videoUrlInfo;
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
				setVideoUrlInfo();
				if (mVideoUrlInfo.mLiveInfo != null
						&& mVideoUrlInfo.mLiveInfo.errorCode != 0) {
					GoplayException mException = new GoplayException();
					mException.setErrorCode(mVideoUrlInfo.mLiveInfo.errorCode);
					mException.setErrorInfo(mVideoUrlInfo.mLiveInfo.errorMsg);
					mException.setVideoUrlInfo(mVideoUrlInfo);
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

	public void setVideoUrlInfo() {
		try {

			JSONObject json = new JSONObject(
					VideoInfoReasult.getResponseString());
			String data = json.getString("data");
			byte[] bytes = Base64.decode(data.getBytes(), Base64.DEFAULT);
			String decrypt = new String(PlayerUtil.decrypt(bytes,
					"qwer3as2jin4fdsa"));
			JSONObject object = new JSONObject(decrypt);
			Logger.d(LogTag.TAG_PLAYER, "解析服务器返回的视频信息 setVideoUrlInfo" + decrypt);
			setVideoUrlInfoFromJson(object);
		} catch (JSONException e) {
			Logger.e(LogTag.TAG_PLAYER, "解析服务器返回的视频信息 setVideoUrlInfo 出错");
			Logger.e(LogTag.TAG_PLAYER, e);
		}
	}

	public void setVideoUrlInfoFromJson(JSONObject object) {
		mVideoUrlInfo.isHLS = true;
		mVideoUrlInfo.mLiveInfo = new LiveInfo();
		mVideoUrlInfo.mLiveInfo.liveId = mLiveId;

		mVideoUrlInfo.setStatus(object.optString("status"));
		int code = PlayerUtil.getJsonInit(object, "code", 0);
		mVideoUrlInfo.setCode(code);
		JSONObject live = object.optJSONObject("live");
		if (live != null) {
			mVideoUrlInfo.token = live.optString("token");
			mVideoUrlInfo.oip = live.optString("ip");
			mVideoUrlInfo.sid = live.optString("sid");
			mVideoUrlInfo.mLiveInfo.isPaid = live.optInt("paid");
			JSONObject streams = live.optJSONObject("streams");
			if (streams != null) {
				if (streams.has("0")) {
					JSONArray streamsInfoArray = streams.optJSONArray("0");
					if (streamsInfoArray != null
							&& streamsInfoArray.length() > 0) {
						JSONObject streamsInfo = streamsInfoArray
								.optJSONObject(0);
						if (streamsInfo != null) {
							mVideoUrlInfo.bps = streamsInfo.optString("bps");
							mVideoUrlInfo.channel = streamsInfo
									.optString("channel");
							mVideoUrlInfo.mLiveInfo.channel = streamsInfo
									.optString("channel");
							mVideoUrlInfo.offset = streamsInfo
									.optString("offset");
						}
					}
				}
			}
			mVideoUrlInfo.setUrl(getUrl());

			mVideoUrlInfo.mLiveInfo.status = live.optInt("status");
			mVideoUrlInfo.mLiveInfo.title = live.optString("title");
			mVideoUrlInfo.setTitle(mVideoUrlInfo.mLiveInfo.title);
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
			mVideoUrlInfo.mLiveInfo.starttime = live.optLong("starttime");
			mVideoUrlInfo.mLiveInfo.endtime = live.optLong("endtime");
			mVideoUrlInfo.mLiveInfo.servertime = live.optLong("servertime");
		}
		JSONObject controller = object.optJSONObject("controller");
		if (controller != null) {
			mVideoUrlInfo.mLiveInfo.autoplay = controller.optInt("autoplay");
			mVideoUrlInfo.mLiveInfo.isFullScreen = controller.optInt("fullscreen");
			mVideoUrlInfo.mLiveInfo.areaCode = controller.optInt("area_code");
			mVideoUrlInfo.mLiveInfo.dmaCode = controller.optInt("dma_code");
			mVideoUrlInfo.mLiveInfo.with_barrage = controller.optInt("with_barrage");
			mVideoUrlInfo.mLiveInfo.barrage_id = controller.optInt("barrage_id");
		}
        if (object.has("user")) {
            JSONObject user = object.optJSONObject("user");
            if (user != null) {
                mVideoUrlInfo.mLiveInfo.isVip = user.optBoolean("vip");
            }
        }

		// TODO 设置vid
		mVideoUrlInfo.setVid(mLiveId);
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
							R.string.player_error_other).toString());
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

	private String getUrl() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(COMBINE_URL_PRE)
				.append("/channel/").append(mVideoUrlInfo.channel)
				.append("/bps/").append(mVideoUrlInfo.bps).append("/offset/")
				.append(mVideoUrlInfo.offset).append("/sid/")
				.append(mVideoUrlInfo.sid).append("?ctype=")
				.append(Profile.ctypeHLS).append("&ev=").append(Profile.ev)
				.append("&token=").append(mVideoUrlInfo.token).append("&oip=").append(mVideoUrlInfo.oip);
		return stringBuffer.toString();
	}
}
