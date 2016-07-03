package com.youku.player.plugin;


import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 剧集列表、缓存剧集、分集剧情
 * */
public class DetailVideoSeriesList implements Parcelable {
	private String id;
	private String title;
	private int show_videoseq=-1;// 节目中视频顺序号
	private String show_videostage;// 节目中视频集数或日期
	private String desc;//
	private int limited;// 版权信息 0-不作限制 二进制第1位为1-禁下载 第2位-禁评论 第3位-禁版权
	private String format;//视频格式
	private ArrayList<String> guest;//针对综艺的嘉宾
	private int cached=0;//是否已缓存 0:未缓存 1：已缓存
	private float play_history=0;
	private int isplaying=0;//当前播放1:正在播放该集
	
	public int isIsplaying() {
		return isplaying;
	}

	public void setIsplaying(int isplaying) {
		this.isplaying = isplaying;
	}

	//播放历史百分比
	public float getPlay_history() {
		return play_history;
	}

	public void setPlay_history(float play_history) {
		this.play_history = play_history;
	}
	private int cache_state=0;
	
	/**缓存状态 0：未缓存 1、在临时缓存列表中2、已缓存*/
	public int getCache_state() {
		return cache_state;
	}

	public void setCache_state(int cache_state) {
		this.cache_state = cache_state;
	}

	public int isCached() {
		return cached;
	}

	public void setCached(int cached) {
		this.cached = cached;
	}

	public ArrayList<String> getGuest() {
		return guest;
	}
	private int isNew=0;
	

	public int getIsNew() {
		return isNew;
	}

	public void setIsNew(int isNew) {
		this.isNew = isNew;
	}

	public void setGuest(ArrayList<String> guest) {
		this.guest = guest;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public DetailVideoSeriesList() {
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getShow_videoseq() {
		return show_videoseq;
	}

	public void setShow_videoseq(int show_videoseq) {
		this.show_videoseq = show_videoseq;
	}

	public String getShow_videostage() {
		return show_videostage;
	}

	public void setShow_videostage(String show_videostage) {
		this.show_videostage = show_videostage;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public int getLimited() {
		return limited;
	}

	public void setLimited(int limited) {
		this.limited = limited;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(title);
		dest.writeInt(show_videoseq);
		dest.writeString(show_videostage);
		dest.writeString(desc);
		dest.writeInt(limited);
		dest.writeString(format);
		dest.writeList(guest);
		dest.writeInt(isNew);
		dest.writeInt(cached);
		dest.writeInt(isplaying);

	}

	public static final Parcelable.Creator<DetailVideoSeriesList> CREATOR = new Parcelable.Creator<DetailVideoSeriesList>() {

		@Override
		public DetailVideoSeriesList createFromParcel(Parcel source) {
			return new DetailVideoSeriesList(source);
		}

		@Override
		public DetailVideoSeriesList[] newArray(int size) {
			return new DetailVideoSeriesList[size];
		}
	};

	protected DetailVideoSeriesList(Parcel parcel) {
		id = parcel.readString();
		title = parcel.readString();
		show_videoseq = parcel.readInt();
		show_videostage = parcel.readString();
		desc = parcel.readString();
		limited = parcel.readInt();
		format=parcel.readString();
		guest=parcel.readArrayList(getClass().getClassLoader());
		isNew=parcel.readInt();
		cached=parcel.readInt();
		isplaying=parcel.readInt();

	}
}
