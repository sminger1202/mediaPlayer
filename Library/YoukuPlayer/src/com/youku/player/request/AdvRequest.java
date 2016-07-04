package com.youku.player.request;

import android.support.v4.app.FragmentActivity;

import com.youku.player.goplay.IGetAdvCallBack;
import com.youku.player.module.PlayVideoInfo;
import com.youku.player.module.VideoUrlInfo;
import com.youku.player.plugin.MediaPlayerDelegate;

import java.lang.ref.WeakReference;

public interface AdvRequest {
    void requestAdv(PlayRequest playRequest, MediaPlayerDelegate mMediaPlayerDelegate,
                    WeakReference<FragmentActivity> mActivity, PlayVideoInfo playVideoInfo, VideoUrlInfo videoUrlInfo, IGetAdvCallBack listener);
}
