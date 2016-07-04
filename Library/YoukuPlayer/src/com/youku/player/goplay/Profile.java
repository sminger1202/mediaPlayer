package com.youku.player.goplay;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import com.baseproject.utils.Logger;
import com.youku.player.base.Plantform;
import com.youku.player.config.MediaPlayerConfiguration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/****
 * 
 * 播放器用到的一些设置
 * @author yuanfang
 * 
 */
public class Profile {

	private static final String TAG = "com.tools.Profile";
	
	// ua
	public static String USER_AGENT = "";
	
	public static String GUID;

	/** android版本 */
	public static final int API_LEVEL = Build.VERSION.SDK_INT;
	/** 播放器类型-默认硬系统解 */
	public static int PLAYER_DEFAULT = 0;
	/** 播放器类型-uplayer */
	public static int PLAYER_OUR = 2;

	/** 视频格式 */
	private static int videoFormat = 5;
	/** Debug模式开关 */
	public static boolean debugMode = false;
	
	/** 强制使用系统播放器，这个给tv使用播放m3u8 */
	public static boolean USE_SYSTEM_PLAYER = false; 
	
	// 平台标志
	public static int PLANTFORM = Plantform.YOUKU;
	
	// 防盗链用
	public static int ctype = 0; 
	
	// 直播
	public static final int ctypeHLS = 80;
	
	//防盗链版本
	public static String ev = "1";
	
	/** 防盗链密钥 */
	public static final String YOUCANGUESS = "094b2a34e812a4282f25c7ca1987789f";
	
	/** 0 超清 1高清 2标清 */
	public static final int VIDEO_QUALITY_HD2 = 0, VIDEO_QUALITY_HD = 1,
			VIDEO_QUALITY_SD = 2, VIDEO_QUALITY_AUTO = 3, VIDEO_QUALITY_HD3 = 4;
	
	/** 是否使用硬解*/
	private static boolean useHardwareDecode = false;
	
	/**
	 * 视频类型定义 数据类型：String 名称内容：扩展名 大小写限制：全小写 已知扩展类型 ：
	 * 3gp、m3u8,3gphd(mp4),flv,mp4(分片mp4),flv
	 * 
	 * */
	public static final int FORMAT_3GP = 2; 
	public static final int FORMAT_FLV_HD = 5; // 网站标清flv Uplayer 标清
	public static final int FORMAT_3GPHD = 4;
	public static final int FORMAT_M3U8 = 6;
	public static final int FORMAT_MP4 = 1; // Uplayer 高清
	public static final int FORMAT_HD2 = 7; // 网站超清hd2 // Uplayer 超清
	public static final int FORMAT_HD3 = 8; // 1080p
	
	public static final int FORMAT_TUDOU_F4V_256P = 2;
	public static final int FORMAT_TUDOU_F4V_360P = 3;
	public static final int FORMAT_TUDOU_F4V_480P = 4;
	public static final int FORMAT_TUDOU_F4V_720P = 5;
	public static final int FORMAT_TUDOU_F4V_ORIGINAL = 99;
	public static final int FORMAT_TUDOU_F4V = 5;
	public static final int FORMAT_TUDOU_3GP = 2;
	public static final int FORMAT_TUDOU_MP4 = 1;
	public static final int FORMAT_TUDOU_FLV = 5;
	
	/** 0 超清 1高清 2标清 */

	public static final int FORMAT_TUDOU_STANDARD = FORMAT_FLV_HD;//标清
	public static final int FORMAT_TUDOU_HIGH = FORMAT_MP4;//高清
	public static final int FORMAT_TUDOU_SUPER = FORMAT_HD2;//超清
	public static final int FORMAT_TUDOU_1080P = FORMAT_HD3;//1080p
	

	/** seekbar点的类型 @author:孙浩斌 */
	public static final String HEAD_POINT = "head"; // 片头
	public static final String STANDARD_POINT = "standard"; // 广告
	public static final String STORY_POINT = "story"; // 精彩点
	public static final String TAIL_POINT = "tail"; // 片尾
	public static final String CONTENTAD_POINT = "contentad"; // 标版

	// 解码方式
	public static final int SOFTWARE_DECODE = 1;
	public static final int HARDWARE_DECODE = 2;
	public static final int OTHER_DECODE = 3;
	
	//百度快播视频的格式
	public static String baiduFormat; 

	public static void setVideoType_and_PlayerType(int VideoType, Context context) {
		
		if (VideoType==FORMAT_FLV_HD) {
			Profile.playerType = Profile.PLAYER_OUR;
		} else {
			Profile.playerType = Profile.PLAYER_DEFAULT;
		}
			videoFormat = VideoType;
	}
	
	public static void setVideoType(int VideoType) {
		Logger.d(TAG, "setVideoType");
		videoFormat = VideoType;
	}


	public static int getVideoFormat() {
		return videoFormat;
	}

	public static boolean ism3u8() {
		return videoFormat == FORMAT_M3U8;
	}

	public static boolean isSegMp4() {
		return videoFormat == FORMAT_MP4;
	}

	public static String getVideoFormatName() {
		String typeName = null;
		if (videoFormat==2) {
			typeName = "3gp";
		} else if (videoFormat==5) {
			typeName = "flv_hd";
		} else if (videoFormat==4) {
			typeName = "3gphd";
		} else if (videoFormat==6) {
			typeName = "m3u8";
		} else if (videoFormat==1) {
			typeName = "mp4";
		} else if(videoFormat==7){
			typeName="hd2";
		}
		return typeName;
	}

	/** 播放器类型 */
	public static int playerType = PLAYER_DEFAULT;

	/** 播放视频的语言。 */
	public static String langCode = "guoyu";
	
	/**弹幕相关常量 */
	public static final int SMALLSIZE = 18;
	public static final int MIDSIZE = 24;
	public static final int BIGSIZE = 30;

	public static final int SCROLL = 1;
	public static final int BOTTOM = 4;
	public static final int TOP = 5;
	
	public static final int RETURNSMALLSIZE = 0;
	public static final int RETURNMIDSIZE = 1;
	public static final int RETURNBIGSIZE = 2;

	public static final int RETURNSCROLL = 3;
	public static final int RETURNBOTTOM = 6;
	public static final int RETURNTOP = 4;
	
	

	

	/** 播放模式【包括是否始终联播、是否不联播、是否单视频循环】 */
	private static int playMode, videoSize = 100;

	/** 视频清晰度 */
	public static int videoQuality = VIDEO_QUALITY_SD;

	/** 是否跳过片头片尾 */
	private static boolean skipHeadAndTail = true;
	/** 联播类型 -始终联播 */
	public static final int ALWAYSHOOKUP = 1;
	/** 联播类型 -不联播 */
	public static final int NOHOOKUP = 2;
	/** 联播类型 -单视频循环 */
	public static final int LOOP = 3;
	
	public static boolean danmakuSwith;
	public static boolean liveDanmakuSwith;
	public static int barrage_effect;
	public static int barrage_position;

	// pid
	public static String pid;
	
	public static final int PAD = 0, PHONE = 1, PHONE_BROWSER = 2,
			PAD_BROWSER = 3, HTC = 4;

	public static int from = PHONE;

	private static boolean sharedPreferenceInit = false;
	private static boolean iku_connected = false;

	public static void set_iku_connected(boolean connected) {
		iku_connected = connected;
	}

	public static boolean get_iku_connected() {
		return iku_connected;
	}

	public static boolean isSkipHeadAndTail() {
		return skipHeadAndTail;
	}

	/** 最小屏幕 */
	private static boolean isSmallScreen;

	public static void setSmallScreen(Activity mActivity) {
		try {
			DisplayMetrics dm = new DisplayMetrics();
			mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
			Profile.isSmallScreen = dm.widthPixels <= 400
					&& dm.heightPixels <= 400;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** 最小屏幕 */
	public static boolean isSmallScreen() {
		return isSmallScreen;
	}

	public static void initSharedPreference(Context context) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		skipHeadAndTail = sp.getBoolean("skip_head", true);
		playMode = sp.getInt("play_mode", ALWAYSHOOKUP);
		videoQuality = sp.getInt("video_quality", VIDEO_QUALITY_SD);
		videoSize = sp.getInt("video_size", 100);
		useHardwareDecode = !sp.getBoolean("isSoftDecoder", false)
				&& sp.getBoolean("isSupportHardDecoder", false);
		sharedPreferenceInit = true;
		sp = null;
	}
	
	public static void getVideoQualityFromSharedPreferences(Context context) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		videoQuality = sp.getInt("video_quality", VIDEO_QUALITY_SD);
	}
	
	public static boolean getDanmakuSwith(Context context) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		danmakuSwith = sp.getBoolean("danmakuSwith", true);
		return danmakuSwith;
	}
	
	public static boolean getLiveDanmakuSwith(Context context) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		liveDanmakuSwith = sp.getBoolean("liveDanmakuSwith", false);
		return liveDanmakuSwith;
	}
	
	public static int getDanmakuEffect(Context context) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		barrage_effect = sp.getInt("barrage_effect", 0);
		return barrage_effect;
	}
	
	public static int getDanmakuPosition(Context context){
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		barrage_position = sp.getInt("barrage_position", 0);
		return barrage_position;
	}

	public static void setPreferences(String key, String value, Context context) {
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context)
				.edit();
		editor.putString(key, value);
		editor.commit();
	}

	public static String getPreferences(String key, Context context) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		String value  = sp.getString(key, "");
		return value;
	}

	public static void setSkipHeadAndTail(boolean skipHeadAndTail) {
		Profile.skipHeadAndTail = skipHeadAndTail;
	}

	public static int getPlayMode(Context context) {
		if (sharedPreferenceInit)
			return playMode;
		initSharedPreference(context);
		return playMode;
	}

	public static void setPlayMode(int playMode) {
		Profile.playMode = playMode;
	}

	public static int getVideoQuality(Context context) {
		if (sharedPreferenceInit)
			return videoQuality;
		initSharedPreference(context);
		return videoQuality;
	}

	public static void setVideoQuality(int videoQuality) {
		Profile.videoQuality = videoQuality;
	}

	public static int getVideoSize(Context context) {
		if (sharedPreferenceInit)
			return videoSize;
		initSharedPreference(context);
		return videoSize;
	}

	public static void setVideoSize(int videoSize) {
		Profile.videoSize = videoSize;
	}
	
	public static boolean useHardwareDecode(Context context) {
		// 灰度发布优先
		if (MediaPlayerConfiguration.getInstance().isGreyControl(MediaPlayerConfiguration.FUN_HWDECODE))
			return MediaPlayerConfiguration.getInstance().useHardwareDecode();

		if (sharedPreferenceInit)
			return useHardwareDecode
					&& MediaPlayerConfiguration.getInstance()
							.useHardwareDecode();
		initSharedPreference(context);
		return useHardwareDecode
				&& MediaPlayerConfiguration.getInstance().useHardwareDecode();
	}
	
	public static void setHardwareDecode(boolean isHardware) {
		useHardwareDecode = isHardware;
	}
	
	public static int getDanmakuTextSize(int size) {
		int textSize = 0;
		switch (size) {
		case SMALLSIZE:
			textSize = RETURNSMALLSIZE;
			break;
		case MIDSIZE:
			textSize = RETURNMIDSIZE;
			break;
		case BIGSIZE:
			textSize = RETURNBIGSIZE;
			break;
		default:
			break;
		}
		return textSize;
	}
	
	public static int getDanmakuPosition(int position) {
		int danmakuPositon = 0;
		switch (position) {
		case SCROLL:
			danmakuPositon = RETURNSCROLL;
			break;
		case TOP:
			danmakuPositon = RETURNTOP;
			break;
		case BOTTOM:
			danmakuPositon = RETURNBOTTOM;
			break;
		default:
			break;
		}
		return danmakuPositon;
	}
	
	public static long getUnsignedInt(int color) {
		long unsignedValue = color & Integer.MAX_VALUE; 
		unsignedValue |= 0x80000000L;
		return unsignedValue;
	}
	
	public static String replaceSpaceWithPlus(String rawString) {
		String regEx = "[' ']+";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(rawString);
		return m.replaceAll("+").trim();
	}
}