package com.youku.player.plugin;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.baseproject.utils.Logger;
import com.youku.player.LogTag;
import com.youku.player.ad.AdForward;
import com.youku.player.ad.AdTaeSDK;
import com.youku.player.base.Plantform;
import com.youku.player.config.MediaPlayerConfiguration;
import com.youku.player.goplay.AdvInfo;
import com.youku.player.goplay.Profile;
import com.youku.player.util.AdUtil;

import java.util.Arrays;

public class AdvClickProcessor {
	/**
	 * 处理广告点击，点击地址以.apk结尾则调起下载器下载
	 * 
	 * @param context
	 * @param url
	 */
	public void processAdvClick(Activity context, String url, AdvInfo advInfo) {
        if (TextUtils.isEmpty(url) || advInfo == null) {
            return;
        }
        int adForward = advInfo.CUF;
		if (AdUtil.isDownloadAPK(advInfo, url) && MediaPlayerDelegate.mIDownloadApk != null) {
			if(adForward == AdForward.GAME_CENTER)
				MediaPlayerDelegate.mIDownloadApk.downloadApkById(advInfo.CU,  getDUS(advInfo), getDUE(advInfo));
			else
				MediaPlayerDelegate.mIDownloadApk.downloadApk(url, getDUS(advInfo), getDUE(advInfo));
		} else {
			if (adForward == AdForward.WEBVIEW
					&& MediaPlayerConfiguration.getInstance().showAdWebView()) {
				if (Profile.PLANTFORM == Plantform.YOUKU) {
					showYoukuWebView(context, url);
				} else {
					Intent intent = new Intent(context,
							com.youku.player.ad.AdWebViewActivity.class);
					intent.putExtra("url", url);
					context.startActivity(intent);
				}
			} else if (adForward == AdForward.YOUKU_VIDEO) {
				try {
					int start = url.indexOf("u=");
					int end = url.indexOf("&", start);
					String id;
					if (end > 0) {
						id = url.substring(start + 2, end);
					} else {
						id = url.substring(start + 2);
					}
					Intent intent = new Intent();
					intent.setClassName(context.getPackageName(),
							MediaPlayerConfiguration.getInstance().getDetailActivityName());
					intent.putExtra("video_id", id);
					context.startActivity(intent);
				} catch (Exception e) {
				} finally {
				}
			} else if (adForward == AdForward.TAESDK_WEBVIEW) {
				if (AdTaeSDK.isInitTaeSDK) {
					AdTaeSDK.showPage(context, url);
				} else {
					showYoukuWebView(context, url);
				}
			} else {
				Uri uri = Uri.parse(url);
				Intent it = new Intent(Intent.ACTION_VIEW, uri);
				context.startActivity(it);
			}
		}
	}

	/**
	 * trueview广告点击“观看广告”处理
	 * @param point 当前广告的播放时间(毫秒)
	 */
	public void trueViewAdvPlayClicked(Activity context, AdvInfo advInfo, int point){
		Logger.d(LogTag.TAG_TRUE_VIEW , "------> trueViewAdvPlayClicked() ");
		if (advInfo == null || advInfo.EM == null || advInfo.EM.VIEW == null) {
			return;
		}
		String url = advInfo.EM.VIEW.CU;
		String vid = advInfo.EM.VIEW.VID;
		Logger.d(LogTag.TAG_TRUE_VIEW,"vid : " + vid);
		Logger.d(LogTag.TAG_TRUE_VIEW,"point : " + point);
		if(!TextUtils.isEmpty(vid) && !vid.equals("0")){ // 跳转app视频页
			try {
				Intent intent = new Intent();
				intent.setClassName(context.getPackageName(), MediaPlayerConfiguration.getInstance().getDetailActivityName());
				intent.putExtra("video_id", vid);
				intent.putExtra("point", point);
				context.startActivity(intent);
			} catch (Exception e) {
			} finally {
			}
		} else { // 使用webview打开
			showYoukuWebView(context,url);
		}
	}

	/**
	 * 使用优酷WebView打开
	 * 
	 * @param context
	 * @param url
	 */
	private void showYoukuWebView(Activity context, String url) {
		try {
			if (Profile.PLANTFORM == Plantform.YOUKU) {
				Intent intent = new Intent("com.youku.action.YoukuWebview");
				intent.putExtra("url", url);
				intent.putExtra("isAdver", true);
				context.startActivity(intent);
			} else {
				Uri uri = Uri.parse(url);
				Intent it = new Intent(Intent.ACTION_VIEW, uri);
				context.startActivity(it);
			}
		} catch (Exception e) {
		}
	}

    /**
     * 获取DUS曝光地址数组
     */
    private String[] getDUS(AdvInfo advInfo){
        if(advInfo == null || advInfo.DUS == null || advInfo.DUS.size() < 1){
            Logger.d(LogTag.TAG_PLAYER,"AdvClickProcessor ------> getDUS : null");
            return null;
        }
        int size = advInfo.DUS.size();
        String[] startTrack = new String[size];
        for (int i = 0; i < size; i++) {
            startTrack[i] = advInfo.DUS.get(i).U;
        }
        Logger.d(LogTag.TAG_PLAYER,"AdvClickProcessor ------> getDUS : " + Arrays.asList(startTrack));
        return startTrack;
    }

    /**
     * 获取DUE曝光地址数组
     */
    private String[] getDUE(AdvInfo advInfo){
        if(advInfo == null || advInfo.DUE == null || advInfo.DUE.size() < 1){
            Logger.d(LogTag.TAG_PLAYER,"AdvClickProcessor ------> getDUE : null");
            return null;
        }
        int size = advInfo.DUE.size();
        String[] endTrack = new String[size];
        for (int i = 0; i < size; i++){
            endTrack[i] = advInfo.DUE.get(i).U;
        }
        Logger.d(LogTag.TAG_PLAYER,"AdvClickProcessor ------> getDUE : " + Arrays.asList(endTrack));
        return endTrack;
    }

}
