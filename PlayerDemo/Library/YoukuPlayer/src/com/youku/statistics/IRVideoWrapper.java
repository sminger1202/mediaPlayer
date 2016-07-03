package com.youku.statistics;

import android.content.Context;
import cn.com.iresearch.mvideotracker.IRVideo;

public class IRVideoWrapper {
	private static boolean isOpen = true;

	public static void init(Context context, String uaid,String customVal) {
		if (!isOpen)
			return;
		IRVideo.getInstance(context).init(uaid, customVal);
	}

	public static void newVideo(Context context, String vid, long videoTime,
			boolean isPlay) {
		if (!isOpen)
			return;
		IRVideo.getInstance(context).newVideoPlay(vid, videoTime, isPlay);
	}

	public static void videoPlay(Context context) {
		if (!isOpen)
			return;
		IRVideo.getInstance(context).videoPlay();
	}

	public static void videoPause(Context context) {
		if (!isOpen)
			return;
		IRVideo.getInstance(context).videoPause();
	}

	public static void videoEnd(Context context) {
		if (!isOpen)
			return;
		IRVideo.getInstance(context).videoEnd();
	}

	public static void clearVideoPlayInfo(Context context) {
		if (!isOpen)
			return;
		IRVideo.getInstance(context).clearVideoPlayInfo();
	}
}

