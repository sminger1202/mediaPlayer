package com.youku.player.request;

import android.support.v4.app.FragmentActivity;

import com.baseproject.utils.Logger;
import com.youdo.vo.XAdInstance;
import com.youku.player.LogTag;
import com.youku.player.ad.OfflineAdSDK;
import com.youku.player.goplay.GoplayException;
import com.youku.player.goplay.IGetAdvCallBack;
import com.youku.player.goplay.IGetOfflineAdvCallBack;
import com.youku.player.goplay.VideoAdvInfo;
import com.youku.player.module.PlayVideoInfo;
import com.youku.player.module.VideoUrlInfo;
import com.youku.player.plugin.MediaPlayerDelegate;
import com.youku.player.util.MainThreadExecutor;

import java.lang.ref.WeakReference;

/**
 * 离线广告请求
 */
public class OfflineAdvRequest implements AdvRequest {


    @Override
    public void requestAdv(final PlayRequest playRequest, final MediaPlayerDelegate mMediaPlayerDelegate, final WeakReference<FragmentActivity> mActivity, PlayVideoInfo playVideoInfo, VideoUrlInfo videoUrlInfo, final IGetAdvCallBack listener) {
        int delayedTime = 0;
        if (playVideoInfo.isLocalPlay())
            delayedTime = 600;
        new MainThreadExecutor().executeDelayed(new Runnable() {
            @Override
            public void run() {
                OfflineAdSDK.getPrerollAd(new IGetOfflineAdvCallBack() {
                    @Override
                    public void onSuccess(VideoAdvInfo videoAdvInfo, XAdInstance instance) {
                        Logger.d(LogTag.TAG_PLAYER, "MediaPlayerDelegate -> getOfflineADV  success!!!!");
                        mMediaPlayerDelegate.setXAdInstance(instance);
                        Logger.d(LogTag.TAG_PLAYER, "获取离线广告信息 getPrerollAd成功");
                        listener.onSuccess(videoAdvInfo);
                    }

                    @Override
                    public void onFailed(GoplayException e) {
                        Logger.d(LogTag.TAG_PLAYER, "获取离线广告信息 getPrerollAd失败");
                        if (!playRequest.isCanceled())
                            mMediaPlayerDelegate.getPlayerAdControl().onAdvInfoGetted(false);
                        listener.onFailed(e);
                    }
                });
            }
        }, delayedTime);// 延迟获取离线广告，是为了防止在转屏的时候触发PluginADPlay.onBaseResume，出现暂停画面
    }
}
