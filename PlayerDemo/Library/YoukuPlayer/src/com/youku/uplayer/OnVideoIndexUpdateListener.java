package com.youku.uplayer;

/**
 * 播放
 * @author yuanfang
 *
 */
public interface OnVideoIndexUpdateListener {
	
	/**
	 * 播放位置变化
	 * @param 分片
	 * @param ip
	 */
	void onVideoIndexUpdate(int currentIndex, int ip);
}
