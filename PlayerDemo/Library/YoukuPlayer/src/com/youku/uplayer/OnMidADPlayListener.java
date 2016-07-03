package com.youku.uplayer;

public interface OnMidADPlayListener {

	/**
	 * 当开始播放中插广告的时候
	 * @param index 
	 * 
	 * @return
	 */
	boolean onStartPlayMidAD(int index);

	/**
	 * 当结束播放中插广告的时候
	 * @param index 
	 * 
	 * @return
	 */
	boolean onEndPlayMidAD(int index);

	/**
	 * 当开始播放中插广告的时候
	 * @param index 
	 * 
	 * @return
	 */
	void onLoadingMidADStart();
}
