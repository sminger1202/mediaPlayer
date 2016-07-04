package com.youku.player.goplay;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 事件监测：跳过广告、点击观看广告等
 * 
 */
public class EventMonitor implements Parcelable {
	// 跳过广告, 包含监测地址和自定义标题
	public EMSkip SKIP;
	// 自定义点击观看广告: 既有跳转又有监测，并带自定义标题TX
	public EMView VIEW;

	public EventMonitor() {

	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(SKIP,PARCELABLE_WRITE_RETURN_VALUE);
		dest.writeParcelable(VIEW,PARCELABLE_WRITE_RETURN_VALUE);
	}

	public EventMonitor(Parcel source) {
		SKIP = source.readParcelable(EMSkip.class.getClassLoader());
		VIEW = source.readParcelable(EMView.class.getClassLoader());
	}

	public static final Creator<EventMonitor> CREATOR = new Creator<EventMonitor>() {
		@Override
		public EventMonitor createFromParcel(Parcel source) {
			return new EventMonitor(source);
		}

		@Override
		public EventMonitor[] newArray(int size) {
			return new EventMonitor[size];
		}
	};
}