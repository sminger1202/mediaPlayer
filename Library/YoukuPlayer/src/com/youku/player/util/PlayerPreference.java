package com.youku.player.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PlayerPreference {
	private static SharedPreferences s;

	public static void init(Context context) {
		s = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public static void savePreference(String key, String value) {
		s.edit().putString(key, value).commit();
	}

	public static void savePreference(String key, int value) {
		s.edit().putInt(key, value).commit();
	}

	public static void savePreference(String key, Boolean value) {
		s.edit().putBoolean(key, value).commit();
	}

	public static boolean getPreferenceBoolean(String key) {
		return s.getBoolean(key, false);
	}

	public static String getPreference(String key) {
		return s.getString(key, "");
	}

	public static int getPreferenceInt(String key) {
		return s.getInt(key, 0);
	}

	public static boolean getPreferenceBoolean(String key, boolean def) {
		return s.getBoolean(key, def);
	}

	public static String getPreference(String key, String def) {
		return s.getString(key, def);
	}

	public static int getPreferenceInt(String key, int def) {
		return s.getInt(key, def);
	}

}
