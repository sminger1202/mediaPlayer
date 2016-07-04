package com.youku.player.request;


import android.support.v4.app.FragmentActivity;

import com.baseproject.utils.Logger;
import com.youku.player.LogTag;
import com.youku.player.config.MediaPlayerConfiguration;
import com.youku.player.module.PlayVideoInfo;
import com.youku.player.plugin.MediaPlayerDelegate;

/**
 * 请求工厂类，根据{@link com.youku.player.base.PlayType}返回相应的请求对象
 */
public class PlayRequests {
    public static PlayRequest newPlayRequest(PlayVideoInfo playVideoInfo, MediaPlayerDelegate mediaPlayerDelegate, FragmentActivity activity) {
        PlayRequest playRequest;
        Logger.d(LogTag.TAG_PLAYER, "newPlayRequest:" + playVideoInfo.getPlayType().name().toLowerCase());
        switch (playVideoInfo.getPlayType()) {
            case ONLINE:
                if (MediaPlayerConfiguration.getInstance().requestAsync()) {
                    Logger.d(LogTag.TAG_PLAYER, "request asynchronous");
                    playRequest = new AsyncPlayRequest(playVideoInfo, new OnlineVideoRequest(mediaPlayerDelegate, activity));
                } else {
                    Logger.d(LogTag.TAG_PLAYER, "request synchronous");
                    playRequest = new SyncPlayRequest(playVideoInfo, new OnlineVideoRequest(mediaPlayerDelegate, activity));
                }
                break;
            case LOCAL_DOWNLOAD:
                playRequest = new SyncPlayRequest(playVideoInfo, new LocalDownloadVideoRequest(mediaPlayerDelegate, activity));
                break;
            case LIVE:
                playRequest = new SyncPlayRequest(playVideoInfo, new LiveVideoRequest(mediaPlayerDelegate, activity));
                break;
            case LOCAL_USER_FILE:
                playRequest = new SyncPlayRequest(playVideoInfo, new LocalFileVideoRequest(mediaPlayerDelegate, activity));
                break;
            default:
                throw new IllegalArgumentException();
        }
        return playRequest;
    }
}
