package com.youku.player.goplay;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 广告信息
 *
 * @author yuanfang
 */
public class AdvInfo implements Parcelable {

    public ArrayList<Stat> SUE;
    // 广告地址，视频的话就是视频地址
    public String RS;
    // 广告类型
    public String RST;
    // 广告位
    public String AT;
    // 广告统计
    public ArrayList<Stat> SU;
    //
    public ArrayList<Stat> SUS;

    public ArrayList<Stat> CUM;

    public ArrayList<Stat> SHU;

    public ArrayList<Stat> DUS;

    public ArrayList<Stat> DUE;
    // 点击访问的url
    public String CU;
    // 服务器调试信息 如果是"1"则曝光VC 如果是"2"不显示素材，只曝光VC
    public String VT;
    // 服务器调试信息
    public String VC;
    // 广告时常，单位秒
    public int AL;
    // 广告SDK
    public int SDKID;
    // 点击跳转的方式,1:webview 或 0:browser
    public int CUF;
    // 调查问卷显示文字
    public String VN;
    // 调查问卷显示时间
    public int VP;
    // 调查问卷地址
    public String VSC;
    // 互动广告素材地址
    public String BRS;
    // 站内视频广告的视频ID
    public String CUU;
    // VT、VC、BRS等暂不管

    // 统计发送字段
    public int PST;
    public String IE;

    // 是否播放过当前广告
    private boolean played;

    // 事件监测：跳过广告、点击观看广告等(用于trueView广告)
    public EventMonitor EM;

    public AdvInfo() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(RS);
        dest.writeString(RST);
        dest.writeString(AT);
        dest.writeList(SU);
        dest.writeList(SUS);
        dest.writeList(SUE);
        dest.writeList(CUM);
        dest.writeList(SHU);
        dest.writeList(DUS);
        dest.writeList(DUE);
        dest.writeString(CU);
        dest.writeString(VC);
        dest.writeString(VT);
        dest.writeInt(AL);
        dest.writeInt(SDKID);
        dest.writeInt(CUF);
        dest.writeString(VN);
        dest.writeString(VSC);
        dest.writeInt(VP);
        dest.writeString(BRS);
        dest.writeString(CUU);
        dest.writeInt(PST);
        dest.writeString(IE);
        dest.writeParcelable(EM,PARCELABLE_WRITE_RETURN_VALUE);
    }

    @SuppressWarnings("unchecked")
    public AdvInfo(Parcel source) {
        RS = source.readString();
        RST = source.readString();
        AT = source.readString();
        SU = source.readArrayList(Stat.class.getClassLoader());
        SUS = source.readArrayList(Stat.class.getClassLoader());
        SUE = source.readArrayList(Stat.class.getClassLoader());
        CUM = source.readArrayList(Stat.class.getClassLoader());
        SHU = source.readArrayList(Stat.class.getClassLoader());
        DUS = source.readArrayList(Stat.class.getClassLoader());
        DUE = source.readArrayList(Stat.class.getClassLoader());
        CU = source.readString();
        VC = source.readString();
        VT = source.readString();
        VN = source.readString();
        VSC = source.readString();
        VP = source.readInt();
        AL = source.readInt();
        SDKID = source.readInt();
        CUF = source.readInt();
        BRS = source.readString();
        CUU = source.readString();
        PST = source.readInt();
        IE = source.readString();
        EM = source.readParcelable(EventMonitor.class.getClassLoader());
    }

    public static final Parcelable.Creator<AdvInfo> CREATOR = new Parcelable.Creator<AdvInfo>() {
        @Override
        public AdvInfo createFromParcel(Parcel source) {
            return new AdvInfo(source);
        }

        @Override
        public AdvInfo[] newArray(int size) {
            return new AdvInfo[size];
        }
    };

    public boolean played() {
        return played;
    }

    public void setPlayed(boolean played) {
        this.played = played;
    }
}