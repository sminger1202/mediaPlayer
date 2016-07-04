package com.youku.player.util;

public interface PlayCode {
//	-998
//	用户主动返回（统计自定义错误码）
//	没有取到播放地址
	public final static String USER_RETURN = "-998";
//	-100
//	其它错误，连接服务器异常，请您观看其他视频 （接口反馈错误码）
	public final static String OTHER_ERROR = "-100";
//	没有取到播放地址
//	-101
//	禁止海外播放 （接口反馈错误码）
//	没有取到播放地址
//	-102
//	视频仅对好友开放 （接口反馈错误码）
//	没有取到播放地址
//	-104
//	版权受限制 （接口反馈错误码）
//	没有取到播放地址
//	-105
//	视频已经加密，需要输入密码 （接口反馈错误码）
//	没有取到播放地址
//	-106
//	视频不存在或无效 （接口反馈错误码）
//	没有取到播放地址
//	-107
//	视频已经加密，密码错误 （接口反馈错误码）
//	没有取到播放地址
	public final static String VIDEO_NOT_EXIST = "-106";
	
	// 广告播放过程中返回
	public final static String VIDEO_ADV_RETURN = "-995";
//	-996
//	获取播放地址成功后，视频加载失败，或播放已损坏的本地视频（统计自定义错误码）
	public final static String VIDEO_LOADING_FAIL = "-996";
//	-997
//	获取到播放地址成功后，视频加载中，用户主动返回 （统计自定义错误码）
	public final static String USER_LOADING_RETURN = "-997";
//	400
//	移动后台接口与主站接口通信失败 （统计自定义错误码）
//	没有取到播放地址
	public final static String SERVER_CONNECT_ERROR = "400";
//	200
//	视频成功播放（出现第一帧画面）（统计自定义错误码）
	public final static String PLAY_SUCC = "200";
	// 付费视频没付费
	public final static String NO_PAY = "-112";
	
	// 直播需要登录
	public static final int LIVE_ERROR_NEED_LOGIN = -13;


	public static final String CONNECT_ERROR = "1";
	public static final String SERVER_ERROR = "2";

	public static final int SERVER_400 = 400;


}
