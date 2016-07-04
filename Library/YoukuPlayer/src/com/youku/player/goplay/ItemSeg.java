package com.youku.player.goplay;

import android.os.Parcel;
import android.os.Parcelable;


public class ItemSeg implements Parcelable{
	/**
	 * xml格式为 <seg id="1" size="29225328" seconds="421" url=
	 * "http://f.youku.com/player/getFlvPath/sid/128868834110390_01/st/mp4/fileid/03000806004C4182B1B0D202A0F59DA3E73E5C-5653-647D-3F30-8B1B9DAF7494?K=af0f8b80c42d21c0161b4f94"
	 * />
	 */
	/**
	 * 段ID
	 */
	private String id;
	/**
	 * 段数据大小
	 */
	private String size;
	/**
	 * 段长度
	 */
	private int intSeconds;
	/**
	 * 段地址
	 */
	private String url;
	
	// 用来防盗链
	private String fieldId;

	/**
	 * 构造函数
	 */
	public ItemSeg(String Id, String Size, String Seconds, String Url) {
		id = Id;
		size = Size;
		intSeconds = Integer.valueOf(Seconds);
		url = Url;
	}
	
	/**
	 * 构造函数
	 */
	public ItemSeg(String id, String size, String seconds, String url, String fieldId) {
		this.id = id;
		this.size = size;
		intSeconds = Integer.valueOf(seconds);
		this.url = url;
		this.fieldId = fieldId;
	}

	/**
	 * 构造函数
	 */
	public ItemSeg(String Id, String Size, int seconds, String Url) {
		id = Id;
		size = Size;
		intSeconds = seconds;
		url = Url;
	}

	/**
	 * 设段视频地址
	 */
	public String get_Url() {
		return url;
	}

	/**
	 * 获取段视频id
	 */
	public String get_id() {
		return id;
	}

	/**
	 * 设段视频大小
	 */
	public String get_Size() {
		return size;
	}

	/**
	 * 获取段视频长度
	 */
	public int get_Seconds() {
		return intSeconds;
	}
	
	public String getFieldId() {
		return fieldId;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(size);
		dest.writeInt(intSeconds);
		dest.writeString(url);
		dest.writeString(fieldId);
	}
	public ItemSeg(Parcel source) {
		id = source.readString();
		size = source.readString();
		intSeconds = source.readInt();
		url = source.readString();
		fieldId = source.readString();
	}

	public static final Parcelable.Creator<ItemSeg> CREATOR = new Parcelable.Creator<ItemSeg>() {
		@Override
		public ItemSeg createFromParcel(Parcel source) {
			return new ItemSeg(source);
		}

		@Override
		public ItemSeg[] newArray(int size) {
			return new ItemSeg[size];
		}
	};
}