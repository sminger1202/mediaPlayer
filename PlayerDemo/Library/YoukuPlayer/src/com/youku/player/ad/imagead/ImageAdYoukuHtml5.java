package com.youku.player.ad.imagead;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baseproject.utils.Logger;
import com.baseproject.utils.Util;
import com.youku.android.player.R;
import com.youku.player.LogTag;
import com.youku.player.apiservice.IPlayerAdControl;
import com.youku.player.apiservice.IPlayerUiControl;
import com.youku.player.goplay.AdvInfo;
import com.youku.player.plugin.MediaPlayerDelegate;
import com.youku.player.util.AdUtil;
import com.youku.player.util.DisposableStatsUtils;
import com.youku.player.util.URLContainer;

/**
 * html5 image ad.
 */
public class ImageAdYoukuHtml5 extends ImageAd {

    private LinearLayout mWebContainer;
    private WebView mAdWeb = null;
    private ImageAdWebViewClient mWebViewClient = null;
    private View closeBtn;
    private AdvInfo mAdvInfo;
    private String mADURL;
    private boolean isOnClick = false;
    // timer
    private LinearLayout mTimerWrap;
    private TextView mTimerText;
    private AdCountDownTimer mTimer;

    public ImageAdYoukuHtml5(Activity context, MediaPlayerDelegate mediaPlayerDelegate, IPlayerUiControl playerUiControl, IPlayerAdControl playerAdControl) {
        super(context, mediaPlayerDelegate, playerUiControl, playerAdControl);
        mAdView = mLayoutInflater.inflate(R.layout.yp_player_ad_image_youku_html5_container, null);
        findView();
    }

    private void findView() {
        mWebContainer = (LinearLayout) mAdView
                .findViewById(R.id.plugin_full_ad_webview);
        closeBtn = (View) mAdView.findViewById(R.id.btn_close);
        closeBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mImageAdCallback != null) {
                    dismiss();
                    mImageAdCallback.onAdClose();
                }
            }
        });
        mTimerWrap = (LinearLayout) mAdView
                .findViewById(R.id.image_ad_timer_wrap);
        mTimerText = (TextView) mAdView.findViewById(R.id.image_ad_count);

    }

    @Override
    public void start(AdvInfo advInfo, IImageAdCallback callback) {
        mAdvInfo = advInfo;
        mImageAdCallback = callback;
        mADURL = mAdvInfo.RS;
        //test
        //mADURL = "http://www.baidu.com";
        mSavedCount = mAdvInfo.AL;
        mTimerText.setText(String.valueOf(mSavedCount));
        if (mSavedCount > 0) {
            mTimerWrap.setVisibility(View.VISIBLE);
        } else {
            mTimerWrap.setVisibility(View.GONE);
        }
        isOnClick = false;
        startYoukuHtml5Ad();
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
        if (mAdWeb != null) {
            mWebContainer.removeAllViews();
            mAdWeb.destroy();
            mAdWeb = null;
        }
        mSavedCount = 0;
        mWebViewClient = null;
        mAdvInfo = null;
    }

    @Override
    public void dismiss() {
        if (mAdWeb != null) {
            mWebContainer.removeAllViews();
            mAdWeb.destroy();
            mAdWeb = null;
        }
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
        mWebViewClient = null;
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
            mSavedCount = (int) millisInFuture / 1000;
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

    // youku html5 ad
    private void startYoukuHtml5Ad() {
        if (mAdWeb != null) {
            mWebContainer.removeAllViews();
            mAdWeb.destroy();
        }
        if (mActivity == null || mActivity.isFinishing()) {
            disposeAdLoss(URLContainer.AD_LOSS_STEP3);
            return;
        }
        mAdWeb = new WebView(mActivity);
        mAdWeb.getSettings().setJavaScriptEnabled(true);
        mAdWeb.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        //mAdWeb.getSettings().setUseWideViewPort(true);
        mAdWeb.getSettings().setLoadWithOverviewMode(true);
        if (mWebViewClient == null) {
            mWebViewClient = new ImageAdWebViewClient();
        }
        mWebViewClient.isGetFeedBack = false;
        mAdWeb.setWebViewClient(mWebViewClient);
        mAdWeb.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mTimer != null) {
                    mTimer.cancel();
                    mTimer = null;
                    mSavedCount = 0;
                    mTimerWrap.setVisibility(View.GONE);
                }
                return false;
            }
        });
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        mAdWeb.setLayoutParams(params);
        mWebContainer.addView(mAdWeb);
        try {
            mAdWeb.loadUrl(mADURL);
        } catch (Exception e) {

        }

        Logger.d(LogTag.TAG_PLAYER, "start to show youku html5 ad");
        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (mPlayerAdControl.isImageAdStartToShow() && mWebViewClient != null
                        && !mWebViewClient.isGetFeedBack) {
                    if (mImageAdCallback != null) {
                        mImageAdCallback.onAdDismiss();
                    }
                    if (mMediaPlayerDelegate != null
                            && !mMediaPlayerDelegate.isPause) {
                        mMediaPlayerDelegate.startPlayAfterImageAD();
                    }
                }
            }
        }, TIME_OUT);
    }

    private class ImageAdWebViewClient extends WebViewClient {
        private boolean isGetFeedBack = false;

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (isGetFeedBack) {
                return;
            }
            mActivity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (mPlayerAdControl != null && mPlayerAdControl.isImageAdStartToShow()) {
                        if (mImageAdCallback != null) {
                            mImageAdCallback.onAdPresent();
                        }
                        isGetFeedBack = true;
                        startTimer();
                    } else {
                        disposeAdLoss(URLContainer.AD_LOSS_STEP3);
                    }
                }
            });
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            if (mImageAdCallback != null) {
                mImageAdCallback.onAdFailed();
            }
            ;
            isGetFeedBack = true;
            super.onReceivedError(view, errorCode, description, failingUrl);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Logger.d(LogTag.TAG_PLAYER, "点击:" + url);
            // 用户点击跳转发送CUM
            if (!isOnClick) {
                DisposableStatsUtils.disposeCUM(mActivity.getApplicationContext(), mAdvInfo);
            }

            if (AdUtil.isDownloadAPK(mAdvInfo, url)
                    && MediaPlayerDelegate.mIDownloadApk != null
                    && mMediaPlayerDelegate != null) {
                creatSelectDownloadDialog(mActivity, Util.isWifi(), url); //FIXME 添加App下载提示相关代码
            } else if (mMediaPlayerDelegate != null) {
                isOnClick = true;
                if (mImageAdCallback != null) {
                    mImageAdCallback.onAdDismiss();
                }
                mMediaPlayerDelegate.pluginManager.onLoaded();
                if (mPlayerAdControl != null) {
                    mPlayerAdControl.onMoreInfoClicked(url, mAdvInfo);
                }
            }
            return true;
        }
    }

}
