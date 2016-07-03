package com.youku.player.ad.imagead;

import android.app.Activity;
import android.view.ViewGroup;

import com.youku.player.ad.AdVender;
import com.youku.player.apiservice.IPlayerAdControl;
import com.youku.player.apiservice.IPlayerUiControl;
import com.youku.player.goplay.AdvInfo;
import com.youku.player.plugin.MediaPlayerDelegate;

/**
 * image ad context.
 */
public class ImageAdContext {
    private ImageAd mYouku = null;
    private ImageAd mYoukuHtml5 = null;
    private ImageAd mCurrentAd = null;

    private Activity mActivity;
    private MediaPlayerDelegate mMediaPlayerDelegate;
    private IPlayerUiControl mPlayerUiControl;
    private IPlayerAdControl mPlayerAdControl;

    public ImageAdContext(Activity context, MediaPlayerDelegate mediaPlayerDelegate,
                          IPlayerUiControl playerUiControl, IPlayerAdControl playerAdControl) {
        mActivity = context;
        mMediaPlayerDelegate = mediaPlayerDelegate;
        mPlayerUiControl = playerUiControl;
        mPlayerAdControl = playerAdControl;
    }

    /**
     * set pause ad type, call this before show()
     */
    public void setType(int type) {
        switch (type) {
            case AdVender.YOUKU:
                if (mYouku == null) {
                    mYouku = new ImageAdYouku(mActivity, mMediaPlayerDelegate, mPlayerUiControl, mPlayerAdControl);
                }
                mCurrentAd = mYouku;
                break;
            case AdVender.YOUKU_HTML:
                if (mYoukuHtml5 == null) {
                    mYoukuHtml5 = new ImageAdYoukuHtml5(mActivity, mMediaPlayerDelegate, mPlayerUiControl, mPlayerAdControl);
                }
                mCurrentAd = mYoukuHtml5;
                break;
        }
    }

    public void setContainer(ViewGroup viewGroup) {
        if (viewGroup != null) {
            mCurrentAd.setContainer(viewGroup);
        }
    }

    public void onStop() {
        if (mCurrentAd != null) {
            mCurrentAd.onStop();
        }
    }

    /**
     * show ad, call setType before this function
     */
    public void show(AdvInfo advInfo, int request, IImageAdCallback callback) {
        if (mCurrentAd != null) {
            mCurrentAd.start(advInfo, callback);
        }
    }

    public void release() {
        if (mYouku != null) {
            mYouku.release();
            mYouku = null;
        }
        if (mYoukuHtml5 != null) {
            mYoukuHtml5.release();
            mYoukuHtml5 = null;
        }
    }

    public boolean isAutoPlayAfterClick() {
        if (mCurrentAd != null) {
            return mCurrentAd.isAutoPlayAfterClick();
        }
        return true;
    }

    public void setAutoPlayAfterClick(boolean autoPlay) {
        if (mCurrentAd != null) {
            mCurrentAd.setAutoPlayAfterClick(autoPlay);
        }
    }

    public boolean isSaveOnResume() {
        if (mCurrentAd != null) {
            return mCurrentAd.isSaveOnResume();
        }
        return true;
    }

    public boolean isSaveOnOrientChange() {
        if (mCurrentAd != null) {
            return mCurrentAd.isSaveOnOrientChange();
        }
        return false;
    }

    public void dismiss() {
        if (mCurrentAd != null) {
            mCurrentAd.dismiss();
        }
    }

    public void startTimer() {
        if (mCurrentAd != null) {
            mCurrentAd.startTimer();
        }
    }

    public void pauseTimer() {
        if (mCurrentAd != null) {
            mCurrentAd.pauseTimer();
        }
    }

    public void onResume() {
        if (mCurrentAd != null) {
            mCurrentAd.onResume();
        }
    }
}
