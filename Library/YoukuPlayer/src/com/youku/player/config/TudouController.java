package com.youku.player.config;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.baseproject.utils.Logger;
import com.baseproject.utils.Util;
import com.baseproject.utils.Util.ENCRYPT_TYPE;
import com.youku.android.player.R;
import com.youku.player.LogTag;
import com.youku.player.apiservice.IPlayerUiControl;
import com.youku.player.apiservice.PlantformController;
import com.youku.player.base.YoukuPlayerView;
import com.youku.player.goplay.GoplayException;
import com.youku.player.goplay.IVideoInfoCallBack;
import com.youku.player.goplay.TaskGetVideoUrl;
import com.youku.player.module.VideoUrlInfo;
import com.youku.player.plugin.MediaPlayerDelegate;
import com.youku.player.service.GetHlsUrlServiceTudou;
import com.youku.player.ui.widget.TudouEncryptDialog;
import com.youku.player.util.PlayCode;
import com.youku.player.util.PlayerUtil;
import com.youku.player.util.URLContainer;
import com.youku.statistics.IRVideoWrapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;

public class TudouController extends PlantformController {
	private static final String IR_UAID = "UA-TUDOU-140001";

	public TudouController() {
		mSiteValue = "-1";
		mVidText = "itemCode";
		mLiveEncryptType = ENCRYPT_TYPE.TUDOU_LIVE;
	}

	@Override
	public void onGetVideoInfoFailed(WeakReference<FragmentActivity> context,
									 MediaPlayerDelegate mediaPlayerDelegate, GoplayException e,
									 String vid, boolean isTudouAlbum, String playlistCode) {
		FragmentActivity activity = context.get();
		if (activity == null)
			return;
		VideoUrlInfo videoUrlInfo = new VideoUrlInfo();
		if (isTudouAlbum) {
			videoUrlInfo.setVid(e.itemCode);
			videoUrlInfo.setShowId(vid);
			videoUrlInfo.setAlbum(true);
		} else {
			videoUrlInfo.setVid(vid);
			videoUrlInfo.setAlbum(false);
		}
		videoUrlInfo.playlistCode = playlistCode;
		if (e.getVideoUrlInfo() != null) {
			videoUrlInfo.setTip(e.getVideoUrlInfo().getTip());
			videoUrlInfo.setVipError(e.getVideoUrlInfo().getVipError());
			videoUrlInfo.mPayInfo = e.getVideoUrlInfo().mPayInfo;
		}
		mediaPlayerDelegate.videoInfo = videoUrlInfo;
		noRightPlay(activity, mediaPlayerDelegate, e);
		if (activity.isFinishing() || mediaPlayerDelegate == null ||
				(mediaPlayerDelegate.mediaPlayer != null && mediaPlayerDelegate.mediaPlayer.isPlaying())) {
			return;
		}
		//去掉无版权提示-104
//		if (Util.hasInternet() && e.getErrorCode() != -105 && e.getErrorCode() != -107 && e.showTip())
//			Toast.makeText(activity, e.getErrorInfo(), Toast.LENGTH_SHORT)
//					.show();
		mediaPlayerDelegate.pluginManager
				.onVideoInfoGetFail(e.getErrorCode() == -1
						|| e.getErrorCode() == -106 || e.getErrorCode() == -100
						|| e.getErrorCode() == -101 || e.getErrorCode() == -102
						|| e.getErrorCode() == -105 || e.getErrorCode() == -107);
		// TODO:土豆加密视频的播放需要等待服务器端接口调试，此时直接return，不再做加密视频的处理
		handleCallbackIfVideoEncrypted(activity, mediaPlayerDelegate, e);
		return;
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
				mediaPlayerDelegate.pluginManager.onPlayNoRightVideo(e);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	TudouEncryptDialog dialog;
	@Override
	public void handleCallbackIfVideoEncrypted(FragmentActivity context,
											   MediaPlayerDelegate mediaPlayerDelegate, GoplayException e) {
		if (e.getErrorCode() == -105) {
			dialog = createEncryptDialog(context, mediaPlayerDelegate);
			dialog.show();
			dialog.setEncryptTips(context.getResources().getString(R.string.tudou_dialog_input_password));
			return;
		}

		if (e.getErrorCode() == -107) {
			dialog = createEncryptDialog(context, mediaPlayerDelegate);
			dialog.show();
			dialog.setEncryptTips(context.getResources().getString(R.string.tudou_dialog_password_error));
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
		try {
			new JSONObject(data);
		} catch (JSONException e) {
			data = new String(PlayerUtil.decrypt(
					Base64.decode(data.getBytes(), Base64.DEFAULT),
					"094b2a34e812a4282f25c7ca1987789f"));
		}
		return data;
	}

	@Override
	public void playVideoWithPassword(MediaPlayerDelegate mediaPlayerDelegate,
									  String password) {
		mediaPlayerDelegate.playTudouVideoWithPassword(
				mediaPlayerDelegate.nowVid, password);
	}

	@Override
	public String getEncreptUrl(String url, String fieldId, String token,
								String oip, String sid, InputStream is, String did) {
		return Util.getEncreptUrl(url, fieldId, token, oip, sid,
				MediaPlayerDelegate.is);
	}

	@Override
	public String getAdDomain() {
		return URLContainer.TUDOU_AD_DOMAIN;
	}

	@Override
	public void processHttpError(TaskGetVideoUrl task, int response,
								 HttpURLConnection httpConn) {
		if (response >= 400) {
			// 土豆请求播放地址返回410，读取timestamp并更新url重新进行请求
			if (response == HttpURLConnection.HTTP_GONE) {
				InputStream is = httpConn.getErrorStream();
				double d = Double.parseDouble(Util.convertStreamToString(is));
				Util.TIME_STAMP = (long) d - System.currentTimeMillis() / 1000;
				task.setRequestUrl(URLContainer.updateUrl(task.getRequestUrl(),
						"GET"));
				task.connectAPI();
			} else
				MediaPlayerDelegate.playCode = Integer.toString(response);

		} else {
			MediaPlayerDelegate.playCode = PlayCode.SERVER_CONNECT_ERROR;
		}
	}

	@Override
	public void playHLS(Context context, VideoUrlInfo videoUrlInfo,
						String liveId, IVideoInfoCallBack listener) {
		GetHlsUrlServiceTudou getHlsUrlServiceTudou = new GetHlsUrlServiceTudou(
				context);
		getHlsUrlServiceTudou.getHlsUrl(videoUrlInfo, liveId, "flv", listener);
	}

	@Override
	public void initIRVideo(Context context) {
		IRVideoWrapper.init(context, IR_UAID, "tudou");
	}

	@Override
	public void setWaterMarkInvisible(YoukuPlayerView youkuPlayerView) {
		if (youkuPlayerView != null) {
			youkuPlayerView.setTudouWaterMarkInvisible();
		}
	}

	@Override
	public void setWaterMarkVisible(YoukuPlayerView youkuPlayerView, int type) {
		if (youkuPlayerView != null) {
			youkuPlayerView.setTudouWaterMarkFrameType(type);
		}
	}

	@Override
	public void onGetHLSVideoInfoFailed(WeakReference<FragmentActivity> context,
										GoplayException e) {

	}

	@Override
	public boolean isTrialOver(VideoUrlInfo videoInfo, int position) {
		if (videoInfo != null
				&& videoInfo.mPayInfo != null
				&& videoInfo.mPayInfo.trail != null
				&& !TextUtils.isEmpty(videoInfo.mPayInfo.trail.type)
				&& (("time".equalsIgnoreCase(videoInfo.mPayInfo.trail.type) && position / 1000 >= videoInfo.mPayInfo.trail.time) || (("episodes")
				.equalsIgnoreCase(videoInfo.mPayInfo.trail.type) && videoInfo
				.getShow_videoseq() > videoInfo.mPayInfo.trail.episodes)))
			return true;
		else
			return false;
	}

	private TudouEncryptDialog createEncryptDialog(final Context context, final MediaPlayerDelegate mediaPlayerDelegate) {
		TudouEncryptDialog encryptDialog = new TudouEncryptDialog(context, R.style.tudou_encrypt_dialog);
		encryptDialog.setPositiveClickListener(new TudouEncryptDialog.OnPositiveClickListener() {
			@Override
			public void onClick(String passWord) {
				playVideoWithPassword(mediaPlayerDelegate, passWord);
			}
		});
		encryptDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(((FragmentActivity)context).getCurrentFocus().getWindowToken(), 0);
			}
		});
		return encryptDialog;
	}

    public void disMissEncryptDialog(){
        if(dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

	@Override
	public String getEncryptParam() {
		return "&_e_=";
	}

	@Override
	public void onQualitySmoothChangeStart(IPlayerUiControl uiControl, int quality) {
		if (uiControl != null && uiControl.getUserCallback() != null) {
			uiControl.getUserCallback().onQualitySmoothChangeStart(quality);
		}
	}

	@Override
	public void onQualitySmoothChangeEnd(IPlayerUiControl uiControl, int quality) {
		if (uiControl != null && uiControl.getUserCallback() != null) {
			uiControl.getUserCallback().onQualitySmoothChangeEnd(quality);
		}
	}
}
