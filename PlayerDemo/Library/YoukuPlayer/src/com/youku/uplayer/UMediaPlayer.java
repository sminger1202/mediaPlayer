package com.youku.uplayer;

import android.content.res.AssetManager;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.baseproject.utils.Logger;
import com.youku.libmanager.FileUtils;
import com.youku.libmanager.SoUpgradeService;
import com.youku.libmanager.SoUpgradeStatics;
import com.youku.player.LogTag;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * file name: UMediaPlayer.java description: this class defines interaction
 * between java UI and native uplayer author: jia lei
 * 
 */

public class UMediaPlayer extends OriginalMediaPlayer {

	// ////////////////////////////////////////////////////////////////////
	// module initialization
	// file name of uplayer library
	private static final String TAG = UMediaPlayer.class.getSimpleName();

	static {
		// add by yujunfeng
		// 如果手机不是armv7-neon的，则从程序安装目录"/data/data/com.youku.ui.CPU架构/"路径下加载ffmpeg解码库
		// 否则正常使用System.loadLibrary("ffmpeg")从"/data/data/com.youku.ui/lib/"文件夹中加载
		// LogOutput.log(TAG, "allSupport = " + Config.allSupport +
		// ", cpuarch = " + Config.cpuarch);
		// if (Config.allSupport == true)

		String ffmpegPath = SoUpgradeStatics
				.getFfmpegSo(com.baseproject.utils.Profile.mContext);
		String uplayer23Path = SoUpgradeStatics
				.getUplayer23So(com.baseproject.utils.Profile.mContext);

		System.loadLibrary("netcache");

		if (FileUtils.isFileExist(ffmpegPath)) {
			Logger.d(SoUpgradeService.TAG, "System.load(" + ffmpegPath + ")");
			System.load(ffmpegPath);
		} else {
			Logger.d(SoUpgradeService.TAG, "System.loadLibrary(uffmpeg)");
			System.loadLibrary("uffmpeg");
		}
		// else
		// System.load("/data/data/com.youku.ui." + Config.cpuarch +
		// "/lib/libffmpeg.so");


			if (FileUtils.isFileExist(uplayer23Path)) {
				Logger.d(SoUpgradeService.TAG, "System.load(" + uplayer23Path
						+ ")");
				System.load(uplayer23Path);
			} else {
				Logger.d(SoUpgradeService.TAG, "System.loadLibrary(uplayer23)");
				System.loadLibrary("uplayer23");
			}
		native_init();
	}

	@SuppressWarnings("deprecation")
	public static int getSDKVersionNumber() {
		int sdkVersion;
		try {
			sdkVersion = Integer.valueOf(android.os.Build.VERSION.SDK);
		} catch (NumberFormatException e) {
			sdkVersion = 0;
		}
		return sdkVersion;
	}

	private class MsgID {
		public static final int MEDIA_NOP = 0;
		public static final int MEDIA_PREPARED = 1;
		public static final int MEDIA_PLAYBACK_COMPLETE = 2;
		public static final int MEDIA_BUFFERING_UPDATE = 3;
		public static final int MEDIA_SEEK_COMPLETE = 4;
		public static final int MEDIA_SET_VIDEO_SIZE = 5;
		public static final int MEDIA_ERROR = 100;
		
		public static final int ERROR_NETWORK_CHECK_10 = 503;

		public static final int MEDIA_INFO_PREPARED = 1000;
		public static final int MEDIA_INFO_COMPLETED = 1001;

		public static final int MEDIA_INFO_START_LOADING = 1003;
		public static final int MEDIA_INFO_END_LOADING = 1004;
		public static final int MEDIA_INFO_SET_VIDEO_SIZE = 1030;
		public static final int MEDIA_INFO_BUFFERING_UPDATE = 1031;
		public static final int MEDIA_INFO_NETWORK_SPEED_UPDATE = 2006;
		
		public static final int MEDIA_INFO_NETWORK_LOW = 1043;

		public static final int MEDIA_INFO_PLAYERROR = MPPErrorCode.MEDIA_INFO_PLAYERROR;
		public static final int MEDIA_INFO_NETWORK_DISSCONNECTED = MPPErrorCode.MEDIA_INFO_NETWORK_DISSCONNECTED;
		public static final int MEDIA_INFO_DATA_SOURCE_ERROR = MPPErrorCode.MEDIA_INFO_DATA_SOURCE_ERROR;
		public static final int MEDIA_INFO_PREPARE_ERROR = MPPErrorCode.MEDIA_INFO_PREPARE_ERROR;
		public static final int MEDIA_INFO_NETWORK_ERROR = MPPErrorCode.MEDIA_INFO_NETWORK_ERROR;
		public static final int MEDIA_INFO_SEEK_ERROR = MPPErrorCode.MEDIA_INFO_SEEK_ERROR;
		public static final int MEDIA_INFO_PREPARE_TIMEOUT_ERROR = MPPErrorCode.MEDIA_INFO_PREPARE_TIMEOUT_ERROR;
		public static final int MEDIA_INFO_PREPARED_AD_CHECK = MPPErrorCode.MEDIA_INFO_PREPARED_AD_CHECK;
		public static final int MEDIA_INFO_PREPARED_MID_AD_CHECK = MPPErrorCode.MEDIA_INFO_PREPARED_MID_AD_CHECK;
		public static final int MEDIA_INFO_MIDAD_DATA_SOURCE_ERROR = MPPErrorCode.MEDIA_INFO_MIDAD_DATA_SOURCE_ERROR;
		public static final int MEDIA_INFO_NETWORK_CHECK = MPPErrorCode.MEDIA_INFO_NETWORK_CHECK;
		public static final int MEDIA_INFO_HW_DECODE_ERROR = 30000; // 初始化时硬解错误
		public static final int MEDIA_INFO_HW_PLAYER_ERROR = 30001; // 播放中硬解错误
		public static final int MEDIA_INFO_AD_HTTP_ERROR_4XX = MPPErrorCode.MEDIA_INFO_AD_HTTP_ERROR_4XX;
		public static final int MEDIA_INFO_VIDEO_HTTP_ERROR_4XX = MPPErrorCode.MEDIA_INFO_VIDEO_HTTP_ERROR_4XX;
		// /////////////////////////////////////////////////
		// message IDs from anroid.media.MediaPlayer
		// public static final int MEIDA_INFO_UNKNOWN = 1;
		// public static final int MEDIA_INFO_VIDEO_TRACK_LAGGING = 700;
		// public static final int MEDIA_INFO_BUFFERING_START = 701;
		// public static final int MEDIA_INFO_BUFFERING_END = 702;
		// public static final int MEDIA_INFO_BAD_INTERLEAVING = 800;
		// public static final int MEDIA_INFO_NOT_SEEKABLE = 801;
		// public static final int MEIDA_INFO_METADATA_UPDATE = 802;

		public static final int MEDIA_INFO_PRE_AD_START = 1011;
		public static final int MEDIA_INFO_PRE_AD_END = 1012;
		public static final int MEDIA_INFO_MID_AD_START = 1013;
		public static final int MEDIA_INFO_MID_AD_END = 1014;
		// public static final int MEDIA_INFO_POST_AD_START = 1015;
		// public static final int MEDIA_INFO_POST_AD_END = 1016;
		public static final int MEDIA_INFO_VIDEO_START = 1017;
		public static final int MEDIA_INFO_VIDEO_END = 1018;
		public static final int MEDIA_INFO_MID_AD_LOADING_START = 1020;
		public static final int MEDIA_INFO_AD_COUNT_DOWN = 1040;
		// 播放点通知
		public static final int MEDIA_INFO_CURRENT_POSITION_UPDATE = 2000;
		// 分片索引和ip通知
		public static final int MEDIA_INFO_INDEX_AND_CDN_IP = 2008;
		// extra information
		public static final int MEDIA_INFO_FRAMERATE_VIDEO = 900;
		public static final int MEDIA_INFO_FRAMERATE_AUDIO = 901;

        // 播放延迟统计
        public static final int MEDIA_INFO_AD_CONNECT_DELAY = 1098;
        public static final int MEDIA_INFO_VIDEO_CONNECT_DELAY = 1099;

        //网速统计通知
        public static final int MEDIA_INFO_NETWORK_SPEED = 2300;
        //缓冲进度通知
        public static final int MEDIA_INFO_BUFFER_PERCENT = 2301;

		// 302跳转时间
		public static final int MEDIA_INFO_HTTP_AD_302_DELAY = 1100;
		public static final int MEDIA_INFO_HTTP_VIDEO_302_DELAY = 1101;
				//清晰度切换成功
		public static final int MEDIA_INFO_SWITCH_FINISH = 1021;
		//清晰度平滑切换失败
		public static final int MEDIA_INFO_SWITCH_FAILED = 1022;
	};

	private int mNativeContext = 0;
	private Surface mSurface = null;
	private SurfaceHolder mSurfaceHolder = null;
	private EventHandler mEventHandler = null;
	private boolean mScreenOnWhilePlaying = false;
	private boolean mStayAwake = false;
	private PowerManager.WakeLock mWakeLock = null;

	public static native void registerAVcodec() throws RuntimeException;

	public static synchronized native int getFileDuration(String filePath)
			throws RuntimeException;

	private static native final void native_init() throws RuntimeException;

	private static native final void native_set_egl_path(String strPath)
			throws RuntimeException;

	private native final void native_setup(Object mediaplayer_this);

	private native final void native_finalize();

	private native int native_suspend_resume(boolean isSuspend);

	public static native int getUplayerVersionCode()
			throws IllegalStateException;

	public static native String getUplayerVersionName()
			throws IllegalStateException;

	public static native int getFFmpegVersionCode()
			throws IllegalStateException;

	public static native String getFFmpegVersionName()
			throws IllegalStateException;

	public static native int getCpuCount() throws IllegalStateException;

	@Override
	public native void prepare() throws IOException, IllegalStateException;

	@Override
	public native void prepareAsync() throws IllegalStateException;

	@Override
	public native void addPostADUrl(String path) throws IOException,
			IllegalArgumentException, SecurityException, IllegalStateException;

	@Override
	public native void setDataSource(String path) throws IOException,
			IllegalArgumentException, SecurityException, IllegalStateException;

	/**
	 * 设置硬解和直播的接口
	 * 
	 * @param path
	 * @param soPath
	 *            so的路径
	 * @param enableHw
	 *            是否强制硬解
	 * @param sysVersion
	 *            系统版本 使用这里边的值{@link #Versions}
	 * @param isHLS
	 * 			  是否是直播
	 * @throws IOException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws IllegalStateException
	 */
	public native void setDataSource(String path, String soPath,
			boolean enableHw, int sysVersion, boolean isHLS) throws IOException,
			IllegalArgumentException, SecurityException, IllegalStateException;

	@Override
	public native int getVideoWidth();

	@Override
	public native int getVideoHeight();

	@Override
	public native boolean isPlaying();

	@Override
	public native void seekTo(int msec) throws IllegalStateException;

	@Override
	public native int getCurrentPosition();

	@Override
	public native int getDuration();

	@Override
	public native void setAudioStreamType(int streamtype);

	private native void _setVideoSurface(Surface surface);

	private native void _start() throws IllegalStateException;

	private native void _stop() throws IllegalStateException;

	private native void _pause() throws IllegalStateException;

	private native void _release();

	private native void _reset();

	public native boolean isSeeking();

	public native void set_timeout(int type, int sec);

	public native void enableVoice(int enable);

	public native int getVoiceStatus();

	public native void setPlayRate(int playRate);

	// yujunfeng
	public native void setHttpUserAgent(String userAgent) throws IOException,
			IllegalArgumentException, SecurityException, IllegalStateException;

	// 0：正向 1：逆90°2：顺90°3：反180°
	public native void setVideoOrientation(int orientation)
			throws IllegalStateException;
	
	/**
	 * 跳过广告
	 */
	public native void skipCurPreAd() throws IllegalStateException;

	// 设置旋转后视频的长宽
	public native void changeVideoSize(int width, int height)
			throws IllegalStateException;

	public native void playMidADConfirm(int videoTime, int adTime)
			throws IllegalStateException;

	public native void setMidADDataSource(String path, String so_path,
			boolean enable_hw, int sys_version, boolean is_hls)
			throws IOException, IllegalArgumentException, SecurityException,
			IllegalStateException;

	public native void prepareMidAD() throws IOException, IllegalStateException;

	public native void setEnhanceMode(boolean isEnhance, float percent);

	public native void setNightMode(float lumRatio, float colorRatio);

	/**
	 * return 0 success, not 0 fail
	 */
	public native int switchDataSource(String url) throws IOException, IllegalArgumentException, SecurityException,
			IllegalStateException;

	public native int screenShotOneFrame(AssetManager assetManager, String outPath, int outWidth, int outHeight,
										  int outFmt, String logoPath, int logoWidth,
										  int logoHeight, int logoLeft, int logoTop);

	private OnADPlayListener mOnADPlayListener;
	private OnMidADPlayListener mOnMidADPlayListener;

	private OnCurrentPositionUpdateListener mOnCurrentPositionUpdateListener;
	private OnVideoIndexUpdateListener mOnVideoIndexUpdateListener;
	private OnLoadingStatusListener mOnLodingStatusListener;
	private OnADCountListener mOnADCountListener;
	private OnNetworkSpeedListener mOnNetworkSpeedListener;

	private OnRealVideoStartListener mOnRealVideoStartListener;
	
	private OnTimeoutListener mOnTimeoutListener;
	
	private OnHwDecodeErrorListener mOnHwDecodeErrorListener;

    private OnConnectDelayListener mOnConnectDelayListener;

	private OnHttp302DelayListener mOnHttp302DelayListener;

	public void setOnHwDecodeErrorListener(OnHwDecodeErrorListener listener){
		mOnHwDecodeErrorListener = listener;
	}

    public void setOnConnectDelayListener(OnConnectDelayListener listener){
        mOnConnectDelayListener = listener;
    }
	
	public void setOnHttp302DelayListener(OnHttp302DelayListener listener){
		mOnHttp302DelayListener = listener;
	}
	
	/**
	 * 设置超时监听接口
	 * @param mOnTimeoutListener
	 */
	public void setmOnTimeoutListener(OnTimeoutListener mOnTimeoutListener) {
		this.mOnTimeoutListener = mOnTimeoutListener;
	}

	/**
	 * 设置广告倒计时的listener
	 * 
	 * @param listener
	 */
	public void setOnADCountListener(OnADCountListener listener) {
		mOnADCountListener = listener;
	}

	/**
	 * 设置网速变化的listener
	 * 
	 * @param listener
	 */
	public void setOnNetworkSpeedListener(OnNetworkSpeedListener listener) {
		mOnNetworkSpeedListener = listener;
	}

	/**
	 * 设置正片开始时的listener
	 * 
	 * @param listener
	 */
	public void setOnRealVideoStartListener(OnRealVideoStartListener listener) {
		mOnRealVideoStartListener = listener;
	}

	/**
	 * 设置播放进度的listener
	 * 
	 * @param listener
	 */
	public void setOnLodingStatusListener(OnLoadingStatusListener listener) {
		mOnLodingStatusListener = listener;
	}

	/**
	 * 设置广告的listener
	 * 
	 * @param listener
	 */
	public void setOnADPlayListener(OnADPlayListener listener) {
		mOnADPlayListener = listener;
	}

	/**
	 * 设置中插广告的listener
	 * 
	 * @param listener
	 */
	public void setOnMidADPlayListener(OnMidADPlayListener listener) {
		mOnMidADPlayListener = listener;
	}

	/**
	 * 设置播放进度更新的listener
	 * 
	 * @param listener
	 */
	public void setOnCurrentPositionUpdateListener(
			OnCurrentPositionUpdateListener listener) {
		mOnCurrentPositionUpdateListener = listener;
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

	private OnPreparedListener mPreparedListener;

	public void setOnPreparedListener(OnPreparedListener listener) {
		mPreparedListener = listener;
	}

	private OnCompletionListener mCompletionListener;

	public void setOnCompletionListener(OnCompletionListener listener) {
		mCompletionListener = listener;
	}

	private OnBufferingUpdateListener mBufferingUpdateListener;

	public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
		mBufferingUpdateListener = listener;
	}

	private OnSeekCompleteListener mSeekCompleteListener;

	public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
		mSeekCompleteListener = listener;
	}

	private OnVideoSizeChangedListener mVideoSizeChangedListener;

	public void setOnVideoSizeChangedListener(
			OnVideoSizeChangedListener listener) {
		mVideoSizeChangedListener = listener;
	}

	private OnErrorListener mErrorListener;

	public void setOnErrorListener(OnErrorListener listener) {
		mErrorListener = listener;
	}

	private OnInfoListener mInfoListener;

	public void setOnInfoListener(OnInfoListener listener) {
		mInfoListener = listener;
	}

	private OnQualityChangeListener mOnQualityChangeListener;
	public void setOnQualityChangeListener(OnQualityChangeListener listener) {
		mOnQualityChangeListener = listener;
	}


	public UMediaPlayer() {
		Looper looper = Looper.getMainLooper();
		if (looper != null) {
			mEventHandler = new EventHandler(this, looper);
		} else {
			mEventHandler = null;
		}

		// 注释后无法播放
		try {
			RegisterCodec recodec = new RegisterCodec();
		} catch (Exception e) {
			Logger.d(TAG, "Error: failed to register codecs!");
		}

		native_setup(new WeakReference<MediaPlayer>(this));
	}

	@Override
	public void setDisplay(SurfaceHolder sh) {
		mSurfaceHolder = sh;
		EGLUtil.setSurfaceHolder(mSurfaceHolder);

		if (mSurfaceHolder != null) {
			mSurfaceHolder.setFormat(PixelFormat.RGB_565);
			mSurface = mSurfaceHolder.getSurface();
		} else {
			mSurface = null;
		}

		_setVideoSurface(mSurface);
		updateSurfaceScreenOn();
	}

	private void updateSurfaceScreenOn() {
		if (mSurfaceHolder != null) {
			mSurfaceHolder.setKeepScreenOn(mScreenOnWhilePlaying && mStayAwake);
		}
	}

	@Override
	public void start() throws IllegalStateException {
		stayAwake(true);
		_start();
	}

	@Override
	public void stop() throws IllegalStateException {
		stayAwake(false);
		_stop();
	}

	@Override
	public void pause() throws IllegalStateException {
		stayAwake(false);
		_pause();
	}

	@Override
	public void setScreenOnWhilePlaying(boolean screenOn) {
		if (mScreenOnWhilePlaying != screenOn) {
			mScreenOnWhilePlaying = screenOn;
			updateSurfaceScreenOn();
		}
	}

	@Override
	public void setTimeout(int type, int sec) {
		set_timeout(type, sec);
	}

	@Override
	public void release() {

		stayAwake(false);
		updateSurfaceScreenOn();
		this.mPreparedListener = null;
		this.mBufferingUpdateListener = null;
		this.mCompletionListener = null;
		this.mSeekCompleteListener = null;
		this.mErrorListener = null;
		this.mVideoSizeChangedListener = null;
		this.mInfoListener = null;

		_release();
	}

	@Override
	public void reset() {
		stayAwake(false);
		_reset();
	}

	@Override
	protected void finalize() {
		native_finalize();
	}

	private void stayAwake(boolean awake) {
		// 注释后无法播放
		if (mWakeLock != null) {
			if (awake && (!mWakeLock.isHeld())) {
				mWakeLock.acquire();
			} else if ((!awake) && mWakeLock.isHeld()) {
				mWakeLock.release();
			}
		}
	}

	int whyStartTwice = 1;

	private class EventHandler extends Handler {

		UMediaPlayer mp = null;

		public EventHandler(UMediaPlayer mp, Looper looper) {
			super(looper);
			this.mp = mp;
		}

		public void handleMessage(Message msg) {
			if (mp == null || mp.mNativeContext == 0) {
				Logger.d(TAG, "mediaplayer went away with unhandled events");
				return;
			}

//			 Logger.d(TAG, "底层发送数据" +
//			 msg.what+" 参数arg1:"+msg.arg1+" 参数arg2:"+msg.arg2);

			switch (msg.what) {
                // 准备
                case MsgID.MEDIA_PREPARED:
                    if (mPreparedListener != null) {
                        mPreparedListener.onPrepared(mp);
                    }
                    break;
                // 播放完成 改用MEDIA_INFO_VIDEO_END
                // case MsgID.MEDIA_PLAYBACK_COMPLETE:
                // Logger.d(TAG, "onCompletionListener MEDIA_PLAYBACK_COMPLETE");
                // if (mCompletionListener != null) {
                // mCompletionListener.onCompletion(mp);
                // }
                // break;
                // 缓存中
                case MsgID.MEDIA_BUFFERING_UPDATE:
                    Logger.d(TAG, "MEDIA_BUFFERING_UPDATE: " + msg.arg1);
                    if (mBufferingUpdateListener != null) {
                        mBufferingUpdateListener.onBufferingUpdate(mp, msg.arg1);
                    }
                    break;
                // seek完成
                case MsgID.MEDIA_SEEK_COMPLETE:
                    if (mSeekCompleteListener != null) {
                        mSeekCompleteListener.onSeekComplete(mp);
                    }
                    break;
                // 设置视频size变化
                case MsgID.MEDIA_SET_VIDEO_SIZE:
                    if (mVideoSizeChangedListener != null) {
                        mVideoSizeChangedListener.onVideoSizeChanged(mp, msg.arg1,
                                msg.arg2);
                    }
                    break;
                // 视频错误信息
                case MsgID.MEDIA_ERROR:
                    Logger.d(LogTag.TAG_PLAYER, "Error: (arg1 = " + msg.arg1 + ", arg2 = "
                            + msg.arg2 + ")");
                    boolean handled = false;
                    if (mErrorListener != null) {
                        handled = mErrorListener.onError(mp, msg.arg1, msg.arg2);
                    }

                    if (!handled) {
                        Logger.e(TAG, "error MsgID.MEDIA_ERROR 错误为处理  what:"
                                + msg.what + " msg.arg1:" + msg.arg1 + " msg.arg2:"
                                + msg.arg2);
                    }
                    break;
                case MsgID.MEDIA_INFO_START_LOADING:
                    if (mOnLodingStatusListener != null) {
                        mOnLodingStatusListener.onStartLoading();
                    }
                    break;
                case MsgID.MEDIA_INFO_END_LOADING:
                    if (mOnLodingStatusListener != null) {
                        mOnLodingStatusListener.onEndLoading();
                    }
                    break;
                case MsgID.MEDIA_INFO_PRE_AD_START:
                    if (mOnADPlayListener != null) {
                        mOnADPlayListener.onStartPlayAD(msg.arg1);
                    }
                    break;
                case MsgID.MEDIA_INFO_PRE_AD_END:
                    Logger.d(TAG, ">>MEDIA_INFO_PRE_AD_END is received");
                    if (mOnADPlayListener != null) {
                        mOnADPlayListener.onEndPlayAD(msg.arg1);
                    }
                    break;
                case MsgID.MEDIA_INFO_MID_AD_START:
                    if (mOnMidADPlayListener != null) {
                        mOnMidADPlayListener.onStartPlayMidAD(msg.arg1);
                    }
                    break;
                case MsgID.MEDIA_INFO_MID_AD_END:
                    if (mOnMidADPlayListener != null) {
                        mOnMidADPlayListener.onEndPlayMidAD(msg.arg1);
                    }
                    break;
                case MsgID.MEDIA_INFO_MID_AD_LOADING_START:
                    if (mOnMidADPlayListener != null) {
                        mOnMidADPlayListener.onLoadingMidADStart();
                    }
                    break;
                case MsgID.MEDIA_NOP:
                    break;

                case MsgID.MEDIA_INFO_CURRENT_POSITION_UPDATE:
                    // 当前位置
                    if (mOnCurrentPositionUpdateListener != null) {
                        mOnCurrentPositionUpdateListener
                                .onCurrentPositionUpdate(msg.arg1);
                    }
                    break;
                case MsgID.MEDIA_INFO_INDEX_AND_CDN_IP:
                    if (mOnVideoIndexUpdateListener != null) {
                        mOnVideoIndexUpdateListener.onVideoIndexUpdate(msg.arg1,
                                msg.arg2);
                    }
                    break;
                case MsgID.MEDIA_INFO_NETWORK_SPEED_UPDATE:
                    Logger.d(TAG, "MEDIA_INFO_NETWORK_SPEED_UPDATE is received"
                            + msg.arg1);
                    if (mOnNetworkSpeedListener != null) {
                        mOnNetworkSpeedListener.onSpeedUpdate(msg.arg1);
                    }
                    break;
                // 播放正片开始
                case MsgID.MEDIA_INFO_VIDEO_START:
                    Logger.d(TAG, ">>MEDIA_INFO_VIDEO_START is received");
                    if (mOnRealVideoStartListener != null) {
                        mOnRealVideoStartListener.onRealVideoStart();
                    }
                    break;
                case MsgID.MEDIA_INFO_VIDEO_END:
                    Logger.d(TAG, "MEDIA_INFO_VIDEO_END is received");
                    if (mCompletionListener != null) {
                        mCompletionListener.onCompletion(mp);
                    }
                    break;

                case MsgID.MEDIA_INFO_AD_COUNT_DOWN:
                    if (mOnADCountListener != null) {
                        mOnADCountListener.onCountUpdate(msg.arg1);
                    }
                    break;

                case MsgID.ERROR_NETWORK_CHECK_10:
                    Logger.d(TAG, ">>ERROR_NETWORK_CHECK_10 is received");
                    if (mOnTimeoutListener != null) {
                        mOnTimeoutListener.onNotifyChangeVideoQuality();
                    }
                    break;
                case MsgID.MEDIA_INFO_NETWORK_LOW:
                    Logger.d(TAG, ">>MEDIA_INFO_NETWORK_LOW is received");
                    if (mOnTimeoutListener != null) {
                        mOnTimeoutListener.onNotifyChangeVideoQuality();
                    }
                    break;
                case MsgID.MEDIA_INFO_HW_DECODE_ERROR:
                    Logger.d(TAG, "MEDIA_INFO_HW_DECODE_ERROR is received");
                    if (mOnHwDecodeErrorListener != null) {
                        mOnHwDecodeErrorListener.OnHwDecodeError();
                    }
                    break;
                case MsgID.MEDIA_INFO_HW_PLAYER_ERROR:
                    Logger.d(TAG, "MEDIA_INFO_HW_PLAYER_ERROR is received");
                    if (mOnHwDecodeErrorListener != null) {
                        mOnHwDecodeErrorListener.onHwPlayError();
                    }
                    break;
                case MsgID.MEDIA_INFO_AD_CONNECT_DELAY:
                    Logger.d(TAG, "MEDIA_INFO_AD_CONNECT_DELAY is received");
                    if (mOnConnectDelayListener != null)
                        mOnConnectDelayListener.onAdConnectDelay(msg.arg1);
                    break;
                case MsgID.MEDIA_INFO_VIDEO_CONNECT_DELAY:
                    Logger.d(TAG, "MEDIA_INFO_AD_CONNECT_DELAY is received");
                    if (mOnConnectDelayListener != null)
                        mOnConnectDelayListener.onVideoConnectDelay(msg.arg1);
                    break;
                case MsgID.MEDIA_INFO_NETWORK_SPEED:
                    Logger.d(TAG, "MEDIA_INFO_NETWORK_SPEED is received " + msg.arg1);
                    if (mOnNetworkSpeedPerMinute != null)
                        mOnNetworkSpeedPerMinute.onSpeedUpdate(msg.arg1);
                    break;
                case MsgID.MEDIA_INFO_BUFFER_PERCENT:
                    Logger.d(TAG, "MEDIA_INFO_BUFFER_PERCENT is received " + msg.arg1);
                    if (mOnBufferPercentUpdateListener != null)
                        mOnBufferPercentUpdateListener.onPercentUpdate(msg.arg1);
                    break;
				case MsgID.MEDIA_INFO_HTTP_AD_302_DELAY:
					Logger.d(TAG, "MEDIA_INFO_HTTP_AD_302_DELAY is received " + msg.arg1);
					if (mOnHttp302DelayListener != null)
						mOnHttp302DelayListener.onAd302Delay(msg.arg1);
					break;
				case MsgID.MEDIA_INFO_HTTP_VIDEO_302_DELAY:
					Logger.d(TAG, "MEDIA_INFO_HTTP_VIDEO_302_DELAY is received " + msg.arg1);
					if (mOnHttp302DelayListener != null)
						mOnHttp302DelayListener.onVideo302Delay(msg.arg1);
					break;
				case MsgID.MEDIA_INFO_SWITCH_FAILED:
					Logger.d(TAG, "MEDIA_INFO_SWITCH_FAILED is received");
					if (mOnQualityChangeListener != null) {
						mOnQualityChangeListener.onQualitySmoothChangeFail();
					}
					break;
				case MsgID.MEDIA_INFO_SWITCH_FINISH:
					Logger.d(TAG, "MEDIA_INFO_SWITCH_FINISH is received");
					if (mOnQualityChangeListener != null) {
						mOnQualityChangeListener.onQualityChangeSuccess();
					}
					break;
				default:
                    Logger.e(TAG, "Unknown message type " + msg.what);
                    break;
            }
		}
	}

	private static void postEventFromNative(Object mediaplayer_ref, int what,
			int arg1, int arg2, Object obj) {
		@SuppressWarnings("unchecked")
		UMediaPlayer mp = ((WeakReference<UMediaPlayer>) mediaplayer_ref).get();
		if (mp == null) {
			return;
		}

        if (mp.mEventHandler != null) {
			Message m = mp.mEventHandler.obtainMessage(what, arg1, arg2, obj);
			switch (m.what) {
			case MsgID.MEDIA_INFO_FRAMERATE_VIDEO:
				Logger.d(TAG, "Video frame rate: " + arg1);
				break;
			case MsgID.MEDIA_INFO_FRAMERATE_AUDIO:
				Logger.d(TAG, "Audio frame rate: " + arg1);
				break;

			case MsgID.MEDIA_INFO_PREPARED:
				Logger.d(TAG, "MEDIA_INFO_PREPARED is received");
				m.what = MsgID.MEDIA_PREPARED;
				break;
			case MsgID.MEDIA_INFO_COMPLETED:
				Logger.d(TAG, "MEDIA_INFO_COMPLETED is received");
				m.what = MsgID.MEDIA_PLAYBACK_COMPLETE;
				break;

			case MsgID.MEDIA_INFO_DATA_SOURCE_ERROR:
				Logger.d(TAG, "MEDIA_INFO_DATA_SOURCE_ERROR is received");
				m.what = MsgID.MEDIA_ERROR;
				m.arg1 = MsgID.MEDIA_INFO_DATA_SOURCE_ERROR;
                m.arg2 = arg1;
				break;

			case MsgID.MEDIA_INFO_PREPARE_ERROR:
				Logger.d(TAG, "MEDIA_INFO_PREPARE_ERROR is received");
				m.what = MsgID.MEDIA_ERROR;
				m.arg1 = MsgID.MEDIA_INFO_PREPARE_ERROR;
                m.arg2 = arg1;
				break;

			case MsgID.MEDIA_INFO_NETWORK_ERROR:
				Logger.d(TAG, "MEDIA_INFO_NETWORK_ERROR is received");
				m.what = MsgID.MEDIA_ERROR;
				m.arg1 = MsgID.MEDIA_INFO_NETWORK_ERROR;
                m.arg2 = arg1;
				break;

			case MsgID.MEDIA_INFO_NETWORK_DISSCONNECTED:
				Logger.d(TAG, "MEDIA_INFO_NETWORK_DISSCONNECTED is received");
				m.what = MsgID.MEDIA_ERROR;
				m.arg1 = MsgID.MEDIA_INFO_NETWORK_DISSCONNECTED;
                m.arg2 = arg1;
				break;
			case MsgID.MEDIA_INFO_SEEK_ERROR:
				Logger.d(TAG, "MEDIA_INFO_SEEK_ERROR is received");
				m.what = MsgID.MEDIA_ERROR;
				m.arg1 = MsgID.MEDIA_INFO_SEEK_ERROR;
                m.arg2 = arg1;
				break;
			case MsgID.MEDIA_INFO_PREPARE_TIMEOUT_ERROR:
				Logger.d(TAG, "MEDIA_INFO_PREPARE_TIMEOUT_ERROR is received");
				m.what = MsgID.MEDIA_ERROR;
				m.arg1 = MsgID.MEDIA_INFO_PREPARE_TIMEOUT_ERROR;
                m.arg2 = arg1;
				break;
				case MsgID.MEDIA_INFO_AD_HTTP_ERROR_4XX:
					Logger.d(TAG, "MEDIA_INFO_AD_HTTP_ERROR_4XX is received");
					m.what = MsgID.MEDIA_ERROR;
					m.arg1 = MsgID.MEDIA_INFO_AD_HTTP_ERROR_4XX;
					m.arg2 = arg1;
					break;
				case MsgID.MEDIA_INFO_VIDEO_HTTP_ERROR_4XX:
					Logger.d(TAG, "MEDIA_INFO_VIDEO_HTTP_ERROR_4XX is received");
					m.what = MsgID.MEDIA_ERROR;
					m.arg1 = MsgID.MEDIA_INFO_VIDEO_HTTP_ERROR_4XX;
					m.arg2 = arg1;
					break;
			case MsgID.MEDIA_INFO_SET_VIDEO_SIZE:
				Logger.d(TAG, "MEDIA_INFO_SET_VIDEO_SIZE is received");
				m.what = MsgID.MEDIA_SET_VIDEO_SIZE;
				m.arg1 = arg1;
				m.arg2 = arg2;
				break;
			case MsgID.MEDIA_INFO_BUFFERING_UPDATE:
				Logger.d(TAG, "MEDIA_INFO_BUFFERING_UPDATE is received");
				m.what = MsgID.MEDIA_BUFFERING_UPDATE;
				m.arg1 = arg1;
				m.arg2 = arg2;
				break;
			case MsgID.MEDIA_INFO_NETWORK_SPEED_UPDATE:
				Logger.d(TAG, "MEDIA_INFO_NETWORK_SPEED_UPDATE is received");
				m.what = MsgID.MEDIA_INFO_NETWORK_SPEED_UPDATE;
				break;
			case MsgID.MEDIA_INFO_PRE_AD_START:
				Logger.d(TAG, "MEDIA_INFO_PRE_AD_START is received");
				m.what = MsgID.MEDIA_INFO_PRE_AD_START;
				m.arg1 = arg1;
				break;
			case MsgID.MEDIA_INFO_PRE_AD_END:
				Logger.d(TAG, "MEDIA_INFO_PRE_AD_END is received");
				m.what = MsgID.MEDIA_INFO_PRE_AD_END;
				m.arg1 = arg1;
				break;

			case MsgID.MEDIA_INFO_CURRENT_POSITION_UPDATE:
				m.what = MsgID.MEDIA_INFO_CURRENT_POSITION_UPDATE;
				break;

			case MsgID.MEDIA_INFO_VIDEO_START:
				m.what = MsgID.MEDIA_INFO_VIDEO_START;
				break;

			case MsgID.MEDIA_INFO_VIDEO_END:
				m.what = MsgID.MEDIA_INFO_VIDEO_END;
				break;

			case MsgID.MEDIA_INFO_AD_COUNT_DOWN:
				Logger.d(TAG, "MEDIA_INFO_AD_COUNT_DOWN is received");
				m.what = MsgID.MEDIA_INFO_AD_COUNT_DOWN;
				break;
			case MsgID.MEDIA_INFO_PREPARED_AD_CHECK:
				Logger.d(TAG, "MEDIA_INFO_PREPARED_AD_CHECK is received");
				m.what = MsgID.MEDIA_ERROR;
				m.arg1 = MsgID.MEDIA_INFO_PREPARED_AD_CHECK;
                m.arg2 = arg1;
				break;
			case MsgID.MEDIA_INFO_PREPARED_MID_AD_CHECK:
				Logger.d(TAG, "MEDIA_INFO_PREPARED_MID_AD_CHECK is received");
				m.what = MsgID.MEDIA_ERROR;
				m.arg1 = MsgID.MEDIA_INFO_PREPARED_MID_AD_CHECK;
                m.arg2 = arg1;
				break;
			case MsgID.MEDIA_INFO_MIDAD_DATA_SOURCE_ERROR:
				Logger.d(TAG, "MEDIA_INFO_MIDAD_DATA_SOURCE_ERROR is received");
				m.what = MsgID.MEDIA_ERROR;
				m.arg1 = MsgID.MEDIA_INFO_MIDAD_DATA_SOURCE_ERROR;
                m.arg2 = arg1;
				break;
			case MsgID.MEDIA_INFO_PLAYERROR:
				Logger.d(TAG, "MEDIA_INFO_PLAYERROR is received");
				m.what = MsgID.MEDIA_ERROR;
				m.arg1 = MsgID.MEDIA_INFO_PLAYERROR;
                m.arg2 = arg1;
				break;
			case MsgID.MEDIA_INFO_NETWORK_CHECK:
				Logger.d(TAG, "MEDIA_INFO_NETWORK_CHECK is received");
				m.what = MsgID.MEDIA_ERROR;
				m.arg1 = MsgID.MEDIA_INFO_NETWORK_CHECK;
                m.arg2 = arg1;
				break;
			case MsgID.MEDIA_INFO_NETWORK_LOW:
				m.what = MsgID.MEDIA_INFO_NETWORK_LOW;
				break;
			case MsgID.MEDIA_INFO_HW_DECODE_ERROR:
				Logger.d(TAG, "MEDIA_INFO_HW_DECODE_ERROR is received");
				break;
			case MsgID.MEDIA_INFO_HW_PLAYER_ERROR:
				Logger.d(TAG, "MEDIA_INFO_HW_PLAYER_ERROR is received");
				break;
			}

			mp.mEventHandler.sendMessage(m);
		}
	}

	public void setSurfaceHolder(SurfaceHolder mSurfaceHolder) {
		this.mSurfaceHolder = mSurfaceHolder;
	}

	public native int getSoVersion() throws IllegalStateException;
	
	/**
	 * @brief Android操作系统版本号
	 */
	public static int[] Versions = new int[] { 
		1000, /* 默认 */
	1022, /* Android 2.2 */
	1023, /* Android 2.3 */
	1030, /* Android 3.0 */
	1040, /* Android 4.0 */
	1041, /* Android 4.1 */
	1042, /* Android 4.2 */
	1043, /* Android 4.3 */
	1044, /* Android 4.4 */
	1050, /* Android LOLLIPOP */
	1099 };
}