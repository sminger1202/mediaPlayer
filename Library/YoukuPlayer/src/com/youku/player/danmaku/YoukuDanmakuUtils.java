package com.youku.player.danmaku;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.baseproject.utils.Logger;
import com.youku.android.player.R;
import com.youku.player.LogTag;
import com.youku.player.base.YoukuPlayerView;
import com.youku.player.goplay.Profile;
import com.youku.player.plugin.MediaPlayerDelegate;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.loader.ILoader;
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.LiveDanmakuInfo;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.parser.android.YoukuDanmakuParser;

public class YoukuDanmakuUtils implements DanmakuUtils {
    private int textSize = 25;

	@Override
	public String getFakeJSONArray() {
		return "{\"data\":{\"1\": [{\"content\": {\"title\":\"hello0\"},\"pub_time\": -1, \"seq\": 82}]}}";
	}

	@Override
	public ILoader createDanmakuLoader() {
		ILoader loader = DanmakuLoaderFactory
				.create(DanmakuLoaderFactory.TAG_YOUKU);
		return loader;
	}

	@Override
	public BaseDanmakuParser createDanmakuParser() {
		return new YoukuDanmakuParser();
	}

	@Override
	public void addDanmaku(JSONObject jsonObject, IDanmakuView danmakuView,
			BaseDanmakuParser parser, long currMillisecond,
			ArrayList<LiveDanmakuInfo> liveDanmakuInfos) {
		long min = Long.MAX_VALUE;
		BaseDanmaku item = null;
		Danmakus danmakus = null;
		danmakus = parser.doParsed(liveDanmakuInfos);
		if (danmakus == null) {
			return;
		}
		Collection<BaseDanmaku> items = danmakus.items;
		Iterator<BaseDanmaku> itr = items.iterator();
		while (itr.hasNext()) {
			item = itr.next();
			if (item.time < min) {
				min = item.time;
			}
		}
		Iterator<BaseDanmaku> itrFinal = items.iterator();
		while (itrFinal.hasNext()) {
			item = itrFinal.next();
			Logger.d(LogTag.TAG_DANMAKU, "text=" + item.text);
			item.time = currMillisecond + (item.time - min) * 1000;
            Logger.d(LogTag.TAG_DANMAKU, "currMillisecond=" + currMillisecond);
			Logger.d(LogTag.TAG_DANMAKU, "time=" + item.time);
			danmakuView.addDanmaku(item);
		}
	}

	@Override
	public long getCurrentMillisecond(BaseDanmakuParser parser,
			long currMillisecond) {
		return currMillisecond;
	}

	@Override
    public void beginDanmaku(String jsonArray, int beginTime,
                             DanmakuManager danmakuManager, YoukuPlayerView youkuPlayerView) {
        if (youkuPlayerView != null) {
            Logger.d(LogTag.TAG_DANMAKU, "开始弹幕");
            youkuPlayerView.beginDanmaku(jsonArray, beginTime);
        }
    }

	@Override
	public void openDanmaku(Context ctx, final DanmakuManager danmakuManager,
			MediaPlayerDelegate mediaPlayerDelegate, String currentVid,
			int currentGolbalPosition) {
		if (danmakuManager == null || ctx == null) {
			return;
		}
		if (Profile.getLiveDanmakuSwith(ctx)) {
			danmakuManager.setDanmakuPreferences(false, "liveDanmakuSwith");
			danmakuManager.showDanmaku();
		}
	}

	@Override
	public void closeDanmaku(Context ctx, final IDanmakuManager danmakuManager) {
		if (danmakuManager == null || ctx == null) {
			return;
		}
		if (!Profile.getLiveDanmakuSwith(ctx)) {
			Logger.d(LogTag.TAG_DANMAKU, "关闭弹幕");
			danmakuManager.setDanmakuPreferences(true, "liveDanmakuSwith");
			danmakuManager.hideDanmaku();
		}
	}

	@Override
	public void showDanmaku(YoukuPlayerView youkuPlayerView,
                            DanmakuManager danmakuManager) {
		if (danmakuManager == null) {
			return;
		}
		if (youkuPlayerView != null) {
			if ((!danmakuManager.isDanmakuShow && !danmakuManager.isDanmakuHide)
					|| (!danmakuManager.isDanmakuShow && danmakuManager.isDanmakuHide)) {
				Logger.d(LogTag.TAG_DANMAKU, "展示弹幕");
				youkuPlayerView.showDanmaku();
				danmakuManager.isDanmakuShow = true;
				danmakuManager.isDanmakuHide = false;
			}
		}
	}

	@Override
	public void hideDanmaku(YoukuPlayerView youkuPlayerView,
                            DanmakuManager danmakuManager) {
		if (danmakuManager == null) {
			return;
		}
		if (youkuPlayerView != null) {
			if ((!danmakuManager.isDanmakuShow && !danmakuManager.isDanmakuHide)
					|| (danmakuManager.isDanmakuShow && !danmakuManager.isDanmakuHide)) {
				Logger.d(LogTag.TAG_DANMAKU, "隐藏弹幕");
				youkuPlayerView.hideDanmaku();
				danmakuManager.isDanmakuShow = false;
				danmakuManager.isDanmakuHide = true;
			}
		}
	}

	@Override
	public void releaseDanmaku(YoukuPlayerView youkuPlayerView,
			MediaPlayerDelegate mediaPlayerDelegate) {
		if (youkuPlayerView != null) {
			Logger.d(LogTag.TAG_DANMAKU, "释放弹幕");
			youkuPlayerView.releaseDanmaku();
		}
	}

	@Override
	public void addDanmaku(String json, YoukuPlayerView youkuPlayerView,
                           DanmakuManager danmakuManager, ArrayList<LiveDanmakuInfo> liveDanmakuInfos) {
		if (youkuPlayerView != null) {
			Logger.d(LogTag.TAG_DANMAKU, "增加弹幕");
			youkuPlayerView.addDanmaku(json, liveDanmakuInfos);
		}
	}

	@Override
	public void showDanmakuWhenRotate(Context ctx, DanmakuManager danmakuManager) {
		if (danmakuManager == null || ctx == null) {
			return;
		}
		if (!Profile.getLiveDanmakuSwith(ctx)) {
			danmakuManager.showDanmaku();
		}
	}

	@Override
	public void hideDanmakuWhenRotate(Context ctx, DanmakuManager danmakuManager) {
		if (danmakuManager == null || ctx == null) {
			return;
		}
		if (!Profile.getLiveDanmakuSwith(ctx)) {
			danmakuManager.hideDanmaku();
		}
	}

	@Override
	public void sendDanmaku(int size, int position, int color, String content,
			MediaPlayerDelegate mediaPlayerDelegate,
			YoukuPlayerView youkuPlayerView, Context ctx, DanmakuManager danmakuManager) {
		if (Profile.getLiveDanmakuSwith(ctx))
			return;
		if (youkuPlayerView != null) {
			Logger.d(LogTag.TAG_DANMAKU, "发送弹幕");
			youkuPlayerView.sendDanmaku(size, position, color, content);
		}
	}

    @Override
	public void resetAndReleaseDanmakuInfo(final IDanmakuManager danmakuManager,
			Boolean isHLS) {
		if (danmakuManager == null || !isHLS ) {
			return;
		}
		danmakuManager.hideDanmaku();
		danmakuManager.resetDanmakuInfo();
		if (DanmakuManager.danmakuHandler != null) {
            DanmakuManager.danmakuHandler.postDelayed(new Runnable() {

				@Override
				public void run() {
					danmakuManager.releaseDanmaku();
				}
			}, 100);
		}
	}

    @Override
    public int getDanmakuSendColor(int color) {
        return color | 0xFF000000;
    }

    @Override
    public void setTextSize(BaseDanmakuParser parser, Context ctx) {
        if (parser == null || ctx == null) {
            return;
        }
        textSize = ctx.getResources().getDimensionPixelSize(R.dimen.danmaku_text_size);
        parser.setTextSize(textSize);
    }

    @Override
    public int getTextSize() {
        return textSize;
    }

	@Override
	public void setDanmakuContextAndDrawable(DanmakuContext danmakuContext, Drawable drawable) {

	}

	@Override
	public void requestStarImage(BaseDanmaku item, IDanmakuView danmakuView) {

	}
}
