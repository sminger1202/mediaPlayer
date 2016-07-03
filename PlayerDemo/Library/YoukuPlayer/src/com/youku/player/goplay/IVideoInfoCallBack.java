package com.youku.player.goplay;

import com.youku.player.module.VideoUrlInfo;

/**
 * 获得播放地址的回调
 * @author yuanfang
 *
 */
public interface IVideoInfoCallBack {

	/** 当调播放器成功时 */
	public void onSuccess(VideoUrlInfo videoUrlInfo);

	/** 当调播放器失败时 */
	public void onFailed(GoplayException e);
}
