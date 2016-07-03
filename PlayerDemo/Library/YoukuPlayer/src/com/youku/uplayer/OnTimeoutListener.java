package com.youku.uplayer;

public interface OnTimeoutListener {
	
	// 超时
	void onTimeOut();
	
	// 切换清晰度
	void onNotifyChangeVideoQuality();
}
