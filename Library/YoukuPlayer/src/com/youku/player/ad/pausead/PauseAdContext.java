package com.youku.player.ad.pausead;

import android.app.Activity;
import android.view.ViewGroup;

import com.youdo.vo.XAdInstance;
import com.youku.player.ad.AdVender;
import com.youku.player.apiservice.IPlayerAdControl;
import com.youku.player.apiservice.IPlayerUiControl;
import com.youku.player.goplay.AdvInfo;
import com.youku.player.plugin.MediaPlayerDelegate;

/**
 * pause ad context, manage pause ad.
 */
public class PauseAdContext {
    private PauseAd mYouku = null;
    private PauseAd mOffline = null;
    private PauseAd mCurrentAd = null;

    private Activity mActivity;
    private MediaPlayerDelegate mMediaPlayerDelegate;
    private IPlayerUiControl mPlayerUiControl;
    private IPlayerAdControl mPlayerAdControl;
    private XAdInstance mOfflineAdInstance;

    public PauseAdContext(Activity context, MediaPlayerDelegate mediaPlayerDelegate,
                          IPlayerUiControl playerUiControl, IPlayerAdControl playerAdControl) {
        mActivity = context;
        mMediaPlayerDelegate = mediaPlayerDelegate;
        mPlayerUiControl = playerUiControl;
        mPlayerAdControl = playerAdControl;
    }

    public void setOfflineAdInstance(XAdInstance offlineAdInstance){
        this.mOfflineAdInstance = offlineAdInstance;
    }

    /**
     * set pause ad type, call this before show()
     */
    public void setType(int type) {
        switch (type) {
            case AdVender.YOUKU:
                if (mYouku == null) {
                    mYouku = new PauseAdYouku(mActivity, mMediaPlayerDelegate, mPlayerUiControl, mPlayerAdControl);
                }
                mCurrentAd = mYouku;
                break;
            case AdVender.OFFLINE_AD:
                if (mOffline == null) {
                    mOffline = new PauseAdOffline(mActivity, mMediaPlayerDelegate, mPlayerUiControl, mPlayerAdControl, mOfflineAdInstance);
                }
                mCurrentAd = mOffline;
                break;
        }
    }

    public void setContainer(ViewGroup viewGroup) {
        if (viewGroup != null) {
            mCurrentAd.setContainer(viewGroup);
        }
    }

    /**
     * show ad, call setType before this function
     */
    public void show(AdvInfo advInfo, int request, IPauseAdCallback callback) {
        if (mCurrentAd != null) {
            removeAllAd();
            mCurrentAd.start(advInfo, request, callback);
        }
    }

    public void removeAllAd() {
        if (mYouku != null) {
            mYouku.removeAd();
        }
        if (mOffline != null) {
            mOffline.removeAd();
        }
    }

    public void release() {
        if (mYouku != null) {
            mYouku.release();
            mYouku = null;
        }
        if (mOffline != null) {
            mOffline.release();
            mOffline = null;
        }
        mActivity = null;
        mMediaPlayerDelegate = null;
        mPlayerUiControl = null;
        mPlayerAdControl = null;
        mCurrentAd = null;
    }
}
