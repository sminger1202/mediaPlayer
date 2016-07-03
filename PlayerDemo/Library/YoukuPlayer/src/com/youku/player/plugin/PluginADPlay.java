package com.youku.player.plugin;

import android.app.Activity;
import android.content.Context;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.baseproject.utils.Logger;
import com.baseproject.utils.Util;
import com.youku.player.LogTag;
import com.youku.player.Track;
import com.youku.player.ad.AdState;
import com.youku.player.ad.AdType;
import com.youku.player.apiservice.IAdPlayerCallback;
import com.youku.player.apiservice.ICacheInfo;
import com.youku.player.apiservice.ILifeCycle;
import com.youku.player.apiservice.IPlayerAdControl;
import com.youku.player.apiservice.IPlayerUiControl;
import com.youku.player.base.Plantform;
import com.youku.player.base.YoukuBasePlayerActivity;
import com.youku.player.config.MediaPlayerConfiguration;
import com.youku.player.goplay.AdvInfo;
import com.youku.player.goplay.Profile;
import com.youku.player.goplay.StaticsUtil;
import com.youku.player.goplay.VideoAdvInfo;
import com.youku.player.module.VideoCacheInfo;
import com.youku.player.util.AdUtil;
import com.youku.player.util.AnalyticsWrapper;
import com.youku.player.util.DisposableStatsUtils;
import com.youku.player.util.PlayerUtil;
import com.youku.player.util.SessionUnitil;
import com.youku.player.util.URLContainer;

import java.util.Arrays;

public class PluginADPlay extends PluginVideoAd implements ILifeCycle, IAdPlayerCallback {
    protected String TAG = "PluginADPlay";

    public PluginADPlay(Activity context, MediaPlayerDelegate mediaPlayerDelegate,
                        IPlayerUiControl playerUiControl, IPlayerAdControl playerAdControl) {
        super(context, mediaPlayerDelegate, playerUiControl, playerAdControl);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        play_adButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Util.hasInternet()
                        && !Util.isWifi()
                        && !PreferenceManager.getDefaultSharedPreferences(
                        mActivity).getBoolean("allowONline3G", MediaPlayerConfiguration.getInstance().defaultAllow3G())) {
                    if (mediaPlayerDelegate.videoInfo.playType
                            .equals(StaticsUtil.PLAY_TYPE_LOCAL)) {
                        mediaPlayerDelegate.playVideoWhenADOverTime();
                    } else {
                        Toast.makeText(mActivity, "请设置3g/2g允许播放",
                                Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
                startPlay();
                play_adButton.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void startPlay() {
        if (null == mMediaPlayerDelegate)
            return;
        if (!mMediaPlayerDelegate.isAdvShowFinished()) {
            startPlayByAdButton();
        } else {
            mMediaPlayerDelegate.start();
        }
    }

    private void startPlayByAdButton() {
        if (mediaPlayerDelegate == null || mActivity == null)
            return;
        if (mediaPlayerDelegate.isPause && mediaPlayerDelegate.isAdvShowFinished()) {
            mediaPlayerDelegate.isPause = false;
        } else {
            mediaPlayerDelegate.start();
            if (mediaPlayerDelegate.videoInfo != null
                    && !mediaPlayerDelegate.videoInfo.isAdvEmpty()) {
                mediaPlayerDelegate.seekToPausedADShowing(mediaPlayerDelegate.getAdPausedPosition());
            }
        }
    }

    @Override
    protected AdvInfo getAdvInfo() {
        try {
            return mediaPlayerDelegate.videoInfo.videoAdvInfo.VAL.get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected VideoAdvInfo getVideoAdvInfo() {
        if (mediaPlayerDelegate != null
                && mediaPlayerDelegate.videoInfo != null) {
            return mediaPlayerDelegate.videoInfo.videoAdvInfo;
        }
        return null;
    }

    @Override
    protected void removeCurrentAdv() {
        if (mediaPlayerDelegate != null && mediaPlayerDelegate.videoInfo != null) {
            mediaPlayerDelegate.videoInfo.removePlayedAdv();
        }
    }

    @Override
    public void onBaseResume() {
        this.onBaseResume(false);
    }

    public void onBaseResume(boolean isAutoPlay) {
        if (mActivity == null || mediaPlayerDelegate == null || !mediaPlayerDelegate.isADShowing) {
            return;
        }
        if (!mediaPlayerDelegate.isAdvShowFinished()) {
            if (mediaPlayerDelegate.mAdType == AdType.AD_TYPE_VIDEO) {
                mPlayerUiControl.updatePlugin(PLUGIN_SHOW_AD_PLAY);
                if (!isAutoPlay) {
                    showPlayIcon();
                    int visibility = mediaPlayerDelegate.isPlayLocalType() ? View.GONE : View.VISIBLE;
                    mSwitchParent.setVisibility(visibility);//在播放缓存视频时隐藏缩放按钮
                }
            } else if (mediaPlayerDelegate.mAdType == AdType.AD_TYPE_IMAGE
                    && !mPlayerAdControl.isImageAdShowing()) {
                mPlayerUiControl.updatePlugin(PLUGIN_SHOW_NOT_SET);
            }
        } else {
            // 解决前贴广告播放完，正片还没播放时挂起，回来不能播放问题
            if (getVisibility() == View.VISIBLE && isCountUpdateVisible()) {
                if (!isAutoPlay) {
                    showPlayIcon();
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        VideoAdvInfo adInfo = getVideoAdvInfo();
        if (adInfo == null || adInfo.VAL == null || (adInfo.VAL.size() <= 0)) {//当前广告跳过或者无广告信息后隐藏广告相关控件
            mActivity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    mCountUpdateTextView.setText("");
                    mCountUpdateTextView.setVisibility(View.GONE);
                    ad_more.setVisibility(View.GONE);
                    mAdSkip.setVisibility(View.GONE);
                    mAdSkipBlank.setVisibility(View.GONE);
                    if (Profile.PLANTFORM == Plantform.YOUKU) {
                        mCountUpdateWrap.setVisibility(View.GONE);
                        hideTrueViewAd();
                    }
                }
            });
        }
    }

    @Override
    public void onBaseConfigurationChanged() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPluginAdded() {
        super.onPluginAdded();
        final VideoAdvInfo adInfo = getVideoAdvInfo();
        if (adInfo != null) {
            showAdView(true);
            ad_more.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    descripClick(adInfo);
                }
            });
            mAdPageHolder.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    descripClick(adInfo);
                }
            });

            mAdTrueViewSkipLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (adInfo.VAL.size() <= 0) {
                        return;
                    }

                    AdvInfo advInfo = adInfo.VAL.get(0);
                    if (advInfo == null) {
                        return;
                    }
                    if(canSkipTrueViewAd){// 跳过广告
                        if(mediaPlayerDelegate.mediaPlayer != null && mediaPlayerDelegate.mediaPlayer.isPreparing()){
                            Logger.d(LogTag.TAG_TRUE_VIEW,"-----> mediaPlayer is preparing!");
                            return;
                        }

                        int progress = (mediaPlayerDelegate.isPlaying() ? mediaPlayerDelegate.getCurrentPosition() : mediaPlayerDelegate.getAdPausedPosition() )/ 1000;
                        DisposableStatsUtils.disposeSkipIMP(advInfo,progress);
                        Track.onAdEnd();
                        if(!mediaPlayerDelegate.isPlaying()){
                            if (Util.hasInternet()
                                    && !Util.isWifi()
                                    && !PreferenceManager.getDefaultSharedPreferences(
                                    mActivity).getBoolean("allowONline3G", MediaPlayerConfiguration.getInstance().defaultAllow3G())) {
                                if (mediaPlayerDelegate.videoInfo.playType
                                        .equals(StaticsUtil.PLAY_TYPE_LOCAL)) {
                                    mediaPlayerDelegate.playVideoWhenADOverTime();
                                } else {
                                    Toast.makeText(mActivity, "请设置3g/2g允许播放",
                                            Toast.LENGTH_SHORT).show();
                                }
                                return;
                            }
                            mediaPlayerDelegate.playVideoWhenADOverTime();
                            play_adButton.setVisibility(View.GONE);
                        }else{
                            removeCurrentAdv();
                            if (mediaPlayerDelegate.videoInfo.isCached()) {
                                ICacheInfo download = MediaPlayerDelegate.mICacheInfo;
                                if (download != null) {
                                    if (download.isDownloadFinished(mediaPlayerDelegate.videoInfo.getVid())) {
                                        VideoCacheInfo downloadInfo = download.getDownloadInfo(mediaPlayerDelegate.videoInfo
                                                .getVid());
                                        if (YoukuBasePlayerActivity.isHighEnd) {
                                            mediaPlayerDelegate.videoInfo.cachePath = PlayerUtil
                                                    .getM3u8File(downloadInfo.savePath + "youku.m3u8");
                                        }
                                    }
                                }
                            }
                            mMediaPlayerDelegate.mediaPlayer.skipCurPreAd();
                            mMediaPlayerDelegate.setAdPausedPosition(0);
                        }
                        dismissDownloadDialog();
                        canSkipTrueViewAd = false; //防止连续点击
                    }
                }
            });

            mAdTrueViewPlay.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (adInfo.VAL.size() <= 0) {
                        return;
                    }

                    AdvInfo advInfo = adInfo.VAL.get(0);
                    if (advInfo == null) {
                        return;
                    }

                    int point = (mediaPlayerDelegate.isPlaying() ? mediaPlayerDelegate.getCurrentPosition() : mediaPlayerDelegate.getAdPausedPosition());
                    int progress = point / 1000;
                    DisposableStatsUtils.disposeViewIMP(advInfo,progress);
                    new AdvClickProcessor().trueViewAdvPlayClicked(mActivity, advInfo, point);
                }
            });

            mAdPageHolder.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    descripClick(adInfo);
                }
            });
        }
    }

    @Override
    public boolean onAdStart(int index) {
        super.onAdStart(index);
        mPlayerAdControl.setAdState(AdState.PREAD);
        Track.onAdStart(mActivity, mediaPlayerDelegate);
        String vid = "";
        if (mediaPlayerDelegate != null
                && mediaPlayerDelegate.videoInfo != null)
            vid = mediaPlayerDelegate.videoInfo.getVid();
        Track.trackAdLoad(mActivity.getApplicationContext(), vid, mediaPlayerDelegate.videoInfo);
        mPlayerUiControl.getYoukuPlayerView().setPlayerBlackGone();
        setInteractiveAdVisible(false);
        if (mediaPlayerDelegate != null) {
            mediaPlayerDelegate.isADShowing = true;
            mediaPlayerDelegate.isAdStartSended = true;
            if (mediaPlayerDelegate.videoInfo != null
                    && mediaPlayerDelegate.videoInfo.videoAdvInfo != null) {
                if ((mediaPlayerDelegate.videoInfo.videoAdvInfo.SKIP != null && mediaPlayerDelegate.videoInfo.videoAdvInfo.SKIP
                        .equals("1")) || (Profile.PLANTFORM == Plantform.TUDOU)) {
                    setSkipVisible(true);
                }
                AdvInfo advInfo = mediaPlayerDelegate.videoInfo.getCurrentAdvInfo();
                if (advInfo != null) {
                    if (!advInfo.played())
                        DisposableStatsUtils.disposeAdLossNew(
                                mActivity,
                                URLContainer.AD_LOSS_STEP4_NEW,
                                SessionUnitil.playEvent_session,
                                Arrays.asList(advInfo));
                    advInfo.setPlayed(true);
                    if (mediaPlayerDelegate.videoInfo.getCurrentAdvInfo().RST
                            .equals("hvideo") && !mPlayerUiControl.isOnPause()) {
                        if (isInteractiveAdShow()) {
                            setInteractiveAdVisible(true);
                        } else {
                            String brs = mediaPlayerDelegate.videoInfo.videoAdvInfo.VAL
                                    .get(0).BRS;
                            int count = mediaPlayerDelegate.videoInfo.videoAdvInfo.VAL
                                    .get(0).AL;
                            startInteractiveAd(brs, count);
                            showInteractiveAd();
                        }
                    }
                    // trueView ad
                    if(isTrueViewAd(mediaPlayerDelegate.videoInfo.getCurrentAdvInfo())){
                        showTrueViewAd(mediaPlayerDelegate.videoInfo.getCurrentAdvInfo());
                    } else {
                        hideTrueViewAd();
                    }
                }

            }
        }
        mPlayerUiControl.updatePlugin(PLUGIN_SHOW_AD_PLAY);
        if (null != mediaPlayerDelegate.pluginManager) {
            mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    mediaPlayerDelegate.pluginManager.onLoaded();
                    setVisible(true);
                }
            });
        }
        if (mediaPlayerDelegate != null
                && mediaPlayerDelegate.videoInfo != null) {
            AnalyticsWrapper.adPlayStart(
                    mActivity.getApplicationContext(),
                    mediaPlayerDelegate.videoInfo,
                    mediaPlayerDelegate.videoInfo.getCurrentAdvInfo());
        }
        if (mediaPlayerDelegate.isOfflinePrerollAd()) {//FIXME 发送离线广告统计
            DisposableStatsUtils.disposeOfflineSUS(mActivity.getApplicationContext(),
                    mediaPlayerDelegate.videoInfo, mediaPlayerDelegate.getOfflinePrerollAd());
        } else {
            DisposableStatsUtils.disposeSUS(mActivity.getApplicationContext(),
                    mediaPlayerDelegate.videoInfo);
            if (mediaPlayerDelegate.videoInfo.getCurrentAdvInfo() != null
                    && (mediaPlayerDelegate.videoInfo.getCurrentAdvInfo().VSC == null ||
                    mediaPlayerDelegate.videoInfo.getCurrentAdvInfo().VSC.equalsIgnoreCase(""))) {
                DisposableStatsUtils.disposeVC(mediaPlayerDelegate.videoInfo);
            }
        }
        return false;
    }

    @Override
    public boolean onAdEnd(int index) {
        if (mediaPlayerDelegate != null) {
            mediaPlayerDelegate.isADShowing = false;
        }
        Track.onAdEnd();
        if (mediaPlayerDelegate != null
                && mediaPlayerDelegate.videoInfo != null) {
            AnalyticsWrapper.adPlayEnd(
                    mActivity.getApplicationContext(),
                    mediaPlayerDelegate.videoInfo,
                    mediaPlayerDelegate.videoInfo.getCurrentAdvInfo());
        }
        if (mediaPlayerDelegate.isOfflinePrerollAd()) {//FIXME 发送离线广告统计
            DisposableStatsUtils.disposeOfflineSUE(mActivity.getApplicationContext(),
                    mediaPlayerDelegate.videoInfo, mediaPlayerDelegate.getOfflinePrerollAd());
        } else {
            // 必须在removePlayedAdv之前调用
            DisposableStatsUtils.disposeSUE(
                    mActivity.getApplicationContext(),
                    mediaPlayerDelegate.videoInfo);
        }
        // 当前广告成功播放完成后，从容器中移除
        mediaPlayerDelegate.videoInfo.removePlayedAdv();
        if (mediaPlayerDelegate.videoInfo.isCached()) {
            ICacheInfo download = MediaPlayerDelegate.mICacheInfo;
            if (download != null) {
                if (download.isDownloadFinished(mediaPlayerDelegate.videoInfo.getVid())) {
                    VideoCacheInfo downloadInfo = download.getDownloadInfo(mediaPlayerDelegate.videoInfo
                            .getVid());
                    if (YoukuBasePlayerActivity.isHighEnd) {
                        mediaPlayerDelegate.videoInfo.cachePath = PlayerUtil
                                .getM3u8File(downloadInfo.savePath + "youku.m3u8");
                    }
                }
            }
        }
        if (null != mediaPlayerDelegate.pluginManager) {
            mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    closeInteractiveAdNotIcludeUI();
                    mediaPlayerDelegate.pluginManager.onLoading();
                }
            });
        }
        dismissDownloadDialog();
        return false;
    }

    @Override
    public void onADCountUpdate(final int count) {
        final int currentPosition = (int)Math.round(mediaPlayerDelegate.getCurrentPosition() / 1000d);
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                notifyUpdate(count);
                // trueview广告的倒计时
                notifyTrueViewSkipTime(currentPosition, getAdvInfo());
            }
        });
        if (mediaPlayerDelegate.isOfflinePrerollAd()) {
            DisposableStatsUtils.disposeOfflineSU(mActivity.getApplicationContext(), mediaPlayerDelegate.videoInfo, currentPosition, mediaPlayerDelegate.getOfflinePrerollAd());
        } else {
            DisposableStatsUtils.disposeSU(mActivity.getApplicationContext(), mediaPlayerDelegate.videoInfo, currentPosition);
        }
        // 播放带有互动广告的前贴时，调用互动SDK相应接口
        if (isInteractiveAd(getAdvInfo())) {
            setInteractiveAdPlayheadTime(currentPosition, (getAdvInfo().AL));
        }
    }


    /**
     * 点击“了解详情”
     */
    private void descripClick(VideoAdvInfo adInfo){
        if (adInfo.VAL.size() <= 0) {
            return;
        }

        AdvInfo advInfo = adInfo.VAL.get(0);
        if (advInfo == null) {
            return;
        }
        String url = advInfo.CU;
        Logger.d(LogTag.TAG_PLAYER, "点击url-->" + url);

        if (url == null || TextUtils.getTrimmedLength(url) <= 0) {
            return;
        }
        DisposableStatsUtils.disposeCUM(mActivity.getApplicationContext(), advInfo);
        if (!Util.isWifi() && AdUtil.isDownloadAPK(advInfo, url)
                && MediaPlayerDelegate.mIDownloadApk != null
                && mediaPlayerDelegate != null) {
            creatSelectDownloadDialog(mActivity, Util.isWifi(), url, advInfo);
            return;
        }
        mPlayerAdControl.onMoreInfoClicked(url, advInfo);

    }

}
