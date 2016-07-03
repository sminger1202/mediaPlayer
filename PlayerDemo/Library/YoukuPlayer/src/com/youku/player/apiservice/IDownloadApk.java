package com.youku.player.apiservice;

public interface IDownloadApk {
	/**
	 * 广告点击地址以.apk结尾则调起下载器下载
	 * 
	 * @param url
	 */
	void downloadApk(String url, String[] startTrack, String[] endTrack);

	/**
	 * 下载对话框展示通知
	 * @param gameId
	 */
	void onDownloadDialogShow(String gameId);

	/**
	 * 通过gameid进行下载
	 * @param gameId
	 * @param startTrack
	 * @param endTrack
	 */
	void downloadApkById(String gameId, String[] startTrack, String[] endTrack);
}
