package com.youku.player.ad.pausead;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

import com.baseproject.image.ImageLoaderManager;
import com.baseproject.utils.Logger;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.youdo.vo.XAdInstance;
import com.youku.android.player.R;
import com.youku.player.LogTag;
import com.youku.player.apiservice.IPlayerAdControl;
import com.youku.player.apiservice.IPlayerUiControl;
import com.youku.player.goplay.AdvInfo;
import com.youku.player.plugin.MediaPlayerDelegate;
import com.youku.player.util.DisposableStatsUtils;

/**
 * Offline pause ad
 */
public class PauseAdOffline extends PauseAd {

    private ImageLoader mImageLoader = null;
    private static Handler mHandler = new Handler();
    private String mADURL;
    private AdvInfo mAdvInfo;

    private ImageView mCloseBtn;
    private ImageView mAdImageView;
    private XAdInstance mOfflineAdInstance;

    public PauseAdOffline(Activity context, MediaPlayerDelegate mediaPlayerDelegate,
                          IPlayerUiControl playerUiControl, IPlayerAdControl playerAdControl, XAdInstance offlineAdInstance) {
        super(context, mediaPlayerDelegate, playerUiControl, playerAdControl);
        mAdView = mLayoutInflater.inflate(R.layout.yp_player_ad_pause_youku_container, null);
        mOfflineAdInstance = offlineAdInstance;
        findView();
    }

    @Override
    public void start(final AdvInfo advInfo, int request, IPauseAdCallback callback) {
        mPauseAdCallback = callback;
        mRequest = request;
        init(advInfo);
        initClickListener();
        Logger.d(LogTag.TAG_PLAYER, "start show offline pause ad");
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
            if (mADURL != null && !mADURL.equals("")) {
                if (!mADURL.startsWith("file://")) {
                    mADURL = "file://" + mADURL;
                }
                DisposableStatsUtils.disposeOfflinePausedSUS(mActivity.getApplicationContext(),
                        mAdvInfo, mOfflineAdInstance);
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
                            if (!mMediaPlayerDelegate.isFullScreen) {
                                return;
                            }
                            if (mPlayerAdControl != null && mPlayerAdControl.isMidAdShowing()) {
                                return;
                            }

                            showADImageWhenLoaded();
                            final Bitmap image = loadedImage;
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mAdImageView.setImageBitmap(image);
                                }
                            });
                        }

                    });
        }
    }

    private void showADImageWhenLoaded() {
        mAdImageView.setOnClickListener(null);
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
}
