package com.youku.player.module;

public class LiveInfo {
	public String title;
	public String desc;
	public int status;
	public String front_adid;
	public String picurl;
	public int autoplay;
	public String liveId;
	public int isPaid;
	public int isFullScreen;
	public int areaCode;
	public int dmaCode;

	public int errorCode;
	public String errorMsg;

    public boolean isVip;
	
	/**
	 * 用于统计
	 */
	public long startLoadingTime;
	public long endLoadingTime;
	
	//直播弹幕新增
	public int with_barrage;
	public int barrage_id;
	public long starttime;
	public long endtime;
	public long servertime;
	public String channel;
}
