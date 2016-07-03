package com.youku.player.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.telephony.CellInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.baseproject.utils.Logger;
import com.youku.player.LogTag;
import com.youku.player.network.HttpIntent;
import com.youku.player.network.HttpRequestManager;
import com.youku.player.network.IHttpRequest;
import com.youku.player.network.YoukuService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by dell on 2015/1/6.
 */
public class FreeFlowUtil {
    private static final String TAG = LogTag.TAG_PREFIX + FreeFlowUtil.class.getSimpleName();

    private HttpRequestManager mHttpRequest;

    private static final int IP_NUMBER_NOT_MATCH = -1;//ip号段不合适
    private static final int CURRENT_AREA_NOT_OPEN = -2;//当前省份未开通
    private static final int CAN_ORDER = 1;//可以订购

    // 更新Cookie值。。。
    public void getUnicomFreeFlowState(final CallBackFreeFlow mCallBackFreeFlow) {
        mHttpRequest = (HttpRequestManager) YoukuService.getService(IHttpRequest.class, true);
        mHttpRequest.request(new HttpIntent(URLContainer.getUnicomFreeFlowUrl(), true),
                new IHttpRequest.IHttpRequestCallBack() {

                    @Override
                    public void onSuccess(HttpRequestManager request) {
                        String result = request.getDataString();
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                JSONObject josnJsonObject = new JSONObject(result);
                                if (josnJsonObject.has("status") && !TextUtils.isEmpty(josnJsonObject.optString("status"))
                                        && "success".equals(josnJsonObject.optString("status"))) {
                                    if (josnJsonObject.has("code") && CAN_ORDER == josnJsonObject.optInt("code")) {
                                        mCallBackFreeFlow.sucessGetFreeFlow();
                                    } else {
                                        mCallBackFreeFlow.failGetFreeFlow(josnJsonObject.opt("desc"));
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        Logger.d(TAG, "======request====成功====request==" + request.getDataString());
                    }

                    @Override
                    public void onFailed(String failReason) {
                        mCallBackFreeFlow.failGetFreeFlow(failReason);
                        Logger.d(TAG, "=======request====失败==failReason====" + failReason);
                    }
                });

    }


    //获取设备的手机号码
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public String getMobileDeviceNumber(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String NativePhoneNumber = null;
        NativePhoneNumber = telephonyManager.getLine1Number();
        Logger.d(TAG, "====本机号码====" + NativePhoneNumber);
        Logger.d(TAG, "====本机号码=sim===" + telephonyManager.getSimSerialNumber());
        List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();
        for (int i = 0; i < cellInfos.size(); i++) {
            Logger.d(TAG, "====本机号码==cellInfos==" + cellInfos.get(i));

        }
        return NativePhoneNumber;
    }

    //回调接口
    public interface CallBackFreeFlow {

        public void sucessGetFreeFlow();

        public void failGetFreeFlow(Object failSeason);
    }


}