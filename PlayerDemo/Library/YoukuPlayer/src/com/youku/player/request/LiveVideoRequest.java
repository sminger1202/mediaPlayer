package com.youku.player.request;

import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.baseproject.utils.Logger;
import com.baseproject.utils.Util;
import com.youku.player.LogTag;
import com.youku.player.config.MediaPlayerConfiguration;
import com.youku.player.goplay.GoplayException;
import com.youku.player.goplay.IGetAdvCallBack;
import com.youku.player.goplay.IVideoInfoCallBack;
import com.youku.player.goplay.MyGoplayManager;
import com.youku.player.module.PlayVideoInfo;
import com.youku.player.module.VideoUrlInfo;
import com.youku.player.plugin.MediaPlayerDelegate;
import com.youku.player.util.PlayerUtil;

/**
 * 直播请求
 */
public class LiveVideoRequest extends VideoRequest {

    public LiveVideoRequest(MediaPlayerDelegate mediaPlayerDelegate, FragmentActivity activity) {
        super(mediaPlayerDelegate, activity);
    }

    @Override
    public void requestVideo(final PlayRequest playRequest, PlayVideoInfo playVideoInfo, final IVideoInfoCallBack listener) {
        if (!MediaPlayerConfiguration.getInstance().livePortrait() || PlayerUtil.isYoukuTablet(mContext)) {
            mMediaPlayerDelegate.goFullScreen();
            mMediaPlayerDelegate.getPlayerUiControl().removeHandlerMessage();
            mMediaPlayerDelegate.getPlayerUiControl().setOrientionDisable();
        }

        mMediaPlayerDelegate.nowVid = playVideoInfo.vid;
        VideoUrlInfo videoInfo = new VideoUrlInfo();
        videoInfo.setVid(playVideoInfo.vid);
        videoInfo.setRequestId(playVideoInfo.vid);
        videoInfo.isHLS = true;
        mMediaPlayerDelegate.videoInfo = videoInfo;
        final MyGoplayManager myGoplayManager = new MyGoplayManager(mContext);
        myGoplayManager.playHls(playVideoInfo.vid, new IVideoInfoCallBack() {
            @Override
            public void onSuccess(VideoUrlInfo videoUrlInfo) {
                Logger.d(LogTag.TAG_PLAYER, "播放信息获取成功");
                videoUrlInfo.mVideoFetchTime = SystemClock.elapsedRealtime();
                if (playRequest.isCanceled()) {
                    Logger.d(LogTag.TAG_PLAYER,
                            "handleSuccessfullyGetVideoUrl  activity is finish, return");
                    listener.onSuccess(videoUrlInfo);
                    return;
                }
                mMediaPlayerDelegate.getPlayerUiControl().initDanmakuManager("", 0, false);
                if (mMediaPlayerDelegate.getPlayerUiControl() != null && mMediaPlayerDelegate.getPlayerUiControl().getDanmakuManager() != null) {
                    mMediaPlayerDelegate.getPlayerUiControl().getDanmakuManager().setDanmakuVisibleWhenLive();
                }
                mMediaPlayerDelegate.videoInfo = videoUrlInfo;
                mMediaPlayerDelegate.pluginManager.onVideoInfoGetted();
                if (videoUrlInfo.mLiveInfo != null
                        && videoUrlInfo.mLiveInfo.autoplay == 1 && videoUrlInfo.mLiveInfo.status == 1) {
                    listener.onSuccess(videoUrlInfo);
                    if (!mMediaPlayerDelegate.getPlayerUiControl().isOnPause()) {
                        mMediaPlayerDelegate.pluginManager.onLoading();
                        mMediaPlayerDelegate.isLoading = true;
                    }
                }
            }

            @Override
            public void onFailed(GoplayException e) {
                if (!playRequest.isCanceled()) {
                    if (Util.hasInternet())
                        Toast.makeText(mContext, e.getErrorInfo(),
                                Toast.LENGTH_SHORT).show();
                    mMediaPlayerDelegate.videoInfo = e.getVideoUrlInfo();
                    MediaPlayerConfiguration.getInstance().mPlantformController
                            .onGetHLSVideoInfoFailed(mActivity, e);
                    mMediaPlayerDelegate.pluginManager.onVideoInfoGetFail(true);
                }
                listener.onFailed(e);
            }
        });
    }

    @Override
    public void requestAdv(PlayRequest playRequest, PlayVideoInfo playVideoInfo, VideoUrlInfo videoUrlInfo, IGetAdvCallBack listener) {
        listener.onFailed(new GoplayException());
    }
}
