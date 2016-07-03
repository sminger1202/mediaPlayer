package com.youku.player.service;

import com.baseproject.image.Utils;
import com.baseproject.utils.Logger;
import com.baseproject.utils.Profile;
import com.baseproject.utils.Util;
import com.youku.player.LogTag;
import com.youku.player.util.DisposableStatsUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

/**
 * 使用apache做基本的http请求.
 */
public class DisposableHttpTaskApache extends Thread {

    private String url;
    private String tag;
    private String requestSumary;
    private String requestMethod;

    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final int TIME_OUT = 5000;

    public DisposableHttpTaskApache(String url) {
        super("DisposableHttpTaskApache");
        this.url = url;
    }

    /**
     * 使用tag打印更多的内容
     */
    public DisposableHttpTaskApache(String tag, String url, String requestSumary) {
        this(url);
        this.tag = tag;
        this.requestSumary = requestSumary;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    /*
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        super.run();

        boolean isSuccess = false;
        String resultDetail = "unknown";
        Logger.d(LogTag.TAG_PLAYER, "Disposable:" + url);

        //TODO
        if (Util.hasInternet()) {
            Utils.disableConnectionReuseIfNecessary();
            try {
                HttpParams httpParams = new BasicHttpParams();
                // 建立链接的时间
                HttpConnectionParams.setConnectionTimeout(httpParams, TIME_OUT);
                // 等数据的时间
                HttpConnectionParams.setSoTimeout(httpParams, TIME_OUT);
                DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);

                //HttpGet get = new HttpGet(url);
                HttpUriRequest request = getRequest(url);
                request.setHeader("User-Agent", Profile.User_Agent);
                HttpResponse httpResponse = httpClient.execute(request);
                int response = httpResponse.getStatusLine().getStatusCode();

                isSuccess = (response == 200) ? true : false;
                resultDetail = "" + response;

                Logger.e(LogTag.TAG_PLAYER, "Disposable result url:" + url + "\t:" + String.valueOf(response));
            } catch (Exception e) {
                resultDetail = "got Exception e : " + e.getMessage();
                Logger.e(LogTag.TAG_PLAYER, e);
            } finally {
            }

        }

        if (requestSumary != null) {
            String result = requestSumary
                    + (isSuccess ? " 成功" : " 失败") + " !  resultCode = "
                    + resultDetail + " 其请求url = " + url;
            if (isSuccess) {
                DisposableStatsUtils.logDebug(result);
                return;
            }
            DisposableStatsUtils.logError(result);
        }
    }

    private HttpUriRequest getRequest(final String url) {
        if (requestMethod != null && requestMethod.equalsIgnoreCase(METHOD_POST)) {
            return new HttpPost(url);
        } else {
            return new HttpGet(url);
        }
    }
}
