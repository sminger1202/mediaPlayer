package com.youku.player.goplay;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * 事件监测：跳过广告、点击观看广告等
 * 
 */
public class EMSkip implements Parcelable {
	// 跳过广告倒计时
	public int T;
	// 点击跳过广告的一系列监测地址
	public ArrayList<Stat> IMP;
	// 倒计时跳过广告的文字
	public String TX1;
	// 倒计时结束后跳过广告的文字
	public String TX2;

	public EMSkip() {

	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(T);
		dest.writeList(IMP);
		dest.writeString(TX1);
		dest.writeString(TX2);

	}

	public EMSkip(Parcel source) {
		T = source.readInt();
		IMP = source.readArrayList(Stat.class.getClassLoader());
		TX1 = source.readString();
		TX2 = source.readString();
	}

	public static final Creator<EMSkip> CREATOR = new Creator<EMSkip>() {
		@Override
		public EMSkip createFromParcel(Parcel source) {
			return new EMSkip(source);
		}

		@Override
		public EMSkip[] newArray(int size) {
			return new EMSkip[size];
		}
	};
}