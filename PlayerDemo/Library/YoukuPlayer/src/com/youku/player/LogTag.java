package com.youku.player;

/**
 * 声明各个模块使用的tag，单独class的tag使用TAG_PREFIX + class.getSimpleName
 * Debug日志使用 {@link com.baseproject.utils.Logger#d(String, String)} 打印
 * 错误日志使用 {@link com.baseproject.utils.Logger#e(String, String)} 打印
 * 频繁打印的日志使用 {@link com.baseproject.utils.Logger#v(String, String)} 打印
 */
public class LogTag {
    public static String TAG_PREFIX = "YKPlayer-";
    public static String TAG_PLAYER = TAG_PREFIX + "PlayFlow";
    public static String TAG_LOCAL = TAG_PREFIX + "Local";
    public static String TAG_STATISTIC = TAG_PREFIX + "Statistic";
    public static String TAG_DANMAKU = TAG_PREFIX + "Danmaku";
    public static String TAG_LOCAL_DANMAKU = TAG_PREFIX + "LacalDanmaku";
    public static String TAG_WATERMARK = TAG_PREFIX + "WaterMark";
    public static String TAG_WO_VIDEO = TAG_PREFIX + "WoVideo";
    public static String TAG_ORIENTATION = TAG_PREFIX + "Orientation";
    public static String TAG_TRUE_VIEW = TAG_PREFIX + "TrueView";
    public static String TAG_GREY = TAG_PREFIX + "GreyConfig";
    public static String MSG_EXCEPTION = "Exception";
}
