package com.youku.player.danmaku;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;

import com.baseproject.utils.Logger;
import com.youku.player.service.NetService;
import com.youku.player.util.URLContainer;

import java.util.ArrayList;

public class GetDanmakuStatusService implements NetService {
	// 回调函数的监听变量
	private IDanmakuInfoCallBack danmakuInfoCallBack;
	private String danmakuStatus;
	private boolean danmakuEnable;
	private boolean isUserShutUp;
	private ArrayList<String> starUids = new ArrayList<String>();

	public GetDanmakuStatusService() {

	}

	public void getDanmakuStatus(String iid, int cid,
			IDanmakuInfoCallBack danmakuInfoCallBack) {
		String url = URLContainer.getDanmakuStatusParameter(iid, cid);
		this.danmakuInfoCallBack = danmakuInfoCallBack;
		TaskGetDanmakuInfo taskGetDanmakuStatus = new TaskGetDanmakuInfo(url);
		taskGetDanmakuStatus.setSuccess(SUCCESS);
		taskGetDanmakuStatus.setFail(FAIL);
		taskGetDanmakuStatus.execute(handler);
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SUCCESS:
				if ((String) msg.obj == null) {
					return;
				}
				setDanmakuStatus((String) msg.obj);
				danmakuInfoCallBack.onSuccess(danmakuStatus, danmakuEnable, isUserShutUp, starUids);
				break;
			case FAIL:
				danmakuInfoCallBack.onFailed();
				break;
			}

		}
	};

	private void setDanmakuStatus(String responseString) {
		try {
			JSONObject json = new JSONObject(
					responseString);
			if (json.has("data")) {
				JSONObject data = json.optJSONObject("data");
				if (data != null) {
					danmakuStatus = data.optString("m_points", "");
					danmakuEnable = data.optBoolean("danmu_enable", true);
					isUserShutUp = data.optBoolean("user_shut_up", false);
					JSONArray style_uidcodes = data.optJSONArray("style_uidcodes");
					if (style_uidcodes != null && style_uidcodes.length() > 0) {
						for (int i = 0; i < style_uidcodes.length(); i++) {
							JSONObject uidcode = style_uidcodes.optJSONObject(i);
							if (uidcode.has("id")) {
								starUids.add(uidcode.optString("id"));
							}
						}
					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
