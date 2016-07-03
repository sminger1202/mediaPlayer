package com.youku.player.apiservice;

import com.youku.player.module.VideoHistoryInfo;
import com.youku.player.module.VideoUrlInfo;

/**
 * 
 *   @author          张宇 
 *   @create-time     Mar 26, 2013   3:49:18 PM   
 *   @version         $Id
 *
 */
public interface IVideoHistoryInfo {
	
	/**
	 * 获取视频播放历史对象
	 * @param vid 
	 * @return
	 */
	public VideoHistoryInfo getVideoHistoryInfo(String vid);

	/**
	 * 添加播放历史，每分钟调用一次
	 * @param videoUrlInfo
	 */
	public void addIntervalHistory(VideoUrlInfo videoUrlInfo);

	/**
	 * 添加播放历史，release时调用
	 * @param videoUrlInfo
	 */
	public void addReleaseHistory(VideoUrlInfo videoUrlInfo);
	
}
