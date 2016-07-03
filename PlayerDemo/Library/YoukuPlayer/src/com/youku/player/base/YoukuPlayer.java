package com.youku.player.base;

import android.support.v4.app.FragmentActivity;

import com.youku.player.apiservice.IPlayerAdControl;
import com.youku.player.apiservice.IPlayerUiControl;
import com.youku.player.goplay.Profile;
import com.youku.player.module.PlayVideoInfo;
import com.youku.player.plugin.MediaPlayerDelegate;
import com.youku.player.util.PlayerUtil;

public class YoukuPlayer {

	private MediaPlayerDelegate mMediaPlayerDelegate;

    private PlayerController mPlayerControl;

    public YoukuPlayer(FragmentActivity activity, YoukuPlayerView youkuPlayerView) {
        mPlayerControl = new PlayerController(activity, youkuPlayerView);
        mMediaPlayerDelegate = mPlayerControl.getMediaPlayerDelegate();
    }

    public MediaPlayerDelegate getmMediaPlayerDelegate() {
		return this.mMediaPlayerDelegate;
	}

	/**
	 * 通过vid和playlist_id播放视频
	 * 
	 * @param vid
	 * @param playlistId
	 */
	public void playVideo(final String vid, final String playlistId) {
		mMediaPlayerDelegate.playVideo(vid, playlistId);
	}


	/**
	 * 通过itemCode播放视频
	 * @param itemCode
	 * 预留设置清晰度的接口
	 */
	public void playTudouVideo(final String itemCode,int point, boolean noadv) {
		mMediaPlayerDelegate.playTudouVideo(itemCode, Profile.FORMAT_TUDOU_F4V_480P, point, null, null, noadv);
	}

	/**
	 * 通过itemCode,point,playlistCode播放视频
	 * @param itemCode
	 * @param point
	 * @param playlistCode
	 * 预留设置清晰度的接口
	 */
	public void playTudouVideo(final String itemCode,int point, final String playlistCode, boolean noadv) {
		mMediaPlayerDelegate.playTudouVideo(itemCode,Profile.FORMAT_TUDOU_F4V_480P,point,playlistCode,null, noadv);
	}
	
	/**
	 * 通过itemCode播放视频
	 * @param itemCode
	 * 预留设置清晰度的接口
	 */
	public void playTudouVideo(final String itemCode, boolean noadv) {
		mMediaPlayerDelegate.playTudouVideo(itemCode,Profile.FORMAT_TUDOU_F4V_480P,0,null,null, noadv);
	}

	/**
	 * 通过itemCode,albumID播放视频
	 * @param itemCode
	 * @param albumID
	 */
	public void playTudouVideoWithAlbumID(final String itemCode, final String albumID, boolean noadv) {
		mMediaPlayerDelegate.playVideo(itemCode, null, false, 0, 0, noadv,
				false, false, Profile.FORMAT_TUDOU_F4V_480P, null, null, albumID, null);
	}

	/**
	 * 通过itemCode和视频的password播放加密视频
	 * @param itemCode
	 * @param password
	 */
	public void playTudouVideoWithPassword(final String itemCode, final String password) {
		mMediaPlayerDelegate.playTudouVideoWithPassword(itemCode, password);
	}

	/**
	 * 痛过itemCode重播
	 * @param itemCode
	 */
	public void replayTudouVideo(final String itemCode, boolean noadv) {
		mMediaPlayerDelegate.replayTudouVideo(itemCode, Profile.FORMAT_TUDOU_F4V_480P, noadv);
	}
	
	/**
	 * 通过albumID播放视频
	 * @param albumID
	 */
	public void playTudouAlbum(final String albumID,int point, boolean noadv) {
		mMediaPlayerDelegate.playTudouAlbum(albumID, point, null, noadv);
	}
	
	/**
	 * 通过albumID播放视频
	 * @param albumID
	 */
	public void playTudouAlbum(final String albumID, boolean noadv) {
		mMediaPlayerDelegate.playTudouAlbum(albumID,0,null, noadv);
	}

	/**
	 * 通过albumID以及languageCode来播放视频
	 * @param albumID
	 * @param languageCode
	 */
	public void playTudouAlbum(final String albumID, final String languageCode, boolean noadv) {
		mMediaPlayerDelegate.playTudouAlbum(albumID,0,languageCode, noadv);
	}

	/**
	 * 通过albumID重播视频
	 * @param albumID
	 */
	public void replayTudouAlbum(final String albumID, boolean noadv) {
		mMediaPlayerDelegate.replayTudouAlbum(albumID, noadv);
	}

	/**
	 * 通过vid、isCache、point播放
	 * 
	 * @param vid
	 * @param isCache
	 *            是否缓存
	 * @param point
	 */
	public void playVideo(String vid, boolean isCache, int point) {
		mMediaPlayerDelegate.playVideo(vid, isCache, point);
	}

	public void playLocalVideo(String vid, String url, String title, int progress, boolean isWaterMark, int type){
		mMediaPlayerDelegate.playLocalVideo(vid, !PlayerUtil.useUplayer(null) ? url
				: PlayerUtil.getM3u8File(url), title, progress, isWaterMark, type);
	}
	
	public void playLocalVideo(String vid, String url, String title,
			int progress, boolean isWaterMark) {
		playLocalVideo(vid, url, title, progress, isWaterMark, 0);
	}

	/**
	 * 播放本地存储的视频
	 * 
	 * @param url
	 *            本地视频地址
	 * @param title
	 *            视频标题
	 * @param progress
	 *            播放进度        
	 */
	public void playLocalVideo(String url, String title, int progress) {
		mMediaPlayerDelegate.playLocalVideo(url, title, progress);
	}

	/**
	 * 通过vid播放本地视频,忽略历史
	 * 
	 * @param vid
	 */
	public void replayLocalVideo(final String vid, String url, String title, boolean isWaterMark, int type) {
		mMediaPlayerDelegate.replayLocalVideo(vid,
				!PlayerUtil.useUplayer(null) ? url : PlayerUtil.getM3u8File(url),
				title, isWaterMark, type);
	}

	/**
	 * 通过showid和stage播放视频
	 * 
	 * @param id
	 * @param isCache
	 * @param point
	 * @param videoStage
	 */
	public void playVideoWithStage(String id, boolean isCache, int point,
			int videoStage) {
		mMediaPlayerDelegate.playVideoWithStage(id, isCache, point, videoStage);
	}
	
	/**
	 * 通过albumID和stage播放视频
	 * 
	 * @param id
	 * @param isCache
	 * @param point
	 * @param videoStage
	 */
	public void playVideoWithStageTudou(String id, boolean isCache, int point,
			int videoStage) {
		mMediaPlayerDelegate.playVideoWithStageTudou(id, isCache, point, videoStage);
	}
	
	public void playVideoAdvext(String id, String adext, String adMid,
			String adPause) {
		mMediaPlayerDelegate.playVideoAdvext(id, adext, adMid, adPause);
	}

	/**
	 * 通过 {@link com.youku.player.module.PlayVideoInfo.Builder} 构建PlayVideoInfo进行播放
	 * @param playVideoInfo
	 */
	public void playVideo(PlayVideoInfo playVideoInfo){
		mMediaPlayerDelegate.playVideo(playVideoInfo);
	}

	public void playHLS(String liveid) {
		mMediaPlayerDelegate.playHLS(liveid);
	}

    public IPlayerUiControl getPlayerUiControl(){
        return mPlayerControl;
    }

    public IPlayerAdControl getPlayerAdControl(){
        return mPlayerControl.getPlayerAdControl();
    }
}
