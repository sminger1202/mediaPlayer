/*
 * 
 * 开发人员：孟令跟
 * 本播放器支持在线分段播放和本地视频播放
 * 
 */
package com.youku.player;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.Build;

import com.youku.player.goplay.Profile;

public class AudioManage {
	/** 声音管理器 */
	private AudioManager mAudioManager;

	public AudioManage(Context context, OnAudioFocusChangeListener l) {
		try {
			mAudioManager = (AudioManager) context
					.getSystemService(Context.AUDIO_SERVICE);
			mAudioManager.setMode(AudioManager.MODE_NORMAL);
			if (Build.VERSION.SDK_INT >= 8) {
				mAudioManager.requestAudioFocus(l, AudioManager.STREAM_MUSIC,
						AudioManager.AUDIOFOCUS_GAIN);// 停掉其他音频软件
			}
		} catch (Exception e) {
		}
	}

	public int getmaxVolume() {
		return mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
	};

	/**
	 * 获取系统音量
	 */
	public int getSound() {
		int nowSound = 0;
		try {
			nowSound = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		} catch (Exception e) {
		}
		return nowSound;
	}

	/**
	 * 释放资源
	 */
	public void destory(OnAudioFocusChangeListener l) {
		if (mAudioManager != null) {
			try {
				if (Profile.API_LEVEL >= 8)
					mAudioManager.abandonAudioFocus(l);
			} catch (Exception e) {
			}
		}
		mAudioManager = null;
	}

	/**
	 * 增大音量，并返回增大后的音量
	 */
	public int addSound(int add) {
		int sou = getSound() + add;
		sou = Math.min(sou, getmaxVolume());
		sou = Math.max(sou, 0);
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, sou, 0);
		return getSound();
	}

	/**
	 * 设置系统音量 (最大值 15)
	 */
	public void setVolume(int sound) {
		try {
			// Logger.d(TAE, " -o");
			sound = Math.max(0, sound);
			sound = Math
					.min(mAudioManager
							.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
							sound);
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, sound, 0);
		} catch (Exception e) {
		}
		// Logger.d(TAE, " -o");
	}

}
