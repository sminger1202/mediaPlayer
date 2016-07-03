package com.youku.player.config;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.widget.Toast;

import com.baseproject.utils.Logger;
import com.baseproject.utils.Util;
import com.baseproject.utils.Util.ENCRYPT_TYPE;
import com.youku.analytics.data.Device;
import com.youku.android.player.R;
import com.youku.player.LogTag;
import com.youku.player.apiservice.IPlayerUiControl;
import com.youku.player.apiservice.PlantformController;
import com.youku.player.base.YoukuPlayerView;
import com.youku.player.goplay.GoplayException;
import com.youku.player.goplay.IVideoInfoCallBack;
import com.youku.player.goplay.TaskGetVideoUrl;
import com.youku.player.module.PayInfo;
import com.youku.player.module.PlayVideoInfo;
import com.youku.player.module.VideoUrlInfo;
import com.youku.player.plugin.MediaPlayerDelegate;
import com.youku.player.service.GetHlsUrlServiceYouku;
import com.youku.player.ui.widget.PasswordInputDialog;
import com.youku.player.ui.widget.PasswordInputDialog.PasswordInputDialogInterface;
import com.youku.player.util.PlayCode;
import com.youku.player.util.URLContainer;
import com.youku.statistics.IRVideoWrapper;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;

public class YoukuController extends PlantformController {
	private static final String IR_UAID = "UA-YOUKU-140001";

	public YoukuController() {
		mSiteValue = "1";
		mVidText = "vid";
		mLiveEncryptType = ENCRYPT_TYPE.YOUKU_LIVE;
	}

	@Override
	public void onGetVideoInfoFailed(WeakReference<FragmentActivity> context,
			MediaPlayerDelegate mediaPlayerDelegate, GoplayException e,
			String vid, boolean isTudouAlbum, String playlistCode) {
		FragmentActivity activity = context.get();
		if(activity == null )
			return;
		noRightPlay(activity, mediaPlayerDelegate, e);
		payVideo(mediaPlayerDelegate, e);
        if (activity.isFinishing() || mediaPlayerDelegate == null ||
                (mediaPlayerDelegate.mediaPlayer != null && mediaPlayerDelegate.mediaPlayer.isPlaying())) {
            return;
        }
        if (Util.hasInternet() && e.getErrorCode() != -112)
			Toast.makeText(activity, e.getErrorInfo(), Toast.LENGTH_SHORT)
					.show();
		mediaPlayerDelegate.pluginManager
				.onVideoInfoGetFail(e.getErrorCode() == -1
						|| e.getErrorCode() == 400 || e.getErrorCode() == -202
						|| e.getErrorCode() == -112 || e.getErrorCode() == -106
						|| e.getErrorCode() == -100 || e.getErrorCode() == -101
						|| e.getErrorCode() == -102 || e.getErrorCode() == -104
						|| e.getErrorCode() == -105 || e.getErrorCode() == -107);

		handleCallbackIfVideoEncrypted(activity, mediaPlayerDelegate, e);
	}

	@Override
	public void noRightPlay(Activity context,
			MediaPlayerDelegate mediaPlayerDelegate, GoplayException e) {
		try {
			// 无版权
			if (e.getErrorCode() == -104) {
				mediaPlayerDelegate.hasRight = false;
				mediaPlayerDelegate.setFirstUnloaded();
				mediaPlayerDelegate.release();
				Logger.d(LogTag.TAG_PLAYER, "无版权播放");
				if (mediaPlayerDelegate.mediaPlayer != null)
					mediaPlayerDelegate.mediaPlayer.resetSurfaceHolder();
				if (e.webUrl == null) {
					if (MediaPlayerDelegate.mIToast != null)
						MediaPlayerDelegate.mIToast.showToast("该视频暂无适合本机播放的格式");
					return;
				}
				Toast.makeText(context, "该视频暂无优酷客户端版权，需进入优酷网观看",
						Toast.LENGTH_SHORT).show();
				mediaPlayerDelegate.finishActivity();
				Uri uri = Uri.parse(e.webUrl);
				Intent it = new Intent(Intent.ACTION_VIEW, uri);
				context.startActivity(it);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void handleCallbackIfVideoEncrypted(FragmentActivity context,
			MediaPlayerDelegate mediaPlayerDelegate, GoplayException e) {

		if (e.getErrorCode() == -105 || e.getErrorCode() == -107) {
            showInputPassWordDialog(context, mediaPlayerDelegate);
        }
	}

	@Override
	public void setPlayCode(GoplayException e) {

		int code = e.getErrorCode();
		if (code == -100 || code == -101 || code == -102 || code == -104 || code == -105
				|| code == -106 || code == -107 || code == -108 || code == -112
				|| code == -125 || code == -126 || code == -127 || code == -128
				|| code == -202 || code == -204 || code == -301 || code == -308
				|| code == 400 || code == -996) {
			MediaPlayerDelegate.playCode = Integer.toString(code);
		} else {
			MediaPlayerDelegate.playCode = PlayCode.SERVER_CONNECT_ERROR;
		}

	}

	@Override
	public String processRawData(String data) {
		return data;
	}

	@Override
	public void playVideoWithPassword(MediaPlayerDelegate mediaPlayerDelegate, String password) {
		PlayVideoInfo playVideoInfo = mediaPlayerDelegate.getPlayRequest().getPlayVideoinfo();
		if (playVideoInfo != null) {
			playVideoInfo.password = password;
			mediaPlayerDelegate.playVideo(playVideoInfo);
	    }
	}

	@Override
	public String getEncreptUrl(String url, String fieldId, String token,
			String oip, String sid, InputStream is, String did) {
		return Util.getEncreptUrl(url, fieldId, token, oip, sid,
				MediaPlayerDelegate.is, Device.gdid);
	}

	void payVideo(MediaPlayerDelegate mediaPlayerDelegate, GoplayException e) {
		if (e.getErrorCode() == -112) {
			PayInfo payInfo = e.payInfo;
			if (MediaPlayerDelegate.mIPayCallBack != null) {
				MediaPlayerDelegate.mIPayCallBack.needPay(
						mediaPlayerDelegate.nowVid, payInfo);
			}
		}
	}

	@Override
	public String getAdDomain() {
		return URLContainer.YOUKU_AD_DOMAIN;
	}

	@Override
	public void processHttpError(TaskGetVideoUrl task, int response,
			HttpURLConnection httpConn) {
		MediaPlayerDelegate.playCode = PlayCode.SERVER_CONNECT_ERROR;
	}

	@Override
	public void playHLS(Context context, VideoUrlInfo videoUrlInfo,
			String liveId, IVideoInfoCallBack listener) {
		GetHlsUrlServiceYouku getHlsUrlServiceYouku = new GetHlsUrlServiceYouku(
				context);
		getHlsUrlServiceYouku.getHlsUrl(videoUrlInfo, liveId, listener);
	}

	@Override
	public void initIRVideo(Context context) {
		IRVideoWrapper.init(context, IR_UAID, "youku");
	}

	@Override
	public void setWaterMarkInvisible(YoukuPlayerView youkuPlayerView) {
		if (youkuPlayerView != null) {
			youkuPlayerView.setWaterMarkVisible(false);
		}
	}

	@Override
	public void setWaterMarkVisible(YoukuPlayerView youkuPlayerView, int type) {
		if (youkuPlayerView != null) {
			youkuPlayerView.setWaterMarkVisible(true);
		}
	}

	@Override
	public void onGetHLSVideoInfoFailed(WeakReference<FragmentActivity> context,
			GoplayException e) {
		try {
			FragmentActivity activity = context.get();
			if (activity == null)
				return;
			if (e != null && e.getErrorCode() == PlayCode.LIVE_ERROR_NEED_LOGIN) {
				Intent intent = new Intent();
				intent.setClassName(activity.getPackageName(),
						"com.youku.ui.activity.LoginRegistCardViewDialogActivity");
				intent.putExtra("from", 0);
				intent.putExtra("track_login_source", 15);
				activity.startActivity(intent);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public boolean isTrialOver(VideoUrlInfo videoInfo, int position) {
		if (videoInfo != null
				&& videoInfo.mPayInfo != null
				&& videoInfo.mPayInfo.trail != null
				&& !TextUtils.isEmpty(videoInfo.mPayInfo.trail.type)
				&& ("time".equalsIgnoreCase(videoInfo.mPayInfo.trail.type) && position / 1000 >= videoInfo.mPayInfo.trail.time))
			return true;
		else
			return false;
	}

    private void showInputPassWordDialog(final FragmentActivity activity, final MediaPlayerDelegate mediaPlayerDelegate) {
        DialogFragment newFragment = PasswordInputDialog
                .newInstance(R.string.player_error_dialog_password_required, new PasswordInputDialogInterface() {

                    @Override
                    public void onPositiveClick(String password) {
						if (Util.hasInternet()
								&& !Util.isWifi()
								&& !PreferenceManager.getDefaultSharedPreferences(
								activity).getBoolean("allowONline3G", true)
								&& mediaPlayerDelegate != null && mediaPlayerDelegate.pluginManager != null) {
							mediaPlayerDelegate.pluginManager.set3GTips();
						} else
                            playVideoWithPassword(mediaPlayerDelegate, password);
                    }

                    @Override
                    public void onNegativeClick() {

                    }
                });
        newFragment.show(activity.getSupportFragmentManager(), "dialog");
    }

	@Override
	public String getEncryptParam() {
		return "&e=";
	}

	@Override
	public void onQualitySmoothChangeStart(IPlayerUiControl uiControl, int quality) {
		if (uiControl != null) {
			uiControl.showSmoothChangeQualityTip(true);
		}
	}

	@Override
	public void onQualitySmoothChangeEnd(IPlayerUiControl uiControl, int quality) {
		if (uiControl != null) {
			uiControl.showSmoothChangeQualityTip(false);
		}
	}
}
