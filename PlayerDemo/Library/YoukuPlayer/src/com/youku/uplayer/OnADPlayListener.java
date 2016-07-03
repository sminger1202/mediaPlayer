package com.youku.uplayer;

/**
 * 当播放广告相关的两个listenner
 * 
 * @author longfan
 * 
 */
public interface OnADPlayListener {

	/**
	 * 当开始播放广告的时候
	 * @param index 
	 * 
	 * @return
	 */
	boolean onStartPlayAD(int index);

	/**
	 * 当结束播放广告的时候
	 * @param index 
	 * 
	 * @return
	 */
	boolean onEndPlayAD(int index);
}