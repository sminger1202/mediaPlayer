package com.youku.player.module;

import android.text.TextUtils;

import com.baseproject.utils.Util;
import com.youku.player.goplay.Point;
import com.youku.player.goplay.Profile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 视频的缓存信息
 * 
 * 2013-4-23 14:10:58
 * @author LongFan
 * 
 */
public final class VideoCacheInfo implements Cloneable {

	/** 视频标题 */
	public String title;
	/** 视频id */
	public String videoid;
	/** 视频格式类型：普通、高清等 */
	public int format;
	/** 语种信息 */
	public String language;
	/** 本地播放时长(单位：秒) */
	public int playTime;
	/** 最后播放时间点 */
	public long lastPlayTime;
	/** 剧集id */
	public String showid;
	/** 剧集标题 */
	public String showname;
	/** 剧集集数号 */
	public int show_videoseq;
	/** 剧集总集数 */
	public int showepisode_total;
	/** 视频总时长 */
	public int seconds;
	public double progress = 0;
	public long lastUpdateTime = 0l;

	/** 分片数量 */
	public int segCount = 0;
	/** 正在缓存的分片id（正在缓存第几个分片） */
	public int segId = 1;
	/** 正在缓存的分片大小 */
	public long segSize = 0l;
	/** 正在缓存的分片地址 */
	public String segUrl;
	/** 每个分片大小 */
	public long[] segsSize;
	/** 每个分片地址 */
	public String[] segsUrl;
	/** 每个分片时间 */
	public int[] segsSeconds;
	public String savePath;
	public String nextVid;
	public String picUrl;
	public String episodemode;
	public String mMediaType;
	public String registerNum;
	public String licenseNum;	
	/**
	 * 是否是竖屏视频
	 */
	public boolean isVerticalVideo = false;
    /**
     * 独播字段
     */
    public boolean exclusiveLogo;
	/** 精彩点中插点等 */
	public ArrayList<Point> adPointArray = new ArrayList<Point>();
	public JSONArray points ;
	/**
	 * 视频清晰度 对应profile {@link com.youku.player.goplay.Profile#VIDEO_QUALITY_SD }
	 * {@link com.youku.player.goplay.Profile#VIDEO_QUALITY_HD }
	 * {@link com.youku.player.goplay.Profile#VIDEO_QUALITY_HD2}
	 * {@link com.youku.player.goplay.Profile#VIDEO_QUALITY_HD3}
	 * 
	 */
	public int quality;

	public boolean mNeedWaterMark = false;
	public int mWaterMarkType = -1;

	public JSONObject toJSONObject() {
		JSONObject o = new JSONObject();
		try {
			o.put("title", title);
			o.put("vid", videoid);
			o.put("showid", showid);
			o.put("format", format);
			o.put("show_videoseq", show_videoseq);
			o.put("showepisode_total", showepisode_total);
			o.put("seconds", seconds);
			o.put("url", segUrl);
			o.put("segcount", segCount);
			o.put("segsize", segSize);
			o.put("segsseconds", Util.join(segsSeconds));
			o.put("segssize", Util.join(segsSize));
			o.put("segstep", segId);
			o.put("savepath", savePath);
			o.put("segsUrl", (segsUrl == null || segsUrl.length == 0) ? ""
					: Util.join((Object[]) segsUrl));
			o.put("progress", progress);
			if (!TextUtils.isEmpty(language)) {
				o.put("language", language);
			}
			if (!TextUtils.isEmpty(showname)) {
				o.put("showname", showname);
			}
			o.put("playTime", playTime);
			o.put("lastPlayTime", lastPlayTime);
			if (points != null && points.length() != 0) {
				o.put("points", points);
			}
		} catch (JSONException e) {
			o = null;
		}
		return o;
	}

	/** 是否按剧集排序，true按剧集排序；false按时间排序 */
	public static boolean compareBySeq = true;


	@Override
	public String toString() {
		JSONObject o = toJSONObject();
		return o == null ? "" : o.toString();
	}

	/**
	 * 获取所有中插点数组
	 * 
	 * @return 中插点数组
	 */
	public synchronized ArrayList<Point> getAdPoints() {
		if (adPointArray == null) {
			adPointArray = new ArrayList<Point>();
		} else {
			adPointArray.clear();
		}
		if (points != null && points.length() != 0) {
			for (int i = 0; i < points.length(); i++) {
				JSONObject point = points.optJSONObject(i);
				if (point != null) {
					Point p = new Point();
					p.start = point.optDouble("start") * 1000;
					p.type = point.optString("type");

					p.title = point.optString("title");
					p.desc = point.optString("desc");
					if (p.type.equals(Profile.STANDARD_POINT)
							|| p.type.equals(Profile.CONTENTAD_POINT)) {//广告点。
						adPointArray.add(p);
					}
				}
			}
		}

		return adPointArray;
	}
}
