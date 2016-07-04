package com.youku.player.plugin;

import android.app.Activity;
import android.os.Handler;
import android.text.Html;
import android.view.View;

import com.youku.player.apiservice.IPlayerAdControl;
import com.youku.player.apiservice.IPlayerUiControl;
import com.youku.player.config.MediaPlayerConfiguration;
import com.youku.player.goplay.Profile;
import com.youku.player.util.DetailUtil;

public class PluginChangeQuality extends PluginPayTip {
	private int mNextQuality;
	private boolean isClosed;
	private boolean isLoading;

    private IPlayerUiControl mPlayerUiControl;
    private IPlayerAdControl mPlayerAdControl;

	private static final int STATE_CHANGE_QUALITY_TIP = 1;
	private static final int STATE_SMOOTH_CHANGE_QUALITY = 2;
	private int mState;
	private String mSmoothTips = null;
	private static Handler handler = new Handler() {
	};

    public PluginChangeQuality(Activity context, MediaPlayerDelegate mediaPlayerDelegate,
                               IPlayerUiControl playerUiControl, IPlayerAdControl PlayerAdControl) {
        super(context, mediaPlayerDelegate);
        mPlayerUiControl = playerUiControl;
        mPlayerAdControl = PlayerAdControl;
		mState = STATE_CHANGE_QUALITY_TIP;
    }

	@Override
	public void onNotifyChangeVideoQuality() {
		if (!MediaPlayerConfiguration.getInstance().showChangeQualityTip()
				|| (mMediaPlayerDelegate != null && mMediaPlayerDelegate.isADShowing)
				|| (mMediaPlayerDelegate != null
						&& mMediaPlayerDelegate.videoInfo != null && (mMediaPlayerDelegate.videoInfo.isHLS || mMediaPlayerDelegate.videoInfo
						.isCached()))
				|| (mPlayerAdControl != null && mPlayerAdControl.isMidAdShowing()))
			return;
		showChangeQualityTip();
	}

	private boolean mIsStartToShowStartTip;
	public void showSmoothChangeQualityTip(boolean start) {
		if (mState == STATE_CHANGE_QUALITY_TIP && isLoading) {
			return;
		}
		final String tip = DetailUtil.getQualityChangeTips(mActivity, start, Profile.videoQuality);
		mIsStartToShowStartTip = start;
		if (!mActivity.isFinishing()) {
			mActivity.runOnUiThread(new Runnable() {
				public void run() {
					if (handler != null) {
						handler.removeCallbacksAndMessages(null);
					}
					isLoading = true;
					mSmoothTips = tip;
					if (mMediaPlayerDelegate != null
							&& mMediaPlayerDelegate.videoInfo != null
							&& mPlayerUiControl != null && mPlayerUiControl.canShowPluginChangeQuality()
							&& !isHide) {
						mState = STATE_SMOOTH_CHANGE_QUALITY;
						mArrowButton.setVisibility(View.GONE);
						mTipTextView.setText(tip);
						show();
					}
				}
			});
			if (!start && handler != null) {
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						if (mState == STATE_SMOOTH_CHANGE_QUALITY && isShowing() && !mIsStartToShowStartTip) {
							close(null);
							isClosed = false;
							isLoading = false;
						}
					}
				}, 1500);
			}
		}
	}

	private void showChangeQualityTip() {
		if (!mActivity.isFinishing())
			mActivity.runOnUiThread(new Runnable() {
				public void run() {
					isLoading = true;
					if (mMediaPlayerDelegate != null
							&& mMediaPlayerDelegate.videoInfo != null
							&& (mContainerView.getVisibility() != View.VISIBLE && mState != STATE_SMOOTH_CHANGE_QUALITY)
							&& mPlayerUiControl != null && mPlayerUiControl.canShowPluginChangeQuality()
							&& (!isClosed && mState != STATE_SMOOTH_CHANGE_QUALITY)
							&& !isHide) {
						mState = STATE_CHANGE_QUALITY_TIP;
						mArrowButton.setVisibility(View.GONE);
						int quality = mMediaPlayerDelegate.videoInfo
								.getCurrentQuality();
						if (quality == Profile.VIDEO_QUALITY_SD)
							return;
						String str = "";
						switch (quality) {
						case Profile.VIDEO_QUALITY_HD:
							mNextQuality = Profile.VIDEO_QUALITY_SD;
							str = "标清模式";
							break;
						case Profile.VIDEO_QUALITY_HD2:
							mNextQuality = Profile.VIDEO_QUALITY_HD;
							str = "高清模式";
							break;
						case Profile.VIDEO_QUALITY_HD3:
							mNextQuality = Profile.VIDEO_QUALITY_HD2;
							str = "超清模式";
							break;
						}
						mTipTextView.setText(Html
								.fromHtml("您当前的网络状况不佳<br>建议<font color=#15a4ff>点击切换</font>为"
										+ str));
						show();
					}
				}
			});
	}

	@Override
	protected void onTextAndArrowButtonClick() {
		if (mState == STATE_SMOOTH_CHANGE_QUALITY) {
			return;
		}
		super.onTextAndArrowButtonClick();
	}

	@Override
	protected void onOkClick() {
		if (mMediaPlayerDelegate != null && mState == STATE_CHANGE_QUALITY_TIP) {
			mMediaPlayerDelegate.pluginManager.onLoading();
			mMediaPlayerDelegate.changeVideoQuality(mNextQuality);
		}
	}

	@Override
	public void onRealVideoStart() {
		if (isShowing())
			close(null);
		isClosed = false;
		isLoading = false;
	}

	@Override
	public void onLoadedListener() {
		if (isShowing() && mState != STATE_SMOOTH_CHANGE_QUALITY) {
			close(null);
		}
		isClosed = false;
		isLoading = false;
	}

	@Override
	public boolean isShowing() {
		return mContainerView.getVisibility() == View.VISIBLE;
	}

	@Override
	protected void onCloseClick() {
		isClosed = true;
	}

	@Override
	public void onRelease() {
		super.onRelease();
		isClosed = false;
		isLoading = false;
	}

	@Override
	public boolean onErrorListener(int what, int extra) {
		isClosed = false;
		return super.onErrorListener(what, extra);
	}

	public void hide() {
		isHide = true;
		if (isShowing())
			close(null);
	}

	public void unHide() {
		isHide = false;
		if (isLoading) {
			if (mState == STATE_SMOOTH_CHANGE_QUALITY) {
				if (mSmoothTips != null) {
					showSmoothChangeQualityTip(mIsStartToShowStartTip);
				}
			} else {
				showChangeQualityTip();
			}
		}

	}
}
