package com.youku.player.request;

import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

import com.baseproject.utils.Logger;
import com.baseproject.utils.Util;
import com.youku.android.player.R;
import com.youku.libmanager.SoUpgradeManager;
import com.youku.libmanager.SoUpgradeService;
import com.youku.player.LogTag;
import com.youku.player.Track;
import com.youku.player.config.MediaPlayerConfiguration;
import com.youku.player.drm.DRMTokenCallback;
import com.youku.player.drm.MarlinDrmManager;
import com.youku.player.goplay.GoplayException;
import com.youku.player.goplay.IGetAdvCallBack;
import com.youku.player.goplay.IVideoInfoCallBack;
import com.youku.player.goplay.MyGoplayManager;
import com.youku.player.goplay.Profile;
import com.youku.player.goplay.StaticsUtil;
import com.youku.player.module.PlayVideoInfo;
import com.youku.player.module.VideoUrlInfo;
import com.youku.player.plugin.MediaPlayerDelegate;
import com.youku.player.util.MainThreadExecutor;

/**
 * 在线播放请求
 */
public class OnlineVideoRequest extends VideoRequest {

    public OnlineVideoRequest(MediaPlayerDelegate mediaPlayerDelegate, FragmentActivity activity) {
        super(mediaPlayerDelegate, activity);
    }

    @Override
    public void requestVideo(final PlayRequest playRequest, final PlayVideoInfo playVideoInfo, final IVideoInfoCallBack listener) {
        final MyGoplayManager myGoplayManager = new MyGoplayManager(mContext);
        myGoplayManager.goplayer(playVideoInfo.vid, playVideoInfo.password, playVideoInfo.languageCode, playVideoInfo.videoStage,
                Profile.getVideoFormat(), playVideoInfo.point, playVideoInfo.isCache, playVideoInfo.noAdv, playVideoInfo.isFromYouku,
                playVideoInfo.isTudouAlbum, playVideoInfo.tudouquality, playVideoInfo.playlistCode, playVideoInfo.playlistId, playVideoInfo.albumID,
                mMediaPlayerDelegate.isFullScreen, new IVideoInfoCallBack() {

                    @Override
                    public void onSuccess(final VideoUrlInfo videoUrlInfo) {
                        if (videoUrlInfo.isDRMVideo()) {
                            if (!SoUpgradeManager.getInstance().isSoDownloaded(SoUpgradeService.LIB_DRM_SO_NAME)) {
                                Logger.d(LogTag.TAG_PLAYER, "needDownloadDRMSo");
                                mMediaPlayerDelegate.videoInfo = videoUrlInfo;
                                SoUpgradeManager.getInstance().startDownloadSo(com.baseproject.utils.Profile.mContext, SoUpgradeService.LIB_DRM_SO_NAME);
                                mMediaPlayerDelegate.pluginManager.needDownloadDRMSo(SoUpgradeService.LIB_DRM_SO_NAME);
                            } else {
                                processDRMToken(videoUrlInfo, new DRMTokenCallback() {
                                    @Override
                                    public void onSuccess() {
                                        mMediaPlayerDelegate.getVideoInfoSuccess(videoUrlInfo, playVideoInfo);
                                        listener.onSuccess(videoUrlInfo);
                                    }

                                    @Override
                                    public void onFail() {
                                        GoplayException exception = new GoplayException();
                                        exception.setErrorInfo(com.baseproject.utils.Profile.mContext.getText(R.string.Player_error_f100)
                                                .toString());
                                        Logger.d(LogTag.TAG_PLAYER, "获取正片信息 失败");
                                        onFailed(exception);
                                    }
                                });
                            }
                        } else {
                            mMediaPlayerDelegate.getVideoInfoSuccess(videoUrlInfo, playVideoInfo);
                            listener.onSuccess(videoUrlInfo);
                        }

                    }

                    @Override
                    public void onFailed(GoplayException e) {
                        MediaPlayerConfiguration.getInstance().mPlantformController
                                .setPlayCode(e);
                        Logger.d(LogTag.TAG_PLAYER, "播放信息获取失败");
                        if (mMediaPlayerDelegate.isChangeLan) {
                            mMediaPlayerDelegate.isChangeLan = false;
                            mMediaPlayerDelegate.onVVEnd();
                        } else
                            Track.onError(mContext, playVideoInfo.vid, Profile.GUID,
                                    StaticsUtil.PLAY_TYPE_NET, MediaPlayerDelegate.playCode,
                                    VideoUrlInfo.Source.YOUKU, Profile.videoQuality, 0,
                                    mMediaPlayerDelegate.isFullScreen, mMediaPlayerDelegate.videoInfo, mMediaPlayerDelegate.getPlayVideoInfo());
                        mMediaPlayerDelegate.isVVBegin998Send = true;
                        MediaPlayerConfiguration.getInstance().mPlantformController
                                .onGetVideoInfoFailed(mActivity,
                                        mMediaPlayerDelegate, e, playVideoInfo.vid,
                                        playVideoInfo.isTudouAlbum, playVideoInfo.playlistCode);
                        listener.onFailed(e);
                    }
                });
    }

    @Override
    public void requestAdv(final PlayRequest playRequest, PlayVideoInfo playVideoInfo, VideoUrlInfo videoUrlInfo, final IGetAdvCallBack listener) {
        if (playVideoInfo.noAdv || !MediaPlayerConfiguration.getInstance().showPreAd()) {
            Logger.d(LogTag.TAG_PLAYER,
                    "handleSuccessfullyGetVideoUrl return directly, due to noAdv="
                            + playVideoInfo.noAdv);
            listener.onFailed(new GoplayException());
            return;
        }
        if (videoUrlInfo != null && !TextUtils.isEmpty(videoUrlInfo.getVid())){ // 当获取视频信息后，更新playVideoInfo里的vid
            Logger.d(LogTag.TAG_PLAYER, "get videoUrlInfo success --> refresh playVideoInfo.vid : " + playVideoInfo.vid + "  to  " + videoUrlInfo.getVid());
            playVideoInfo.vid = videoUrlInfo.getVid();
        }
        AdvRequest advRequest;
        if (!Util.isWifi() && MediaPlayerConfiguration.getInstance().showOfflineAd()
                && downloaded(playVideoInfo)
                && MediaPlayerDelegate.mIUserInfo != null && !MediaPlayerDelegate.mIUserInfo.isVip())
            advRequest = new OfflineAdvRequest();
        else
            advRequest = new OnlineAdvRequest();
        advRequest.requestAdv(playRequest, mMediaPlayerDelegate, mActivity, playVideoInfo, videoUrlInfo, listener);
    }


    private boolean downloaded(PlayVideoInfo playVideoInfo) {
        if (MediaPlayerDelegate.mICacheInfo == null)
            return false;
        return MediaPlayerDelegate.mICacheInfo.getDownloadInfo(playVideoInfo.vid) != null
                || MediaPlayerDelegate.mICacheInfo.getDownloadInfo(playVideoInfo.vid, playVideoInfo.videoStage) != null;
    }

    private void processDRMToken(final VideoUrlInfo videoInfo, final DRMTokenCallback callback) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                boolean result = false;
                try {
                    MarlinDrmManager drmManager = new MarlinDrmManager(
                            com.baseproject.utils.Profile.mContext);
                    result = drmManager.acquireLicense(videoInfo
                            .getMarlinToken());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                final boolean drmResult = result;
                new MainThreadExecutor().execute(new Runnable() {

                    @Override
                    public void run() {
                        if (drmResult) {
                            callback.onSuccess();
                        } else {
                            callback.onFail();
                        }
                    }
                });
            }
        }).start();
    }
}