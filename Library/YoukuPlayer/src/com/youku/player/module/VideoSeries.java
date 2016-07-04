/*
 * @(#)VideoSeries.java	 2012-11-29
 *
 * Copyright 2005-2012 YOUKU.com
 * All rights reserved.
 * 
 * YOUKU.com PROPRIETARY/CONFIDENTIAL.
 */

package com.youku.player.module;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.youku.android.player.R;
import com.youku.player.util.Constants;

/**
 * @class A VideoSeries.
 * @Description: 视频剧集
 * 
 * @author 刘仲男
 * @version $Revision$
 * @created time 2012-11-29 下午2:54:34
 */
public class VideoSeries {

	/** 视频总集数 */
	public int total;

	/** 视频类型 */
	public int type;

	/** 视频列表 */
	public ArrayList<VideoSeriesItem> videoList;

	/**
	 * TODO Comment：解析josn 生成VideoSeries对象
	 * 
	 * @param jsonString
	 * @return Series
	 */
	public static VideoSeries parseJson(String json, Context context) {

		try {
			JSONObject obj = new JSONObject(json);
			if (obj.optString("status", null) == null) {
				return null;
			}
			VideoSeries vs = new VideoSeries();
			vs.total = obj.optInt("total");
			ArrayList<VideoSeriesItem> vList = new ArrayList<VideoSeriesItem>();
			VideoSeriesItem v;
			JSONArray array = obj.getJSONArray("results");
			for (int i = 0, n = array.length(); i < n; i++) {
				obj = array.getJSONObject(i);
				v = new VideoSeriesItem();
				v.videoid = obj.optString("videoid");
				v.title = obj.optString("title");
				v.show_videoseq = obj.optInt("show_videoseq");
				v.show_videostage = obj.optString("show_videostage");
				v.cats = obj.optString("cats");
				v.limit = obj.optInt("limit");
				v.is_new = obj.optBoolean("is_new");
				JSONArray guestarray = obj.optJSONArray("guest");
				int m = guestarray.length();
				if (m != 0) {
					v.guest = new String[m];
					for (int j = 0; j < m; j++) {
						obj = guestarray.getJSONObject(j);
						v.guest[j] = obj.optString("name");
					}
				}
				vList.add(v);
			}
			if (vList.size() > 0) {
				vs.type = getTypeId(vList.get(0).cats, vs.total, context);
			}
			vs.videoList = vList;
			return vs;
		} catch (JSONException e) {
			return null;
		}
	}

	/**
	 * TODO Comment：视频类型
	 * 
	 * @param type
	 *            视频类型
	 * @param total
	 *            剧集总集数
	 * @return
	 */
	public static int getTypeId(String type, int total, Context context) {
		int typeId = 0;
		if ((context.getResources().getString(R.string.detail_tv))
				.equals(type)) {
			if (total > 1) {
				typeId = Constants.SHOW_MANY;
			} else {
				typeId = Constants.SHOW_SINLE;
			}
		} else if ((context.getResources()
				.getString(R.string.detail_movie)).equals(type)) {
			if (total > 1) {
				typeId = Constants.MOVIE_MANY;
			} else {
				typeId = Constants.MOVIE_SINGLE;
			}
		} else if ((context.getResources()
				.getString(R.string.detail_variety)).equals(type)) {
			if (total > 1) {
				typeId = Constants.VARIETY_MANY;
			} else {
				typeId = Constants.VARIETY_SINGLE;
			}
		} else if ((context.getResources()
				.getString(R.string.detail_cartoon)).equals(type)) {
			if (total > 1) {
				typeId = Constants.CARTOON_MANY;
			} else {
				typeId = Constants.CARTOON_SINGLE;
			}
		} else if ((context.getResources()
				.getString(R.string.detail_music)).equals(type)) {
			typeId = Constants.MUSIC;
		} else if ((context.getResources()
				.getString(R.string.detail_memory)).equals(type)) {
			if (total > 1) {
				typeId = Constants.MEMORY_MANY;
			} else {
				typeId = Constants.MEMORY_SINGLE;
			}
		} else if ((context.getResources()
				.getString(R.string.detail_education)).equals(type)) {
			if (total > 1) {
				typeId = Constants.EDUCATION_MANY;
			} else {
				typeId = Constants.EDUCATION_SINGLE;
			}
		} else if ((context.getResources().getString(R.string.detail_ugc))
				.equals(type)) {
			typeId = Constants.UGC;
		} else if ((context.getResources()
				.getString(R.string.detail_special)).equals(type)) {
			if (total > 1) {
				typeId = Constants.SPECIAL_MANY;
			} else {
				typeId = Constants.SPECIAL_SINGLE;
			}
		}
		return typeId;
	}
}
