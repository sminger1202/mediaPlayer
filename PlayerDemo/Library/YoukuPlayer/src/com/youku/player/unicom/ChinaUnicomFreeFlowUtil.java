package com.youku.player.unicom;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.baseproject.utils.Logger;
import com.baseproject.utils.NetworkType;
import com.baseproject.utils.Util;
import com.unicom.iap.sdk.WoVideoSDK;
import com.youku.player.LogTag;
import com.youku.player.config.MediaPlayerConfiguration;
import com.youku.player.plugin.MediaPlayerDelegate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by malijie on 2014/12/16.
 * 联通3G免流量工具类
 */
public class ChinaUnicomFreeFlowUtil {
    public static final String TAG = LogTag.TAG_WO_VIDEO;
    //判断是否为联通包月用户
    public static boolean isChinaUnicomSubscribed = false;
    //记录地址是否转换成功
    public static boolean isTransformUrlSuccess = false;
    //记录提示对话框的状态
    public static boolean isAlertDialogShown = false;
    //是否第一次显示，用于防止用户多次点击
    public static boolean isFirstShow = true;

    //JSON解析WoVideo
    private static ChinaUnicomVideoInfo parseChinaUnicomVideoInfo(String jsonWoVideo){
        ChinaUnicomVideoInfo mWoVideo = null;
        if(jsonWoVideo != null && !jsonWoVideo.trim().equals("")){
            String jWoVideo = parseJSONString(jsonWoVideo);
            try{
                JSONArray jArray = new JSONArray(jWoVideo);
                JSONObject jObject = jArray.getJSONObject(0);
                mWoVideo = new ChinaUnicomVideoInfo();
                mWoVideo.setStatus(jObject.getInt("status"));
                mWoVideo.setVedioId(jObject.getString("videoid"));
            }
            catch(JSONException e){
                e.printStackTrace();
            }
        }
        return mWoVideo;
    }


    /**
     * 初始化联通3G免流量播放
     */
    public static void initChinaUnicomSDK(Context context) {
        Log.d(TAG, " =====开始执行联通初始化=====");
        if (!Util.isWifi() && Util.hasInternet() && ChinaUnicomFreeFlowUtil.isChinaUnicom3G4GCard()) {
            String strWoVideoInfo = WoVideoSDK.getMonthOrderInfo(context);
            Logger.d(TAG, "WoVideoInfo = " + strWoVideoInfo);
            if (strWoVideoInfo != null) {
                ChinaUnicomVideoInfo mWoVideo = parseChinaUnicomVideoInfo(strWoVideoInfo);
                if (mWoVideo != null) {
                    //订购状态是否为订购或当月退订用户
                    if (mWoVideo.getStatus() == ChinaUnicomConstant.FREEFLOW_ORDER_STAUS_SUBSCRIBED ||
                            mWoVideo.getStatus() == ChinaUnicomConstant.FREEFLOW_ORDER_STAUS_UNSBSCRIBED) {
                        ChinaUnicomFreeFlowUtil.isChinaUnicomSubscribed = true;
                    }
                } else {
                    ChinaUnicomFreeFlowUtil.isChinaUnicomSubscribed = false;
                }
            }
            Logger.d(TAG, " =====联通初始化结束,订购信息为=====" + ChinaUnicomFreeFlowUtil.isChinaUnicomSubscribed);
        }
    }
    /**
     * 将服务器返回的字符串加工为JSON数据格式
     * @param str
     * @return
     */
    private static String parseJSONString(String str){
        String jsonStr = null;
        if(str != null && !str.trim().equals("")){
            jsonStr = str.replaceAll("\"","\\\"");
        }
        return jsonStr;
    }

    /**
     *判断是否为联通3G或4G卡
     */
    public static boolean isChinaUnicom3G4GCard(){
        int netType = Util.getNetworkType();
        if(netType == NetworkType.LTE || netType == NetworkType.UMTS
                || netType == NetworkType.HSDPA ||  netType == NetworkType.HSPAPlus
                || netType == NetworkType.HSPA || netType == NetworkType.UNKNOWN){
            //NetworkType.UNKNOWN 用于联通卡插入双卡双待手机
            return true;
        }
        return false;
    }

    /**
     * 是否为联通3g4g网络
     * @return
     */
    public static boolean isChinaUnicom3GNet(){
        if(isChinaUnicom3G4GCard() && !Util.isWifi() && Util.hasInternet()){
            return true;
        }
        return false;
    }

    /**
     * 判断网络是否满足联通3G/4G免流量播放条件
     * 1. 联通3G/4G网络
     * 2. 已订购
     * @return
     */
    public static boolean isSatisfyChinaUnicomFreeFlow() {
        if (MediaPlayerConfiguration.getInstance().unicomFree() && isChinaUnicom3GNet() && isChinaUnicomSubscribed) {
            return true;
        }
        return false;
    }

    public static final String CHINA_MOBILE = "mobile"; //中国移动
    public static final String CHINA_UNCIOM = "unicom"; //中国联通
    public static final String CHINA_TELETCOM = "telecom"; //中国电信
    /**
     * 判断运营商
     * @param context
     * @return
     */
    public static String getOperatorType(Context context) {
        if (null != context) {
            TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String operator = telManager.getSimOperator();
            if (operator != null) {
                if (operator.equals("46000") || operator.equals("46002")) {
                    return CHINA_MOBILE;
                } else if (operator.equals("46001")) {
                    return CHINA_UNCIOM;
                } else if (operator.equals("46003")) {
                    return CHINA_TELETCOM;
                }
            }
        }
        return "";
    }


    /**是否为联通3GWAP 网络*/
    public static boolean checkChinaUnicom3GWapNet(Context context){
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivity.getActiveNetworkInfo();
        if(activeNetInfo != null){
            if(activeNetInfo.getExtraInfo() != null && activeNetInfo.getExtraInfo().equals("3gwap")){
                return true;
            }
        }
        return false;
    }

    public static void checkChinaUnicomStatus(Activity mActivity, MediaPlayerDelegate mMediaPlayerDelegate){
        if(ChinaUnicomFreeFlowUtil.isAlertDialogShown == false){
            ChinaUnicomManager.checkChinaUnicomStatus(mActivity, mMediaPlayerDelegate,
                    mMediaPlayerDelegate.videoInfo);
        }
        //恢复初始化状态
        if(ChinaUnicomFreeFlowUtil.isAlertDialogShown == true){
            ChinaUnicomFreeFlowUtil.isAlertDialogShown = false;
        }
    }

    /**
     * 显示联通地址转换失败对话框
     */
    public static void showChinaUnicomTransformFailedDialog(final Activity activity,final MediaPlayerDelegate mediaPlayerDelegate){
        if(activity != null && mediaPlayerDelegate != null){
            if(isFirstShow){//防止多次点击
                isFirstShow = false;
                final ChinaUnicomAlertDialog unicomAlertDialog = new ChinaUnicomAlertDialog(activity,
                        ChinaUnicomConstant.NORMAL_TRANSFORM_FREEFLOW_FAILED);
                unicomAlertDialog.setUnicomPositiveBtnText(ChinaUnicomConstant.BUTTON_CONTINE_WATCH);
                unicomAlertDialog.setUnicomNegativeBtnText(ChinaUnicomConstant.BUTTON_CANCEL_WATCH);
                unicomAlertDialog.setUnicomPositiveBtnListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isAlertDialogShown = true;
                        isFirstShow = true;
                        unicomAlertDialog.dismiss();
                        mediaPlayerDelegate.start();

                    }
                });

                //取消观看，返回到主界面
                unicomAlertDialog.setUnicomNegativeBtnListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isAlertDialogShown = false;
                        isFirstShow = true;
                        unicomAlertDialog.dismiss();
                        activity.finish();
                    }
                });

                unicomAlertDialog.show();
            }
        }
    }

    /**
     * 显示联通3GWAP对话框
     * @param activity
     */
    public static void showChinaUnicom3GWAPDialog(final Activity activity,final MediaPlayerDelegate mediaPlayerDelegate){
        if(activity != null && mediaPlayerDelegate != null){
            if(isFirstShow == true){//防止用户多次点击，出现多个对话框
                isFirstShow = false;
                final ChinaUnicomAlertDialog alertDialog = new ChinaUnicomAlertDialog(activity,
                        ChinaUnicomConstant.WAP_TANSFORM_TFREEFLOW_FAILED);
                alertDialog.setUnicomPositiveBtnText(ChinaUnicomConstant.BUTTON_CONTINE_WATCH);
                alertDialog.setUnicomNegativeBtnText(ChinaUnicomConstant.BUTTON_CANCEL_WATCH);
                //取消观看
                alertDialog.setUnicomNegativeBtnListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isAlertDialogShown = false;
                        isFirstShow = true;
                        alertDialog.dismiss();
                        activity.finish();
                    }
                });

                alertDialog.setUnicomPositiveBtnListener(new View.OnClickListener() {
                    //继续观看
                    @Override
                    public void onClick(View v) {
                        Logger.d(TAG,"WAP Dialog, user choose to continue playing");
                        isFirstShow = true;
                        isAlertDialogShown = true;
                        alertDialog.dismiss();
                        mediaPlayerDelegate.start();

                        //3GWAP网络切换3GNET时处理逻辑
                        if(!ChinaUnicomFreeFlowUtil.checkChinaUnicom3GWapNet(activity)){
                            try {
                                Thread.sleep(1000);//获取地址是否转换成功有延迟
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            if(!Util.isWifi() && Util.hasInternet()){
                                if(ChinaUnicomFreeFlowUtil.isTransformUrlSuccess){
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(activity.getApplicationContext(), ChinaUnicomConstant.NORMAL_TRANSFORM_FREEFLOW_SUCCESS,
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }else{
                                    //转换为Net网络接入点时，地址转换失败
                                    Logger.d(TAG,"china unicom transform failed, and show dialog");
                                    if(mediaPlayerDelegate != null){
                                        mediaPlayerDelegate.release();
                                    }
                                    showChinaUnicomTransformFailedDialog(activity,mediaPlayerDelegate);
                                }
                            }

                        }

                    }
                });
                alertDialog.show();
            }
        }
    }
}
