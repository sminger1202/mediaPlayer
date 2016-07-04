package com.youku.player.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Handler;
import android.widget.Toast;

import com.baseproject.utils.Logger;
import com.youku.player.BaseMediaPlayer;
import com.youku.player.LogTag;
import com.youku.player.Track;
import com.youku.player.ad.AdState;
import com.youku.player.apiservice.IAdPlayerCallback;
import com.youku.player.apiservice.IPlayerAdControl;
import com.youku.player.apiservice.IPlayerUiControl;
import com.youku.player.config.MediaPlayerConfiguration;
import com.youku.player.danmaku.LocalDanmakuManager;
import com.youku.player.goplay.Profile;
import com.youku.player.goplay.StaticsUtil;
import com.youku.player.module.LiveInfo;
import com.youku.player.p2p.P2pManager;
import com.youku.player.plugin.MediaPlayerDelegate;
import com.youku.player.unicom.ChinaUnicomFreeFlowUtil;
import com.youku.player.util.DetailMessage;
import com.youku.player.util.DisposableStatsUtils;
import com.youku.player.util.PlayCode;
import com.youku.player.util.PlayerUtil;
import com.youku.player.util.SessionUnitil;
import com.youku.player.util.URLContainer;
import com.youku.statistics.TaskSendPlayBreak;
import com.youku.uplayer.MPPErrorCode;
import com.youku.uplayer.MediaPlayerProxy;
import com.youku.uplayer.OnADCountListener;
import com.youku.uplayer.OnADPlayListener;
import com.youku.uplayer.OnBufferPercentUpdateListener;
import com.youku.uplayer.OnConnectDelayListener;
import com.youku.uplayer.OnCurrentPositionUpdateListener;
import com.youku.uplayer.OnHttp302DelayListener;
import com.youku.uplayer.OnHwDecodeErrorListener;
import com.youku.uplayer.OnLoadingStatusListener;
import com.youku.uplayer.OnMidADPlayListener;
import com.youku.uplayer.OnNetworkSpeedListener;
import com.youku.uplayer.OnNetworkSpeedPerMinute;
import com.youku.uplayer.OnQualityChangeListener;
import com.youku.uplayer.OnRealVideoStartListener;
import com.youku.uplayer.OnTimeoutListener;
import com.youku.uplayer.OnUplayerPreparedListener;
import com.youku.uplayer.OnVideoIndexUpdateListener;

import java.util.ArrayList;

/**
 * media player initialize callbacks
 */
@SuppressLint("NewApi")
public final class MediaPlayerInit implements DetailMessage {
    private final Activity mActivity;
    private final MediaPlayerDelegate mMediaPlayerDelegate;

    private boolean isSendPlayBreakEvent = false; //是否发送播放中断，只发送一�?\
    private ArrayList<LiveInfo> mliveInfos = new ArrayList<LiveInfo>();
    private LiveInfo mliveInfo;

    private Handler handler = new Handler() {
    };

    private static String TAG = LogTag.TAG_PLAYER;

    public MediaPlayerInit(Activity activity, MediaPlayerDelegate mediaPlayerDelegate) {
        mActivity = activity;
        mMediaPlayerDelegate = mediaPlayerDelegate;
    }

    public void execute(final IPlayerUiControl playerUiControl, final IPlayerAdControl playerAdControl,
                        final IAdPlayerCallback preAdCallback, final IAdPlayerCallback midAdCallback) {
        if (mActivity == null || mMediaPlayerDelegate == null
                || playerUiControl == null || playerAdControl == null) {
            return;
        }
        mMediaPlayerDelegate.mediaPlayer = BaseMediaPlayer.getInstance();
        mMediaPlayerDelegate.mediaPlayer
                .setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {

                    @Override
                    public void onBufferingUpdate(MediaPlayer mp, int percent) {
                        if (playerUiControl.isOnPause()) {
                            mp.release();
                            return;
                        }
                        if (mMediaPlayerDelegate.pluginManager == null)
                            return;
                        mMediaPlayerDelegate.pluginManager.onBufferingUpdateListener(percent);
                    }
                });
        mMediaPlayerDelegate.mediaPlayer
                .setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        if (mMediaPlayerDelegate != null)
                            mMediaPlayerDelegate.onComplete();
                        if (playerUiControl.getYoukuPlayerView() != null) {
                            playerUiControl.getYoukuPlayerView().setPlayerBlack();
                        }
                        playerUiControl.hideWebView();
                    }
                });
        mMediaPlayerDelegate.mediaPlayer
                .setOnErrorListener(new MediaPlayer.OnErrorListener() {

                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        Logger.d(LogTag.TAG_PLAYER, "播放器出现错�?MediaPlayer onError what=" + what
                                + " !!!");
                        if (mMediaPlayerDelegate != null) {
                            mMediaPlayerDelegate.pauseDuringSeek = false;
                            mMediaPlayerDelegate.isSeeking = false;
                        }
                        // 解决加密视频系统播放器无法播放问题，本地mp4视频播放失败，从系统播放器切换到软解播放�?
                        if (mMediaPlayerDelegate != null
                                && mMediaPlayerDelegate.videoInfo != null
                                && mMediaPlayerDelegate.videoInfo.isExternalVideo
                                && MediaPlayerProxy.isUplayerSupported()
                                && mp != null
                                && !((MediaPlayerProxy) mp)
                                .isUsingUMediaplayer()) {
                            processExternalVideoError();
                            return true;
                        }

                        if (playerUiControl.getYoukuPlayerView() != null)
                            playerUiControl.getYoukuPlayerView().setDebugText("出现错误-->onError:"
                                    + what);
                        disposeAdErrorLoss(what);
                        if (playerAdControl.getMidAdModel() != null) {
                            playerAdControl.getMidAdModel().isAfterEndNoSeek = false;
                        }
                        if (what == MPPErrorCode.MEDIA_INFO_MIDAD_DATA_SOURCE_ERROR
                                && !playerAdControl.isMidAdShowing()) {
                            //解决未切换到中插广告状态时，加载中插失败，一直loading问题，需重新倒计时。
                            if (playerAdControl.getMidAdModel() != null) {
                                playerAdControl.getMidAdModel().isAfterEndNoSeek = false;
                                playerAdControl.getMidAdModel().startTimer();
                                playerAdControl.onMidAdLoadingEndListener();
                            }
                            return true;
                        }
                        if (isAdPlayError(what)) {
                            Logger.d(LogTag.TAG_PLAYER, "出现错误:" + what + " 处理结果:跳过广告播放");
                            Track.addAdLevelErrors(what, extra, !mMediaPlayerDelegate.isAdvShowFinished());
                            return loadingADOverTime(playerUiControl, playerAdControl);
                        }
                        Track.onPlayError(what, extra, mMediaPlayerDelegate != null ? mMediaPlayerDelegate.getCurrentPosition() : 0);
                        // 播放P2P过程中出错，不通知上层直接重试，如果超过重试次数则直接使用CDN地址，需要排除切换清晰度的情�?
                        if (P2pManager.getInstance().isUsingP2P()
                                && mMediaPlayerDelegate != null
                                && !mMediaPlayerDelegate.changeQuality) {
                            processP2PError();
                            return true;
                        }
                        if (playerUiControl.getYoukuPlayerView() != null
                                && !playerUiControl.getYoukuPlayerView().realVideoStart) {
                            playerUiControl.updatePlugin(PLUGIN_SHOW_NOT_SET);
                        }
                        // 出现4xx错误重新获取播放地址
                        if (what == MPPErrorCode.MEDIA_INFO_VIDEO_HTTP_ERROR_4XX && mMediaPlayerDelegate != null
                                && mMediaPlayerDelegate.videoInfo != null)
                            mMediaPlayerDelegate.videoInfo.setHttp4xxError(true);

                        if (!isSendPlayBreakEvent
                                && MediaPlayerConfiguration.getInstance().trackPlayError()
                                && playerUiControl.getYoukuPlayerView().realVideoStart
                                && mMediaPlayerDelegate != null
                                && mMediaPlayerDelegate.videoInfo != null) {
                            final String videoUrl = mMediaPlayerDelegate.videoInfo
                                    .getWeburl();
                            final TaskSendPlayBreak task = new TaskSendPlayBreak(
                                    videoUrl);
                            task.execute();
                            isSendPlayBreakEvent = true;
                        }
                        if (playerUiControl.getYoukuPlayerView().realVideoStart && mMediaPlayerDelegate.isLoading)
                            Track.onPlayLoadingEnd(mMediaPlayerDelegate.videoInfo);
                        mMediaPlayerDelegate.isLoading = false;
                        trackError(what);
                        onLoadingFailError();
                        if (mMediaPlayerDelegate.pluginManager == null) {
                            Logger.d(LogTag.TAG_PLAYER, "onError出现错误:" + what + " pluginManager == null  return false");
                            return false;
                        }
                        //Logger.d(LogTag.TAG_PLAYER, "出现错误:" + what + " 处理结果:去重�?);"
                        int nowPostition = mMediaPlayerDelegate.getCurrentPosition();
                        if (nowPostition > 0) {
                            mMediaPlayerDelegate.setAdPausedPosition(nowPostition);
                        }
                        // 系统播放器错误特殊处�?
                        if (what == -38
                                && !MediaPlayerProxy.isUplayerSupported()) {
                            what = MPPErrorCode.MEDIA_INFO_PLAY_UNKNOW_ERROR;
                        }

                        playerUiControl.hideWebView();
                        return mMediaPlayerDelegate.pluginManager.onError(what, extra);
                    }

                    private void processExternalVideoError() {
                        mActivity.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                if (mMediaPlayerDelegate != null
                                        && mMediaPlayerDelegate.videoInfo != null)
                                    mMediaPlayerDelegate.videoInfo.isEncyptError = true;
                                mMediaPlayerDelegate.release();
                                if (!playerUiControl.isOnPause())
                                    mMediaPlayerDelegate.start();
                            }
                        });
                    }

                    private void processP2PError() {
                        ++P2pManager.getInstance().mRetryTimes;
                        mActivity.runOnUiThread(new Runnable() {
                            public void run() {
                                Logger.e(LogTag.TAG_PLAYER, "p2p error, retry");
                                if (mMediaPlayerDelegate.pluginManager != null)
                                    mMediaPlayerDelegate.pluginManager.onLoading();
                                if (mMediaPlayerDelegate != null) {
                                    mMediaPlayerDelegate.release();
                                    if (null != handler) {
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (!playerUiControl.isOnPause()
                                                        && mMediaPlayerDelegate != null)
                                                    mMediaPlayerDelegate.start();
                                            }
                                        }, 100);
                                    }
                                }
                            }
                        });
                    }

                    private boolean isAdPlayError(int what) {
                        return what == MPPErrorCode.MEDIA_INFO_PREPARED_AD_CHECK
                                || what == MPPErrorCode.MEDIA_INFO_AD_HTTP_ERROR_4XX
                                || what == MPPErrorCode.MEDIA_INFO_PREPARED_MID_AD_CHECK
                                || (what == MPPErrorCode.MEDIA_INFO_DATA_SOURCE_ERROR && !mMediaPlayerDelegate
                                .isAdvShowFinished())
                                || (what == MPPErrorCode.MEDIA_INFO_MIDAD_DATA_SOURCE_ERROR && playerAdControl.isMidAdShowing())
                                || (what == MPPErrorCode.MEDIA_INFO_PLAYERROR && (mMediaPlayerDelegate.isADShowing || playerAdControl.isMidAdShowing()))
                                || (what == MPPErrorCode.MEDIA_INFO_NETWORK_ERROR && (mMediaPlayerDelegate.isADShowing || playerAdControl.isMidAdShowing()))
                                || (what == MPPErrorCode.MEDIA_INFO_NETWORK_CHECK && (mMediaPlayerDelegate.isADShowing || playerAdControl.isMidAdShowing()));
                    }

                    //广告损耗埋点使�?
                    private void disposeAdErrorLoss(int what) {
                        if (mMediaPlayerDelegate == null
                                || mMediaPlayerDelegate.videoInfo == null) {
                            return;
                        }
                        DisposableStatsUtils.disposeNotPlayedAd(mActivity, mMediaPlayerDelegate.videoInfo, URLContainer.AD_LOSS_STEP6_NEW);

                        if (playerAdControl.isMidAdShowing()) {
                            if (what == MPPErrorCode.MEDIA_INFO_DATA_SOURCE_ERROR)
                                DisposableStatsUtils.disposeAdLoss(
                                        mActivity,
                                        URLContainer.AD_LOSS_STEP4,
                                        SessionUnitil.playEvent_session,
                                        URLContainer.AD_LOSS_MO);
                            else if (what == MPPErrorCode.MEDIA_INFO_PREPARED_AD_CHECK)
                                DisposableStatsUtils.disposeAdLoss(
                                        mActivity,
                                        URLContainer.AD_LOSS_STEP6,
                                        SessionUnitil.playEvent_session, URLContainer.AD_LOSS_MO);

                        }
                    }
                });

        mMediaPlayerDelegate.mediaPlayer
                .setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        if (mMediaPlayerDelegate.pluginManager == null)
                            return;
                        mMediaPlayerDelegate.pluginManager.onPrepared();
                    }
                });

        mMediaPlayerDelegate.mediaPlayer
                .setOnUplayerPreparedListener(new OnUplayerPreparedListener() {

                    @Override
                    public void OnUplayerPrepared() {
                        if (playerAdControl.isMidAdShowing() && playerAdControl.getMidAdModel() != null
                                && !playerAdControl.getMidAdModel().isCurrentAdvEmpty()) {
                            playerAdControl.getMidAdModel().playMidAD(mMediaPlayerDelegate.getAdPausedPosition());
                        }
                    }

                });

        mMediaPlayerDelegate.mediaPlayer
                .setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {

                    @Override
                    public void onSeekComplete(MediaPlayer mp) {
                        Logger.d(LogTag.TAG_PLAYER, "onSeekComplete");
                        Track.onSeekComplete();
                        if (mMediaPlayerDelegate != null) {
                            mMediaPlayerDelegate.isLoading = false;
                            mMediaPlayerDelegate.isSeeking = false;
                        }
                        Track.setTrackPlayLoading(true);
                        if (mMediaPlayerDelegate.pluginManager == null)
                            return;

                        mActivity.runOnUiThread(new Runnable() {
                            public void run() {
                                mMediaPlayerDelegate.pluginManager.onSeekComplete();
                            }
                        });

                    }
                });
        mMediaPlayerDelegate.mediaPlayer
                .setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {

                    @Override
                    public void onVideoSizeChanged(MediaPlayer mp, int width,
                                                   int height) {
                        if (mMediaPlayerDelegate.pluginManager == null)
                            return;
                        mMediaPlayerDelegate.pluginManager.onVideoSizeChanged(width, height);
                        Logger.d(TAG, "onVideoSizeChanged-->" + width + height);
                        if (mMediaPlayerDelegate.mediaPlayer != null)
                            mMediaPlayerDelegate.mediaPlayer.updateWidthAndHeight(
                                    width, height);

                    }
                });
        mMediaPlayerDelegate.mediaPlayer
                .setOnTimeOutListener(new OnTimeoutListener() {

                    @Override
                    public void onTimeOut() {
                        if (mMediaPlayerDelegate == null)
                            return;
                        Logger.d(LogTag.TAG_PLAYER, "onTimeOut");
                        mMediaPlayerDelegate.release();
                        mActivity.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                Track.pauseForIRVideo(mActivity);
                                Track.pause();
                                onLoadingFailError();
                            }
                        });
                        if (!isSendPlayBreakEvent
                                && MediaPlayerConfiguration.getInstance().trackPlayError()
                                && playerUiControl.getYoukuPlayerView().realVideoStart
                                && mMediaPlayerDelegate != null
                                && mMediaPlayerDelegate.videoInfo != null) {
                            final String videoUrl = mMediaPlayerDelegate.videoInfo
                                    .getWeburl();
                            final TaskSendPlayBreak task = new TaskSendPlayBreak(
                                    videoUrl);
                            task.execute();
                            isSendPlayBreakEvent = true;
                        }
                        mActivity.runOnUiThread(new Runnable() {
                            public void run() {
                                if (mMediaPlayerDelegate.pluginManager == null)
                                    return;
                                mMediaPlayerDelegate.pluginManager.onTimeout();
                            }
                        });
                    }

                    @Override
                    public void onNotifyChangeVideoQuality() {
                        if (mMediaPlayerDelegate.pluginManager == null)
                            return;
                        Logger.d(LogTag.TAG_PLAYER, "onNotifyChangeVideoQuality");
                        mMediaPlayerDelegate.pluginManager.onNotifyChangeVideoQuality();
                    }
                });
        mMediaPlayerDelegate.mediaPlayer
                .setOnCurrentPositionUpdateListener(new OnCurrentPositionUpdateListener() {

                    @Override
                    public void onCurrentPositionUpdate(
                            final int currentPosition) {
                        if (mMediaPlayerDelegate.pluginManager == null)
                            return;
                        if (playerAdControl.getMidAdModel() != null) {
                            playerAdControl.getMidAdModel().onPositionUpdate(currentPosition);
                        }
                        mActivity.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    mMediaPlayerDelegate.pluginManager.onCurrentPositionChange(currentPosition);
                                    if (mMediaPlayerDelegate != null
                                            && mMediaPlayerDelegate.isFullScreen && playerUiControl.getSubtitleOperate() != null) {
                                        playerUiControl.getSubtitleOperate().showSubtitle(currentPosition);
                                    } else {
                                        if (playerUiControl.getSubtitleOperate() != null) {
                                            playerUiControl.getSubtitleOperate().dismissSubtitle();
                                        }
                                    }
                                } catch (Exception e) {
                                }
                            }
                        });
                        if (playerUiControl.getDanmakuManager() != null) {
                            playerUiControl.getDanmakuManager().onPositionChanged(currentPosition);
                        }
                        if(mMediaPlayerDelegate.isLooping()){
                            if(currentPosition >= mMediaPlayerDelegate.getLoopEndTime()){
                                mMediaPlayerDelegate.startLoopVideo(mMediaPlayerDelegate.getLoopStartTime(),
                                        mMediaPlayerDelegate.getLoopEndTime());
                            }
                        }
                    }
                });

        if (PlayerUtil.useUplayer(mMediaPlayerDelegate.videoInfo)) {
            mMediaPlayerDelegate.mediaPlayer
                    .setOnADPlayListener(new OnADPlayListener() {

                        @Override
                        public boolean onStartPlayAD(int index) {
                            Logger.d(LogTag.TAG_PLAYER, "onstartPlayAD");
                            Track.setOnPaused(false);
                            if (preAdCallback != null) {
                                return preAdCallback.onAdStart(index);
                            }
                            return false;
                        }

                        @Override
                        public boolean onEndPlayAD(int index) {
                            Logger.d(LogTag.TAG_PLAYER, "onEndPlayAD");
                            if (preAdCallback != null) {
                                return preAdCallback.onAdEnd(index);
                            }
                            return false;
                        }
                    });
            mMediaPlayerDelegate.mediaPlayer
                    .setOnADCountListener(new OnADCountListener() {

                        @Override
                        public void onCountUpdate(final int count) {
                            mMediaPlayerDelegate.setAdPausedPosition(mMediaPlayerDelegate.getCurrentPosition());
                            if (playerAdControl.isMidAdShowing() && midAdCallback != null) {
                                midAdCallback.onADCountUpdate(count);
                            } else if (preAdCallback != null) {
                                preAdCallback.onADCountUpdate(count);
                            }
                        }
                    });
            mMediaPlayerDelegate.mediaPlayer
                    .setOnMidADPlayListener(new OnMidADPlayListener() {

                        @Override
                        public boolean onStartPlayMidAD(int index) {
                            Logger.d(LogTag.TAG_PLAYER, "onStartPlayMidAD");
                            if (playerUiControl.getDanmakuManager() != null) {
                                playerUiControl.getDanmakuManager().hideDanmakuWhenOpen();
                            }
                            if (null != mMediaPlayerDelegate.pluginManager) {
                                mActivity.runOnUiThread(new Runnable() {
                                    public void run() {
                                        mMediaPlayerDelegate.pluginManager.onADplaying();
                                    }
                                });
                            }
                            if (midAdCallback != null) {
                                return midAdCallback.onAdStart(index);
                            }
                            return false;
                        }

                        @Override
                        public boolean onEndPlayMidAD(int index) {
                            Logger.d(LogTag.TAG_PLAYER, "onEndPlayMidAD");
                            if (midAdCallback != null) {
                                return midAdCallback.onAdEnd(index);
                            }
                            return false;
                        }

                        @Override
                        public void onLoadingMidADStart() {
                            playerAdControl.onMidAdLoadingStartListener();
                        }

                    });
            mMediaPlayerDelegate.mediaPlayer
                    .setOnNetworkSpeedListener(new OnNetworkSpeedListener() {

                        @Override
                        public void onSpeedUpdate(final int count) {
                            if (null != mMediaPlayerDelegate.pluginManager) {
                                mActivity.runOnUiThread(new Runnable() {
                                    public void run() {
                                        mMediaPlayerDelegate.pluginManager.onNetSpeedChange(count);
                                    }
                                });
                            }
                        }
                    });
            mMediaPlayerDelegate.mediaPlayer.setOnNetworkSpeedPerMinute(new OnNetworkSpeedPerMinute() {
                @Override
                public void onSpeedUpdate(int speed) {
                    Logger.d(TAG, "network speed per minute:" + speed);
                }
            });
            mMediaPlayerDelegate.mediaPlayer.setOnBufferPercentUpdateListener(new OnBufferPercentUpdateListener() {
                @Override
                public void onPercentUpdate(int percent) {
                    Logger.d(TAG, "buffer percent:" + percent);
                    if (playerUiControl.isOnPause()) {
                        return;
                    }
                    if (mMediaPlayerDelegate.pluginManager == null)
                        return;
                    mMediaPlayerDelegate.pluginManager.onBufferPercentUpdate(percent);
                }
            });
        }
        mMediaPlayerDelegate.mediaPlayer
                .setOnRealVideoStartListener(new OnRealVideoStartListener() {

                    @Override
                    public void onRealVideoStart() {

                        if (playerUiControl.isOnPause())
                            return;
                        // 这个listener的理解是正片开始播放的时候调�?这个时候的
                        // mMediaPlayerDelegate为空的概率比较大
                        Logger.d(LogTag.TAG_PLAYER, "正片开始播放，没有错误");
                        Track.isRealVideoStarted = true;
                        Track.setPlayerStarted(true);
                        playerAdControl.setAdState(AdState.REALVIDEO);
                        if (mMediaPlayerDelegate != null) {
                            mMediaPlayerDelegate.changeQuality = false;
                        }
                        String vid = "";
                        if (mMediaPlayerDelegate != null
                                && mMediaPlayerDelegate.videoInfo != null) {
                            vid = mMediaPlayerDelegate.videoInfo.getVid();
                        }
                        Track.onRealVideoFirstLoadEnd(mActivity.getApplicationContext(),
                                vid, mMediaPlayerDelegate.videoInfo);
                        //播放器获取时间有误， eg：银魂211.
//                        localStartSetDuration();
                        sentonVVBegin(playerUiControl);
                        playerUiControl.getYoukuPlayerView().setPlayerBlackGone();
                        if (mMediaPlayerDelegate != null
                                && mMediaPlayerDelegate.videoInfo != null) {
                            mMediaPlayerDelegate.isADShowing = false;
                            Logger.d(TAG, "onRealVideoStart"
                                    + mMediaPlayerDelegate.videoInfo.IsSendVV);
                            Logger.d(
                                    TAG,
                                    "OnRealVideoStartListener mMediaPlayerDelegate.videoInfo.getProgress():"
                                            + mMediaPlayerDelegate.videoInfo
                                            .getProgress());
                            mMediaPlayerDelegate.setPlayRate(mMediaPlayerDelegate.getPlayRate());
                        } else {
                            Logger.e(TAG,
                                    "onRealVideoStart mMediaPlayerDelegate空指");
                        }
                        mMediaPlayerDelegate.isLoading = false;
                        if (null != mMediaPlayerDelegate.pluginManager) {
                            mActivity.runOnUiThread(new Runnable() {
                                public void run() {
                                    if (playerUiControl.getDanmakuManager() != null) {
                                        playerUiControl.getDanmakuManager().continueDanmaku();
                                        if(!playerUiControl.getDanmakuManager().isDanmakuClosed()) {
                                            playerUiControl.getDanmakuManager().showDanmaku();
                                        }
                                    }
                                    playerUiControl.updatePlugin(PLUGIN_SHOW_NOT_SET);
                                    mMediaPlayerDelegate.pluginManager.onRealVideoStart();
                                    mMediaPlayerDelegate.pluginManager.onLoaded();
                                }
                            });
                        }

                        if (playerUiControl.getDanmakuManager() != null && MediaPlayerConfiguration.getInstance().hideDanmaku()) {
                            playerUiControl.getDanmakuManager().startLiveDanmaku();
                        }

                        skipHeadOrSeekToHistory(playerAdControl);
                        if (playerAdControl.getMidAdModel() != null) {
                            playerAdControl.getMidAdModel().isAfterEndNoSeek = false;
                            playerAdControl.getMidAdModel().startTimer();
                            playerAdControl.getMidAdModel().checkBufferStartContentAd();
                        }
                        playerAdControl.showInvestigate();
                        ChinaUnicomFreeFlowUtil.checkChinaUnicomStatus(mActivity,mMediaPlayerDelegate);
                        if (mMediaPlayerDelegate != null && mMediaPlayerDelegate.videoInfo != null && mMediaPlayerDelegate.videoInfo.isCached()) {
                            if (playerUiControl.getDanmakuManager() != null && (playerUiControl.getDanmakuManager() instanceof  LocalDanmakuManager) && !((LocalDanmakuManager)playerUiControl.getDanmakuManager()).isPaused) {
                                playerUiControl.getDanmakuManager().beginDanmaku(mMediaPlayerDelegate.videoInfo.savePath + "danmu", 0);
                            }
                        }

                    }
                });
        mMediaPlayerDelegate.mediaPlayer
                .setOnLoadingStatusListener(new OnLoadingStatusListener() {

                    @Override
                    public void onStartLoading() {

                        Logger.d(TAG, "onStartLoading");

                        if (mMediaPlayerDelegate.pluginManager == null || playerUiControl.isOnPause()
                                || mMediaPlayerDelegate.isLooping()) {// 当回放录屏的时候不提示loading
                            return;
                        }

                        mliveInfo = new LiveInfo();
                        mliveInfo.startLoadingTime = System.nanoTime() / 1000000;
                        Track.onPlayLoadingStart(mMediaPlayerDelegate.mediaPlayer
                                .getCurrentPosition());
                        if (mMediaPlayerDelegate != null) {
                            mMediaPlayerDelegate.isLoading = true;
                        }
                        mActivity.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                if (playerUiControl.getDanmakuManager() != null) {
                                    playerUiControl.getDanmakuManager().pauseDanmaku();
                                }
                                if (mMediaPlayerDelegate.pluginManager == null)
                                    return;
                                mMediaPlayerDelegate.pluginManager.onLoading();
                                if (PlayerUtil.useUplayer(mMediaPlayerDelegate.videoInfo) && !mMediaPlayerDelegate.videoInfo.isUseCachePath())
                                    mMediaPlayerDelegate.loadingPause();
                            }

                        });
                    }

                    @Override
                    public void onEndLoading() {
                        Logger.d(TAG, "onEndLoading");
                        mActivity.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                if (playerUiControl.getDanmakuManager() != null) {
                                    playerUiControl.getDanmakuManager().resumeDanmaku();
                                }
                                if (mMediaPlayerDelegate.pluginManager == null)
                                    return;
                                mMediaPlayerDelegate.pluginManager.onLoaded();
                            }
                        });
                        if (mliveInfo != null && mliveInfos != null) {
                            mliveInfo.endLoadingTime = System.nanoTime() / 1000000;
                            mliveInfos.add(mliveInfo);
                        }
                        if (null != mMediaPlayerDelegate) {
                            Track.onPlayLoadingEnd(mMediaPlayerDelegate.videoInfo);
                            mMediaPlayerDelegate.isStartPlay = true;
                            mMediaPlayerDelegate.isLoading = false;
                            if (null != mMediaPlayerDelegate.videoInfo) {
                                playerUiControl.updateVideoId(mMediaPlayerDelegate.videoInfo.getVid());
                                mMediaPlayerDelegate.videoInfo.isFirstLoaded = true;
                            }
                            // 本地mp4不控制自动开�?
                            if (PlayerUtil.useUplayer(mMediaPlayerDelegate.videoInfo)
                                    && !mMediaPlayerDelegate.videoInfo
                                    .isUseCachePath()
                                    && (!mMediaPlayerDelegate.pauseDuringSeek))
                                mMediaPlayerDelegate.start();
                        }
                    }
                });
        mMediaPlayerDelegate.mediaPlayer
                .setOnPlayHeartListener(new BaseMediaPlayer.OnPlayHeartListener() {

                    @Override
                    public void onPlayHeartSixtyInterval() {
                        if (mMediaPlayerDelegate != null
                                && mMediaPlayerDelegate.videoInfo != null)
                            Track.trackPlayHeart(mActivity.getApplicationContext(),
                                    mMediaPlayerDelegate.videoInfo,
                                    mMediaPlayerDelegate.isFullScreen);
                    }

                    @Override
                    public void onPlayHeartTwentyInterval() {
                        mActivity.runOnUiThread(new Runnable() {
                            public void run() {
                                if (mMediaPlayerDelegate != null
                                        && mMediaPlayerDelegate.videoInfo != null
                                        && Profile.PLANTFORM == Plantform.YOUKU) {
                                    Track.trackPlayHeartTwentyInterval(mActivity.getApplicationContext(),
                                            mMediaPlayerDelegate.videoInfo, mMediaPlayerDelegate.isFullScreen);
                                    if (mliveInfos != null) {
                                        if (mliveInfos.size() != 0) {
                                            float totalSecond = 0;
                                            for (int i = 0; i < mliveInfos.size(); i++) {
                                                totalSecond += (mliveInfos.get(i).endLoadingTime - mliveInfos
                                                        .get(i).startLoadingTime) / 1000F;
                                            }
                                            Track.trackUserExperience(
                                                    mActivity.getApplicationContext(),
                                                    mMediaPlayerDelegate.videoInfo,
                                                    totalSecond, mliveInfos.size());
                                            mliveInfos.clear();
                                        }
                                    }

                                }
                            }
                        });
                    }
                });
        mMediaPlayerDelegate.mediaPlayer
                .setOnVideoIndexUpdateListener(new OnVideoIndexUpdateListener() {

                    @Override
                    public void onVideoIndexUpdate(int currentIndex, int ip) {
                        Logger.d(LogTag.TAG_PLAYER, "onVideoIndexUpdate:"
                                + currentIndex + "  " + ip);
                        if (mMediaPlayerDelegate != null
                                && mMediaPlayerDelegate.videoInfo != null)
                            Track.onVideoIndexUpdate(mActivity.getApplicationContext(),
                                    currentIndex, ip,
                                    mMediaPlayerDelegate.videoInfo
                                            .getCurrentQuality());
                    }
                });
        mMediaPlayerDelegate.mediaPlayer
                .setOnHwDecodeErrorListener(new OnHwDecodeErrorListener() {
                    boolean isHwPlayErrorReceived;

                    @Override
                    public void OnHwDecodeError() {
                        Logger.d(LogTag.TAG_PLAYER, "OnHwDecodeError");
                        MediaPlayerConfiguration.getInstance()
                                .setUseHardwareDecode(false);
                        Track.sendHwError();
                    }

                    @Override
                    public void onHwPlayError() {
                        Logger.d(LogTag.TAG_PLAYER, "onHwPlayError");
                        mActivity.runOnUiThread(new Runnable() {
                            public void run() {
                                if (mMediaPlayerDelegate != null) {
                                    mMediaPlayerDelegate.release();
                                    if (isHwPlayErrorReceived)
                                        return;
                                    isHwPlayErrorReceived = true;
                                    if (playerAdControl.isMidAdShowing()
                                            && playerAdControl.getMidAdModel() != null) {
                                        playerAdControl.getMidAdModel().removeCurrentAdvInfo();
                                        playerAdControl.setAdState(AdState.REALVIDEO);
                                        playerAdControl.getMidAdModel().isAfterEndNoSeek = false;
                                        DisposableStatsUtils
                                                .disposeAdLoss(
                                                        mActivity,
                                                        URLContainer.AD_LOSS_STEP6,
                                                        SessionUnitil.playEvent_session,
                                                        URLContainer.AD_LOSS_MO);
                                    }
                                    Track.sendHwError();
                                    MediaPlayerConfiguration.getInstance()
                                            .setUseHardwareDecode(false);
                                    mMediaPlayerDelegate.pluginManager.onLoading();
                                    if (playerAdControl.getAdState() == AdState.PREAD) {
                                        playerAdControl.setAdState(AdState.REALVIDEO);
                                        mMediaPlayerDelegate
                                                .playVideoWhenADOverTime();
                                        DisposableStatsUtils
                                                .disposeNotPlayedAd(
                                                        mActivity,
                                                        mMediaPlayerDelegate.videoInfo, URLContainer.AD_LOSS_STEP6_NEW);
                                    } else {
                                        mMediaPlayerDelegate.start();
                                    }
                                }
                            }
                        });

                    }
                });
        mMediaPlayerDelegate.mediaPlayer.setOnConnectDelayListener(new OnConnectDelayListener() {
            @Override
            public void onVideoConnectDelay(int time) {
                Logger.d(TAG, "onVideoConnectDelay:" + time);
                Track.setVideoConnectDelayTime(time);
            }

            @Override
            public void onAdConnectDelay(int time) {
                Logger.d(TAG, "onAdConnectDelay:" + time);
                Track.addAdRsReqTimes(time, !mMediaPlayerDelegate.isAdvShowFinished());
            }
        });

        mMediaPlayerDelegate.mediaPlayer.setOnQualityChangeListener(new OnQualityChangeListener() {
            @Override
            public void onQualityChangeSuccess() {
                if (mMediaPlayerDelegate != null && mMediaPlayerDelegate.mediaPlayer != null) {
                    mMediaPlayerDelegate.mediaPlayer.updateWidthAndHeightFromNative();
                }
                MediaPlayerConfiguration.getInstance().mPlantformController.onQualitySmoothChangeEnd(playerUiControl, Profile.videoQuality);
                Track.onSmoothChangeVideoQualityEnd(true);
            }

            @Override
            public void onQualitySmoothChangeFail() {
                //平滑切换失败
                Track.onSmoothChangeVideoQualityEnd(false);
                //底层播放器自动进行普通切换
                if (mMediaPlayerDelegate != null) {
                    mMediaPlayerDelegate.changeVideoQualityByRestart(Profile.videoQuality);
                }
            }
        });
        mMediaPlayerDelegate.mediaPlayer.setOnHttp302DelayListener(new OnHttp302DelayListener() {
            @Override
            public void onVideo302Delay(int time) {
                Track.onVideo302Delay(time);
                Logger.d(TAG, "onVideo302Delay:" + time);
            }

            @Override
            public void onAd302Delay(int time) {
                Track.onAd302Delay(time);
                Logger.d(TAG, "onAd302Delay:" + time);
            }
        });
    }

    private void trackError(int what) {
        if (mMediaPlayerDelegate.videoInfo == null || mMediaPlayerDelegate.videoInfo.IsSendVV)
            return;

        if ((what == MPPErrorCode.MEDIA_INFO_DATA_SOURCE_ERROR
                || what == MPPErrorCode.MEDIA_INFO_NETWORK_DISSCONNECTED
                || what == MPPErrorCode.MEDIA_INFO_NETWORK_CHECK
                || what == MPPErrorCode.MEDIA_INFO_NETWORK_ERROR || what == MPPErrorCode.MEDIA_INFO_PREPARE_TIMEOUT_ERROR)
                && !mMediaPlayerDelegate.videoInfo.isHLS && !StaticsUtil.PLAY_TYPE_LOCAL
                .equals(mMediaPlayerDelegate.videoInfo.playType)) {
            Track.onError(mActivity.getApplicationContext(),
                    mMediaPlayerDelegate.videoInfo.getVid(), Profile.GUID,
                    mMediaPlayerDelegate.videoInfo.playType,
                    PlayCode.VIDEO_LOADING_FAIL,
                    mMediaPlayerDelegate.videoInfo.mSource,
                    mMediaPlayerDelegate.videoInfo.getCurrentQuality(),
                    mMediaPlayerDelegate.videoInfo.getProgress(),
                    mMediaPlayerDelegate.isFullScreen,
                    mMediaPlayerDelegate.videoInfo, mMediaPlayerDelegate.getPlayVideoInfo());
        } else if (StaticsUtil.PLAY_TYPE_LOCAL
                .equals(mMediaPlayerDelegate.videoInfo.playType) && what == MPPErrorCode.MEDIA_INFO_DATA_SOURCE_ERROR
                || what == MPPErrorCode.MEDIA_INFO_NETWORK_CHECK
                || what == MPPErrorCode.MEDIA_INFO_NETWORK_DISSCONNECTED
                || what == MPPErrorCode.MEDIA_INFO_SEEK_ERROR) {
            Track.onError(mActivity.getApplicationContext(),
                    mMediaPlayerDelegate.videoInfo.getVid(), Profile.GUID,
                    mMediaPlayerDelegate.videoInfo.playType,
                    PlayCode.VIDEO_NOT_EXIST,
                    mMediaPlayerDelegate.videoInfo.mSource,
                    mMediaPlayerDelegate.videoInfo.getCurrentQuality(),
                    mMediaPlayerDelegate.videoInfo.getProgress(),
                    mMediaPlayerDelegate.isFullScreen,
                    mMediaPlayerDelegate.videoInfo, mMediaPlayerDelegate.getPlayVideoInfo());
        } else if (what == 1 && !PlayerUtil.useUplayer(mMediaPlayerDelegate.videoInfo)) {
            Track.onError(mActivity.getApplicationContext(), mMediaPlayerDelegate.videoInfo.getVid(),
                    Profile.GUID, mMediaPlayerDelegate.videoInfo.playType,
                    PlayCode.VIDEO_LOADING_FAIL,
                    mMediaPlayerDelegate.videoInfo.mSource,
                    mMediaPlayerDelegate.videoInfo.getCurrentQuality(),
                    mMediaPlayerDelegate.videoInfo.getProgress(),
                    mMediaPlayerDelegate.isFullScreen,
                    mMediaPlayerDelegate.videoInfo, mMediaPlayerDelegate.getPlayVideoInfo());
        }
    }

    private boolean loadingADOverTime(final IPlayerUiControl playerUiControl, final IPlayerAdControl playerAdControl) {
        if (playerAdControl == null) {
            return true;
        }
        if (playerAdControl.isMidAdShowing() && playerAdControl.getMidAdModel() != null) {
            playerAdControl.getMidAdModel().removeCurrentAdvInfo();
            playerAdControl.setAdState(AdState.REALVIDEO);
        }
        mMediaPlayerDelegate.playVideoWhenADOverTime();
        if (playerUiControl != null) {
            playerUiControl.updatePlugin(PLUGIN_SHOW_NOT_SET);
        }
        if (mMediaPlayerDelegate.pluginManager != null) {
            mActivity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    playerAdControl.dismissInteractiveAD();
                }
            });
        }
        return true;
    }

    private void onLoadingFailError() {
        if (null == mMediaPlayerDelegate)
            return;
        try {
            Track.changeVideoQualityOnError(mActivity);
            Track.mIsChangingLanguage = false;
            Track.setVVEndError(true);
            mMediaPlayerDelegate.onVVEnd();
        } catch (Exception e) {

        }
    }

    private void localStartSetDuration() {
        if (mMediaPlayerDelegate == null
                || mMediaPlayerDelegate.videoInfo == null)
            return;
//        if (StaticsUtil.PLAY_TYPE_LOCAL.equals(mMediaPlayerDelegate.videoInfo
//                .getPlayType())) {
        Logger.d(LogTag.TAG_PLAYER, "视频时间读取成功 :"
                + mMediaPlayerDelegate.mediaPlayer.getDuration());
        // 试看视频和drm视频使用接口返回时长
        if (mMediaPlayerDelegate.videoInfo.trialByTime() ||
                mMediaPlayerDelegate.videoInfo.isDRMVideo())
            return;
        mMediaPlayerDelegate.videoInfo
                .setDurationMills(mMediaPlayerDelegate.mediaPlayer
                        .getDuration());

        mActivity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (mMediaPlayerDelegate.pluginManager != null) {
                    mMediaPlayerDelegate.pluginManager.onVideoInfoGetted();
                }
            }
        });
//        }
    }

    private void sentonVVBegin(final IPlayerUiControl playerUiControl) {
        if (null != mMediaPlayerDelegate) {
            mMediaPlayerDelegate.isStartPlay = true;
            if (null != mMediaPlayerDelegate.videoInfo
                    && !mMediaPlayerDelegate.videoInfo.isFirstLoaded) {
                if (playerUiControl != null) {
                    playerUiControl.updateVideoId(mMediaPlayerDelegate.videoInfo.getVid());
                }
                mMediaPlayerDelegate.videoInfo.isFirstLoaded = true;
                if (!mMediaPlayerDelegate.videoInfo.IsSendVV) {
                    mMediaPlayerDelegate.onVVBegin();
                }
            }
        }
    }

    private void skipHeadOrSeekToHistory(final IPlayerAdControl playerAdControl) {
        if (mMediaPlayerDelegate != null
                && (playerAdControl.getMidAdModel() == null || !playerAdControl.getMidAdModel().isAfterEndNoSeek)) {
            if (mMediaPlayerDelegate.videoInfo != null
                    && mMediaPlayerDelegate.videoInfo.getProgress() > 1000
                    && !mMediaPlayerDelegate.videoInfo.isHLS
                    && mMediaPlayerDelegate.videoInfo.isCached()) {
                mMediaPlayerDelegate.seekTo(mMediaPlayerDelegate.videoInfo.getProgress());
                Logger.d(LogTag.TAG_PLAYER, "SEEK TO" + mMediaPlayerDelegate.videoInfo.getProgress());
            }

            if (Profile.isSkipHeadAndTail()
                    && Profile.PLANTFORM == Plantform.TUDOU
                    && mMediaPlayerDelegate.videoInfo != null
                    && mMediaPlayerDelegate.videoInfo.getProgress() <= 1000
                    && mMediaPlayerDelegate.videoInfo.isHasHead()) {
                mActivity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
//                        Toast.makeText(mActivity, "为您跳过片头", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }
    }

}
