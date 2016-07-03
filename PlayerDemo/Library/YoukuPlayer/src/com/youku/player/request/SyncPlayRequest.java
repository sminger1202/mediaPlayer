package com.youku.player.request;

import com.baseproject.utils.Logger;
import com.youku.player.LogTag;
import com.youku.player.goplay.GoplayException;
import com.youku.player.goplay.IGetAdvCallBack;
import com.youku.player.goplay.IVideoInfoCallBack;
import com.youku.player.goplay.VideoAdvInfo;
import com.youku.player.module.PlayVideoInfo;
import com.youku.player.module.VideoUrlInfo;

/**
 * 同步请求，先请求正片，正片请求成功后请求广告，请求成功后回调{@link OnRequestDoneListener}
 */
public class SyncPlayRequest extends PlayRequest {
    private VideoRequest mVideoRequest;

    public SyncPlayRequest(PlayVideoInfo playVideoInfo, VideoRequest videoRequest) {
        super(playVideoInfo);
        mVideoRequest = videoRequest;
    }

    @Override
    public void playRequest(final PlayVideoInfo playVideoInfo, final OnRequestDoneListener listener) {
        mVideoRequest.requestVideo(this, playVideoInfo, new IVideoInfoCallBack() {
            @Override
            public void onSuccess(final VideoUrlInfo videoUrlInfo) {
                if (isCanceled()) {
                    Logger.d(LogTag.TAG_PLAYER, "SyncPlayRequest is canceled");
                    return;
                }
                mVideoRequest.requestAdv(SyncPlayRequest.this, playVideoInfo, videoUrlInfo, new IGetAdvCallBack() {
                    @Override
                    public void onSuccess(VideoAdvInfo videoAdvInfo) {
                        notifySuccess(listener, videoUrlInfo, videoAdvInfo);
                    }

                    @Override
                    public void onFailed(GoplayException e) {
                        notifySuccess(listener, videoUrlInfo, null);
                    }
                });
            }

            @Override
            public void onFailed(GoplayException e) {

            }
        });
    }

    private void notifySuccess(OnRequestDoneListener listener, VideoUrlInfo videoUrlInfo, VideoAdvInfo videoAdvInfo) {
        if (!isCanceled())
            listener.onRequestDone(videoUrlInfo, videoAdvInfo);
    }
}
