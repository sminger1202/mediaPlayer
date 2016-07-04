package com.youku.player.goplay;

import android.content.Context;
import android.os.Handler;

import com.baseproject.utils.Logger;
import com.baseproject.utils.Util;
import com.youku.player.CheckRecordException;
import com.youku.player.LogTag;
import com.youku.player.Track;
import com.youku.player.ad.AdGetInfo;
import com.youku.player.ad.AdPosition;
import com.youku.player.apiservice.ICacheInfo;
import com.youku.player.base.Plantform;
import com.youku.player.base.YoukuBasePlayerActivity;
import com.youku.player.config.MediaPlayerConfiguration;
import com.youku.player.module.PlayVideoInfo;
import com.youku.player.module.VideoCacheInfo;
import com.youku.player.module.VideoHistoryInfo;
import com.youku.player.module.VideoUrlInfo;
import com.youku.player.plugin.MediaPlayerDelegate;
import com.youku.player.service.GetVideoUrlServiceTudou;
import com.youku.player.service.GetVideoUrlServiceYouku;
import com.youku.player.util.AnalyticsWrapper;
import com.youku.player.util.PlayerUtil;

/**
 * 文件名：MyGoplayManager 功能：调用播放器的接口实现 作者：贾磊 创建时间：2012-12-04
 * 
 */
public class MyGoplayManager {
	private Context mContext;
	private VideoUrlInfo mVideoUrlInfo = new VideoUrlInfo();

	// 构造函数
	public MyGoplayManager(Context mContext) {
		this.mContext = mContext;
	};

//	public void replay(IVideoInfoCallBack call) {
//		if (id == null || "".equals(id))
//			return;
//		goplayer(id, languageCode, videostage, format, 0, isCache, isFromYouku,
//				isTudouAlbum, tudouquality, call);
//	}

	public void goplayer(String id, String languageCode, int videostage,
			int format, int point, boolean isCache, boolean isFromYouku,
			boolean isTudouAlbum, int tudouquality, boolean isFullscreen, IVideoInfoCallBack call) {
		goplayer(id, languageCode, videostage, format, point, isCache, false,
				isFromYouku, isTudouAlbum, tudouquality, null, isFullscreen, call);
	}

	String id;
	String password;
	String languageCode;
	int videostage;
	int format;
	int point;
	boolean isCache;
	boolean isFromYouku;
	boolean isTudouAlbum;
	int tudouquality;
	String playlistCode;
	String playlistId;
	String albumID;

	/**
	 * @param id
	 * @param languageCode
	 * @param videostage
	 * @param format
	 * @param point
	 * @param isCache
	 * @param noAdv
	 * @param isFromYouku
	 *            土豆是false youku是true
	 * @param call
	 */
	public void goplayer(final String id, final String languageCode,
			final int videostage, final int format, final int point,
			boolean isCache, boolean noAdv, boolean isFromYouku,
			boolean isTudouAlbum, int tudouquality, final String playlistCode,
			boolean isFull, final IVideoInfoCallBack call) {
		goplayer(id, null, languageCode, videostage, format, point, isCache,
				noAdv, isFromYouku, isTudouAlbum, tudouquality, playlistCode, null, null, isFull, call);
	}
	
	/**
	 * @param id
	 * @param password
	 *            非加密视频传入null即可
	 * @param languageCode
	 * @param videostage
	 * @param format
	 * @param point
	 * @param isCache
	 * @param noAdv
	 * @param isFromYouku
	 *            土豆是false youku是true
	 * @param call
	 */
	public void goplayer(final String id,final String password, final String languageCode,
			final int videostage, final int format, final int point,
			boolean isCache, boolean noAdv, boolean isFromYouku,
			boolean isTudouAlbum, int tudouquality, final String playlistCode,
			final String playlistId, final String albumID, final boolean isFull, final IVideoInfoCallBack call) {
		//Track.init();
		//Track.setTrackPlayLoading(false);
		this.id = id;
		this.password = password;
		this.languageCode = languageCode;
		this.videostage = videostage;
		this.format = format;
		this.point = point;
		this.isCache = isCache;
		this.isFromYouku = isFromYouku;
		this.isTudouAlbum = isTudouAlbum;
		this.tudouquality = tudouquality;
		this.playlistCode = playlistCode;
		this.playlistId = playlistId;
		this.albumID = albumID;
		try {
			mVideoUrlInfo.setVideoStage(videostage);
			mVideoUrlInfo.setProgress(point);
			mVideoUrlInfo.setid(id);
			mVideoUrlInfo.setRequestId(id);
			mVideoUrlInfo.password = password;
			mVideoUrlInfo.setVid(id);
			mVideoUrlInfo.setAlbum(isTudouAlbum);
			mVideoUrlInfo.playlistCode = playlistCode;
			mVideoUrlInfo.playlistId = playlistId;
			mVideoUrlInfo.albumID = albumID;
			Profile.setVideoType_and_PlayerType(
					YoukuBasePlayerActivity.getCurrentFormat(), mContext);
			Profile.from = Profile.PHONE;
			// if (MediaPlayerDelegate.mICacheInfo != null) {
			// if (MediaPlayerDelegate.mICacheInfo.getDownloadInfo(id) != null
			// || MediaPlayerDelegate.mICacheInfo.getDownloadInfo(id,
			// videostage) != null) {
			// isCache = true;
			// }
			// }
			final boolean hasInternet = Util.hasInternet();
			if (isCache || !hasInternet) {// 视频已经缓存到本地 或 无网络服务。
				// mVideoUrlInfo.setPlayType(StaticsUtil.PLAY_TYPE_LOCAL);
				playVideoFromLocal(mVideoUrlInfo, hasInternet, call);
			} else {
				// 联网播放视频
				String local_vid = "";
				String local_time = String.valueOf(point);
				String local_history = "";
				/** 未登录时，读取本地播放历史 */
				try {
					if ( !MediaPlayerDelegate.mIUserInfo.isLogin() && MediaPlayerDelegate.mIVideoHistoryInfo != null) {
						VideoHistoryInfo hisVideoInfo = MediaPlayerDelegate.mIVideoHistoryInfo
								.getVideoHistoryInfo(id);
						if (null != hisVideoInfo) {
							local_vid = hisVideoInfo.vid;
							local_history = String
									.valueOf((hisVideoInfo.lastPlayTime));
                            local_time = String
									.valueOf(hisVideoInfo.playTime);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				// 没有传videostage的情况下读取播放历史，videostage为零则不再读取播放历史
				if (!PlayerUtil.isLogin() && null != local_vid
						&& 0 != local_vid.length() && videostage == 0) {
					mVideoUrlInfo.setid(local_vid);
					isTudouAlbum = false;
				}
				mVideoUrlInfo.setPlayType(StaticsUtil.PLAY_TYPE_NET);

				Track.playRequest(mContext.getApplicationContext(), id,
						mVideoUrlInfo.getPlayType(), PlayerUtil.isLogin());

				if (isFromYouku) {
					Logger.d(LogTag.TAG_PLAYER, "获取播放信息 playVideoFormNetYouKu");
					playVideoFormNetYouKu(mVideoUrlInfo.getId(), languageCode,
							mVideoUrlInfo.getVideoStage(), format,
							mVideoUrlInfo, point <= 0, local_vid, local_time,
							local_history, noAdv,isFull, call);
				} else {
					Logger.d(LogTag.TAG_PLAYER, "获取播放信息 playVideoFormNetTudou");
					playVideoFormNetTudou(mVideoUrlInfo.getId(), languageCode,
							mVideoUrlInfo.getVideoStage(), format,
							mVideoUrlInfo, point <= 0, local_vid, local_time,
							local_history, noAdv, isTudouAlbum, tudouquality,
							call);
				}
			}
			/*
			TrackerEvent.playRequest(id, mVideoUrlInfo.getPlayType(),
					PlayerUtil.isLogin());
					*/
		} catch (Exception e) {
			GoplayException ex = new GoplayException();
			ex.setErrorCode(107);
			call.onFailed(ex);
		}
	}
	
	/**
	 * @param id
	 * @param password
	 *            非加密视频传入null即可
	 * @param languageCode
	 * @param videostage
	 * @param format
	 * @param point
	 * @param isCache
	 * @param noAdv
	 * @param isFromYouku
	 *            土豆是false youku是true
	 * @param call
	 */
	public void goplayer(final String id, final String adext, final IVideoInfoCallBack call) {
		//Track.init();
		//Track.setTrackPlayLoading(false);
		this.id = id;
		try {
			mVideoUrlInfo.setVideoStage(videostage);
			mVideoUrlInfo.setProgress(point);
			mVideoUrlInfo.setid(id);
			mVideoUrlInfo.password = password;
			mVideoUrlInfo.setVid(id);
			mVideoUrlInfo.setAlbum(isTudouAlbum);
			mVideoUrlInfo.playlistCode = playlistCode;
			mVideoUrlInfo.playlistId = playlistId;
			Profile.setVideoType_and_PlayerType(Profile.FORMAT_FLV_HD, mContext);
			
			Profile.from = Profile.PHONE;
			if (Profile.PLANTFORM == Plantform.YOUKU)
				getVideoUrl(id, languageCode, videostage,
						Profile.FORMAT_FLV_HD, mVideoUrlInfo, false, "", "",
						"", call);
			else if (Profile.PLANTFORM == Plantform.TUDOU)
				getTudouVideoUrl(id, adext, videostage, format, mVideoUrlInfo,
						false, "", "", "", isTudouAlbum, tudouquality, call);
			Track.playRequest(mContext.getApplicationContext(), id,
					mVideoUrlInfo.getPlayType(), PlayerUtil.isLogin());
		} catch (Exception e) {
			GoplayException ex = new GoplayException();
			ex.setErrorCode(107);
			call.onFailed(ex);
		}
	}

	private void playVideoFromLocal(VideoUrlInfo mVideoUrlInfo, 
			final boolean hasInternet, final IVideoInfoCallBack call) throws CheckRecordException {
		ICacheInfo download = MediaPlayerDelegate.mICacheInfo;
		VideoCacheInfo downloadInfo = download.getDownloadInfo(mVideoUrlInfo
				.getVid());
		if (downloadInfo == null) {
			downloadInfo = download.getDownloadInfo(id, videostage);
		}
		if (downloadInfo == null) {
			AnalyticsWrapper.playRequest(mContext, id, mVideoUrlInfo.getPlayType());
			
			GoplayException e = new GoplayException();
			if (isCache) {
				e.setErrorCode(-106);
			} else if (!hasInternet) {
				e.setErrorCode(400);
			}
			call.onFailed(e);
			return;
		}
		if (hasInternet && !isCache) {
			mVideoUrlInfo.setPlayType(StaticsUtil.PLAY_TYPE_NET);
		} else {
			mVideoUrlInfo.setPlayType(StaticsUtil.PLAY_TYPE_LOCAL);
		}
		mVideoUrlInfo.setTitle(downloadInfo.title);
		mVideoUrlInfo.setCached(true);
		if (PlayerUtil.useUplayer(mVideoUrlInfo)) {
			mVideoUrlInfo.cachePath = PlayerUtil
					.getM3u8File(downloadInfo.savePath + "youku.m3u8");
		} else {
			mVideoUrlInfo.cachePath = downloadInfo.savePath + "1.3gp";
		}
		mVideoUrlInfo.setDurationSecs(downloadInfo.seconds);
		if (mVideoUrlInfo.getProgress() != -1) {
			mVideoUrlInfo.setProgress(downloadInfo.playTime * 1000);
			mVideoUrlInfo = MediaPlayerDelegate.getRecordFromLocal(mVideoUrlInfo);
		}
		mVideoUrlInfo.setShowId(downloadInfo.showid);
		mVideoUrlInfo.setShow_videoseq(downloadInfo.show_videoseq);
		mVideoUrlInfo.setEpisodemode(downloadInfo.episodemode);
		mVideoUrlInfo.setMediaType(downloadInfo.mMediaType);
		mVideoUrlInfo.setRegisterNum(downloadInfo.registerNum);
		mVideoUrlInfo.setLicenseNum(downloadInfo.licenseNum);
		mVideoUrlInfo.setVerticalVideo(downloadInfo.isVerticalVideo);
        mVideoUrlInfo.setExclusiveLogo(downloadInfo.exclusiveLogo);
		Track.playRequest(mContext.getApplicationContext(), id,
				mVideoUrlInfo.getPlayType(), PlayerUtil.isLogin());
		if (this.mVideoUrlInfo.getProgress() >= downloadInfo.seconds * 1000 - 60000) {
			mVideoUrlInfo.setProgress(0);
		}
		
		final VideoUrlInfo info = mVideoUrlInfo;

		if (mVideoUrlInfo.getUrl() != null && mVideoUrlInfo.isUrlOK()) {
			new Handler().postDelayed(new Runnable() {
				
				@Override
				public void run() {
					call.onSuccess(info);
				}
			}, 1000);
			
		} else {

			GoplayException e = new GoplayException();
			if (isCache) {
				e.setErrorCode(-996);
				e.setErrorInfo("本地文件已损坏");
			} else if (!hasInternet) {
				e.setErrorCode(400);
			}

			call.onFailed(e);
		}
	}

	/**
	 * 当前为优酷Url地址，使用GetVideoUrlServiceYouku去联网获取数据
	 * 
	 * @param id
	 *            :视频id languageCode：视频语言类型码 videostage 播放阶段
	 *            format：视频清晰度格式(超清，高清，标清) mVideoUrlInfo：存储Url信息的对象 call：回调函数监听
	 * @param resetProgress
	 *            带时间点进入的将忽略其他时间
	 * @param mIUserInfo
	 * 
	 * @return 无返回值
	 * */
	private void playVideoFormNetYouKu(final String id,
			final String languageCode, final int videostage, final int format,
			final VideoUrlInfo mVideoUrlInfo, final boolean resetProgress,
			final String local_vid, final String local_time,
			final String local_history, boolean noAdv, boolean isFull,
			final IVideoInfoCallBack call) {

		// 强制硬解的时候播放m3u8
		if (Profile.USE_SYSTEM_PLAYER) {
			getVideoUrl(id, languageCode, videostage, format, mVideoUrlInfo,
					resetProgress, local_vid, local_time, local_history, call);
			return;
		}

		//先获取视频地址，获取成功以后再获取广告地址
		getVideoUrl(id, languageCode, videostage, format, mVideoUrlInfo,
				resetProgress, local_vid, local_time, local_history, call);

	}

	public void getAdvUrl(final String id, boolean isFull, String adext,
						  VideoUrlInfo videoInfo, PlayVideoInfo playVideoInfo, boolean isvert, IGetAdvCallBack advCallBack) {
		getAdvUrl(id, isFull, false, adext, videoInfo, playVideoInfo, isvert, advCallBack);
	}

	public void getAdvUrl(final String id, boolean isFull, boolean isOfflineAd,
						  String adext, VideoUrlInfo videoInfo, PlayVideoInfo playVideoInfo, boolean isvert, IGetAdvCallBack advCallBack) {
		Logger.d(LogTag.TAG_PLAYER, "获取播放广告信息 GetVideoAdvService");
		GetVideoAdvService getVideoAdvService = new GetVideoAdvService(videoInfo);
		AdGetInfo adGetInfo = new AdGetInfo(id, AdPosition.PRE, isFull, isOfflineAd, playVideoInfo.getSource(), playVideoInfo.playlistId, videoInfo, isvert);
		if (adext == null) {
			getVideoAdvService.getVideoAdv(adGetInfo, mContext, advCallBack);
		} else {
			getVideoAdvService.getVideoAdv(adGetInfo, mContext, adext, advCallBack);
		}
	}

	private void getVideoUrl(String id, String languageCode, int videostage,
			int format, VideoUrlInfo mVideoUrlInfo, boolean resetProgress,
			String local_vid, String local_time, String local_history,
			IVideoInfoCallBack call) {
		Logger.d(LogTag.TAG_PLAYER, "获取正片信息 getVideoUrl");
		GetVideoUrlServiceYouku mGetVideoUrlYouku = new GetVideoUrlServiceYouku(
				mContext);
		mGetVideoUrlYouku.getVideoUrl(id, languageCode, videostage, format,
				mVideoUrlInfo, resetProgress, local_vid, local_time,
				local_history, call);
	}

	// private void playVideoFromNetTudou(String id, int videoseq, String
	// localVid,
	// String localTime, String localHistory, IVideoInfoCallBack call){
	// GetVideoUrlServiceTudou tudou = new GetVideoUrlServiceTudou(mContext);
	// tudou.getVideoUrl(id, localHistory, videoseq, videoseq, mVideoUrlInfo,
	// resetProgress, local_vid, local_time, local_history, mListener);
	// }

	/**
	 * 当前为优酷Url地址，使用GetVideoUrlServiceYouku去联网获取数据
	 * 
	 * @param id
	 *            :视频id languageCode：视频语言类型码 videostage 播放阶段
	 *            format：视频清晰度格式(超清，高清，标清) mVideoUrlInfo：存储Url信息的对象 call：回调函数监听
	 * @param resetProgress
	 *            带时间点进入的将忽略其他时间
	 * @param mIUserInfo
	 * 
	 * @return 无返回值
	 * */
	private void playVideoFormNetTudou(final String id,
			final String languageCode, final int videostage, final int format,
			final VideoUrlInfo mVideoUrlInfo, final boolean resetProgress,
			final String local_vid, final String local_time,
			final String local_history, boolean noAdv,
			final boolean isTudouAlbum, final int tudouquality,
			final IVideoInfoCallBack call) {

		// 强制硬解的时候播放m3u8
		if (Profile.USE_SYSTEM_PLAYER) {
			getTudouVideoUrl(id, languageCode, videostage, format,
					mVideoUrlInfo, resetProgress, local_vid, local_time,
					local_history, isTudouAlbum, tudouquality, call);
			return;
		}

		getTudouVideoUrl(id, languageCode, videostage, format,
				mVideoUrlInfo, resetProgress, local_vid,
				local_time, local_history, isTudouAlbum,
				tudouquality, call);

	}

	private void getTudouVideoUrl(String id, String languageCode,
			int videostage, int format, VideoUrlInfo mVideoUrlInfo,
			boolean resetProgress, String local_vid, String local_time,
			String local_history, boolean isTudouAlbum,int tudouquality, IVideoInfoCallBack call) {
		Logger.d(LogTag.TAG_PLAYER, "获取正片信息 getVideoUrl");
		GetVideoUrlServiceTudou mGetVideoUrlTudou = new GetVideoUrlServiceTudou(
				mContext);
		mGetVideoUrlTudou.getVideoUrl(id, languageCode, videostage, format,
				mVideoUrlInfo, resetProgress, local_vid, local_time,
				local_history, isTudouAlbum,tudouquality, call);
	}
	
	public void playHls(String liveid, final IVideoInfoCallBack call) {
		MediaPlayerConfiguration.getInstance().mPlantformController.playHLS(
				mContext, mVideoUrlInfo, liveid, call);
	}

}
