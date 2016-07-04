package com.youku.player.request;

import android.content.Context;
import android.support.v4.app.FragmentActivity;

import com.youku.player.goplay.IGetAdvCallBack;
import com.youku.player.goplay.IVideoInfoCallBack;
import com.youku.player.module.PlayVideoInfo;
import com.youku.player.module.VideoUrlInfo;
import com.youku.player.plugin.MediaPlayerDelegate;

import java.lang.ref.WeakReference;

/**
 * 视频请求，包含正片和广告的请求
 */
public abstract class VideoRequest {
    protected MediaPlayerDelegate mMediaPlayerDelegate;
    protected WeakReference<FragmentActivity> mActivity;
    protected Context mContext;

    public VideoRequest(MediaPlayerDelegate mediaPlayerDelegate, FragmentActivity activity) {
        mMediaPlayerDelegate = mediaPlayerDelegate;
        mActivity = new WeakReference<FragmentActivity>(activity);
        mContext = activity.getApplicationContext();
    }


    abstract public void requestVideo(PlayRequest playRequest, PlayVideoInfo playVideoInfo, IVideoInfoCallBack listener);

    abstract public void requestAdv(PlayRequest playRequest, PlayVideoInfo playVideoInfo, VideoUrlInfo videoUrlInfo, IGetAdvCallBack listener);
}
