package com.youku.uplayer;

import java.io.IOException;

public class OriginalMediaPlayer extends android.media.MediaPlayer {

	// yujunfeng
	public void setHttpUserAgent(String userAgent) throws IOException,
			IllegalArgumentException, SecurityException, IllegalStateException {

	}

	// 0：正向 1：逆90°2：顺90°3：反180°
	public void setVideoOrientation(int orientation)
			throws IllegalStateException {

	}

    public  void enableVoice(int enable) throws IllegalStateException{

    }

    public int getVoiceStatus() throws IllegalStateException{
        return 1;
    }

	// 设置旋转后视频的长宽
	public void changeVideoSize(int width, int height)
			throws IllegalStateException {

	}
	
	public void skipCurPreAd() throws IllegalStateException{
		
	}

	public void setPlayRate(int rate) throws IllegalStateException {

	}

	/**
	 * 添加后贴url
	 * @param path
	 * @throws IOException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws IllegalStateException
	 */
	public void addPostADUrl(String path) throws IOException,
			IllegalArgumentException, SecurityException, IllegalStateException {

	}

	private OnADPlayListener mOnADPlayListener;

	private OnMidADPlayListener mOnMidADPlayListener;

	private OnCurrentPositionUpdateListener mOnCurrentPositionUpdateListener;
	
	private OnVideoIndexUpdateListener mOnVideoIndexUpdateListener;
	
	private OnTimeoutListener mOnTimeoutListener;
	
	public void setmOnTimeoutListener(OnTimeoutListener mOnTimeoutListener) {
		this.mOnTimeoutListener = mOnTimeoutListener;
	}

	/**
	 * 设置广告的listener
	 * 
	 * @param mADPlayListener
	 */
	public void setOnADPlayListener(OnADPlayListener mADPlayListener) {
		mOnADPlayListener = mADPlayListener;
	}

	/**
	 * 设置中插广告的listener
	 * 
	 * @param listener
	 */
	public void setOnMidADPlayListener(OnMidADPlayListener listener) {
		mOnMidADPlayListener = listener;
	}

	/**
	 * 设置播放进度的listener
	 * 
	 * @param listener
	 */
	public void setOnCurrentPositionUpdateListener(
			OnCurrentPositionUpdateListener listener) {
		mOnCurrentPositionUpdateListener = listener;
	}

	private OnLoadingStatusListener mOnLodingStatusListener;

	/**
	 * 设置播放进度的listener
	 * 
	 * @param listener
	 */
	public void setOnLodingStatusListener(OnLoadingStatusListener listener) {
		mOnLodingStatusListener = listener;
	}
	
	
	private OnADCountListener mOnADCountListener;
	/**
	 * 设置广告播放进度的listener
	 * 
	 * @param listener
	 */
	public void setOnADCountListener(OnADCountListener listener) {
		mOnADCountListener = listener;
	}
	
	private OnNetworkSpeedListener mOnNetworkSpeedListener;
	/**
	 * 设置网络状态的listener
	 * 
	 * @param listener
	 */
	public void setOnNetworkSpeedListener(OnNetworkSpeedListener listener) {
		mOnNetworkSpeedListener = listener;
	}

    protected OnNetworkSpeedPerMinute mOnNetworkSpeedPerMinute;

    public void setOnNetworkSpeedPerMinute(OnNetworkSpeedPerMinute listener) {
        mOnNetworkSpeedPerMinute = listener;
    }

    protected OnBufferPercentUpdateListener mOnBufferPercentUpdateListener;

    public void setOnBufferPercentUpdateListener(OnBufferPercentUpdateListener listener) {
        mOnBufferPercentUpdateListener = listener;
    }

    private OnRealVideoStartListener mOnRealVideoStartListener;

	private OnHwDecodeErrorListener mOnHwDecodeErrorListener;

    private OnConnectDelayListener mOnConnectDelayListener;

	private OnHttp302DelayListener mOnHttp302DelayListener;
	/**
	 * 设置开始播放正片的listener
	 * 
	 * @param listener
	 */
	public void setOnRealVideoStartListener(OnRealVideoStartListener listener) {
		mOnRealVideoStartListener = listener;
	}
	
	public void setTimeout(int type, int sec) {
		
	}
	
	/**
	 * 播放位置变化的listener
	 * 
	 * @param listener
	 */
	public void setOnVideoIndexUpdateListener(
			OnVideoIndexUpdateListener listener) {
		mOnVideoIndexUpdateListener = listener;
	}
	
	/**
	 * 硬解错误的listener
	 * 
	 * @param listener
	 */
	public void setOnHwDecodeErrorListener(OnHwDecodeErrorListener listener) {
		mOnHwDecodeErrorListener = listener;
	}

    public void setOnConnectDelayListener(OnConnectDelayListener listener){
        mOnConnectDelayListener = listener;
    }

	public void setOnHttp302DelayListener(OnHttp302DelayListener listener){
		mOnHttp302DelayListener = listener;
	}

	private OnQualityChangeListener mOnQualityChangeListener;
	public void setOnQualityChangeListener(OnQualityChangeListener listener) {
		mOnQualityChangeListener = listener;
	}
}