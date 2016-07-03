package com.youku.player.danmaku;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;

import com.baseproject.utils.Logger;
import com.youku.libmanager.FileUtils;
import com.youku.player.LogTag;
import com.youku.player.base.YoukuPlayerView;
import com.youku.player.config.MediaPlayerConfiguration;
import com.youku.player.goplay.Profile;
import com.youku.player.plugin.MediaPlayerDelegate;

import java.io.File;
import java.util.ArrayList;

import master.flame.danmaku.danmaku.model.LiveDanmakuInfo;

public class LocalDanmakuManager implements IDanmakuManager {
    private YoukuPlayerView mYoukuPlayerView;
    private Context context;
    private MediaPlayerDelegate mMediaPlayerDelegate;
    public DanmakuUtils danmakuUtils;
    public boolean isPaused = false;
    public int danmakuProcessStatus;
    public int beginMs = 0;
    public boolean isCanSeek = false;
    public static String TAG = LogTag.TAG_LOCAL_DANMAKU;
    public static Handler danmakuHandler;
    public Looper danmakuLooper;
    public static final int DANMAKUOPEN = 0x1;
    public static final int DANMAKUPLAY = 0x2;
    public static final int DANMAKUPAUSE = 0x4;

    public LocalDanmakuManager(Context context, YoukuPlayerView view,
                               MediaPlayerDelegate mediaPlayerDelegate) {
        this.context = context;
        mYoukuPlayerView = view;
        mMediaPlayerDelegate = mediaPlayerDelegate;
        danmakuUtils = MediaPlayerConfiguration.getInstance().getDanmakuUtils();
        HandlerThread thread = new HandlerThread("danmuThread");
        thread.start();
        danmakuLooper = thread.getLooper();
        danmakuHandler = new Handler(danmakuLooper);
    }

    @Override
    public void beginDanmaku(final String path, final int beginTime) {
        if (danmakuProcessStatus != 0) {
            Logger.d(TAG, "单视频重复播放，不再begin");
            return;
        }

        if (danmakuHandler != null) {
            danmakuHandler.post(new Runnable() {

                @Override
                public void run() {
                    Long before = System.currentTimeMillis();
                    if (mYoukuPlayerView != null) {
                        if (fileToString(path) == null) {
                            Logger.d(TAG, "弹幕文件为空");
                            return;
                        }
                        danmakuProcessStatus = DANMAKUOPEN;
                        mYoukuPlayerView.beginDanmaku(fileToString(path), beginMs);
                        Logger.d(TAG, "开始弹幕:" + beginMs + " path:" + path);
                    }
                    danmakuProcessStatus |= DANMAKUPLAY;
                    if (Profile.getDanmakuSwith(context)) {
                        hideDanmaku();
                    } else {
                        showDanmaku();
                    }
                    isCanSeek = true;
                    Long now = System.currentTimeMillis();
                    Logger.d(TAG, "加载了:" + (now - before) + "毫秒");
                }
            });
        }
    }

    private String fileToString(String path) {
        File f = new File(path);
        if (f == null || !f.exists()) {
            return null;
        }
        return FileUtils.file2String(f, "UTF-8");
    }

    @Override
    public void openDanmaku() {
        if (Profile.getDanmakuSwith(context)) {
            setDanmakuPreferences(false, "danmakuSwith");
            Logger.d(TAG, "打开弹幕");
            if (!isPaused) {
                showDanmaku();
            }
        }
    }

    @Override
    public void closeDanmaku() {
        if (!Profile.getDanmakuSwith(context)) {
            Logger.d(TAG, "关闭弹幕");
            setDanmakuPreferences(true, "danmakuSwith");
            hideDanmaku();
        }
    }

    @Override
    public void pauseDanmaku() {
        if (((danmakuProcessStatus & DANMAKUOPEN) != 0)
                && ((danmakuProcessStatus & DANMAKUPLAY) != 0)) {
            if (mYoukuPlayerView != null) {
                danmakuProcessStatus = DANMAKUPAUSE | DANMAKUOPEN;
                mYoukuPlayerView.pauseDanmaku();
                Logger.d(TAG, "暂停弹幕");
            }
        }
    }

    @Override
    public void resumeDanmaku() {
        if (isPaused) {
            return;
        }
        if (((danmakuProcessStatus & DANMAKUOPEN) != 0)
                && ((danmakuProcessStatus & DANMAKUPAUSE) != 0)) {
            if (mYoukuPlayerView != null) {
                danmakuProcessStatus = DANMAKUPLAY | DANMAKUOPEN;
                mYoukuPlayerView.resumeDanmaku();
                Logger.d(TAG, "继续弹幕");
            }
        }
    }

    @Override
    public void seekToDanmaku(int ms) {
        if (mYoukuPlayerView != null) {
            beginMs = ms;
            if (isCanSeek) {
                new seekDanmakuRunnable(ms).run();
            }
        }
    }

    class seekDanmakuRunnable implements Runnable {

        int ms;

        public seekDanmakuRunnable(int ms) {
            this.ms = ms;
        }
        @Override
        public void run() {
            if (danmakuProcessStatus > DANMAKUOPEN) {
                if (mYoukuPlayerView != null) {
                    mYoukuPlayerView.seekToDanmaku((long) ms);
                    danmakuProcessStatus = DANMAKUPLAY | DANMAKUOPEN;
                    Logger.d(TAG, "seek to" + ms);
                }
            } else {
                if (danmakuHandler != null) {
                    danmakuHandler
                            .postDelayed(new seekDanmakuRunnable(ms), 200);
                    Logger.d(TAG, "弹幕尚未准备好，延迟200毫秒再重试");
                }
            }
        }

    }

    @Override
    public void showDanmaku() {
        if (danmakuProcessStatus > DANMAKUOPEN && mYoukuPlayerView != null) {
            mYoukuPlayerView.showDanmaku();
            Logger.d(TAG, "展示弹幕" + Log.getStackTraceString(new Throwable()));
        }
    }

    @Override
    public void hideDanmaku() {
        if (danmakuProcessStatus > DANMAKUOPEN && mYoukuPlayerView != null) {
            mYoukuPlayerView.hideDanmaku();
            Logger.d(TAG, "隐藏弹幕");
        }
    }

    @Override
    public void releaseDanmaku() {
        if (mYoukuPlayerView != null) {
            mYoukuPlayerView.releaseDanmaku();
            Logger.d(TAG, "释放弹幕");
        }
    }

    @Override
    public void hideDanmakuWhenOpen() {
        if (!Profile.getDanmakuSwith(context)) {
            isPaused = true;
            hideDanmaku();
            pauseDanmaku();
        }
    }

    @Override
    public void continueDanmaku() {
        if (isPaused) {
            isPaused = false;
            if (!Profile.getDanmakuSwith(context)) {
                showDanmaku();
            }
        }
    }

    @Override
    public void hideDanmakuAgain() {
        if (isPaused) {
            if (mYoukuPlayerView != null) {
                mYoukuPlayerView.hideDanmaku();
            }
        }
    }

    @Override
    public void resetDanmakuInfo() {
        danmakuProcessStatus = 0;
        isPaused = false;
        beginMs = 0;
        isCanSeek = false;
    }

    @Override
    public void resetAndReleaseDanmakuInfo() {
        hideDanmaku();
        resetDanmakuInfo();
        if (danmakuHandler != null) {
            danmakuHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    releaseDanmaku();
                }
            }, 100);
        }
    }

    @Override
    public void releaseDanmakuWhenDestroy() {
        if (!MediaPlayerConfiguration.getInstance().hideDanmaku()) {
            releaseDanmaku();
        }
    }

    @Override
    public void setDanmakuPreferences(boolean danmakuSwith, String key) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context)
                .edit();
        editor.putBoolean(key, danmakuSwith);
        editor.commit();
    }

    //下面代码均为空
    @Override
    public void onPositionChanged(int currentPosition) {

    }

    @Override
    public void setDanmakuTextScale(boolean isFullScreenPlay) {

    }

    @Override
    public void handleDanmakuEnable(boolean danmakuEnable) {

    }

    @Override
    public boolean isPaused() {
        return false;
    }

    @Override
    public boolean isHls() {
        return false;
    }

    @Override
    public void setVid(String vid, int cid) {

    }

    @Override
    public void handleDanmakuInfo(String iid, int minute_at, int minute_count) {

    }

    @Override
    public void sendDanmaku(int size, int position, int color, String content) {

    }

    @Override
    public void sendDanmaku(int color, String content) {

    }

    @Override
    public void addDanmaku(String json, ArrayList<LiveDanmakuInfo> liveDanmakuInfos) {

    }

    @Override
    public void addDanmaku(String json) {

    }

    @Override
    public void setDanmakuPosition(int position) {

    }

    @Override
    public void setDanmakuEffect(int effect) {

    }

    @Override
    public boolean isDanmakuClosed() {
        return false;
    }

    @Override
    public void startLiveDanmaku() {

    }

    @Override
    public void setDanmakuVisibleWhenLive() {

    }

    @Override
    public void showDanmakuWhenRotate() {

    }

    @Override
    public void hideDanmakuWhenRotate() {

    }

    @Override
    public int getDanmakuCount(String danmakuInfo) {
        return 0;
    }

    @Override
    public void closeCMSDanmaku() {

    }
}
