package com.youku.player;

import android.content.res.AssetManager;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.LogPrinter;
import android.view.SurfaceHolder;

import com.baseproject.utils.Logger;
import com.youku.player.plugin.MediaPlayerDelegate;
import com.youku.player.goplay.AdvInfo;
import com.youku.player.goplay.Profile;
import com.youku.player.module.VideoUrlInfo;
import com.youku.player.util.PlayerUtil;
import com.youku.uplayer.MPPErrorCode;
import com.youku.uplayer.MediaPlayerProxy;
import com.youku.uplayer.OnADCountListener;
import com.youku.uplayer.OnADPlayListener;
import com.youku.uplayer.OnBufferPercentUpdateListener;
import com.youku.uplayer.OnConnectDelayListener;
import com.youku.uplayer.OnCurrentPositionUpdateListener;
import com.youku.uplayer.OnHttp302DelayListener;
import com.youku.uplayer.OnHwDecodeErrorListener;
import com.youku.uplayer.OnLoadingStatusListener;
import com.youku.uplayer.OnMidADPlayListener;
import com.youku.uplayer.OnNetworkSpeedListener;
import com.youku.uplayer.OnNetworkSpeedPerMinute;
import com.youku.uplayer.OnQualityChangeListener;
import com.youku.uplayer.OnRealVideoStartListener;
import com.youku.uplayer.OnTimeoutListener;
import com.youku.uplayer.OnUplayerPreparedListener;
import com.youku.uplayer.OnVideoIndexUpdateListener;
import com.youku.uplayer.ReleaseTimeoutException;

/**
 * 播放器封装
 * 
 * @author yuanfang
 * 
 */
public class BaseMediaPlayer extends android.media.MediaPlayer implements
		SurfaceHolder.Callback {

	private static final String TAG = LogTag.TAG_PLAYER;
	private static final String TAG_HANDLER = "BaseMediaPlayer_Handler";

	protected SurfaceHolder mSurfaceHolder = null;

	public VideoUrlInfo videoInfo;

	private String mMidAdUrl = null;

	public static synchronized BaseMediaPlayer getInstance() {
		if (mediaPlayer == null) {
			mediaPlayer = new BaseMediaPlayer();
		}
		return mediaPlayer;
	}

	/** 播放器的状态。 */
	protected enum STATE {
		PLAY, PAUSE, PREPARE, PREPARED, SEEK_TO, IDLE
	}

	/** 播放器內部的消息 */
	protected static interface MESSAGE {
		int START = 0;
		int MONITER = 1;
		int RELEASE = 2;
		int SEEK_TO = 3;
		int PAUSE = 4;
		int STOP = 5;
		int PREPARE = 6;
		int SWITCH_DATA_SOURCE = 7; //切换清晰度使用
	}
	
	protected static interface HEARTINTERVAL {
		int TWENTY = 20;
		int SIXTY = 60;
	}

	protected BaseMediaPlayer() {
	}

	/**
	 * 搜索
	 * 
	 * @param msec
	 */
	protected void internalSeekTo(int msec) {
		try {
			if (mCurrentState == STATE.PLAY || mCurrentState == STATE.PAUSE) {
				mCurrentPlayer.seekTo(msec);
				mCurrentState = STATE.SEEK_TO;
				this.mDefaultPlayerState = STATE.SEEK_TO;
			} else if (mCurrentState == STATE.IDLE) {
				play();
				mSeekWhenPrepared = msec;
			} else if (mCurrentState == STATE.PREPARE) {
				mSeekWhenPrepared = msec;
			} else if (mCurrentState == STATE.SEEK_TO) {
				mCurrentPlayer.seekTo(msec);
				mCurrentState = STATE.SEEK_TO;
				this.mDefaultPlayerState = STATE.SEEK_TO;
			}
		} catch (Exception e) {
			Logger.e(TAG, e);
		}

	}

	/**
	 * 在整个视频中的播放位置
	 * 
	 * @return
	 */
	protected int getRealPosition() {

		if (mCurrentPlayer == null)
			return 0;
		try {
			return mCurrentPlayer.getCurrentPosition();
		} catch (Exception e) {
			Logger.e(TAG, e);
			return 0;
		}

	}

	/**
	 * 准备播放器
	 */
	protected void internalPrepare() {
		if (mSurfaceHolder != null) {
			if (onSwitchListener != null) {
				onSwitchListener.onSmall(mSurfaceHolder);
			}
			mCurrentState = STATE.IDLE;
			play();
			this.internalPrepared = true;
			return;
		}
		mHandler.sendEmptyMessageDelayed(MESSAGE.START, 1000);

	}

	/**
	 * 准备完成回调
	 * 
	 * @param mp
	 */
	protected void onPrepared(android.media.MediaPlayer mp) {
		mCurrentPlayer = mp;
		mCurrentState = STATE.PREPARED;
		if (mSeekWhenPrepared > 0) {

			if (Profile.playerType == Profile.PLAYER_OUR) {
				mCurrentPlayer.start();
			}
			mCurrentState = STATE.SEEK_TO;
			mCurrentPlayer.seekTo(mSeekWhenPrepared);
			mSeekWhenPrepared = 0;
		} else {
			if (mTargetState == STATE.PAUSE) {
				mCurrentState = STATE.PAUSE;
				mTargetState = null;
			} else {
				play();
				if (!PlayerUtil.useUplayer(videoInfo) || videoInfo.isNeedLoadedNotify()) {
					if (mOnRealVideoStartListener != null)
						mOnRealVideoStartListener.onRealVideoStart();
				}
			}
		}

	}

	/**
	 * 播放器出错回调
	 * 
	 * @param mp
	 * @param what
	 * @param extra
	 * @return
	 */
	protected boolean onPlayerError(android.media.MediaPlayer mp, int what,
			int extra) {
		onErrorCount++;
		fireError(mCurrentPlayer, what, extra);
		return true;
	}

	/**
	 * 获得正在播放的播放器
	 * 
	 * @return
	 */
	protected android.media.MediaPlayer getCurrentPlayer() {
		return this.mCurrentPlayer;
	}

	/**
	 * 处理暂停
	 */
	protected void internalPause() {

		try {
			mCurrentPlayer.pause();
			mCurrentState = STATE.PAUSE;
		} catch (Exception e) {

		}

	}

	/**
	 * 准备给播放器的数据
	 * 
	 * @return
	 */
	protected String getDataSource() {

		String url = "";
		if (null != videoInfo)
			url = videoInfo.getUrl();
		if ((Profile.from == Profile.PHONE_BROWSER
				|| Profile.from == Profile.PAD_BROWSER)
				&& !PlayerUtil.useUplayer(videoInfo)) {
			return PlayerUtil.getFinnalUrl(url, "");
		}
		return url;
	}

	/**
	 * 各播放器在播放后调用的内容
	 */
	protected void playCallback() {

	}

	/**
	 * 播放中产生错误的回调
	 */
	protected void onPlayError() {
		if (this.mOnErrorListener != null) {
			mOnErrorListener.onError(mCurrentPlayer,
					android.media.MediaPlayer.MEDIA_ERROR_UNKNOWN, 1);
		}
	}

	/**
	 * 准备播放器
	 * 
	 * @throws Exception
	 */
	protected void preparePlayer() throws Exception {
		mSeekWhenPrepared = 0;
		if (this.onSwitchListener != null)
			this.onSwitchListener.onLarge(mSurfaceHolder, 0, 0);
		mCurrentPlayer = new MediaPlayerProxy();
		setMediaListener(mCurrentPlayer);
		new Thread(new Runnable() {

			@Override
			public void run() {
				Handler lastHandler = mHandler;
				final String dataSource = getDataSource();
				Logger.d(TAG_HANDLER, "preparePlayer,lastHandler=" + lastHandler + "  mHandler=" + mHandler);
				// p2p播放会进行302跳转，返回后播放器有可能已经release
				if (lastHandler != mHandler) {
					Logger.d(LogTag.TAG_PLAYER, "handler changed, return!");
					return;
				}
				if (mHandler != null) {
					mHandler.post(new Runnable() {

						@Override
						public void run() {
							try {
								Logger.d(LogTag.TAG_PLAYER, "设置播放地址-->" + dataSource);
								if (TextUtils.isEmpty(dataSource)) {
									fireError(
											mCurrentPlayer,
											MPPErrorCode.MEDIA_INFO_DATA_SOURCE_ERROR);
									return;
								}
								((MediaPlayerProxy) mCurrentPlayer)
										.setHLS(videoInfo == null ? false
												: videoInfo.isHLS);
								((MediaPlayerProxy) mCurrentPlayer)
										.setDRM((videoInfo == null ? false
												: videoInfo.isDRMVideo()));
								((MediaPlayerProxy) mCurrentPlayer).setHardwareDecode(Profile
										.useHardwareDecode(com.baseproject.utils.Profile.mContext)
										&& !(videoInfo == null ? false
												: videoInfo.isExternalVideo));
								Logger.d(LogTag.TAG_PLAYER, "setMidADDataSource" + mMidAdUrl);
								if (mMidAdUrl != null) {
									setMidADDataSource(mMidAdUrl);
								}
								mCurrentPlayer.setDataSource(dataSource);
								if (mHandler.hasMessages(MESSAGE.RELEASE)) {
									Logger.d(LogTag.TAG_PLAYER, "release message in handler, return");
									return;
								}
								mCurrentPlayer.setDisplay(mSurfaceHolder);
								mCurrentPlayer
										.setAudioStreamType(AudioManager.STREAM_MUSIC);
								mCurrentPlayer.setScreenOnWhilePlaying(true);
								mCurrentPlayer.prepareAsync();
								if (mMidAdUrl != null) {
									prepareMidAD();
									mMidAdUrl = null;
								}
								mDefaultPlayerState = STATE.PREPARE;
							} catch (ReleaseTimeoutException e) {
								Logger.e(TAG, e);
								LogPrinter logPrinter = new LogPrinter(Log.VERBOSE, TAG_HANDLER);
								if (mHandler != null) {
									mHandler.dump(logPrinter, TAG_HANDLER);
									if (mHandler.hasMessages(MESSAGE.RELEASE)) {
										Logger.d(TAG, "release message in handler, return");
										return;
									} else
										onPlayError();
								}
							} catch (Exception e) {
								Logger.e(TAG, e);
								onPlayError();
							}
						}
					});
				} else {
					Logger.d(LogTag.TAG_PLAYER, "handler is null, start again.");
					start();
				}
			}
		}).start();
	}

	/**
	 * 内部消息循环
	 * 
	 * @param msg
	 */
	private void interalHandleMessage(Message msg) {
		switch (msg.what) {
			case MESSAGE.START:
				this.internalStart();
				break;
			case MESSAGE.MONITER:
				moniter();
				break;
			case MESSAGE.RELEASE:
				quitLooper();
				break;
			case MESSAGE.SEEK_TO:
				internalSeekTo(msg.arg1);
				break;
			case MESSAGE.PAUSE:
				this.internalPause();
				break;
			case MESSAGE.STOP:
				try {
					this.mCurrentPlayer.stop();
				} catch (Exception e) {
					Logger.e(TAG, e);
				}
				break;
			case MESSAGE.SWITCH_DATA_SOURCE:
				internalSwitchDataSource((String) msg.obj);
				break;
		}
	}

	private void moniter() {
		heartBeat++;
		if (videoInfo != null) {
			interval = videoInfo.isHLS ? HEARTINTERVAL.TWENTY
					: HEARTINTERVAL.SIXTY;
		}
		if (heartBeat >= interval) {
			heartBeat = 0;
			if (mPlayHeartListener != null)
				if (videoInfo != null ? videoInfo.isHLS : false) {
					mPlayHeartListener.onPlayHeartTwentyInterval();
				} else {
					mPlayHeartListener.onPlayHeartSixtyInterval();
				}
			if (getCurrentPosition() >= 1000 && videoInfo != null
					&& videoInfo.getVid() != null
					&& videoInfo.getTitle() != null
					&& videoInfo.getTitle().length() != 0) {
				MediaPlayerDelegate.addIntervalHistory(videoInfo);
			}
		}
		//Logger.v(TAG, "onCurrentPositionUpdate:" + getCurrentPosition() + " mCurrentPosition:" + mCurrentPosition);
		if ((videoInfo == null || !videoInfo.isNeedLoadedNotify()) && (!Profile.USE_SYSTEM_PLAYER && PlayerUtil.useUplayer(videoInfo)
				|| (Profile.from == Profile.PHONE_BROWSER && !PlayerUtil.useUplayer(videoInfo)))) {
			if (mHandler != null)
				mHandler.sendEmptyMessageDelayed(MESSAGE.MONITER, 1000);
			return;
		}
		if (mCurrentState == STATE.PAUSE || mTargetState == STATE.PAUSE) {
			// current state is pause.
			timeout = 0;
			notifyLoaded();
			if (mHandler != null)
				mHandler.sendEmptyMessageDelayed(MESSAGE.MONITER, 1000);
			return;
		}
		try {
			if (mCurrentState == STATE.PLAY || mCurrentState == STATE.SEEK_TO)
				mCurrentPosition = getRealPosition();
		} catch (Exception e) {
			Logger.e(TAG, e);
		}

		if (mCurrentState == STATE.IDLE) {
			if (mHandler != null) {
				mHandler.sendEmptyMessageDelayed(MESSAGE.MONITER, 1000);
				return;
			}
		}

		if (mCurrentState != STATE.IDLE && mCurrentPosition > lastMoniterdPostion) {
			timeout = 0;
			notifyLoaded();
			// 由于获得当前时间的接口是上层判断的，这里可能有连续跳过片尾的问题
			if (mCurrentState != STATE.SEEK_TO
					&& mCurrentPosition - lastMoniterdPostion <= 2000) {
				if (mOnCurrentPositionUpdateListener != null)
					mOnCurrentPositionUpdateListener
							.onCurrentPositionUpdate(mCurrentPosition);
			}
		} else {
			timeout++;
			if (timeout >= 10 && mCurrentState != STATE.PREPARE) {
				notifyChangeVideoQuality();
			}
			
			if (timeout >= 20) {
				notifyTimeOut();
				return;
			}

			if (timeout > 0) {
				notifyLoading();
			}
		}
		lastMoniterdPostion = mCurrentPosition;
		if (mHandler != null)
			mHandler.sendEmptyMessageDelayed(MESSAGE.MONITER, 1000);

	}

	private void notifyChangeVideoQuality() {
		if (this.onTimeOutListener != null)
			this.onTimeOutListener.onNotifyChangeVideoQuality();
	}

	protected static volatile Handler mHandler;

	protected static BaseMediaPlayer mediaPlayer = null;

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Logger.d(TAG, "surfaceChanged");
		mSurfaceHolder = holder;
		// 增加屏幕方向切换时，视频尺寸重置的逻辑。
		if (this.onSwitchListener != null) {
			this.onSwitchListener.onResizeCurrent(width, height);
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Logger.d(TAG, "surfaceCreated");
		if (mSurfaceHolder == null) {
			mSurfaceHolder = holder;
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Logger.d(TAG, "surfaceDestroyed");
		mSurfaceHolder = null;
	}

	public SurfaceHolder getSurfaceHolder() {
		return mSurfaceHolder;
	}
	protected int lastMoniterdPostion = 0;

	protected int mCurrentPosition = 0;

	protected int timeout = 0;

	private int heartBeat = 0;
	
	private int interval = HEARTINTERVAL.SIXTY;

	private android.media.MediaPlayer.OnSeekCompleteListener internalOnSeekCompleteListener = new android.media.MediaPlayer.OnSeekCompleteListener() {

		@Override
		public void onSeekComplete(android.media.MediaPlayer mp) {
			if (mOnSeekCompleteListener != null)
				mOnSeekCompleteListener.onSeekComplete(mp);
			if (mTargetState == STATE.PAUSE) {
				mCurrentState = STATE.PAUSE;
				mTargetState = null;
			} else if (mCurrentState == STATE.PAUSE) {

			} else {
				play();
				if (mSeekWhenSeekComplete > 0) {
					seekTo(mSeekWhenSeekComplete);
					mSeekWhenSeekComplete = 0;
				}
			}
		}

	};

	private android.media.MediaPlayer.OnPreparedListener internalOnPreparedListener = new android.media.MediaPlayer.OnPreparedListener() {

		@Override
		public void onPrepared(android.media.MediaPlayer mp) {
			mVideoHeight = mp.getVideoHeight();
			mVideoWidth = mp.getVideoWidth();
			Track.width = mVideoWidth;
			Track.height = mVideoHeight;
			if (isUsingUMediaplayer() && mOnUplayerPreparedListener != null) {
				mOnUplayerPreparedListener.OnUplayerPrepared();
			}
			BaseMediaPlayer.this.onPrepared(mp);
		}
	};

	private android.media.MediaPlayer.OnErrorListener internalOnErrorListener = new android.media.MediaPlayer.OnErrorListener() {

		@Override
		public boolean onError(android.media.MediaPlayer mp, int what, int extra) {
			error = true;
			return onPlayerError(mp, what, extra);
		}

	};

    protected void fireError(android.media.MediaPlayer player, int what, int extra) {
        if (mOnErrorListener != null)
            mOnErrorListener.onError(player, what, extra);
        error = true;
    }

	private void fireError(android.media.MediaPlayer player, int what) {
        fireError(player, what, 1);
	}

	boolean error = false;
	int onSwitch;

	public android.media.MediaPlayer mSegPlayer1 = null, mSegPlayer2 = null,
			mCurrentPlayer = null;

	protected STATE mSegPlayer1State, mSegPlayer2State, mDefaultPlayerState,
			mCurrentState, mTargetState;

	private void internalStart() {
		if (!this.internalPrepared) {
			this.internalPrepare();
			return;
		}
		if (mCurrentState == STATE.PAUSE) {
			play();
			return;
		} else
			mTargetState = STATE.PLAY;
	}

	public void setMediaListener(android.media.MediaPlayer player) {
		player.setOnPreparedListener(internalOnPreparedListener);
		player.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
		player.setOnCompletionListener(mOnCompletionListener);
		player.setOnErrorListener(internalOnErrorListener);
		player.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
		player.setOnSeekCompleteListener(internalOnSeekCompleteListener);
		if (player instanceof MediaPlayerProxy) {
			((MediaPlayerProxy) player).setOnADPlayListener(mADPlayListener);
			((MediaPlayerProxy) player).setOnMidADPlayListener(mMidADPlayListener);
			((MediaPlayerProxy) player)
					.setOnADCountListener(mOnADCountListener);
			((MediaPlayerProxy) player)
					.setOnNetworkSpeedListener(mOnNetworkSpeedListener);
            ((MediaPlayerProxy) player)
                    .setOnNetworkSpeedPerMinute(mOnNetworkSpeedPerMinute);
            ((MediaPlayerProxy) player)
                    .setOnBufferPercentUpdateListener(mOnBufferPercentUpdateListener);
			((MediaPlayerProxy) player)
					.setOnRealVideoStartListener(mOnRealVideoStartListener);
			((MediaPlayerProxy) player)
					.setOnLodingStatusListener(mOnLoadingStatusListener);
			((MediaPlayerProxy) player)
					.setOnCurrentPositionUpdateListener(mOnCurrentPositionUpdateListener);
			((MediaPlayerProxy) player)
					.setOnVideoIndexUpdateListener(mOnVideoIndexUpdateListener);
			((MediaPlayerProxy)player).setOnTimeoutListener(onTimeOutListener);
			((MediaPlayerProxy)player).setOnHwDecodeErrorListener(mOnHwDecodeErrorListener);
            ((MediaPlayerProxy)player).setOnConnectDelayListener(mOnConnectDelayListener);
			((MediaPlayerProxy)player).setOnQualityChangeListener(mOnQualityChangeListener);
			((MediaPlayerProxy)player).setOnHttp302DelayListener(mOnHttp302DelayListener);
		}
	}

	protected void play() {
		if (mCurrentState == STATE.PLAY)
			return;
		else if (mCurrentState == STATE.PREPARED
				|| mCurrentState == STATE.PAUSE
				|| mCurrentState == STATE.SEEK_TO) {
			mCurrentState = STATE.PLAY;
			try {
				if (mCurrentPlayer != null)
					mCurrentPlayer.start();
			} catch (Exception e) {
				fireError(mCurrentPlayer,
						android.media.MediaPlayer.MEDIA_ERROR_UNKNOWN);
				return;
			}
		} else {
			mCurrentState = STATE.PREPARE;
			try {
				preparePlayer();
			} catch (Exception e) {
				Logger.e(TAG, e);
				onPlayError();
			}
		}
		playCallback();
	}

	protected boolean internalPrepared = false;

	protected int mSeekWhenPrepared = 0;

	private int mSeekWhenSeekComplete = 0;

	protected int mVideoHeight = 0;

	protected int mVideoWidth = 0;

	public void setTimeout(int type, int sec) {
		if (mCurrentPlayer instanceof MediaPlayerProxy) {
			((MediaPlayerProxy)mCurrentPlayer).setTimeout(type, sec);
		}
	}

	@Override
	public int getCurrentPosition() {
		try {
			if (mCurrentPlayer != null)
				return mCurrentPlayer.getCurrentPosition();
		} catch (Exception e) {
			return 0;
		}
		return mCurrentPosition;
	}

	@Override
	public void seekTo(int time) {
		// 由下层控制发送loading
		// if (this.onLoadingListener != null)
		// this.onLoadingListener.onLoading();
		if (null == mHandler)
			return;
        Track.onSeek();
		mHandler.removeMessages(MESSAGE.SEEK_TO);
		Message msg = Message.obtain();
		msg.what = MESSAGE.SEEK_TO;
		msg.arg1 = time;
		mHandler.sendMessage(msg);
	}

	@Override
	public void pause() {
		if (mHandler != null)
			mHandler.sendEmptyMessage(MESSAGE.PAUSE);
	}

	@Override
	public void start() {
		prepareLooper();
		if (mHandler != null) {
			mHandler.sendEmptyMessage(MESSAGE.START);
			mHandler.removeMessages(MESSAGE.MONITER);
			mHandler.sendEmptyMessageDelayed(MESSAGE.MONITER, 1000l);
		}
	}

	@Override
	public void stop() {
		if (mHandler != null)
			mHandler.sendEmptyMessage(MESSAGE.STOP);
	}

	@Override
	public void release() {
		internalPrepared = false;
		error = false;
		mCurrentPosition = 0;
		lastMoniterdPostion = 0;
		timeout = 0;
		heartBeat = 0;
		if (!releasing && !looperQuited) {
			releasing = true;
			internalRelease();
		}
		if (null != mHandler)
			mHandler.sendEmptyMessage(MESSAGE.RELEASE);
	}

	@Override
	public int getVideoHeight() {
		return mVideoHeight;
	}

	@Override
	public int getVideoWidth() {
		return mVideoWidth;
	}

	public void updateWidthAndHeight(int width, int height) {
		mVideoHeight = height;
		mVideoWidth = width;
	}

	public void updateWidthAndHeightFromNative() {
		if (mCurrentPlayer != null) {
			mVideoHeight = mCurrentPlayer.getVideoHeight();
			mVideoWidth = mCurrentPlayer.getVideoWidth();
		}
	}

	@Override
	public int getDuration() {
		if (mCurrentPlayer != null)
			return mCurrentPlayer.getDuration();
		else if (videoInfo != null)
			return videoInfo.getDurationMills();
		else
			return 0;
	}

	@Override
	public boolean isPlaying() {
		if (mCurrentPlayer != null)
			return mCurrentPlayer.isPlaying();
		return mCurrentState == STATE.PLAY;
	}

	@Override
	public void setDisplay(SurfaceHolder sh) {
	}

	/**
	 * Register a callback to be invoked when the end of a media source has been
	 * reached during playback.
	 * 
	 * @param listener
	 *            the callback that will be run
	 */
	@Override
	public void setOnCompletionListener(
			android.media.MediaPlayer.OnCompletionListener listener) {
		mOnCompletionListener = listener;
	}

	private android.media.MediaPlayer.OnCompletionListener mOnCompletionListener;

	/**
	 * Register a callback to be invoked when the status of a network stream's
	 * buffer has changed.
	 * 
	 * @param listener
	 *            the callback that will be run.
	 */
	@Override
	public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
		mOnBufferingUpdateListener = listener;
	}

	private OnBufferingUpdateListener mOnBufferingUpdateListener;

	/**
	 * Register a callback to be invoked when a seek operation has been
	 * completed.
	 * 
	 * @param listener
	 *            the callback that will be run
	 */
	@Override
	public void setOnSeekCompleteListener(
			android.media.MediaPlayer.OnSeekCompleteListener listener) {
		mOnSeekCompleteListener = listener;
	}

	private android.media.MediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener;

	/**
	 * Register a callback to be invoked when the video size is known or
	 * updated.
	 * 
	 * @param listener
	 *            the callback that will be run
	 */
	@Override
	public void setOnVideoSizeChangedListener(
			android.media.MediaPlayer.OnVideoSizeChangedListener listener) {
		mOnVideoSizeChangedListener = listener;
	}

	private android.media.MediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener;

	/**
	 * Register a callback to be invoked when an error has happened during an
	 * asynchronous operation.
	 * 
	 * @param listener
	 *            the callback that will be run
	 */
	@Override
	public void setOnErrorListener(
			android.media.MediaPlayer.OnErrorListener listener) {
		mOnErrorListener = listener;
	}

	protected android.media.MediaPlayer.OnErrorListener mOnErrorListener;

	private OnTimeoutListener onTimeOutListener;

	/**
	 * Interface definition of a callback to be invoked when there has been an
	 * error during an asynchronous operation (other errors will throw
	 * exceptions at method call time).
	 */
	public interface OnSwitchListener {

		void onLarge(SurfaceHolder hodler, int videoWidth, int videoHeight);

		void onSmall(SurfaceHolder holder);

		void onSwitch(SurfaceHolder holder1, SurfaceHolder holder2,
				int videoWidth, int videoHeight);

		void onResizeCurrent(int videoWidth, int videoHeight);
	}
	
	/**
	 * 播放心跳的回调，用于统计
	 */
	public interface OnPlayHeartListener {
		void onPlayHeartSixtyInterval();
		void onPlayHeartTwentyInterval();
	}
	
	protected  OnPlayHeartListener mPlayHeartListener;
	
	public void setOnPlayHeartListener(OnPlayHeartListener onPlayheartListener) {
		mPlayHeartListener = onPlayheartListener;
	}

	protected OnSwitchListener onSwitchListener;

	public void setOnCurrentPositionUpdateListener(
			OnCurrentPositionUpdateListener onCurrentPositionUpdateListener) {
		mOnCurrentPositionUpdateListener = onCurrentPositionUpdateListener;
	}

	public void setOnTimeOutListener(OnTimeoutListener onTimeOutListener) {
		this.onTimeOutListener = onTimeOutListener;
	}

	public void setOnSwitchListener(OnSwitchListener onSwitchListener) {
		this.onSwitchListener = onSwitchListener;
	}

	public void notifyLoaded() {
		if (this.mOnLoadingStatusListener != null)
			this.mOnLoadingStatusListener.onEndLoading();
	}

	private void notifyLoading() {
		if (this.mOnLoadingStatusListener != null)
			this.mOnLoadingStatusListener.onStartLoading();
	}

	private void notifyTimeOut() {
		if (this.onTimeOutListener != null)
			this.onTimeOutListener.onTimeOut();
	}
	
	private synchronized void prepareLooper() {
		Logger.d(TAG_HANDLER, "prepareLooper releasing:" + releasing + "  looperQuited:" + looperQuited);
		if (releasing) {
			cancelQuitLooper = true;
			return;
		} else if (this.looperQuited) {
			new Thread() {
				public void run() {
					Looper.prepare();
					mHandler = new Handler() {

						@Override
						public void handleMessage(Message msg) {
							interalHandleMessage(msg);
						}
					};
					looperPrepared();
					Looper.loop();
				}
			}.start();
			try {
				wait();
			} catch (InterruptedException e) {
				Logger.e(TAG, e);
			}
		}
		/** The Looper has been prepared already. */
	}

	private synchronized void looperPrepared() {
		looperQuited = false;
		this.notify();
	}

	protected synchronized void quitLooper() {
		Logger.d(TAG_HANDLER, "quitLooper, cancelQuitLooper:" + cancelQuitLooper);
		if (cancelQuitLooper) {
			cancelQuitLooper = false;
			mHandler.removeMessages(MESSAGE.RELEASE);
			releasing = false;
			return;
		}
		internalPrepared = false;
		timeout = 0;
		mHandler.removeCallbacksAndMessages(null);
		mHandler = null;
		Looper.myLooper().quit();
		notify();
		looperQuited = true;
		releasing = false;
		mediaPlayer = null;
	}

	private boolean looperQuited = true, releasing = false,
			cancelQuitLooper = false;

	protected int reprepareCount = 0;

	protected int onErrorCount = 0;

	/**
	 * 释放播放器资源
	 */
	protected void internalRelease() {

		try {
			mSeekWhenPrepared = 0;
			mDefaultPlayerState = STATE.IDLE;
			mCurrentState = STATE.IDLE;
			if (mCurrentPlayer != null) {
				mCurrentPlayer.release();
				mCurrentPlayer = null;
			}
		} catch (Exception e) {
			Logger.e(TAG, e);
		}

	}

	public void resetSurfaceHolder() {
		mSurfaceHolder = null;
	}

	public void clearListener() {
		mOnCompletionListener = null;
		mOnBufferingUpdateListener = null;
		mOnSeekCompleteListener = null;
		mOnVideoSizeChangedListener = null;
		mOnErrorListener = null;
		onTimeOutListener = null;
		onSwitchListener = null;
		//setOnPreparedListener(null);
		//setOnInfoListener(null);
		mSurfaceHolder = null;
		mADPlayListener = null;
		mMidADPlayListener = null;
		mOnADCountListener = null;
		mOnNetworkSpeedListener = null;
        mOnNetworkSpeedPerMinute = null;
        mOnBufferPercentUpdateListener = null;
        mOnLoadingStatusListener = null;
		mOnCurrentPositionUpdateListener = null;
		mOnRealVideoStartListener = null;
		mPlayHeartListener = null;
		mOnVideoIndexUpdateListener = null;
		mOnHwDecodeErrorListener = null;
        mOnConnectDelayListener = null;
		mOnUplayerPreparedListener = null;
		mOnHttp302DelayListener = null;
		mOnQualityChangeListener = null;
	}

	public boolean isListenerInit() {
		return (mOnCurrentPositionUpdateListener != null);
	}

	public void changeVideoSize(int width, int height) {
		try {
			if (mCurrentPlayer == null
					|| !(mCurrentPlayer instanceof MediaPlayerProxy))
				return;
			((MediaPlayerProxy) mCurrentPlayer).changeVideoSize(width, height);
		} catch (Exception e) {
		}
	}

	public boolean getTimeOut() {
		return timeout >= 1;
	}

	private OnADPlayListener mADPlayListener;
	private OnMidADPlayListener mMidADPlayListener;
	private OnADCountListener mOnADCountListener;
	private OnNetworkSpeedListener mOnNetworkSpeedListener;
    private OnNetworkSpeedPerMinute mOnNetworkSpeedPerMinute;
    private OnBufferPercentUpdateListener mOnBufferPercentUpdateListener;
	private OnLoadingStatusListener mOnLoadingStatusListener;
	private OnCurrentPositionUpdateListener mOnCurrentPositionUpdateListener;
	private OnRealVideoStartListener mOnRealVideoStartListener;
	private OnVideoIndexUpdateListener mOnVideoIndexUpdateListener;
	private OnHwDecodeErrorListener mOnHwDecodeErrorListener;
    private OnConnectDelayListener mOnConnectDelayListener;
    private OnUplayerPreparedListener mOnUplayerPreparedListener;
	private OnHttp302DelayListener mOnHttp302DelayListener;
	private OnQualityChangeListener mOnQualityChangeListener;

	public void setOnADPlayListener(OnADPlayListener mADPlayListener) {
		this.mADPlayListener = mADPlayListener;
	}

	public void setOnMidADPlayListener(OnMidADPlayListener listener) {
		mMidADPlayListener = listener;
	}

	/**
	 * 转圈状态
	 * 
	 * @param mOnLodingStatusListener
	 */
	public void setOnLoadingStatusListener(
			OnLoadingStatusListener mOnLodingStatusListener) {
		this.mOnLoadingStatusListener = mOnLodingStatusListener;
	}

	public void setOnADCountListener(OnADCountListener mOnADCountListener) {
		this.mOnADCountListener = mOnADCountListener;
	}

	public void setOnNetworkSpeedListener(
			OnNetworkSpeedListener mOnNetworkSpeedListener) {
		this.mOnNetworkSpeedListener = mOnNetworkSpeedListener;
	}

    public void setOnNetworkSpeedPerMinute(OnNetworkSpeedPerMinute listener) {
        mOnNetworkSpeedPerMinute = listener;
    }

    public void setOnBufferPercentUpdateListener(OnBufferPercentUpdateListener listener) {
        mOnBufferPercentUpdateListener = listener;
    }

	public void setOnRealVideoStartListener(
			OnRealVideoStartListener mOnRealVideoStartListener) {
		this.mOnRealVideoStartListener = mOnRealVideoStartListener;
	}

	public void setOnVideoIndexUpdateListener(
			OnVideoIndexUpdateListener listener) {
		mOnVideoIndexUpdateListener = listener;
	}

	public void setOnUplayerPreparedListener(OnUplayerPreparedListener onUplayerPreparedListener) {
		mOnUplayerPreparedListener = onUplayerPreparedListener;
	}

	public int getAdvDuration(){
		int duration = 0;
		if(videoInfo == null || videoInfo.videoAdvInfo == null)
			return duration;
		for (AdvInfo advInfo : videoInfo.videoAdvInfo.VAL) {
			duration += advInfo.AL;
		}
		return duration * 1000;
	}

	public void setOnHwDecodeErrorListener(OnHwDecodeErrorListener listener) {
		this.mOnHwDecodeErrorListener = listener;
	}

    public void setOnConnectDelayListener(OnConnectDelayListener listener){
        mOnConnectDelayListener = listener;
    }

	public void setOnQualityChangeListener(OnQualityChangeListener listener) {
		mOnQualityChangeListener = listener;
	}

	public void setOnHttp302DelayListener(OnHttp302DelayListener listener){
		mOnHttp302DelayListener = listener;
	}
		
	public boolean isPreparing() {
		return mCurrentState == STATE.PREPARE;
	}

	public boolean isPause() {
		return mCurrentState == STATE.PAUSE;
	}

	public void setVideoOrientation(int orientation) {
		try {
			if (mCurrentPlayer == null
					|| !(mCurrentPlayer instanceof MediaPlayerProxy))
				return;
			((MediaPlayerProxy) mCurrentPlayer).setVideoOrientation(orientation);
		} catch (Exception e) {
		}
	}
	
	public void setPlayRate(int rate) {
		try {
			if (mCurrentPlayer == null
					|| !(mCurrentPlayer instanceof MediaPlayerProxy))
				return;
			((MediaPlayerProxy) mCurrentPlayer).setPlayRate(rate);
		} catch (Exception e) {

		}
	}

	public int getVideoOrientation() {
		return mCurrentPlayer == null ? 0 : ((MediaPlayerProxy) mCurrentPlayer)
				.getVideoOrientation();
	}
	
	public boolean isUsingUMediaplayer() {
		if (mCurrentPlayer != null) {
			return ((MediaPlayerProxy) mCurrentPlayer).isUsingUMediaplayer();
		} else
			return MediaPlayerProxy.isUplayerSupported();
	}
	
	public void skipCurPreAd(){
		try {
			if (mCurrentPlayer == null
					|| !(mCurrentPlayer instanceof MediaPlayerProxy))
				return;
			((MediaPlayerProxy) mCurrentPlayer).skipCurPreAd();
		} catch (Exception e) {
		}
	}

	public void setMidADDataSource(String path) {
		try {
			if (mCurrentPlayer == null
					|| !(mCurrentPlayer instanceof MediaPlayerProxy))
				return;
			((MediaPlayerProxy) mCurrentPlayer).setMidADDataSource(path);
		} catch (Exception e) {
		}
	}

	public void prepareMidAD() {
		try {
			if (mCurrentPlayer == null
					|| !(mCurrentPlayer instanceof MediaPlayerProxy))
				return;
			((MediaPlayerProxy) mCurrentPlayer).prepareMidAD();
		} catch (Exception e) {
		}
	}

	public void playMidADConfirm(int videoTime, int adTime) {
		try {
			if (mCurrentPlayer == null
					|| !(mCurrentPlayer instanceof MediaPlayerProxy))
				return;
			((MediaPlayerProxy) mCurrentPlayer).playMidADConfirm(videoTime,
					adTime);
		} catch (Exception e) {
		}
	}

	/**
	 * 切换清晰度时，设置正片URL
	 *
	 * @param url
	 */
	public void switchDataSource(String url) {
		if (null == mHandler)
			return;
		mHandler.removeMessages(MESSAGE.SWITCH_DATA_SOURCE);
		Message msg = Message.obtain();
		msg.what = MESSAGE.SWITCH_DATA_SOURCE;
		msg.obj = url;
		mHandler.sendMessage(msg);
	}

	private int internalSwitchDataSource(String url) {
		try {
			if (mCurrentPlayer == null
					|| !(mCurrentPlayer instanceof MediaPlayerProxy))
				return -1;
			return ((MediaPlayerProxy) mCurrentPlayer).switchDataSource(url);
		} catch (Exception e) {
			return -1;
		}
	}
	
	/**
	 * 设置中插广告URL，setDataSource时，如果中插存在，则先播放中插
	 * 
	 * @param url
	 */
	public void setMidAdUrl(String url) {
		if (url != null && url.length() != 0) {
			mMidAdUrl = url;
		}
	}

    public boolean isStatePlay(){
        return mCurrentState == STATE.PLAY;
    }

    public void enableVoice(int enable) throws IllegalStateException {

        try {
            if (mCurrentPlayer == null
                    || !(mCurrentPlayer instanceof MediaPlayerProxy))
                return;
            ((MediaPlayerProxy) mCurrentPlayer).enableVoice(enable);
        } catch (Exception e) {
}
    }

    public int getVoiceStatus() throws IllegalStateException {
        try {
            if (mCurrentPlayer == null
                    || !(mCurrentPlayer instanceof MediaPlayerProxy))
                return 1;
            ((MediaPlayerProxy) mCurrentPlayer).getVoiceStatus();
        } catch (Exception e) {
        }
        return 1;
    }

	/**
	 * 设置增强模式
	 * @param isEnhance 是否开启增强模式
	 */
	public void setEnhanceMode(boolean isEnhance, float percent){
		if (mCurrentPlayer instanceof MediaPlayerProxy) {
			((MediaPlayerProxy) mCurrentPlayer).setEnhanceMode(isEnhance, percent);
		}
	};

	/**
	 * 设置夜间模式
	 * @param lumRatio 亮度
	 * @param colorRatio 色度
	 */
	public  void setNightMode(float lumRatio, float colorRatio){
		if (mCurrentPlayer instanceof MediaPlayerProxy) {
			((MediaPlayerProxy) mCurrentPlayer).setNightMode(lumRatio, colorRatio);
		}
	};

	/**
	 * 截取视频中的图片
	 * @param outPath 截屏图片输出的完整路径
	 * @param outWidth 截屏图片的宽
	 * @param outHeight 截屏图片的高
	 * @param outFmt 图片格式 （0为png）
	 * @param logoPath 水印的完整路径
	 * @param logoWidth 水印的宽
	 * @param logoHeight 水印的高
	 * @param logoLeft 水印距左边框的距离
	 * @param logoTop 水印距上边框的距离
	 * @return  0成功，非0失败
	 */
	public int screenShotOneFrame(AssetManager assetManager, String outPath, int outWidth, int outHeight,
								   int outFmt, String logoPath, int logoWidth,
								   int logoHeight, int logoLeft, int logoTop){
		if (mCurrentPlayer instanceof MediaPlayerProxy) {
			return (((MediaPlayerProxy) mCurrentPlayer).screenShotOneFrame(assetManager, outPath, outWidth, outHeight, outFmt,
					logoPath, logoWidth, logoHeight, logoLeft, logoTop));
		}
		return -1;
	}

}
