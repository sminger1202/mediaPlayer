package com.youku.uplayer;

/**
 * 文件名：ProxyMediaPlayer
 * 功能：该类是播放器的代理类，定义了播放器霄1�7要的扄1�7有基本接口函�?
 * 作�1�7�：贾磊
 * 创建时间�?012-05-30
 *  
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.os.Build;
import android.view.SurfaceHolder;

import com.baseproject.utils.Logger;
import com.youku.player.LogTag;
import com.youku.player.goplay.Profile;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

@SuppressLint("DefaultLocale")
public class MediaPlayerProxy extends android.media.MediaPlayer {

	// ///////////////////////////////////////////////////////////////////
	// static variables
	private static MediaPlayerProxy sPlayer = null;
	private static final String TAG = "MediaPlayerProxy";

	// class of media player current action
	private class MPAction {
		public static final int GETCURRENTPOSITION = 30;
		public static final int GETDURATION = 35;
		public static final int GETVIDEOHEIGHT = 40;
		public static final int GETVIDEOWIDTH = 45;
		public static final int ISPLAYING = 50;
		public static final int PAUSE = 55;
		public static final int PREPARE = 60;
		public static final int PREPAREASYNC = 65;
		public static final int RELEASE = 70;
		public static final int RESET = 75;
		public static final int SEEKTO = 80;
		public static final int SETAUDIOSTREAMTYPE = 85;
		public static final int SETDATASOURCE = 90;
		public static final int START = 95;
		public static final int STOP = 100;
	}

	// class of media player state
	private class MPS {
		public static final int UNINITIALIZED = 0;
		public static final int IDLE = 1;
		public static final int INITIALIZED = 2;
		public static final int PREPARING = 3;
		public static final int PREPARED = 4;
		public static final int STARTED = 5;
		public static final int PAUSED = 6;
		public static final int STOPPED = 7;
		public static final int END = 8;
		public static final int PLAYBACKCOMPLETED = 9;

		// public static final int SEEKING = 10;
		public static final int ERROR = -1;
	}

	// //////////////////////////////////////////////////////////////////
	// local variables
	private OriginalMediaPlayer mInnerPlayer = null;
	private SurfaceHolder mHolder = null;
	private boolean mInnerDisplaySet = false;
	private int mMPState = MPS.UNINITIALIZED;
	private int mMPLastState = MPS.UNINITIALIZED;
	private String mPath = null;
	private int mAudioStreamType = -1;
	private boolean isHLS;
	private boolean useHardwareDecode;
	private boolean isDRM;
	// ///////////////////////////////////////////////////////////////////
	// listeners
	private OnBufferingUpdateListener mOuterBufferingUpdateListener = null;
	private OnCompletionListener mOuterCompletionListener = null;
	private OnErrorListener mOuterErrorListener = null;
	private OnInfoListener mOuterInfoListener = null;
	private OnPreparedListener mOuterPreparedListener = null;
	private OnSeekCompleteListener mOuterSeekCompleteListener = null;
	private OnVideoSizeChangedListener mOuterVideoSizeChangedListener = null;

	private int mCurrentOrientation;
	private boolean isReleased;
	/**
	 * 建立播放器
	 */
	private void createInnerPlayer() {
		if (mInnerPlayer != null) {
			return;
		}

		PlayerChooser pc = new PlayerChooser(mPath);
		pc.addAlternative(new PlayerChooser.PlayerAlternative() {

			@Override
			public boolean rule(String fileName) {
				if (fileName == null) {
					return false;
				}
				if (Profile.USE_SYSTEM_PLAYER)
					return true;
				if (isDRM)
					return true;
				String strUpperCase = mPath.toUpperCase();
				/*
				 * int i = 1; if(strUpperCase.startsWith("#PLSEXTM3U")) {
				 * String[] strArray = strUpperCase.split("\n");
				 * 
				 * while(i < strArray.length) {
				 * if(strArray[i].startsWith("#EXTINF:")) { ++i; if(i <
				 * strArray.length) { strUpperCase = strArray[i]; break; } }
				 * ++i; } }
				 */

				strUpperCase = strUpperCase.trim();
				if (strUpperCase.startsWith("#PLSEXTM3U")
						&& strUpperCase.endsWith("#EXT-X-ENDLIST\n"))
					return false;

				boolean b = (strUpperCase.endsWith(".MP4") || strUpperCase
						.endsWith(".3GP"));
				return b || !isUplayerSupported();
			}

			@Override
			public void action() {
				// 系统播放器建立
				mInnerPlayer = new SystemMediaPlayer();
				Logger.d(TAG, "System MediaPlayer is created");
			}

		}).addAlternative(new PlayerChooser.PlayerAlternative() {
			@Override
			public boolean rule(String fileName) {
				return true;
			}

			@Override
			public void action() {
				// 自有播放器建立
				mInnerPlayer = new UMediaPlayer();
				try {
					mInnerPlayer.setHttpUserAgent(Profile.USER_AGENT);
				} catch (IllegalArgumentException e) {
					Logger.e(TAG, e);
				} catch (SecurityException e) {
					Logger.e(TAG, e);
				} catch (IllegalStateException e) {
					Logger.e(TAG, e);
				} catch (IOException e) {
					Logger.e(TAG, e);
				}
				Logger.d(TAG, "UMediaPlayer is created");
			}

		}).decide();
		if (isReleased)
			return;
		mInnerPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
		mInnerPlayer.setOnCompletionListener(mCompletionListener);
		mInnerPlayer.setOnErrorListener(mErrorListener);
		mInnerPlayer.setOnInfoListener(mInfoListener);
		mInnerPlayer.setOnPreparedListener(mPreparedListener);
		mInnerPlayer.setOnSeekCompleteListener(mSeekCompleteListener);
		mInnerPlayer.setOnVideoSizeChangedListener(mVideoSizeChangedListener);
		mInnerPlayer.setOnADPlayListener(mADPlayListener);
		mInnerPlayer.setOnMidADPlayListener(mMidADPlayListener);
		mInnerPlayer
				.setOnCurrentPositionUpdateListener(mOnCurrentPositionUpdateListener);
		mInnerPlayer.setOnLodingStatusListener(mOnLodingStatusListener);
		mInnerPlayer.setOnADCountListener(mOnADCountListener);
		mInnerPlayer.setOnNetworkSpeedListener(mOnNetworkSpeedListener);
        mInnerPlayer.setOnNetworkSpeedPerMinute(mOnNetworkSpeedPerMinute);
        mInnerPlayer.setOnBufferPercentUpdateListener(mOnBufferPercentUpdateListener);
		mInnerPlayer.setOnRealVideoStartListener(mOnRealVideoStartListener);
		mInnerPlayer.setOnVideoIndexUpdateListener(mOnVideoIndexUpdateListener);
		mInnerPlayer.setmOnTimeoutListener(mOnTimeoutListener);
		mInnerPlayer.setOnHwDecodeErrorListener(mOnHwDecodeErrorListener);
        mInnerPlayer.setOnConnectDelayListener(mOnConnectDelayListener);
		mInnerPlayer.setOnHttp302DelayListener(mOnHttp302DelayListener);		
		mInnerPlayer.setOnQualityChangeListener(mOnQualityChangeListener);	
	}

	private void _release() {
		if (mMPState == MPS.PAUSED || mMPState == MPS.PREPARED
				|| mMPState == MPS.STARTED || mMPState == MPS.PLAYBACKCOMPLETED) {
			try {
				// setDisplay(null);
				// stop();
			} catch (Exception e) {
				Logger.e(TAG, e);
			}
		}

		// 去除不用的监听器
		mBufferingUpdateListener = null;
		mCompletionListener = null;
		mErrorListener = null;
		mInfoListener = null;
		mPreparedListener = null;
		mSeekCompleteListener = null;
		mVideoSizeChangedListener = null;
		mADPlayListener = null;
		mMidADPlayListener = null;
		mOnCurrentPositionUpdateListener = null;
		mOnLodingStatusListener = null;
		mOnADCountListener = null;
		mOnNetworkSpeedListener = null;
        mOnNetworkSpeedPerMinute = null;
        mOnBufferPercentUpdateListener = null;
		mOnRealVideoStartListener = null;
		mOnVideoIndexUpdateListener = null;

		mOuterBufferingUpdateListener = null;
		mOuterCompletionListener = null;
		mOuterErrorListener = null;
		mOuterInfoListener = null;
		mOuterPreparedListener = null;
		mOuterSeekCompleteListener = null;
		mOuterVideoSizeChangedListener = null;
		mOnHwDecodeErrorListener = null;
        mOnConnectDelayListener = null;
		mOnTimeoutListener = null;
		mOnHttp302DelayListener = null;		
		mOnQualityChangeListener = null;
		if (mInnerPlayer != null) {
			// mInnerPlayer.pause();

			// 去除不用的监听器
			mInnerPlayer.setOnADPlayListener(null);
			mInnerPlayer.setOnCurrentPositionUpdateListener(null);
			mInnerPlayer.setOnLodingStatusListener(null);
			mInnerPlayer.setOnADCountListener(null);
			mInnerPlayer.setOnNetworkSpeedListener(null);
			mInnerPlayer.setOnRealVideoStartListener(null);
			mInnerPlayer.setOnVideoIndexUpdateListener(null);
			mInnerPlayer.setmOnTimeoutListener(null);
			mInnerPlayer.setOnHwDecodeErrorListener(null);

			mInnerPlayer.release();
			if (mInnerPlayer instanceof UMediaPlayer) {
				((UMediaPlayer) mInnerPlayer).setSurfaceHolder(null);
			}
			mInnerPlayer = null;
		}

		mHolder = null;
	}

	private void verifyState(int mCurrentAction) throws IllegalStateException {
		boolean illegal = false;
		switch (mCurrentAction) {
		case MPAction.GETCURRENTPOSITION:
			illegal = (mMPState == MPS.ERROR);
			break;
		case MPAction.GETDURATION:
			illegal = (mMPState == MPS.IDLE || mMPState == MPS.INITIALIZED || mMPState == MPS.ERROR || mMPState==MPS.PREPARING);
			break;
		case MPAction.GETVIDEOWIDTH:
		case MPAction.GETVIDEOHEIGHT:
			illegal = (mMPState == MPS.ERROR);
			break;
		case MPAction.ISPLAYING:
			illegal = (mMPState == MPS.ERROR);
			break;
		case MPAction.PAUSE:
			illegal = (mMPState == MPS.IDLE || mMPState == MPS.INITIALIZED
					|| mMPState == MPS.PREPARED || mMPState == MPS.STOPPED
					|| mMPState == MPS.PLAYBACKCOMPLETED || mMPState == MPS.ERROR
					|| mMPState == MPS.PREPARING);
			break;
		case MPAction.PREPARE:
		case MPAction.PREPAREASYNC:
			illegal = (mMPState != MPS.INITIALIZED && mMPState != MPS.STOPPED);
			break;
		case MPAction.RELEASE:
		case MPAction.RESET:
			break;
		case MPAction.SEEKTO:
			illegal = (mMPState == MPS.IDLE || mMPState == MPS.INITIALIZED
					|| mMPState == MPS.STOPPED || mMPState == MPS.ERROR);
			break;
		case MPAction.SETAUDIOSTREAMTYPE:
			illegal = (mMPState == MPS.ERROR);
			break;
		case MPAction.SETDATASOURCE:
			illegal = (mMPState != MPS.IDLE);
			break;
		case MPAction.START:
			illegal = (mMPState == MPS.IDLE || mMPState == MPS.INITIALIZED
					|| mMPState == MPS.STOPPED || mMPState == MPS.ERROR);
			break;
		case MPAction.STOP:
			illegal = (mMPState == MPS.IDLE || mMPState == MPS.INITIALIZED || mMPState == MPS.ERROR);
			break;
		}

		if (illegal) {
			throw new IllegalStateException();
		}
	}

	private OnBufferingUpdateListener mBufferingUpdateListener = new OnBufferingUpdateListener() {
		@Override
		public void onBufferingUpdate(android.media.MediaPlayer mp, int percent) {
			Logger.d(TAG, "onBufferingUpdate, " + percent + "% bufferred.");
			if (mOuterBufferingUpdateListener != null) {
				mOuterBufferingUpdateListener.onBufferingUpdate(
						MediaPlayerProxy.this, percent);
			}
		}
	};

	private OnCompletionListener mCompletionListener = new OnCompletionListener() {
		@Override
		public void onCompletion(android.media.MediaPlayer mp) {
			mMPState = MPS.PLAYBACKCOMPLETED;
			if (mOuterCompletionListener != null) {
				mOuterCompletionListener.onCompletion(MediaPlayerProxy.this);
			} else {
				release();
			}
		}
	};

	private OnErrorListener mErrorListener = new OnErrorListener() {
		@Override
		public boolean onError(android.media.MediaPlayer mp, int what, int extra) {
			Logger.e(TAG, "Bug fix: Error received in MediaPlayerProxy" + what);
			if (mOuterErrorListener != null) {
				if (!mOuterErrorListener.onError(MediaPlayerProxy.this, what,
						extra)) {
					mMPState = MPS.ERROR;
				}
			}
				return true;
			}
	};

	private OnInfoListener mInfoListener = new OnInfoListener() {
		@Override
		public boolean onInfo(android.media.MediaPlayer mp, int what, int extra) {
			if (mOuterInfoListener != null) {
				return mOuterInfoListener.onInfo(MediaPlayerProxy.this, what,
						extra);
			}
			return false;
		}
	};

	private OnPreparedListener mPreparedListener = new OnPreparedListener() {
		@Override
		public void onPrepared(android.media.MediaPlayer mp) {
			mMPState = MPS.PREPARED;
			if (mOuterPreparedListener != null) {
				mOuterPreparedListener.onPrepared(MediaPlayerProxy.this);
			}
		}
	};

	private OnSeekCompleteListener mSeekCompleteListener = new OnSeekCompleteListener() {
		@Override
		public void onSeekComplete(android.media.MediaPlayer mp) {
			mMPState = mMPLastState;
			if (mOuterSeekCompleteListener != null) {
				mOuterSeekCompleteListener
						.onSeekComplete(MediaPlayerProxy.this);
			}
			// mInnerPlayer.start();
		}
	};

	private OnVideoSizeChangedListener mVideoSizeChangedListener = new OnVideoSizeChangedListener() {
		@Override
		public void onVideoSizeChanged(android.media.MediaPlayer mp, int width,
				int height) {
			if (mOuterVideoSizeChangedListener != null) {
				mOuterVideoSizeChangedListener.onVideoSizeChanged(
						MediaPlayerProxy.this, width, height);
			}
		}
	};

	// API
	public boolean isLooping() {
		if (mMPState == MPS.IDLE) {
			return false;
		}
		return mInnerPlayer.isLooping();
	}

	public int getCurrentPosition() throws IllegalStateException {
		verifyState(MPAction.GETCURRENTPOSITION);
		if (mMPState == MPS.IDLE) {
			return 0;
		} else {
			if(mInnerPlayer == null)
				return 0;
			return mInnerPlayer.getCurrentPosition();
		}
	}

	public int getDuration() throws IllegalStateException {
		verifyState(MPAction.GETDURATION);
		return mInnerPlayer.getDuration();
	}

	// yujunfeng
	public void setHttpUserAgent(String userAgent) throws IOException,
			IllegalArgumentException, SecurityException, IllegalStateException {
		mInnerPlayer.setHttpUserAgent(userAgent);
	}

	// 0：正向 1：逆90°2：顺90°3：反180°
	public void setVideoOrientation(int orientation)
			throws IllegalStateException {
		mCurrentOrientation = orientation;
		mInnerPlayer.setVideoOrientation(orientation);
	}
	
	public void setPlayRate(int rate) throws IllegalStateException {
		if (mInnerPlayer != null)
			mInnerPlayer.setPlayRate(rate);
	}
	
	public int getVideoOrientation(){
		return mCurrentOrientation;
	}

    public  void enableVoice(int enable) throws IllegalStateException{
        mInnerPlayer.enableVoice(enable);
    }

    public int getVoiceStatus() throws IllegalStateException{
        return mInnerPlayer.getVoiceStatus();
    }

	// 设置旋转后视频的长宽
	public void changeVideoSize(int width, int height)
			throws IllegalStateException {
		if (mInnerPlayer == null)
			return;
		mInnerPlayer.changeVideoSize(width, height);
	}

	public int getVideoWidth() throws IllegalStateException {
		verifyState(MPAction.GETVIDEOWIDTH);
		if (mMPState == MPS.IDLE) {
			return 0;
		} else {
			return mInnerPlayer.getVideoWidth();
		}
	}

	public int getVideoHeight() throws IllegalStateException {
		verifyState(MPAction.GETVIDEOHEIGHT);
		if (mMPState == MPS.IDLE) {
			return 0;
		} else {
			return mInnerPlayer.getVideoHeight();
		}
	}

	public boolean isPlaying() throws IllegalStateException {
		if (mInnerPlayer == null)
			return false;
		verifyState(MPAction.ISPLAYING);
		if (mMPState == MPS.IDLE) {
			return false;
		} else {
			return mInnerPlayer.isPlaying();
		}
	}

	public void pause() throws IllegalStateException {

		verifyState(MPAction.PAUSE);

		mInnerPlayer.pause();
		mMPLastState = mMPState = MPS.PAUSED;
	}

	private void _prepare() {

		try {
			if (mAudioStreamType != -1) {
				mInnerPlayer.setAudioStreamType(mAudioStreamType);
			} else {
				mInnerPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			}

			if (!mInnerDisplaySet) {
				setDisplay(mHolder);
			}
			mInnerPlayer.setScreenOnWhilePlaying(true);
			mInnerPlayer.prepare();
		} catch (Exception e) {
			mErrorListener.onError(mInnerPlayer,
					android.media.MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
		}
		mMPState = MPS.PREPARING;
	}

	public void prepare() throws IOException, IllegalStateException {

		verifyState(MPAction.PREPARE);

		_prepare();
	}

	private void _prepareAsync() {

		try {
			if (mAudioStreamType != -1) {
				mInnerPlayer.setAudioStreamType(mAudioStreamType);
			} else {
				mInnerPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			}

			if (!mInnerDisplaySet) {
				setDisplay(mHolder);
			}
			mInnerPlayer.setScreenOnWhilePlaying(true);
			mInnerPlayer.prepareAsync();
		} catch (Exception e) {
			mErrorListener.onError(mInnerPlayer,
					android.media.MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
		}
		mMPState = MPS.PREPARING;
	}

	public void prepareAsync() throws IllegalStateException {

		verifyState(MPAction.PREPAREASYNC);

		_prepareAsync();
	}

	public void setTimeout(int type, int sec) {
		if (mInnerPlayer != null) {
			mInnerPlayer.setTimeout(type, sec);
		}
	}

	public void release() {
		isReleased = true;
		_release();
		mMPLastState = mMPState = MPS.END;

		sPlayer = null;
	}

	public void reset() {
		if (mInnerPlayer != null) {
			mInnerPlayer.reset();
		}
		mMPLastState = mMPState = MPS.IDLE;
	}

	public void seekTo(int msec) throws IllegalStateException {

		verifyState(MPAction.SEEKTO);
		// if(mMPState == MPS.SEEKING) {
		// return;
		// }
		// mInnerPlayer.pause();
		mInnerPlayer.seekTo(msec);
		mMPLastState = mMPState;
		// mMPState = MPS.SEEKING;
	}

	public void setAudioStreamType(int streamType) throws IllegalStateException {
		verifyState(MPAction.SETAUDIOSTREAMTYPE);
		if (mMPState == MPS.IDLE) {
			mAudioStreamType = streamType;
		} else {
			mInnerPlayer.setAudioStreamType(streamType);
		}
	}

	public void setDisplay(SurfaceHolder sh) {
		mHolder = sh;
		if ((mInnerPlayer != null) && (mHolder != null)) {
			/*
			 * if (mInnerPlayer instanceof UMediaPlayer) { LogOutput.log(TAG,
			 * "<********> SURFACE_TYPE_NORMAL");
			 * mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); } else
			 * { LogOutput.log(TAG, "<********> SURFACE_TYPE_PUSH_BUFFERS");
			 * mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); }
			 */
			Logger.d(TAG, "<********> mInnerPlayer.setDisplay(mHolder)");
			mInnerPlayer.setDisplay(mHolder);
			mInnerDisplaySet = true;
		}
	}

	public void setDataSource(String path) throws IllegalStateException,
			IllegalArgumentException, SecurityException, IOException {
		verifyState(MPAction.SETDATASOURCE);

		mPath = path;
		createInnerPlayer();
		if (mInnerPlayer instanceof UMediaPlayer) {
			int sysVersion = UMediaPlayer.Versions[0];
			if (Build.VERSION.SDK_INT > 21) {
				sysVersion = UMediaPlayer.Versions[10];
			} else if (Build.VERSION.SDK_INT == 21) {
				sysVersion = UMediaPlayer.Versions[9];
			} else if (Build.VERSION.SDK_INT == 19
					|| Build.VERSION.SDK_INT == 20) {
				sysVersion = UMediaPlayer.Versions[8];
			} else if (Build.VERSION.SDK_INT == 18) {
				sysVersion = UMediaPlayer.Versions[7];
			} else if (Build.VERSION.SDK_INT == 17) {
				sysVersion = UMediaPlayer.Versions[6];
			} else if (Build.VERSION.SDK_INT == 16) {
				sysVersion = UMediaPlayer.Versions[5];
			} else if (Build.VERSION.SDK_INT == 14
					|| Build.VERSION.SDK_INT == 15) {
				sysVersion = UMediaPlayer.Versions[4];
			} else if (Build.VERSION.SDK_INT == 11
					|| Build.VERSION.SDK_INT == 12
					|| Build.VERSION.SDK_INT == 13) {
				sysVersion = UMediaPlayer.Versions[3];
			} else if (Build.VERSION.SDK_INT == 10
					|| Build.VERSION.SDK_INT == 9) {
				sysVersion = UMediaPlayer.Versions[2];
			} else if (Build.VERSION.SDK_INT == 8) {
				sysVersion = UMediaPlayer.Versions[1];
			}
			// 直播强制走软解
			((UMediaPlayer) mInnerPlayer).setDataSource(path, "/data/data/"
					+ com.baseproject.utils.Profile.mContext.getPackageName()
					+ "/lib/", isHLS ? false : useHardwareDecode, sysVersion,
					isHLS);
		} else
			mInnerPlayer.setDataSource(mPath);
		Logger.d(LogTag.TAG_PLAYER, "useHardwareDecode:" + useHardwareDecode
				+ " isHLS:" + isHLS);
		mMPLastState = mMPState = MPS.INITIALIZED;
	}

	public void setMidADDataSource(String path) throws IllegalStateException,
			IllegalArgumentException, SecurityException, IOException {
		mPath = path;
		createInnerPlayer();
		if (mInnerPlayer != null && mInnerPlayer instanceof UMediaPlayer) {
			int sysVersion = UMediaPlayer.Versions[0];
			if (Build.VERSION.SDK_INT > 21) {
				sysVersion = UMediaPlayer.Versions[10];
			} else if (Build.VERSION.SDK_INT == 21) {
				sysVersion = UMediaPlayer.Versions[9];
			} else if (Build.VERSION.SDK_INT == 19
					|| Build.VERSION.SDK_INT == 20) {
				sysVersion = UMediaPlayer.Versions[8];
			} else if (Build.VERSION.SDK_INT == 18) {
				sysVersion = UMediaPlayer.Versions[7];
			} else if (Build.VERSION.SDK_INT == 17) {
				sysVersion = UMediaPlayer.Versions[6];
			} else if (Build.VERSION.SDK_INT == 16) {
				sysVersion = UMediaPlayer.Versions[5];
			} else if (Build.VERSION.SDK_INT == 14
					|| Build.VERSION.SDK_INT == 15) {
				sysVersion = UMediaPlayer.Versions[4];
			} else if (Build.VERSION.SDK_INT == 11
					|| Build.VERSION.SDK_INT == 12
					|| Build.VERSION.SDK_INT == 13) {
				sysVersion = UMediaPlayer.Versions[3];
			} else if (Build.VERSION.SDK_INT == 10
					|| Build.VERSION.SDK_INT == 9) {
				sysVersion = UMediaPlayer.Versions[2];
			} else if (Build.VERSION.SDK_INT == 8) {
				sysVersion = UMediaPlayer.Versions[1];
			}
			((UMediaPlayer) mInnerPlayer).setMidADDataSource(
					path,
					"/data/data/"
							+ com.baseproject.utils.Profile.mContext
									.getPackageName() + "/lib/", isHLS ? false
							: useHardwareDecode, sysVersion, isHLS);
		}
	}

	public void prepareMidAD() throws IOException, IllegalStateException {
		if (mInnerPlayer instanceof UMediaPlayer) {
			((UMediaPlayer) mInnerPlayer).prepareMidAD();
		}
	}

	public void playMidADConfirm(int videoTime, int adTime) throws IllegalStateException {
		Logger.d(LogTag.TAG_PLAYER, "mid ad start to play");
		if (mInnerPlayer instanceof UMediaPlayer) {
			((UMediaPlayer) mInnerPlayer).playMidADConfirm(videoTime, adTime);
		}
	}

	public int switchDataSource(String url) throws IOException, IllegalArgumentException, SecurityException,
			IllegalStateException {
		Logger.d(LogTag.TAG_PLAYER, "switch data source");
		if (mInnerPlayer instanceof UMediaPlayer) {
			return ((UMediaPlayer) mInnerPlayer).switchDataSource(url);
		}
		return -1;
	}


	public void start() throws IllegalStateException {
		verifyState(MPAction.START);
		mInnerPlayer.start();
		mMPLastState = mMPState = MPS.STARTED;
	}

	public void stop() throws IllegalStateException {
		verifyState(MPAction.STOP);
		mInnerPlayer.stop();
		mMPLastState = mMPState = MPS.STOPPED;
	}

	public void skipCurPreAd() throws IllegalStateException{
		mInnerPlayer.skipCurPreAd();
	}
	
	// set listeners
	public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
		mOuterBufferingUpdateListener = listener;
	}

	public void setOnCompletionListener(OnCompletionListener listener) {
		mOuterCompletionListener = listener;
	}

	public void setOnErrorListener(OnErrorListener listener) {
		mOuterErrorListener = listener;
	}

	public void setOnInfoListener(OnInfoListener listener) {
		mOuterInfoListener = listener;
	}

	public void setOnPreparedListener(OnPreparedListener listener) {
		mOuterPreparedListener = listener;
	}

	public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
		mOuterSeekCompleteListener = listener;
	}

	public void setOnVideoSizeChangedListener(
			OnVideoSizeChangedListener listener) {
		mOuterVideoSizeChangedListener = listener;
	}

	public void setEnhanceMode(boolean isEnhance, float percent){
		Logger.d("nightMode","setEnhanceMode isEnhance :" + isEnhance + " / percent : " + percent);
		if (mInnerPlayer instanceof UMediaPlayer) {
			((UMediaPlayer) mInnerPlayer).setEnhanceMode(isEnhance, percent);
		}
	};

	public  void setNightMode(float lumRatio, float colorRatio){
		if (mInnerPlayer instanceof UMediaPlayer) {
			((UMediaPlayer) mInnerPlayer).setNightMode(lumRatio, colorRatio);
		}
	};

	public int screenShotOneFrame(AssetManager assetManager, String outPath, int outWidth, int outHeight,
										  int outFmt, String logoPath, int logoWidth,
										  int logoHeight, int logoLeft, int logoTop){
		Logger.d("PlayFlow","screenShotOneFrame outPath : " + outPath + " , logoPath : " + logoPath);
		if (mInnerPlayer instanceof UMediaPlayer) {
			return ((UMediaPlayer) mInnerPlayer).screenShotOneFrame(assetManager, outPath, outWidth, outHeight, outFmt,
					logoPath, logoWidth, logoHeight, logoLeft, logoTop);
		}
		return -1;
	}

	// ////////////////////////////////////////////////////////////
	// constructors
	public MediaPlayerProxy() {
		mInnerPlayer = null;
		mHolder = null;
		mInnerDisplaySet = false;
		mPath = null;

		mMPLastState = mMPState = MPS.IDLE;

		if (isUplayerSupported()) {
			Logger.d(TAG, "UPlayer is supported.");
		} else {
			Logger.d(TAG, "UPlyaer may not be supported.");
		}
	}

	public static MediaPlayerProxy create(Context context, String path,
			SurfaceHolder holder) {
		try {
			sPlayer = new MediaPlayerProxy();
			if (holder != null) {
				sPlayer.setDisplay(holder);
			}
			sPlayer.setDataSource(path);
			sPlayer.prepare();

			return sPlayer;
		} catch (IOException ex) {
			Logger.d(TAG, "failed to create MediaPlayerProxy:", ex);
			// something extra
		} catch (IllegalArgumentException ex) {
			Logger.d(TAG, "failed to create MediaPlayerProxy:", ex);
			// something extra
		} catch (SecurityException ex) {
			Logger.d(TAG, "failed to create MediaPlayerProxy:", ex);
			// something extra
		}
		return null;
	}

	public static boolean isHD2Supported() {
		return freq >= 1200;
	}

	public static boolean supportH265(){
		return isHD2Supported();
	}

	public static int freq;
	private static boolean isCpuinfoReaded = false;
	// 表明是否支持uplayer
	private static boolean isUplayerSupported = false;

	public static boolean isUplayerSupported() {
		if(isCpuinfoReaded)
			return isUplayerSupported;
		isCpuinfoReaded = true;
		Logger.d(TAG, "--------------------------------------------");
		Logger.d(TAG, "CPU_ABI: " + android.os.Build.CPU_ABI);
		Logger.d(TAG, "CPU_ABI2: " + android.os.Build.CPU_ABI2);

		final String ARMV7A = "armeabi-v7a";

		String strLine;
		boolean hasNeon = false;
		if(android.os.Build.CPU_ABI.toLowerCase().equals("x86")){
			isUplayerSupported = true;
			return isUplayerSupported;
		}
		boolean isArmv7a = android.os.Build.CPU_ABI.toLowerCase()
				.equals(ARMV7A);
		int freq = 0;
		int sdkVersion = 0;

		try {
			sdkVersion = Integer.valueOf(android.os.Build.VERSION.SDK);
		} catch (NumberFormatException e) {
			sdkVersion = 0;
		}

		if (sdkVersion < 8) {
			Logger.e(TAG,
					"Android version is less than 2.2, not supported by Uplayer!!");
			return false;
		}

		String cpuInfo = "";
		try {
			BufferedReader cpuinfoReader = new BufferedReader(new FileReader(
					"/proc/cpuinfo"));
			while ((strLine = cpuinfoReader.readLine()) != null) {
				cpuInfo = cpuInfo + strLine + "\n";
				strLine = strLine.toUpperCase();

				if (strLine.startsWith("FEATURES")) {
					int idx = strLine.indexOf(':');
					if (idx != -1) {
						strLine = strLine.substring(idx + 1);
						hasNeon = (strLine.indexOf("NEON") != -1);
					}
				}
			}

			cpuinfoReader.close();
			cpuinfoReader = null;

			cpuinfoReader = new BufferedReader(new FileReader(
					"/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq"));
			strLine = cpuinfoReader.readLine();
			if (strLine != null) {
				strLine = strLine.trim();
				freq = Integer.parseInt(strLine);
				cpuInfo = cpuInfo + "cpu0 max frequency: " + strLine;
			}

			cpuinfoReader.close();

		} catch (IOException e) {
			Logger.e(TAG, e);
		}

		Logger.d(TAG, cpuInfo);

		freq += 9999;
		freq /= 1000;
		MediaPlayerProxy.freq = freq;
		isUplayerSupported = isArmv7a && hasNeon;
		return isUplayerSupported;
	}

	private OnADPlayListener mADPlayListener = new OnADPlayListener() {

		@Override
		public boolean onStartPlayAD(int index) {
			return false;
		}

		@Override
		public boolean onEndPlayAD(int index) {
			return false;
		}

	};

	private OnMidADPlayListener mMidADPlayListener = new OnMidADPlayListener() {

		@Override
		public boolean onStartPlayMidAD(int index) {
			return false;
		}

		@Override
		public boolean onEndPlayMidAD(int index) {
			return false;
		}

		@Override
		public void onLoadingMidADStart() {
			return;
		}
	};

	/**
	 * 设置广告的listener
	 * 
	 * @param listener
	 */
	public void setOnADPlayListener(OnADPlayListener listener) {
		mADPlayListener = listener;
	}

	/**
	 * 设置中插广告的listener
	 * 
	 * @param listener
	 */
	public void setOnMidADPlayListener(OnMidADPlayListener listener) {
		mMidADPlayListener = listener;
	}

	/**
	 * 设置播放进度的listener
	 * 
	 * @param listener
	 */
	public void setOnCurrentPositionUpdateListener(
			OnCurrentPositionUpdateListener listener) {
		mOnCurrentPositionUpdateListener = listener;
	}

	/**
	 * 播放进度的监听
	 */
	private OnCurrentPositionUpdateListener mOnCurrentPositionUpdateListener = new OnCurrentPositionUpdateListener() {

		@Override
		public void onCurrentPositionUpdate(int currentPosition) {
			// TODO Auto-generated method stub
			Logger.d(TAG, "onCurrentPositionUpdate-->" + currentPosition);
		}
	};

	private OnLoadingStatusListener mOnLodingStatusListener = new OnLoadingStatusListener() {

		@Override
		public void onStartLoading() {
			Logger.d(TAG, "onStartLoading-->");
		}

		@Override
		public void onEndLoading() {
			Logger.d(TAG, "onEndLoading-->");
		}
	};

	/**
	 * 设置播放进度的listener
	 * 
	 * @param listener
	 */
	public void setOnLodingStatusListener(OnLoadingStatusListener listener) {
		mOnLodingStatusListener = listener;
	}

	private OnADCountListener mOnADCountListener = new OnADCountListener() {

		@Override
		public void onCountUpdate(int count) {
			Logger.d(TAG, "onCountUpdate-->" + count);
		}
	};

	private OnNetworkSpeedListener mOnNetworkSpeedListener = new OnNetworkSpeedListener() {

		@Override
		public void onSpeedUpdate(int count) {
			Logger.d(TAG, "onSpeedUpdate-->" + count + "kb/s");
		}
	};

    private OnNetworkSpeedPerMinute mOnNetworkSpeedPerMinute = new OnNetworkSpeedPerMinute() {
        @Override
        public void onSpeedUpdate(int speed) {
            Logger.d(TAG, "onSpeedUpdate-->" + speed);
        }
    };

    private OnBufferPercentUpdateListener mOnBufferPercentUpdateListener = new OnBufferPercentUpdateListener() {
        @Override
        public void onPercentUpdate(int percent) {
            Logger.d(TAG, "onPercentUpdate-->" + percent);
        }
    };

	private OnRealVideoStartListener mOnRealVideoStartListener = new OnRealVideoStartListener() {

		@Override
		public void onRealVideoStart() {
			Logger.d(TAG, "onRealVideoStart-->");
		}
	};
	
	private OnVideoIndexUpdateListener mOnVideoIndexUpdateListener = new OnVideoIndexUpdateListener() {

		@Override
		public void onVideoIndexUpdate(int currentIndex, int ip) {
			Logger.d(TAG, "onVideoIndexUpdate--> " + currentIndex + "  " + ip);
		}
	};
	
	private OnTimeoutListener mOnTimeoutListener = new OnTimeoutListener() {
		
		@Override
		public void onTimeOut() {
			Logger.d(TAG, "onTimeOut-->" );
		}
		
		@Override
		public void onNotifyChangeVideoQuality() {
			Logger.d(TAG, "onNotifyChangeVideoQuality--> ");
		}
	};
	
	private OnHwDecodeErrorListener mOnHwDecodeErrorListener = new OnHwDecodeErrorListener() {
		
		@Override
		public void OnHwDecodeError() {
			Logger.d(TAG, "OnHwDecodeError-->");
		}

		@Override
		public void onHwPlayError() {
			Logger.e(TAG, "onHwPlayError-->");
		}
	};

    private OnConnectDelayListener mOnConnectDelayListener = new OnConnectDelayListener() {
        @Override
        public void onVideoConnectDelay(int time) {
            Logger.d(TAG, "onVideoConnectDelay-->" + time);
        }

        @Override
        public void onAdConnectDelay(int time) {
            Logger.d(TAG, "onAdConnectDelay-->" + time);
        }
    };

	private OnQualityChangeListener mOnQualityChangeListener = new OnQualityChangeListener() {
		@Override
		public void onQualityChangeSuccess() {}

		@Override
		public void onQualitySmoothChangeFail() {}
	};

	private OnHttp302DelayListener mOnHttp302DelayListener = new OnHttp302DelayListener() {
		@Override
		public void onVideo302Delay(int time) {
			Logger.d(TAG, "onVideo302Delay-->" + time);
		}

		@Override
		public void onAd302Delay(int time) {
			Logger.d(TAG, "onAd302Delay-->" + time);
		}
	};	/**
	 * 设置清晰度切换listener
	 *
	 * @param listener
	 */
	public void setOnQualityChangeListener(OnQualityChangeListener listener) {
		mOnQualityChangeListener = listener;
	}

	/**
	 * 设置播放进度的listener
	 * 
	 * @param listener
	 */
	public void setOnADCountListener(OnADCountListener listener) {
		mOnADCountListener = listener;
	}

	/**
	 * 设置网络状态的listener
	 * 
	 * @param listener
	 */
	public void setOnNetworkSpeedListener(OnNetworkSpeedListener listener) {
		mOnNetworkSpeedListener = listener;
	}

    public void setOnNetworkSpeedPerMinute(OnNetworkSpeedPerMinute listener) {
        mOnNetworkSpeedPerMinute = listener;
    }

    public void setOnBufferPercentUpdateListener(OnBufferPercentUpdateListener listener) {
        mOnBufferPercentUpdateListener = listener;
    }

	/**
	 * 设置播放正片的listener
	 * 
	 * @param listener
	 */
	public void setOnRealVideoStartListener(OnRealVideoStartListener listener) {
		mOnRealVideoStartListener = listener;
	}
	
	/**
	 * 播放位置变化的listener
	 * 
	 * @param listener
	 */
	public void setOnVideoIndexUpdateListener(
			OnVideoIndexUpdateListener listener) {
		mOnVideoIndexUpdateListener = listener;
	}

	public void setOnTimeoutListener(OnTimeoutListener listener){
		mOnTimeoutListener = listener;
	}
	
	/**
	 * 硬解错误的listener
	 * 
	 * @param listener
	 */
	public void setOnHwDecodeErrorListener(OnHwDecodeErrorListener listener) {
		mOnHwDecodeErrorListener = listener;
	}

    public void setOnConnectDelayListener(OnConnectDelayListener listener){
        mOnConnectDelayListener = listener;
    }
	
	public void setOnHttp302DelayListener(OnHttp302DelayListener listener){
		mOnHttp302DelayListener = listener;
	}
	
	public boolean isUsingUMediaplayer() {
		if (mInnerPlayer != null) {
			if (mInnerPlayer instanceof UMediaPlayer)
				return true;
			else
				return false;
		} else
			return MediaPlayerProxy.isUplayerSupported();
	}
	
	public void setHLS(boolean isHLS){
		this.isHLS = isHLS;
	}
	
	public void setHardwareDecode(boolean useHardwareDecode){
		this.useHardwareDecode = useHardwareDecode;
	}

	public void setDRM(boolean isDRM) {
		this.isDRM = isDRM;
	}
}

class PlayerChooser {
	interface PlayerAlternative {
		boolean rule(String fileName);

		void action();
	}

	private Vector<PlayerAlternative> mDMakers;
	private String fileName;

	public PlayerChooser(String filename) {
		fileName = filename;
		mDMakers = new Vector<PlayerAlternative>();
	}

	public PlayerChooser addAlternative(PlayerAlternative alter) {
		mDMakers.add(alter);
		return this;
	}

	public void decide() {
		for (int i = 0; i < mDMakers.size(); ++i) {
			if (mDMakers.get(i).rule(fileName)) {
				mDMakers.get(i).action();
				return;
			}
		}
	}

}