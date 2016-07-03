package com.youku.player.request;


import android.support.v4.app.FragmentActivity;

import com.baseproject.utils.Logger;
import com.baseproject.utils.Util;
import com.youku.player.LogTag;
import com.youku.player.Track;
import com.youku.player.config.MediaPlayerConfiguration;
import com.youku.player.goplay.GoplayException;
import com.youku.player.goplay.IGetAdvCallBack;
import com.youku.player.goplay.IVideoInfoCallBack;
import com.youku.player.goplay.StaticsUtil;
import com.youku.player.module.PlayVideoInfo;
import com.youku.player.module.VideoCacheInfo;
import com.youku.player.module.VideoHistoryInfo;
import com.youku.player.module.VideoUrlInfo;
import com.youku.player.plugin.MediaPlayerDelegate;
import com.youku.player.util.MainThreadExecutor;
import com.youku.player.util.PlayerUtil;

/**
 * 下载视频请求
 */
public class LocalDownloadVideoRequest extends VideoRequest {

    public LocalDownloadVideoRequest(MediaPlayerDelegate mediaPlayerDelegate, FragmentActivity activity) {
        super(mediaPlayerDelegate, activity);
    }

    @Override
    public void requestVideo(PlayRequest playRequest, PlayVideoInfo playVideoInfo, IVideoInfoCallBack listener) {
        Track.playRequest(mContext, playVideoInfo.vid, StaticsUtil.PLAY_TYPE_LOCAL, PlayerUtil.isLogin());
        final VideoUrlInfo videoInfo = new VideoUrlInfo();
        videoInfo.setVid(playVideoInfo.vid);
        videoInfo.cachePath = playVideoInfo.getUrl();
        videoInfo.playType = StaticsUtil.PLAY_TYPE_LOCAL;
        videoInfo.setCached(true);
        videoInfo.isLocalWaterMark = playVideoInfo.waterMark();
        mMediaPlayerDelegate.getPlayerUiControl().initDanmakuManager("", 0, true);
        for (int i = 0; i < 5; i++) {
            videoInfo.waterMarkType[i] = playVideoInfo.getWaterMarkType();
        }
        boolean resetProgress = false;
        if (MediaPlayerDelegate.mICacheInfo != null) {
            VideoCacheInfo videoCacheInfo = MediaPlayerDelegate.mICacheInfo.getDownloadInfo(playVideoInfo.vid);
            if (videoCacheInfo != null) {
                videoInfo.setTitle(videoCacheInfo.title);
                videoInfo.setProgress(videoCacheInfo.playTime * 1000);
                videoInfo.setShowId(videoCacheInfo.showid);
                videoInfo.setDurationMills(videoCacheInfo.seconds * 1000);
                videoInfo.nextVideoId = videoCacheInfo.nextVid;
                videoInfo.setShow_videoseq(videoCacheInfo.show_videoseq);
                videoInfo.setVideoLanguage(videoCacheInfo.language);
                videoInfo.setCurrentVideoQuality(videoCacheInfo.quality);
                videoInfo.setItem_img_16_9(videoCacheInfo.picUrl);
                videoInfo.savePath = videoCacheInfo.savePath;
                videoInfo.setimgurl(videoCacheInfo.picUrl);
                videoInfo.setEpisodemode(videoCacheInfo.episodemode);
                videoInfo.setMediaType(videoCacheInfo.mMediaType);
                videoInfo.setVerticalVideo(videoCacheInfo.isVerticalVideo);
                videoInfo.setExclusiveLogo(videoCacheInfo.exclusiveLogo);
                videoInfo.setAdPoints(videoCacheInfo.getAdPoints());
                videoInfo.need_mark = videoCacheInfo.mNeedWaterMark;
                if (playVideoInfo.point / 1000 > videoCacheInfo.seconds - 60) {
                    resetProgress = true;
                    playVideoInfo.point = 0;
                }

                if(videoCacheInfo.playTime * 1000 > videoInfo.getDurationMills() - 60 * 1000) {
                    videoInfo.setProgress(0);
                }
            }
        }
        // progress为0，读取历史,
        if (playVideoInfo.point <= 0 && MediaPlayerDelegate.mIVideoHistoryInfo != null && !resetProgress) {
            VideoHistoryInfo history = MediaPlayerDelegate.mIVideoHistoryInfo
                    .getVideoHistoryInfo(playVideoInfo.vid);
            if (history != null && history.playTime > 1) {
                playVideoInfo.point = history.playTime * 1000;
            }
            if (playVideoInfo.point > videoInfo.getDurationMills() - 60 * 1000) {
                playVideoInfo.point = 0;
            }
        }



        mMediaPlayerDelegate.videoInfo = videoInfo;
        mMediaPlayerDelegate.pluginManager.onVideoInfoGetted();
        if (videoInfo.getAdPoints() != null
                && !videoInfo.getAdPoints().isEmpty()) {
            mMediaPlayerDelegate.getPlayerAdControl().setMidADInfo(videoInfo.getAdPoints(), null);
        }

        if (videoInfo.isVerticalVideo() && !PlayerUtil.isYoukuTablet(mContext))
            mMediaPlayerDelegate.getPlayerUiControl().goVerticalFullScreen();
        mMediaPlayerDelegate.getPlayerUiControl().setOrientionDisable();
        mMediaPlayerDelegate.prepareSubtitle(playVideoInfo.vid);
        listener.onSuccess(videoInfo);
    }

    @Override
    public void requestAdv(PlayRequest playRequest, PlayVideoInfo playVideoInfo, VideoUrlInfo videoUrlInfo, final IGetAdvCallBack listener) {
        if (!mMediaPlayerDelegate.isUsingUMediaplyer()
                || !MediaPlayerConfiguration.getInstance().showPreAd()
                || !MediaPlayerConfiguration.getInstance().showOfflineAd()) {
            Logger.d(LogTag.TAG_PLAYER, "MediaPlayerDelegate -> playLocalVideoWithAdv   with null adv");
            new MainThreadExecutor().executeDelayed(new Runnable() {
                @Override
                public void run() {
                    listener.onFailed(new GoplayException());
                }
            }, 100);

            return;
        }
        AdvRequest mAdvRequest = null;
        boolean isVip = (MediaPlayerDelegate.mIUserInfo != null && MediaPlayerDelegate.mIUserInfo.isVip());

        // 获取离线广告
        if (!Util.isWifi() && MediaPlayerConfiguration.getInstance().showOfflineAd() && !isVip) {
            mAdvRequest = new OfflineAdvRequest();
        } else if (Util.isWifi()) {
            mAdvRequest = new OnlineAdvRequest();
        } else {
            listener.onFailed(new GoplayException());
        }

        if (mAdvRequest != null)
            mAdvRequest.requestAdv(playRequest, mMediaPlayerDelegate, mActivity, playVideoInfo, videoUrlInfo, listener);
    }
}
