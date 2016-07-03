package com.youku.player.grey;

import android.content.Context;


import com.baseproject.utils.Logger;
import com.youku.player.LogTag;
import com.youku.player.config.MediaPlayerConfiguration;
import com.youku.player.network.HttpIntent;
import com.youku.player.network.HttpRequestManager;
import com.youku.player.network.IHttpRequest;
import com.youku.player.network.YoukuService;
import com.youku.player.util.URLContainer;


/**
 * Created by liangji on 15/8/24.
 * 灰度发布配置
 */
public class GreyConfig {
    private static final String TAG = LogTag.TAG_GREY;

    private static GreyConfig instance;
    private IHttpRequest request = null;
    private boolean isRunning = false;

    private GreyConfig() {
    }

    public static synchronized GreyConfig getInstance() {
        if (instance == null) instance = new GreyConfig();
        return instance;
    }


    public void doRequestData(String pid,String ver) {
        if (!isRunning) {
            requestData(pid,ver);
        }
    }

    public void stopRequest() {
        if (request != null) {
            request.cancel();
            request = null;
        }
        isRunning = false;
    }

    public void destroy() {
        isRunning = false;
        request = null;
        instance = null;
    }

    private void requestData(String pid,String YoukuVer) {
        isRunning = true;
        request = YoukuService.getService(IHttpRequest.class, true);
        String url= URLContainer.getGreyInitURL(pid,YoukuVer,
                MediaPlayerConfiguration.getInstance().getVersionCode());
        Logger.d(TAG, url);

        HttpIntent httpIntent = new HttpIntent(url, false);
        request.request(httpIntent, new IHttpRequest.IHttpRequestCallBack() {
            @Override
            public void onSuccess(HttpRequestManager httpRequestManager) {

                try {
                    Logger.d(TAG,request.getDataString());
                    String jsonStr=request.getDataString();
                    if (jsonStr!=null)
                        MediaPlayerConfiguration.getInstance().setGreyConfiguration(jsonStr);
                } catch (Exception e) {
                    e.printStackTrace();
                    Logger.e(TAG,e.toString());
                }
                destroy();
            }


            @Override
            public void onFailed(String failReason) {
                Logger.d(TAG,"get data fail "+failReason);
                destroy();
            }


        });
    }

}
