package com.youku.uplayer;

public class MPPErrorCode {
	// videoId为空
	public  static final int VIDEO_ID_NULL = -2;
	// 播放错误
	public static final int MEDIA_INFO_PLAYERROR = 1002;
	// 网络连接失败
	public static final int MEDIA_INFO_NETWORK_DISSCONNECTED = 1005;
	// 数据源错误
	public static final int MEDIA_INFO_DATA_SOURCE_ERROR = 1006;
	// 播放器准备失败
	public static final int MEDIA_INFO_PREPARE_ERROR = 1007;
	// 网络出错
	public static final int MEDIA_INFO_NETWORK_ERROR = 1008;
	// 搜索出错
	public static final int MEDIA_INFO_SEEK_ERROR = 1009;
	// 播放20秒播放点不动
	public static final int MEDIA_INFO_NETWORK_CHECK = 2004;
	// 播放广告时播放器准备出错
	public static final int MEDIA_INFO_PREPARED_AD_CHECK = 2005;
	// 中插广告播放器准备出错
	public static final int MEDIA_INFO_PREPARED_MID_AD_CHECK = 2200;
	// 中插广告数据源错误
	public static final int MEDIA_INFO_MIDAD_DATA_SOURCE_ERROR = 2201;
	// 准备超时
	public static final int MEDIA_INFO_PREPARE_TIMEOUT_ERROR = 1010;

	public static final int MEDIA_INFO_VIDEO_TRACK_LAGGING = 700;
	public static final int MEDIA_INFO_BAD_INTERLEAVING = 800;
	public static final int MEDIA_INFO_NOT_SEEKABLE = 801;

	// 正片及广告出现4xx错误
	public static final int MEDIA_INFO_AD_HTTP_ERROR_4XX = 1110;
	public static final int MEDIA_INFO_VIDEO_HTTP_ERROR_4XX = 1111;

	// 其他错误
	public static final int MEDIA_INFO_PLAY_UNKNOW_ERROR =1;
}
