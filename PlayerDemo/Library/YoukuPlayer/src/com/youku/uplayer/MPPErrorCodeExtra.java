package com.youku.uplayer;

public final class MPPErrorCodeExtra {
	public static final int ERROR_NO = 0;
	// 播放器状态非空闲 101
	public static final int ERROR_PLAYER_NO_IDLE = 101;
	// 播放地址为空 102
	public static final int ERROR_PLAYER_ADDRESS_NULL = 102;
	// dns解析超时 103
	public static final int ERROR_DNS_RESOLUTION_TIMEOUT = 103;
	// dns解析出错 104
	public static final int ERROR_DNS_RESOLUTION_FAILED = 104;
	// 跳转失败 105
	public static final int ERROR_URL_JUMP_FAILED = 105;
	// 跳转超时 106
	public static final int ERROR_URL_JUMP_TIMEOUT = 106;
	// 获取文件失败 107
	public static final int ERROR_URL_GET_FILE_FAILED = 107;
	// 获取文件超时 108
	public static final int ERROR_URL_GET_FILE_TIMEOUT = 108;
	// http 400系列错误 109
	public static final int ERROR_URL_HTTP_4XX_ERROR = 109;
}
