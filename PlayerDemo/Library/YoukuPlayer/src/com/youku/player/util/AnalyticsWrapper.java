package com.youku.player.util;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.youku.analytics.AnalyticsAgent;
import com.youku.analytics.data.PlayActionData;
import com.youku.player.Track;
import com.youku.player.goplay.AdvInfo;
import com.youku.player.module.VideoUrlInfo;
import com.youku.player.plugin.MediaPlayerDelegate;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 统计封装类
 * 
 */
public class AnalyticsWrapper {
	private static boolean isPlayRequestCalled;

	public static boolean isAnalyticsOpen() {
		// return Profile.PLANTFORM == Plantform.TUDOU ? true : false;
		return true;
	}

	public static void startSession(Activity context, String pageName,
			String userID) {
		if (isAnalyticsOpen()) {
			AnalyticsAgent.startSession(context, pageName, userID);
		}
	}

	public static void endSession(Activity context, String userID) {
		if (isAnalyticsOpen()) {
			AnalyticsAgent.endSession(context, userID);
		}
	}

	public static void setDebugMode(boolean debug) {
		if (isAnalyticsOpen()) {
			AnalyticsAgent.setDebugMode(debug);
		}
	}

	public static void playRequest(Context context, String vid, String playType) {
		if (isAnalyticsOpen()) {
			AnalyticsAgent.playRequest(context, vid, playType, getUserID());
			isPlayRequestCalled = true;
		}
	}

	public static void playStart(Context context, PlayActionData.Builder builder) {
		if (isAnalyticsOpen()) {
			if (!isPlayRequestCalled)
				AnalyticsAgent.playRequest(context, builder.getVid(),
						builder.getPlayType(), getUserID());
			isPlayRequestCalled = false;
			AnalyticsAgent.playStart(context, builder, getUserID());
		}
	}

	public static void playEnd(Context context, PlayActionData.Builder builder) {
		if (isAnalyticsOpen()) {
			AnalyticsAgent.playEnd(context, builder, getUserID());
		}
	}

	public static void playPause(Context context, String vid) {
		if (isAnalyticsOpen()) {
			AnalyticsAgent.playPause(context, vid, getUserID());
		}
	}

	public static void playContinue(Context context, String vid,
			String playcode, String userID) {
		if (isAnalyticsOpen()) {
			AnalyticsAgent.playContinue(context, vid, playcode, userID);
		}
	}

	public static void playHeart(Context context,
			PlayActionData.Builder builder, String userID) {
		if (isAnalyticsOpen()) {
			AnalyticsAgent.playHeart(context, builder, getUserID());
		}
	}

	public static void trackCustomEvent(Context context, String name,
			String page, String target, String userID) {
		if (isAnalyticsOpen()) {
			AnalyticsAgent
					.trackCustomEvent(context, name, page, target, userID);
		}
	}

	public static void trackExtendCustomEvent(Context context, String name,
			String page, String target, HashMap<String, String> extend) {
		AnalyticsAgent.trackExtendCustomEvent(context, name, page, target,
				getUserID(), extend);
	}

	public static void pageClick(Context context,
			String name, String page, String target, String refercode, HashMap<String, String> extend){
		if (isAnalyticsOpen()) {
			AnalyticsAgent.pageClick(context, name, page, target, refercode, getUserID(), extend);
		}
	}


	public static void setContinueSessionMillis(long time) {
		if (isAnalyticsOpen()) {
			AnalyticsAgent.setContinueSessionMillis(time);
		}
	}

	public static void setCaCheSize(int size) {
		if (isAnalyticsOpen()) {
			AnalyticsAgent.setCaCheSize(size);
		}
	}

	public static void setCachePersentage(float persentage) {
		if (isAnalyticsOpen()) {
			AnalyticsAgent.setCachePersentage(persentage);
		}
	}

	public static void setReportInterval(long time) {
		if (isAnalyticsOpen()) {
			AnalyticsAgent.setReportInterval(time);
		}
	}

	public static void setEventSize(int size) {
		if (isAnalyticsOpen()) {
			AnalyticsAgent.setEventSize(size);
		}
	}

	public static void setTrackLocation(boolean isTrack) {
		if (isAnalyticsOpen()) {
			AnalyticsAgent.setTrackLocation(isTrack);
		}
	}

	public static void onKillProcess(Context context, String userID) {
		if (isAnalyticsOpen()) {
			AnalyticsAgent.onKillProcess(context, userID);
		}
	}

	public static void setUserAgent(Context context, String userAgent) {
		if (isAnalyticsOpen()) {
			AnalyticsAgent.setUserAgent(context, userAgent);
		}
	}

	public static void adPlayStart(Context context, VideoUrlInfo videoInfo,
			AdvInfo advInfo) {
		if (isAnalyticsOpen() && videoInfo != null && advInfo != null) {
			ArrayList<String> arr = new ArrayList<String>();
			if (!TextUtils.isEmpty(advInfo.RS)) {
				arr.add(advInfo.RS);
				AnalyticsAgent.adPlayStart(context, Track
						.getAnalyticsVid(videoInfo), false, arr,
						MediaPlayerDelegate.mIUserInfo == null ? null
								: MediaPlayerDelegate.mIUserInfo.getUserID());
			}
		}
	}

	public static void adPlayEnd(Context context, VideoUrlInfo videoInfo,
			AdvInfo advInfo) {
		if (isAnalyticsOpen() && videoInfo != null && advInfo != null) {
			ArrayList<String> arr = new ArrayList<String>();
			if (advInfo != null && !TextUtils.isEmpty(advInfo.RS)) {
				arr.add(advInfo.RS);
			}
			AnalyticsAgent.adPlayEnd(context, Track.getAnalyticsVid(videoInfo),
					true, arr, getUserID());
		}
	}

	/**
	 * 	播放器跳过广告
	 */
	public static void adSkipClick(Context context, String vid, boolean isFullScreen){
		HashMap<String, String> extend = new HashMap<String, String>();
		if(!TextUtils.isEmpty(vid)){
			extend.put("vid", vid);
		}
		pageClick(context, "点击跳过广告",isFullScreen ? "大屏播放": "详情页", null,
				isFullScreen ? "y1.player.skipad" : "y1.detail.skipad", extend);
	}

	/**
	 * 播放器点击会员影片控件
	 */
	public static void vipVideoClick(Context context, VideoUrlInfo videoInfo, boolean isvip, boolean isFullScreen){
		HashMap<String, String> extend = new HashMap<String, String>();
		if(videoInfo != null){
			extend.put("buttonName", "buyvideo");
			extend.put("videoStatus", "playbegin");
			extend.put("vid", videoInfo.getVid());
			extend.put("isvip", isvip ? "1" : "0");
			if(videoInfo.mPayInfo != null && videoInfo.mPayInfo.payType != null) {
				StringBuffer payState = new StringBuffer();
				int size = videoInfo.mPayInfo.payType.size();
				if(size == 1) {
					payState.append(videoInfo.mPayInfo.payType.get(0));
				} else if(size == 2) {
					payState.append(videoInfo.mPayInfo.payType.get(0));
					payState.append("-");
					payState.append(videoInfo.mPayInfo.payType.get(1));
				}
				if(payState != null && payState.length() > 0){
					extend.put("payState", payState.toString());
				}
			}
		}
		pageClick(context, "点击会员影片相关控件", isFullScreen ? "大屏播放": "详情页", null,
				isFullScreen ? "y1.player.vipVideoClick" : "y1.detail.vipVideoClick", extend);
	}

	private static String getUserID() {
		return MediaPlayerDelegate.mIUserInfo == null ? null
				: MediaPlayerDelegate.mIUserInfo.getNumUserID();
	}

}
