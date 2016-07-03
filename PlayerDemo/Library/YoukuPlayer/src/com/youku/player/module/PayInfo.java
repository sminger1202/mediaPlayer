package com.youku.player.module;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 付费视频的付费信息
 * 
 * @author yuanfang
 * 
 */
public class PayInfo implements Parcelable {
	// 有效期
	public String duration;
	// 不能购买的意思是只能会员看
	public boolean play;
	// 原价
	public String oriprice;
	// 付费类型
	public ArrayList<String> payType;
	// 折扣价
	public String coprice;

	public String showid;

	public String showname;

	public String vip;

	public String paid_url; // 当前收费视频的跳转参数，如 psid=1&pstype=100
	public int paid; // 当前视频是否为收费视频，0不是，1是
	public int show_paid; // 当前视频所属节目是否为收费节目
	public Trial trail;

	public static class Trial {
		public String type; // 试看类型:不支持试看(cannot) 节目按时长试看(time) 节目按集数试看(episode)
		public int time; // 试 看时长（秒）
		public int episodes; // 试看集数
		public String trialStr; // 试看提示语
		public String tip;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(duration);
		dest.writeString(oriprice);
		dest.writeList(payType);
		dest.writeString(coprice);
		dest.writeInt(play ? 1 : 0);
		dest.writeString(showid);
		dest.writeString(showname);
		dest.writeString(vip);
	}

	public PayInfo() {

	}

	public PayInfo(Parcel source) {
		duration = source.readString();
		oriprice = source.readString();
		payType = source.readArrayList(String.class.getClassLoader());
		coprice = source.readString();
		play = source.readInt() == 1;
		showid = source.readString();
		showname = source.readString();
		vip = source.readString();
	}

	public boolean supportMon() {
		if (payType != null) {
			for (String type : payType) {
				if ("mon".equalsIgnoreCase(type))
					return true;
			}
		}
		return false;
	}
}
