package com.youku.player.goplay;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 广告统计
 * 
 * @author yuanfang
 * 
 */
public class Stat implements Parcelable {
	// url
	public String U;
	// 发送时间
	public String T;
	// 是否用sdk发送
	public int SDK;

	public Stat() {

	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(U);
		dest.writeString(T);
		dest.writeInt(SDK);
	}

	public Stat(Parcel source) {
		U = source.readString();
		T = source.readString();
		SDK =source.readInt();
	}

	public static final Parcelable.Creator<Stat> CREATOR = new Parcelable.Creator<Stat>() {
		@Override
		public Stat createFromParcel(Parcel source) {
			return new Stat(source);
		}

		@Override
		public Stat[] newArray(int size) {
			return new Stat[size];
		}
	};
}