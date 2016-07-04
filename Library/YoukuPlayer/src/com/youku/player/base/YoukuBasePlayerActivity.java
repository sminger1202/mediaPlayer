package com.youku.player.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;

import com.baseproject.utils.Logger;
import com.youku.player.LogTag;
import com.youku.player.ad.AdState;
import com.youku.player.ad.api.IAdControlListener;
import com.youku.player.apiservice.IPlayerUiControl;
import com.youku.player.apiservice.IUserCallback;
import com.youku.player.apiservice.OnInitializedListener;
import com.youku.player.apiservice.ScreenChangeListener;
import com.youku.player.danmaku.IDanmakuManager;
import com.youku.player.goplay.Profile;
import com.youku.player.plugin.MediaPlayerDelegate;
import com.youku.player.plugin.PluginOverlay;
import com.youku.player.ui.widget.YoukuAnimation;
import com.youku.player.util.DetailMessage;

import java.util.ArrayList;

import master.flame.danmaku.danmaku.model.LiveDanmakuInfo;

@SuppressLint("NewApi")
public abstract class YoukuBasePlayerActivity extends AppCompatActivity
        implements DetailMessage, ScreenChangeListener, OnInitializedListener, IAdControlListener, IUserCallback {
    public static final String TAG = YoukuBasePlayerActivity.class.getSimpleName();
    public static boolean isHighEnd; // 是否高端机型

    public static final int END_REQUEST = 201;

    public static final int END_PLAY = 202;

    // 因为切换�?g暂停
    public boolean is3GPause = false;

    IPlayerUiControl mPlayerController;

    protected static Handler handler = new Handler() {
    };

    public MediaPlayerDelegate getMediaPlayerDelegate() {
        return mPlayerController.getMediaPlayerDelegate();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected void onPause() {
        super.onPause();
        if(null != mPlayerController) {
            mPlayerController.onPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(null != mPlayerController) {
            mPlayerController.onStop();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(null != mPlayerController) {
            mPlayerController.onNewIntent(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(null != mPlayerController) {
            mPlayerController.onResume();
            setOrientionDisable();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null != mPlayerController) {
            mPlayerController.onDestroy();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(null != mPlayerController) {
            mPlayerController.onConfigurationChanged(newConfig);
        }
    }

    public abstract void setPadHorizontalLayout();


    /**
     * 互动娱乐显示WebView
     *
     * @param width WebView 宽度
     * @param fragment  WebViewFragment
     */
    public void showWebView(int width,Fragment fragment) {
        if(null != mPlayerController) {
            mPlayerController.showWebView(width, fragment);
        }
    }

    public boolean isWebViewShown() {
        if(null != mPlayerController) {
            return mPlayerController.isWebViewShown();
        } else {
            return false;
        }
    }

    /**
     * 隐藏WebView
     */
    public void hideWebView() {
        if(null != mPlayerController) {
            mPlayerController.hideWebView();
        }
    }

    public void goSmall() {
        if(null != mPlayerController) {
            mPlayerController.goSmall();
        }
    }

    public void goFullScreen() {
        if(null != mPlayerController) {
            mPlayerController.goFullScreen();
        }
    }

    public void addPlugins() {
        if(null != mPlayerController) {
            mPlayerController.addPlugins();
        }
    }

    @Override
    public boolean onSearchRequested() {
        if(null != mPlayerController) {
            return mPlayerController.onSearchRequested();
        } else {
            return false;
        }
    }

//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (null != mPlayerController && mPlayerController.onKeyDown(keyCode, event)) {
//            Logger.d(TAG, "play Activity controller onKeyDown true");
//            return true;
//        } else {
//            Logger.d(TAG, "play Activity super onKeyDown");
//            return super.onKeyDown(keyCode, event);
//        }
//    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(null != mPlayerController) {
            mPlayerController.onBackPressed();
        }
    }

    @Override
    public void onInitializationSuccess(YoukuPlayer player) {
        mPlayerController = player.getPlayerUiControl();
        mPlayerController.setUserCallback(this);
    }


    /**
     * 全屏的回�?当程序全屏的时候应该将其他的view都设置为gone
     */
    public abstract void onFullscreenListener();

    /**
     * 小屏幕的回调 当程序全屏的时候应该显示其他的view
     */
    public abstract void onSmallscreenListener();

    public void resizeMediaPlayer(int percent) {
        if(null != mPlayerController) {
            mPlayerController.resizeMediaPlayer(percent);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if(null != mPlayerController) {
            mPlayerController.onLowMemory();
        }
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


    public void setmPluginFullScreenPlay(PluginOverlay mPluginFullScreenPlay) {
        if(null != mPlayerController) {
            mPlayerController.setmPluginFullScreenPlay(mPluginFullScreenPlay);
        }
    }

    public void setmPluginSmallScreenPlay(PluginOverlay mPluginSmallScreenPlay) {
        if(null != mPlayerController) {
            mPlayerController.setmPluginSmallScreenPlay(mPluginSmallScreenPlay);
        }
    }

    public void clearUpDownFav() {
        if(null != mPlayerController) {
            mPlayerController.clearUpDownFav();
        }
    }

    protected void detectPlugin() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(null != mPlayerController) {
                    mPlayerController.detectPlugin();
                }
            }
        });
    }

    protected void changeConfiguration(Configuration newConfig) {
        if(null != mPlayerController) {
            mPlayerController.changeConfiguration(newConfig);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(null != mPlayerController) {
//            mPlayerController.onStart();
//            mPlayerController.onPause();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(null != mPlayerController) {
            mPlayerController.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void onPayClick() {
        if(null != mPlayerController) {
            mPlayerController.onPayClick();
        }
    }

    /**
     * 隐藏调查问卷,清晰度切换和付费试看提示�?
     */
    public void hideTipsPlugin() {
        if(null != mPlayerController) {
            mPlayerController.hideTipsPlugin();
        }
    }

    /**
     * 取消隐藏
     */
    public void unHideTipsPlugin() {
        if(null != mPlayerController) {
            mPlayerController.unHideTipsPlugin();
        }
    }

    /**
     * loading mid ad
     */
    public void onMidAdLoadingStartListener() {
    }

    public void onMidAdLoadingEndListener() {
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
        if(null != mPlayerController) {
            return mPlayerController.isMidAdShowing();
        }
        return false;
    }

    public void setAdState(AdState state) {
        if(null != mPlayerController) {
            mPlayerController.setAdState(state);
        }
    }

    public void dissmissPauseAD() {
        if(null != mPlayerController) {
            mPlayerController.dissmissPauseAD();
        }
    }

    public void hideDanmaku() {
        if(null != mPlayerController) {
            mPlayerController.hideDanmaku();
        }
    }

    public void showDanmaku() {
        if(null != mPlayerController) {
            mPlayerController.showDanmaku();
        }
    }

    public void sendDanmaku(int size, int position, int color, String content) {
        if(null != mPlayerController) {
            mPlayerController.sendDanmaku(size, position, color, content);
        }
    }

    public void sendDanmaku(LiveDanmakuInfo liveDanmakuInfo) {
        if(null != mPlayerController) {
            mPlayerController.sendDanmaku(liveDanmakuInfo);
        }
    }

    public void openDanmaku() {
        if(null != mPlayerController) {
            mPlayerController.openDanmaku();
            getDanmakuManager().showDanmaku();
        }
    }

    public void closeDanmaku() {
        if(null != mPlayerController) {
            mPlayerController.closeDanmaku();
        }
        //多加一次关闭置位，防止初次加载弹幕manager还没有初始化成功。
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this)
                .edit();
        editor.putBoolean("danmakuSwith", true);
        editor.commit();
    }

    public void addDanmaku(ArrayList<LiveDanmakuInfo> liveDanmakuInfos) {
        if(null != mPlayerController) {
            mPlayerController.addDanmaku(liveDanmakuInfos);
        }
    }

    public boolean isDanmakuClosed() {
        if(null != mPlayerController) {
            return mPlayerController.isDanmakuClosed();
        }
        return false;
    }

    public IDanmakuManager getDanmakuManager() {
        if(null != mPlayerController) {
            return mPlayerController.getDanmakuManager();
        }
        return null;
    }

    public void initDanmakuManager(String vid, int cid) {
        if(null != mPlayerController) {
            mPlayerController.initDanmakuManager(vid, cid, false);
        }
    }


    public IPlayerUiControl getPlayerUiControl() {
        return mPlayerController;
    }

    @Override
    public void updatePlugin(int pluginId) {
        if(null != mPlayerController) {
            mPlayerController.updatePlugin(pluginId);
        }
    }

    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        Logger.d(LogTag.TAG_PLAYER, "setRequestedOrientation:" + requestedOrientation);
        super.setRequestedOrientation(requestedOrientation);
    }

    public boolean isOrientationEnable() {
        if(null != mPlayerController) {
            return mPlayerController.isOrientationEnable();
        }
        return false;
    }

    public void setOrientionEnable() {
        if(null != mPlayerController) {
            mPlayerController.setOrientionEnable();
        }
    }

    public void setOrientionDisable() {
        if(null != mPlayerController) {
            mPlayerController.setOrientionDisable();
        }
    }
    /**
     * 平滑清晰度切换开始
     */
    public void onQualitySmoothChangeStart(int quality) {}

    /**
     * 清晰度平滑切换结束
     */
    public void onQualitySmoothChangeEnd(int quality) {}

}