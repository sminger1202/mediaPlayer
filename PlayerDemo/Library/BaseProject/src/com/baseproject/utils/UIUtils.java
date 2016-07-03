/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.baseproject.utils;

import java.util.TimeZone;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.StyleSpan;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

/**
 * An assortment of UI helpers.
 */
public class UIUtils {
	/**
	 * Time zone to use when formatting all session times. To always use the
	 * phone local time, use {@link TimeZone#getDefault()}.
	 */
	private static final int SECOND_MILLIS = 1000;
	private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
	private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
	private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

	/** Flags used with {@link DateUtils#formatDateRange}. */
	private static final int TIME_FLAGS = DateUtils.FORMAT_SHOW_TIME
			| DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_WEEKDAY;

	private static StyleSpan sBoldSpan = new StyleSpan(Typeface.BOLD);

	/**
	 * Populate the given {@link TextView} with the requested text, formatting
	 * through {@link Html#fromHtml(String)} when applicable. Also sets
	 * {@link TextView#setMovementMethod} so inline links are handled.
	 */
	public static void setTextMaybeHtml(TextView view, String text) {
		if (TextUtils.isEmpty(text)) {
			view.setText("");
			return;
		}
		if (text.contains("<") && text.contains(">")) {
			view.setText(Html.fromHtml(text));
			view.setMovementMethod(LinkMovementMethod.getInstance());
		} else {
			view.setText(text);
		}
	}

	/**
	 * Given a snippet string with matching segments surrounded by curly braces,
	 * turn those areas into bold spans, removing the curly braces.
	 */
	public static Spannable buildStyledSnippet(String snippet) {
		final SpannableStringBuilder builder = new SpannableStringBuilder(
				snippet);

		// Walk through string, inserting bold snippet spans
		int startIndex = -1, endIndex = -1, delta = 0;
		while ((startIndex = snippet.indexOf('{', endIndex)) != -1) {
			endIndex = snippet.indexOf('}', startIndex);

			// Remove braces from both sides
			builder.delete(startIndex - delta, startIndex - delta + 1);
			builder.delete(endIndex - delta - 1, endIndex - delta);

			// Insert bold style
			builder.setSpan(sBoldSpan, startIndex - delta,
					endIndex - delta - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

			delta += 2;
		}

		return builder;
	}

	public static void preferPackageForIntent(Context context, Intent intent,
			String packageName) {
		PackageManager pm = context.getPackageManager();
		for (ResolveInfo resolveInfo : pm.queryIntentActivities(intent, 0)) {
			if (resolveInfo.activityInfo.packageName.equals(packageName)) {
				intent.setPackage(packageName);
				break;
			}
		}
	}

	private static final int BRIGHTNESS_THRESHOLD = 130;

	/**
	 * Calculate whether a color is light or dark, based on a commonly known
	 * brightness formula.
	 * 
	 * @see {@literal http://en.wikipedia.org/wiki/HSV_color_space%23Lightness}
	 */
	public static boolean isColorDark(int color) {
		return ((30 * Color.red(color) + 59 * Color.green(color) + 11 * Color
				.blue(color)) / 100) <= BRIGHTNESS_THRESHOLD;
	}

	// Shows whether a notification was fired for a particular session time
	// block. In the
	// event that notification has not been fired yet, return false and set the
	// bit.
	public static boolean isNotificationFiredForBlock(Context context,
			String blockId) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		final String key = String.format("notification_fired_%s", blockId);
		boolean fired = sp.getBoolean(key, false);
		sp.edit().putBoolean(key, true).commit();
		return fired;
	}

	private static final long sAppLoadTime = System.currentTimeMillis();

	public static long getCurrentTime(final Context context) {
		return context.getSharedPreferences("mock_data", Context.MODE_PRIVATE)
				.getLong("mock_current_time", System.currentTimeMillis())
				+ System.currentTimeMillis() - sAppLoadTime;
	}

	public static void safeOpenLink(Context context, Intent linkIntent) {
		try {
			context.startActivity(linkIntent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(context, "Couldn't open link", Toast.LENGTH_SHORT)
					.show();
		}
	}

	public static boolean isGoogleTV(Context context) {
		return context.getPackageManager().hasSystemFeature(
				"com.google.android.tv");
	}

	public static boolean hasFroyo() {
		// Can use static final constants like FROYO, declared in later versions
		// of the OS since they are inlined at compile time. This is guaranteed
		// behavior.
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
	}

	public static boolean hasGingerbread() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
	}

	public static boolean hasHoneycomb() {
		return Build.VERSION.SDK_INT >= 11;
	}

	public static boolean hasHoneycombMR1() {
		return Build.VERSION.SDK_INT >= 12;
	}
	
	public static boolean hasHoneycombMR2() {
		return Build.VERSION.SDK_INT >= 13;
	}

	public static boolean hasICS() {
		return Build.VERSION.SDK_INT >= 14;
	}

	public static boolean hasJellyBean() {
		return Build.VERSION.SDK_INT >= 16;
	}
	
	public static boolean hasKitKat(){
		return Build.VERSION.SDK_INT >= 19;
	}
	public static boolean isTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}

	public static boolean isHoneycombTablet(Context context) {
		return hasHoneycomb() && isTablet(context);
	}

	public static String getFormatTime(long millseconds) {
		long seconds = millseconds / 1000;
		StringBuffer buf = new StringBuffer();
		long hour = seconds / 3600;
		long min = seconds / 60 - hour * 60;
		long sec = seconds - hour * 3600 - min * 60;
		if (hour < 10) {
			buf.append("0");
		}
		buf.append(hour).append(":");
		if (min < 10) {
			buf.append("0");
		}
		buf.append(min).append(":");
		if (sec < 10) {
			buf.append("0");
		}
		buf.append(sec);
		return buf.toString();
	}
	
	/**
	 * 获取设备默认的Orientation
	 * 
	 * @param context
	 * @return
	 */
	private static int DEFAULT_ORIENTATION;
	public static int getDeviceDefaultOrientation(Context context) {
		if (0 == DEFAULT_ORIENTATION) {
			WindowManager windowManager = (WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE);
			Configuration config = context.getResources().getConfiguration();
			int rotation = windowManager.getDefaultDisplay().getRotation();
			if (((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) && config.orientation == Configuration.ORIENTATION_LANDSCAPE)
					|| ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) && config.orientation == Configuration.ORIENTATION_PORTRAIT)) {
				DEFAULT_ORIENTATION = Configuration.ORIENTATION_LANDSCAPE;
			} else {
				DEFAULT_ORIENTATION = Configuration.ORIENTATION_PORTRAIT;
			}
		}
		return DEFAULT_ORIENTATION;
	}

}
