package com.youku.player.goplay;

import com.youku.player.module.PayInfo;
import com.youku.player.module.VideoUrlInfo;

/**
 * 文件名：GoplayException 功能：调播放器失败的错误信息 作者：贾磊 创建时间：2012-12-04
 * 
 */
public class GoplayException {

	private int errorCode = -1;
	private String errorInfo;
	public VideoAdvInfo videoAdvInfo;
	private VideoUrlInfo videoUrlInfo;
	private boolean mShowTip = true;
	
	// 土豆 无版权视频播放时 需要返回itemCode
	public String itemCode;
	
	// 用于104时，跳出浏览器
	public String webUrl;
	
	// 用于付费视频
	public PayInfo payInfo;

	public GoplayException setErrorCode(int errorCode) {
		this.errorCode = errorCode;
		return this;
	}

	public GoplayException setErrorInfo(String errorInfo) {
		this.errorInfo = errorInfo;
		return this;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public String getErrorInfo() {
		return errorInfo;
	}
	
	public VideoUrlInfo getVideoUrlInfo() {
		return videoUrlInfo;
	}

	public void setVideoUrlInfo(VideoUrlInfo videoUrlInfo) {
		this.videoUrlInfo = videoUrlInfo;
	}

	public void setShowTip(boolean show){
		mShowTip = show;
	}

	public boolean showTip(){
		return mShowTip;
	}
}
