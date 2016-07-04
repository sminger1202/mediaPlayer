package com.youku.uplayer;

/**
 * 当前网速的接口
 * 
 * @author LongFan
 * 
 */
public interface OnNetworkSpeedListener {

	/**
	 * 网速更新更新
	 * 
	 * @param count
	 * 
	 * @return
	 */
	void onSpeedUpdate(int count);
}