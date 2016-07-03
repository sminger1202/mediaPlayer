package com.youku.player.goplay;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * 事件监测：跳过广告、点击观看广告等
 * 
 */
public class EMView implements Parcelable {
	// 点击观看广告的自定义链接标题
	public String TX;
	// 点击观看广告的跳转地址
	public String CU;
	// 点击跳过广告的一系列监测地址
	public ArrayList<Stat> IMP;
	//点击观看跳转地址的vid，移动端判断vid>0,根据vid跳转，否则用webview或内置浏览器对cu进行跳转
	public String VID;

	public EMView() {

	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(TX);
		dest.writeString(CU);
		dest.writeList(IMP);
		dest.writeString(VID);

	}

	public EMView(Parcel source) {
		TX = source.readString();
		CU = source.readString();
		IMP = source.readArrayList(Stat.class.getClassLoader());
		VID = source.readString();
	}

	public static final Creator<EMView> CREATOR = new Creator<EMView>() {
		@Override
		public EMView createFromParcel(Parcel source) {
			return new EMView(source);
		}

		@Override
		public EMView[] newArray(int size) {
			return new EMView[size];
		}
	};
}