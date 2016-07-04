package com.youku.player.apiservice;

/**
 * 下载的回调
 * @author yuanfang
 *
 */
public interface IStartCacheCallBack {
	
	public void onSucc();

	/**
	 * 添加任务失败
	 * @param msg 失败提示信息 
	 */
	public void onFail(String msg);
}
