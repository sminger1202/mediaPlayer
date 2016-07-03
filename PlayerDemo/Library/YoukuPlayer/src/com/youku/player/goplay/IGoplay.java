package com.youku.player.goplay;


/**
 * 文件名：IGoplay 功能：播放器调用接口 作者：贾磊 创建时间：2012-12-04
 * 
 */
public interface IGoplay {

	/**
	 * 调用播放器的函数定义
	 * 
	 * @param call
	 *            ：函数回调
	 * 
	 * @return 无返回值
	 * 
	 * */
	public void goplayer(String vid, String languageCode, int videostage,
			int format, int point, boolean isCache, IVideoInfoCallBack call);
	
}
