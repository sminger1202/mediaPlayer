package com.youku.player.ad;

import android.text.TextUtils;

import com.youku.player.module.VideoUrlInfo;
import com.youku.uplayer.MediaPlayerProxy;

/**
 * 请求广告需要的相关信息
 */
public class AdGetInfo {
    public String vid;
    public boolean isFullscreen;
    public boolean isOfflineAd; // 是否离线广告
    public int noqt; // 是否不请求前贴广告，1代表无前贴，0代表有
    public int position; // 7表示前贴， 10表示暂停， 8表示中插
    public int paid; // is paid video or not, 1 or 0;
    public String trailType; // 试看类型
    public int ps; //中插广告使用，第几个中插点
    public double pt; //中插广告使用，中插点时间戳
    public String playlistCode = "";
    public String ev; // 播放入口，广告接口根据情况返回不同的广告内容，推荐过来的ev=1001
    public String playlistId = "";
    public int isvert = 0; // 是否为竖屏, 0代表横屏，1代表竖屏，没传默认为横屏0.

    public AdGetInfo(final String id, int p, boolean isFull, boolean isOffline, String ev, String playlistId,
                     final VideoUrlInfo videoInfo, boolean isvert) {
        this(id, p, isFull, isOffline, playlistId, videoInfo, 0, 0, isvert);
        this.ev = ev;
    }

    public AdGetInfo(final String id, int p, boolean isFull, boolean isOffline, String playlistId,
                     final VideoUrlInfo videoInfo, int ps, double pt, boolean isvert) {
        vid = id;
        position = p;
        isFullscreen = isFull;
        isOfflineAd = isOffline;
        noqt = MediaPlayerProxy.isUplayerSupported() ? 0 : 1;
        this.ps = ps;
        this.pt = pt;
        if (videoInfo != null) {
            if (videoInfo.mPayInfo != null) {
                paid = videoInfo.mPayInfo.paid;
                if (videoInfo.mPayInfo.trail != null) {
                    trailType = videoInfo.mPayInfo.trail.type;
                }
            }

            if (videoInfo.playlistCode != null
                    && videoInfo.playlistCode.length() > 0) {
                this.playlistCode = videoInfo.playlistCode;
            }
        }
        if (!TextUtils.isEmpty(playlistId))
            this.playlistId = playlistId;
        this.isvert = isvert ? 1 : 0;
    }
}
