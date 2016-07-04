package com.youku.player.ad.pausead;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.baseproject.image.ImageLoaderManager;
import com.baseproject.utils.Logger;
import com.baseproject.utils.Util;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.youku.android.player.R;
import com.youku.player.LogTag;
import com.youku.player.apiservice.IPlayerAdControl;
import com.youku.player.apiservice.IPlayerUiControl;
import com.youku.player.base.Plantform;
import com.youku.player.goplay.AdvInfo;
import com.youku.player.goplay.Profile;
import com.youku.player.plugin.MediaPlayerDelegate;
import com.youku.player.ui.widget.YpYoukuDialog;
import com.youku.player.util.AdUtil;
import com.youku.player.util.AnalyticsWrapper;
import com.youku.player.util.DisposableStatsUtils;
import com.youku.player.util.URLContainer;

/**
 * youku pause ad
 */
public class PauseAdYouku extends PauseAd {

    private ImageLoader mImageLoader = null;
    private static Handler mHandler = new Handler();
    private String mADURL;
    private String mADClickURL;
    private AdvInfo mAdvInfo;

    private ImageView mCloseBtn;
    private ImageView mAdImageView;

    public PauseAdYouku(Activity context, MediaPlayerDelegate mediaPlayerDelegate,
                        IPlayerUiControl playerUiControl, IPlayerAdControl playerAdControl) {
        super(context, mediaPlayerDelegate, playerUiControl, playerAdControl);
        if (Profile.PLANTFORM == Plantform.YOUKU) {
            mAdView = mLayoutInflater.inflate(R.layout.yp_player_ad_pause_youku_container, null);
        } else {
            mAdView = mLayoutInflater.inflate(R.layout.yp_player_ad_pause_tudou_container, null);
        }

        findView();
    }

    @Override
    public void start(final AdvInfo advInfo, int request, IPauseAdCallback callback) {
        mPauseAdCallback = callback;
        mRequest = request;
        init(advInfo);
        initClickListener();
        Logger.d(LogTag.TAG_PLAYER, "start show youku pause ad");
        loadImage();
    }

    @Override
    public void release() {
        mActivity = null;
        mMediaPlayerDelegate = null;
        mImageLoader = null;
        mPlayerAdControl = null;
        mPlayerUiControl = null;
        mPauseAdCallback = null;
    }

    @Override
    public void removeAd() {
        mAdView.setVisibility(View.INVISIBLE);
    }

    private void init(final AdvInfo advInfo) {
        if (advInfo != null) {
            mAdvInfo = advInfo;
            mADURL = mAdvInfo.RS;
            mADClickURL = mAdvInfo.CU;
            if (mADURL != null && !mADURL.equals("")) {
                if (mMediaPlayerDelegate != null)
                    AnalyticsWrapper.adPlayStart(mActivity,
                            mMediaPlayerDelegate.videoInfo, mAdvInfo);
                DisposableStatsUtils.disposePausedSUS(
                        mActivity.getApplicationContext(),
                        mAdvInfo);
                DisposableStatsUtils.disposePausedVC(mAdvInfo);
            }

        }
    }

    private void findView() {
        mCloseBtn = (ImageView) mAdView
                .findViewById(R.id.btn_close_pausead);
        mAdImageView = (ImageView) mAdView
                .findViewById(R.id.plugin_pause_ad_image);
    }

    private void initClickListener() {
        mCloseBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mPauseAdCallback != null) {
                    mPauseAdCallback.onPauseAdClose();
                }
            }
        });
        mAdImageView.setOnClickListener(null);
    }

    private void loadImage() {
        Logger.d(LogTag.TAG_PLAYER, "pause ad loadImage start");
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
                                        disposeAdLoss(URLContainer.AD_LOSS_STEP4);
                                    }
                                }
                            }, 30000);
                        }

                        @Override
                        public void onLoadingComplete(String imageUri,
                                                      View view, Bitmap loadedImage) {
                            Logger.d(LogTag.TAG_PLAYER, "image onLoadingComplete");
                            if (loadedImage == null || isCanceled == true) {
                                return;
                            }
                            isLoaded = true;
                            if (mMediaPlayerDelegate != null && !mMediaPlayerDelegate.isFullScreen) {
                                disposeAdLoss(URLContainer.AD_LOSS_STEP3);
                                return;
                            }
                            if (mPlayerAdControl != null && mPlayerAdControl.isMidAdShowing()) {
                                disposeAdLoss(URLContainer.AD_LOSS_STEP3);
                                return;
                            }

                            showADImageWhenLoaded();
                            final Bitmap image = loadedImage;
                            if (mActivity != null) {
                                mActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mAdImageView.setImageBitmap(image);
                                    }
                                });
                            }
                        }

                    });
        }
    }

    private void showADImageWhenLoaded() {
        if (null != mADClickURL && TextUtils.getTrimmedLength(mADClickURL) > 0) {
            mAdImageView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Logger.d(LogTag.TAG_PLAYER, "点击:" + mADClickURL);
                    // 用户点击跳转发送CUM
                    DisposableStatsUtils.disposeCUM(
                            mActivity.getApplicationContext(), mAdvInfo);
                    if (!Util.isWifi() && AdUtil.isDownloadAPK(mAdvInfo, mADClickURL)
                            && MediaPlayerDelegate.mIDownloadApk != null) {
                        creatSelectDownloadDialog(mActivity, Util.isWifi());
                        return;
                    }

                    if (mPauseAdCallback != null) {
                        mPauseAdCallback.onPauseAdDismiss();
                    }
                    
                    if (mPlayerAdControl != null) {
                        mPlayerAdControl.onMoreInfoClicked(mADClickURL, mAdvInfo);
                    }
                }
            });
        } else {
            mAdImageView.setOnClickListener(null);
        }
        if (mPlayerUiControl != null) {
            mPlayerUiControl.hideWebView();
            mPlayerUiControl.hideInteractivePopWindow();
        }
        setVisible(true);
        if (mPauseAdCallback != null) {
            mPauseAdCallback.onPauseAdPresent(mRequest);
        }
    }

    private void setVisible(boolean visible) {
        mAdView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * 当在广告点击的url为app下载地址，让用户选择是否下载
     * FIXME 添加App下载提示相关代码
     */
    private void creatSelectDownloadDialog(Activity activity, boolean isWifi) {
        final YpYoukuDialog downLoadDialog = new YpYoukuDialog(activity);
        downLoadDialog.setNormalPositiveBtn(
                R.string.youku_ad_dialog_selectdownload_cancel,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        downLoadDialog.dismiss();
                    }
                });
        downLoadDialog.setNormalNegtiveBtn(
                R.string.youku_ad_dialog_selectdownload_ok,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!Util.hasInternet()) {
                            Toast.makeText(mActivity.getApplicationContext(), "当前无网络连接", Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            if (mPlayerAdControl != null) {
                                mPlayerAdControl.onMoreInfoClicked(mADClickURL, mAdvInfo);
                            }
                        }
                        if (mPauseAdCallback != null) {
                            mPauseAdCallback.onPauseAdDismiss();
                        }
                        downLoadDialog.dismiss();
                    }
                });
        downLoadDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                downLoadDialog.dismiss();
            }
        });
        downLoadDialog.setMessage(isWifi ? R.string.youku_ad_dialog_selectdownload_message_wifi : R.string.youku_ad_dialog_selectdownload_message_3g);
        downLoadDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
                    downLoadDialog.dismiss();
                }
                return true;
            }
        });
        downLoadDialog.setCanceledOnTouchOutside(false);
        if (activity.isFinishing()) {
            return;
        }
        downLoadDialog.show();
        mPlayerAdControl.onDownloadDialogShow(mAdvInfo);
    }
}
