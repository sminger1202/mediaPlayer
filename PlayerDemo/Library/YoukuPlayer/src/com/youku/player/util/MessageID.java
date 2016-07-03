/*
 * 
 * 开发人员：孟令跟
 * 本播放器支持在线分段播放和本地视频播放
 * 
 */
package com.youku.player.util;

public interface MessageID {
	/** 显示控制栏 */
	public static final int REFRESH_SEEKBAR = 2;
	/** 设置播放按钮状态 */
	public static final int ADD_HISTORY = 49;
	/***/
	public static final int FIRST = 1499;
	/** 播放视频列表中的索引 */
	public static final int PLAY_RELEATED = 137;
	// public static final int CLEAN_SHOW_TIME = 138;
	// /** 设置缓冲进度 */
	// public static final int SET_SECOND_PROGRESS = 238;
	public static final int SET_BEFORE_AND_NEXT_BUTTON_STATE = 239;

	public static final int ONCLICK_PLAY = 244;
	public static final int ON_VIDEO_SIZE_CHANGED = 247;
	public static final int ON_BUFFERING_UPDATE = 248;

	/** 播放器开始播放视频 */
	public static final int ON_LOADED = 250;
	/** 播放器此时正在加载 */
	public static final int ON_LOADING = 251;
	/** 播放器播放完成 */
	public static final int ON_COMPLETION = 252;
	/** 播放器通知，需要将正在播放的SurfaceView尺寸切换到正常 */
	public static final int ON_SWITCH_LARGE = 253;
	/** 播放器通知，需要将不在播放的SurfaceView尺寸切换到最小 */
	public static final int ON_SWITCH_SMALL = 254;
	/** 播放器通知，需要切换SurfaceView的尺寸。 */
	public static final int ON_SWITCH = 255;
	/** 播放器通知当前播放进度。 */
	int ON_CURRENT_POSITION_UPDATE = 256;
	/** 播放器通知超时。 */
	int ON_TIME_OUT = 257;
	/** 播放器通知当前视频总时长。 */
	int ON_DURATION_UPDATE = 258;
	/** 重置当前播放画面尺寸。 */
	int ON_RESIZE_CURRENT = 259;
	/** 播放器出错。 */
	int ON_ERROR = 260;
	/** 载入百分比更新 */
	public static final int LOADING_PERCENT_UPDATE = 261;
	/** 播放高清和超清清晰度视频时，载入时间超过十秒，提示切换到标清观看。 */
	public static final int NOTIFY_CHANGE_VIDEO_QUALITY = 262;

	public static final int SHOW_MEDIA_CONTROLLER = 263;

	/**
	 * 播放器工具条消息
	 * 
	 * @author 孙浩斌
	 */
	public final static int PLAYER_PLAY = 200; // 播放
	/** 暂停 */
	public final static int PLAYER_PAUSE = 201;
	/** 点击设置按钮，显示设置popupWindow */
	public final static int SHOW_SETTING_WINDOW = 202;
	/** 点击精彩点popup，需要seekto该点【时间点类型double,key：point_time】 */
	public final static int SEEKTO_HOTPOINT = 203;
	/** 用户seek视频 */
	public final static int ON_HOT_POINT_CLICK = 204;
	/** 用户設置播放畫質 */
	public static final int PLAYER_RESIZE_VIDEO = 206;
	/** 用户设置跳過片頭片尾 通知 */
	int PLAYER_SKIP_HEAD_AND_TAIL = 207;
	/** 用户与工具条交互，延长消失时间 */
	public final static int DELAY_DISMISS_TIME = 208;
	/** 右侧视频列表开关消息 */
	public final static int OFFSIDE_CHANGE = 210;
	/** phone 按下返回键退出播放器 */
	public final static int BACK = 211;
	/** 相关视频列表加载完毕 */
	public static final int RELEATIVE_VIDEO_LOAD_FINISH = 212;
	/** 用戶登錄 */
	public static final int USER_LOGIN = 213;

	/** 切换视频清晰度 */
	public static final int CHANGE_VIDEO_QUALITY = 214;
	/** 用于分享产生的中断消息 */
	public static final int USER_SHARE = 215;
	/** 更改视频的语言。 */
	public static final int CHANGE_LANGUAGE = 216;
	/**改变播放锁屏设置消息*/
	public static final int ORIENTATION_LOCK_CHANGED = 217;
	
	
	

}
