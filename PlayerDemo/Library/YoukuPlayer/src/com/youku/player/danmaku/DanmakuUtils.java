package com.youku.player.danmaku;

import android.content.Context;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;

import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.loader.ILoader;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.LiveDanmakuInfo;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;

import org.json.JSONObject;

import com.youku.player.base.YoukuPlayerView;
import com.youku.player.plugin.MediaPlayerDelegate;

public interface DanmakuUtils {

	public String getFakeJSONArray();

	public ILoader createDanmakuLoader();

	public BaseDanmakuParser createDanmakuParser();

	public void addDanmaku(JSONObject jsonObject, IDanmakuView danmakuView,
			BaseDanmakuParser parser, long currMillisecond, ArrayList<LiveDanmakuInfo> liveDanmakuInfos);

	public long getCurrentMillisecond(BaseDanmakuParser parser,
			long currMillisecond);

    public int getDanmakuSendColor(int color);

	public void beginDanmaku(String jsonArray, int beginTime,
                             DanmakuManager danmakuManager, YoukuPlayerView youkuPlayerView);

	public void openDanmaku(Context ctx, DanmakuManager danmakuManager,
			MediaPlayerDelegate mediaPlayerDelegate, String currentVid,
			int currentGolbalPosition);

	public void closeDanmaku(Context ctx, IDanmakuManager danmakuManager);

	public void showDanmaku(YoukuPlayerView youkuPlayerView,
                            DanmakuManager danmakuManager);

	public void hideDanmaku(YoukuPlayerView youkuPlayerView,
                            DanmakuManager danmakuManager);

	public void releaseDanmaku(YoukuPlayerView youkuPlayerView,
			MediaPlayerDelegate mediaPlayerDelegate);

	public void addDanmaku(String json, YoukuPlayerView youkuPlayerView,
                           DanmakuManager danmakuManager, ArrayList<LiveDanmakuInfo> liveDanmakuInfos);

	public void showDanmakuWhenRotate(Context ctx, DanmakuManager danmakuManager);

	public void hideDanmakuWhenRotate(Context ctx, DanmakuManager danmakuManager);

	public void sendDanmaku(int size, int position, int color, String content,
			MediaPlayerDelegate mediaPlayerDelegate,
			YoukuPlayerView youkuPlayerView, Context ctx, DanmakuManager danmakuManager);

	public void resetAndReleaseDanmakuInfo(IDanmakuManager danmakuManager,
			Boolean isHLS);

    public void setTextSize(BaseDanmakuParser parser, Context ctx);

    public int getTextSize();

	public void setDanmakuContextAndDrawable(DanmakuContext danmakuContext, Drawable drawable);

	public void requestStarImage(BaseDanmaku item, IDanmakuView danmakuView);
}
