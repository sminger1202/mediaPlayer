package com.youku.player.apiservice;

import com.youku.player.module.VideoCacheInfo;

/**
 * 
 * @author yuanfang
 * 
 */
public interface ICacheInfo {

	/**
	 * 查找
	 * 
	 * @param vid
	 * @return
	 */
	public VideoCacheInfo getDownloadInfo(String vid);

	/**
	 * 获取本地下一集信息
	 * 
	 * @param vid
	 * @return
	 */
	public VideoCacheInfo getNextDownloadInfo(String vid);

	/**
	 * 通过showid和videoseq取下载信息
	 * 
	 * @param showId
	 * @param videoseq
	 * @return
	 */
	public VideoCacheInfo getDownloadInfo(String showId, int videoseq);

	/** 支持语言和格式的下载 */
	public void startCache(String vid, String title, String language,
			int format, IStartCacheCallBack callback);

	/** 支持语言和格式的下载 */
	public void startCache(String[] vids, String[] titles, String language,
			int format, IStartCacheCallBack callback);

//	public static final LanguageBean[] ALL_LANGAUGE = {
//			new LanguageBean(0, "default", "默认语言"),
//			new LanguageBean(1, "guoyu", "国语"),
//			new LanguageBean(2, "yue", "粤语"),
//			new LanguageBean(3, "chuan", "川话"),
//			new LanguageBean(4, "tai", "台语"),
//			new LanguageBean(5, "min", "闽南语"), new LanguageBean(6, "en", "英语"),
//			new LanguageBean(7, "ja", "日语"), new LanguageBean(8, "kr", "韩语"),
//			new LanguageBean(9, "in", "印度语"), new LanguageBean(10, "ru", "俄语"),
//			new LanguageBean(11, "fr", "法语"), new LanguageBean(12, "de", "德语"),
//			new LanguageBean(13, "it", "意大利语"),
//			new LanguageBean(14, "es", "西班牙语"),
//			new LanguageBean(15, "th", "泰语"),
//			new LanguageBean(16, "po", "葡萄牙语") };

	public boolean isDownloadFinished(String videoid);

	public void makeDownloadInfoFile(VideoCacheInfo downloadInfo);

	public void afresh();

}
