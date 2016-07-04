package com.youku.player.goplay;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 视频广告信息
 * @author yuanfang
 *
 */
public class VideoAdvInfo implements Parcelable {

	// 广告位
	public String P;
	// 中插相关
	public String JS;
	// 广告信息
	public ArrayList<AdvInfo> VAL;
	// 是否出现“跳过广告”
	public String SKIP;

	public VideoAdvInfo(){
		
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(P);
		dest.writeString(JS);
		dest.writeList(VAL);
		dest.writeString(SKIP);
	}

	@SuppressWarnings("unchecked")
	public VideoAdvInfo(Parcel source) {
		P = source.readString();
		JS = source.readString();
		VAL = source.readArrayList(AdvInfo.class.getClassLoader());
		SKIP = source.readString();
	}

	/**
	 * 实现Parcelable接口的greator
	 */
	public static final Parcelable.Creator<VideoAdvInfo> CREATOR = new Parcelable.Creator<VideoAdvInfo>() {
		@Override
		public VideoAdvInfo createFromParcel(Parcel source) {
			return new VideoAdvInfo(source);
		}

		@Override
		public VideoAdvInfo[] newArray(int size) {
			return new VideoAdvInfo[size];
		}
	};
}
