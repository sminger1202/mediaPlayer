package com.youku.player.plugin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.baseproject.utils.Logger;
import com.baseproject.utils.UIUtils;
import com.baseproject.utils.Util;
import com.youku.android.player.R;
import com.youku.player.LogTag;
import com.youku.player.Track;
import com.youku.player.ad.AdType;
import com.youku.player.ad.AdVender;
import com.youku.player.ad.imagead.IImageAdCallback;
import com.youku.player.ad.imagead.ImageAdContext;
import com.youku.player.apiservice.ILifeCycle;
import com.youku.player.apiservice.IPlayerAdControl;
import com.youku.player.apiservice.IPlayerUiControl;
import com.youku.player.goplay.AdvInfo;
import com.youku.player.goplay.GoplayException;
import com.youku.player.goplay.Profile;
import com.youku.player.goplay.StaticsUtil;
import com.youku.player.goplay.VideoAdvInfo;
import com.youku.player.module.VideoUrlInfo;
import com.youku.player.module.VideoUrlInfo.Source;
import com.youku.player.util.AdUtil;
import com.youku.player.util.DetailMessage;
import com.youku.player.util.DisposableStatsUtils;
import com.youku.player.util.SessionUnitil;
import com.youku.player.util.URLContainer;

@SuppressLint("NewApi")
public class PluginImageAD extends PluginOverlay implements DetailMessage, ILifeCycle {

	LayoutInflater mLayoutInflater;
	View containerView;
	private Activity mActivity;
	private MediaPlayerDelegate mediaPlayerDelegate;
    private IPlayerUiControl mPlayerUiControl;
    private IPlayerAdControl mPlayerAdControl;
    private FrameLayout mAdContainer;
    private ImageAdContext mAdContext;

	private int mAdType = AdVender.YOUKU;

	public boolean isOnClick = false;
	AdvInfo mAdvInfo = null;

	private boolean mIsStartToShow = false;

	public PluginImageAD(Activity context, MediaPlayerDelegate delegate,
                         IPlayerUiControl playerUiControl, IPlayerAdControl playerAdControl) {
		super(context, delegate);
		this.mediaPlayerDelegate = delegate;
		mActivity = context;
		mLayoutInflater = LayoutInflater.from(context);
        mPlayerUiControl = playerUiControl;
        mPlayerAdControl = playerAdControl;
        mAdContext = new ImageAdContext(context, delegate, playerUiControl, playerAdControl);
		init();
	}

	private void init() {
		containerView = mLayoutInflater.inflate(R.layout.yp_plugin_image_ad, null);
		addView(containerView);
		findView();
	}

	public void findView() {
        mAdContainer = (FrameLayout)containerView.findViewById(R.id.play_middle);
	}

	private String TAG = "PluginImageAD";
	protected VideoAdvInfo mVideoAdvInfo = null;

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
		mActivity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				dismissImageAD();
			}
		});
	}

	@Override
	public void onLoadedListener() {
	}

	@Override
	public void onLoadingListener() {
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
			containerView.setVisibility(View.INVISIBLE);
			setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onPluginAdded() {
		super.onPluginAdded();
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

	private boolean isVideoNoAdv() {

		VideoUrlInfo videoInfo = mMediaPlayerDelegate.videoInfo;
		boolean notFromYouku = videoInfo.mSource != Source.YOUKU;

		if (notFromYouku) {
			Logger.d(LogTag.TAG_PLAYER, "PluginImageAD->isVideoNoAdv = true");
			return true;
		}

		boolean isWifi = Util.isWifi();
		boolean isLocalVideo = videoInfo.playType
				.equals(StaticsUtil.PLAY_TYPE_LOCAL);

		if (!isWifi && isLocalVideo) {
			Logger.d(LogTag.TAG_PLAYER, "PluginImageAD->isVideoNoAdv = true");
			return true;
		}

		Logger.d(LogTag.TAG_PLAYER, "PluginImageAD->isVideoNoAdv = false");
		return false;
	}

	/**
	 * 开始获取广告
	 */
	public void showAD(VideoAdvInfo videoAdvInfo) {
		if (!firstLoaded) {
			// return;
		}

		isOnClick = false;
		mVideoAdvInfo = videoAdvInfo;

		if (isVideoNoAdv() || Profile.from == Profile.PHONE_BROWSER) {
			return;
		}

		if (mMediaPlayerDelegate != null && mVideoAdvInfo != null) {
			int size = mVideoAdvInfo.VAL.size();
			if (size == 0) {
				// mADURL = "";
				Logger.d(LogTag.TAG_PLAYER, "全屏广告VC:为空");
				dismissImageAD();
				if (mediaPlayerDelegate != null) {
					mediaPlayerDelegate.startPlayAfterImageAD();
				}
				return;
			}

            if (!AdUtil.isAdvVideoType(mVideoAdvInfo)) {
                int i = AdUtil.getAdvImageTypePosition(mVideoAdvInfo);
                mAdvInfo = mVideoAdvInfo.VAL.get(i);
                if (mAdvInfo.SDKID == AdVender.YOUKU && mAdvInfo.RS != null
                        && !mAdvInfo.RS.equals("")) {
                    if (mAdvInfo.RST != null
                            && mAdvInfo.RST.equalsIgnoreCase("html")) {
                        setAdType(AdVender.YOUKU_HTML);
                    } else {
                        setAdType(AdVender.YOUKU);
                    }

                    showADImage();
                    return;
                }
                setAdType(mAdvInfo.SDKID);
                showADImage();
                return;
            }
		}
		dismissImageAD();
		if (mediaPlayerDelegate != null) {
			mediaPlayerDelegate.startPlayAfterImageAD();
		}
	}

	/**
	 * 获取广告信息去加载
	 */
	protected void showADImage() {
		AdvInfo advInfo = null;
		try {
			// 显示广告的时候是使用
			advInfo = getAdvInfo();
			if (advInfo == null) {
				Logger.d(LogTag.TAG_PLAYER, "全屏广告显示 SUS:为空");
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.e(LogTag.TAG_PLAYER, "全屏广告显示 SUS为空");
		} finally {
			if (advInfo != null) {
				mIsStartToShow = true;
                mAdContext.setType(mAdType);
                mAdContext.setContainer(mAdContainer);
                mAdContext.show(advInfo, 1, mImageAdCallback);
			}
		}

	}

	/**
	 * 获取广告信息
	 *
	 * @return
	 */
    private AdvInfo getAdvInfo() {
        return mAdvInfo;
    }

	/**
	 * 不显示全屏广告蒙层
	 */
	public void dismissImageAD() {
		if (containerView.getVisibility() == View.VISIBLE) {
            if (mPlayerUiControl != null) {
                mPlayerUiControl.updatePlugin(PLUGIN_SHOW_NOT_SET);
            }
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					containerView.setVisibility(View.INVISIBLE);
				}
			});
			Track.onImageAdEnd();
		}

        if (mAdContext != null) {
            mAdContext.dismiss();
        }
		setImageAdShowing(false);
		mIsStartToShow = false;
		mAdvInfo = null;
	}

	public void onStop() {
		if (mAdContext != null) {
            mAdContext.onStop();
		}
	}

	public void release() {
		if (mPlayerAdControl.isImageAdShowing()) {
			Track.onImageAdEnd();
		}

        if (mAdContext != null) {
            mAdContext.release();
            mAdContext = null;
        }
		setImageAdShowing(false);
		mIsStartToShow = false;
		mAdvInfo = null;
	}

    private void updateImageAdPlugin() {
        if (mPlayerUiControl != null) {
            mPlayerUiControl.updatePlugin(PLUGIN_SHOW_IMAGE_AD);
        }
    }

    private void setPluginHolderPaddingZero() {
        if (mPlayerUiControl != null && UIUtils.hasKitKat()) {
            mPlayerUiControl.setPluginHolderPaddingZero();
        }
    }

    private void setImageAdShowing(boolean isShow) {
        if (mPlayerAdControl != null) {
            mPlayerAdControl.setImageAdShowing(isShow);
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
		//pauseTimer();
        if (mAdContext != null) {
            mAdContext.pauseTimer();
        }
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

	public void setAdType(int adType) {
		mAdType = adType;
	}

	public int getAdType() {
		return mAdType;
	}

	public boolean isSaveOnOrientChange() {
        return mAdContext != null && mAdContext.isSaveOnOrientChange();
    }

	public boolean isStartToShow() {
		return mIsStartToShow;
	}

	private void disposeAdLoss(int step) {
		DisposableStatsUtils.disposeAdLoss(mActivity, step,
				SessionUnitil.playEvent_session, URLContainer.AD_LOSS_MF);
	}

	@Override
	public void onBaseResume() {
		if (mActivity == null || mediaPlayerDelegate == null) {
			return;
		}
        if (mPlayerAdControl.isImageAdShowing()) {
            if (mAdContext.isSaveOnResume()) {
                updateImageAdPlugin();
                setVisible(true);
                setVisibility(View.VISIBLE);
                mAdContext.onResume();
                return;
            } else {
                dismissImageAD();
                mediaPlayerDelegate.pluginManager.onLoading();
                mediaPlayerDelegate.startPlayAfterImageAD();
                return;
            }
        }
        if (!mediaPlayerDelegate.isComplete
                && mediaPlayerDelegate.mAdType == AdType.AD_TYPE_IMAGE
                && mAdContext.isAutoPlayAfterClick()) {
            mAdContext.setAutoPlayAfterClick(false);
            mediaPlayerDelegate.pluginManager.onLoading();
            mediaPlayerDelegate.startPlayAfterImageAD();
        }
	}

	@Override
	public void onBaseConfigurationChanged() {
        if (mActivity == null || mediaPlayerDelegate == null) {
            return;
        }
        if (mediaPlayerDelegate.mAdType == AdType.AD_TYPE_IMAGE) {
            if (mPlayerAdControl.isImageAdShowing() && !mAdContext.isAutoPlayAfterClick()
                    && isSaveOnOrientChange()) {
                updateImageAdPlugin();
                setVisible(true);
                setVisibility(View.VISIBLE);
            } else if (isStartToShow() && !isSaveOnOrientChange()
                    && !(mPlayerUiControl != null && mPlayerUiControl.isOnPause())) {
                    dismissImageAD();
                    mMediaPlayerDelegate.pluginManager.onLoading();
                    mediaPlayerDelegate.startPlayAfterImageAD();
            }
        }
    }

    private ImageAdCallback mImageAdCallback = new ImageAdCallback();
    private class ImageAdCallback implements IImageAdCallback {

        @Override
        public void onAdClose() {
            if (mPlayerAdControl.isImageAdShowing()) {
                setImageAdShowing(false);
                mIsStartToShow = false;
                Track.onImageAdEnd();
                if (mediaPlayerDelegate != null) {
                    mediaPlayerDelegate.pluginManager.onLoading();
                    mediaPlayerDelegate.startPlayAfterImageAD();
                }
            }
        }

        @Override
        public void onAdPresent() {
            updateImageAdPlugin(); //adapter to nexus10
            setPluginHolderPaddingZero();
            setImageAdShowing(true);
            Track.onImageAdStart(mActivity, mMediaPlayerDelegate);
            setVisible(true);
            setVisibility(View.VISIBLE);
        }

        @Override
        public void onAdClicked() {

        }

        @Override
        public void onAdFailed() {
            setImageAdShowing(false);
            mIsStartToShow = false;
            disposeAdLoss(URLContainer.AD_LOSS_STEP4);
            if (mediaPlayerDelegate != null) {
                mediaPlayerDelegate.startPlayAfterImageAD();
            }
        }

        @Override
        public void onAdDismiss() {
            dismissImageAD();
        }
    }
}