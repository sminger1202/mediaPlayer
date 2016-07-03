package com.youku.player.subtitle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Environment;
import android.text.TextUtils;

import com.baseproject.utils.Logger;
import com.youku.player.util.PlayerPreference;

public class SubtitleManager {
	public static String TAG = "SubtitleManager";
	
	public static boolean sHasSubtitle = false;
	
	public static final int ONLINE_TYPE = 0;
	
	public static final int LOCAL_TYPE = 1;
	
	private boolean ready = false;
	
	private int curSubtitleIndex;
	
	private Subtitle curSubtitle;
	
	private Subtitle nextSubtitle;
	
	private Boolean stopFind;
	
	private Boolean isSeeking; 
	
	private int count;
	
	public static float fontSize = 20;

	public static int fontColor = android.graphics.Color.RED;
	
	private SubtitleSeekThread subtitleSeekThread;
	
	private final static String LINE_SEPARATOR = System.getProperty("line.separator");
	
	private final int STOP_FIND = -100;
	
	private List<Subtitle> subtitles = new ArrayList<Subtitle>();
	
	public static int sMode = 0;
	
	public static final int NO_SUBTITLE = -1;
	
	public static final int SIMPLIFIED_CHINESE = 0;
	
	public static final int TRADITIONAL_CHINESE = 1;

	public static final int ENGLISH = 2;
	
	public static final int SIMPLIFIED_AND_ENGLISH = 3;
	
	public static final int TRADITIONAL_AND_ENGLISH = 4;
	
	public static String sChsContent = "";
	
	public static String sChtContent = "";
	
	public static String sEnContent = "";
	
	public static List<DownloadedSubtitle> sDownloadedSubtitles;
	
	//private static SubtitleManager mInstance = new SubtitleManager();
	
	public SubtitleManager() {
	}
	
//	public static SubtitleManager getInstance() {
//		return mInstance;
//	}
	
	public static void removeSubtitle(String lang) {
		if (sDownloadedSubtitles == null || sDownloadedSubtitles != null && sDownloadedSubtitles.size() <= 0) {
			return;
		}
		
		DownloadedSubtitle subtitle;
		int location = 0;
		
		for (; location < sDownloadedSubtitles.size(); location++) {
			subtitle = sDownloadedSubtitles.get(location);
			if (subtitle.lang.equals(lang)) break;
		}
		
		if (location < sDownloadedSubtitles.size()) {
			sDownloadedSubtitles.remove(location);
		}
		
	}
	
	public void init() {
		Logger.d(TAG, "init()");
		
		ready = false; 
		
		isSeeking = false;
		
		curSubtitleIndex = 0;

		if (subtitles != null) {
			subtitles.clear();
		}
		
	}
	
	public static void setCurrentMode(int mode) {
        Logger.d(TAG,"成功进入setCurrentMode()方法！");
        Logger.d(TAG, "setCurrentMode() : mode = " + mode);
        Logger.d(TAG,"setCurrentMode()保存的mode = "+ mode);
		sMode = mode;
		PlayerPreference.savePreference("subtitleMode", mode);//20150512添加
	}
	
	public static void setDefaultMode() {
        Logger.d(TAG,"已成功进入setDefaultMode()方法！");
		
		if (sDownloadedSubtitles == null) {
			sMode = NO_SUBTITLE;
			return;
		}
		
		boolean chs = false;
		
		boolean cht = false;
		
		boolean en = false;
		
		boolean tmp = false;//20150512添加
		
		int tMode = PlayerPreference.getPreferenceInt("subtitleMode");
        Logger.d(TAG,"从SharedPreferences中读取的mode值为： "+ tMode);
		
		for (DownloadedSubtitle subtitle : sDownloadedSubtitles) {
			//判断前一个视频的字幕是否在下一个视频中存在
            Logger.d(TAG,"subtitle.mode = " + subtitle.mode);
            Logger.d(TAG,"subtitle.name = " + subtitle.name);
//            Logger.d(TAG,"subtitle.fullname = " + subtitle.fullName);
            Logger.d(TAG,"subtitle.lang = " + subtitle.lang);
            Logger.d(TAG,"subtitle.path = " + subtitle.path);
			if (subtitle.mode == tMode){
				tmp = true;
			}//20150512添加
			
			// 简体中文
			if (subtitle.lang.equals("chs")) {
				chs = true;
				Logger.d(TAG, "has chs");
			}

			// 繁体中文
			if (subtitle.lang.equals("cht")) {
				cht = true;
				Logger.d(TAG, "has cht");
			}

			// 英文
			if (subtitle.lang.equals("en")) {
				en = true;
				Logger.d(TAG, "has en");
			}
		}
		

		/*******************20150804修改***********************/
		if (tMode == NO_SUBTITLE) {
			sMode = tMode;
		}else if (tMode == SIMPLIFIED_AND_ENGLISH){
			if (chs && en){
				sMode = tMode;
			}else if (chs && (!en)){
				sMode = SIMPLIFIED_CHINESE;
			}else if ((!chs) && en){
				sMode = ENGLISH;
			}
		}else if (tMode == TRADITIONAL_AND_ENGLISH){
			if (cht && en){
				sMode = tMode;
			}else if (cht && (!en)){
				sMode = TRADITIONAL_CHINESE;
			}else if ((!cht) && en){
				sMode = ENGLISH;
			}
		}else if (tMode == SIMPLIFIED_CHINESE){
			if (chs){
				sMode = tMode;
			} else if (cht) {
				sMode = TRADITIONAL_CHINESE;
			} else if (en) {
				sMode = ENGLISH;
			} else {
				sMode = NO_SUBTITLE;
			}
		}else if (tMode == TRADITIONAL_CHINESE){
			if (cht){
				sMode = tMode;
			} else if (chs) {
				sMode = SIMPLIFIED_CHINESE;
			} else if (en) {
				sMode = ENGLISH;
			} else {
				sMode = NO_SUBTITLE;
			}
		}else if (tMode == ENGLISH){
			if (en){
				sMode = tMode;
			} else if (chs) {
				sMode = SIMPLIFIED_CHINESE;
			} else if (cht) {
				sMode = TRADITIONAL_CHINESE;
			} else {
				sMode = NO_SUBTITLE;
			}
		} else if (chs) {
			sMode = SIMPLIFIED_CHINESE;
		} else if (cht) {
			sMode = TRADITIONAL_CHINESE;
		} else if (en) {
			sMode = ENGLISH;
		} else {
			sMode = NO_SUBTITLE;
		}

	}
	
	public static void clearAllSubtitle() {
		if (sDownloadedSubtitles != null) {
			sDownloadedSubtitles.clear();
			sDownloadedSubtitles = null;
		}
		
		sChsContent = null;
		sChtContent = null;
		sEnContent = null;
		
		sMode = NO_SUBTITLE;
	}
	
	public static void addSubtitle(DownloadedSubtitle subtitle) {
		if (sDownloadedSubtitles == null) {
			sDownloadedSubtitles = new ArrayList<DownloadedSubtitle>();
		}
		sDownloadedSubtitles.add(subtitle);
	}
	
	public static List<DownloadedSubtitle> getDownloadedSubtitles() {
		Logger.d(TAG, "getDownloadedSubtitle()");
		
		return sDownloadedSubtitles;
	}
	
	public static void setHasSubtitle(Boolean flag) {
		Logger.d(TAG, "setHasSubtitle flag = " + flag);
		sHasSubtitle = flag;
	}
	
	public static Boolean hasSubtitle() {
		Logger.d(TAG, "hasSubtitle = " + sHasSubtitle);
		return sHasSubtitle;
	}
	
	public Subtitle getNext() {
		Logger.d(TAG, "getNext()");
		
		if (curSubtitleIndex + 1 < count) {
			curSubtitleIndex++;
			curSubtitle = subtitles.get(curSubtitleIndex);
		} 
		
		if (curSubtitleIndex + 1 < count) {
			nextSubtitle = subtitles.get(curSubtitleIndex + 1);
		} else {
			nextSubtitle = null;
		}
		
		Logger.d(TAG, "cur.start = " + curSubtitle.start + ", cur.end = " + curSubtitle.end);
		
		return curSubtitle;
	}
	
	public Subtitle getSubtitle(long time) {
		Logger.d(TAG, "getSubtitle " + time);
		Logger.d(TAG, "cur.start = " + curSubtitle.start + ", cur.end = " + curSubtitle.end);
		
		if (time >= curSubtitle.start && time <= curSubtitle.end) {
			Logger.d(TAG, "------时间在当前字幕中------");
			return curSubtitle;
		}
		
		if (curSubtitleIndex == 0 && time <= curSubtitle.start) {
			Logger.d(TAG, "------时间小于第一条字幕的时间------");
			return curSubtitle;
		}
		
		if (curSubtitleIndex > 0 && subtitles.get(curSubtitleIndex - 1).end < time && time < curSubtitle.end) {
			Logger.d(TAG, "------当前字幕即将显示------");
			return curSubtitle;
		}
		
		if (nextSubtitle != null && time > curSubtitle.end && time <= nextSubtitle.end) {
			Logger.d(TAG, "------获取下一条字幕------");
			return getNext();
		}
		
		seek(time);
		
		return curSubtitle;
	}
	
	public void seek(long seekTo) {
		Logger.d(TAG, "seek : " + seekTo);
		if (isSeeking) {
			stopFind = true;
		} else {
			stopFind = false;
		}
		
		while(isSeeking) {
			Logger.d(TAG, "is Seeking...");
		}
		
		subtitleSeekThread = new SubtitleSeekThread();
		subtitleSeekThread.seekTo(seekTo);
		subtitleSeekThread.start();
	}
	
	class SubtitleSeekThread extends Thread{

		long seekTo;
		
		public void seekTo(long seekTo) {
			isSeeking = true;
			this.seekTo = seekTo;
		}
		
		@Override
		public void run() {
			Logger.d(TAG, "seek begin " + seekTo);
			Subtitle subtitle;

			int index = find(seekTo);

			if (STOP_FIND != index) {
				if (index >= subtitles.size()) {
					index = subtitles.size() - 1;
				}

				subtitle = subtitles.get(index);

				if (seekTo >= subtitle.start && seekTo <= subtitle.end || index == 0) {
					curSubtitleIndex = index;
					curSubtitle = subtitles.get(curSubtitleIndex);
					if (curSubtitleIndex + 1 >= subtitles.size()) {
						nextSubtitle = null;
					} else {
						nextSubtitle = subtitles.get(curSubtitleIndex + 1);
					}
				} else if (index > 0) {
					curSubtitleIndex = index - 1;
					curSubtitle = subtitles.get(curSubtitleIndex);
					nextSubtitle = subtitles.get(index);
				} /*else if (index == 0) {
					curSubtitleIndex = index;
					curSubtitle = subtitles.get(curSubtitleIndex);
					if (curSubtitleIndex + 1 >= subtitles.size()) {
						nextSubtitle = null;
					} else {
						nextSubtitle = subtitles.get(curSubtitleIndex + 1);
					}
				}
*/
			}

			isSeeking = false;
			Logger.d(TAG, "cur.start = " + curSubtitle.start + ", cur.end = "
					+ curSubtitle.end);
			Logger.d(TAG, "seek end " + seekTo);
		}
		
	}
	
	public boolean isReady() {
		
		return ready;
	}
	
	/**
	 * 将long型格式的时间转换为字符串格式，只转换到秒，例如：00:00:00
	 * 
	 * */
	public  String time2string(long time) {
		String string = null;
		long hour, minute, second;
		String hourStr, minuteStr, secondStr;
		hour = time / (3600 * 1000);
		time = time % (3600 * 1000);
		minute = time / (60 * 1000);
		time = time % (60 * 1000);
		second = time / 1000;

		if (hour < 10) {
			hourStr = "0" + String.valueOf(hour);
		} else {
			hourStr = String.valueOf(hour);
		}
		
		if (minute < 10) {
			minuteStr = "0" + String.valueOf(minute);
		} else {
			minuteStr = String.valueOf(minute);
		}
		
		if (second < 10) {
			secondStr = "0" + String.valueOf(second);
		} else {
			secondStr = String.valueOf(second);
		}
		
		string = hourStr + ":" + minuteStr + ":" + secondStr;
		return string;
	}
	
	/**
	 * 将字符串格式的时间转换为long型，单位（微秒）
	 * 
	 * */
	public  long string2time(String times) {
		long time = 0;
		String[] hmsm;
        String[] hms;
		long hour, minute, second, mill;
		
		if (times == null || times != null && times.equals("")) {
			return 0;
		}
		
		hmsm = times.split("\\.");
		
		if (hmsm != null && hmsm.length == 2) {
			hms = hmsm[0].split(":");
			
			if (hms != null && hms.length == 3) {
				hour = Long.parseLong(hms[0]);
				minute = Long.parseLong(hms[1]);
				second = Long.parseLong(hms[2]);
				mill = Long.parseLong(hmsm[1]);
				
				time = hour * 3600 * 1000 + minute * 60 * 1000 + second * 1000 + mill;
			} else {
				time = 0;
			}
		} else {
			time = 0;
		}
		
		return time;
	}
	
	/**
	 * 判断字幕文件是否存在
	 * 
	 * @param path 字幕文件的存放路径
	 * @param name 字幕文件名
	 * @return
	 */
	public  Boolean exist(String name) {
		Logger.d(TAG, "haveSubtitleFile called!");
		
		String path = getDownloadPath();
		if (path == null || name == null)
			return false;
		
		if (!path.endsWith("/")) {
			path += "/";
		}
		
		Logger.d(TAG, "path = " + path + ", name = " + name);

		File f = new File(path + name);
		if (f.exists()) {
			return true;
		}
		
		return false;
	}
	
	public  String getJsonValue(JSONObject object, String name) {
		if (object != null)
			return object.optString(name);
		return "";
	}

	public  int getJsonInt(JSONObject object, String name, int defaultValue) {
		try {
			return object.isNull(name) ? defaultValue : object.getInt(name);
		} catch (JSONException e) {
			return defaultValue;
		}
	}
	
	
	/**
	 * 解析srt格式字幕
	 * 
	 * @param jsonString [in] srt格式的字幕
	 */
	public  boolean parseSrt(String jsonString) {
		Logger.d(TAG, "parseSrt() \n" + jsonString);
		
		if (jsonString == null) {
			Logger.e(TAG, "parseResponse : jsonString = null ");
			return false;
		}
		
		JSONObject jsonObject = null;
		
		try {
			jsonObject = new JSONObject(jsonString);
		} catch (JSONException e) {
			Logger.e(TAG, e);
		}

		if (jsonObject == null) {
			Logger.e(TAG, "parseResponse : jsonObject == null ");
			return false;
		}
		
		if (!jsonObject.has("results")) {
			Logger.e(TAG, "parseResponse : jsonObject has not results");
			return false;
		}
		
		JSONArray resultArray = jsonObject.optJSONArray("results");
		
		if (resultArray == null) {
			Logger.e(TAG, "parseResponse : resultArray == null");
			return false;
		}
		
		long prevEnd = -1L;
		
		for (int i = 0; i < resultArray.length(); i++) {
			JSONObject object = resultArray.optJSONObject(i);
			if (object == null) {
				continue;
			}
			
			Subtitle subtitle = new Subtitle();
			subtitle.start = string2time(getJsonValue(object, "start"));
			subtitle.end = string2time(getJsonValue(object, "end"));
			subtitle.text = Crypt.decode(getJsonValue(object, "text"));
			
			if (subtitle.start > subtitle.end || subtitle.end < prevEnd) {
				subtitle = null;
				continue;
			}
			
			prevEnd = subtitle.end;
			
			subtitles.add(subtitle);
		}
		
		count = subtitles.size();
		if (count > 0) {
			curSubtitle = subtitles.get(0);
			ready = true;
		}
		
		if (count > 1) {
			nextSubtitle = subtitles.get(1);
		} else {
			nextSubtitle = null;
		}
		
		if (count > 0) {
			return true;
		} else { 
			return false;
		}
		
	}
	
	public String readFile(String path) {

		Logger.d(SubtitleManager.TAG, "path = " + path);
		BufferedReader reader;
		final StringBuilder sb = new StringBuilder();
		String line = null;
		FileInputStream inputStream = null;
		
		try {
			inputStream = new FileInputStream(path);
			reader = new BufferedReader(new InputStreamReader(inputStream));
			while ((line = reader.readLine()) != null) {
				sb.append(line);
				sb.append(LINE_SEPARATOR);
			}
		} catch (IOException e) {
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {

			}
		}
		return sb.toString();
	}
	
	public boolean prepare(String lang) {
		if (lang == null || lang != null && lang.equals("")) 
			return false;
		
		if ("chs".equals(lang)) {
			
			if (sChsContent != null) {
				return parseSrt(sChsContent);
			}
			
		} else if ("cht".equals(lang)) {
			
			if (sChtContent != null) {
				return parseSrt(sChtContent);
			}
			
		} else if ("en".equals(lang)) {
			
			if (sEnContent != null) {
				return parseSrt(sEnContent);
			}
			
		}
		
		return false;
	}
	
	public boolean prepare(String path, String name) {
		if (!path.endsWith("/")) {
			path += "/";
		}
		
		String content = readFile(path + name);

		return parseSrt(content);
	}
	
	public static final int IN_CURRENT_SUBTITLE = 0;
	public static final int IN_NEXT_SUBTITLE = 1;
	
	class SeekResult{
		
		public SeekResult(int flag, int index) {
			this.flag = flag;
			this.index = index;
		}
		
		int flag;
		int index;
	}
	
	/**
	 * 查找指定时间time的字幕
	 * 
	 * @return 找到，返回索引值；否则，返回最接近time的下一条字幕的索引值
	 * */
	synchronized public int find(long time) {
		int low, high, mid = 0;
		
		Subtitle subtitle;
		
		if (subtitles == null || (subtitles != null && subtitles.size() <= 0)) {
			return -1;
		}
		
		low = 0;
		high = subtitles.size() - 1;
		
		while ((low <= high) && !stopFind) {
			
			mid = (low + high) >> 1;
			
			subtitle = subtitles.get(mid);
			
			if (time < subtitle.start) {
				high = mid - 1;
			} else if (time > subtitle.end) {
				low = mid + 1;
			} else {
				return mid;
			}
		}
		
		if (stopFind) {
			return STOP_FIND;
		}
		
		return low;
	}
	
	
	public static boolean hasSDCard() {
		return Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
	}

	public static String getDefauleSDCardPath() {
		return hasSDCard() ? Environment.getExternalStorageDirectory()
				.getAbsolutePath() : "";
	}
	
	public static String getDownloadPath() {
		String path = SubtitleManager.getDefauleSDCardPath();
		
		if (path != null && TextUtils.getTrimmedLength(path) > 0) {
			File downloadFile = new File(path + "/youku/subtitles");
			boolean success = true;
			if (!downloadFile.exists()) {
				success = downloadFile.mkdirs();
			}
			
			if (success) return downloadFile.getAbsolutePath();
			
		} 
		
		return null;
	}
	
}
