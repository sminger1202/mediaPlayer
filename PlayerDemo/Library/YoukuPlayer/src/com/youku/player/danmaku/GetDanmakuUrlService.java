package com.youku.player.danmaku;

import android.os.Handler;
import android.os.Message;

import com.baseproject.utils.Logger;
import com.youku.player.service.NetService;
import com.youku.player.util.URLContainer;

public class GetDanmakuUrlService implements NetService {
	// 回调函数的监听变量
	private IDanmakuInfoCallBack danmakuInfoCallBack;

	public GetDanmakuUrlService() {

	}

	public void getDanmakuUrl(String iid, int minute_at, int minute_count,
			IDanmakuInfoCallBack danmakuInfoCallBack) {
		String url = URLContainer.getDanmakuParameter(iid, minute_at,
				minute_count);
		this.danmakuInfoCallBack = danmakuInfoCallBack;
		TaskGetDanmakuInfo taskGetDanmakuUrl = new TaskGetDanmakuInfo(url);
		taskGetDanmakuUrl.setSuccess(SUCCESS);
		taskGetDanmakuUrl.setFail(FAIL);
		taskGetDanmakuUrl.execute(handler);
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SUCCESS:
				if ((String) msg.obj == null) {
					return;
				}
				danmakuInfoCallBack.onSuccess((String) msg.obj, true, false, null);
				break;
			case FAIL:
				danmakuInfoCallBack.onFailed();
				break;
			}
		}
	};
}
