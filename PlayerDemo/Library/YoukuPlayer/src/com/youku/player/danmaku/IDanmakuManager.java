package com.youku.player.danmaku;

import java.util.ArrayList;

import master.flame.danmaku.danmaku.model.LiveDanmakuInfo;

public interface IDanmakuManager {
	void setVid(String vid, int cid);

	void handleDanmakuInfo(String iid, int minute_at,
						   int minute_count);

	void sendDanmaku(int size, int position, int color, String content);

	void sendDanmaku(int color, String content);

	void beginDanmaku(String jsonArray, int beginTime);

	void addDanmaku(String json, ArrayList<LiveDanmakuInfo> liveDanmakuInfos);

	void addDanmaku(String json);

	void setDanmakuPosition(int position);

	void setDanmakuEffect(int effect);

	void pauseDanmaku();

	void resumeDanmaku();

	void seekToDanmaku(int ms);

	void openDanmaku();

	void closeDanmaku();

	void closeCMSDanmaku();

	boolean isDanmakuClosed();

	void showDanmaku();

	void hideDanmaku();

	void releaseDanmaku();

	void setDanmakuPreferences(boolean danmakuSwith, String key);

	void startLiveDanmaku();

	void setDanmakuVisibleWhenLive();

	void showDanmakuWhenRotate();

	void hideDanmakuWhenRotate();

	int getDanmakuCount(String danmakuInfo);

	void hideDanmakuWhenOpen();

	void continueDanmaku();

	void hideDanmakuAgain();

	void resetDanmakuInfo();

	void resetAndReleaseDanmakuInfo();

	void onPositionChanged(int currentPosition);

	void releaseDanmakuWhenDestroy();

	void setDanmakuTextScale(boolean isFullScreenPlay);

	void handleDanmakuEnable(boolean danmakuEnable);

	boolean isPaused();

	boolean isHls();
}
