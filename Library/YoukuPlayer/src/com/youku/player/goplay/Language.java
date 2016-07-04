package com.youku.player.goplay;

import android.os.Parcel;
import android.os.Parcelable;

/** 视频的语言 */
public class Language implements Parcelable {
	public String lang = null;
	public String vid = null;
	public boolean isDisplay = false;
	public String langCode;
	public int id;//语言id

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(isDisplay?1:0);
		dest.writeString(lang);
		dest.writeString(vid);
		dest.writeString(langCode);
		dest.writeInt(id);
	}

	public Language() {
		super();
	}

	public Language(Parcel source) {
		isDisplay = source.readInt() == 1;
		lang = source.readString();
		vid = source.readString();
		langCode = source.readString();
		id = source.readInt();
	}

	public static final Parcelable.Creator<Language> CREATOR = new Parcelable.Creator<Language>() {
		@Override
		public Language createFromParcel(Parcel source) {
			return new Language(source);
		}

		@Override
		public Language[] newArray(int size) {
			return new Language[size];
		}
	};
}