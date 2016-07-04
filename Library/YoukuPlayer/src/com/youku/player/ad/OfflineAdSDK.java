package com.youku.player.ad;

import android.content.Context;

import com.alibaba.fastjson.JSONObject;
import com.baseproject.utils.Logger;
import com.youdo.XNativeAdManager;
import com.youdo.vo.XAdInstance;
import com.youku.player.goplay.GoplayException;
import com.youku.player.goplay.IGetOfflineAdvCallBack;
import com.youku.player.goplay.VideoAdvInfo;

import org.openad.events.IXYDEvent;
import org.openad.events.IXYDEventListener;

/**
 * 离线广告SDK 相关
 */
public class OfflineAdSDK {

    public static String TAG = "AdSDK";

    /**
     * 判断是否已经初始化TaeSDK
     */
    public static boolean isInitAdSDK = false;


    private static IXYDEventListener mIXYDEventListener = new IXYDEventListener() {
        @Override
        public void run(IXYDEvent arg0) {
            final String type = arg0.getType();
            Logger.d(TAG,"AdSDK -----> " + type);
            if (type.equals(XNativeAdManager.NATIVE_AD_SERVER_START_SUCCESS)) {
                isInitAdSDK = true;
            } else if (type.equals(XNativeAdManager.NATIVE_AD_SERVER_START_FAILED)) {
                isInitAdSDK = false;
            }
        }
    };


    /**
     * 初始化AdSDK（使用默认的IXYDEventListener）
     */
    public static void initAdSDK(Context context, int screenDensity, int screenWidth, int screenHeight,
                                 String outputFolder, String openUDID, String rst,  String appVersion, String pid, String site){

        initAdSDK(context, screenDensity, screenWidth, screenHeight, outputFolder, openUDID, rst, appVersion, pid, site, mIXYDEventListener);
    }

    /**
     * 初始化adSDK
     */
    public static void initAdSDK(Context context, int screenDensity, int screenWidth, int screenHeight,
                                 String outputFolder, String openUDID, String rst, String appVersion, String pid, String site,
                                 IXYDEventListener listener) {
        if (isInitAdSDK){
           Logger.d(TAG,"AdSDK -----> is inited adSDK!");
           return;
        }
        Logger.d(TAG, "XNativeAdManager.getInstance().getState() -----> " + XNativeAdManager.getInstance().getState());
        if (XNativeAdManager.getInstance().getState() == XNativeAdManager.XNativeAdManagerState.UNKNOWN) {
            XNativeAdManager.getInstance().onCreate();

            XNativeAdManager.getInstance().setContentPath(outputFolder);
            XNativeAdManager.getInstance().setApplicationContext(context);
            XNativeAdManager.getInstance().setScreenDensity(screenDensity);
            XNativeAdManager.getInstance().setScreenSize(screenWidth, screenHeight);

            XNativeAdManager.getInstance().setOpenUDID(openUDID);
            XNativeAdManager.getInstance().setRST(rst);
            XNativeAdManager.getInstance().setAppVersion(appVersion);
            XNativeAdManager.getInstance().setPID(pid);
            XNativeAdManager.getInstance().setSite(site);

            XNativeAdManager.getInstance().setPrerollMockAdServer("http://mf.atm.youku.com/mf");
            XNativeAdManager.getInstance().setPauserollMockAdServer("http://mp.atm.youku.com/mp");
            XNativeAdManager.getInstance().setDisplayMockAdServer("http://mi.atm.youku.com/mi");

            XNativeAdManager.getInstance().removeAllListeners();
            XNativeAdManager.getInstance().addEventListener(XNativeAdManager.NATIVE_AD_SERVER_START_SUCCESS, listener);
            XNativeAdManager.getInstance().addEventListener(XNativeAdManager.NATIVE_AD_SERVER_START_FAILED, listener);
            XNativeAdManager.getInstance().startNativeAdServer();
            Logger.d(TAG,"AdSDK -----> startNativeAdServer!");
        }else{
            isInitAdSDK = false;
        }

    }

    /**
     * 可以进行初始化操作
     */
    public static boolean canStartNativeAdServer(){
        return ( !isInitAdSDK && (XNativeAdManager.getInstance().getState() == XNativeAdManager.XNativeAdManagerState.UNKNOWN));
    }

    /**
     * 获取离线前贴广告
     */
    public static void getPrerollAd(IGetOfflineAdvCallBack callBack){
        Logger.d(TAG, "--------> OfflineAdSDK.getPrerollAd ");
        getAdInstance(callBack, AdPosition.PRE);
    }

    /**
     * 获取离线暂停广告
     */
    public static void getPauserollAd(IGetOfflineAdvCallBack callBack){
        Logger.d(TAG, "--------> OfflineAdSDK.getPauserollAd ");
        getAdInstance(callBack, AdPosition.PAUSE);
    }
    /**
     * 获取开机图广告
     */
    public static void getDisplayAd(IGetOfflineAdvCallBack callBack){
        Logger.d(TAG, "--------> OfflineAdSDK.getDisplayAd ");
        getAdInstance(callBack, -1);
    }

    /**
     * 获取离线广告
     * @param adPosition 广告分类（前贴：7，暂停：10，开机图：-1）
     */
    private static void getAdInstance (IGetOfflineAdvCallBack callBack, final int adPosition){
        try {
            Logger.d(TAG,"AdSDK -----> XNativeAdManager.getInstance().getState() : " + XNativeAdManager.getInstance().getState());
            XAdInstance instance = null;
            if(adPosition == AdPosition.PRE){
                instance = XNativeAdManager.getInstance().getPrerollAd();
            }else if(adPosition == AdPosition.PAUSE){
                instance = XNativeAdManager.getInstance().getPauserollAd();
            } else if(adPosition == -1){
                instance = XNativeAdManager.getInstance().getDisplayAd();
            }

            if (instance != null) {
                String json = instance.toLiveJSONObject().toString();
                Logger.d(TAG, "instance.toLiveJSONObject() -----> " + json);
                VideoAdvInfo advInfo = JSONObject.parseObject(json, VideoAdvInfo.class);
                if (advInfo != null) {
                    Logger.d(TAG, "getAdInstance -----> callBack.onSuccess() ");
                    callBack.onSuccess(advInfo, instance);
                    return;
                }
            }else{
                Logger.d(TAG, "instance.toLiveJSONObject() -----> null");
            }
            Logger.d(TAG, "OfflineAdSDK.getAdInstance -------->   callBack.onFailed");
            callBack.onFailed(new GoplayException());
        }catch (Exception e){
            Logger.d(TAG, "OfflineAdSDK.getAdInstance -------->   callBack.onFailed: " + e.getMessage());
            callBack.onFailed(new GoplayException());
        }
    }

}
