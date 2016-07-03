package com.youku.player.plugin;

import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.baseproject.utils.Logger;
import com.baseproject.utils.Util;
import com.youdo.vo.XAdInstance;
import com.youku.analytics.data.Device;
import com.youku.libmanager.SoUpgradeManager;
import com.youku.libmanager.SoUpgradeService;
import com.youku.player.BaseMediaPlayer;
import com.youku.player.LogTag;
import com.youku.player.Track;
import com.youku.player.ad.AdState;
import com.youku.player.ad.AdType;
import com.youku.player.ad.PreAdTimes;
import com.youku.player.apiservice.ICacheInfo;
import com.youku.player.apiservice.IDanmakuEnable;
import com.youku.player.apiservice.IDownloadApk;
import com.youku.player.apiservice.ILanguageCode;
import com.youku.player.apiservice.IPayCallBack;
import com.youku.player.apiservice.IPlayerAdControl;
import com.youku.player.apiservice.IPlayerUiControl;
import com.youku.player.apiservice.IToast;
import com.youku.player.apiservice.IUserInfo;
import com.youku.player.apiservice.IVideoHistoryInfo;
import com.youku.player.base.Plantform;
import com.youku.player.base.PlayType;
import com.youku.player.base.YoukuBasePlayerActivity;
import com.youku.player.config.MediaPlayerConfiguration;
import com.youku.player.danmaku.DanmakuManager;
import com.youku.player.danmaku.MyGetDanmakuManager;
import com.youku.player.goplay.Profile;
import com.youku.player.goplay.StaticsUtil;
import com.youku.player.goplay.VideoAdvInfo;
import com.youku.player.module.PlayVideoInfo;
import com.youku.player.module.VideoCacheInfo;
import com.youku.player.module.VideoHistoryInfo;
import com.youku.player.module.VideoUrlInfo;
import com.youku.player.p2p.P2pManager;
import com.youku.player.request.OnRequestDoneListener;
import com.youku.player.request.PlayRequest;
import com.youku.player.request.PlayRequests;
import com.youku.player.subtitle.Attachment;
import com.youku.player.subtitle.DownloadedSubtitle;
import com.youku.player.subtitle.SubtitleDownloadThread;
import com.youku.player.subtitle.SubtitleManager;
import com.youku.player.subtitle.SubtitleOperate;
import com.youku.player.unicom.ChinaUnicomManager;
import com.youku.player.util.AdUtil;
import com.youku.player.util.AnalyticsWrapper;
import com.youku.player.util.Constants;
import com.youku.player.util.DisposableStatsUtils;
import com.youku.player.util.PlayCode;
import com.youku.player.util.PlayerPreference;
import com.youku.player.util.PlayerUtil;
import com.youku.player.util.SessionUnitil;
import com.youku.player.util.URLContainer;
import com.youku.statistics.IRVideoWrapper;
import com.youku.uplayer.MPPErrorCode;
import com.youku.uplayer.MediaPlayerProxy;

import java.io.InputStream;
import java.util.List;

/**
 * Class MediaPlayerDelegate
 */
public class MediaPlayerDelegate {

    public static final int PLAY_50 = 50;
    public static final int PLAY_75 = 75;
    public static final int PLAY_100 = 100;
    public static final int PLAY_FULL = -1;

    // 播放信息
    private XAdInstance offlinePrerollAd;//离线广告
    public VideoUrlInfo videoInfo;
    public BaseMediaPlayer mediaPlayer;
    // 播放器容器activity
    private FragmentActivity context;
    public PluginManager pluginManager;
    public boolean onChangeOrient = true;
    public Orientation currentOriention;

    // 广告类型
    public int mAdType = AdType.AD_TYPE_VIDEO;

    // 广告监测发送
    public boolean isAdStartSended = false;
    public boolean isAdEndSended = false;

    // 是否播放完成
    public boolean isComplete = false;
    public boolean isStartPlay;
    public static String playCode = PlayCode.USER_RETURN;

    // 是否全屏
    public boolean isFullScreen = false;

    // 是否DLNA连接
    public boolean isDLNA = false;
    /**
     * 是否在播放广告
     */
    public boolean isADShowing = false;
    public boolean isADInterrupt = false;
    public String nowVid = "";

    // vv是否发送
    public boolean isVVBegin998Send;

    /**
     * 主要为暂停广告服务，因为无法获取到用户主动暂停。所以需要排除 在loading跟seek的时候不显示暂停广告
     */
    public boolean isLoading = false;

    // release时是暂停状态
    public boolean isPause = false;
    public boolean isChangeLan = false;
    public boolean changeQuality = false;

    // 是否调用过play
    public boolean isPlayCalled = false;

    /**
     * 用来标识在seek过程中的pause操作
     */
    public boolean pauseDuringSeek;
    public boolean isSeeking;

    // 挂起时的进度
    private int adPausedPosition = 0;

    private int mPlayRate = DEFAULT_PLAY_RATE;
    public static final int DEFAULT_PLAY_RATE = 10;
    private static final int PLAY_RATE_MAX = 20;
    private static final int PLAY_RATE_MIN = 5;

    private Handler handler = new Handler() {
    };

    // 播放历史还差10秒的时候重播
    public static int mHistoryReplayTime = 10;

    private PlayRequest mPlayRequest;

    // 下载信息接口
    public static ICacheInfo mICacheInfo;

    // 用戶信息
    public static IUserInfo mIUserInfo;

    // 播放历史
    public static IVideoHistoryInfo mIVideoHistoryInfo;

    // 付费信息
    public static IPayCallBack mIPayCallBack;

    //是否有弹幕
    public IDanmakuEnable mIDanmakuEnable;

    // toast接口
    public static IToast mIToast;

    // 播放语言code接口
    public static ILanguageCode mILanguageCode;

    // 用来读取lua
    public static InputStream is;

    // 广告点击地址以.apk结尾则调起下载器下载
    public static IDownloadApk mIDownloadApk;

    // subtitle download thread
    public SubtitleDownloadThread mSubtitleDownloadThread;

    public static void setICacheInfo(ICacheInfo iDownInfo) {
        mICacheInfo = iDownInfo;
    }

    public static void setIUserInfo(IUserInfo iUserInfo) {
        mIUserInfo = iUserInfo;
    }

    public static void setIVideoHistoryInfo(IVideoHistoryInfo iVideoHistoryInfo) {
        mIVideoHistoryInfo = iVideoHistoryInfo;
    }

    public static void setIToast(IToast iToast) {
        mIToast = iToast;
    }

    public static void setILanguageCode(ILanguageCode languageCode) {
        mILanguageCode = languageCode;
    }

    public static void setIDownloadApk(IDownloadApk downloadApk) {
        mIDownloadApk = downloadApk;
    }

    public void setIDanmakuEnable(IDanmakuEnable danmakuEnable) {
        mIDanmakuEnable = danmakuEnable;
    }

    public static VideoUrlInfo getRecordFromLocal(final VideoUrlInfo videoUrlInfo) {
        if (videoUrlInfo.getVid() != null && mIVideoHistoryInfo != null) {
            VideoHistoryInfo videoInfo = mIVideoHistoryInfo.getVideoHistoryInfo(videoUrlInfo.getVid());
            if (videoInfo != null) {
                int playHistory = videoInfo.playTime * 1000;
                if (playHistory > videoUrlInfo.getProgress())
                    videoUrlInfo.setProgress(playHistory);
            }
        }
        Logger.d(LogTag.TAG_PLAYER, "getRecordFromLocal:" + videoUrlInfo.getVid() + " " + videoUrlInfo.getProgress());
        return videoUrlInfo;
    }

    /**
     * 添加到播放历史，包括本地，缓存，和云端播放历史
     *
     * @param videoInfo
     */
    public static void addReleaseHistory(VideoUrlInfo videoInfo) {
        if (mIVideoHistoryInfo == null)
            return;
        mIVideoHistoryInfo.addReleaseHistory(videoInfo);
    }

    public static void addIntervalHistory(VideoUrlInfo videoInfo) {
        if (MediaPlayerDelegate.mIVideoHistoryInfo == null)
            return;
        MediaPlayerDelegate.mIVideoHistoryInfo.addIntervalHistory(videoInfo);
    }

    private IPlayerUiControl mPlayerUiControl;
    private IPlayerAdControl mPlayerAdControl;

    public MediaPlayerDelegate(FragmentActivity context,
                               IPlayerUiControl playerUiControl, IPlayerAdControl playerAdControl) {
        this.context = context;
        this.offlinePrerollAd = null;
        mPlayerUiControl = playerUiControl;
        mPlayerAdControl = playerAdControl;
        this.pluginManager = playerUiControl.getPluginManager();
    }

    /**
     */
    public void pause() {
//        if (getPlayerUiControl() != null && getPlayerUiControl().getDanmakuManager() != null) {
//            getPlayerUiControl().getDanmakuManager().hideDanmaku();
//        }
        pauseNoAd();
        if (mediaPlayer != null && mPlayerAdControl != null
                && isFullScreen && !isLoading) {
            mPlayerAdControl.showPauseAD();
        }
    }

    public void pauseForDLNA() {
        Logger.d(LogTag.TAG_PLAYER, "开始DLNA暂停");
        if (videoInfo != null) {
            Track.pauseForIRVideo(context);
            Track.pause();
            showPauseADForDLNA();
        }
    }

    /**
     * 暂停不显示广告
     */
    public void pauseNoAd() {
        if (mediaPlayer != null && videoInfo != null) {
            if (mPlayerUiControl.getDanmakuManager() != null) {
                mPlayerUiControl.getDanmakuManager().pauseDanmaku();
            }
            if (isSeeking)
                pauseDuringSeek = true;
            mediaPlayer.pause();
            if (mPlayerAdControl.getMidAdModel() != null) {
                mPlayerAdControl.getMidAdModel().timerPause();
            }
            AnalyticsWrapper.playPause(context,
                    Track.getAnalyticsVid(videoInfo));
            Track.pauseForIRVideo(context);
            Track.pause();
        }
    }

    /**
     * 目前的逻辑是缓冲的时候，主动暂停，缓冲完成后，再自动继续播放
     */
    public void loadingPause() {
        if (mediaPlayer != null && videoInfo != null) {
            mediaPlayer.pause();
        }
    }

    public void onStart() {
        pluginManager.onStart();
    }

    public void onPause() {
        pluginManager.onPause();
    }

    private void showPauseADForDLNA() {
        if (isFullScreen) {
            mPlayerAdControl.showPauseAD();
        }
    }

    /**
     * video end
     * <p/>
     * send some vv info
     * Notice* do not call this method except mediaplayerdelegate.
     */
    protected void videoEnd() {
        try {

            if (getPlayRequest() != null)
                getPlayRequest().cancel();
            if (videoInfo != null && videoInfo.isHLS) {
                IRVideoWrapper.videoEnd(context);
                return;
            }

            if (!isStartPlay
                    && !isVVBegin998Send && !isChangeLan) {
                if (videoInfo == null
                        || TextUtils.isEmpty(videoInfo
                        .getVid())) {
                    Track.onError(
                            context,
                            mPlayerUiControl.getVideoId(),
                            Device.guid,
                            StaticsUtil.PLAY_TYPE_NET,
                            PlayCode.USER_RETURN,
                            videoInfo == null ? VideoUrlInfo.Source.YOUKU
                                    : videoInfo.mSource,
                            Profile.videoQuality, 0,
                            isFullScreen, null, getPlayVideoInfo());
                    isVVBegin998Send = true;
                } else if (!videoInfo.IsSendVV
                        && !videoInfo.isSendVVEnd) {
                    DisposableStatsUtils.disposeNotPlayedAd(context, videoInfo, URLContainer.AD_LOSS_STEP3_NEW);
                    if (isADShowing) {
                        Track.onError(context, videoInfo
                                        .getVid(), Device.guid,
                                videoInfo.playType,
                                PlayCode.VIDEO_ADV_RETURN,
                                videoInfo.mSource,
                                videoInfo
                                        .getCurrentQuality(),
                                videoInfo.getProgress(),
                                isFullScreen,
                                videoInfo, getPlayVideoInfo());
                    } else {
                        Track.onError(
                                context,
                                videoInfo.getVid(),
                                Device.guid,
                                PlayerUtil
                                        .isBaiduQvodSource(videoInfo.mSource) ? StaticsUtil.PLAY_TYPE_NET
                                        : videoInfo.playType,
                                PlayCode.USER_LOADING_RETURN,
                                videoInfo.mSource,
                                videoInfo
                                        .getCurrentQuality(),
                                videoInfo.getProgress(),
                                isFullScreen,
                                videoInfo, getPlayVideoInfo());
                    }
                }
            }
            isStartPlay = false;
//          mediaPlayerDelegate.isChangeLan = false;
            if (!isVVBegin998Send) {
                onVVEnd();
            } else {
                videoInfo.isSendVVEnd = true;
            }
        } catch (Exception e) {
        }
    }

    /**
     * 由于release方法不是立刻返回的，所以需要isReleased来判断是否已经释放
     */
    public boolean isReleased = false;

    /**
     * 播放器释放
     */
    public void release() {
        handler.removeCallbacksAndMessages(null);
        isReleased = true;
        pauseDuringSeek = false;
        isLoading = false;
        isSeeking = false;
        P2pManager.getInstance().setUsingP2P(false);
        changeQuality = false;
        if (videoInfo != null) {
            Track.setPlayerStarted(false);
            if (videoInfo.isHLS) {
                Track.pauseForIRVideo(context);
                Track.pause();
            }
        }
        if (mediaPlayer != null) {
            if (!isADShowing) {
                if (videoInfo != null && videoInfo.getProgress() >= 1000
                        && videoInfo.getVid() != null
                        && videoInfo.getTitle() != null
                        && videoInfo.getTitle().length() != 0) {
                    Logger.d(LogTag.TAG_PLAYER, "addToPlayHistory:" + videoInfo.getVid() + " " + videoInfo.getProgress());
                    addReleaseHistory(videoInfo);
                }
            }
            context.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        mediaPlayer.release();
                    }
                    if (pluginManager != null)
                        pluginManager.onRelease();
                    // 解决软硬解切换的问题
//					if (PlayerUtil.useUplayer(videoInfo))
                    mPlayerUiControl.getYoukuPlayerView().recreateSurfaceHolder();
                    if (mPlayerUiControl.getDanmakuManager() != null) {
//                        resetAndReleaseDanmakuInfo();
                    }
                }
            });
            if (mPlayerAdControl.getMidAdModel() != null) {
                mPlayerAdControl.getMidAdModel().resetAfterRelease();
            }

        }
        mPlayerUiControl.hideWebView();
        stopLoopVideo();
        try {
            context.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } catch (Exception e) {

        }

    }

    /**
     * 重置
     */
    public void setFirstUnloaded() {
        if (videoInfo != null) {
            videoInfo.isFirstLoaded = false;
            videoInfo.IsSendVV = false;
        }
    }

    /**
     * 重置播放器
     */
    public void reset() {
        isReleased = false;
        if (mediaPlayer != null)
            mediaPlayer.reset();
    }

    /**
     * seek到某个时间点
     *
     * @param msec 毫秒
     */
    public void seekTo(int msec) {
        if (mPlayerAdControl.isMidAdShowing()) {
            return;
        }
        if (mediaPlayer != null) {
            isLoading = true;
            // mediaPlayer.pause();
            if (goPay(msec))
                return;
            if (mPlayerUiControl.getDanmakuManager() != null) {
                mPlayerUiControl.getDanmakuManager().seekToDanmaku(msec);
            }
            mediaPlayer.seekTo(msec);
            isSeeking = true;
            if (isAdvShowFinished() && videoInfo != null && msec > 1000) {
                videoInfo.setProgress(msec);
            }
            Track.setTrackPlayLoading(false);
        }
    }

    /**
     * 使用这个方法将不会暂停视频
     *
     * @param msec
     */
    public void seekWithoutPause(int msec) {
        if (mediaPlayer != null) {
            if (goPay(msec))
                return;
            isLoading = true;
            mediaPlayer.seekTo(msec);
            // Track.setTrackPlayLoading(false);
        }
    }

    /**
     * 试看视频seek至10分钟以后进入付费页面
     */
    private boolean goPay(int msec) {
        if (isTrialOver(msec)) {
            needPay();
            return true;
        }
        return false;
    }

    public void needPay() {
        if (mIPayCallBack != null && videoInfo != null && videoInfo.mPayInfo != null) {
            release();
            onVVEnd();
            MediaPlayerDelegate.mIPayCallBack.needPay(videoInfo.getVid(), videoInfo.mPayInfo);
        }
    }

    public boolean isTrialOver(int position) {
        return MediaPlayerConfiguration.getInstance().mPlantformController
                .isTrialOver(videoInfo, position);
    }

    /**
     */
    public void setDisplay(SurfaceHolder sh) {
        if (mediaPlayer != null)
            mediaPlayer.setDisplay(sh);
    }

    /**
     * 开始播放
     */
    public void start() {
        Logger.d(LogTag.TAG_PLAYER, "开始播放" /*+ Log.getStackTraceString(new Throwable())*/);

        try {
            context.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } catch (Exception e) {

        }

        if (videoInfo != null && videoInfo.isDRMVideo() && !SoUpgradeManager.getInstance().isSoDownloaded(SoUpgradeService.LIB_DRM_SO_NAME)) {
            Logger.d(LogTag.TAG_PLAYER, "drm downloading return");
            return;
        }

        isReleased = false;
        isPause = false;
        pauseDuringSeek = false;

        int loadingTimeOutData = getLoadingTimeOutByPlayType();
        int prepareTimeOutData = getPreparingTimeOutByPlayType();
        if (mediaPlayer == null) {
            mediaPlayer = BaseMediaPlayer.getInstance();
        }

        isLoading = !(mediaPlayer.isPause() || mediaPlayer.isStatePlay());
        Logger.d(LogTag.TAG_PLAYER, "MediaPlayerDelegate start isLoading=" + isLoading);
        mediaPlayer.setTimeout(Constants.LOAD_TIME_TYPE, loadingTimeOutData);
        mediaPlayer.setTimeout(Constants.PREPARE_AD_CHECK_TIME_TYPE, prepareTimeOutData);

        if (mPlayerUiControl != null && mPlayerUiControl.getDanmakuManager() != null) {
            mPlayerUiControl.getDanmakuManager().resumeDanmaku();
        }
//        if (mPlayerUiControl != null
//                && mPlayerUiControl.getDanmakuManager() != null
//                && !mPlayerUiControl.getDanmakuManager().isDanmakuClosed()) {
//            getPlayerUiControl().getDanmakuManager().showDanmaku();
//        }
        Track.play(videoInfo != null && videoInfo.isHLS, context);
        if (videoInfo != null)
            P2pManager.getInstance().reset(videoInfo.getVid());

        if (mPlayerAdControl != null) {
            mPlayerAdControl.dismissImageAD();
        }
        if (mPlayerAdControl.getMidAdModel() != null && mediaPlayer.isPause()) {
            mPlayerAdControl.getMidAdModel().timerStart();
        }
        if (videoInfo != null
                && StaticsUtil.PLAY_TYPE_LOCAL.equals(videoInfo.playType)
                || Util.hasInternet() && Util.isWifi()) {
            startPlay();
            dismissPauseAD();
            return;
        }

        startPlay();
        dismissPauseAD();

    }

    /**
     * 互动广告使用
     */
    public void startByInteractiveAd() {
        isReleased = false;
        isPause = false;
        isLoading = false;

        if (mediaPlayer == null) {
            mediaPlayer = BaseMediaPlayer.getInstance();
        }
        startPlay();
    }

    /**
     * 互动广告使用
     */
    public void pauseByInteractiveAd() {
        if (mediaPlayer != null) {
            Logger.d(LogTag.TAG_PLAYER, "pause by interactive ad");
            mediaPlayer.pause();
        }
    }

    private int getPreparingTimeOutByPlayType() {
        if (videoInfo == null)
            return Constants.TIME_10_SECONDS;
        boolean isLocal = StaticsUtil.PLAY_TYPE_LOCAL
                .equals(videoInfo.playType);
        return isLocal ? Constants.TIME_5_SECONDS : Constants.TIME_10_SECONDS;
    }

    private int getLoadingTimeOutByPlayType() {
        if (videoInfo == null)
            return Constants.TIME_20_SECONDS;
        boolean isLocal = StaticsUtil.PLAY_TYPE_LOCAL
                .equals(videoInfo.playType);
        return isLocal ? Constants.TIME_5_SECONDS : Constants.TIME_20_SECONDS;
    }

    /**
     * 开始DLNA播放
     */
    public void startForDLNA() {
        Logger.d(LogTag.TAG_PLAYER, "开始DLNA播放");

        Track.play(videoInfo != null && videoInfo.isHLS, context);
        if (mPlayerAdControl != null) {
            mPlayerAdControl.dismissImageAD();
        }
        if (videoInfo != null
                && StaticsUtil.PLAY_TYPE_LOCAL.equals(videoInfo.playType)
                || Util.hasInternet() && Util.isWifi()) {
            dismissPauseAD();
            return;
        }

        dismissPauseAD();
    }

    /**
     * 隐藏暂停广告
     */
    private void dismissPauseAD() {
        mPlayerAdControl.dismissPauseAD();
    }

    /**
     * 播放器开始播放
     */
    private void startPlay() {
        if (mediaPlayer != null) {
            isComplete = false;
            mediaPlayer.videoInfo = videoInfo;
            mediaPlayer.start();
        }
    }

    /**
     * 播放器停止不放
     */
    public void stop() {
        if (mediaPlayer != null)
            mediaPlayer.stop();
    }

    /**
     * 返回播放器是否在播放
     *
     * @return true为在播放
     */
    public boolean isPlaying() {
        // 目前正在加载的情况下，由sdk暂停了视频，等到加载完成后自动续播，此时，需要告诉上层，目前的状态其实是正在播放
        if (isLoading)
            return true;
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    /**
     * 返回当前播放到的时间点
     *
     * @return 当前的时间点 毫秒
     */
    public int getCurrentPosition() {
        if (mediaPlayer != null)
            return mediaPlayer.getCurrentPosition();
        return 0;
    }

    /**
     * 获取视频的高度
     *
     * @return 高度
     */
    public int getVideoHeight() {
        if (mediaPlayer != null)
            return mediaPlayer.getVideoHeight();
        return 0;
    }

    /**
     * 获取视频的宽度
     *
     * @return 宽度
     */
    public int getVideoWidth() {
        if (mediaPlayer != null)
            return mediaPlayer.getVideoWidth();
        return 0;
    }

    /**
     * 获取视频的时长
     *
     * @return 时长
     */
    public int getDuration() {
        try {
            if (mediaPlayer != null)
                return mediaPlayer.getDuration();
        } catch (Exception e) {

        }
        return 0;
    }

    /**
     * 获得广告总时长
     *
     * @return
     */
    public int getAdvDuration() {
        try {
            if (mediaPlayer != null)
                return mediaPlayer.getAdvDuration();
        } catch (Exception e) {
        }
        return 0;
    }

    /**
     * 播放加密视频
     *
     * @param vid
     * @param password
     */
    public void playVideoWithPassword(final String vid, final String password) {
        playVideo(vid, password, false, 0, 0, false, true, false,
                Profile.FORMAT_TUDOU_F4V_480P, null, null, null, null);
    }

    /**
     * 播放视频
     *
     * @param vid
     */
    public void playVideo(final String vid) {
        playVideo(vid, false, false);
    }

    /**
     * 播放视频
     *
     * @param vid
     * @param playlistId
     */
    public void playVideo(final String vid, final String playlistId) {
        playVideo(vid, null, false, 0, 0, false, true, false,
                Profile.FORMAT_TUDOU_F4V_480P, null, playlistId, null, null);
    }

    /**
     * 广告预览使用
     *
     * @param id
     */
    public void playVideoAdvext(final String id, final String adext,
                                final String adMid, final String adPause) {
        PlayVideoInfo playVideoInfo = new PlayVideoInfo.Builder(id).setAdExt(adext).setAdMid(adMid).setAdPause(adPause).build();
        playVideo(playVideoInfo);
    }

    /**
     * 土豆广告预览使用
     *
     * @param id
     */
    public void playVideoAdvext(final String id, final String adext,
                                final String adMid, final String adPause, final String password,
                                final boolean isCache, final int point, int videoStage,
                                final boolean noAdv, final boolean isFromYouku,
                                final boolean isTudouAlbum, int tudouquality,
                                final String playlistCode, final String playlistId,
                                final String albumID, final String languageCode) {
        PlayVideoInfo playVideoInfo = new PlayVideoInfo.Builder(id).setAdExt(adext).setAdMid(adMid).setAdPause(adPause)
                .setPassword(password).setCache(isCache).setPoint(point).setVideoStage(videoStage).setNoAdv(noAdv)
                .setFromYouku(isFromYouku).setTudouAlbum(isTudouAlbum).setTudouQuality(tudouquality).setPlaylistCode(playlistCode)
                .setPlaylistId(playlistId).setAlbumID(albumID).setLanguageCode(languageCode).build();

        playVideo(playVideoInfo);
    }

    /**
     * 播放土豆视频
     *
     * @param itemCode     视频的id
     * @param tudouquality 视频的清晰度
     * @param noadv        无广告
     */
    public void playTudouVideo(final String itemCode, int tudouquality,
                               boolean noadv) {
        playVideo(itemCode, false, 0, 0, noadv, false, false, tudouquality,
                null);
    }

    /**
     * 播放土豆视频
     *
     * @param itemCode     视频的id
     * @param tudouquality 视频的清晰度
     * @param languageCode 视频的languageCode
     */
    public void playTudouVideo(final String itemCode, int tudouquality,
                               String languageCode, boolean noadv) {
        playVideo(itemCode, false, 0, 0, noadv, false, false, tudouquality,
                languageCode);
    }

    /**
     * 播放土豆视频
     *
     * @param itemCode     视频的id
     * @param password     视频的password
     * @param tudouquality 视频的清晰度
     */
    public void playTudouVideo(final String itemCode, final String password,
                               int tudouquality, int point, boolean noadv) {
        playVideo(itemCode, null, false, point, 0, noadv, false, false,
                tudouquality, null, null, null, null);
    }

    /**
     * 播放土豆视频
     *
     * @param itemCode     视频的id
     * @param tudouquality 视频的清晰度
     */
    public void playTudouVideo(final String itemCode, int tudouquality,
                               int point, final String playlistCode, final String languageCode,
                               boolean noadv) {
        playVideo(itemCode, null, false, point, 0, noadv, false, false,
                tudouquality, playlistCode, null, null, languageCode);
    }

    /**
     * 重播土豆视频
     *
     * @param itemCode     视频的id
     * @param tudouquality 视频的清晰度
     */
    public void replayTudouVideo(final String itemCode, int tudouquality,
                                 boolean noadv) {
        playVideo(itemCode, false, -1, 0, noadv, false, false, tudouquality,
                null);
    }


    /**
     * 重播视频
     */
    public void replayVideo(final int autoPlay) {
        release();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                videoInfo.IsSendVV = false;
                videoInfo.isSendVVEnd = false;
                videoInfo.videoAdvInfo = null;
                videoInfo.isFirstLoaded = false;
                videoInfo.setReplay(true);
                videoInfo.setProgress(0);
                pluginManager.onReplay();
                getPlayVideoInfo().autoPlay = autoPlay;
                start();
            }
        }, 100);
    }

    public void playLocalVideo(final String vid, String url, String title,
                               int progress, boolean isWaterMark) {
        playLocalVideo(vid, url, title, progress, isWaterMark, 0);
    }

    public void playLocalVideo(final String vid, String url, String title,
                               int progress, boolean isWaterMark, int type) {
        Logger.d(LogTag.TAG_PLAYER, "playLocalVideo:" + url);

        PlayVideoInfo playVideoInfo = new PlayVideoInfo.Builder(vid).setUrl(url).setTitle(title).setPoint(progress).setCache(true)
                .setWaterMark(isWaterMark).setWaterMarkType(type).setPlayType(PlayType.LOCAL_DOWNLOAD).setLocal(true).build();

        playVideo(playVideoInfo);
    }

    /**
     * 是否为离线广告
     *
     * @return
     */
    public boolean isOfflinePrerollAd() {
        return (offlinePrerollAd != null);
    }

    /**
     * 获取离线前贴广告
     */
    public XAdInstance getOfflinePrerollAd() {
        return offlinePrerollAd;
    }

    /**
     * 播放本地存储的视频
     *
     * @param url      本地视频地址
     * @param title    视频标题
     * @param progress 播放进度
     */
    public void playLocalVideo(String url, String title, int progress) {
        PlayVideoInfo playVideoInfo = new PlayVideoInfo.Builder(url).setTitle(title).setPoint(progress).setPlayType(PlayType.LOCAL_USER_FILE).build();
        playVideo(playVideoInfo);
    }


    /**
     * 重播本地视频
     *
     * @param vid
     * @param url
     */
    public void replayLocalVideo(final String vid, String url, String title, boolean isWaterMark, int type) {
        resetAndReleaseDanmakuInfo();
        mPlayerUiControl.initDanmakuManager("", 0, true);
        VideoUrlInfo videoInfo = new VideoUrlInfo();
        videoInfo.setVid(vid);
        videoInfo.cachePath = url;
        videoInfo.setTitle(title);
        videoInfo.playType = StaticsUtil.PLAY_TYPE_LOCAL;
        videoInfo.setProgress(0);
        videoInfo.setCached(true);
        videoInfo.isLocalWaterMark = isWaterMark;
        for (int i = 0; i < 5; i++) {
            videoInfo.waterMarkType[i] = type;
        }
        if (mICacheInfo != null) {
            VideoCacheInfo videoCacheInfo = mICacheInfo.getDownloadInfo(vid);
            if (videoCacheInfo != null) {
                videoInfo.setShowId(videoCacheInfo.showid);
                videoInfo.nextVideoId = videoCacheInfo.nextVid;
                videoInfo.setShow_videoseq(videoCacheInfo.show_videoseq);
                videoInfo.setimgurl(videoCacheInfo.picUrl);
                videoInfo.setVideoLanguage(videoCacheInfo.language);
                videoInfo.setCurrentVideoQuality(videoCacheInfo.quality);
                videoInfo.setItem_img_16_9(videoCacheInfo.picUrl);
                videoInfo.setEpisodemode(videoCacheInfo.episodemode);
                videoInfo.setMediaType(videoCacheInfo.mMediaType);
                videoInfo.savePath = videoCacheInfo.savePath;
                videoInfo.setRegisterNum(videoCacheInfo.registerNum);
                videoInfo.setLicenseNum(videoCacheInfo.licenseNum);
                videoInfo.setVerticalVideo(videoCacheInfo.isVerticalVideo);
                videoInfo.setExclusiveLogo(videoCacheInfo.exclusiveLogo);
            }
        }
        this.videoInfo = videoInfo;
        pluginManager.onVideoInfoGetted();
        pluginManager.onChangeVideo();
        context.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mPlayerUiControl.goFullScreen();
            }
        });
        videoInfo.setProgress(0);
        AnalyticsWrapper.playRequest(context, vid, videoInfo.playType);

        prepareSubtitle(vid);

        start();
    }

    /**
     * @param vid
     * @param isCache
     * @param point   -1 代表不读取播放历史
     */
    public void playVideo(final String vid, final boolean isCache,
                          final int point) {
        playVideo(vid, isCache, point, 0, false, true, false, -1, null);
    }

    public void playVideo(final String vid, final boolean isCache) {
        playVideo(vid, isCache, 0, false);
    }

    public void playVideo(final String vid, final boolean isCache, boolean noAdv) {
        playVideo(vid, isCache, 0, noAdv);
    }


    // 有播放版权
    public boolean hasRight = true;

    public void finishActivity() {
        isStartPlay = false;
//        context.finish();
    }

    public void goSmall() {
        mPlayerUiControl.goSmall();
    }

    public void goFullScreen() {
        mPlayerUiControl.goFullScreen();
    }

    public void goVerticalFullScreen() {
        mPlayerUiControl.goVerticalFullScreen();
    }

    public void playCompleteGoSmall() {
        mPlayerUiControl.playCompleteGoSmall();
    }

    public void setOrientionDisable() {
        mPlayerUiControl.setOrientionDisable();
    }

    public void setOrientionEnable() {
        mPlayerUiControl.setOrientionEnable();
    }

    public void onEnd() {
        if (!PlayerUtil.isYoukuTablet(context))
            context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public void clearEnd() {
        if (null != videoInfo
                && (StaticsUtil.PLAY_TYPE_LOCAL.equals(videoInfo.playType) || videoInfo.isHLS))
            return;
        if (null == context)
            return;
        if (!PlayerUtil.isYoukuTablet(context))
            context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    public void retry() {
        seekToPausedADShowing(getAdPausedPosition());
    }

    /**
     * 播放器挂起后重新加载，广告的续播处理
     *
     * @param position 挂起时的播放位置
     */
    public void seekToPausedADShowing(final int position) {
        if (isADShowing) {
            // 给临界值一个缓冲，让正片的播放器能够起来
            if (getAdvDuration() - position >= 2000) {
                seekTo(position);
            } else {
                seekTo(Math.max(position - 1000, 0));
            }
        }
    }

    public void onComplete() {
        if (goPay(getDuration()))
            return;
        isComplete = true;
        Track.setplayCompleted(true);
        isStartPlay = false;
        onVVEnd();
        context.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                pluginManager.onCompletionListener();
            }
        });
    }

    public void onVVBegin() {
        if (TextUtils.isEmpty(videoInfo.getVid()))
            return;
        // Track.setTrackPlayLoading(true);
        // Track.onLoadingToPlayEnd();
        MediaPlayerDelegate.playCode = PlayCode.PLAY_SUCC;
        if (isChangeLan) {
            isChangeLan = false;
            Track.mIsChangingLanguage = false;
            videoInfo.isSendVVEnd = false;
            return;
        }
        if (videoInfo.IsSendVV) {
            return;
        }
        videoInfo.isSendVVEnd = false;
        if (videoInfo.isHLS && Profile.PLANTFORM == Plantform.YOUKU) {
            Track.onPlayHlsStart(context, videoInfo.getVid(),
                    getUserID(), videoInfo.mLiveInfo.isPaid,
                    videoInfo.sid, videoInfo.bps, videoInfo.mLiveInfo.autoplay,
                    videoInfo.mLiveInfo.areaCode, videoInfo.mLiveInfo.dmaCode,
                    videoInfo.oip, Profile.ctypeHLS, Profile.ev,
                    videoInfo.token, videoInfo.mLiveInfo.isVip, isFullScreen,
                    MediaPlayerConfiguration.getInstance().getVersionCode(),
                    MediaPlayerConfiguration.getInstance().mTestid,
                    MediaPlayerConfiguration.getInstance().mIstest);
        } else {
            Track.onPlayStart(context, videoInfo, isFullScreen,
                    MediaPlayerConfiguration.getInstance().getVersionCode(),
                    MediaPlayerConfiguration.getInstance().mTestid,
                    MediaPlayerConfiguration.getInstance().mIstest, getPlayVideoInfo());
        }

        videoInfo.IsSendVV = true;
    }

    public void onVVEnd() {
        if (Track.mIsChangingLanguage || Track.isTrackChangeVideoQualtiy()) {
            return;
        }
        if (videoInfo == null || videoInfo.isSendVVEnd) {
            return;
        }
//        Logger.d(LogTag.TAG_STATISTIC, "onVVEnd videoInfo:" + videoInfo.getVid() + Log.getStackTraceString(new Throwable()));
        if (videoInfo != null && !TextUtils.isEmpty(videoInfo.getVid())) {
            try {
                videoInfo.isSendVVEnd = true;
                Track.onPlayEnd(context, videoInfo, isFullScreen,
                        MediaPlayerConfiguration.getInstance().getVersionCode(),
                        MediaPlayerConfiguration.getInstance().mTestid,
                        MediaPlayerConfiguration.getInstance().mIstest);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void playVideo(final String vid, final boolean isCache,
                          final int point, final boolean noAdv) {
        playVideo(vid, isCache, point, 0, noAdv, true, false, -1, null);
    }

    public void playTudouVideo(final String vid, final boolean isCache,
                               final int point, final boolean noAdv, int tudouquality) {
        playVideo(vid, isCache, point, 0, noAdv, false, false, tudouquality,
                null);
    }

    public void playVideoWithOutAdv(final String vid, int point) {
        playVideo(vid, false, point, true);
    }

    public void playVideoWithStage(String id, boolean isCache, int point,
                                   int videoStage) {
        playVideo(id, isCache, point, videoStage, false, true, false, -1, null);
    }

    public void playVideoWithStageTudou(String id, boolean isCache, int point,
                                        int videoStage) {
        playVideo(id, isCache, point, videoStage, false, false, true,
                Profile.FORMAT_TUDOU_F4V_480P, null);
    }

    public void playVideo(final String vid, final boolean isCache,
                          final int point, int videoStage, final boolean noAdv,
                          boolean isFromYouku, final boolean isTudouAlbum, int tudouquality,
                          String languageCode) {
        playVideo(vid, null, isCache, point, videoStage, noAdv, isFromYouku,
                isTudouAlbum, tudouquality, null, null, null, languageCode);
    }

    /**
     * 通过itemCode和视频的password播放加密视频
     *
     * @param itemCode
     * @param password
     */
    public void playTudouVideoWithPassword(final String itemCode,
                                           final String password) {
        playVideo(itemCode, password, false, 0, 0, false, false, false,
                Profile.FORMAT_TUDOU_F4V_480P, null, null, null, null);
    }

    /**
     * @param vid
     * @param password     加密视频的password，非加密视频传入null
     * @param isCache
     * @param point        -1代表不读取播放历史
     * @param videoStage   剧集集数
     * @param noAdv        不播放广告
     * @param isFromYouku  用于区分土豆还是youku
     * @param isTudouAlbum 是否是土豆的剧集
     * @param tudouquality 土豆播放单个视频时候需要清晰度
     */
    public void playVideo(final String vid, final String password,
                          final boolean isCache, final int point, int videoStage,
                          boolean noAdv, final boolean isFromYouku,
                          final boolean isTudouAlbum, int tudouquality,
                          final String playlistCode, final String playlistId,
                          final String albumID, final String languageCode) {

        PlayVideoInfo playVideoInfo = new PlayVideoInfo.Builder(vid).setPassword(password).setCache(isCache).setPoint(point).setVideoStage(videoStage)
                .setNoAdv(noAdv).setFromYouku(isFromYouku).setTudouAlbum(isTudouAlbum).setTudouQuality(tudouquality).setPlaylistCode(playlistCode)
                .setPlaylistId(playlistId).setAlbumID(albumID).setLanguageCode(languageCode).setLocal(false).build();
        playVideo(playVideoInfo);
    }

    private void sendVVAdvReturn() {
        if (videoInfo != null && !videoInfo.isAdvEmpty()) {
            Track.onError(context, videoInfo.getVid(), Device.guid, videoInfo.playType, PlayCode.VIDEO_ADV_RETURN,
                    videoInfo.mSource, videoInfo.getCurrentQuality(), videoInfo.getProgress(), isFullScreen, videoInfo,
                    getPlayVideoInfo());
        }
    }

    /**
     * 重置VideoInfo信息，会释放播放器
     */
    private void resetVideoInfoAndRelease() {
        if (mPlayerUiControl != null) {
            mPlayerUiControl.clearSubtitle();
            mPlayerUiControl.onDownloadSubtitle(null, -1);
        }
        mPlayRate = DEFAULT_PLAY_RATE;
        release();
        resetAndReleaseDanmakuInfo();
        Track.setTrackChangeVideoQualtiy(false);
        mAdType = AdType.AD_TYPE_VIDEO;
        if (videoInfo != null) {
            if (!isChangeLan) {
                onVVEnd();
                IRVideoWrapper.videoEnd(context);
                Track.mIsChangingLanguage = false;
                videoInfo.clear();
            } else {
//                isChangeLan = false;
                Track.mIsChangingLanguage = true;
            }
            isStartPlay = false;
            if (mPlayerAdControl != null) {
                mPlayerAdControl.clearMidAD();
                mPlayerAdControl.setImageAdShowing(false);
            }
            isVVBegin998Send = false;
            isAdStartSended = false;
            isAdEndSended = false;
            isADInterrupt = false;
        }
    }

    public void resetAndReleaseDanmakuInfo() {
        if (mPlayerUiControl != null && mPlayerUiControl.getDanmakuManager() != null) {
            mPlayerUiControl.getDanmakuManager().resetAndReleaseDanmakuInfo();
        }
    }

    public void setDanmakuPosition(int position) {
        if (mPlayerUiControl != null && mPlayerUiControl.getDanmakuManager() != null) {
            mPlayerUiControl.getDanmakuManager().setDanmakuPosition(position);
        }
    }

    public void setDanmakuEffect(int effect) {
        if (mPlayerUiControl != null && mPlayerUiControl.getDanmakuManager() != null) {
            mPlayerUiControl.getDanmakuManager().setDanmakuEffect(effect);
        }
    }

    /**
     * 获取播放地址，成功后通过prepareAndStartPlayVideo进行播放
     *
     * @param playVideoInfo
     */
    public void getVideoUrlInfo(final PlayVideoInfo playVideoInfo) {
        Logger.d(LogTag.TAG_PLAYER, "开始获取播放地址信息");

        mPlayRequest = PlayRequests.newPlayRequest(playVideoInfo, this, context);
        mPlayRequest.playRequest(playVideoInfo, new OnRequestDoneListener() {
            @Override
            public void onRequestDone(VideoUrlInfo videoUrlInfo, VideoAdvInfo advInfo) {
                if (videoUrlInfo != null) {
                    prepareAndStartPlayVideo(videoUrlInfo, advInfo);
                }
            }
        });
    }

    public void getVideoInfoSuccess(VideoUrlInfo videoUrlInfo, PlayVideoInfo playVideoInfo) {
        if (context.isFinishing()) {
            Logger.d(LogTag.TAG_PLAYER,
                    "handleSuccessfullyGetVideoUrl  activity is finish, return");
            return;
        }

        // 在线竖屏联播到本地横屏视频的情况下去小屏防止横屏视频竖屏全屏显示
        if (mPlayerUiControl.isVerticalFullScreen() && !videoUrlInfo.isVerticalVideo()
                && StaticsUtil.PLAY_TYPE_LOCAL.equals(videoUrlInfo.getPlayType())) {
            mPlayerUiControl.goSmall();
        }

        // 获取video视频成功时，需要重新获取videoId:当点击剧集播放，只知道剧集id,不清楚视频id。
        if (videoUrlInfo.getAdPoints() != null
                && !videoUrlInfo.getAdPoints().isEmpty()) {
            mPlayerAdControl.setMidADInfo(videoUrlInfo.getAdPoints(), playVideoInfo.getAdMid());
        }
        mPlayerAdControl.setPauseTestAd(playVideoInfo.getAdPause());
        handleSuccessfullyGetVideoUrl(videoUrlInfo);
        if (videoUrlInfo.isCached()) {
            mPlayerUiControl.initDanmakuManager("", 0, true);
            return;
        } else {
            mPlayerUiControl.initDanmakuManager(videoUrlInfo.getVid(), videoUrlInfo.getCid(), false);
        }
        Logger.d(LogTag.TAG_DANMAKU, "itemId=" + videoUrlInfo.getVid());
        Logger.d(LogTag.TAG_DANMAKU, "进入视频播放，第一请求第" + videoUrlInfo.getProgress() / 60000 + "分钟的数据");

        if (videoUrlInfo.isCached() || Profile.getDanmakuSwith(context)) {
            Logger.d(LogTag.TAG_DANMAKU, "缓存视频或者弹幕开关关闭，不请求弹幕数据");
            ((DanmakuManager) mPlayerUiControl.getDanmakuManager()).isFirstOpen = true;
            return;
        }
        mPlayerUiControl.getDanmakuManager().handleDanmakuInfo(videoUrlInfo.getVid(),
                videoUrlInfo.getProgress() / 60000, 1);
    }

    public void submitDanmaku(String ver, String iid, String playat,
                              String propertis, String content) {
        final MyGetDanmakuManager myGetDanmakuManager = new MyGetDanmakuManager();
        myGetDanmakuManager.submitDanmaku(ver, iid, playat, propertis, content,
                context);
    }

    private void handleSuccessfullyGetVideoUrl(final VideoUrlInfo videoUrlInfo) {
        videoUrlInfo.mVideoFetchTime = SystemClock.elapsedRealtime();

        // Track.play();
        Logger.d(LogTag.TAG_PLAYER, "播放信息获取成功");

        //download subtitle if has subtitle
        if (mSubtitleDownloadThread != null) {
            mSubtitleDownloadThread.stopSelf();
            mSubtitleDownloadThread = null;
        }

        mPlayerUiControl.clearSubtitle();
        List<Attachment> attachments = videoUrlInfo.getAttachments();
        if (attachments != null && attachments.size() > 0) {
            Logger.d(SubtitleManager.TAG, "handleSuccessfullyGetVideoUrl : downloadSubtitles");
            downloadSubtitles(attachments, videoUrlInfo.getVid());
        } else {
            Logger.d(SubtitleManager.TAG, "handleSuccessfullyGetVideoUrl : no subtitle");
            mPlayerUiControl.onDownloadSubtitle(null, -1);
        }
    }

    private void prepareAndStartPlayVideo(final VideoUrlInfo videoUrlInfo, VideoAdvInfo videoAdvInfo) {
        if (!isVideoInfoStartToPlay(videoUrlInfo)) {
            Logger.d(LogTag.TAG_PLAYER, "new video is started, this will stop");
            return;
        }
        changeQuality = false;
        hasRight = true;
        boolean isShowImageAD = !AdUtil.isAdvVideoType(videoAdvInfo);
        if (!isShowImageAD)
            videoUrlInfo.videoAdvInfo = videoAdvInfo;
        videoUrlInfo.addAdvToCachePathIfNecessary();
        // 这里已经得到时间了
        videoInfo = videoUrlInfo;
        if (isChangeLan) {
            videoInfo.IsSendVV = true;
            videoInfo.isSendVVEnd = false;
            isChangeLan = false;
            // Track.onLoadingToPlayEnd();
        }

        if (mediaPlayer != null)
            mediaPlayer.videoInfo = videoInfo;

        // 如果正在播放就不通知onVideoInfoGetted，防止一直loading
        if (mediaPlayer == null || !mediaPlayer.isPlaying()) {
            pluginManager.onVideoInfoGetted();
            pluginManager.onChangeVideo();
        }

        if (isShowImageAD) {
            mAdType = AdType.AD_TYPE_IMAGE;
            mPlayerAdControl.showImageAD(videoAdvInfo);
        } else {
            mAdType = AdType.AD_TYPE_VIDEO;
            if (MediaPlayerConfiguration.getInstance().showLoginDialog() && !videoInfo.isAdvEmpty()
                    && !PlayerUtil.isLogin()) {
                PreAdTimes.times++;
            }
            startPlayAfterImageAD();
        }
    }

    public void startPlayAfterImageAD() {

        if (!mPlayerUiControl.isOnPause()) {
            mPlayerUiControl.initPlayerPart();
            /**
             * addCallback() can not invoke surfaceCreated, if addCallback()
             * happened after onResume, it will not work. so call surfaceCreated directly
             */
//            if (mediaPlayer != null && mediaPlayer.getSurfaceHolder() == null
//                    && MediaPlayerProxy.isUplayerSupported()) {
//                mediaPlayer.surfaceCreated(mPlayerUiControl.getYoukuPlayerView().surfaceView.getHolder());
//            }
            // 3g允许播放
            if (isPause && isADShowing) {
                isPause = false;
            } else {
                if (Util.hasInternet()
                        && !Util.isWifi()
                        && !StaticsUtil.PLAY_TYPE_LOCAL
                        .equals(videoInfo.playType)
                        && !PlayerPreference.getPreferenceBoolean(
                        "allowONline3G", MediaPlayerConfiguration.getInstance().defaultAllow3G())) {
                    if (mPlayerAdControl != null) {
                        mPlayerAdControl.dismissImageAD();
                    }
                    Track.onError(context, mPlayerUiControl.getVideoId(), Profile.GUID,
                            StaticsUtil.PLAY_TYPE_NET, PlayCode.USER_RETURN,
                            videoInfo.mSource, videoInfo.getCurrentQuality(),
                            0, isFullScreen, videoInfo, getPlayVideoInfo());
                } else
                    start();
            }
        } else {
            mPlayerUiControl.setPauseBeforeLoaded(true);
        }
    }

    /**
     * 当前视频的广告是否不存在或已经播放完毕
     */
    public boolean isAdvShowFinished() {
        if (isADInterrupt) {
            return true;
        }

        if (isADShowing) {
            return false;
        }

        if (mPlayerAdControl != null && mPlayerAdControl.isImageAdShowing()) {
            return false;
        }

        if (videoInfo == null) {
            return true;
        }

        return videoInfo.isAdvEmpty();
    }

    public void seekToHistory() {
        // 低端机 或 硬解播放本地存储视频 需要seek到播放历史
        if ((!MediaPlayerProxy.isUplayerSupported() || videoInfo
                .isNeedLoadedNotify()) && videoInfo.getProgress() > 1000) {
            seekTo(videoInfo.getProgress());
        }
    }

    public void playVideoWhenADOverTime() {
        videoInfo.videoAdvInfo = null;
        isADShowing = false;
        // 重新设置播放地址，去除广告
        if (videoInfo.isCached()) {
            ICacheInfo download = MediaPlayerDelegate.mICacheInfo;
            if (download != null) {
                if (download.isDownloadFinished(videoInfo.getVid())) {
                    VideoCacheInfo downloadInfo = download
                            .getDownloadInfo(videoInfo.getVid());
                    if (YoukuBasePlayerActivity.isHighEnd) {
                        videoInfo.cachePath = PlayerUtil
                                .getM3u8File(downloadInfo.savePath
                                        + "youku.m3u8");
                    }
                }
            }
        }
        try {
            release();
            if (!mPlayerUiControl.isOnPause()) {
                // 3g允许播放
                if (isPause && isADShowing) {
                    isPause = false;
                } else {
                    if (pluginManager != null && context != null) {
                        context.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                pluginManager.onVideoInfoGetting();
                                pluginManager.onVideoInfoGetted();
                                pluginManager.onLoading();
                            }
                        });
                    }
                    start();
                }

                mPlayerUiControl.initPlayerPart();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean isPlayLocalType() {
        if (videoInfo == null) {
            return false;
        }
        return StaticsUtil.PLAY_TYPE_LOCAL.equals(videoInfo.playType);
    }

    /**
     * 切换清晰度
     *
     * @param quality
     */
    public void changeVideoQuality(int quality) {
        if (isPause || (mediaPlayer != null && mediaPlayer.isPause())) {
            changeVideoQualityByRestart(quality);
        } else {
            changeVideoQualitySmooth(quality);
        }
    }

    public void changeVideoQualityByRestart(int quality) {
        Logger.d(LogTag.TAG_PLAYER, "changeVideoQuality:" + quality);
        Profile.setVideoQuality(quality);
        pluginManager.onLoading();
        Track.onChangeVideoQualityStart(context);
        Track.setTrackPlayLoading(false);
        release();
        changeQuality = true;
        start();
    }

    /**
     * 平滑切换清晰度
     *
     * @param quality
     */
    private void changeVideoQualitySmooth(int quality) {
        Logger.d(LogTag.TAG_PLAYER, "changeVideoQuality:" + quality);
        Track.onSmoothChangeVideoQualityStart(Profile.videoQuality, quality);
        Profile.setVideoQuality(quality);
        if (mediaPlayer != null && videoInfo != null) {
            mediaPlayer.switchDataSource(videoInfo.getUrl());
        }
        MediaPlayerConfiguration.getInstance().mPlantformController.onQualitySmoothChangeStart(mPlayerUiControl, quality);
    }

    /**
     * 切换语言
     *
     * @param languageCode
     */
    public void changeVideoLanguage(String languageCode) {
        Logger.d(LogTag.TAG_PLAYER, "changeVideoLanguage:" + languageCode);
        isChangeLan = true;
        Profile.langCode = languageCode;
        playVideo(videoInfo.getVid(), null, false, videoInfo.getProgress(), 0, true, true,
                false, -1, null, videoInfo.playlistId, null, null);
    }

    public void playTudouAlbum(String albumID, boolean noadv) {
        playVideo(albumID, false, 0, 0, noadv, false, true, -1, null);
    }

    public void playTudouAlbum(String albumID, int point, String languageCode,
                               boolean noadv) {
        playVideo(albumID, false, point, 0, noadv, false, true, -1,
                languageCode);
    }

    public void replayTudouAlbum(String albumID, boolean noadv) {
        playVideo(albumID, false, -1, 0, noadv, false, true, -1, null);
    }

    /**
     * 获取userid
     */
    public static String getUserID() {
        if (mIUserInfo != null)
            return mIUserInfo.getUserID();
        else
            return "";
    }

    public void setVideoOrientation(int orientation) {
        if (mediaPlayer != null) {
            mediaPlayer.setVideoOrientation(orientation);
            mPlayerUiControl.getYoukuPlayerView().setOrientation(orientation);
            mPlayerUiControl.getYoukuPlayerView().resizeMediaPlayer(true);
        }
    }

    /**
     * 设置播放速率
     *
     * @param rate 取值范围是5到20，5代表0.5倍速度播放  20代表2倍速度播放
     */
    public void setPlayRate(int rate) {
        if (rate < PLAY_RATE_MIN || rate > PLAY_RATE_MAX)
            throw new IllegalArgumentException();
        if (mediaPlayer != null) {
            Logger.d("PlayFlow", "setPlayRate:" + rate);
            mediaPlayer.setPlayRate(rate);
        }
    }

    public void increasePlayRate(int step) {
        mPlayRate += step;
        if (mPlayRate > PLAY_RATE_MAX)
            mPlayRate = PLAY_RATE_MAX;
        setPlayRate(mPlayRate);
    }

    public void decreasePlayRate(int step) {
        mPlayRate -= step;
        if (mPlayRate < PLAY_RATE_MIN)
            mPlayRate = PLAY_RATE_MIN;
        setPlayRate(mPlayRate);
    }

    public int getPlayRate() {
        return mPlayRate;
    }

    /**
     * 是否使用umediaplayer，在OnPrepared之后再调用
     */
    public boolean isUsingUMediaplyer() {
        if (mediaPlayer != null) {
            return mediaPlayer.isUsingUMediaplayer();
        } else
            return MediaPlayerProxy.isUplayerSupported();
    }

    /**
     * 直播
     *
     * @param liveid
     */
    public void playHLS(String liveid) {
        PlayVideoInfo playVideoInfo = new PlayVideoInfo.Builder(liveid).setPlayType(PlayType.LIVE).build();
        playVideo(playVideoInfo);
    }

    private boolean isVideoInfoStartToPlay(VideoUrlInfo videoUrlInfo) {
        if (videoUrlInfo == null) {
            return false;
        }
        return nowVid.equalsIgnoreCase(videoUrlInfo.getVid())
                || nowVid.equalsIgnoreCase(videoUrlInfo.getShowId())
                || nowVid.equalsIgnoreCase(videoUrlInfo.getRequestId());
    }

    /**
     * 切换解码方式
     *
     * @param isHardware 是否使用硬解
     */
    public void changeDecodeMode(boolean isHardware) {
        Logger.d(LogTag.TAG_PLAYER, "changeDecodeMode:" + isHardware);
        pluginManager.onLoading();
        Profile.setHardwareDecode(isHardware);
        release();
        mPlayerUiControl.getYoukuPlayerView().recreateSurfaceHolder();
        start();
    }

    //处理字幕下载消息
    public Handler subtitleHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SubtitleDownloadThread.SUBTITLE_DOWNLOAD_SUCCESS:
                    DownloadedSubtitle subtitle = (DownloadedSubtitle) msg.obj;
                    mPlayerUiControl.onDownloadSubtitle(subtitle, SubtitleManager.ONLINE_TYPE);
                    break;

                case SubtitleDownloadThread.SUBTITLE_DOWNLOAD_FAIL:

                    break;
            }
        }

    };

    /**
     * 下载字幕文件
     */
    public void downloadSubtitles(List<Attachment> attachments, String name) {

        mSubtitleDownloadThread = new SubtitleDownloadThread(context, subtitleHandler, name);

        mSubtitleDownloadThread.setTask(attachments);

        mSubtitleDownloadThread.start();
    }

    /**
     * 如果有缓存字幕则准备字幕
     */
    public void prepareSubtitle(String vid) {
        mPlayerUiControl.clearSubtitle();
        if (mICacheInfo != null) {
            VideoCacheInfo videoCacheInfo = mICacheInfo.getDownloadInfo(vid);
            if (videoCacheInfo != null) {
                List<DownloadedSubtitle> subtitles = SubtitleOperate.getSubtitles(videoCacheInfo.savePath, vid);
                for (DownloadedSubtitle subtitle : subtitles) {
                    SubtitleManager.addSubtitle(subtitle);
                    mPlayerUiControl.onDownloadSubtitle(subtitle, SubtitleManager.LOCAL_TYPE);
                }
            }
        }
    }

    /**
     * 重新加载播放信息
     */
    public void rePlayWoVedio() {
        ChinaUnicomManager.initChinaUnicomSDK();
        pluginManager.onLoading();
        Track.setTrackPlayLoading(false);
        release();
        start();
    }

    public int getAdPausedPosition() {
        return adPausedPosition;
    }

    public void setAdPausedPosition(int time) {
        adPausedPosition = time;
    }

    public void enableVoice(int enable) {
        if (mediaPlayer != null)
            mediaPlayer.enableVoice(enable);
    }

    public int getVoiceStatus() {
        if (mediaPlayer != null)
            return mediaPlayer.getVoiceStatus();
        return 1;
    }

    public IPlayerAdControl getPlayerAdControl() {
        return mPlayerAdControl;
    }

    public IPlayerUiControl getPlayerUiControl() {
        return mPlayerUiControl;
    }

    public PlayRequest getPlayRequest() {
        return mPlayRequest;
    }

    public void playVideo(final PlayVideoInfo playVideoInfo) {
        if (getPlayerUiControl().getDanmakuManager() != null) {
            getPlayerUiControl().getDanmakuManager().hideDanmaku();
        }
        if (TextUtils.isEmpty(playVideoInfo.vid)) {
            pluginManager.onError(MPPErrorCode.VIDEO_ID_NULL, 0);
            Logger.e(LogTag.TAG_PLAYER, "play video with null vid, return!");
            return;
        }
        if (mPlayRequest != null)
            mPlayRequest.cancel();

        Logger.d(LogTag.TAG_PLAYER, "playVideo vid:" + playVideoInfo.vid + "  autoPlay:" + playVideoInfo.autoPlay);
        onNewPlayRequest(playVideoInfo);

        if (videoInfo != null && playVideoInfo.videoStage == -1)
            playVideoInfo.videoStage = videoInfo.getVideoStage();
        if (playVideoInfo.languageCode == null) {
            if (mILanguageCode != null)
                playVideoInfo.languageCode = mILanguageCode.getLanCode();
            else
                playVideoInfo.languageCode = Profile.langCode;
        }

        if (playVideoInfo.getPlayType() == PlayType.LOCAL_DOWNLOAD) {
            if (PlayerUtil.useUplayer(null)) {
                playVideoInfo.url = PlayerUtil.getM3u8File(playVideoInfo.url);
            }
            if (MediaPlayerDelegate.mICacheInfo != null) {
                VideoCacheInfo videoCacheInfo = MediaPlayerDelegate.mICacheInfo.getDownloadInfo(playVideoInfo.vid);
                if (videoCacheInfo != null) {
                    getPlayerUiControl().goFullScreen();
                }
            }
            getPlayerUiControl().setOrientionDisable();
        }
        if (playVideoInfo.IsFullScreenPlay()) {
            getPlayerUiControl().goFullScreen();
            getPlayerUiControl().setOrientionDisable();
        }

        if ((playVideoInfo.getPlayType() == PlayType.ONLINE || playVideoInfo.getPlayType() == PlayType.LOCAL_DOWNLOAD) && playVideoInfo.isFromYouku && !PlayerUtil.isLogin()
                && PreAdTimes.times >= PreAdTimes.TIMESTOHINT
                && Util.hasInternet()) {
            PreAdTimes.times = 0;
            final int delayedTime = playVideoInfo.getPlayType() == PlayType.LOCAL_DOWNLOAD ? 600 : 0;
            if (null != handler) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        PreAdTimes.times = 0;
                        mPlayerAdControl.creatDialogToLogin(playVideoInfo);
                    }
                }, delayedTime);
            }
        } else {
            getVideoUrlInfo(playVideoInfo);
        }
    }

    private void onNewPlayRequest(PlayVideoInfo playVideoInfo) {
        nowVid = playVideoInfo.vid;
        isPlayCalled = true;
        if (mPlayerAdControl.isImageAdStartToShow()) {
            if (!mPlayerAdControl.isImageAdShowing() && videoInfo != null) {
                DisposableStatsUtils.disposeAdLoss(context,
                        URLContainer.AD_LOSS_STEP3,
                        SessionUnitil.playEvent_session,
                        URLContainer.AD_LOSS_MF);
            }
            mPlayerAdControl.dismissImageAD();
        }
        DisposableStatsUtils.disposeNotPlayedAd(context, videoInfo, URLContainer.AD_LOSS_STEP3_NEW);

        if (videoInfo != null && !TextUtils.isEmpty(playVideoInfo.vid) && playVideoInfo.vid.equals(videoInfo.getVid())) {
            if (videoInfo.isVideoUrlOutOfDate()) {
                Logger.d(LogTag.TAG_PLAYER, "video url is out of date, play without adv.");
                playVideoInfo.noAdv = true;
            }
            if (videoInfo.isHttp4xxError()) {
                Logger.d(LogTag.TAG_PLAYER, "video 4xx error, play without adv.");
                playVideoInfo.noAdv = true;
            }
        }

        sendVVAdvReturn();
        resetVideoInfoAndRelease();
        dismissPauseAD();
        mPlayerAdControl.releaseInvestigate();
        mPlayerAdControl.dismissInteractiveAD();
        isComplete = false;
        isPause = false;
        pluginManager.onVideoInfoGetting();
        mPlayerAdControl.setAdState(AdState.INITIALIZE);
    }


    public void setXAdInstance(XAdInstance adInstance) {
        offlinePrerollAd = adInstance;
    }

    private EnhanceCountDownTimer mEnhanceTimer;

    /**
     * 开启增强模式(带有动画)
     */
    public void openEnhanceModeWithAnim() {
        if (mediaPlayer != null) {
            mEnhanceTimer = new EnhanceCountDownTimer();
            mEnhanceTimer.start();
            Track.changeEnhanceSwitchOpenTimes();
        }
    }

    /**
     * 开启增强模式(无动画)
     */
    public void openEnhanceMode() {
        if (mediaPlayer != null) {
            mediaPlayer.setEnhanceMode(true, 1.02f);
            Track.changeEnhanceSwitchAutoOpenTimes();
        }
    }

    /**
     * 关闭增强模式
     */
    public void closeEnhanceMode() {
        if (mEnhanceTimer != null) {
            mEnhanceTimer.cancel();
        } else if (mediaPlayer != null) {
            mediaPlayer.setEnhanceMode(false, 1.02f);
        }
        Track.changeEnhanceSwitchCloseTimes();
    }

    /**
     * 设置夜间模式
     *
     * @param lumRatio   亮度
     * @param colorRatio 色度
     */
    public void setNightMode(float lumRatio, float colorRatio) {
//		if (mediaPlayer != null) {
//			mediaPlayer.setNightMode(lumRatio, colorRatio);
//		}
    }

    private class EnhanceCountDownTimer {
        public static final int START = 1;
        public static final int CANCEL = 2;
        public static final int CANCEL_ON_TICK = 3;
        public boolean isFinish = false;
        private float mWidthPercent = 0f;

        private final float mStartWidthPercent = 0.0f;

        private long mMillisInFuture = 2 * 1000;
        private final long mCountDownInterval = 10;
        private CountDownTimer mTimer = null;

        private Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case START:
                        mTimer.start();
                        if (mediaPlayer != null) {
                            mediaPlayer.setEnhanceMode(true, mStartWidthPercent);
                        }
                        break;
                    case CANCEL_ON_TICK:
                        mTimer.cancel();
                        isFinish = true;
                        break;
                    case CANCEL:
                        mTimer.cancel();
                        isFinish = true;
                        if (mediaPlayer != null) {
                            mediaPlayer.setEnhanceMode(false, 1.02f);
                        }
                        break;
                }
            }
        };

        public EnhanceCountDownTimer() {
            isFinish = false;
            mTimer = new CountDownTimer(mMillisInFuture, mCountDownInterval) {
                @Override
                public void onTick(long millisUntilFinished) {
                    if (isFinish) {
                        if (mTimer != null && mHandler != null) {
                            mHandler.sendEmptyMessage(CANCEL_ON_TICK);
                        }
                        return;
                    }
                    float percent = ((float) millisUntilFinished / (float) (mMillisInFuture));
                    mWidthPercent = (1.02f - mStartWidthPercent) * (1.0f - percent);
                    Logger.d("nightMode2", "timer millisUntilFinished : " + millisUntilFinished + " , mWidthPercent : " + mWidthPercent
                            + " , percent :  " + (1.0f - percent));
                    isFinish = (mWidthPercent >= (1.02f - mStartWidthPercent));
                    if (mediaPlayer != null) {
                        mediaPlayer.setEnhanceMode(true, mWidthPercent + mStartWidthPercent);
                    }
                }

                @Override
                public void onFinish() {
                    if (mediaPlayer != null) {
                        Logger.d("nightMode2", "timer finish . setEnhanceMode(true, 1.02f)");
                        mediaPlayer.setEnhanceMode(true, 1.02f);
                    }
                }
            };
        }

        public void start() {
            if (mTimer != null && mHandler != null) {
                mHandler.sendEmptyMessage(START);
            }
        }

        public void cancel() {
            if (mTimer != null && mHandler != null) {
                mHandler.sendEmptyMessage(CANCEL);
            }
        }

    }

    /**
     * 截取视频中的图片
     *
     * @param assetManager
     * @param outPath      截屏图片输出的完整路径
     * @param outWidth     截屏图片的宽
     * @param outHeight    截屏图片的高
     * @param outFmt       图片格式 0 为 png（目前只支持png格式）
     * @param logoName     水印的文件名（asset目录下）
     * @param logoWidth    水印的宽
     * @param logoHeight   水印的高
     * @param logoLeft     水印距左边框的距离
     * @param logoTop      水印距上边框的距离
     * @return 0成功，非0失败
     */
    public int screenShotOneFrame(AssetManager assetManager, final String outPath, int outWidth, int outHeight,
                                  int outFmt, String logoName, int logoWidth,
                                  int logoHeight, int logoLeft, int logoTop) {
        if (mediaPlayer != null) {
            int result = mediaPlayer.screenShotOneFrame(assetManager, outPath, outWidth, outHeight, outFmt, logoName, logoWidth, logoHeight, logoLeft, logoTop);
            return result;
        }
        return -1;
    }

    /**
     * 截取视频中的图片(默认为png格式)
     *
     * @param outPath    截屏图片输出的完整路径
     * @param outWidth   截屏图片的宽
     * @param outHeight  截屏图片的高
     * @param logoName   水印的文件名（asset目录下）
     * @param logoWidth  水印的宽
     * @param logoHeight 水印的高
     * @param logoLeft   水印距左边框的距离
     * @param logoTop    水印距上边框的距离
     * @return 0成功，非0失败
     */
    public int screenShotOneFrame(String outPath, int outWidth, int outHeight,
                                  String logoName, int logoWidth, int logoHeight,
                                  int logoLeft, int logoTop) {
        if (context == null) {
            return -1;
        }
        return screenShotOneFrame(context.getResources().getAssets(), outPath, outWidth, outHeight, 0,
                logoName, logoWidth, logoHeight, logoLeft, logoTop);
    }


    private int loopStartTime = -1; // 截取视频开始时间
    private int loopEndTime = -1;    // 截取视频结束时间
    private boolean isLooping = false; // 是否正在回放视频

    /**
     * 获取录屏结束时间
     */
    public int getLoopEndTime() {
        return loopEndTime;
    }

    /**
     * 获取录屏开始时间
     */
    public int getLoopStartTime() {
        return loopStartTime;
    }

    /**
     * 是否正在回放录屏
     */
    public boolean isLooping() {
        return isLooping;
    }

    /**
     * 短视频回放
     *
     * @param start 回放开始时间
     * @param end   回放结束时间
     */
    public void startLoopVideo(int start, int end) {
        if (mPlayerAdControl.isMidAdShowing()) {
            stopLoopVideo();
            return;
        }
        if (start == -1 || end == -1) {
            stopLoopVideo();
            return;
        }
        Logger.d("PlayFlow", "start loop video , start :" + start + " / end :" + end);
        if (mediaPlayer != null) {
            loopStartTime = start;
            loopEndTime = end;
            isLooping = true;
            if (goPay(start))
                return;
            if (mPlayerUiControl.getDanmakuManager() != null) {
                mPlayerUiControl.getDanmakuManager().seekToDanmaku(start);
            }
            mediaPlayer.seekTo(start);
            if (isAdvShowFinished() && videoInfo != null && start >= 0) {
                videoInfo.setProgress(start);
            }
        }
    }

    /**
     * 停止回放录屏
     */
    public void stopLoopVideo() {
        isLooping = false;
        loopStartTime = -1;
        loopEndTime = -1;
    }

    public void setAccountAndImgUrl(String userid, String nickName, String url) {
        Profile.setPreferences("danmuUserid", userid, context);
        Profile.setPreferences("danmuNickName", nickName, context);
        Profile.setPreferences("danmuUrl", url, context);
        Logger.d("star", "useid:" + userid + "，nickName:" + nickName + "，url:" + url);
    }

    public PlayVideoInfo getPlayVideoInfo() {
        if (mPlayRequest != null)
            return mPlayRequest.getPlayVideoinfo();
        else
            return null;
    }
}
