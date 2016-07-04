package com.youku.player.ad;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;

import com.baseproject.utils.Logger;
import com.youku.android.player.R;
import com.youku.player.LogTag;
import com.youku.player.ad.api.IAdControlListener;
import com.youku.player.apiservice.IAdPlayerCallback;
import com.youku.player.apiservice.IPlayerAdControl;
import com.youku.player.apiservice.IPlayerUiControl;
import com.youku.player.config.MediaPlayerConfiguration;
import com.youku.player.goplay.AdvInfo;
import com.youku.player.goplay.Point;
import com.youku.player.goplay.VideoAdvInfo;
import com.youku.player.module.PlayVideoInfo;
import com.youku.player.plugin.AdvClickProcessor;
import com.youku.player.plugin.MediaPlayerDelegate;
import com.youku.player.plugin.PluginADPlay;
import com.youku.player.plugin.PluginFullScreenPauseAD;
import com.youku.player.plugin.PluginImageAD;
import com.youku.player.plugin.PluginInvestigate;
import com.youku.player.plugin.PluginMidADPlay;
import com.youku.player.plugin.PluginOverlay;
import com.youku.player.ui.widget.YpYoukuDialog;
import com.youku.player.util.DetailMessage;

import java.util.ArrayList;

/**
 * player ad controller.
 */

public class PlayerAdControl implements IPlayerAdControl, DetailMessage {

    private Activity mActivity;
    private MediaPlayerDelegate mMediaPlayerDelegate;

    // 保存PlayVideo()信息，onActivityResult()使用
    private PlayVideoInfo mSavedPlayVideoInfo = null;
    // 提示用户登录dialog
    private Dialog mAdDialogHint = null;

    private boolean isImageADShowing = false;

    //ad state
    private AdState mAdState;
    // mid ad
    private MidAdModel mMidAdModel = null;

    private PluginImageAD mImageAD;
    private PluginInvestigate mInvestigate;
    private PluginFullScreenPauseAD mFullScreenPauseAD;
    private PluginADPlay mPluginADPlay;
    private PluginMidADPlay mPluginMidADPlay = null;

    private IAdControlListener mAdControlListener = null;

    public PlayerAdControl(Activity activity) {
        mActivity = activity;
    }

    /**
     * 设置 监听器
     *
     * @param listener
     */
    public void setListener(IAdControlListener listener) {
        mAdControlListener = listener;
    }

    /**
     * 创建所有广告plugin，初始化mMediaPlayerDelegate
     */
    public void createAdPlugins(IPlayerUiControl playerUiControl, MediaPlayerDelegate mediaPlayerDelegate) {
        mMediaPlayerDelegate = mediaPlayerDelegate;
        if (mPluginADPlay == null) {
            mPluginADPlay = new PluginADPlay(mActivity, mMediaPlayerDelegate, playerUiControl, this);
        }
        if (mPluginMidADPlay == null) {
            mPluginMidADPlay = new PluginMidADPlay(mActivity, mMediaPlayerDelegate, playerUiControl, this);
        }

        if (mImageAD == null) {
            mImageAD = new PluginImageAD(mActivity, mMediaPlayerDelegate, playerUiControl, this);
            mImageAD.setVisibility(View.INVISIBLE);
        }

        if (mFullScreenPauseAD == null) {
            mFullScreenPauseAD = new PluginFullScreenPauseAD(mActivity, mMediaPlayerDelegate,
                    playerUiControl, this);
        }

        if (mInvestigate == null) {
            mInvestigate = new PluginInvestigate(mActivity, mMediaPlayerDelegate,
                    playerUiControl, this);
        }

    }

    public PluginOverlay getPlugin(int adType) {
        PluginOverlay plugin = null;
        switch (adType) {
            case PLUGIN_SHOW_AD_PLAY:
                plugin = mPluginADPlay;
                break;
            case PLUGIN_SHOW_IMAGE_AD:
                plugin = mImageAD;
                break;
            case PLUGIN_SHOW_PAUSE_AD:
                plugin = mFullScreenPauseAD;
                break;
            case PLUGIN_SHOW_MID_AD_PLAY:
                plugin = mPluginMidADPlay;
                break;
            case PLUGIN_SHOW_INVESTIGATE:
                plugin = mInvestigate;
                break;
        }
        return plugin;
    }

    /**
     * 获取前贴或者中插广告播放 UI callback
     */
    public IAdPlayerCallback getVideoADCallBack(int adType) {
        if (adType == PLUGIN_SHOW_AD_PLAY) {
            return mPluginADPlay;
        }
        if (adType == PLUGIN_SHOW_MID_AD_PLAY) {
            return mPluginMidADPlay;
        }
        return null;
    }

    /**
     * 暂停互动广告
     */
    public void pauseInteractiveAd() {
        if (mPluginADPlay != null) {
            mPluginADPlay.pauseInteractiveAd();
        }
        if (mPluginMidADPlay != null) {
            mPluginMidADPlay.pauseInteractiveAd();
        }
    }

    /**
     * 广告登陆提示弹窗返回后，继续播放方法
     */
    public void onLoginDialogComplete() {
        if (mMediaPlayerDelegate == null) {
            return;
        }
        if (mMediaPlayerDelegate.pluginManager != null) {
            mMediaPlayerDelegate.pluginManager.onVideoInfoGetting();
        }
        mMediaPlayerDelegate.getVideoUrlInfo(mSavedPlayVideoInfo);
    }

    /**
     * 广告登陆提示弹窗是否在显示
     */
    public boolean isSuggestLoginDialogShowing() {
        if (mAdDialogHint != null && mAdDialogHint.isShowing()) {
            return true;
        }
        return false;
    }

    /**
     * 取消广告登陆提示弹窗
     */
    public void cancelSuggestLoginDialog() {
        if (mAdDialogHint != null) {
            mAdDialogHint.cancel();
            mAdDialogHint = null;
        }
    }

    /**
     * 互动广告是否正在显示
     */
    public boolean isInteractiveAdShowing() {
        if (mMediaPlayerDelegate != null
                && !mMediaPlayerDelegate.isAdvShowFinished()
                && mPluginADPlay != null && mPluginADPlay.isInteractiveAdShow()
                && !mPluginADPlay.isInteractiveAdHide()) {
            return true;
        } else if (isMidAdShowing() && mPluginMidADPlay != null && mPluginMidADPlay.isInteractiveAdShow()
                && !mPluginMidADPlay.isInteractiveAdHide()) {
            return true;
        }
        return false;
    }

    /**
     * activity onPause时，需调用
     */
    public void onPause() {
        if (mMediaPlayerDelegate != null && !mMediaPlayerDelegate.isAdvShowFinished()
                && mPluginADPlay.isInteractiveAdShow()) {
            if (!mPluginADPlay.isInteractiveAdHide()) {
                mPluginADPlay.closeInteractiveAd();
            } else {
                mPluginADPlay.setInteractiveAdVisible(false);
            }
        } else if (isMidAdShowing()) {
            if (mPluginMidADPlay.isInteractiveAdShow()) {
                if (!mPluginMidADPlay.isInteractiveAdHide()) {
                    mPluginMidADPlay.closeInteractiveAd();
                } else {
                    mPluginMidADPlay.setInteractiveAdVisible(false);
                }
            }
        }
    }

    /**
     * activity onStop时，需调用
     */
    public void onStop() {
        dismissPauseAD();
    }

    /**
     * 释放所有广告，activity onDestroy时调用
     */
    public void destroy() {
        if (mImageAD != null) {
            mImageAD.release();
            mImageAD = null;
        }
        if (mInvestigate != null) {
            mInvestigate.release();
            mInvestigate = null;
        }
        clearMidAD();
        if (mFullScreenPauseAD != null) {
            mFullScreenPauseAD.release();
            mFullScreenPauseAD = null;
        }
    }

    /**
     * 转屏操作时调用
     */
    public void changeConfiguration() {
        if (mInvestigate != null) {
            mInvestigate.updateLayout();
        }
        if (mImageAD != null) {
            mImageAD.onBaseConfigurationChanged();
        }
        if (mPluginADPlay != null) {
            mPluginADPlay.updateBackBtn();
        }
        if (mPluginMidADPlay != null) {
            mPluginMidADPlay.updateBackBtn();
        }
    }

    /**
     * activity onResume时，需要最后延时执行的操作
     */
    public void doOnResumeDelayedOperation(boolean isAutoPlay) {
        if (null != mPluginADPlay) {
            mPluginADPlay.onBaseResume(isAutoPlay);
        }
        if (mMediaPlayerDelegate != null && mMediaPlayerDelegate.isAdvShowFinished()) {
            if (mAdState == AdState.MIDAD
                    && mMidAdModel != null
                    && !mMidAdModel.isCurrentAdvEmpty()) {
                mAdControlListener.updatePlugin(PLUGIN_SHOW_MID_AD_PLAY);
                if (null != mPluginMidADPlay && !isAutoPlay) {
                    mPluginMidADPlay.showPlayIcon();
                }
            } else {
                if (mMidAdModel != null) {
                    mMidAdModel.isAfterEndNoSeek = false;
                }
                if (mAdControlListener != null) {
                    mAdControlListener.updatePlugin(PLUGIN_SHOW_NOT_SET);
                }
            }
        }
    }

    /**
     * activity onResume时，需要执行的操作
     */
    public void doOnResumeOperation() {
        if (mImageAD != null) {
            mImageAD.onBaseResume();
        }
    }

    /**
     * 暂停广告是否正在显示
     */
    public boolean isPauseAdVisible() {
        return mFullScreenPauseAD != null && mFullScreenPauseAD.isVisible();
    }

    /**
     * 设置调查问卷是否隐藏
     */
    public void setInvestigateAdHide(boolean hide) {
        if (mInvestigate == null) {
            return;
        }
        if (hide) {
            mInvestigate.hide();
        } else {
            mInvestigate.unHide();
        }
    }

    /**
     * 屏幕旋转时，全屏广告需要执行的操作
     */
    public void imageAdOnOrientChange() {
        if (mImageAD != null
                && (mImageAD.isStartToShow() || isImageADShowing)
                && !mImageAD.isSaveOnOrientChange()) {
            mImageAD.dismissImageAD();
            if (mMediaPlayerDelegate != null) {
                mMediaPlayerDelegate.startPlayAfterImageAD();
            }
        }
    }

    /**
     * 全屏广告是否正在显示，并且不支持转屏显示？
     */
    public boolean isImageADShowingAndNoSave() {
        return isImageADShowing && mImageAD != null && !mImageAD.isSaveOnOrientChange();
    }

    @Override
    public void onAdvInfoGetted(boolean hasAd) {
        if (mAdControlListener != null) {
            mAdControlListener.onAdvInfoGetted(hasAd);
        }
    }

    @Override
    public void creatDialogToLogin(final PlayVideoInfo playVideoInfo) {
        try {
            if (MediaPlayerConfiguration.getInstance().showLoginDialog()) {
                final YpYoukuDialog dialog = new YpYoukuDialog(mActivity);
                dialog.setNormalPositiveBtn(R.string.playersdk_ad_hint_tologin_cancel,
                        new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                mMediaPlayerDelegate.getVideoUrlInfo(playVideoInfo);
                                mAdDialogHint = null;
                                dialog.dismiss();
                            }
                        });
                dialog.setNormalNegtiveBtn(R.string.playersdk_ad_hint_tologin_ok,
                        new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                try {
                                    mSavedPlayVideoInfo = playVideoInfo;
                                    Intent intent = new Intent();
                                    intent.setClassName(mActivity.getPackageName(),
                                            "com.youku.ui.activity.LoginRegistCardViewDialogActivity");
                                    intent.putExtra("from", 0);
                                    intent.putExtra("track_login_source", 15);
                                    mActivity.startActivity(intent);
                                } catch (Exception e) {
                                } finally {
                                    mAdDialogHint = null;
                                    dialog.dismiss();
                                }
                            }
                        });
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mMediaPlayerDelegate.getVideoUrlInfo(playVideoInfo);
                    }
                });
                dialog.setMessage(R.string.playersdk_ad_hint_tologin_des);
                dialog.setCancelable(true);
                dialog.setCanceledOnTouchOutside(false);
                if (mActivity.isFinishing()) {
                    return;
                }
                dialog.show();
                mAdDialogHint = dialog;
                return;
            }
        } catch (Exception e) {
        }
    }

    @Override
    public boolean isImageAdShowing() {
        return isImageADShowing;
    }

    @Override
    public boolean isImageAdStartToShow() {
        if (mImageAD != null) {
            return mImageAD.isStartToShow();
        }
        return false;
    }

    @Override
    public void setImageAdShowing(boolean isShowing) {
        isImageADShowing = isShowing;
    }

    @Override
    public void showImageAD(VideoAdvInfo videoAdvInfo) {
        if (isImageADShowing) {
            return;
        }
        Logger.d(LogTag.TAG_PLAYER, "show Image AD");
        mImageAD.showAD(videoAdvInfo);
    }

    @Override
    public void dismissImageAD() {
        if (mImageAD != null) {
            mImageAD.dismissImageAD();
        }
    }

    @Override
    public void setMidADInfo(ArrayList<Point> list, String adTest) {
        if (mMidAdModel != null) {
            mMidAdModel.clear();
        }
        if (list != null) {
            mMidAdModel = new MidAdModel(mActivity, mMediaPlayerDelegate, this, adTest);
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).start > 0) {
                    Logger.d(LogTag.TAG_PLAYER, "mid ad point: " + list.get(i).start + "ms");
                    mMidAdModel.addMidAdTimestamp((int) (list.get(i).start));
                    mMidAdModel.addMidAdTypes((int) (list.get(i).start), list.get(i).type);
                }
            }
        }
    }

    @Override
    public void clearMidAD() {
        if (mMidAdModel != null) {
            mMidAdModel.clear();
            mMidAdModel = null;
        }
    }

    @Override
    public boolean isMidAdShowing() {
        return mAdState == AdState.MIDAD;
    }

    @Override
    public MidAdModel getMidAdModel() {
        return mMidAdModel;
    }

    @Override
    public void onMidAdLoadingStartListener() {
        if (mAdControlListener != null) {
            mAdControlListener.onMidAdLoadingStartListener();
        }
    }

    @Override
    public void onMidAdLoadingEndListener() {
        if (mAdControlListener != null) {
            mAdControlListener.onMidAdLoadingEndListener();
        }
    }

    @Override
    public void onMidAdPlay() {
        if (mPluginMidADPlay != null)
            mPluginMidADPlay.initMuteButton();
    }

    @Override
    public void initInvestigate(VideoAdvInfo videoAdvInfo) {
        if (mInvestigate != null) {
            mInvestigate.initData(videoAdvInfo);
        }
    }

    @Override
    public void showInvestigate() {
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                if (mInvestigate != null) {
                    mInvestigate.show();
                }
            }
        });
    }

    @Override
    public void releaseInvestigate() {
        if (mInvestigate != null) {
            mInvestigate.release();
        }
    }

    @Override
    public void showPauseAD() {
        if (!MediaPlayerConfiguration.getInstance().showPauseAd())
            return;
        mFullScreenPauseAD.setVisibility(View.VISIBLE);
        mFullScreenPauseAD.showPauseAD();
    }

    @Override
    public void dismissPauseAD() {
        if (mFullScreenPauseAD != null) {
            mFullScreenPauseAD.setVisible(false);
        }
    }

    @Override
    public void setPauseTestAd(String adext) {
        if (mFullScreenPauseAD != null) {
            mFullScreenPauseAD.setTestAd(adext);
        }
    }

    @Override
    public void dismissInteractiveAD() {
        if (mPluginADPlay != null) {
            mPluginADPlay.closeInteractiveAd();
        }
        if (mPluginMidADPlay != null) {
            mPluginMidADPlay.closeInteractiveAd();
        }
    }

    @Override
    public void setAdState(AdState state) {
        mAdState = state;
    }

    @Override
    public AdState getAdState() {
        return mAdState;
    }

    @Override
    public void onSkipAdClicked() {
        if (mAdControlListener != null) {
            mAdControlListener.onSkipAdClicked();
        }
    }

    @Override
    public void onMoreInfoClicked(String url, AdvInfo advInfo) {
        new AdvClickProcessor().processAdvClick(mActivity, url, advInfo);
    }

    @Override
    public void onDownloadDialogShow(AdvInfo advInfo) {
        if (MediaPlayerDelegate.mIDownloadApk != null && advInfo != null && advInfo.CUF == AdForward.GAME_CENTER)
            MediaPlayerDelegate.mIDownloadApk.onDownloadDialogShow(advInfo.CU);
    }

    @Override
    public void interuptAD() {
        if (mMediaPlayerDelegate != null) {
            mMediaPlayerDelegate.isADShowing = false;
            mActivity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (mAdControlListener != null) {
                        mAdControlListener.updatePlugin(PLUGIN_SHOW_NOT_SET);
                    }
                }
            });
        }
    }
}
