package com.youku.player.goplay;


import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.baseproject.utils.Logger;
import com.youku.player.LogTag;
import com.youku.player.Track;
import com.youku.player.ad.AdGetInfo;
import com.youku.player.ad.AdPosition;
import com.youku.player.module.VideoUrlInfo;
import com.youku.player.service.NetService;
import com.youku.player.util.URLContainer;

import java.util.Iterator;

/**
 * 获取广告视频
 * 
 * @author yuanfang
 * 
 */
public class GetVideoAdvService implements NetService {
	Context mContext;
	
	// 用来发送统计
	private String mVid;

	IGetAdvCallBack getAdvCallBack;
	
	/**
	 * 前贴广告开始获取时间
	 */
	private long mAdVideoGetTime;
	
	private boolean isGetADVideo;
	
	private VideoUrlInfo mVideoInfo;

	public GetVideoAdvService(VideoUrlInfo videoInfo) {
		mVideoInfo = videoInfo;
	}

	/**
	 * 获取前贴为7，中插为8， 暂停为10
	 * 
	 * @param adGetInfo
	 * @param context
	 * @param getAdvCallBack
	 */
	public void getVideoAdv(AdGetInfo adGetInfo, Context context,
			IGetAdvCallBack getAdvCallBack) {
		if (adGetInfo == null) {
			return;
		}
		mVid = adGetInfo.vid;
		mContext = context.getApplicationContext();
		this.isGetADVideo = adGetInfo.position != AdPosition.PAUSE;
		this.getAdvCallBack = getAdvCallBack;

		String url = URLContainer.getVideoAdv(adGetInfo, context);
		if (adGetInfo.position == AdPosition.PRE) {
			mAdVideoGetTime = SystemClock.elapsedRealtime();
			Logger.d(LogTag.TAG_PLAYER, "前贴广告请求地址url-->" + url);
		} else if (adGetInfo.position == AdPosition.MID) {
			mAdVideoGetTime = SystemClock.elapsedRealtime();
			Logger.d(LogTag.TAG_PLAYER, "中插广告请求地址url-->" + url);
		} else if (adGetInfo.position == AdPosition.SD) {
			mAdVideoGetTime = SystemClock.elapsedRealtime();
			Logger.d(LogTag.TAG_PLAYER, "标版广告请求地址url-->" + url);
		} else {
			Logger.d(LogTag.TAG_PLAYER, "暂停广告请求地址url-->" + url);
		}
		String userAgent = PreferenceManager.getDefaultSharedPreferences(
				context).getString("user_agent", null);
		TaskGetVideoAdvUrl getVideoAdv = new TaskGetVideoAdvUrl(url, userAgent);
		getVideoAdv.setSuccess(SUCCESS);
		getVideoAdv.setFail(FAIL);
		getVideoAdv.execute(handler);
	}

	public void getVideoAdv(AdGetInfo adGetInfo, Context context,
			String adext, IGetAdvCallBack getAdvCallBack) {
		if (adGetInfo == null) {
			return;
		}
		mVid = adGetInfo.vid;
		mContext = context.getApplicationContext();
		this.isGetADVideo = adGetInfo.position != AdPosition.PAUSE;
		this.getAdvCallBack = getAdvCallBack;

		String url = URLContainer.getVideoAdv(adGetInfo, context, adext);
		if (adGetInfo.position == AdPosition.PRE) {
			mAdVideoGetTime = SystemClock.elapsedRealtime();
			Logger.d(LogTag.TAG_PLAYER, "前贴广告请求地址url-->" + url);
		} else if (adGetInfo.position == AdPosition.MID) {
			Logger.d(LogTag.TAG_PLAYER, "中插广告请求地址url-->" + url);
		} else if (adGetInfo.position == AdPosition.SD) {
			mAdVideoGetTime = SystemClock.elapsedRealtime();
			Logger.d(LogTag.TAG_PLAYER, "标版广告请求地址url-->" + url);
		} else {
			Logger.d(LogTag.TAG_PLAYER, "暂停广告请求地址url-->" + url);
		}
		String userAgent = PreferenceManager.getDefaultSharedPreferences(
				context).getString("user_agent", null);
		TaskGetVideoAdvUrl getVideoAdv = new TaskGetVideoAdvUrl(url, userAgent);
		getVideoAdv.setSuccess(SUCCESS);
		getVideoAdv.setFail(FAIL);
		getVideoAdv.execute(handler);
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SUCCESS:
				parseVideoAdvInfo();
				break;
			case FAIL:
				getAdvCallBack.onFailed(new GoplayException());
				break;
			}
		}

		private void parseVideoAdvInfo() {
			try {
				VideoAdvInfo advInfo = JSONObject.parseObject(
						VideoAdvInfoResult.getResponseString(),
						VideoAdvInfo.class);
				Logger.d(LogTag.TAG_PLAYER,
						"获得广告信息:" + VideoAdvInfoResult.getResponseString());
				Track.trackValfLoad(mContext, mAdVideoGetTime, mVid, mVideoInfo, advInfo);
				if (isGetADVideo)
					removeNullRS(advInfo);
				getAdvCallBack.onSuccess(advInfo);
			} catch (Exception e) {
				Logger.e(LogTag.TAG_PLAYER, e);
				GoplayException ge = new GoplayException();
				getAdvCallBack.onFailed(ge);
			}
		}

		/**
		 * 如果出现rs为空，则删除这条广告
		 * @param advInfo 
		 */
		private void removeNullRS(VideoAdvInfo advInfo) {
			if (advInfo != null && advInfo.VAL != null) {
				for (Iterator<AdvInfo> it = advInfo.VAL.iterator(); it
						.hasNext();) {
					AdvInfo info = it.next();
					if (info == null
							|| (TextUtils.isEmpty(info.RS) && info.SDKID == 0)) {
						Logger.d(LogTag.TAG_PLAYER, "removeNullRS");
						it.remove();
					}
				}
			}
		}
	};

}
