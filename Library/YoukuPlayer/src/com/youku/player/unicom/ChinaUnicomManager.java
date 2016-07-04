package com.youku.player.unicom;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.baseproject.utils.Logger;
import com.baseproject.utils.Profile;
import com.unicom.iap.sdk.IWoVideoSDKCallBack;
import com.unicom.iap.sdk.WoVideoSDK;
import com.youku.analytics.data.Device;
import com.youku.player.LogTag;
import com.youku.player.config.MediaPlayerConfiguration;
import com.youku.player.goplay.ItemSeg;
import com.youku.player.goplay.VideoAdvInfo;
import com.youku.player.module.VideoUrlInfo;
import com.youku.player.plugin.MediaPlayerDelegate;
import com.youku.player.util.MainThreadExecutor;
import com.youku.player.util.PlayerUtil;
import com.youku.service.acc.AcceleraterManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 联通3G免流量管理类
 */
public class ChinaUnicomManager {
    private static final int MAX_WORKING_THREAD = 8;
    private static final int KEEP_ALIVE = 30;
    /**
     * 使用无界队列保证所有提交任务都执行，通过{@link ThreadPoolExecutor#allowCoreThreadTimeOut}设置核心线程空闲存活时间
     */
    private static ThreadPoolExecutor service = new ThreadPoolExecutor(MAX_WORKING_THREAD, MAX_WORKING_THREAD, KEEP_ALIVE, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    private static ChinaUnicomManager instance;
    private AcceleraterManager acceleraterManager;
    private static Object object = new Object();
    private static final String TAG = "ChinaUnicomManager";

    public synchronized static ChinaUnicomManager getInstance() {

        initChinaUnicomSDK();

        if (instance == null) {
            synchronized (object) {
                instance = new ChinaUnicomManager(Profile.mContext);
                service.allowCoreThreadTimeOut(true);
            }
        }
        return instance;
    }

    public static void initChinaUnicomSDK() {

        //已订购的用户则不需要再次鉴权

        if (MediaPlayerConfiguration.getInstance().unicomFree() && ChinaUnicomFreeFlowUtil.isChinaUnicom3GNet() && !ChinaUnicomFreeFlowUtil.isChinaUnicomSubscribed) {
            ChinaUnicomFreeFlowUtil.initChinaUnicomSDK(Profile.mContext);
        }
    }

    private ChinaUnicomManager(Context context) {
        acceleraterManager = AcceleraterManager.getInstance(context);
        acceleraterManager.bindService();
    }

    public void getWoVideoUrls(final String videoId, final List<ItemSeg> vSeg,
                               final Map<String, String> woVideoUrls, final String token,
                               final String oip, final String sid) {
        if (vSeg == null || vSeg.isEmpty())
            return;
        final CountDownLatch countDownLatch = new CountDownLatch(vSeg.size());
        int size = vSeg.size();
        for (int i = 0; i < size; i++) {
            final int index = i;
            service.execute(new Runnable() {
                @Override
                public void run() {
                    getWoVideoUrl(videoId, countDownLatch, vSeg.get(index), woVideoUrls,
                            token, oip, sid);
                }
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void getWoVideoUrl(final String videoId, final CountDownLatch countDownLatch,
                               final ItemSeg item, final Map<String, String> woVideoUrls,
                               final String token, final String oip, final String sid) {
        String WoVideoUrl = woVideoUrls.get(item.get_Url());
        if (TextUtils.isEmpty(WoVideoUrl)) {
            String encryptUrl = MediaPlayerConfiguration.getInstance().mPlantformController
                    .getEncreptUrl(item.get_Url(), item.getFieldId(), token,
                            oip, sid, MediaPlayerDelegate.is, Device.gdid);
            Logger.d(LogTag.TAG_WO_VIDEO, "encryptUrl:" + encryptUrl);

            Map<String, String> woVideoMap = getChinaUnicomVideoInfo(encryptUrl, videoId);

            WoVideoSDK.identifyWoVideoSDK(Profile.mContext, woVideoMap,
                    new IWoVideoSDKCallBack() {

                        @Override
                        public void sdkCallback(boolean isSuccess, String resultCode,
                                                String resultMessage, Object object, int intefaceType) {
                            Logger.d(LogTag.TAG_WO_VIDEO, "identifyWoVideoSDK:" + isSuccess + " "
                                    + resultCode + " " + resultMessage);
                            String url = (String) object;
                            ChinaUnicomFreeFlowUtil.isTransformUrlSuccess = isSuccess;

                            if (!TextUtils.isEmpty(url)) {
                                woVideoUrls.put(item.get_Url(), url);
                                Logger.d(LogTag.TAG_PLAYER, "getWoVideo url-->" + url);
                            } else
                                Logger.e(LogTag.TAG_PLAYER, "getWoVideo url failed");
                            countDownLatch.countDown();
                        }
                    });
        } else {
            countDownLatch.countDown();
        }
    }

    public static Map<String, String> getChinaUnicomVideoInfo(String url, String videoId) {
        Map<String, String> woVideoMap = new HashMap<String, String>();
        woVideoMap.put("videoid", videoId);
        woVideoMap.put("videourl", url);
        woVideoMap.put("tag", "1");
        woVideoMap.put("phoneversion", Device.os_ver);
        woVideoMap.put("phonetype", "android");
        woVideoMap.put("modelnumber", Device.btype);
        return woVideoMap;
    }

    /**
     * 检查当前联通网络，地址转换等情况
     *
     * @param activity
     * @param mediaPlayerDelegate
     * @param videoInfo
     */
    public static void checkChinaUnicomStatus(final Activity activity, final MediaPlayerDelegate mediaPlayerDelegate,
                                              final VideoUrlInfo videoInfo) {
        //联通运营商
        if (ChinaUnicomFreeFlowUtil.getOperatorType(activity).equals(ChinaUnicomFreeFlowUtil.CHINA_UNCIOM)) {
            // 1. 满足免流播放条件
            // 2. 非视频本地
            if (ChinaUnicomFreeFlowUtil.isSatisfyChinaUnicomFreeFlow()
                    && !PlayerUtil.isFromLocal(videoInfo) && !videoInfo.isCached()) {

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Logger.d(TAG, "check china unicom status");

                        //直播视频不免流
                        if (videoInfo.mLiveInfo != null) {
                            if (videoInfo.mLiveInfo.status == 1) {
                                Logger.d(TAG, "live is not free flow");
                                return;
                            }
                        }

                        // drm视频不免流
                        if (videoInfo.isDRMVideo()) {
                            return;
                        }

                        //3GWAP网络不免流
                        if (ChinaUnicomFreeFlowUtil.checkChinaUnicom3GWapNet(activity)) {
                            Logger.d(TAG, "3G WAP is not free flow, turn on a alert dialog");
                            ChinaUnicomFreeFlowUtil.showChinaUnicom3GWAPDialog(activity, mediaPlayerDelegate);
                            mediaPlayerDelegate.release();
                            return;
                        }

                        //联通地址转换失败不免流
                        if (!ChinaUnicomFreeFlowUtil.isTransformUrlSuccess) {
                            Logger.d(TAG, "transform free flow failed, turn on a alert dialog");
                            ChinaUnicomFreeFlowUtil.showChinaUnicomTransformFailedDialog(activity, mediaPlayerDelegate);
                            mediaPlayerDelegate.release();
                            return;
                        }

                        //地址转换成功，显示联通免流Toast提示
                        if (activity == null || videoInfo == null) {
                            return;
                        }

                        //是联通3G网络且非本地
                        //1. 地址转换成功 2.非3GWAP网络
                        if (ChinaUnicomFreeFlowUtil.isTransformUrlSuccess && !ChinaUnicomFreeFlowUtil.checkChinaUnicom3GWapNet(activity)) {
                            Toast.makeText(activity.getApplicationContext(), ChinaUnicomConstant.NORMAL_TRANSFORM_FREEFLOW_SUCCESS,
                                    Toast.LENGTH_LONG).show();
                        } else {
                            // Toast.makeText(context.getApplicationContext(), "当前使用2G/3G/4G播放，消耗套餐流量", Toast.LENGTH_LONG).show();
                            //不免流弹出提示对话框
                            ChinaUnicomFreeFlowUtil.showChinaUnicomTransformFailedDialog(activity, mediaPlayerDelegate);
                            mediaPlayerDelegate.release();
                            return;
                        }
                    }
                });

            }
        }


    }


    public void replaceAdvUrl(final VideoAdvInfo videoAdvInfo, final Runnable runnable) {
        Logger.d(LogTag.TAG_WO_VIDEO, "联通3G前贴广告开始");
        int size = videoAdvInfo.VAL.size();
        final CountDownLatch countDownLatch = new CountDownLatch(size);
        for (int i = 0; i < size; i++) {
            final Map<String, String> woVideoAdMap = ChinaUnicomManager.getChinaUnicomVideoInfo(videoAdvInfo.VAL.get(i).RS, "0");
            final int index = i;
            service.execute(new Runnable() {
                @Override
                public void run() {
                    WoVideoSDK.identifyWoVideoSDK(com.baseproject.utils.Profile.mContext, woVideoAdMap,
                            new IWoVideoSDKCallBack() {
                                @Override
                                public void sdkCallback(boolean success, String resultCode,
                                                        String resultMessage, Object object, int intefaceType) {
                                    String adurl = (String) object;
                                    videoAdvInfo.VAL.get(index).RS = adurl;
                                    videoAdvInfo.VAL.get(index).RS = (String) object;
                                    countDownLatch.countDown();
                                }
                            });
                }
            });

        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                new MainThreadExecutor().execute(runnable);
            }
        }).start();

    }

}
