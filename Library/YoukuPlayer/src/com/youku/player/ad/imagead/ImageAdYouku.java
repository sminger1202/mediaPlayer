package com.youku.player.ad.imagead;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baseproject.image.ImageLoaderManager;
import com.baseproject.utils.Logger;
import com.baseproject.utils.Util;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.youku.android.player.R;
import com.youku.player.LogTag;
import com.youku.player.ad.AdForward;
import com.youku.player.apiservice.IPlayerAdControl;
import com.youku.player.apiservice.IPlayerUiControl;
import com.youku.player.base.Plantform;
import com.youku.player.goplay.AdvInfo;
import com.youku.player.goplay.Profile;
import com.youku.player.goplay.StaticsUtil;
import com.youku.player.plugin.MediaPlayerDelegate;
import com.youku.player.util.AdUtil;
import com.youku.player.util.DisposableStatsUtils;
import com.youku.player.util.URLContainer;

/**
 * youku Image ad.
 */
public class ImageAdYouku extends ImageAd{

    private ImageLoader mImageLoader = null;
    private View closeBtn;
    private ImageView adImageView;
    private String mADURL;
    private String mADClickURL;
    private boolean isOnClick = false;
    // timer
    private LinearLayout mTimerWrap;
    private TextView mTimerText;
    private AdCountDownTimer mTimer;

    public ImageAdYouku(Activity context, MediaPlayerDelegate mediaPlayerDelegate, IPlayerUiControl playerUiControl, IPlayerAdControl playerAdControl) {
        super(context, mediaPlayerDelegate, playerUiControl, playerAdControl);
        if (Profile.PLANTFORM == Plantform.YOUKU) {
            mAdView = mLayoutInflater.inflate(R.layout.yp_player_ad_image_youku_container, null);
        } else {
            mAdView = mLayoutInflater.inflate(R.layout.yp_player_ad_image_tudou_container, null);
        }

        findView();
    }

    private void findView() {
        closeBtn = (View) mAdView.findViewById(R.id.btn_close);
        adImageView = (ImageView) mAdView
                .findViewById(R.id.plugin_full_ad_image);
        closeBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mImageAdCallback != null) {
                    dismiss();
                    mImageAdCallback.onAdClose();
                }
            }
        });
        adImageView.setOnClickListener(null);
        mTimerWrap = (LinearLayout) mAdView
                .findViewById(R.id.image_ad_timer_wrap);
        mTimerText = (TextView) mAdView.findViewById(R.id.image_ad_count);

    }

    @Override
    public void start(AdvInfo advInfo, IImageAdCallback callback) {
        mAdvInfo = advInfo;
        mImageAdCallback = callback;
        mADURL = mAdvInfo.RS;
        mADClickURL = mAdvInfo.CU;
        mSavedCount = mAdvInfo.AL;
        mTimerText.setText(String.valueOf(mSavedCount));
        if (mSavedCount > 0) {
            mTimerWrap.setVisibility(View.VISIBLE);
        } else {
            mTimerWrap.setVisibility(View.GONE);
        }
        isOnClick = false;
        loadImage();
    }

    @Override
    public void release() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mDownLoadDialog != null) {
            if (mDownLoadDialog.isShowing()) {
                mDownLoadDialog.dismiss();
            }
            mDownLoadDialog = null;
        }
        mSavedCount = 0;
        mAdvInfo = null;
    }

    @Override
    public void dismiss() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if(mDownLoadDialog != null){
            if(mDownLoadDialog.isShowing()){
                mDownLoadDialog.dismiss();
            }
            mDownLoadDialog = null;
        }
        mSavedCount = 0;
    }

    private void loadImage() {
        if (mImageLoader == null) {
            mImageLoader = ImageLoaderManager.getInstance();
        }
        if (mImageLoader != null) {
            mImageLoader.loadImage(mADURL,
                    new SimpleImageLoadingListener() {
                        private boolean isLoaded = false;
                        private boolean isCanceled = false;

                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                            mHandler.postDelayed(new Runnable() {

                                @Override
                                public void run() {
                                    if (!isLoaded) {
                                        isCanceled = true;
                                        setImageAdShowing(false);
                                        if (mMediaPlayerDelegate != null
                                                && !mMediaPlayerDelegate.isPause) {
                                            if (StaticsUtil.PLAY_TYPE_LOCAL
                                                    .equals(mMediaPlayerDelegate.videoInfo
                                                            .getPlayType())
                                                    && mMediaPlayerDelegate.pluginManager != null
                                                    && mActivity != null) {
                                                if (mActivity != null)
                                                    mActivity
                                                            .runOnUiThread(new Runnable() {

                                                                @Override
                                                                public void run() {
                                                                    mMediaPlayerDelegate.pluginManager
                                                                            .onVideoInfoGetted();
                                                                    mMediaPlayerDelegate.pluginManager
                                                                            .onChangeVideo();
                                                                }
                                                            });
                                            }
                                            mMediaPlayerDelegate
                                                    .startPlayAfterImageAD();
                                        }
                                        disposeAdLoss(URLContainer.AD_LOSS_STEP4);
                                    }
                                }
                            }, TIME_OUT);
                        }

                        @Override
                        public void onLoadingComplete(String imageUri,
                                                      View view, Bitmap loadedImage) {
                            if (loadedImage == null || isCanceled) {
                                return;
                            }
                            if (mPlayerAdControl == null || !mPlayerAdControl.isImageAdStartToShow()) {
                                disposeAdLoss(URLContainer.AD_LOSS_STEP3);
                                return;
                            }
                            isLoaded = true;

                            Logger.d(LogTag.TAG_PLAYER, "全屏广告加载成功");
                            updateImageAdPlugin();
                            adImageView.setImageBitmap(loadedImage);
                            showADImageWhenLoaded();
                        }
                    });
        }
    }

    /**
     * 全屏广告获取成功 去显示
     */
    private void showADImageWhenLoaded() {
        if (null != mADClickURL && TextUtils.getTrimmedLength(mADClickURL) > 0) {
            adImageView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Logger.d(LogTag.TAG_PLAYER, "点击:" + mADClickURL);
                    // 用户点击跳转发送CUM
                    DisposableStatsUtils.disposeCUM(mActivity.getApplicationContext(), mAdvInfo);

                    if (AdUtil.isDownloadAPK(mAdvInfo, mADClickURL)
                            && MediaPlayerDelegate.mIDownloadApk != null
                            && mMediaPlayerDelegate != null) {
                        if(Util.isWifi()){
                            if (mPlayerAdControl != null) {
                                mPlayerAdControl.onMoreInfoClicked(mADClickURL, mAdvInfo);
                            }
                            if (mImageAdCallback != null) {
                                mImageAdCallback.onAdDismiss();
                            }
                            if (mMediaPlayerDelegate != null) {
                                mMediaPlayerDelegate.pluginManager.onLoading();
                                mMediaPlayerDelegate.startPlayAfterImageAD();
                            }
                        }else{
                            creatSelectDownloadDialog(mActivity, Util.isWifi(), mADClickURL);
                        }
                        return;
                    } else if (mMediaPlayerDelegate != null) {
                        isOnClick = true;
                        if (mImageAdCallback != null) {
                            mImageAdCallback.onAdDismiss();
                        }
                        if (mAdvInfo.CUF != AdForward.YOUKU_VIDEO) {
                            mMediaPlayerDelegate.pluginManager.onLoaded();
                        }
                        if (mPlayerAdControl != null) {
                            mPlayerAdControl.onMoreInfoClicked(mADClickURL, mAdvInfo);
                        }
                    }
                }
            });
        } else {
            adImageView.setOnClickListener(null);
        }
        if (StaticsUtil.PLAY_TYPE_LOCAL.equals(mMediaPlayerDelegate.videoInfo
                .getPlayType())
                && mMediaPlayerDelegate != null
                && mMediaPlayerDelegate.pluginManager != null) {
            mMediaPlayerDelegate.pluginManager.onVideoInfoGetted();
            mMediaPlayerDelegate.pluginManager.onChangeVideo();
        }
        if (mActivity.isFinishing()) {
            disposeAdLoss(URLContainer.AD_LOSS_STEP3);
            return;
        }

        if (mImageAdCallback != null) {
            mImageAdCallback.onAdPresent();
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startTimer();
            }
        }, 400);
    }

    @Override
    public void pauseTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    @Override
    public void startTimer() {
        if (mSavedCount > 0 && mPlayerAdControl.isImageAdShowing()) {
            mTimer = new AdCountDownTimer(mSavedCount * 1000, 100);
            mTimer.start();
        }
    }

    @Override
    public boolean isSaveOnResume() {
        return true;
    }

    @Override
    public boolean isAutoPlayAfterClick() {
        return isOnClick && isDownLoadDialogNotShowing();
    }

    @Override
    public void setAutoPlayAfterClick(boolean autoPlay) {
        isOnClick = autoPlay;
    }

    @Override
    public boolean isSaveOnOrientChange() {
        return true;
    }

    @Override
    public void onStop() {

    }


    private class AdCountDownTimer extends CountDownTimer {

        public AdCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            mSavedCount = (int)millisInFuture/1000;
        }

        @Override
        public void onFinish() {
            mImageAdCallback.onAdDismiss();
            if (mMediaPlayerDelegate != null) {
                mMediaPlayerDelegate.pluginManager.onLoading();
                mMediaPlayerDelegate.startPlayAfterImageAD();
            }
        }

        @Override
        public void onTick(long millisUntilFinished) {
            synchronized (mTimerText) {
                float time = (float) millisUntilFinished / 1000;
                int count = Math.round(time);
                if (mSavedCount != count && count > 0) {
                    mSavedCount = count;
                    mTimerText.setText(String.valueOf(mSavedCount));
                }
            }
        }

    }
}
