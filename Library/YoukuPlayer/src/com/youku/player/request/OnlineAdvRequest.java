package com.youku.player.request;

import android.support.v4.app.FragmentActivity;

import com.baseproject.utils.Logger;
import com.baseproject.utils.Profile;
import com.youku.player.LogTag;
import com.youku.player.Track;
import com.youku.player.goplay.GoplayException;
import com.youku.player.goplay.IGetAdvCallBack;
import com.youku.player.goplay.MyGoplayManager;
import com.youku.player.goplay.VideoAdvInfo;
import com.youku.player.module.PlayVideoInfo;
import com.youku.player.module.VideoUrlInfo;
import com.youku.player.plugin.MediaPlayerDelegate;
import com.youku.player.unicom.ChinaUnicomFreeFlowUtil;
import com.youku.player.unicom.ChinaUnicomManager;
import com.youku.player.util.AdUtil;
import com.youku.player.util.DisposableStatsUtils;
import com.youku.player.util.SessionUnitil;
import com.youku.player.util.URLContainer;

import java.lang.ref.WeakReference;

/**
 * 在线广告请求
 */
public class OnlineAdvRequest implements AdvRequest {

    @Override
    public void requestAdv(final PlayRequest playRequest, final MediaPlayerDelegate mMediaPlayerDelegate,
                           final WeakReference<FragmentActivity> mActivity, PlayVideoInfo playVideoInfo, VideoUrlInfo videoUrlInfo, final IGetAdvCallBack listener) {
        if (mActivity.get() == null) {
            Logger.d(LogTag.TAG_PLAYER, "mActivity.get() == null return");
            listener.onFailed(new GoplayException());
            return;
        }
        Track.onAdReqStart();
        DisposableStatsUtils.disposeAdLossNew(Profile.mContext,
                URLContainer.AD_LOSS_STEP0_NEW,
                SessionUnitil.playEvent_session,
                null);
        MyGoplayManager myGoplayManager = new MyGoplayManager(mActivity.get());
        myGoplayManager.getAdvUrl(playVideoInfo.vid, mMediaPlayerDelegate.isFullScreen, playVideoInfo.isLocalPlay(), playVideoInfo.getAdExt(), videoUrlInfo, playVideoInfo, mMediaPlayerDelegate.getPlayerUiControl().isVerticalFullScreen(),
                new IGetAdvCallBack() {

                    @Override
                    public void onSuccess(final VideoAdvInfo videoAdvInfo) {
                        Logger.d(LogTag.TAG_PLAYER, "获取播放广告信息 GetVideoAdvService成功");
                        if (videoAdvInfo != null) {
                            Track.onAdReqEnd(videoAdvInfo);
                            int size = videoAdvInfo.VAL.size();
                            if (size == 0) {
                                Logger.d(LogTag.TAG_PLAYER, "全屏广告VC:为空");
                                mMediaPlayerDelegate.getPlayerAdControl().onAdvInfoGetted(false);
                            } else {
                                mMediaPlayerDelegate.getPlayerAdControl().onAdvInfoGetted(true);
                                if (!playRequest.isCanceled()) {
                                    mMediaPlayerDelegate.getPlayerAdControl().initInvestigate(videoAdvInfo);
                                }
                            }
                            if (!AdUtil.isAdvVideoType(videoAdvInfo)) {
                                if (AdUtil.isAdvImageTypeYouku(videoAdvInfo)) {
                                    int i = AdUtil.getAdvImageTypePosition(videoAdvInfo);
                                    DisposableStatsUtils.disposeSUS(
                                            Profile.mContext,
                                            videoAdvInfo.VAL.get(i));
                                    if (videoAdvInfo.VAL.get(i).VSC == null || videoAdvInfo.VAL.get(i).VSC
                                            .equalsIgnoreCase("")) {
                                        DisposableStatsUtils.disposeVC(videoAdvInfo.VAL.get(i));
                                    }
                                }
                            }

                            if (mActivity.get() == null || mActivity.get().isFinishing()) {
                                Logger.d(LogTag.TAG_PLAYER,
                                        "GetVideoAdvService success, but activity is finish, so we return directly.");
                                DisposableStatsUtils.disposeAdLossNew(
                                        Profile.mContext,
                                        URLContainer.AD_LOSS_STEP1_NEW,
                                        SessionUnitil.playEvent_session, null);
                                listener.onSuccess(videoAdvInfo);
                                return;
                            }

                            DisposableStatsUtils.disposeAdLossNew(Profile.mContext,
                                    URLContainer.AD_LOSS_STEP2_NEW,
                                    SessionUnitil.playEvent_session,
                                    videoAdvInfo.VAL);
                            //联通3G免流量用户
                            if (ChinaUnicomFreeFlowUtil.isSatisfyChinaUnicomFreeFlow() && videoAdvInfo.VAL.size() != 0) {
                                ChinaUnicomManager.getInstance().replaceAdvUrl(videoAdvInfo, new Runnable() {
                                    @Override
                                    public void run() {
                                        for (int i = 0; i < videoAdvInfo.VAL.size(); i++) {
                                            Logger.d(LogTag.TAG_WO_VIDEO, "FrontADFinalURL=" +
                                                    videoAdvInfo.VAL.get(i).RS);
                                        }
                                        listener.onSuccess(videoAdvInfo);
                                    }

                                });
                            } else
                                listener.onSuccess(videoAdvInfo);
                        } else
                            listener.onSuccess(null);
                    }

                    @Override
                    public void onFailed(GoplayException e) {
                        if (mActivity.get() == null || mActivity.get().isFinishing()) {
                            Logger.d(LogTag.TAG_PLAYER,
                                    "GetVideoAdvService failed, but activity is finish, so we return directly.");
                            DisposableStatsUtils.disposeAdLossNew(Profile.mContext,
                                    URLContainer.AD_LOSS_STEP1_NEW,
                                    SessionUnitil.playEvent_session,
                                    null);
                        } else {
                            DisposableStatsUtils.disposeAdLossNew(Profile.mContext,
                                    URLContainer.AD_LOSS_STEP21_NEW,
                                    SessionUnitil.playEvent_session,
                                    null);

                            Logger.d(LogTag.TAG_PLAYER, "获取播放广告信息 GetVideoAdvService失败");
                            if (!playRequest.isCanceled()) {
                                mMediaPlayerDelegate.getPlayerAdControl().releaseInvestigate();
                            }
                            mMediaPlayerDelegate.getPlayerAdControl().onAdvInfoGetted(false);
                        }
                        listener.onFailed(e);
                    }
                });
    }
}
