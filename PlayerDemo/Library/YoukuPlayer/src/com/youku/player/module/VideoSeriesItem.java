/*
 * @(#)VideoSeriesItem.java	 2012-11-29
 *
 * Copyright 2005-2012 YOUKU.com
 * All rights reserved.
 * 
 * YOUKU.com PROPRIETARY/CONFIDENTIAL.
 */

package com.youku.player.module;

/**
 * @class A VideoSeriesItem.
 * @Description: TODO 视频剧集对象（剧集里的每一个视频信息）
 * 
 * @author 刘仲男
 * @version $Revision$
 * @created time 2012-11-29 上午10:32:24
 */
public class VideoSeriesItem {
	public String videoid;
	public String title;

	/** 节目中视频顺序号 */
	public int show_videoseq;

	/** 节目中视频集数或日期 */
	public String show_videostage;

	/** 类型：综艺、娱乐 */
	public String cats;

	/** 嘉宾 */
	public String[] guest;

	/** 版权信息 0-不作限制 1-禁下载 3-禁版权 */
	public int limit;

	/** 是否为更新 在72小时之内有更新 返回true 否则为false */
	public boolean is_new;

}
