package com.youku.player.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.baseproject.utils.Logger;
import com.youku.android.player.R;
import com.youku.player.LogTag;
import com.youku.player.base.Plantform;
import com.youku.player.goplay.Profile;
import com.youku.player.plugin.DetailVideoSeriesList;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class DetailUtil {
	private static String TAG = LogTag.TAG_PREFIX + DetailUtil.class.getSimpleName();

	public static boolean haveLandScreen(Context context) {
		SharedPreferences sp = context.getSharedPreferences("land_size", 0);
		if (sp.getInt("height", 0) != 0)
			return true;
		else {
			return false;
		}
	}



	public static boolean havePortScreen(Context context) {
		SharedPreferences sp = context.getSharedPreferences("port_size", 0);
		if (sp.getInt("height", 0) != 0)
			return true;
		else {
			return false;
		}
	}

	public static void writeLandScreen(Context context, int left, int top,
			int right, int bottom, int height, int width) {
		SharedPreferences sp = context.getSharedPreferences("land_size", 0);
		if (sp.getInt("height", 0) != 0)
			return;
		Editor editor = sp.edit();
		editor.putInt("left", left);
		editor.putInt("top", top);
		editor.putInt("right", right);
		editor.putInt("bottom", bottom);
		editor.putInt("height", height);
		editor.putInt("width", width);
		editor.commit();

	}

	public static int[] readLandScreen(Context context) {
		int land_size[] = new int[6];
		SharedPreferences sp = context.getSharedPreferences("land_size", 0);
		land_size[0] = sp.getInt("left", 0);
		land_size[1] = sp.getInt("top", 0);
		land_size[2] = sp.getInt("right", 0);
		land_size[3] = sp.getInt("bottom", 0);
		land_size[4] = sp.getInt("height", 0);
		land_size[5] = sp.getInt("width", 0);
		return land_size;
	}

	public static void writePortScreen(Context context, int left, int top,
			int right, int bottom, int height, int width) {
		SharedPreferences sp = context.getSharedPreferences("port_size", 0);
		Editor editor = sp.edit();
		editor.putInt("left", left);
		editor.putInt("top", top);
		editor.putInt("right", right);
		editor.putInt("bottom", bottom);
		editor.putInt("height", height);
		editor.putInt("width", width);
		editor.commit();
	}

	public static int[] readPortScreen(Context context) {
		int port_size[] = new int[6];
		SharedPreferences sp = context.getSharedPreferences("port_size", 0);
		port_size[0] = sp.getInt("left", 0);
		port_size[1] = sp.getInt("top", 0);
		port_size[2] = sp.getInt("right", 0);
		port_size[3] = sp.getInt("bottom", 0);
		port_size[4] = sp.getInt("height", 0);
		port_size[5] = sp.getInt("width", 0);
		return port_size;
	}

	public static int getScreenWidth(Activity context) {
		DisplayMetrics displayMetrics = new DisplayMetrics();
		if (Build.VERSION.SDK_INT >= 17) {
			context.getWindowManager().getDefaultDisplay()
					.getRealMetrics(displayMetrics);
		} else {
			context.getWindowManager().getDefaultDisplay()
					.getMetrics(displayMetrics);
		}
		return displayMetrics.widthPixels > displayMetrics.heightPixels ? displayMetrics.heightPixels
				: displayMetrics.widthPixels;
	}

	public static int getScreenHeight(Activity context) {
		DisplayMetrics displayMetrics = new DisplayMetrics();
		if (Build.VERSION.SDK_INT >= 17) {
			context.getWindowManager().getDefaultDisplay()
					.getRealMetrics(displayMetrics);
		} else {
			context.getWindowManager().getDefaultDisplay()
					.getMetrics(displayMetrics);
		}
		return displayMetrics.widthPixels > displayMetrics.heightPixels ? displayMetrics.widthPixels
				: displayMetrics.heightPixels;
	}

	public static int getScreenDensity(Activity context) {
		DisplayMetrics displayMetrics = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay()
				.getMetrics(displayMetrics);
		return displayMetrics.densityDpi;
	}

	public static String getAndroidId(Context context) {
		String s = getPreference(context, "android_id");
		if (s == null || s.length() <= 0) {
			s = Secure.getString(context.getContentResolver(),
					Secure.ANDROID_ID);
			if (s == null || s.length() <= 0)
				return "";
			savePreference(context, "android_id", s);
		}
		return s;
	}

	/**
	 * 播放器横屏全屏播放时，获取虚拟按键高度
	 * 竖屏时不适用,PAD不适用
	 * 
	 * @param context
	 */
	public static int getFullScreentNavigationBarHeight(Activity context) {
		int widthPixels;
		WindowManager w = context.getWindowManager();
		Display d = w.getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		d.getMetrics(metrics);
		// since SDK_INT = 1;
		widthPixels = metrics.widthPixels;
		int realWidthPixels = widthPixels;
		// includes window decorations (statusbar bar/navigation bar)
		if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 17) {
			try {
				realWidthPixels = (Integer) Display.class.getMethod(
						"getRawWidth").invoke(d);
			} catch (Exception ignored) {
			}
		}
		// includes window decorations (statusbar bar/navigation bar)
		else if (Build.VERSION.SDK_INT >= 17) {
			try {
				Point realSize = new Point();
				Display.class.getMethod("getRealSize",
						Point.class).invoke(d, realSize);
				realWidthPixels = realSize.x;
			} catch (Exception ignored) {
			}
		}
		int height = realWidthPixels - widthPixels;
		if (height < 0) {
			// 如果获取失败，使用默认值
			height = (int) (metrics.density * 40);
		}
		return height;
	}

	/**
	 * 保存sharePreference
	 * 
	 * @param context
	 * @param key
	 * @param value
	 */
	public static void savePreference(Context context, String key, String value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit()
				.putString(key, value).commit();
	}
	/**
	 * 获取sharePreference
	 * 
	 * @param key
	 * @return
	 */
	public static String getPreference(Context context, String key) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getString(key, "");
	}

	public static int getOrientation(Activity context) {
		if (getScreenHeight(context) > getScreenWidth(context))// vertical
			return 1;
		else if (getScreenHeight(context) < getScreenWidth(context))
			return 2;
		return -1;
	}

	public static int getRealWidth(int sdk_version, Display display) {
		int width = -1;
		String methodString = null;
		if (sdk_version == 13)
			methodString = "getRealWidth";
		else if (sdk_version > 13)
			methodString = "getRawWidth";
		Class c = null;
		try {
			c = Class.forName("android.view.Display");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		Method method = null;
		try {
			method = c.getMethod(methodString);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

		try {
			width = (Integer) method.invoke(display);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return width;
	}

	public static int getRealHeight(int sdk_version, Display display) {
		String methodString = null;
		if (sdk_version == 13)
			methodString = "getRealHeight";
		else if (sdk_version > 13)
			methodString = "getRawHeight";
		Class c = null;
		try {
			c = Class.forName("android.view.Display");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		Method method = null;
		try {
			method = c.getMethod(methodString);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		int height = -1;
		try {
			height = (Integer) method.invoke(display);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return height;
	}

	/**
	 * 读取设置的默认清晰度选择
	 * */
	public static int readCachedFormat(Context thisActivity) {
		int i;
		try {
			i = PlayerPreference.getPreferenceInt("definition");
		} catch (Exception e) {
			// TODO: handle exception
			i = Integer.parseInt(PlayerPreference
					.getPreference("definition"));
		}
		return i;
	}

	public static int readCachedLanguage(Context thisActivity) {
		return PlayerPreference.getPreferenceInt("cachepreferlanguage");
	}

	/**
	 * 字符串为否为�?
	 * */
	public static boolean isEmpty(String s) {
		if (null != s && s.length() > 0)
			return false;
		return true;
	}

	public static void showFailedMsg(String failReason) {
		//TODO
//		if (failReason.equals("数据为空")) {
//			Util.showTips(R.string.mycenter_upload_update_fail + "");
//
//		} else if (failReason.equals("网络状态不太好呀")) {
//			Util.showTips(R.string.weak_network + "");
//		} else if (failReason.equals("网络没有连接上哦")) {
//			Util.showTips(R.string.download_no_network + "");
//		} else {
//			Util.showTips(R.string.network_error + "");
//		}
	}

	/**
	 * 解析http返回的json对象 int
	 * 
	 * @param object
	 * @param name
	 *            json的key
	 * @return 对应键值的返回�?0为无返回
	 */
	public static int getJsonInt(JSONObject object, String name) {
		try {
			return object.containsKey(name) ? object.getIntValue(name) : 0;
		} catch (JSONException e) {
			Logger.e(TAG, e.toString());
			return 0;
		}
	}

	/**
	 * 解析http返回的json对象String
	 * 
	 * @param object
	 *            json对象
	 * @param name
	 *            键�?
	 * @return 对应于键值的返回�?""为无返回
	 */
	public static String getJsonValue(JSONObject object, String name) {
		try {
			return object.containsKey(name) ? object.getString(name) : "";
			// return !object.isNull(name) ? object.getString(name) : "";
		} catch (JSONException e) {
			Logger.e(TAG, e.toString());
			return "";
		}
	}

	/**
	 * 解析http返回的json对象ArrayList
	 * 
	 * @param object
	 *            json对象
	 * @param name
	 *            键�?
	 * @return 对应于键值的返回�?null为无返回
	 */
	public static ArrayList<String> getJsonStrings(JSONArray jsa) {
		if (null == jsa)
			return null;
		ArrayList<String> result = new ArrayList<String>();
		try {

			for (int i = 0; i < jsa.size(); i++) {
				result.add(jsa.getString(i));
			}
		} catch (Exception e) {
		}
		return result;
	}

	/**
	 * 将list转成string
	 * 
	 * @param list
	 * @param divider
	 * @return
	 */
	public static String listToString(List<String> list, String divider) {
		if (null == list || 0 == list.size())
			return null;
		StringBuffer sBuffer = new StringBuffer();
		for (int i = 0; i < list.size() - 1; i++) {
			sBuffer.append(list.get(i));
			sBuffer.append(divider);
		}
		sBuffer.append(list.get(list.size() - 1));
		return sBuffer.toString();
	}

	/**
	 * 解析http返回的json对象 float
	 * 
	 * @param object
	 * @param name
	 *            json的key
	 * @return 对应键值的返回�?-1为无返回
	 */
	public static float getJsonDouble(JSONObject object, String name) {
		try {
			return object.containsKey(name) ? (float) object
					.getFloatValue(name) : -1;
		} catch (JSONException e) {
			Logger.e(TAG, e.toString());
			return -1;
		}
	}

	/**
	 * 获取下载权限 第一位第三位同时�?才可下载
	 * */
	public static int getLimit(int lim) {
		return lim & 5;
	}

	public static float formatFloat(float value) {
		DecimalFormat df = new DecimalFormat();
		String style = "0.0";
		df.applyPattern(style);
		return Float.valueOf(df.format(value));
	}

	/**
	 * 获取剧集列表
	 * */
	public static ArrayList<DetailVideoSeriesList> getJsonDetailSeriesList(
			JSONArray jsonArray) {
		ArrayList<DetailVideoSeriesList> videoSeriesLists = new ArrayList<DetailVideoSeriesList>();
		if (null != jsonArray) {
			for (int i = 0; i < jsonArray.size(); i++) {
				try {
					JSONObject jsonObject;
					jsonObject = jsonArray.getJSONObject(i);
					DetailVideoSeriesList videoSeriesList = new DetailVideoSeriesList();
					// videoSeriesList.setId(jsonObject.getString("videoid"));
					videoSeriesList.setId(getJsonValue(jsonObject, "videoid"));
					videoSeriesList.setTitle(getJsonValue(jsonObject, "title"));
					videoSeriesList.setDesc(getJsonValue(jsonObject, "desc"));

					videoSeriesList.setShow_videoseq(getJsonInt(jsonObject,
							"show_videoseq"));

					videoSeriesList.setShow_videostage(getJsonValue(jsonObject,
							"show_videostage"));
					if (getJsonBoolean(jsonObject, "is_new") == true) {
						videoSeriesList.setIsNew(1);
					} else
						videoSeriesList.setIsNew(0);
					videoSeriesList.setLimited(getJsonInt(jsonObject, "limit"));
					if (jsonObject.containsKey("guest")
							&& null != jsonObject.getJSONArray("guest"))
						videoSeriesList.setGuest(getJsonArrayList(jsonObject
								.getJSONArray("guest")));
					else
						videoSeriesList.setGuest(null);
					videoSeriesLists.add(videoSeriesList);

				} catch (Exception e) {
					Logger.e(TAG,
							"int getJsonDetailSeriesList" + e.toString());
				}
			}

		}
		return videoSeriesLists;
	}

	public static boolean getJsonBoolean(JSONObject object, String name) {
		try {
			return object.containsKey(name) ? object.getBoolean(name) : false;
		} catch (JSONException e) {
			Logger.e(TAG, e.toString());
			return false;
		}
	}

	public static ArrayList<String> getJsonArrayList(JSONArray jsonArray) {
		ArrayList<String> guests = new ArrayList<String>();

		if (null != jsonArray && 0 < jsonArray.size()) {
			for (int i = 0; i < jsonArray.size(); i++) {
				try {
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					guests.add(getJsonValue(jsonObject, "name"));
				} catch (Exception e) {
					// TODO: handle exception
				}

			}
		}

		return guests;
	}

	public static Location getLocation(Context context) {
		LocationManager localLocationManager = null;
		try {
			localLocationManager = (LocationManager) context
					.getSystemService("location");
			Location localLocation;
			if (checkPermission(context,
					"android.permission.ACCESS_FINE_LOCATION")) {
				localLocation = localLocationManager
						.getLastKnownLocation("gps");
				if (localLocation != null) {
					Logger.d(
							"getLocation",
							"get location from gps:"
									+ localLocation.getLatitude() + ","
									+ localLocation.getLongitude());
					return localLocation;
				}
			}

			if (checkPermission(context,
					"android.permission.ACCESS_COARSE_LOCATION")) {
				localLocation = localLocationManager
						.getLastKnownLocation("network");
				if (localLocation != null) {
					return localLocation;
				}
			}
			return null;
		} catch (Exception localException) {
			Logger.e(TAG, localException.getMessage());
		}
		return null;
	}

	public static boolean checkPermission(Context context, String permission) {
		PackageManager localPackageManager = context.getPackageManager();
		if (localPackageManager.checkPermission(permission,
				context.getPackageName()) != 0) {
			return false;
		}
		return true;
	}

	private static double mInch = 0;
	/**
	 * 获取屏幕尺寸
	 * @param context
	 * @return
	 */
	public static double getScreenInch(Activity context) {
		if (mInch != 0.0d) {
			return mInch;
		}

		try {
			int realWidth = 0, realHeight = 0;
			Display display = context.getWindowManager().getDefaultDisplay();
			DisplayMetrics metrics = new DisplayMetrics();
			display.getMetrics(metrics);
			if (Build.VERSION.SDK_INT >= 17) {
				Point size = new Point();
				display.getRealSize(size);
				realWidth = size.x;
				realHeight = size.y;
			} else if (Build.VERSION.SDK_INT < 17
					&& Build.VERSION.SDK_INT >= 14) {
				Method mGetRawH = Display.class.getMethod("getRawHeight");
				Method mGetRawW = Display.class.getMethod("getRawWidth");
				realWidth = (Integer) mGetRawW.invoke(display);
				realHeight = (Integer) mGetRawH.invoke(display);
			} else {
				realWidth = metrics.widthPixels;
				realHeight = metrics.heightPixels;
			}

			mInch =formatDouble(Math.sqrt((realWidth/metrics.xdpi) * (realWidth /metrics.xdpi) + (realHeight/metrics.ydpi) * (realHeight / metrics.ydpi)),1);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return mInch;
	}
	/**
	 * Double类型保留指定位数的小数，返回double类型（四舍五入）
	 * newScale 为指定的位数
	 */
	private static double formatDouble(double d,int newScale) {
		BigDecimal bd = new BigDecimal(d);
		return bd.setScale(newScale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	/**
	 * 获取U+开关状态
	 */
	public static boolean isUSwitchOpen(Context context) {
		SharedPreferences sp ;
		if (Profile.PLANTFORM == Plantform.YOUKU) {
			sp = context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);
			return sp.getInt("u_switch", -1) == 1;// 优酷，0关，1开
		} else {
			sp = context.getSharedPreferences("DetailActivity_DetailProp", Context.MODE_PRIVATE);
			String v = sp.getString("detail.player.u.plus.state", "-1");
			int state = 2;
			try {
				state = !TextUtils.isEmpty(v) ? Integer.parseInt(v) : 2;
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			return state == 1;// 土豆，2关，1开
		}
	}

	public static String getQualityChangeTips(Context context, boolean start, int quality) {
		int id = -1;
		switch (quality) {
			case Profile.VIDEO_QUALITY_SD:
				id = start ? R.string.quality_change_start_SD : R.string.quality_change_end_SD;
				break;
			case Profile.VIDEO_QUALITY_HD:
				id = start ? R.string.quality_change_start_HD : R.string.quality_change_end_HD;
				break;
			case Profile.VIDEO_QUALITY_HD2:
				id = start ? R.string.quality_change_start_HD2 : R.string.quality_change_end_HD2;
				break;
		}
		if (id > 0 && context != null) {
			return context.getString(id);
		}
		return "";
	}

}
