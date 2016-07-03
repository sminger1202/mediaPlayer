package com.youku.player.danmaku;

import java.util.ArrayList;

public interface IDanmakuInfoCallBack {
	public void onSuccess(String danmakuInfo, boolean danmakuEnable, boolean isUserShutUp, ArrayList<String> starUids);

	public void onFailed();
}
