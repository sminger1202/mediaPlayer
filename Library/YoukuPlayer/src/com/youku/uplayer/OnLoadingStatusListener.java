package com.youku.uplayer;

/**
 * 播放器转圈的listenner
 * 
 * @author longfan
 * 
 */
public interface OnLoadingStatusListener {

	/**
	 * 当开始转圈
	 * 
	 * @return
	 */
	void onStartLoading();

	/**
	 * 当结束转圈
	 * 
	 * @return
	 */
	void onEndLoading();
}