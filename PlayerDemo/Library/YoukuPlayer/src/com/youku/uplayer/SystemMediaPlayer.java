package com.youku.uplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.SurfaceHolder;

import com.baseproject.utils.Logger;
import com.baseproject.utils.Profile;
import com.youku.player.LogTag;

import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class SystemMediaPlayer extends OriginalMediaPlayer {
	private static ReentrantLock mLock = new ReentrantLock();
	private static int RELEASE_TIMEOUT = 10;

	private class PlayListItem {
		public String url;
		public int offset;
		public int duration;
	}

	private class SMPState {
		public static final int NORMAL = 0;
		public static final int CHANGING_VIDEO = 1;
	}

	private int mPlayerState = SMPState.NORMAL;

	private Vector<PlayListItem> mUrlList;
	private int mCurrentItemIndex = 0;
	private int mTotalDurationInMills = 0;
	private int mSeekPositionInMills = 0;
	private int mLastSeekPositionInMills = 0;
	private boolean mNeedAnotherSeek = false;
	private boolean mIsPlaylistPrepared = false;

	private static final int SEEKING_NONE = 0;
	private static final int SEEKING_IN_PROGRESS = 1;
	private static final int SEEKING_DELAYED = 2;
	private int mSeekingState = SEEKING_NONE;

	private MediaPlayer mCurrentPlayer = null;
	private String mPath = null;
	private SurfaceHolder mHolder = null;
	private boolean released;

	// ///////////////////////////////////////////////////////////////////
	// listeners
	private OnBufferingUpdateListener mExternalBufferingUpdateListener = null;
	private OnCompletionListener mExternalCompletionListener = null;
	private OnErrorListener mExternalErrorListener = null;
	private OnInfoListener mExternalInfoListener = null;
	private OnPreparedListener mExternalPreparedListener = null;
	private OnSeekCompleteListener mExternalSeekCompleteListener = null;
	private OnVideoSizeChangedListener mExternalVideoSizeChangedListener = null;

	private static final String TAG = LogTag.TAG_PREFIX + SystemMediaPlayer.class.getSimpleName();

	private void myLogger(String strLog) {
		Logger.d(TAG, strLog + " is called.");
	}

	void setListeners() {

		mCurrentPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
		mCurrentPlayer.setOnCompletionListener(mCompletionListener);
		mCurrentPlayer.setOnErrorListener(mErrorListener);
		mCurrentPlayer.setOnInfoListener(mInfoListener);
		mCurrentPlayer.setOnPreparedListener(mPreparedListener);
		mCurrentPlayer.setOnSeekCompleteListener(mSeekCompleteListener);
		mCurrentPlayer.setOnVideoSizeChangedListener(mVideoSizeChangedListener);
	}

	private void changeVideo() {
		assert mCurrentPlayer != null;

		// mBufferingUpdateListener.onBufferingUpdate(this, 0);
		String strPath = mUrlList.get(mCurrentItemIndex).url;

		try {

			mPlayerState = SMPState.CHANGING_VIDEO;
			mCurrentPlayer.reset();
			mCurrentPlayer.setDataSource(strPath);
			mCurrentPlayer.setDisplay(mHolder);
			mCurrentPlayer.prepareAsync();

			mPlayerState = SMPState.NORMAL;
		} catch (Exception e) {
			Logger.e(TAG, e);
		}

	}

	private OnBufferingUpdateListener mBufferingUpdateListener = new OnBufferingUpdateListener() {
		@Override
		public void onBufferingUpdate(android.media.MediaPlayer mp, int percent) {
			myLogger("onBufferingUpdate ");
			mExternalBufferingUpdateListener.onBufferingUpdate(
					SystemMediaPlayer.this, percent);
		}
	};

	private OnCompletionListener mCompletionListener = new OnCompletionListener() {
		@Override
		public void onCompletion(android.media.MediaPlayer mp) {
			myLogger("onCompletion ");
			mExternalCompletionListener.onCompletion(SystemMediaPlayer.this);
//			if (mp == mCurrentPlayer) {
//				++mCurrentItemIndex;
//				if (mCurrentItemIndex < mUrlList.size()) {
//					mSeekPositionInMills = 0;
//					changeVideo();
//				} else {
//					mExternalCompletionListener
//							.onCompletion(SystemMediaPlayer.this);
//				}
//			}
		}
	};

	private OnErrorListener mErrorListener = new OnErrorListener() {
		@Override
		public boolean onError(android.media.MediaPlayer mp, int what, int extra) {
			myLogger("onError ");
			mExternalErrorListener.onError(SystemMediaPlayer.this, what,
					extra);
			return true;
		}
	};

	private OnInfoListener mInfoListener = new OnInfoListener() {
		@Override
		public boolean onInfo(android.media.MediaPlayer mp, int what, int extra) {
			myLogger("onInfo what:"+what+" extra:"+extra);
			return mExternalInfoListener.onInfo(SystemMediaPlayer.this, what,
					extra);
		}
	};

	private OnPreparedListener mPreparedListener = new OnPreparedListener() {
		@Override
		public void onPrepared(android.media.MediaPlayer mp) {
			myLogger("onPrepared ");
			if (!mIsPlaylistPrepared) {
				mExternalPreparedListener.onPrepared(SystemMediaPlayer.this);
				mIsPlaylistPrepared = true;
			} else {
				if (mSeekingState == SEEKING_DELAYED) {
					mSeekingState = SEEKING_IN_PROGRESS;
					mCurrentPlayer.seekTo(mSeekPositionInMills);
				} else {
					mCurrentPlayer.start();
				}
			}
		}
	};

	private OnSeekCompleteListener mSeekCompleteListener = new OnSeekCompleteListener() {
		@Override
		public void onSeekComplete(android.media.MediaPlayer mp) {
			myLogger("onSeekComplete ");
			mSeekingState = SEEKING_NONE;
			// if (mNeedAnotherSeek) {
			// mNeedAnotherSeek = false;
			// _seekTo(mLastSeekPositionInMills);
			// } else {
			// mCurrentPlayer.start();
			mExternalSeekCompleteListener
					.onSeekComplete(SystemMediaPlayer.this);
			// }
		}
	};

	private OnVideoSizeChangedListener mVideoSizeChangedListener = new OnVideoSizeChangedListener() {
		@Override
		public void onVideoSizeChanged(android.media.MediaPlayer mp, int width,
									   int height) {
			myLogger("onVideoSizeChanged ");
			mExternalVideoSizeChangedListener.onVideoSizeChanged(
					SystemMediaPlayer.this, width, height);
		}
	};

	public SystemMediaPlayer() {
		Logger.d(TAG, "init wait");
		try {
			if (!mLock.tryLock(RELEASE_TIMEOUT, TimeUnit.SECONDS)) {
				throw new ReleaseTimeoutException("SystemMediaPlayer release timeout");
			} else {
				mLock.unlock();
			}
		} catch (InterruptedException e) {
			Logger.e(TAG, e);
		}

		Logger.d(TAG, "init wait over");
		myLogger("SystemMediaPlayer() ");
		mCurrentItemIndex = 0;
		mPlayerState = SMPState.NORMAL;
		mSeekingState = SEEKING_NONE;
		mSeekPositionInMills = 0;
		mLastSeekPositionInMills = 0;
		mNeedAnotherSeek = false;
		mIsPlaylistPrepared = false;

		mCurrentPlayer = new android.media.MediaPlayer();

		setListeners();
	}

	// @Override
	// public void setSurface(Surface surface) {
	// myLogger("setSurface ");
	// mCurrentPlayer.setSurface(surface);
	// }

	@Override
	public void setWakeMode(Context context, int mode) {
		myLogger("setWakeMode ");
		mCurrentPlayer.setWakeMode(context, mode);
	}

	@Override
	public void setScreenOnWhilePlaying(boolean screenOn) {
		myLogger("setScreenOnWhilePlaying ");
		mCurrentPlayer.setScreenOnWhilePlaying(screenOn);
	}

	@Override
	public void setLooping(boolean looping) {
		myLogger("setLooping ");
		mCurrentPlayer.setLooping(looping);
	}

	@Override
	public void setVolume(float leftVolume, float rightVolume) {
		myLogger("setVolume ");
		mCurrentPlayer.setVolume(leftVolume, rightVolume);
	}

	@Override
	public int getCurrentPosition() {
		int pos = 0;
		if (mCurrentPlayer != null)
			pos = mCurrentPlayer.getCurrentPosition();
		myLogger("getCurrentPosition:" + pos);
		return pos;
	}

	@Override
	public int getDuration() {
		myLogger("getDuration ");
//		if (mTotalDurationInMills == -1) {
		// return mCurrentPlayer.getDuration() * 1000;
//		return mCurrentPlayer.getDuration();
//		}
		if (mCurrentPlayer != null)
			return mCurrentPlayer.getDuration();
		else return 0;
	}

	@Override
	public int getVideoHeight() {
		myLogger("getVideoHeight ");
		return mCurrentPlayer.getVideoHeight();
	}

	@Override
	public int getVideoWidth() {
		myLogger("getVideoWidth ");
		return mCurrentPlayer.getVideoWidth();
	}

	@Override
	public boolean isLooping() {
		myLogger("isLooping ");
		return mCurrentPlayer.isLooping();
	}

	@Override
	public boolean isPlaying() {
		try {
			boolean isPlaying = mCurrentPlayer.isPlaying();
			myLogger("isPlaying: " + isPlaying);
			return isPlaying;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public void pause() throws IllegalStateException {
		myLogger("pause ");
		mCurrentPlayer.pause();
	}

	@Override
	public void prepare() throws IOException, IllegalStateException {
		myLogger("prepare ");
		mCurrentPlayer.prepare();
	}

	@Override
	public void prepareAsync() throws IllegalStateException {
		myLogger("prepareAsync ");
		mCurrentPlayer.prepareAsync();
	}

	private void resetData() {
		mPath = null;
		mUrlList = null;
		mCurrentItemIndex = 0;
		mTotalDurationInMills = 0;
		mSeekPositionInMills = 0;
		mLastSeekPositionInMills = 0;
		mNeedAnotherSeek = false;
		mIsPlaylistPrepared = false;

		mPlayerState = SMPState.NORMAL;
		mSeekingState = SEEKING_NONE;
	}

	@Override
	public void release() {
		if (released)
			return;
		released = true;
		new Thread(new Runnable() {

			@Override
			public void run() {
				mLock.lock();
				try {
					myLogger("release ");
					Logger.d(TAG, "start release");
					mCurrentPlayer.setDisplay(null);
					mCurrentPlayer.reset();
					mCurrentPlayer.release();
					Logger.d(TAG, "end release");
					resetData();
					mCurrentPlayer = null;
				} finally {
					mLock.unlock();
				}
			}
		}).start();
	}

	@Override
	public void reset() {
		myLogger("reset ");
		mCurrentPlayer.reset();
		resetData();
	}

	private int calcSeekCoord(int posInMills) {
		int i = 0;
		while (i < mUrlList.size() && mUrlList.get(i).offset < posInMills) {
			++i;
		}

		return i - 1;
	}

	private void _seekTo(int posInMills) {
		mSeekPositionInMills = posInMills;
		mSeekingState = SEEKING_IN_PROGRESS;
		mCurrentPlayer.seekTo(mSeekPositionInMills);
//		int i = mCurrentItemIndex;
//		if (mTotalDurationInMills >= 0 && posInMills >= 0
//				&& posInMills <= mTotalDurationInMills) {
//			i = calcSeekCoord(posInMills);
//		}
//
//		mSeekPositionInMills = posInMills - mUrlList.get(i).offset;
//
//		if (mCurrentItemIndex != i) {
//			mSeekingState = SEEKING_DELAYED;
//			mCurrentItemIndex = i;
//			changeVideo();
//		} else {
//			mSeekingState = SEEKING_IN_PROGRESS;
//			mCurrentPlayer.seekTo(mSeekPositionInMills);
//		}
	}

	@Override
	public void seekTo(int locationInMills) throws IllegalStateException {
		myLogger("seekTo: " + locationInMills);
//		if (mPlayerState == SMPState.CHANGING_VIDEO
//				|| mSeekingState != SEEKING_NONE) {
//			mNeedAnotherSeek = true;
//			mLastSeekPositionInMills = locationInMills;
//		} else {
		_seekTo(locationInMills);
//		}
	}

	@Override
	public void setAudioStreamType(int streamtype) {
		myLogger("setAudioStreamType ");
		mCurrentPlayer.setAudioStreamType(streamtype);
	}

	@Override
	public void setDataSource(String path) throws IOException,
			IllegalArgumentException, IllegalStateException {
		myLogger("setDataSource ");
		mPath = path;
//		mUrlList = new Vector<PlayListItem>();
//
//		String[] strArray = mPath.split("\n");
//		int i = 0, offset = 0;
//		while (i < strArray.length) {
//			String s = strArray[i];
//			if (s.startsWith("#EXTINF:")) {
//				s = s.substring(8);
//				s = s.trim();
//				int d = Integer.parseInt(s);
//				// in milliseconds
//				d *= 1000;
//				++i;
//
//				if (i < strArray.length) {
//					PlayListItem pli = new PlayListItem();
//					pli.duration = d;
//					pli.offset = offset;
//					pli.url = strArray[i].trim();
//
//					mUrlList.add(pli);
//
//					offset += d;
//				}
//			}
//			++i;
//		}
//
//		if (mUrlList.size() < 1) {
//			PlayListItem pli = new PlayListItem();
//			pli.duration = -1;
//			pli.offset = 0;
//			pli.url = mPath;
//			mUrlList.add(pli);
//
//			mTotalDurationInMills = -1;
//		} else {
//			mTotalDurationInMills = offset;
//		}

		mCurrentPlayer.setDataSource(Profile.mContext, Uri.parse(path));
//		mCurrentItemIndex = 0;

//		for (i = 0; i < mUrlList.size(); ++i) {
//			myLogger("No." + i + " offset: " + mUrlList.get(i).offset
//					+ "\tduration: " + mUrlList.get(i).duration);
//		}
	}

	@Override
	public void setDisplay(SurfaceHolder sh) {
		myLogger("setDisplay ");
		mHolder = sh;
		mCurrentPlayer.setDisplay(sh);
	}

	@Override
	public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
		myLogger("setOnBufferingUpdateListener ");
		mExternalBufferingUpdateListener = listener;
	}

	@Override
	public void setOnCompletionListener(OnCompletionListener listener) {
		myLogger("setOnCompletionListener ");
		mExternalCompletionListener = listener;
	}

	@Override
	public void setOnErrorListener(OnErrorListener listener) {
		myLogger("setOnErrorListener ");
		mExternalErrorListener = listener;
	}

	@Override
	public void setOnInfoListener(OnInfoListener listener) {
		myLogger("setOnInfoListener ");
		mExternalInfoListener = listener;
	}

	@Override
	public void setOnPreparedListener(OnPreparedListener listener) {
		myLogger("setOnPreparedListener ");
		mExternalPreparedListener = listener;
	}

	@Override
	public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
		myLogger("setOnSeekCompleteListener ");
		mExternalSeekCompleteListener = listener;
	}

	@Override
	public void setOnVideoSizeChangedListener(
			OnVideoSizeChangedListener listener) {
		myLogger("setOnVideoSizeChangedListener ");
		mExternalVideoSizeChangedListener = listener;
	}

	@Override
	public void start() throws IllegalStateException {
		myLogger("start ");
		mCurrentPlayer.start();
	}

	@Override
	public void stop() throws IllegalStateException {
		myLogger("stop ");
		mCurrentPlayer.stop();
	}

}
