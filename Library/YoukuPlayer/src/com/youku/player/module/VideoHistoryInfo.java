package com.youku.player.module;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * 视频的历史信息
 * 
 * @author LongFan
 * 
 */
public class VideoHistoryInfo implements Parcelable{

	/**
	 * 上次播放结束时间,自1970年来所经历的时间，单位秒
	 */
	public long lastPlayTime;
	/**
	 * 播放时长，单位为秒
	 */
	public int playTime;
	/**
	 * 是否有下一个正片，0表示无，1表示有(节目类该字段有效)
	 */
	public int isStage;
	/**
	 * 当前视频的集数
	 */
	public int stage;
	/**
	 * 标题
	 */
	public String title;
	/**
	 * 视频长度
	 */
	public int duration;
	/**
	 * 视频的vid
	 */
	public String vid;
	/**
	 * 视频的showID
	 */
	public String showid; 
	

	public VideoHistoryInfo() {

	}

	public VideoHistoryInfo(Parcel source) {
		title = source.readString();
		duration = source.readInt();
		vid = source.readString();
		showid = source.readString();
		playTime = source.readInt();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(title);
		dest.writeInt(duration);
		dest.writeString(vid);
		dest.writeString(showid);
		dest.writeInt(playTime);

	}

	/**
	 * 实现Parcelable接口的greator
	 */
	public static final Parcelable.Creator<VideoHistoryInfo> CREATOR = new Parcelable.Creator<VideoHistoryInfo>() {

		@Override
		public VideoHistoryInfo createFromParcel(Parcel source) {
			return new VideoHistoryInfo(source);
		}

		@Override
		public VideoHistoryInfo[] newArray(int size) {
			return new VideoHistoryInfo[size];
		}
	};

}
