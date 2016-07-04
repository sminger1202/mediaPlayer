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
import com.youku.player.ad.PlayerAdControl;
import com.youku.player.apiservice.IAdPlayerCallback;
import com.youku.player.apiservice.IPlayerAdControl;
import com.youku.player.apiservice.IPlayerUiControl;
import com.youku.player.base.Plantform;
import com.youku.player.config.MediaPlayerConfiguration;
import com.youku.player.goplay.AdvInfo;
import com.youku.player.goplay.Profile;
import com.youku.player.goplay.VideoAdvInfo;
import com.youku.player.util.AdUtil;
import com.youku.player.util.AnalyticsWrapper;
import com.youku.player.util.DisposableStatsUtils;

public class PluginMidADPlay extends PluginVideoAd implements IAdPlayerCallback {
	protected String TAG = "PluginMidADPlay";

	public PluginMidADPlay(Activity context, MediaPlayerDelegate mediaPlayerDelegate,
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
                    Toast.makeText(mActivity, "请设置3g/2g允许播放",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                startPlay();
                play_adButton.setVisibility(View.GONE);
            }
        });
	}

	@Override
	protected void startPlay() {
        if (null == mActivity || mediaPlayerDelegate == null) {
            return;
        }
        if (mediaPlayerDelegate.isPause && mPlayerAdControl.isMidAdShowing()) {
            if (mediaPlayerDelegate.mediaPlayer != null
                    && mPlayerAdControl.getMidAdModel() != null) {
                String url = mPlayerAdControl.getMidAdModel().getCurrentMidAdUrl();
                if (url != null) {
                    mediaPlayerDelegate.mediaPlayer.setMidAdUrl(url);
                }
            }
            mediaPlayerDelegate.start();
        }
    }

	@Override
	protected AdvInfo getAdvInfo() {
		try {
			return mPlayerAdControl.getMidAdModel().getCurrentAdv();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected VideoAdvInfo getVideoAdvInfo() {
		if (mPlayerAdControl != null && mPlayerAdControl.getMidAdModel() != null) {
			return mPlayerAdControl.getMidAdModel().getCurrentAdvInfo();
		}
		return null;
	}

	@Override
	protected void removeCurrentAdv() {
		if (mPlayerAdControl != null && mPlayerAdControl.getMidAdModel() != null) {
            mPlayerAdControl.getMidAdModel().removeCurrentAdv();
		}
	}

    protected String getVideoAdvType() {
        if (mPlayerAdControl != null && mPlayerAdControl.getMidAdModel() != null) {
            return mPlayerAdControl.getMidAdModel().getCurrentAdType();
        }
        return null;
    }

    @Override
    public void onPluginAdded() {
        super.onPluginAdded();
        final VideoAdvInfo adInfo = getVideoAdvInfo();
        String midAdType = getVideoAdvType();
        Logger.d(TAG, "onPluginAdded -----> midAdType :" + midAdType);

        if (adInfo != null) {
            if(midAdType.equals(Profile.CONTENTAD_POINT)){
                showAdView(false);
                setInvestigateAdHide(true);
            } else {
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
            }

        }
    }


    @Override
    public boolean onAdStart(int index) {
        super.onAdStart(index);
        Track.onMidAdAStart();
        mPlayerAdControl.setAdState(AdState.MIDAD);
        mediaPlayerDelegate.mAdType = AdType.AD_TYPE_VIDEO;
        mPlayerUiControl.hideWebView();
        // Track.onAdStart();
        String vid = "";
        if (mediaPlayerDelegate != null
                && mediaPlayerDelegate.videoInfo != null)
            vid = mediaPlayerDelegate.videoInfo.getVid();
        // Track.trackAdLoad(getApplicationContext(), vid);
        mPlayerUiControl.getYoukuPlayerView().setPlayerBlackGone();
        mPlayerUiControl.getYoukuPlayerView().setWaterMarkVisible(false);
        setInteractiveAdVisible(false);
        if (mediaPlayerDelegate != null) {
            mediaPlayerDelegate.isAdStartSended = true;
            if (mPlayerAdControl.getMidAdModel() != null
                    && mPlayerAdControl.getMidAdModel().getCurrentAdvInfo() != null) {
                if ((mPlayerAdControl.getMidAdModel().getCurrentAdvInfo().SKIP != null && mPlayerAdControl.getMidAdModel()
                        .getCurrentAdvInfo().SKIP.equals("1")) || (Profile.PLANTFORM == Plantform.TUDOU)) {
                    setSkipVisible(true);
                }
                if (getAdvInfo() != null && getAdvInfo().RST.equals("hvideo") && !mPlayerUiControl.isOnPause()) {
                    if (isInteractiveAdShow()) {
                        setInteractiveAdVisible(true);
                    } else {
                        String brs = getAdvInfo().BRS;
                        int count = getAdvInfo().AL;
                        startInteractiveAd(brs, count);
                        showInteractiveAd();
            }
        }
            }
        }
        mPlayerUiControl.updatePlugin(PLUGIN_SHOW_MID_AD_PLAY);
        if (null != mediaPlayerDelegate.pluginManager) {
            mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    mediaPlayerDelegate.pluginManager.onLoaded();
                    setVisible(true);
                }
            });
        }
        if (mPlayerAdControl.getMidAdModel() != null
                && mPlayerAdControl.getMidAdModel().getCurrentAdvInfo() != null) {
            AnalyticsWrapper.adPlayStart(
                    mActivity.getApplicationContext(),
                    mediaPlayerDelegate.videoInfo,
                    mPlayerAdControl.getMidAdModel().getCurrentAdv());

            mPlayerAdControl.getMidAdModel().startMidAD();
            DisposableStatsUtils.disposeSUS(
                    mActivity.getApplicationContext(),
                    mPlayerAdControl.getMidAdModel().getCurrentAdv());
        }
        if (mPlayerAdControl.getMidAdModel().getCurrentAdv() != null
                && (mPlayerAdControl.getMidAdModel().getCurrentAdv().VSC == null ||
                mPlayerAdControl.getMidAdModel().getCurrentAdv().VSC.equalsIgnoreCase(""))) {
            DisposableStatsUtils.disposeVC(mPlayerAdControl.getMidAdModel().getCurrentAdv());
        }
        mPlayerUiControl.getYoukuPlayerView().realVideoStart = false;
        mPlayerAdControl.onMidAdLoadingEndListener();

        String type = getVideoAdvType();
        if (!TextUtils.isEmpty(type) && type.equals(Profile.CONTENTAD_POINT)) { // 当是标版广告展示时，隐藏调查问卷
            setInvestigateAdHide(true);
        }
        return false;
    }

    @Override
    public boolean onAdEnd(int index) {
        Track.onMidAdEnd();
        String type = getVideoAdvType();
        if (mPlayerAdControl.getMidAdModel() != null) {
            // 必须在removeCurrentAdv之前调用
            if (mediaPlayerDelegate != null)
                AnalyticsWrapper.adPlayEnd(
                        mActivity.getApplicationContext(),
                        mediaPlayerDelegate.videoInfo,
                        mPlayerAdControl.getMidAdModel().getCurrentAdv());
            DisposableStatsUtils.disposeSUE(
                    mActivity.getApplicationContext(),
                    mPlayerAdControl.getMidAdModel().getCurrentAdv());
            mPlayerAdControl.getMidAdModel().removeCurrentAdv();
            mPlayerAdControl.getMidAdModel().endMidAD();
            if (mPlayerAdControl.getMidAdModel().isCurrentAdvEmpty()) {
                mPlayerAdControl.setAdState(AdState.REALVIDEO);
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

        if (!TextUtils.isEmpty(type) && type.equals(Profile.CONTENTAD_POINT)
                && !mPlayerAdControl.isMidAdShowing()) { // 当是标版广告结束时，显示调查问卷
            setInvestigateAdHide(false);
        }
        return false;
    }

    @Override
    public void onADCountUpdate(final int count) {
            final int currentPosition = (int)Math.round(mediaPlayerDelegate.getCurrentPosition() / 1000d);
            mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    notifyUpdate(count);
                    DisposableStatsUtils.disposeSU(mActivity.getApplicationContext(), mPlayerAdControl.getMidAdModel().getCurrentAdv(), currentPosition);

                    // 播放带有互动广告的前贴时，调用互动SDK相应接口
                    if (getAdvInfo().RST.equals("hvideo")) {
                        setInteractiveAdPlayheadTime(currentPosition, (getAdvInfo().AL));
                    }
                }
            });
    }


    /**
     * 点击“了解详情”
     */
    private void descripClick(VideoAdvInfo adInfo) {
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
            creatSelectDownloadDialog(mActivity, Util.isWifi(),url,advInfo);
            return;
        }
        mPlayerAdControl.onMoreInfoClicked(url, advInfo);
    }

    /**
     * 设置调查问卷是否隐藏
     * @param hide true：隐藏，false：显示
     */
    private void setInvestigateAdHide(boolean hide){
        if (mPlayerAdControl != null) {
            try{
                ((PlayerAdControl)mPlayerAdControl).setInvestigateAdHide(hide);
            } catch (ClassCastException e){
                e.printStackTrace();
            }
        }
    }

}
