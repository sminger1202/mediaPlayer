package com.youku.player.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.widget.FrameLayout;

import com.baseproject.image.ImageLoaderManager;
import com.baseproject.utils.Logger;
import com.baseproject.utils.UIUtils;
import com.baseproject.utils.Util;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.youku.analytics.data.Device;
import com.youku.android.player.R;
import com.youku.libmanager.SoUpgradeManager;
import com.youku.libmanager.SoUpgradeService;
import com.youku.player.BaseMediaPlayer;
import com.youku.player.LogTag;
import com.youku.player.NewSurfaceView;
import com.youku.player.Track;
import com.youku.player.ad.AdState;
import com.youku.player.ad.AdTaeSDK;
import com.youku.player.ad.OfflineAdSDK;
import com.youku.player.ad.PlayerAdControl;
import com.youku.player.apiservice.IPlayerAdControl;
import com.youku.player.apiservice.IPlayerUiControl;
import com.youku.player.apiservice.IUserCallback;
import com.youku.player.apiservice.ScreenChangeListener;
import com.youku.player.config.MediaPlayerConfiguration;
import com.youku.player.danmaku.DanmakuManager;
import com.youku.player.danmaku.IDanmakuManager;
import com.youku.player.danmaku.LocalDanmakuManager;
import com.youku.player.goplay.Profile;
import com.youku.player.goplay.StaticsUtil;
import com.youku.player.module.VideoUrlInfo;
import com.youku.player.module.VideoUrlInfo.Source;
import com.youku.player.plugin.MediaPlayerDelegate;
import com.youku.player.plugin.Orientation;
import com.youku.player.plugin.PluginChangeQuality;
import com.youku.player.plugin.PluginManager;
import com.youku.player.plugin.PluginOverlay;
import com.youku.player.plugin.PluginPayTip;
import com.youku.player.plugin.PluginSimplePlayer;
import com.youku.player.reporter.KeyCounter;
import com.youku.player.subtitle.DownloadedSubtitle;
import com.youku.player.subtitle.SubtitleManager;
import com.youku.player.subtitle.SubtitleOperate;
import com.youku.player.ui.widget.InteractionWebView;
import com.youku.player.ui.widget.YoukuAnimation;
import com.youku.player.unicom.ChinaUnicomFreeFlowUtil;
import com.youku.player.util.DetailMessage;
import com.youku.player.util.DetailUtil;
import com.youku.player.util.DeviceOrientationHelper;
import com.youku.player.util.DisposableStatsUtils;
import com.youku.player.util.PlayCode;
import com.youku.player.util.PlayerPreference;
import com.youku.player.util.PlayerUtil;
import com.youku.player.util.URLContainer;
import com.youku.statistics.IRVideoWrapper;
import com.youku.statistics.OfflineStatistics;
import com.youku.uplayer.EGLUtil;
import com.youku.uplayer.MPPErrorCode;
import com.youku.uplayer.MediaPlayerProxy;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

import cn.com.mma.mobile.tracking.OpenUDID_manager;
import master.flame.danmaku.danmaku.model.LiveDanmakuInfo;

@SuppressLint("NewApi")
public class PlayerController
        implements DetailMessage, IPlayerUiControl {

    FragmentActivity mActivity;


    NewSurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    public YoukuPlayerView mYoukuPlayerView;
    PluginManager pluginManager;
    MediaPlayerDelegate mediaPlayerDelegate;
    private PlayerAdControl mPlayerAdControl;
    PluginOverlay mPluginSmallScreenPlay;
    protected PluginPayTip mPaytipPlugin;
    protected PluginChangeQuality mChangeQualityPlugin;
    public Context youkuContext;
    private boolean mIsOnNewIntent = false;
    // 记录onCreate和onDestroy次数，防止多次启动时在onDestroy时将surfaceholder置空
    private static int mCreateTime;
    private int DELAY_TIME = 500;

    protected KeyCounter keyCounter = null;

    //弹幕相关参数
    private IDanmakuManager mDanmakuManager;

    public MediaPlayerDelegate getMediaPlayerDelegate() {
        return this.mediaPlayerDelegate;
    }

    protected static Handler handler = new Handler() {
    };
    public String id;// 上页传递id video/show

    public ImageLoader mImageLoader;
    private static String TAG = LogTag.TAG_PLAYER;
    private static final boolean DEVELOPER_MODE = false;

    private static InteractionWebView interActionWebView = null;

    // 记录登录状态，在没有加载成功的情况下显示登录界面但没有登录的显示播放出
    private boolean isLogin;

    // 是否是竖屏全屏
    private boolean isVerticalFullScreen;

    private ScreenChangeListener mScreenChangeListener;

    // 通知上层接口
    private IUserCallback mUserCallback;
    /**
     *  加密视频处理的回调，如果客户端没有调用YoukuPlayer的setIEncryptVideoCallBack设置�?
     *  YoukuBasePlayerActivity默认提供一个call调用，用于处理加密视频的回调信息�?
     */

    /**
     * **************************subtitle start************************
     */
    private SubtitleOperate mSubtitleOperate = null;

    public void onDownloadSubtitle(DownloadedSubtitle subtitle, int type) {
        if (mSubtitleOperate == null) {
            mSubtitleOperate = new SubtitleOperate(mYoukuPlayerView,
                    pluginManager);
        }
        mSubtitleOperate.onDownloadSubtitle(subtitle, type);
    }

    public void clearSubtitle() {
        SubtitleManager.clearAllSubtitle();
        if (mSubtitleOperate != null) {
            mSubtitleOperate.clearSubtitle();
            mSubtitleOperate = null;
        }
    }

    public SubtitleOperate getSubtitleOperate() {
        return mSubtitleOperate;
    }

    /**
     * **************************subtitle end************************
     */

    PlayerController(FragmentActivity activity, YoukuPlayerView youkuPlayerView) {
        mActivity = activity;
        mYoukuPlayerView = youkuPlayerView;
        pluginManager = new PluginManager(mActivity);
        mPlayerAdControl = new PlayerAdControl(mActivity);
        mediaPlayerDelegate = new MediaPlayerDelegate(mActivity, this, mPlayerAdControl);

        int[] keyary = new int[]{
                KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_UP,
                KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_DOWN};//,
        //KeyEvent.KEYCODE_VOLUME_UP,KeyEvent.KEYCODE_VOLUME_DOWN,KeyEvent.KEYCODE_VOLUME_UP};
        keyCounter = new KeyCounter(keyary);
    }


    @Override
    public void onCreate() {

        Logger.d(LogTag.TAG_PLAYER, "YoukuBasePlayerActivity->onCreate");
        if (DEVELOPER_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads().detectDiskWrites().detectNetwork() // 这里可以替换为detectAll()
                    // 就包括了磁盘读写和网络I/O
                    .penaltyLog() // 打印logcat，当然也可以定位到dropbox，通过文件保存相应的log
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects() // 探测SQLite数据库操�?
                    .penaltyLog() // 打印logcat
                    .penaltyDeath().build());
        }
        if (mCreateTime == 0) {
            // 优酷进入播放器需要重新获取设置的清晰�?
            Profile.getVideoQualityFromSharedPreferences(mActivity.getApplicationContext());
        }
        ++mCreateTime;
        youkuContext = mActivity;
        mImageLoader = ImageLoaderManager.getInstance();
        mActivity.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        OfflineStatistics offline = new OfflineStatistics();
        offline.sendVV(mActivity);

        PlayerPreference.init(mActivity.getApplicationContext());

        // 初始化设备信�?
        Profile.GUID = Device.guid;
        if (TextUtils.isEmpty(com.baseproject.utils.Profile.User_Agent)) {
            com.baseproject.utils.Profile.initProfile("player",Profile.USER_AGENT, mActivity.getApplicationContext());
        }

        com.baseproject.utils.Profile.mContext = mActivity.getApplicationContext();
        if (MediaPlayerProxy.isUplayerSupported()) {
            YoukuBasePlayerActivity.isHighEnd = true;
            // 使用软解
            PlayerPreference.savePreference("isSoftwareDecode", true);
            Profile.setVideoType_and_PlayerType(
                    Profile.FORMAT_FLV_HD, mActivity);
        } else {
            YoukuBasePlayerActivity.isHighEnd = false;
            Profile.setVideoType_and_PlayerType(
                    Profile.FORMAT_3GPHD, mActivity);
        }
        URLContainer.setDebugMode(com.baseproject.utils.Profile.DEBUG);
        MediaPlayerDelegate.is = mActivity.getResources().openRawResource(R.raw.aes);
        orientationHelper = new DeviceOrientationHelper(mActivity, this);

        try {
            Class.forName("com.youku.network.YoukuAsyncTask");
            Class.forName("android.os.AsyncTask");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        MediaPlayerConfiguration.getInstance().mPlantformController.initIRVideo(mActivity);
        isLogin = PlayerUtil.isLogin();
        Track.setFirstVV();
        AdTaeSDK.initTaeSDK(mActivity.getApplicationContext());

        if (!Util.hasInternet() && OfflineAdSDK.canStartNativeAdServer() && mActivity.getExternalFilesDir(null) != null) {//当通过push进出入app时，有可能没有进行初始化操作，在此进行初始化
            final String offlineAdPath = mActivity.getExternalFilesDir(null).getAbsolutePath() + "/offlinead/";
            OfflineAdSDK.initAdSDK(mActivity.getApplicationContext(), DetailUtil.getScreenDensity(mActivity), DetailUtil.getScreenWidth(mActivity), DetailUtil.getScreenHeight(mActivity), offlineAdPath, (OpenUDID_manager.isInitialized() ? OpenUDID_manager
                    .getOpenUDID() : ""), "flv", SoUpgradeService.getVersionName(mActivity.getApplicationContext()), SoUpgradeService.getPid(mActivity.getApplicationContext()), "1");
        }
    }

    @Override
    public void initLayoutView() {
        onCreateInitialize();
    }

    FrameLayout player_holder;
    private PluginOverlay mPluginFullScreenPlay;

    /**
     *
     */
    public void addPlugins() {
        mActivity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                player_holder = (FrameLayout) mYoukuPlayerView
                        .findViewById(R.id.player_holder_all);
                mPlayerAdControl.createAdPlugins(PlayerController.this, mediaPlayerDelegate);

                mPaytipPlugin = new PluginPayTip(mActivity,
                        mediaPlayerDelegate);
                mChangeQualityPlugin = new PluginChangeQuality(mActivity, mediaPlayerDelegate,
                        PlayerController.this, mPlayerAdControl);


                // 全屏插件
                pluginManager.addPlugin(mPluginFullScreenPlay, player_holder);
                // 小屏播放控制
                if (mPluginSmallScreenPlay == null)
                    mPluginSmallScreenPlay = new PluginSimplePlayer(
                            mActivity, mediaPlayerDelegate);


                // 特殊的播放页�?
                mYoukuPlayerView.mMediaPlayerDelegate = mediaPlayerDelegate;
                pluginManager.addYoukuPlayerView(mYoukuPlayerView);
                pluginManager.addPlugin(mPluginSmallScreenPlay, player_holder);
                pluginManager.addPlugin(mPlayerAdControl.getPlugin(PLUGIN_SHOW_IMAGE_AD), player_holder);
                pluginManager.addPlugin(mPaytipPlugin, player_holder);
                pluginManager.addPlugin(mChangeQualityPlugin, player_holder);
                pluginManager.addPluginAbove(mPlayerAdControl.getPlugin(PLUGIN_SHOW_PAUSE_AD), player_holder);
                updatePlugin(PLUGIN_SHOW_NOT_SET);
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void initPlayAndSurface() {
        surfaceView = mYoukuPlayerView.surfaceView;
        mYoukuPlayerView.mMediaPlayerDelegate = mediaPlayerDelegate;
        mediaPlayerDelegate.mediaPlayer = BaseMediaPlayer.getInstance();
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(mediaPlayerDelegate.mediaPlayer);
        if (!isHighEnd)
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    // 初始化播放区控件
    public void initPlayerPart() {
        if (mediaPlayerDelegate != null
                && mediaPlayerDelegate.mediaPlayer != null
                && mediaPlayerDelegate.mediaPlayer.isListenerInit())
            return;
        initPlayAndSurface();
        initMediaPlayer();
    }

    private MediaPlayerInit mMediaPlayerInit;

    private void initMediaPlayer() {
        if (mMediaPlayerInit == null) {
            mMediaPlayerInit = new MediaPlayerInit(mActivity, mediaPlayerDelegate);
        }
        if (mPlayerAdControl != null) {
            mPlayerAdControl.createAdPlugins(this, mediaPlayerDelegate);
        }
        mMediaPlayerInit.execute(this, mPlayerAdControl, mPlayerAdControl.getVideoADCallBack(PLUGIN_SHOW_AD_PLAY),
                mPlayerAdControl.getVideoADCallBack(PLUGIN_SHOW_MID_AD_PLAY));
    }

    private DeviceOrientationHelper orientationHelper;

    @Override
    public void onPause() {
        Logger.d(LogTag.TAG_PLAYER, "YoukuBasePlayerActivity->onPause");
        onPause = true;
        if (mDanmakuManager != null) {
            mDanmakuManager.hideDanmakuWhenOpen();
        }
        if (pluginManager != null) {
            // 通知插件Activity进入onPause
            pluginManager.onPause();
        }
        if (!mYoukuPlayerView.firstOnloaded) {
            pauseBeforeLoaded = true;
        }
        if (null != handler)
            handler.removeCallbacksAndMessages(null);
        if (mediaPlayerDelegate != null) {
            if (mediaPlayerDelegate.hasRight) {
                int nowPostition = mediaPlayerDelegate.getCurrentPosition();
                if (nowPostition > 0) {
                    mediaPlayerDelegate.setAdPausedPosition(nowPostition);
                }
                mediaPlayerDelegate.isPause = true;
                if (MediaPlayerConfiguration.getInstance().hideDanmaku() && mediaPlayerDelegate.videoInfo != null &&
                        mediaPlayerDelegate.videoInfo.isHLS) {
                    mediaPlayerDelegate.resetAndReleaseDanmakuInfo();
                }
                mediaPlayerDelegate.isLoading = false;
                if (mediaPlayerDelegate.videoInfo != null)
                    mediaPlayerDelegate.videoInfo.isFirstLoaded = false;
            }
            mediaPlayerDelegate.release();
        }
        if (surfaceHolder != null && mediaPlayerDelegate.mediaPlayer != null
                && !mediaPlayerDelegate.hasRight) {
            surfaceHolder.removeCallback(mediaPlayerDelegate.mediaPlayer);
        }
        setPlayerBlack();
        if (mPlayerAdControl != null && !(mediaPlayerDelegate != null && mediaPlayerDelegate.videoInfo != null && mediaPlayerDelegate.videoInfo.isDRMVideo() && !SoUpgradeManager.getInstance().isSoDownloaded(SoUpgradeService.LIB_DRM_SO_NAME))) {
            mPlayerAdControl.pauseInteractiveAd();
            mPlayerAdControl.onPause();
            mPlayerAdControl.dismissPauseAD();
            mPlayerAdControl.onMidAdLoadingEndListener();
        }
        dissmissPauseAD();
        keyCounter.stop();
        Track.pauseForIRVideo(mActivity);
        Track.pause();
        Track.setOnPaused(true);
    }

    @Override
    public void onStop() {
        if (mPlayerAdControl != null) {
            mPlayerAdControl.onStop();
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        Logger.d(LogTag.TAG_PLAYER, "YoukuBasePlayerActivity->onNewIntent()");
        mIsOnNewIntent = true;
        mPlayerAdControl.releaseInvestigate();
    }

    @Override
    public void onResume() {
        Logger.d(LogTag.TAG_PLAYER, "YoukuBasePlayerActivity->onResume()");
        onPause = false;
        mActivity.setTitle("");
        // 无版�?
        if (mediaPlayerDelegate != null && !mediaPlayerDelegate.hasRight)
            return;

        if (null != handler) {
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    surfaceHolder = surfaceView.getHolder();
                    // 解决android4.4上有虚拟键的设备锁屏后播放没有调用surfaceChanged导致画面比例失常
                    if (UIUtils.hasKitKat()) {
                        surfaceView.requestLayout();
                    }
                    if (null != mediaPlayerDelegate && null != surfaceHolder) {
                        mPlayerAdControl.doOnResumeDelayedOperation(false);
                    }
                }
            }, 100);
        }
        pluginManager.onResume();
        callPluginBack();

        if (mediaPlayerDelegate.isFullScreen) {
            mActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            changeConfiguration(mActivity.getResources().getConfiguration());
        }
        if (mPlayerAdControl != null) {
            mPlayerAdControl.doOnResumeOperation();
        }
        if (mDanmakuManager != null) {
            mDanmakuManager.hideDanmakuAgain();
        }
    }

    private void callPluginBack() {
        if (null != mPluginSmallScreenPlay)
            mPluginSmallScreenPlay.back();

        if (null != mPluginFullScreenPlay) {
            mPluginFullScreenPlay.back();
        }
        //全屏广告显示时和登录提示dialog显示时，不上报error
        if (pauseBeforeLoaded
                && !mIsOnNewIntent
                && !(mPlayerAdControl != null && mPlayerAdControl.isImageAdShowing())
                && !(mPlayerAdControl != null && mPlayerAdControl.isSuggestLoginDialogShowing())
                && !(mediaPlayerDelegate != null
                && mediaPlayerDelegate.videoInfo != null && mediaPlayerDelegate.videoInfo.isHLS)
                && !(PlayerUtil.isLogin() && !isLogin)
                && (mediaPlayerDelegate != null && mediaPlayerDelegate.isAdvShowFinished())
                && !(mediaPlayerDelegate != null && mediaPlayerDelegate.videoInfo != null && mediaPlayerDelegate.videoInfo.isDRMVideo() && !SoUpgradeManager.getInstance().isSoDownloaded(SoUpgradeService.LIB_DRM_SO_NAME))
                && !(mediaPlayerDelegate != null && mediaPlayerDelegate.videoInfo != null && mediaPlayerDelegate.videoInfo.getVipError() != 0)
                && (PreferenceManager.getDefaultSharedPreferences(
                mActivity).getBoolean("ifautoplay", true) || (mediaPlayerDelegate != null && mediaPlayerDelegate.isPlayCalled))) {
            Logger.d("PlayFlow", "+++++++++++++++++++++++++++++++++++++++++++++++++ callPluginBack onError");
            pluginManager.onError(MPPErrorCode.MEDIA_INFO_PREPARE_ERROR, 0);
        }
        // 位置要在back()之后
        pauseBeforeLoaded = false;
        mIsOnNewIntent = false;
        isLogin = PlayerUtil.isLogin();
    }

    @Override
    public void onDestroy() {
        Logger.d(LogTag.TAG_PLAYER, "YoukuBasePlayerActivity->onDestroy");
        if (interActionWebView != null) {
            interActionWebView = null;
        }
        if (mediaPlayerDelegate != null) {
            //mediaPlayerDelegate.onVVEnd();
            if (mPlayerAdControl != null) {
                mPlayerAdControl.destroy();
                mPlayerAdControl = null;
            }
            mPaytipPlugin = null;
            mChangeQualityPlugin = null;
            if (mDanmakuManager != null) {
                mDanmakuManager.releaseDanmakuWhenDestroy();
            }
            Track.forceEnd(mActivity, mediaPlayerDelegate.videoInfo, mediaPlayerDelegate.isFullScreen,
                    MediaPlayerConfiguration.getInstance().getVersionCode(),
                    MediaPlayerConfiguration.getInstance().mTestid,
                    MediaPlayerConfiguration.getInstance().mIstest);
            Track.clear();
            try {
                if (surfaceHolder != null
                        && mediaPlayerDelegate.mediaPlayer != null)
                    surfaceHolder
                            .removeCallback(mediaPlayerDelegate.mediaPlayer);

                if (null != handler)
                    handler.removeCallbacksAndMessages(null);
                if (null != DanmakuManager.danmakuHandler) {
                    DanmakuManager.danmakuHandler.removeCallbacksAndMessages(null);
                }
            } catch (Exception e) {

            }
            if (orientationHelper != null) {
                orientationHelper.disableListener();
                orientationHelper.setCallback(null);
            }
            mediaPlayerDelegate.mediaPlayer.setOnPreparedListener(null);
            mediaPlayerDelegate.mediaPlayer.clearListener();
            mediaPlayerDelegate.mediaPlayer = null;
            mPluginSmallScreenPlay = null;
            pluginManager = null;
            mPluginFullScreenPlay = null;
            mediaPlayerDelegate = null;
            surfaceView = null;
            surfaceHolder = null;
            mYoukuPlayerView = null;
            youkuContext = null;
            mMediaPlayerInit = null;
            ChinaUnicomFreeFlowUtil.isAlertDialogShown = false;
            if (--mCreateTime <= 0)
                EGLUtil.setSurfaceHolder(null);
        }
    }

    public void playNoRightVideo(String mUri) {

        if (mUri == null || mUri.trim().equals("")) {
            Profile.from = Profile.PHONE;
            return;
        }
        if (mUri.startsWith("youku://")) {
            mUri = mUri.replaceFirst("youku://", "http://");
        } else {
            Profile.from = Profile.PHONE;
            return;
        }
        // 不能再初始化一遍，因为已经初始化过�?
        // initPlayAndSurface();
        if (mediaPlayerDelegate.videoInfo == null)
            mediaPlayerDelegate.videoInfo = new VideoUrlInfo();
        final int queryPosition = mUri.indexOf("?");
        if (queryPosition != -1) {
            String url = new String(URLUtil.decode(mUri.substring(0,
                    queryPosition).getBytes()));
            if (PlayerUtil.useUplayer(null)) {
                StringBuffer m3u8Url = new StringBuffer();
                m3u8Url.append("#PLSEXTM3U\n#EXT-X-TARGETDURATION:10000\n")
                        .append("#EXT-X-VERSION:2\n#EXT-X-DISCONTINUITY\n")
                        .append("#EXTINF:10000\n").append(url)
                        .append("\n#EXT-X-ENDLIST\n");
                mediaPlayerDelegate.videoInfo.setUrl(m3u8Url.toString());
            } else {
                mediaPlayerDelegate.videoInfo.setUrl(url);
            }
            String[] params = mUri.substring(queryPosition + 1).split("&");
            for (int i = 0; i < params.length; i++) {
                String[] param = params[i].split("=");
                if (param[0].trim().equals("vid")) {
                    mediaPlayerDelegate.videoInfo.setVid(param[1].trim());
                }
                if (param[0].trim().equals("title")) {
                    try {
                        mediaPlayerDelegate.videoInfo.setTitle(URLDecoder
                                .decode(param[1].trim(), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            return;
        }
        onParseNoRightVideoSuccess();
    }

    public void onParseNoRightVideoSuccess() {
        if (PlayerUtil.useUplayer(mediaPlayerDelegate != null ? mediaPlayerDelegate.videoInfo : null)) {
            Profile.setVideoType_and_PlayerType(Profile.FORMAT_FLV_HD, mActivity);
        } else {
            Profile.setVideoType_and_PlayerType(Profile.FORMAT_3GPHD, mActivity);
        }
        Profile.from = Profile.PHONE_BROWSER;
        pluginManager.onVideoInfoGetted();
        pluginManager.onChangeVideo();
        goFullScreen();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Logger.e("fisrtsss","onConfigurationChanged");
        Logger.d(LogTag.TAG_PLAYER, "onConfigurationChange:" + newConfig.orientation);
        if (mPlayerAdControl != null) {
            mPlayerAdControl.cancelSuggestLoginDialog();
        }
        if (mediaPlayerDelegate != null && mediaPlayerDelegate.isFullScreen
                && PlayerUtil.isYoukuTablet(mActivity)) {
            return;
        }
        if (isVerticalFullScreen && mediaPlayerDelegate != null && mediaPlayerDelegate.isFullScreen
                && newConfig.orientation != Configuration.ORIENTATION_PORTRAIT)
            isVerticalFullScreen = false;
        changeConfiguration(newConfig);

    }

    public void removeHandlerMessage() {
        layoutHandler.removeCallbacksAndMessages(null);
    }


    public void changeConfiguration(Configuration newConfig) {
        if (Profile.PHONE_BROWSER == Profile.from) {
            return;
        }
        boolean island = mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        Logger.d(LogTag.TAG_PLAYER, "isLand:" + island);
        // 横屏有两种情�?�?
        if (island
                || (mediaPlayerDelegate.videoInfo != null
                       && (/*StaticsUtil.PLAY_TYPE_LOCAL.equals(mediaPlayerDelegate.videoInfo.getPlayType())
                       ||*/ (mediaPlayerDelegate.videoInfo.isHLS && !MediaPlayerConfiguration.getInstance().livePortrait())
                       || isVerticalFullScreen))) {
            Logger.d(LogTag.TAG_PLAYER, "isTablet:" + PlayerUtil.isYoukuTablet(mActivity));
            Logger.d(LogTag.TAG_PLAYER, "isVerticalFullScreen:" + isVerticalFullScreen);
            // 不是平板去全�?
            if (!PlayerUtil.isYoukuTablet(mActivity)) {
                mActivity.closeOptionsMenu();
                setPlayerFullScreen();
                mPlayerAdControl.changeConfiguration();
                if (null != handler) {
                    handler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            if (mPlayerAdControl != null
                                    && mPlayerAdControl.isSuggestLoginDialogShowing()) {
                                mPlayerAdControl.cancelSuggestLoginDialog();
                            }
                        }
                    }, 100);
                }
                return;
            } else {
                if (mediaPlayerDelegate.isFullScreen || PlayerUtil.isYoukuHlsTablet(mActivity, mediaPlayerDelegate)) {
                    setPlayerFullScreen();
                } else {
                    mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    setPlayerSmall();
                    mScreenChangeListener.setPadHorizontalLayout();
                    if (!mediaPlayerDelegate.isAdvShowFinished() && mediaPlayerDelegate.isADShowing) {
                        updatePlugin(PLUGIN_SHOW_AD_PLAY);
                    } else if (mPlayerAdControl.isMidAdShowing()) {
                        updatePlugin(PLUGIN_SHOW_MID_AD_PLAY);
                    } else {
                        updatePlugin(PLUGIN_SHOW_NOT_SET);
                    }
                    Logger.d(LogTag.TAG_PLAYER, "平板去横屏小播放");
                    layoutHandler.removeCallbacksAndMessages(null);
                }
            }
        } else {
            setPlayerSmall();
            Logger.d(LogTag.TAG_PLAYER, "goSmall");
            PlayerUtil.showSystemUI(mActivity, mPlayerAdControl.getPlugin(PLUGIN_SHOW_AD_PLAY));
            orientationHelper.fromUser = false;
            if (!mediaPlayerDelegate.isAdvShowFinished() && mediaPlayerDelegate.isADShowing) {
                updatePlugin(PLUGIN_SHOW_AD_PLAY);
            } else if (mPlayerAdControl.isMidAdShowing()) {
                updatePlugin(PLUGIN_SHOW_MID_AD_PLAY);
            } else {
                updatePlugin(PLUGIN_SHOW_NOT_SET);
            }
            // fitsSystemWindows为true时，转小屏，会保留横屏的padding，所以需重置
            player_holder.setPadding(0, 0, 0, 0);
            Logger.d(LogTag.TAG_PLAYER, "去竖屏小播放");
        }
        mPlayerAdControl.changeConfiguration();
    }

    private Handler layoutHandler = new Handler();

    public void setPlayerFullScreen() {
        Logger.d(TAG, "setPlayerFullScreen");
        if (mediaPlayerDelegate == null)
            return;
        mediaPlayerDelegate.isFullScreen = true;
        mediaPlayerDelegate.currentOriention = Orientation.LAND;
        mScreenChangeListener.onFullscreenListener();
        if (mediaPlayerDelegate.videoInfo != null
                && StaticsUtil.PLAY_TYPE_LOCAL
                .equals(mediaPlayerDelegate.videoInfo.getPlayType()) && !isVerticalFullScreen) {
            orientationHelper.disableListener();
        }
        if (Profile.from != Profile.PHONE_BROWSER
                && !PlayerUtil.isYoukuTablet(mActivity)
                && mediaPlayerDelegate != null
                && (mediaPlayerDelegate.videoInfo == null || (!StaticsUtil.PLAY_TYPE_LOCAL
                .equals(mediaPlayerDelegate.videoInfo.getPlayType()) && !mediaPlayerDelegate.videoInfo.isHLS))) {
            orientationHelper.enableListener();
        }
        mActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mYoukuPlayerView.setFullscreenBack();

        if (!mediaPlayerDelegate.isAdvShowFinished() && mediaPlayerDelegate.isADShowing) {
            updatePlugin(PLUGIN_SHOW_AD_PLAY);
        } else if (mPlayerAdControl.isMidAdShowing()) {
            updatePlugin(PLUGIN_SHOW_MID_AD_PLAY);
        } else {
            updatePlugin(PLUGIN_SHOW_NOT_SET);
        }
        if (!mediaPlayerDelegate.isOfflinePrerollAd() && !mediaPlayerDelegate.isPlaying() && mSubtitleOperate != null && mediaPlayerDelegate.videoInfo != null) {
            mSubtitleOperate.showSubtitle(mediaPlayerDelegate.videoInfo.getProgress());
        }
    }

    /**
     * 互动娱乐显示WebView
     *
     * @param width    WebView 宽度
     * @param fragment WebViewFragment
     */
    public void showWebView(int width, Fragment fragment) {
        if (mPlayerAdControl.isPauseAdVisible()) {
            return;
        }
        hideTipsPlugin(); // 隐藏调查问卷
        if (interActionWebView != null) {
            hideWebView();
        }
        if (width >= 0 && fragment != null) {
            interActionWebView = new InteractionWebView(mActivity, width, fragment);
            interActionWebView.addInteractionFragment();
            player_holder.addView(interActionWebView);

            if (!interActionWebView.isWebViewShown()) {
                interActionWebView.setVisiable();
            }
        }
    }

    public boolean isWebViewShown() {
        if (interActionWebView == null)
            return false;

        return interActionWebView.isWebViewShown();
    }

    /**
     * 隐藏WebView
     */
    public void hideWebView() {
        if (interActionWebView != null) {
            interActionWebView.hideWebView();
            player_holder.removeView(interActionWebView);
            interActionWebView = null;
            unHideTipsPlugin(); //显示调查问卷
        }
    }

    /**
     * 隐藏互动娱乐popwindow
     */
    public void hideInteractivePopWindow() {
    }

    public void goFullScreen() {
        Logger.d(LogTag.TAG_PLAYER, "goFullScreen");
        if(mScreenChangeListener != null) {
            mScreenChangeListener.onGoFull();
        }
        if (UIUtils.hasGingerbread()
                && !PlayerPreference.getPreferenceBoolean("video_lock",
                false)) {
            Logger.d(LogTag.TAG_ORIENTATION,
                    "setPlayerFullScreen SCREEN_ORIENTATION_SENSOR_LANDSCAPE");
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        isVerticalFullScreen = false;
        if (mediaPlayerDelegate != null)
            mediaPlayerDelegate.isFullScreen = true;
        if (PlayerUtil.isYoukuTablet(mActivity)
                || mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            changeConfiguration(mActivity.getResources().getConfiguration());
        }
        orientationHelper.isFromUser();
    }

    @Override
    public void goVerticalFullScreen() {
        Logger.d(LogTag.TAG_PLAYER, "goVerticalFullScreen");
        if (mediaPlayerDelegate != null)
            if (mediaPlayerDelegate.isFullScreen) {
                goSmall();
                isVerticalFullScreen = true;
                if (StaticsUtil.PLAY_TYPE_LOCAL
                        .equals(mediaPlayerDelegate.videoInfo.getPlayType()))
                    changeConfiguration(new Configuration());
            } else {
                mediaPlayerDelegate.isFullScreen = true;
                isVerticalFullScreen = true;
                changeConfiguration(new Configuration());
            }
        orientationHelper.isFromUser();
    }

    @Override
    public boolean isVerticalFullScreen() {
        return isVerticalFullScreen;
    }

    private void setPlayerSmall() {
        Logger.d(TAG, "setPlayerSmall");
        mediaPlayerDelegate.isFullScreen = false;
        if (null != mediaPlayerDelegate)
            mediaPlayerDelegate.currentOriention = Orientation.VERTICAL;
        mScreenChangeListener.onSmallscreenListener();
        mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (mPlayerAdControl.isInteractiveAdShowing()) {
            mPlayerAdControl.dismissInteractiveAD();
            if (!onPause) {
                mediaPlayerDelegate.startByInteractiveAd();
            }
        }
        if (PlayerUtil.isYoukuTablet(mActivity)) {
            // pad没有转屏，所有在去小屏时设置padding
            setPluginHolderPaddingZero();
            if (mPlayerAdControl != null && !onPause) {
                mPlayerAdControl.imageAdOnOrientChange();
            }
        } else {
            mYoukuPlayerView.setVerticalLayout();
//            if (mediaPlayerDelegate != null) {
//                orientationHelper.enableListener();
//            }
        }
        hideWebView();

        if (!mediaPlayerDelegate.isPlaying() && mSubtitleOperate != null) {
            mSubtitleOperate.dismissSubtitle();
        }
    }

    public void goSmall() {
        Logger.d(LogTag.TAG_PLAYER, "goSmall");
        if(mScreenChangeListener != null) {
            mScreenChangeListener.onGoSmall();
        }
        if (mediaPlayerDelegate != null)
            mediaPlayerDelegate.isFullScreen = false;
        if (PlayerUtil.isYoukuTablet(mActivity)) {
            changeConfiguration(mActivity.getResources().getConfiguration());
        } else {
            if (isVerticalFullScreen) {
                isVerticalFullScreen = false;
                changeConfiguration(new Configuration());
            }
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            if (orientationHelper != null) {
                orientationHelper.enableListener();
                orientationHelper.isFromUser();
            }
        }
    }

    public void playCompleteGoSmall() {
        mScreenChangeListener.onSmallscreenListener();
        // mPluginFullScreenPlay.showSystemUI();
        if (mediaPlayerDelegate == null)
            return;
        if (PlayerUtil.isYoukuTablet(mActivity)) {
            goSmall();
        } else {
            goSmall();
            orientationHelper.disableListener();
//			orientationHelper.isFromUser();
//			orientationHelper.isFromComplete();
        }
    }

    @Override
    public boolean onSearchRequested() {
        return mediaPlayerDelegate.isFullScreen;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        try {

            switch (keyCode) {
                case KeyEvent.KEYCODE_MENU:
                    // 取消长按menu键弹出键盘事�?
                    if (event.getRepeatCount() > 0) {
                        return true;
                    }
                    return mediaPlayerDelegate.isFullScreen;
                case KeyEvent.KEYCODE_BACK:
                    // 点击过快的取消操�?
                    if (event.getRepeatCount() > 0) {
                        return true;
                    }
                    if (!mediaPlayerDelegate.isDLNA) {
                        if (mediaPlayerDelegate.isFullScreen
                                && !PlayerUtil.isFromLocal(mediaPlayerDelegate.videoInfo)
                                && (mediaPlayerDelegate.videoInfo != null && !mediaPlayerDelegate.videoInfo.isHLS)) {
                            goSmall();
                            return true;
                        } else {
//                            onkeyback();
                            return false;
                        }
                    } else {
                        return true;
                    }
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    keyCounter.addKey(mActivity, keyCode);
                    return volumeDown();
                case KeyEvent.KEYCODE_VOLUME_UP:
                    keyCounter.addKey(mActivity, keyCode);
                    return volumeUp();
                case KeyEvent.KEYCODE_SEARCH:
                    return mediaPlayerDelegate.isFullScreen;
                case 125:
                    /** 有些手机载入中弹出popupwindow */
                    return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 处理一些播放器关闭后的动作。
     */
    protected void onPlayerClosed() {
        try {
            if (mediaPlayerDelegate != null) {
                if (mediaPlayerDelegate.getPlayRequest() != null)
                    mediaPlayerDelegate.getPlayRequest().cancel();
                if (mediaPlayerDelegate.videoInfo != null && mediaPlayerDelegate.videoInfo.isHLS) {
                    IRVideoWrapper.videoEnd(mActivity);
                    return;
                }
            }
            if (!mediaPlayerDelegate.isStartPlay
                    && !mediaPlayerDelegate.isVVBegin998Send && !mediaPlayerDelegate.isChangeLan) {
                if (mediaPlayerDelegate.videoInfo == null
                        || TextUtils.isEmpty(mediaPlayerDelegate.videoInfo
                        .getVid())) {
                    Track.onError(
                            mActivity,
                            id,
                            Device.guid,
                            StaticsUtil.PLAY_TYPE_NET,
                            PlayCode.USER_RETURN,
                            mediaPlayerDelegate.videoInfo == null ? Source.YOUKU
                                    : mediaPlayerDelegate.videoInfo.mSource,
                            Profile.videoQuality, 0,
                            mediaPlayerDelegate.isFullScreen, null, mediaPlayerDelegate.getPlayVideoInfo());
                    mediaPlayerDelegate.isVVBegin998Send = true;
                } else if (!mediaPlayerDelegate.videoInfo.IsSendVV
                        && !mediaPlayerDelegate.videoInfo.isSendVVEnd) {
                    DisposableStatsUtils.disposeNotPlayedAd(mActivity, mediaPlayerDelegate.videoInfo, URLContainer.AD_LOSS_STEP3_NEW);
                    if (mediaPlayerDelegate.isADShowing) {
                        Track.onError(mActivity, mediaPlayerDelegate.videoInfo
                                        .getVid(), Device.guid,
                                mediaPlayerDelegate.videoInfo.playType,
                                PlayCode.VIDEO_ADV_RETURN,
                                mediaPlayerDelegate.videoInfo.mSource,
                                mediaPlayerDelegate.videoInfo
                                        .getCurrentQuality(),
                                mediaPlayerDelegate.videoInfo.getProgress(),
                                mediaPlayerDelegate.isFullScreen,
                                mediaPlayerDelegate.videoInfo, mediaPlayerDelegate.getPlayVideoInfo());
                    } else {
                        Track.onError(
                                mActivity,
                                mediaPlayerDelegate.videoInfo.getVid(),
                                Device.guid,
                                PlayerUtil
                                        .isBaiduQvodSource(mediaPlayerDelegate.videoInfo.mSource) ? StaticsUtil.PLAY_TYPE_NET
                                        : mediaPlayerDelegate.videoInfo.playType,
                                PlayCode.USER_LOADING_RETURN,
                                mediaPlayerDelegate.videoInfo.mSource,
                                mediaPlayerDelegate.videoInfo
                                        .getCurrentQuality(),
                                mediaPlayerDelegate.videoInfo.getProgress(),
                                mediaPlayerDelegate.isFullScreen,
                                mediaPlayerDelegate.videoInfo, mediaPlayerDelegate.getPlayVideoInfo());
                    }
                }
            }
            mediaPlayerDelegate.isStartPlay = false;
            if (!mediaPlayerDelegate.isVVBegin998Send) {
                mediaPlayerDelegate.onVVEnd();
            } else {
                mediaPlayerDelegate.videoInfo.isSendVVEnd = true;
            }
        } catch (Exception e) {
        } finally {
        }
    }

    protected void onkeyback() {
//        try {
//            if (mediaPlayerDelegate != null) {
//                if (mediaPlayerDelegate.getPlayRequest() != null)
//                    mediaPlayerDelegate.getPlayRequest().cancel();
//                if (mediaPlayerDelegate.videoInfo != null && mediaPlayerDelegate.videoInfo.isHLS) {
//                IRVideoWrapper.videoEnd(mActivity);
//                return;
//            }
//            }
//            if (!mediaPlayerDelegate.isStartPlay
//                    && !mediaPlayerDelegate.isVVBegin998Send && !mediaPlayerDelegate.isChangeLan) {
//                if (mediaPlayerDelegate.videoInfo == null
//                        || TextUtils.isEmpty(mediaPlayerDelegate.videoInfo
//                        .getVid())) {
//                    Track.onError(
//                            mActivity,
//                            id,
//                            Device.guid,
//                            StaticsUtil.PLAY_TYPE_NET,
//                            PlayCode.USER_RETURN,
//                            mediaPlayerDelegate.videoInfo == null ? Source.YOUKU
//                                    : mediaPlayerDelegate.videoInfo.mSource,
//                            Profile.videoQuality, 0,
//                            mediaPlayerDelegate.isFullScreen, null, mediaPlayerDelegate.getPlayVideoInfo());
//                    mediaPlayerDelegate.isVVBegin998Send = true;
//                } else if (!mediaPlayerDelegate.videoInfo.IsSendVV
//                        && !mediaPlayerDelegate.videoInfo.isSendVVEnd) {
//                    DisposableStatsUtils.disposeNotPlayedAd(mActivity, mediaPlayerDelegate.videoInfo, URLContainer.AD_LOSS_STEP3_NEW);
//                    if (mediaPlayerDelegate.isADShowing) {
//                        Track.onError(mActivity, mediaPlayerDelegate.videoInfo
//                                        .getVid(), Device.guid,
//                                mediaPlayerDelegate.videoInfo.playType,
//                                PlayCode.VIDEO_ADV_RETURN,
//                                mediaPlayerDelegate.videoInfo.mSource,
//                                mediaPlayerDelegate.videoInfo
//                                        .getCurrentQuality(),
//                                mediaPlayerDelegate.videoInfo.getProgress(),
//                                mediaPlayerDelegate.isFullScreen,
//                                mediaPlayerDelegate.videoInfo, mediaPlayerDelegate.getPlayVideoInfo());
//                    } else {
//                        Track.onError(
//                                mActivity,
//                                mediaPlayerDelegate.videoInfo.getVid(),
//                                Device.guid,
//                                PlayerUtil
//                                        .isBaiduQvodSource(mediaPlayerDelegate.videoInfo.mSource) ? StaticsUtil.PLAY_TYPE_NET
//                                        : mediaPlayerDelegate.videoInfo.playType,
//                                PlayCode.USER_LOADING_RETURN,
//                                mediaPlayerDelegate.videoInfo.mSource,
//                                mediaPlayerDelegate.videoInfo
//                                        .getCurrentQuality(),
//                                mediaPlayerDelegate.videoInfo.getProgress(),
//                                mediaPlayerDelegate.isFullScreen,
//                                mediaPlayerDelegate.videoInfo, mediaPlayerDelegate.getPlayVideoInfo());
//                    }
//                }
//            }
//            mediaPlayerDelegate.isStartPlay = false;
////          mediaPlayerDelegate.isChangeLan = false;
//            if (!mediaPlayerDelegate.isVVBegin998Send) {
//                mediaPlayerDelegate.onVVEnd();
//            } else {
//                mediaPlayerDelegate.videoInfo.isSendVVEnd = true;
//            }
//        } catch (Exception e) {
//        } finally {
//            mActivity.setResult(mActivity.RESULT_OK);
//            if (!mActivity.isFinishing())
//                mActivity.finish();
//        }
    }

    /**
     * 是否静音
     */
    private boolean isMute = false;
    private AudioManager am;
    private int currentSound;

    private void initSound() {
        am = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
        currentSound = am.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    private boolean volumeUp() {
        if (!isMute) {
            am.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_RAISE,
                    AudioManager.FX_FOCUS_NAVIGATION_UP);
        } else {
            if (currentSound >= 0) {
                am.setStreamVolume(AudioManager.STREAM_MUSIC, Math.min(
                        currentSound + 1,
                        am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)), 0);
            }

        }
        pluginManager.onVolumnUp();
        return true;
    }

    private boolean volumeDown() {
        if (!isMute) {
            am.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_LOWER,
                    AudioManager.FX_FOCUS_NAVIGATION_UP);
        } else {
            if (currentSound >= 0) {
                int destSound = currentSound - 1;
                am.setStreamVolume(AudioManager.STREAM_MUSIC,
                        destSound >= 0 ? destSound : 0, 0);
            }
        }
        pluginManager.onVolumnDown();
        return true;
    }

    @Override
    public void onBackPressed() {
        Logger.d(LogTag.TAG_PLAYER, "onBackPressed");
        onkeyback();
    }

    public void onCreateInitialize() {
        initPlayerPart();
        if (!Util.hasInternet()
                && (mediaPlayerDelegate.videoInfo == null || !mediaPlayerDelegate.videoInfo
                .isCached())) {
            // Util.showTips("!Util.hasInternet(this)");
        } else {
            if ((mediaPlayerDelegate.videoInfo == null || !mediaPlayerDelegate.videoInfo
                    .isCached()) && Util.hasInternet() && !Util.isWifi()) {
                // 提示用户没有wifi
                // Util.showTips("mediaPlayerDelegate.videoInfo == null ");
            }
            // 读取是否自动播放
            getIntentData();
            if (mediaPlayerDelegate.videoInfo == null) {
            } else {
                pluginManager.onVideoInfoGetted();
            }
        }
        initSound();
    }

    public void resizeMediaPlayer(int percent) {
        mYoukuPlayerView.resizeVideoView(percent, false);
    }

    @Override
    public void land2Port() {
        Logger.d(LogTag.TAG_ORIENTATION, "start land2Port");
        layoutHandler.removeCallbacksAndMessages(null);
        layoutHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                Logger.d(LogTag.TAG_ORIENTATION, "land2Port");
                if (!PlayerPreference.getPreferenceBoolean("video_lock", false)) {
                    mScreenChangeListener.onGoSmall();
                    mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                }
                Logger.d(LogTag.TAG_PLAYER, "land2Port");
            }
        }, DELAY_TIME);

    }

    @Override
    public void port2Land() {
        Logger.d(LogTag.TAG_ORIENTATION, "start port2Land");
        layoutHandler.removeCallbacksAndMessages(null);
        layoutHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                Logger.d(LogTag.TAG_ORIENTATION, "port2Land");
                mScreenChangeListener.onGoFull();
                if (PlayerPreference.getPreferenceBoolean("video_lock",
                        false)
                        && UIUtils.hasGingerbread()
                        && mActivity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE)
                    return;
                else
                    mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }, DELAY_TIME);
    }

    @Override
    public void reverseLand() {
        Logger.d(LogTag.TAG_ORIENTATION, "reverseLand");
        layoutHandler.removeCallbacksAndMessages(null);
        layoutHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                mScreenChangeListener.onGoFull();
                if (PlayerPreference.getPreferenceBoolean("video_lock",
                        false) || !UIUtils.hasGingerbread()) {
                    mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else {
                    mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                }
            }
        }, DELAY_TIME);
    }

    @Override
    public void reversePort() {
        Logger.d(LogTag.TAG_ORIENTATION, "reversePort");
        layoutHandler.removeCallbacksAndMessages(null);
        layoutHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (PlayerPreference.getPreferenceBoolean("video_lock",
                        false))
                    return;
                mScreenChangeListener.onGoSmall();
                if (UIUtils.hasGingerbread())
                    mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                else
                    mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }, DELAY_TIME);
    }

    @Override
    public void onFullScreenPlayComplete() {
        Logger.d(LogTag.TAG_ORIENTATION, "onFullScreenPlayComplete");
        if (null != orientationHelper)
            orientationHelper.disableListener();
        mScreenChangeListener.onGoSmall();
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public boolean isOrientationEnable() {
        if (null != orientationHelper) {
            return orientationHelper.isOrientionEnable();
        }
        return false;
    }
    public void setOrientionDisable() {
        if (null != orientationHelper)
            Logger.d(LogTag.TAG_ORIENTATION, "orientation disable.");
//        try {
//            orientationHelper.disableListener();
//        } catch (NullPointerException e) {
//            e.printStackTrace();
//        }
//            orientationHelper.setCallback(null);
        orientationHelper.disableListener();
    }

    public void setOrientionEnable() {
        if (null != orientationHelper)
            Logger.d(LogTag.TAG_ORIENTATION, "orientation enable.");
//        try {
//            orientationHelper.enableListener();
//        } catch (NullPointerException e) {
//            e.printStackTrace();
//        }

//            orientationHelper.setCallback(this);
        orientationHelper.enableListener();
    }

    // 从youkuApplicaiton 挪到YoukuBaseActivity
    public static final String TAG_GLOBAL = "YoukuBaseActivity";


    public static boolean isHighEnd; // 是否高端机型

    @Override
    public void onLowMemory() {
        System.gc();
    }

    /**
     * 获取当前下默认载格式
     */
    public static int getCurrentFormat() {
        return isHighEnd ? Profile.FORMAT_FLV_HD : Profile.FORMAT_3GPHD;
    }

    public static void startActivity(Context context, Intent intent) {
        startActivityForResult(context, intent, -1);
    }

    public static void startActivityForResult(Context context, Intent intent,
                                              int requestCode) {
        ((Activity) context).startActivityForResult(intent, requestCode);
        YoukuAnimation.activityOpen(context);
    }

    /**
     * @param pluginID 更新plugin
     */
    public void updatePlugin(final int pluginID) {
        Logger.d(TAG, "数组访问 updatePlugin");
        if (pluginManager == null) {
            return;
        }
        mActivity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                switch (pluginID) {
                    case PLUGIN_SHOW_AD_PLAY: {
                        if (mPlayerAdControl != null && mPlayerAdControl.getPlugin(PLUGIN_SHOW_AD_PLAY) != null) {
                            pluginManager.addPlugin(mPlayerAdControl.getPlugin(PLUGIN_SHOW_AD_PLAY), player_holder);
                        }
                        break;
                    }
                    case PLUGIN_SHOW_MID_AD_PLAY: {
                        if (mPlayerAdControl != null && mPlayerAdControl.getPlugin(PLUGIN_SHOW_MID_AD_PLAY) != null) {
                            pluginManager.addPlugin(mPlayerAdControl.getPlugin(PLUGIN_SHOW_MID_AD_PLAY), player_holder);
                        }
                        break;
                    }
                    case PLUGIN_SHOW_IMAGE_AD: {
                        if (mPlayerAdControl != null && mPlayerAdControl.getPlugin(PLUGIN_SHOW_IMAGE_AD) != null) {
                            pluginManager.addPlugin(mPlayerAdControl.getPlugin(PLUGIN_SHOW_IMAGE_AD), player_holder);
                        }
                        break;
                    }
                    case PLUGIN_SHOW_INVESTIGATE: {
                        if (mPlayerAdControl != null && mPlayerAdControl.getPlugin(PLUGIN_SHOW_INVESTIGATE) != null) {
                            pluginManager.addInvestigatePlugin(mPlayerAdControl.getPlugin(PLUGIN_SHOW_INVESTIGATE), player_holder);
                        }
                        break;
                    }
                    case PLUGIN_SHOW_NOT_SET: {
                        detectPlugin();
                        break;
                    }
                    default:
                        detectPlugin();
                        break;
                }
            }

        });
    }

    public void detectPlugin() {
        if (pluginManager == null) {
            return;
        }
        if (mediaPlayerDelegate != null && mediaPlayerDelegate.isFullScreen) {
            if (mPluginFullScreenPlay == null) {
                // 没有全屏用小屏幕插件
                pluginManager.addPlugin(mPluginSmallScreenPlay, player_holder);
                mPluginSmallScreenPlay.pluginEnable = true;
                return;
            }
            pluginManager.addPlugin(mPluginFullScreenPlay, player_holder);
            if (mediaPlayerDelegate.videoInfo == null) {
                return;
            }
        } else {
            if(mPluginSmallScreenPlay != null) {
                pluginManager.addPlugin(mPluginSmallScreenPlay, player_holder);
                mPluginSmallScreenPlay.pluginEnable = true;
            }
        }
    }

    public boolean onPause;

    /**
     * 在正片开始前暂停
     */
    private boolean pauseBeforeLoaded = false;

    public void notifyFav() {
        setFav();
        if (pluginManager != null)
            pluginManager.setFav();
    }

    private void setFav() {
        if (mediaPlayerDelegate != null
                && mediaPlayerDelegate.videoInfo != null) {
            mediaPlayerDelegate.videoInfo.isFaved = true;
        }
    }

    public void clearUpDownFav() {
        if (pluginManager != null)
            pluginManager.clearUpDownFav();
    }

    public void setmPluginFullScreenPlay(PluginOverlay mPluginFullScreenPlay) {
        this.mPluginFullScreenPlay = mPluginFullScreenPlay;
    }

    public void setmPluginSmallScreenPlay(PluginOverlay mPluginSmallScreenPlay) {
        this.mPluginSmallScreenPlay = mPluginSmallScreenPlay;
    }

    private boolean isLocalPlay() {
        return Profile.USE_SYSTEM_PLAYER
                || (mediaPlayerDelegate != null
                && mediaPlayerDelegate.videoInfo != null && StaticsUtil.PLAY_TYPE_LOCAL
                .equals(mediaPlayerDelegate.videoInfo.playType));
    }

    private void getIntentData() {
        Intent intent = mActivity.getIntent();
        if (null != intent && null != intent.getExtras()) {
            String tidString = intent.getExtras().getString("video_id");
            id = tidString;
        }
    }

    public void setDebugInfo(String string) {
        if (null != mYoukuPlayerView) {
            mYoukuPlayerView.setDebugText(string);
        }
    }

    public void setPlayerBlack() {
        if (mYoukuPlayerView != null) {
            mYoukuPlayerView.setPlayerBlack();
        }
    }

    public void playReleateNoRightVideo() {
        pluginManager.onPlayReleateNoRightVideo();
    }

    @Override
    public void onStart() {
        if (pluginManager != null)
            pluginManager.onStart();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("LXF", "========YoukuBasePlayerActivity=====onActivityResult==========");
        if (requestCode == 10999) {
            if (mPlayerAdControl != null) {
                mPlayerAdControl.onLoginDialogComplete();
            }
            return;
        }
    }

    public void onPayClick() {
        if (mediaPlayerDelegate != null) {
            mediaPlayerDelegate.needPay();
        }
    }

    /**
     * 隐藏调查问卷,清晰度切换和付费试看提示�?
     */
    public void hideTipsPlugin() {
        if (mPlayerAdControl != null) {
            mPlayerAdControl.setInvestigateAdHide(true);
        }
        if (mPaytipPlugin != null) {
            mPaytipPlugin.hide();
        }
        if (mChangeQualityPlugin != null) {
            mChangeQualityPlugin.hide();
        }
    }

    /**
     * 取消隐藏
     */
    public void unHideTipsPlugin() {
        if (mPlayerAdControl != null) {
            mPlayerAdControl.setInvestigateAdHide(false);
        }
        if (mPaytipPlugin != null) {
            mPaytipPlugin.unHide();
        }
        if (mChangeQualityPlugin != null) {
            mChangeQualityPlugin.unHide();
        }
    }

    public boolean canShowPluginChangeQuality() {
        return !mPlayerAdControl.getPlugin(PLUGIN_SHOW_INVESTIGATE).isShowing() && !mPaytipPlugin.isShowing();
    }

    public void setPluginHolderPaddingZero() {
        if (player_holder != null) {
            player_holder.setPadding(0, 0, 0, 0);
        }
    }

    /**
     * skip ad on clicked callback
     */
    public void onSkipAdClicked() {
    }

    /**
     * pre ad info getted callback
     */
    public void onAdvInfoGetted(boolean hasAd) {
    }

    public boolean isMidAdShowing() {
        if (mPlayerAdControl != null) {
            return mPlayerAdControl.isMidAdShowing();
        }
        return false;
    }

    public void setAdState(AdState state) {
        if (mPlayerAdControl != null) {
            mPlayerAdControl.setAdState(state);
        }
    }

    @Override
    public void setOnPause(boolean onPause) {
        this.onPause = onPause;
    }


    public void dissmissPauseAD() {
        if (mPlayerAdControl != null) {
            mPlayerAdControl.dismissPauseAD();
        }
    }

    /**
     * **************************implement IPlayerUiControl start ************************
     */
    @Override
    public boolean isOnPause() {
        return onPause;
    }

    @Override
    public void updateVideoId(String videoId) {
        this.id = videoId;
    }

    @Override
    public String getVideoId() {
        return id;
    }

    @Override
    public void setPauseBeforeLoaded(boolean isPause) {
        pauseBeforeLoaded = isPause;
    }

    /**
     * **************************implement IPlayerUiControl end ************************
     */

    public void sendDanmaku(int size, int position, int color, String content) {
        if (mDanmakuManager != null) {
            mDanmakuManager.sendDanmaku(size, position, color, content);
        }
    }

    public void sendDanmaku(LiveDanmakuInfo liveDanmakuInfo) {
        sendDanmaku(25, 1, liveDanmakuInfo.color, liveDanmakuInfo.title);
    }

    public void hideDanmaku() {
        if (mDanmakuManager != null) {
            mDanmakuManager.hideDanmaku();
        }
    }

    public void showDanmaku(){
        if (mDanmakuManager != null) {
            mDanmakuManager.showDanmaku();
        }
    }

    public void openDanmaku() {
        if (mDanmakuManager != null) {
            mDanmakuManager.openDanmaku();
        }
    }

    public void closeDanmaku() {
        if (mDanmakuManager != null) {
            mDanmakuManager.closeDanmaku();
        }
    }

    public void addDanmaku(ArrayList<LiveDanmakuInfo> liveDanmakuInfos) {
        if (mDanmakuManager != null && mDanmakuManager.isHls()) {
            mDanmakuManager.addDanmaku(null, liveDanmakuInfos);
        }
    }

    public boolean isDanmakuClosed() {
        return Profile.getDanmakuSwith(mActivity);
    }

    public IDanmakuManager getDanmakuManager() {
        return mDanmakuManager;
    }

    public void initDanmakuManager(String vid, int cid, boolean isCached) {
        if (mDanmakuManager == null) {
            if (isCached) {
                mDanmakuManager = new LocalDanmakuManager(mActivity, mYoukuPlayerView, mediaPlayerDelegate);
            } else {
                mDanmakuManager = new DanmakuManager(mActivity, mYoukuPlayerView, mediaPlayerDelegate, vid, cid);
            }
        } else {
            if (mDanmakuManager instanceof DanmakuManager && isCached) {
                mDanmakuManager = new LocalDanmakuManager(mActivity, mYoukuPlayerView, mediaPlayerDelegate);
            }
            if (mDanmakuManager instanceof LocalDanmakuManager && !isCached) {
                mDanmakuManager = new DanmakuManager(mActivity, mYoukuPlayerView, mediaPlayerDelegate, vid, cid);
            }
            if (!isCached) {
                mDanmakuManager.setVid(vid, cid);
            }
        }
    }

    @Override
    public void setScreenChangeListener(ScreenChangeListener screenChangeListener) {
        mScreenChangeListener = screenChangeListener;
    }

    @Override
    public PluginManager getPluginManager() {
        return pluginManager;
    }

    @Override
    public YoukuPlayerView getYoukuPlayerView() {
        return mYoukuPlayerView;
    }

    public IPlayerAdControl getPlayerAdControl() {
        return mPlayerAdControl;
    }

    @Override
    public void showSmoothChangeQualityTip(boolean start) {
        if (mChangeQualityPlugin != null) {
            mChangeQualityPlugin.showSmoothChangeQualityTip(start);
        }
    }

    @Override
    public void setUserCallback(IUserCallback callback) {
        mUserCallback = callback;
    }

    @Override
    public IUserCallback getUserCallback() {
        return mUserCallback;
    }

}