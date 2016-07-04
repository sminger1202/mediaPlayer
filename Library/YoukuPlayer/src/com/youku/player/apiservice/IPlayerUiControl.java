package com.youku.player.apiservice;

import android.content.res.Configuration;
import android.support.v4.app.Fragment;

import com.youku.player.ad.AdState;
import com.youku.player.base.YoukuPlayerView;
import com.youku.player.danmaku.IDanmakuManager;
import com.youku.player.plugin.MediaPlayerDelegate;
import com.youku.player.plugin.PluginManager;
import com.youku.player.plugin.PluginOverlay;
import com.youku.player.subtitle.DownloadedSubtitle;
import com.youku.player.subtitle.SubtitleOperate;
import com.youku.player.util.DeviceOrientationHelper;

import java.util.ArrayList;

import master.flame.danmaku.danmaku.model.LiveDanmakuInfo;

/**
 * player view related operations
 */
public interface IPlayerUiControl extends ActivityCallback,DeviceOrientationHelper.OrientationChangeCallback {

    /**
     * get YoukuPlayerView object
     *
     * @return YoukuPlayerView
     */
    YoukuPlayerView getYoukuPlayerView();

    /**
     * 初始化播放器UI控件
     */
    void initPlayerPart();

    /**
     * 更新plugin
     *
     * @param pluginID
     */
    void updatePlugin(final int pluginID);

    /**
     * 设置所有plugin的父容器的padding为0
     */
    void setPluginHolderPaddingZero();

    /**
     * 隐藏互动娱乐webview
     */
    void hideWebView();

    /**
     * 隐藏互动娱乐popwindow
     */
    void hideInteractivePopWindow();

    /**
     * 获取字幕控制器
     *
     * @return SubtitleOperate
     */
    SubtitleOperate getSubtitleOperate();

    /**
     * 清除字幕
     */
    void clearSubtitle();

    /**
     * 字幕下载完成
     */
    void onDownloadSubtitle(DownloadedSubtitle subtitle, int type);

    /**
     * 获取弹幕控制器
     *
     * @return DanmakuManager
     */
    IDanmakuManager getDanmakuManager();

    /**
     * 初始化弹幕控制器
     */
    void initDanmakuManager(String vid, int cid, boolean isCached);

    /**
     * activity 是否pause
     */
    boolean isOnPause();

    /**
     * 更新上页传递ID video/show
     */
    void updateVideoId(String videoId);

    /**
     * 获取上页传递ID video/show
     */
    String getVideoId();

    /**
     * 判断当前是否可以转屏
     * @return true 允许转屏
     */
    boolean isOrientationEnable();
    /**
     * 禁止转屏
     */
    void setOrientionDisable();

    /**
     * 允许转屏
     */
    void setOrientionEnable();

    /**
     * 切换到全屏模式
     */
    void goFullScreen();

    /**
     * 切换到竖屏全屏模式
     */
    void goVerticalFullScreen();

    /**
     * 获取当前是否是竖屏全屏状态
     * @return
     */
    boolean isVerticalFullScreen();

    /**
     * 切换到小屏模式
     */
    void goSmall();

    /**
     * 是否允许显示切换视频清晰度提示
     */
    boolean canShowPluginChangeQuality();

    /**
     * 设置视频第一帧显示前是否挂起标志位
     */
    void setPauseBeforeLoaded(boolean isPause);

    /**
     * 视频播放完成，旋转至小屏UI
     */
    void playCompleteGoSmall();

    /**
     * 清除对player ui操作的handler消息
     */
    void removeHandlerMessage();

    MediaPlayerDelegate getMediaPlayerDelegate();

    PluginManager getPluginManager();

    void initLayoutView();

    void setScreenChangeListener(ScreenChangeListener screenChangeListener);

    void showWebView(int width,Fragment fragment);

    void addPlugins();

    boolean isWebViewShown();

    void resizeMediaPlayer(int percent);

    void setmPluginFullScreenPlay(PluginOverlay mPluginFullScreenPlay);

    void setmPluginSmallScreenPlay(PluginOverlay mPluginSmallScreenPlay);

    void clearUpDownFav();

    void detectPlugin();

    void changeConfiguration(Configuration newCoreen);


    void onPayClick();

    /**
     * 隐藏调查问卷,清晰度切换和付费试看提示�?
     */
    void hideTipsPlugin();

    /**
     * 取消隐藏
     */
    void unHideTipsPlugin();


    void dissmissPauseAD();

    void sendDanmaku(int size, int position, int color, String content);

    void sendDanmaku(LiveDanmakuInfo liveDanmakuInfo);

    void hideDanmaku();

    void showDanmaku();

    void openDanmaku();

    void closeDanmaku();

    void addDanmaku(ArrayList<LiveDanmakuInfo> liveDanmakuInfos);

    boolean isDanmakuClosed();

    boolean isMidAdShowing();

    void setAdState(AdState state);

    void setOnPause(boolean onPause);

    void onParseNoRightVideoSuccess();

    void showSmoothChangeQualityTip(boolean start);

    void setUserCallback(IUserCallback callback);

    IUserCallback getUserCallback();
}
