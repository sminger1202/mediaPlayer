package com.youku.player.request;


import android.support.v4.app.FragmentActivity;

import com.youku.player.Track;
import com.youku.player.goplay.IGetAdvCallBack;
import com.youku.player.goplay.IVideoInfoCallBack;
import com.youku.player.goplay.StaticsUtil;
import com.youku.player.module.PlayVideoInfo;
import com.youku.player.module.VideoHistoryInfo;
import com.youku.player.module.VideoUrlInfo;
import com.youku.player.plugin.MediaPlayerDelegate;
import com.youku.player.util.PlayerUtil;

/**
 * 用户本地视频请求
 */
public class LocalFileVideoRequest extends VideoRequest {
    public LocalFileVideoRequest(MediaPlayerDelegate mediaPlayerDelegate, FragmentActivity activity) {
        super(mediaPlayerDelegate, activity);
    }

    @Override
    public void requestVideo(PlayRequest playRequest, PlayVideoInfo playVideoInfo, IVideoInfoCallBack listener) {
        Track.playRequest(mContext, playVideoInfo.vid, StaticsUtil.PLAY_TYPE_LOCAL, PlayerUtil.isLogin());
        VideoUrlInfo videoInfo = new VideoUrlInfo();
        videoInfo.setVid(playVideoInfo.vid);
        videoInfo.setProgress(playVideoInfo.point);
        videoInfo.cachePath = playVideoInfo.vid;
        videoInfo.setCached(true);
        videoInfo.playType = StaticsUtil.PLAY_TYPE_LOCAL;
        videoInfo.isExternalVideo = true;
        videoInfo.mSource = VideoUrlInfo.Source.LOCAL;
        videoInfo.setTitle(playVideoInfo.getTitle());

        if (playVideoInfo.point <= 0 && MediaPlayerDelegate.mIVideoHistoryInfo != null) {
            VideoHistoryInfo history = MediaPlayerDelegate.mIVideoHistoryInfo
                    .getVideoHistoryInfo(playVideoInfo.vid);
            if (history != null && history.playTime > 1) {
                videoInfo.setProgress(history.playTime * 1000);
            }
        }
        mMediaPlayerDelegate.getPlayerUiControl().goFullScreen();
        listener.onSuccess(videoInfo);
    }

    @Override
    public void requestAdv(PlayRequest playRequest, PlayVideoInfo playVideoInfo, VideoUrlInfo videoUrlInfo, IGetAdvCallBack listener) {
        listener.onFailed(null);
    }
}
