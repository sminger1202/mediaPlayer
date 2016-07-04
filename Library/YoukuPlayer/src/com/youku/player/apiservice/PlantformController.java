package com.youku.player.apiservice;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentActivity;

import com.baseproject.utils.Util.ENCRYPT_TYPE;
import com.youku.player.base.YoukuPlayerView;
import com.youku.player.goplay.GoplayException;
import com.youku.player.goplay.IVideoInfoCallBack;
import com.youku.player.goplay.TaskGetVideoUrl;
import com.youku.player.module.VideoUrlInfo;
import com.youku.player.plugin.MediaPlayerDelegate;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;

/**
 * 处理各平台差异
 */
public abstract class PlantformController {
	protected String mSiteValue;

	protected String mVidText;

	protected ENCRYPT_TYPE mLiveEncryptType;

	public abstract void onGetVideoInfoFailed(WeakReference<FragmentActivity> context,
			MediaPlayerDelegate mediaPlayerDelegate, GoplayException e,
			String vid, boolean isTudouAlbum, String playlistCode);

	public abstract void noRightPlay(Activity context,
			MediaPlayerDelegate mediaPlayerDelegate, final GoplayException e);

	public abstract void handleCallbackIfVideoEncrypted(
            FragmentActivity context,
			MediaPlayerDelegate mediaPlayerDelegate, GoplayException e);

	public abstract void setPlayCode(GoplayException e);

	public abstract String processRawData(String data);

	public abstract void playVideoWithPassword(
			MediaPlayerDelegate mediaPlayerDelegate, String password);

	public String getSiteValue() {
		return mSiteValue;
	}

	public String getVidText() {
		return mVidText;
	}

	public ENCRYPT_TYPE getLiveEncyptType() {
		return mLiveEncryptType;
	}

	public abstract String getEncreptUrl(String url, String fieldId,
			String token, String oip, String sid, InputStream is, String did);

	public abstract String getAdDomain();

	public abstract void processHttpError(TaskGetVideoUrl task, int response,
			HttpURLConnection httpConn);

	public abstract void playHLS(Context context, VideoUrlInfo videoUrlInfo,
			String liveId, IVideoInfoCallBack listener);

	public abstract void initIRVideo(Context context);

	public abstract void setWaterMarkInvisible(YoukuPlayerView context);

	public abstract void setWaterMarkVisible(YoukuPlayerView context,
			int type);

	public abstract void onGetHLSVideoInfoFailed(
			WeakReference<FragmentActivity> context, GoplayException e);

	public abstract boolean isTrialOver(VideoUrlInfo videoInfo,int position);

	public abstract String getEncryptParam();

	public abstract void onQualitySmoothChangeStart(IPlayerUiControl uiControl, int quality);

	public abstract void onQualitySmoothChangeEnd(IPlayerUiControl uiControl, int quality);

	public void disMissEncryptDialog(){
	}
}
