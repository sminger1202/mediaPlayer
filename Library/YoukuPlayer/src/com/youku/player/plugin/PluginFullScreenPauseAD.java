package com.youku.player.plugin;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.baseproject.utils.Logger;
import com.baseproject.utils.Util;
import com.youdo.vo.XAdInstance;
import com.youku.android.player.R;
import com.youku.player.LogTag;
import com.youku.player.ad.AdGetInfo;
import com.youku.player.ad.AdPosition;
import com.youku.player.ad.AdVender;
import com.youku.player.ad.OfflineAdSDK;
import com.youku.player.ad.pausead.IPauseAdCallback;
import com.youku.player.ad.pausead.PauseAdContext;
import com.youku.player.apiservice.IPlayerAdControl;
import com.youku.player.apiservice.IPlayerUiControl;
import com.youku.player.config.MediaPlayerConfiguration;
import com.youku.player.goplay.AdvInfo;
import com.youku.player.goplay.GetVideoAdvService;
import com.youku.player.goplay.GoplayException;
import com.youku.player.goplay.IGetAdvCallBack;
import com.youku.player.goplay.IGetOfflineAdvCallBack;
import com.youku.player.goplay.Profile;
import com.youku.player.goplay.StaticsUtil;
import com.youku.player.goplay.VideoAdvInfo;
import com.youku.player.module.VideoUrlInfo;
import com.youku.player.module.VideoUrlInfo.Source;
import com.youku.player.util.AnalyticsWrapper;
import com.youku.player.util.DetailMessage;
import com.youku.player.util.DisposableStatsUtils;
import com.youku.player.util.SessionUnitil;
import com.youku.player.util.URLContainer;

public class PluginFullScreenPauseAD extends PluginOverlay implements
		DetailMessage {
	LayoutInflater mLayoutInflater;
	View containerView;
	private Activity mActivity;
	private MediaPlayerDelegate mediaPlayerDelegate;
    private IPlayerUiControl mPlayerUiControl;
    private IPlayerAdControl mPlayerAdControl;
    private PauseAdContext mPauseAdContext;
    private FrameLayout mPauseAdContainer;
    private int mRequest = 1;

	private int mAdType = AdVender.YOUKU;

    //测试使用ad
	private String mTestAd = null;

    public PluginFullScreenPauseAD(Activity context, MediaPlayerDelegate mediaPlayerDelegate,
                                   IPlayerUiControl playerUiControl, IPlayerAdControl playerAdControl) {
        super(context, mediaPlayerDelegate);
        this.mediaPlayerDelegate = mediaPlayerDelegate;
        mActivity = context;
        mPlayerUiControl = playerUiControl;
        mPlayerAdControl = playerAdControl;
        mLayoutInflater = LayoutInflater.from(context);
        mPauseAdContext = new PauseAdContext(context, mediaPlayerDelegate, playerUiControl, playerAdControl);
        init();
    }

	private void init() {
		containerView = mLayoutInflater.inflate(
				R.layout.yp_plugin_player_popup_ad, null);
		addView(containerView);
		findView();
	}

	private void findView() {
        mPauseAdContainer = (FrameLayout) containerView
                .findViewById(R.id.play_middle_setting);
		containerView.setVisibility(View.GONE);
	}

	private String TAG = "PluginFullScreenPauseAD";
	protected VideoAdvInfo pauseADVideoAdvInfo = null;

	@Override
	public void onBufferingUpdateListener(int percent) {

	}

	@Override
	public void onCompletionListener() {

	}

	@Override
	public boolean onErrorListener(int what, int extra) {
		return false;
	}

	@Override
	public void OnPreparedListener() {

	}

	@Override
	public void OnSeekCompleteListener() {
	}

	@Override
	public void OnVideoSizeChangedListener(int width, int height) {
	}

	@Override
	public void OnTimeoutListener() {

	}

	@Override
	public void OnCurrentPositionChangeListener(int currentPosition) {
		if (!firstLoaded) {
			firstLoaded = true;
		}
		if (mediaPlayerDelegate != null && mediaPlayerDelegate.isPlaying()) {
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					dismissPauseAD();
				}
			});
		}
	}

	@Override
	public void onLoadedListener() {
		// pauseADcanceled = false;
	}

	@Override
	public void onLoadingListener() {
		// pauseADcanceled = true;
	}

	@Override
	public void onUp() {
	}

	@Override
	public void onDown() {
	}

	@Override
	public void onFavor() {
	}

	@Override
	public void onUnFavor() {
	}

	@Override
	public void newVideo() {
	}

	@Override
	public void onVolumnUp() {
	}

	@Override
	public void onVolumnDown() {
	}

	@Override
	public void onMute(boolean mute) {
	}

	@Override
	public void onVideoChange() {
		firstLoaded = false;
	}

	@Override
	public void onVideoInfoGetting() {
	}

	@Override
	public void onVideoInfoGetted() {

	}

	@Override
	public void onVideoInfoGetFail(boolean needRetry) {

	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			containerView.setVisibility(View.VISIBLE);
		} else {
			dismissPauseAD();
		}
	}

	@Override
	public void onPluginAdded() {
		super.onPluginAdded();
		containerView.setVisibility(View.GONE);
	}

	@Override
	public void onNotifyChangeVideoQuality() {

	}

	@Override
	public void onRealVideoStart() {

	}

	@Override
	public void onADplaying() {

	}

	private boolean pauseADcanceled = false;

	private boolean isVideoNoAdv() {

		VideoUrlInfo videoInfo = mMediaPlayerDelegate.videoInfo;
		boolean notFromYouku = videoInfo.mSource != Source.YOUKU;

		if (notFromYouku) {
			Logger.d(LogTag.TAG_PLAYER, "PluginImageAD->isVideoNoAdv = true");
			return true;
		}

		Logger.d(LogTag.TAG_PLAYER, "PluginImageAD->isVideoNoAdv = false");
		return false;
	}

	private boolean isLocalVideo(VideoUrlInfo videoInfo) {
		if (videoInfo == null) {
			return false;
		}
		return videoInfo.playType.equals(StaticsUtil.PLAY_TYPE_LOCAL);
	}

    //离线广告
    private XAdInstance mOfflinePauseAd;

	/**
	 * 开始获取暂停广�?
	 */
	public void showPauseAD() {
		Logger.d(LogTag.TAG_PLAYER, "暂停广告showPauseAD id:"
				+ mMediaPlayerDelegate.videoInfo.getVid());

        if (mMediaPlayerDelegate.isADShowing ||
                (mPlayerAdControl != null && mPlayerAdControl.isMidAdShowing())) {
            return;
        }
        if (isVideoNoAdv() || Profile.from == Profile.PHONE_BROWSER) {
			return;
		}

		boolean isOfflineAd = isLocalVideo(mMediaPlayerDelegate.videoInfo);
		// 只有youku请求离线广告
		if (isOfflineAd && !MediaPlayerConfiguration.getInstance().showOfflineAd())
			return;
		pauseADcanceled = false;
        /**
         * 请求离线暂停广告
         * FIXME 联网状态下，点击首页最近播放的离线视频，isOfflineAd 不准确，使用mMediaPlayerDelegate.videoInfo.isCached()
         */
        if (!Util.isWifi() && mMediaPlayerDelegate.videoInfo.isCached()
                && MediaPlayerConfiguration.getInstance().showOfflineAd()
                && ( MediaPlayerDelegate.mIUserInfo == null || !MediaPlayerDelegate.mIUserInfo.isVip())) {
            OfflineAdSDK.getPauserollAd(new IGetOfflineAdvCallBack() {
                @Override
                public void onSuccess(VideoAdvInfo videoAdvInfo, XAdInstance instance) {
                    mOfflinePauseAd = instance;
                    pauseADVideoAdvInfo = videoAdvInfo;
                    if (pauseADVideoAdvInfo != null) {
                        int size = pauseADVideoAdvInfo.VAL.size();
                        if (size == 0) {
                            mADURL = "";
                            Logger.d(LogTag.TAG_PLAYER, "离线暂停广告VC:为空");
                        }
                        mAdType = AdVender.OFFLINE_AD;//FIXME 设置为离线广告
                        mPauseAdContext.setOfflineAdInstance(mOfflinePauseAd);
                        for (int i = 0; i < size; i++) {
                            mADURL = pauseADVideoAdvInfo.VAL.get(i).RS;
                        }
                    }
                    Logger.d(LogTag.TAG_PLAYER, "离线暂停广告地址 imageURL--->" + mADURL);
                    if (mAdType == AdVender.OFFLINE_AD && !TextUtils.isEmpty(mADURL)) {
                        showADImage();
                    }
                }

                @Override
                public void onFailed(GoplayException e) {
                }
            });

        } else { //请求在线暂停广告
			if(mMediaPlayerDelegate.videoInfo.isCached() && MediaPlayerConfiguration.getInstance().showOfflineAd() && !Util.isWifi()){
				return;//当是离线视频在移动网络环境下不请求在线暂停广告
			}
            GetVideoAdvService getVideoAdvService = new GetVideoAdvService(mMediaPlayerDelegate.videoInfo);
            if (!TextUtils.isEmpty(mMediaPlayerDelegate.videoInfo.getVid())) {
                AdGetInfo adGetInfo = new AdGetInfo(
                        mMediaPlayerDelegate.videoInfo.getVid(), AdPosition.PAUSE,
                        mMediaPlayerDelegate.isFullScreen, isOfflineAd, null, mMediaPlayerDelegate.videoInfo.playlistId,
                        mMediaPlayerDelegate.videoInfo, mMediaPlayerDelegate.getPlayerUiControl().isVerticalFullScreen());
                if (mTestAd == null || mTestAd.length() == 0) {
                    getVideoAdvService.getVideoAdv(adGetInfo, mActivity,
                            new IGetAdvCallBack() {

                                @Override
                                public void onSuccess(VideoAdvInfo videoAdvInfo) {
                                    pauseADVideoAdvInfo = videoAdvInfo;
                                    if (videoAdvInfo != null) {
                                        for (AdvInfo advInfo : videoAdvInfo.VAL) {
                                            if ("2".equals(advInfo.VT)) {
                                                DisposableStatsUtils
                                                        .disposePausedVC(advInfo);
                                                videoAdvInfo.VAL.remove(advInfo);
                                            }
                                        }
                                    }
                                    if (pauseADVideoAdvInfo != null) {
                                        int size = pauseADVideoAdvInfo.VAL.size();
                                        if (size == 0) {
                                            mADURL = "";
                                            Logger.d(LogTag.TAG_PLAYER, "暂停广告VC:为空");
                                        }
                                        for (int i = 0; i < size; i++) {
                                            mADURL = pauseADVideoAdvInfo.VAL.get(i).RS;
                                            mAdType = pauseADVideoAdvInfo.VAL
                                                    .get(i).SDKID;
                                        }
                                    }
                                    // 用于测试 假设每次都能取到图片
                                    // mADURL =
                                    // "http://g4.ykimg.com/11270F1F46509C3F5716DA0123193CA669B69C-09D5-BA6A-22B6-2EE5F6CD4A55";
                                    Logger.d(LogTag.TAG_PLAYER, "暂停广告地址 imageURL--->" + mADURL);
                                    if (mAdType == AdVender.YOUKU
                                            && (mADURL == null || mADURL.equals("")))
                                        return;
                                    showADImage();
                                }

                                @Override
                                public void onFailed(GoplayException e) {
                                    disposeAdLoss(URLContainer.AD_LOSS_STEP2);
                                }
                            });
                } else {
                    getVideoAdvService.getVideoAdv(adGetInfo, mActivity,
                            mTestAd, new IGetAdvCallBack() {

                                @Override
                                public void onSuccess(VideoAdvInfo videoAdvInfo) {
                                    pauseADVideoAdvInfo = videoAdvInfo;
                                    if (videoAdvInfo != null) {
                                        for (AdvInfo advInfo : videoAdvInfo.VAL) {
                                            if ("2".equals(advInfo.VT)) {
                                                DisposableStatsUtils
                                                        .disposePausedVC(advInfo);
                                                videoAdvInfo.VAL.remove(advInfo);
                                            }
                                        }
                                    }
                                    if (pauseADVideoAdvInfo != null) {
                                        int size = pauseADVideoAdvInfo.VAL.size();
                                        if (size == 0) {
                                            mADURL = "";
                                            Logger.d(LogTag.TAG_PLAYER, "暂停广告VC:为空");
                                        }
                                        for (int i = 0; i < size; i++) {
                                            mADURL = pauseADVideoAdvInfo.VAL.get(i).RS;
                                            mAdType = pauseADVideoAdvInfo.VAL.get(i).SDKID;
                                        }
                                    }
                                    Logger.d(LogTag.TAG_PLAYER, "暂停广告地址 imageURL--->" + mADURL);
                                    if (mAdType == AdVender.YOUKU
                                            && (mADURL == null || mADURL.equals("")))
                                        return;
                                    showADImage();
                                }

                                @Override
                                public void onFailed(GoplayException e) {
                                }
                            });
                }

            }
        }
	}

	public void setTestAd(String testAd) {
		mTestAd = testAd;
	}

	private String mADURL;

	/**
	 * 获取暂停广告信息去加�?
	 */
	protected void showADImage() {
		if ((mediaPlayerDelegate != null && !mediaPlayerDelegate.isFullScreen)
				|| mActivity.isFinishing()) {
			disposeAdLoss(URLContainer.AD_LOSS_STEP1);
			return;
		}
		// 显示暂停广告
		AdvInfo advInfo = null;
		try {
			// 显示广告的时候是使用
			advInfo = getAdvInfo();
			if (advInfo == null) {
				Logger.d(LogTag.TAG_PLAYER, "暂停广告显示 SUS:为空");
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.e(LogTag.TAG_PLAYER, "暂停广告显示 SUS为空");
		} finally {
			if (advInfo != null) {
				Logger.d(LogTag.TAG_PLAYER, "暂停广告SDK --->" + mAdType);
                removeAllAd();
                mPauseAdContext.setType(mAdType);
                mPauseAdContext.setContainer(mPauseAdContainer);
                ++mRequest;
                mPauseAdContext.show(getAdvInfo(), mRequest, mPauseAdCallback);
                if (mPlayerAdControl != null) {
                    mPlayerAdControl.releaseInvestigate();
                }
            }
		}
	}

	public boolean isVisible() {
		return containerView.getVisibility() == View.VISIBLE;
	}

	/**
	 * 获取广告信息
	 * 
	 * @return
	 */
	private AdvInfo getAdvInfo() {
		try {
			return pauseADVideoAdvInfo.VAL.get(0);
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * 不显示暂停广告蒙�?
	 */
	public void dismissPauseAD() {
        pauseADcanceled = true;
        if (containerView.getVisibility() == View.VISIBLE) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    containerView.setVisibility(View.GONE);
                    removeAllAd();
                }
            });
            if (mAdType == AdVender.YOUKU) {
                AdvInfo advInfo = getAdvInfo();
                if (mMediaPlayerDelegate != null)
                    AnalyticsWrapper.adPlayEnd(mActivity,
                            mMediaPlayerDelegate.videoInfo, advInfo);
                DisposableStatsUtils.disposePausedSUE(
                        mActivity.getApplicationContext(), advInfo);
            }
        }
    }

	@Override
	public void onRealVideoStarted() {

	}

	@Override
	public void onStart() {

	}

	@Override
	public void onClearUpDownFav() {

	}

	@Override
	public void onPause() {
	}

	boolean firstLoaded = false;

	@Override
	public void back() {
	}

	@Override
	public void onPlayNoRightVideo(GoplayException e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlayReleateNoRightVideo() {
		// TODO Auto-generated method stub

	}

	private void disposeAdLoss(int step) {
		DisposableStatsUtils.disposeAdLoss(mActivity, step,
				SessionUnitil.playEvent_session, URLContainer.AD_LOSS_MP);
	}

	public void release() {
        if (mPauseAdContext != null) {
            mPauseAdContext.release();
            mPauseAdContext = null;
        }
        mPauseAdCallback = null;
        mPlayerUiControl = null;
        mPlayerAdControl = null;
	}

    private IPauseAdCallback mPauseAdCallback = new PauseAdCallback();
    private class PauseAdCallback implements IPauseAdCallback {

        @Override
        public void onPauseAdClose() {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    containerView.setVisibility(View.GONE);
                }
            });
        }

        @Override
        public void onPauseAdPresent(final int request) {
            mActivity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (mediaPlayerDelegate != null
                            && mediaPlayerDelegate.isFullScreen
                            && !pauseADcanceled
                            && mRequest == request
                            && (mPlayerAdControl != null && !mPlayerAdControl.isMidAdShowing())) {
                        if (mPlayerUiControl != null) {
                            mPlayerUiControl.hideWebView();
                            mPlayerUiControl.hideInteractivePopWindow();
                        }
                        setVisible(true);
                        setVisibility(View.VISIBLE);
                    } else {
                        disposeAdLoss(URLContainer.AD_LOSS_STEP3);
                    }
                }
            });
        }

        @Override
        public void onPauseAdClicked() {

        }

        @Override
        public void onPauseAdFailed() {
            disposeAdLoss(URLContainer.AD_LOSS_STEP4);
        }

        @Override
        public void onPauseAdDismiss() {
            dismissPauseAD();
        }
    }

    private void removeAllAd() {
        if (mPauseAdContext != null) {
            mPauseAdContext.removeAllAd();
        }
        if (mPauseAdContainer != null) {
            mPauseAdContainer.removeAllViews();
        }
    }

}