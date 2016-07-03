package com.youku.player.util;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

//import com.alimm.ad.mobile.open.AdLogic;
//import com.alimm.ad.mobile.open.model.MediaFile;
import com.baseproject.utils.Logger;
import com.baseproject.utils.UIUtils;
import com.youdo.XNativeAdManager;
import com.youdo.vo.XAdInstance;
import com.youku.player.LogTag;
import com.youku.player.ad.OfflineAdSDK;
import com.youku.player.config.MediaPlayerConfiguration;
import com.youku.player.goplay.AdvInfo;
import com.youku.player.goplay.Stat;
import com.youku.player.module.VideoUrlInfo;
import com.youku.player.service.DisposableHttpTask;
import com.youku.player.service.DisposableHttpTaskApache;

import java.util.ArrayList;
import java.util.List;

import cn.com.mma.mobile.tracking.api.Countly;

public class DisposableStatsUtils {

    public static final String TAG = LogTag.TAG_PLAYER;
    private static final String FILENAME = "DisposableStatsUtils";
    public static final String REQUEST_SUMARY = "发送广告统计 ";

    public static final String TYPE_VC = "VC";

    public static final String TYPE_PAUSED_VC = "PAUSED_VC";

    public static final String TYPE_SUS = "SUS";
    public static final String TYPE_SU = "SU";
    public static final String TYPE_SUE = "SUE";
    public static final String TYPE_SHU = "SHU";

    public static final String TYPE_PAUSED_SUS = "PAUSED_SUS";
    public static final String TYPE_PAUSED_SUE = "PAUSED_SUE";

    public static final String TYPE_SKIP_IMP = "SKIP_IMP";
    public static final String TYPE_VIEW_IMP = "VIEW_IMP";

    public static final String TYPE_CUM = "CUM";

    public static final int SEND_BY_MMA = 1;
    public static final int SEND_BY_ALIMM = 2;
    public static final int SEND_BY_ADSDK = 9;

    public static final String SDK_TAG = "adv_sdk";

    private DisposableStatsUtils() {
    }

    /**
     * 开始播放视频广告时发送VC *
     */
    public static void disposeVC(VideoUrlInfo videoInfo) {

        logDebug(TYPE_VC);

        AdvInfo advInfo = getAdvInfo(videoInfo);

        disposeVC(TYPE_VC, advInfo);
    }

    /**
     * 开始播放视频广告时发送VC *
     */
    public static void disposeVC(AdvInfo advInfo) {

        logDebug(TYPE_VC);

        //AdvInfo advInfo = getAdvInfo(videoInfo);

        disposeVC(TYPE_VC, advInfo);
    }

    /**
     * 暂停广告开始播放时获取，发送PAUSED_VC *
     */
    public static void disposePausedVC(AdvInfo advInfo) {

        logDebug(TYPE_PAUSED_VC);

        if (advInfo == null) {
            logError(TYPE_PAUSED_VC + " 失败: advInfo 为空!!");
            return;
        }

        disposeVC(TYPE_PAUSED_VC, advInfo);
    }

    /**
     * 开始播放发送SUS *
     */
    public static void disposeSUS(Context context, VideoUrlInfo videoInfo) {
        logDebug(TYPE_SUS);
        disposeStat(context, videoInfo, TYPE_SUS);
    }

    /**
     * 开始播放发送SUS *
     */
    public static void disposeSUS(Context context, AdvInfo advInfo) {
        logDebug(TYPE_SUS);
        disposeStat(context, advInfo, TYPE_SUS);
    }

    /**
     * 开始播放使用AdSDK发送SUS*
     */
    public static void disposeOfflineSUS(Context context, VideoUrlInfo videoInfo, XAdInstance instance) {
        disposeOfflineStat(TYPE_SUS, instance);
        disposeStat(context, videoInfo, TYPE_SUS);
    }

    /**
     * 开始播放使用AdSDK发送SUS*
     */
    public static void disposeOfflineSUS(Context context, AdvInfo advInfo, XAdInstance instance) {
        disposeOfflineStat(TYPE_SUS, instance);
        disposeStat(context, advInfo, TYPE_SUS);
    }

    /**
     * 互动广告显示发送SHU *
     */
    public static void disposeSHU(Context context, AdvInfo advInfo) {
        logDebug(TYPE_SHU);
        disposeStat(context, advInfo, TYPE_SHU);
    }

    /**
     * 播放过程中发送SU *
     */
    public static void disposeSU(Context context, VideoUrlInfo videoInfo, int position) {
        disposeStat(context, videoInfo, TYPE_SU, position);
    }

    /**
     * 播放过程中发送SU *
     */
    public static void disposeSU(Context context, AdvInfo advInfo, int position) {
        disposeStat(context, advInfo, TYPE_SU, position);
    }

    /**
     * 播放过程中使用AdSDK发送SU *
     */
    public static void disposeOfflineSU(Context context, VideoUrlInfo videoInfo, int position, XAdInstance instance) {
        disposeOfflineStat(TYPE_SU, position, instance);
        disposeStat(context, videoInfo, TYPE_SU, position);
    }

    /**
     * 播放过程中使用AdSDK发送SU *
     */
    public static void disposeOfflineSU(Context context, AdvInfo advInfo, int position, XAdInstance instance) {
        disposeOfflineStat(TYPE_SU, position, instance);
        disposeStat(context, advInfo, TYPE_SU, position);
    }

    /**
     * 结束播放发送SUE *
     */
    public static void disposeSUE(Context context, VideoUrlInfo videoInfo) {
        logDebug(TYPE_SUE);
        disposeStat(context, videoInfo, TYPE_SUE);
    }

    /**
     * 结束播放发送SUE *
     */
    public static void disposeSUE(Context context, AdvInfo advInfo) {
        logDebug(TYPE_SUE);
        disposeStat(context, advInfo, TYPE_SUE);
    }

    /**
     * 结束播放使用AdSDK发送SUE *
     */
    public static void disposeOfflineSUE(Context context, VideoUrlInfo videoInfo, XAdInstance instance) {
        disposeOfflineStat(TYPE_SUE, instance);
        disposeStat(context, videoInfo, TYPE_SUE);
    }

    /**
     * 结束播放使用AdSDK发送SUE *
     */
    public static void disposeOfflineSUE(Context context, AdvInfo advInfo, XAdInstance instance) {
        disposeOfflineStat(TYPE_SUE, instance);
        disposeStat(context, advInfo, TYPE_SUE);
    }

    /**
     * 暂停广告开始，发送SUS *
     */
    public static void disposePausedSUS(Context context, AdvInfo advInfo) {
        logDebug(TYPE_PAUSED_SUS);
        disposeStat(context, advInfo, TYPE_PAUSED_SUS);
    }

    /**
     * 离线暂停广告开始，发送SUS *
     */
    public static void disposeOfflinePausedSUS(Context context, AdvInfo advInfo, XAdInstance instance) {
        disposeOfflineStat(TYPE_PAUSED_SUS, instance);
        disposeStat(context, advInfo, TYPE_PAUSED_SUS);
    }

    /**
     * 暂停广告结束，发送SUE *
     */
    public static void disposePausedSUE(Context context, AdvInfo advInfo) {
        logDebug(TYPE_PAUSED_SUE);
        disposeStat(context, advInfo, TYPE_PAUSED_SUE);
    }

    /**
     * 点击广告CUM *
     */
    public static void disposeCUM(Context context, AdvInfo advInfo) {
        logDebug(TYPE_CUM);
        disposeStat(context, advInfo, TYPE_CUM);
    }

    private static void disposeStat(Context context, VideoUrlInfo videoInfo, String type) {
        disposeStat(context, videoInfo, type, -1);
    }

    private static void disposeStat(Context context, AdvInfo advInfo, String type) {
        disposeStat(context, advInfo, type, -1);
    }

    private static void disposeStat(Context context, VideoUrlInfo videoInfo, String type,
                                    int position) {
        AdvInfo advInfo = getAdvInfo(videoInfo);
        disposeStat(context, advInfo, type, position);
    }

    private static void disposeOfflineStat(String type, XAdInstance instance) {
        disposeOfflineStat(type, -1, instance);
    }

    private static void disposeVC(String type, AdvInfo advInfo) {

        if (advInfo == null) {
            logError(type + " 失败: advInfo 为空!!");
            return;
        }

        if (!isVC_Valid(type, advInfo)) {
            return;
        }

        disposeHttp(advInfo.VC, type);

        advInfo.VT = "";
        advInfo.VC = "";
    }

    /**
     * 用于非离线广告监测
     */
    private static void disposeStat(Context context, AdvInfo advInfo, String type, int position) {
        if (advInfo == null) {
            logError(type + " 失败: advInfo非法 !!!!");
            return;
        }

        List<Stat> statList = getStatListByType(advInfo, type);
        if (statList == null || statList.size() == 0) {
            //此log太频繁，不打印
//			String why = statList == null ? "为空" : "size为 0 ";
//			logDebug(type + " 失败: advInfo." + type + "  " + why);

            return;
        }

        disposeStat(context, advInfo, statList, type, position);
    }

    private static void disposeStat(Context context, AdvInfo advInfo, List<Stat> statlist,
                                    String type, int position) {

        // 曝光过的统计，我们记录下来,发送完清除无效统计的list。
        List<Stat> diposedlist = new ArrayList<Stat>(5);

        int itemIndex = 0;

        for (Stat stat : statlist) {

            itemIndex++;

            if (stat == null) {
                logError(type + " 失败: list中 stat 对象为空 !!");
                continue;
            }

            if (TextUtils.isEmpty(stat.U)) {
                logError(type + " 失败: stat 对象的 url为空 !!");
                continue;
            }

            if (stat.SDK == SEND_BY_ADSDK) {
                logError(type + " 失败: stat 对象为离线广告检测 !!");
                continue; //离线广告检测由AdSDK发送
            }

            if (position == -1 || (!TextUtils.isEmpty(stat.T) && position == Integer.valueOf(stat.T))) {

                String detail = type + "  第" + itemIndex + "项 (共"
                        + statlist.size() + "项) ";

                //用sdk发送
                if (stat.SDK == SEND_BY_MMA) {
                    if (type.equals(TYPE_CUM)) {
                        Logger.d(SDK_TAG, "sdk发送点击cum:" + stat.U);
                        Countly.sharedInstance().onClick(stat.U);
                    } else if (type.equals(TYPE_SU)) {
                        Logger.d(SDK_TAG, "sdk发送su:" + stat.U);
                        Countly.sharedInstance().onExpose(stat.U);
                    } else if (type.equals(TYPE_SUS)) {
                        Logger.d(SDK_TAG, "sdk发送sus:" + stat.U);
                        Countly.sharedInstance().onExpose(stat.U);
                    } else if (type.equals(TYPE_SUE)) {
                        Logger.d(SDK_TAG, "sdk发送sue:" + stat.U);
                        Countly.sharedInstance().onExpose(stat.U);
                    } else if (type.equals(TYPE_PAUSED_SUS)) {
                        Logger.d(SDK_TAG, "sdk发送sus:" + stat.U);
                        Countly.sharedInstance().onExpose(stat.U);
                    } else if (type.equals(TYPE_PAUSED_SUE)) {
                        Logger.d(SDK_TAG, "sdk发送sue:" + stat.U);
                        Countly.sharedInstance().onExpose(stat.U);
                    } else if (type.equals(TYPE_SHU)) {
                        Logger.d(SDK_TAG, "sdk发送shu:" + stat.U);
                        Countly.sharedInstance().onExpose(stat.U);
                    }
                } else if (stat.SDK == SEND_BY_ALIMM) {
//                    if (type.equals(TYPE_CUM)) {
//                        String loadingTime = String.valueOf(System
//                                .currentTimeMillis() / 1000);
//                        String dur = String.valueOf(advInfo.AL);
//                        String playingTime = "5";
//                        ArrayList<String> urls = new ArrayList<String>();
//                        urls.add(stat.U);
//                        MediaFile file = new MediaFile("", "", "1",
//                                MediaFile.STATUS_CLICK, dur, loadingTime,
//                                playingTime, urls);
//                        ArrayList<String> reportUrls = AdLogic
//                                .getPreRollStaticUrl(context, file);
//                        if (reportUrls != null && reportUrls.get(0) != null) {
//                            String url = reportUrls.get(0).replaceAll("\n", "");
//                            Logger.e(TAG, "alibaba发送 CUM :" + url);
//                            new DisposableHttpTask(TAG, url, detail).start();
//                        }
//                    } else if (type.equals(TYPE_SUS)) {
//                        String loadingTime = String.valueOf(System
//                                .currentTimeMillis() / 1000);
//                        String playingTime = "0";
//                        ArrayList<String> urls = new ArrayList<String>();
//                        urls.add(stat.U);
//                        String dur = String.valueOf(advInfo.AL);
//                        MediaFile file = new MediaFile("", "", "1",
//                                MediaFile.STATUS_START, dur, loadingTime,
//                                playingTime, urls);
//                        ArrayList<String> reportUrls = AdLogic
//                                .getPreRollStaticUrl(context, file);
//                        if (reportUrls != null && reportUrls.get(0) != null) {
//                            String url = reportUrls.get(0).replaceAll("\n", "");
//                            Logger.e(TAG, "alibaba发送 SUS :" + url);
//                            new DisposableHttpTask(TAG, url, detail).start();
//                        }
//                    } else if (type.equals(TYPE_SUE)) {
//                        String loadingTime = String.valueOf(System
//                                .currentTimeMillis() / 1000);
//                        ArrayList<String> urls = new ArrayList<String>();
//                        urls.add(stat.U);
//                        String dur = String.valueOf(advInfo.AL);
//                        MediaFile file = new MediaFile("", "", "1",
//                                MediaFile.STATUS_COMPLETE, dur, loadingTime,
//                                dur, urls);
//                        ArrayList<String> reportUrls = AdLogic
//                                .getPreRollStaticUrl(context, file);
//                        if (reportUrls != null && reportUrls.get(0) != null) {
//                            String url = reportUrls.get(0).replaceAll("\n", "");
//                            Logger.e(TAG, "alibaba发送 SUE :" + url);
//                            new DisposableHttpTask(TAG, url, detail).start();
//                        }
//                    }
                } else {
                    String url = stat.U;
                    if (url.contains("##TS##")) {
                        long time = System.currentTimeMillis();
                        url = url.replace("##TS##", String.valueOf(time));
                    }
                    disposeHttp(url, detail);
                }
                diposedlist.add(stat);
            }
        }

        // CUM发送的统计，非单次的，不能直接从容器中移除。
        if (type.equals(TYPE_CUM)) {
            return;
        }

        for (Stat stat : diposedlist) {
            statlist.remove(stat);
        }
        diposedlist.clear();
    }

    /**
     * Adsdk发送数据检测
     */
    private static void disposeOfflineStat(String type, int position, XAdInstance instance){
        if (instance != null) {
            if (type.equals(TYPE_SU)) {
                Logger.d(OfflineAdSDK.TAG, "AdSDK -----> disposeOfflineSU : " + position + "");
                XNativeAdManager.getInstance().onXAdSlotPlayingAtCuepoint(instance, position);
            } else if (type.equals(TYPE_SUS)) {
                Logger.d(OfflineAdSDK.TAG, "AdSDK -----> disposeOfflineSUS");
                XNativeAdManager.getInstance().onXAdSlotStart(instance);
            } else if (type.equals(TYPE_SUE)) {
                Logger.d(OfflineAdSDK.TAG, "AdSDK -----> disposeOfflineSUE");
                XNativeAdManager.getInstance().onXAdSlotEnd(instance);
            } else if (type.equals(TYPE_PAUSED_SUS)) {
                Logger.d(OfflineAdSDK.TAG, "AdSDK -----> disposeOfflinePauseSUS");
                XNativeAdManager.getInstance().onXAdSlotStart(instance);
            }
        }
    }

    private static List<Stat> getStatListByType(AdvInfo advInfo, String type) {
        if (type.equals(TYPE_SUS) || type.equals(TYPE_PAUSED_SUS)) {
            return advInfo.SUS;
        }

        if (type.equals(TYPE_SU)) {
            return advInfo.SU;
        }

        if (type.equals(TYPE_SUE) || type.equals(TYPE_PAUSED_SUE)) {
            return advInfo.SUE;
        }

        if (type.equals(TYPE_CUM)) {
            return advInfo.CUM;
        }

        if (type.equals(TYPE_SHU)) {
            return advInfo.SHU;
        }

        logError(type + " 失败: getStatListByType不支持此 " + type + "!!!");
        return null;
    }

    private static AdvInfo getAdvInfo(VideoUrlInfo videoInfo) {

        if (videoInfo == null) {
            logError("getVideoInfo 失败，mediaPlayerDelegate.videoInfo = null !");
            return null;
        }

        return videoInfo.getCurrentAdvInfo();
    }

    private static boolean isVC_Valid(String type, AdvInfo advInfo) {

        if (advInfo.VT == null || (!advInfo.VT.equals("1") && !"2".equals(advInfo.VT))) {
            //此log太频繁，不打印
//			logDebug(type + " 失败: advInfo.VT = " + advInfo.VT + " !!");
            return false;
        }

        if (advInfo.VC == null || TextUtils.getTrimmedLength(advInfo.VC) <= 0) {
            //此log太频繁，不打印
//			logError(type + " 失败: advInfo.VC = " + advInfo.VC + " !!");
            return false;
        }

        return true;
    }

    public static void logDebug(String s) {
//		Logger.d(TAG, FILENAME + " " + REQUEST_SUMARY + s);
    }

    public static void logError(String s) {
//		Logger.e(TAG, FILENAME + " " + REQUEST_SUMARY + s);
    }

    public static void disposeAdLoss(Context context, int step,
                                     String sessionId, String at) {
        try {
            String url = URLContainer.AD_LOSS_URL + "lvs="
                    + URLContainer.AD_LOSS_VERSION + "&step=" + step
                    + "&os=android" + "&bt="
                    + (UIUtils.isTablet(context) ? "pad" : "phone") + "&bd="
                    + URLContainer.getTextEncoder(android.os.Build.BRAND)
                    + "&avs=" + URLContainer.verName + "&sid=" + sessionId
                    + "&at=" + at;

            disposeHttp(url, null);
        } catch (Exception e) {
        }
    }

    public static void disposeNotPlayedAd(Context context, VideoUrlInfo videoInfo, int step) {
        if (videoInfo != null && !videoInfo.isAdvEmpty()) {
            List<AdvInfo> lossAdvInfo = new ArrayList<AdvInfo>();
            for (AdvInfo advInfo : videoInfo.videoAdvInfo.VAL) {
                if (!advInfo.played())
                    lossAdvInfo.add(advInfo);
            }
            if (!lossAdvInfo.isEmpty())
                disposeAdLossNew(context, step, SessionUnitil.playEvent_session, lossAdvInfo);
        }
    }

    public static void disposeAdLossNew(Context context, int step, String sessionId, List<AdvInfo> advInfoList) {
        String p = "";
        String ie = "";
        if (advInfoList != null) {
            for (AdvInfo advInfo : advInfoList) {
                if (advInfo.PST != 0)
                    p += "," + advInfo.PST;
                if (advInfo.IE != null && !advInfo.IE.isEmpty())
                    ie += "," + advInfo.IE;
            }
            if (!TextUtils.isEmpty(p))
                p = p.substring(1);
            if (!TextUtils.isEmpty(ie))
                ie = ie.substring(1);
        }
        try {
            String url = URLContainer.AD_LOSS_URL_NEW + "lvs="
                    + URLContainer.AD_LOSS_VERSION_NEW + "&sp=" + step + "&st=" + MediaPlayerConfiguration.getInstance().getAdvPlatform()
                    + "&bt=" + (UIUtils.isTablet(context) ? "0" : "1") + "&os=1"
                    + "&avs=" + URLContainer.verName + "&sid=" + sessionId
                    + "&p=" + p + "&ie=" + ie;
            disposeHttp(url, null);
        } catch (Exception e) {
        }
    }


    /**
     *  发送trueView广告SKIP中的IMP曝光
     * @param advInfo  当前广告
     * @param progress 当前广告播放时间（秒）
     */
    public static void disposeSkipIMP(AdvInfo advInfo,int progress){
        Logger.d(LogTag.TAG_TRUE_VIEW, "------> disposeSkipIMP()");
        disposeTrueViewStatIMP(advInfo,progress,TYPE_SKIP_IMP);
    }

    /**
     *  发送trueView广告SKIP中的IMP曝光
     * @param advInfo  当前广告
     * @param progress 当前广告播放时间（秒）
     */
    public static void disposeViewIMP(AdvInfo advInfo,int progress){
        Logger.d(LogTag.TAG_TRUE_VIEW, "------> disposeViewIMP()");
        disposeTrueViewStatIMP(advInfo,progress,TYPE_VIEW_IMP);
    }

    /**
     * 获取trueview广告中对应的曝光点
     * @param type TYPE_SKIP_IPM , TYPE_VIEW_IPM
     */
    private static List<Stat> getTrueViewStatListByType(AdvInfo advInfo, String type) {
        if(type.equals(TYPE_SKIP_IMP)){
            if(advInfo.EM.SKIP == null){
                return null;
            }
            return advInfo.EM.SKIP.IMP;
        }

        if (type.equals(TYPE_VIEW_IMP)){
            if (advInfo.EM.VIEW == null){
                return null;
            }
            return advInfo.EM.VIEW.IMP;
        }
        logError(type + " 失败: getTrueViewStatListByType不支持此 " + type + "!!!");
        return null;
    }


    /**
     * 用于发送trueview广告相关点击事件曝光
     * @param advInfo  当前广告
     * @param progress 当前广告播放时间（秒）
     */
    private static void disposeTrueViewStatIMP(AdvInfo advInfo, int progress, String type){
        if(advInfo == null || advInfo.EM == null || TextUtils.isEmpty(type)){
            Logger.d(LogTag.TAG_TRUE_VIEW,"disposeTrueViewStatIMP ----> advInfo.EM is null.");
            return;
        }
        List<Stat> statlist = getTrueViewStatListByType(advInfo, type);

        if(statlist == null || statlist.size() <= 0){
            Logger.d(LogTag.TAG_TRUE_VIEW,"disposeTrueViewStatIMP ----> statlist is null.");
            return;
        }
        Logger.d(LogTag.TAG_TRUE_VIEW,"disposeTrueViewStatIMP ----> progress : " + progress);
        int itemIndex = 0;
        // 曝光过的统计，我们记录下来,发送完清除无效统计的list。
        List<Stat> diposedlist = new ArrayList<Stat>(5);
        for (Stat stat : statlist) {
            itemIndex ++ ;
            if (stat == null) {
                continue;
            }

            if (TextUtils.isEmpty(stat.U)) {
                continue;
            }

            String detail = type + "  第" + itemIndex + "项 (共"
                    + statlist.size() + "项) ";

            if (stat.SDK == SEND_BY_MMA) {
                if (type.equals(TYPE_SKIP_IMP)) {
                    Logger.d(LogTag.TAG_TRUE_VIEW, "sdk发送skip.ipm:" + stat.U);
                    Countly.sharedInstance().onExpose(stat.U);
                } else if (type.equals(TYPE_VIEW_IMP)) {
                    Logger.d(LogTag.TAG_TRUE_VIEW, "sdk发送view.imp:" + stat.U);
                    Countly.sharedInstance().onExpose(stat.U);
                }
            } else {
                String url = stat.U;
                if (url.contains("##VE##")) {
                    url = url.replace("##VE##", String.valueOf(progress));
                }
                Logger.d(LogTag.TAG_TRUE_VIEW,"disposeTrueViewStatIMP ----> url : " + url);
                disposeHttp(url, detail);
                diposedlist.add(stat);
            }
        }

        diposedlist.clear();
    }

    private static void disposeHttp(final String url, final String requestSumary) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            new DisposableHttpTaskApache(TAG, url, requestSumary).start();
        } else {
            new DisposableHttpTask(TAG, url, requestSumary).start();
        }
    }
}
