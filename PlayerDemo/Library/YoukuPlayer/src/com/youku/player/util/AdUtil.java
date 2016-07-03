package com.youku.player.util;

import android.text.TextUtils;

import com.baseproject.utils.Logger;
import com.youku.player.LogTag;
import com.youku.player.ad.AdForward;
import com.youku.player.ad.AdVender;
import com.youku.player.goplay.AdvInfo;
import com.youku.player.goplay.VideoAdvInfo;

/**
 * 广告相关工具类
 */
public class AdUtil {

    /**
     * 判断clickUrl是否为下载APK类型的url
     */
    public static boolean isDownloadAPK(AdvInfo advInfo, String clickUrl) {
        if (!TextUtils.isEmpty(clickUrl)) {
            if (clickUrl.endsWith(".apk")) {
                return true;
            } else if (clickUrl.startsWith("http://val.atm.youku.com") && clickUrl.contains(".apk")) {
                return true;
            } else if (advInfo != null && advInfo.CUF == AdForward.GAME_CENTER) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAdvVideoType(final VideoAdvInfo videoAdvInfo) {
        if (videoAdvInfo != null) {
            int size = videoAdvInfo.VAL.size();
            if (size == 0) {
                Logger.d(LogTag.TAG_PLAYER, "全屏广告VC:为空");
            }
            for (int i = 0; i < size; i++) {
                if (videoAdvInfo.VAL.get(i).AT.equals("76")) {
                    return false;
                }

            }
        }
        return true;
    }

    public static boolean isAdvImageTypeYouku(final VideoAdvInfo videoAdvInfo) {
        if (videoAdvInfo != null) {
            int size = videoAdvInfo.VAL.size();
            if (size == 0) {
                Logger.d(LogTag.TAG_PLAYER, "全屏广告VC:为空");
            }
            for (int i = 0; i < size; i++) {
                if (videoAdvInfo.VAL.get(i).AT.equals("76")
                        && videoAdvInfo.VAL.get(i).SDKID == AdVender.YOUKU) {
                    return true;
                }

            }
        }
        return false;
    }

    public static int getAdvImageTypePosition(final VideoAdvInfo videoAdvInfo) {
        if (videoAdvInfo != null) {
            int size = videoAdvInfo.VAL.size();
            if (size == 0) {
                Logger.d(LogTag.TAG_PLAYER, "全屏广告VC:为空");
            }
            for (int i = 0; i < size; i++) {
                if (videoAdvInfo.VAL.get(i).AT.equals("76")) {
                    return i;
                }

            }
        }
        return -1;
    }

    public static boolean isTrueViewAd(AdvInfo advInfo){
        if(advInfo != null && advInfo.EM != null){
            return true;
        }
        return false;
    }

}
