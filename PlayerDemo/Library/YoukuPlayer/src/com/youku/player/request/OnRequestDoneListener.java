package com.youku.player.request;

import com.youku.player.goplay.VideoAdvInfo;
import com.youku.player.module.VideoUrlInfo;

/**
 * 请求完成的回调，只有请求成功可以播放的情况下回调
 */
public interface OnRequestDoneListener {
    void onRequestDone(VideoUrlInfo videoUrlInfo, VideoAdvInfo advInfo);
}
