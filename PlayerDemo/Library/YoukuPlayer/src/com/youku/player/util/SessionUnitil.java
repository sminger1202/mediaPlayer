package com.youku.player.util;

import java.util.HashMap;
import java.util.Map;

import com.baseproject.utils.Util;
import com.youku.analytics.data.Device;

public class SessionUnitil {

	public static String playEvent_session;

	private static boolean isSessionCreated;

	public static Map<String, String> http_session = new HashMap<String, String>();

	public static String creatSession() {
		isSessionCreated = true;
		return Util.md5(Util.getTime() + creatRandom() + Device.gdid);
	}

	/**
	 * 生成 1--10之间的随机数
	 * 
	 * @return
	 */
	public static int creatRandom() {
		return (int) (Math.random() * 10);
	}

	public static String getPlayVVBeginSession() {
		if (!isSessionCreated) {
			playEvent_session = creatSession();
		}
		isSessionCreated = false;
		return playEvent_session;
	}
}
