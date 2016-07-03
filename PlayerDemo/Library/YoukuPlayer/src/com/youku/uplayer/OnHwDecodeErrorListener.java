package com.youku.uplayer;

/**
 * 硬解失败的回调
 */
public interface OnHwDecodeErrorListener {
	/**
	 * 初始化失败
	 */
	void OnHwDecodeError();

	/**
	 * 播放失败
	 */
	void onHwPlayError();
}
