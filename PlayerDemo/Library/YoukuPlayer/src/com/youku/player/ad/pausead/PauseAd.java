package com.youku.player.ad.pausead;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.youku.player.apiservice.IPlayerAdControl;
import com.youku.player.apiservice.IPlayerUiControl;
import com.youku.player.goplay.AdvInfo;
import com.youku.player.plugin.MediaPlayerDelegate;
import com.youku.player.util.DisposableStatsUtils;
import com.youku.player.util.SessionUnitil;
import com.youku.player.util.URLContainer;

/**
 * abstract pause ad.
 */
public abstract class PauseAd {

    protected Activity mActivity;
    protected MediaPlayerDelegate mMediaPlayerDelegate;
    protected IPlayerUiControl mPlayerUiControl;
    protected IPlayerAdControl mPlayerAdControl;
    protected LayoutInflater mLayoutInflater;
    protected View mAdView;

    protected IPauseAdCallback mPauseAdCallback;
    protected int mRequest;

    protected LayoutParams mContainerParams;

    public PauseAd(Activity context, MediaPlayerDelegate mediaPlayerDelegate,
                           IPlayerUiControl playerUiControl, IPlayerAdControl playerAdControl) {
        mActivity = context;
        mMediaPlayerDelegate = mediaPlayerDelegate;
        mPlayerUiControl = playerUiControl;
        mPlayerAdControl = playerAdControl;
        mLayoutInflater = LayoutInflater.from(context);
    }

    public abstract void start(final AdvInfo advInfo, int request, IPauseAdCallback callback);

    protected void disposeAdLoss(int step) {
        DisposableStatsUtils.disposeAdLoss(mActivity, step,
                SessionUnitil.playEvent_session, URLContainer.AD_LOSS_MP);
    }

    public abstract void release();

    public abstract void removeAd();

    public void setContainer(ViewGroup viewGroup) {
        if (viewGroup != null) {
            viewGroup.addView(mAdView, getParams());
        }
    }

    protected LayoutParams getParams() {
        if (mContainerParams == null) {
            mContainerParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        }
        return mContainerParams;
    }
}
