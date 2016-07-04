package com.youku.player.danmaku;

import android.content.Context;

import com.baseproject.utils.Logger;
import com.youku.player.LogTag;
import com.youku.player.util.URLContainer;
import com.youku.statistics.StatisticsTask;

public class MyGetDanmakuManager {

	public void goGetDanmakuUrl(String iid, int minute_at, int minute_count,
			IDanmakuInfoCallBack danmakuInfoCallBack) {
		GetDanmakuUrlService getDanmakuUrlService = new GetDanmakuUrlService();
		getDanmakuUrlService.getDanmakuUrl(iid, minute_at, minute_count,
				danmakuInfoCallBack);
	}

	public void goGetDanmakuStatus(String iid, int cid,
			IDanmakuInfoCallBack danmakuInfoCallBack) {
		GetDanmakuStatusService getDanmakuStatusService = new GetDanmakuStatusService();
		getDanmakuStatusService.getDanmakuStatus(iid, cid,
				danmakuInfoCallBack);
	}

	public void submitDanmaku(String ver, String iid, String playat,
			String propertis, String content, Context context) {
		String url = URLContainer.submitDanmakuParameter(ver, iid, playat,
				propertis, content);
		Logger.d(LogTag.TAG_DANMAKU, "submitDanmakuUrl" + url);
		final StatisticsTask statisticsTask = new StatisticsTask(url, context,
				true, true);
		statisticsTask.execute();
	}
}
