package com.youku.uplayer;

/**
 * 播放位置变化接口
 * @author yuanfang
 *
 */
public interface OnCurrentPositionUpdateListener {
	/**
	 * 播放位置变化
	 * @param currentPosition
	 */
	void onCurrentPositionUpdate(int currentPosition);
}
