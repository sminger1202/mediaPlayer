package com.youku.player.request;

import com.baseproject.utils.Logger;
import com.youku.player.LogTag;
import com.youku.player.goplay.GoplayException;
import com.youku.player.goplay.IGetAdvCallBack;
import com.youku.player.goplay.IVideoInfoCallBack;
import com.youku.player.goplay.VideoAdvInfo;
import com.youku.player.module.PlayVideoInfo;
import com.youku.player.module.VideoUrlInfo;
import com.youku.player.util.MainThreadExecutor;

import java.util.concurrent.CountDownLatch;

/**
 * 正片和广告异步请求，请求成功后回调{@link OnRequestDoneListener}
 */
public class AsyncPlayRequest extends PlayRequest {
    private CountDownLatch mCountDownLatch;
    private VideoRequest mVideoRequest;
    private boolean mSuccess;
    private VideoUrlInfo mVideoInfo;
    private VideoAdvInfo mAdvInfo;

    public AsyncPlayRequest(PlayVideoInfo playVideoInfo, VideoRequest videoRequest) {
        super(playVideoInfo);
        mCountDownLatch = new CountDownLatch(2);
        mVideoRequest = videoRequest;
    }


    @Override
    public void playRequest(PlayVideoInfo playVideoInfo, final OnRequestDoneListener listener) {
        mVideoRequest.requestVideo(this, playVideoInfo, new IVideoInfoCallBack() {
            @Override
            public void onSuccess(VideoUrlInfo videoUrlInfo) {
                mSuccess = true;
                mVideoInfo = videoUrlInfo;
                mCountDownLatch.countDown();
            }

            @Override
            public void onFailed(GoplayException e) {
                mCountDownLatch.countDown();
            }
        });

        mVideoRequest.requestAdv(this, playVideoInfo, null, new IGetAdvCallBack() {
            @Override
            public void onSuccess(VideoAdvInfo videoAdvInfo) {
                mAdvInfo = videoAdvInfo;
                mCountDownLatch.countDown();
            }

            @Override
            public void onFailed(GoplayException e) {
                mCountDownLatch.countDown();
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mCountDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                new MainThreadExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        Logger.d(LogTag.TAG_PLAYER, "playRequest done,success:" + mSuccess + "  isCanceled:" + isCanceled());
                        if (mSuccess && !isCanceled())
                            listener.onRequestDone(mVideoInfo, mAdvInfo);
                    }
                });

            }
        }).start();
    }
}
