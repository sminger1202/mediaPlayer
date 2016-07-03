package com.youku.uplayer;

/**
 * 广告倒计时
 * 
 * @author longfan
 * 
 */
public interface OnADCountListener {

	/**
	 * 当倒计时更新
	 * @param count 
	 * 
	 * @return
	 */
	void onCountUpdate(int count);
}