package com.youku.player.goplay;

import android.os.Parcel;
import android.os.Parcelable;

public class Point implements Parcelable {
	public double start;
	public String type;
	public String title;
	public String desc;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeDouble(start);
		dest.writeString(type);
		dest.writeString(title);
		dest.writeString(desc);
	}

	public Point() {
		super();
	}

	public Point(Parcel source) {
		start = source.readDouble();
		type = source.readString();
		title = source.readString();
		desc = source.readString();
	}

	public static final Parcelable.Creator<Point> CREATOR = new Parcelable.Creator<Point>() {
		@Override
		public Point createFromParcel(Parcel source) {
			return new Point(source);
		}

		@Override
		public Point[] newArray(int size) {
			return new Point[size];
		}
	};
}