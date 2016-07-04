package com.youku.player.request;

import com.youku.player.module.PlayVideoInfo;

/**
 * 抽象的请求对象，请求成功后回调{@link OnRequestDoneListener}，通过{@code mCanceled}标识请求是否已经取消
 */
public abstract class PlayRequest {

    private boolean mCanceled = false;

    private PlayVideoInfo mPlayVideoinfo;

    public PlayRequest(PlayVideoInfo playVideoInfo) {
        mPlayVideoinfo = playVideoInfo;
    }

    abstract public void playRequest(PlayVideoInfo playVideoInfo, OnRequestDoneListener listener);

    public void cancel() {
        mCanceled = true;
    }

    public boolean isCanceled() {
        return mCanceled;
    }

    public PlayVideoInfo getPlayVideoinfo() {
        return mPlayVideoinfo;
    }
}
